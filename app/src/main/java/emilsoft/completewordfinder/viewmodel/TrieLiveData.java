package emilsoft.completewordfinder.viewmodel;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;

import emilsoft.completewordfinder.MainActivity;
import emilsoft.completewordfinder.trie.DoubleArrayTrie;
import emilsoft.completewordfinder.utils.Dictionary;

public class TrieLiveData extends LiveData<DoubleArrayTrie> {

    private TrieViewModel.InternalMaxWordLengthListener listener;

    public TrieLiveData(Context context, Dictionary dictionary, TrieViewModel.InternalMaxWordLengthListener listener) {
        this.listener = listener;
        createTrie(context, dictionary, false);
        Log.v(MainActivity.TAG, "TrieLiveData() called");
    }

    public void setMaxWordLength(int maxWordLength) {
        if(listener != null)
            listener.onGetMaxWordLength(maxWordLength);
    }

    public void createTrie(Context context, Dictionary dictionary, boolean isDictionaryLanguageChanged) {
        CreateTrie.CreateTrieTaskListener createTrieTaskListener = (trie, maxWordLength) -> {
            setValue(trie);
            if(maxWordLength != 0)
                setMaxWordLength(maxWordLength);
        };
        CreateTrie task = new CreateTrie(context, dictionary, createTrieTaskListener);
        task.execute(isDictionaryLanguageChanged);
    }

    public static class CreateTrie extends AsyncTask<Boolean, Void, DoubleArrayTrie> {

        private WeakReference<Context> context;
        private CreateTrieTaskListener listener;
        private Dictionary dictionary;
        private int maxWordLength;

        public CreateTrie(Context context, Dictionary dictionary, CreateTrieTaskListener listener) {
            this.context = new WeakReference<>(context);
            this.dictionary = dictionary;
            this.listener = listener;
            maxWordLength = 0;
        }

        @Override
        protected DoubleArrayTrie doInBackground(Boolean... booleans) {
            //String filename = strings[0];
            boolean isDictionaryLanguageChanged = booleans[0];
            String filename = dictionary.getFilename();
            DoubleArrayTrie trie = new DoubleArrayTrie(dictionary.getAlphabetSize());

            try {
                // If trie already exists, read it
                if (!isDictionaryLanguageChanged && fileExists(context.get(), MainActivity.TRIE_FILENAME)) {
                    FileInputStream fis = context.get().openFileInput(MainActivity.TRIE_FILENAME);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    Log.v(MainActivity.TAG, "Reading trie from file");
                    trie = (DoubleArrayTrie) ois.readObject();
                    Log.v(MainActivity.TAG, "Finished reading trie from file");
                    ois.close();
                    fis.close();
                } else {
                    //Read dictionary file
                    BufferedReader reader = new BufferedReader(new InputStreamReader(context.get().getAssets().open(filename))); //context or getApplication() ?
                    String line;
                    Log.v(MainActivity.TAG, "Beginning trie insertions");
                    long start = System.nanoTime();
                    while ((line = reader.readLine()) != null) {
                        if(line.length() > maxWordLength)
                            maxWordLength = line.length();
                        trie.insert(line); //Should add .toLowerCase()?
                    }
                    long stop = System.nanoTime();
                    Log.v(MainActivity.TAG,"Trie insertion time: "+(((stop-start)/(double)1000000))+" ms");
                    reader.close();

                    //Write trie to file. So we skip to build it every time
                    FileOutputStream fos = context.get().openFileOutput(MainActivity.TRIE_FILENAME, Context.MODE_PRIVATE);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(trie);
                    oos.close();
                    fos.close();

                    Log.v(MainActivity.TAG, "Finished writing trie to file");
                }
                return trie;
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.v(MainActivity.TAG, "exception while reading/writing trie file");
                return null;
            }
        }

        @Override
        protected void onPostExecute(DoubleArrayTrie trie) {
            if(maxWordLength != 0) {
                dictionary.setMaxWordLength(maxWordLength);
            }
            if(listener != null)
                listener.onTrieCreate(trie, maxWordLength);
            Log.v(MainActivity.TAG, "setValue(trie) called in onPostExecute of CreateTrie");
        }

        public interface CreateTrieTaskListener {

            void onTrieCreate(DoubleArrayTrie trie, int maxWordLength);

        }
    }

    private static boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        Log.v(MainActivity.TAG, "File Length = "+file.length());
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }

}
