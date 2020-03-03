package emilsoft.completewordfinder.viewmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.FileUtils;
import android.util.Log;

import androidx.core.content.ContentResolverCompat;
import androidx.lifecycle.LiveData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.file.Files;

import emilsoft.completewordfinder.MainActivity;
import emilsoft.completewordfinder.R;
import emilsoft.completewordfinder.trie.DoubleArrayTrie;
import emilsoft.completewordfinder.utils.Dictionaries;
import emilsoft.completewordfinder.utils.Dictionary;
import emilsoft.completewordfinder.utils.ZipUtils;

public class TrieLiveData extends LiveData<DoubleArrayTrie> {

    private TrieViewModel.InternalMaxWordLengthListener listener;
    private OnCreateTrieListener createTrieListener;
    private DictionaryUnzipListener dictionaryUnzipListener;

    public TrieLiveData(Context context, Dictionary dictionary, boolean isTrieVersionChanged,
                        TrieViewModel.InternalMaxWordLengthListener internalMaxWordLengthListener,
                        OnCreateTrieListener createTrieListener,
                        DictionaryUnzipListener dictionaryUnzipListener) {
        this.listener = internalMaxWordLengthListener;
        this.createTrieListener = createTrieListener;
        this.dictionaryUnzipListener = dictionaryUnzipListener;
        createTrie(context, dictionary, false, isTrieVersionChanged);
    }

    public void setMaxWordLength(int maxWordLength) {
        if(listener != null)
            listener.onGetMaxWordLength(maxWordLength);
    }

    private CreateTrie.CreateTrieTaskListener createTrieTaskListener = (trie, maxWordLength) -> {
        setValue(trie);
        if(maxWordLength != 0)
            setMaxWordLength(maxWordLength);
        if(createTrieListener != null)
            createTrieListener.onCreateTrie();
    };

    public void createTrie(Context context, Dictionary dictionary, boolean isDictionaryLanguageChanged,
                           boolean isTrieVersionChanged) {
        CreateTrie task = new CreateTrie(context, dictionary, isDictionaryLanguageChanged,
                isTrieVersionChanged, createTrieTaskListener, dictionaryUnzipListener);
        task.execute();
    }

    public static class CreateTrie extends AsyncTask<Void, Void, DoubleArrayTrie> {

        private WeakReference<Context> context;
        private CreateTrieTaskListener listener;
        private DictionaryUnzipListener dictionaryUnzipListener;
        private Dictionary dictionary;
        private int maxWordLength;
        private boolean isDictionaryLanguageChanged;
        private boolean isTrieVersionChanged;

        public CreateTrie(Context context, Dictionary dictionary, boolean isDictionaryLanguageChanged,
                          boolean isTrieVersionChanged, CreateTrieTaskListener listener,
                          DictionaryUnzipListener dictionaryUnzipListener) {
            this.context = new WeakReference<>(context);
            this.dictionary = dictionary;
            this.isDictionaryLanguageChanged = isDictionaryLanguageChanged;
            this.isTrieVersionChanged = isTrieVersionChanged;
            this.listener = listener;
            this.dictionaryUnzipListener = dictionaryUnzipListener;
            maxWordLength = 0;
        }

        @Override
        protected DoubleArrayTrie doInBackground(Void... voids) {
            String filename = dictionary.getFilename();
            DoubleArrayTrie trie = new DoubleArrayTrie(dictionary.getAlphabetSize());

            try {
                // If trie already exists, read it
                if (!isDictionaryLanguageChanged && !isTrieVersionChanged && ZipUtils.fileExists(context.get(), MainActivity.TRIE_FILENAME)) {
                    FileInputStream fis = context.get().openFileInput(MainActivity.TRIE_FILENAME);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    //Log.v(MainActivity.TAG, "Reading trie from file");
                    trie = (DoubleArrayTrie) ois.readObject();
                    //Log.v(MainActivity.TAG, "Finished reading trie from file");
                    ois.close();
                    fis.close();
                } else {
                    //Read dictionary file
                    if (isDictionaryLanguageChanged) {
                        // Delete previous dictionary file
                        String[] files = context.get().fileList();
                        if (files != null) {
                            for (String file : files)
                                if (file.endsWith(".txt")) // do not delete trie.dat
                                    context.get().deleteFile(file);
                        }
                    }

                    String txtFile = Dictionaries.getTxtFilenameFromZipFilename(filename);
                    if (!ZipUtils.fileExists(context.get(), txtFile)) {
                        InputStream zipFileStream = context.get().getAssets().open(filename);
                        ZipUtils.unzipToInternalStorage(zipFileStream, context.get().openFileOutput(txtFile, Context.MODE_PRIVATE));
                        if (dictionaryUnzipListener != null)
                            dictionaryUnzipListener.onDictionaryUnzip();
                    }

//                    String[] files = context.get().fileList();
//                    for (String file : files)
//                        Log.v(MainActivity.TAG, "Files after unzip: "+file);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(context.get().openFileInput(txtFile)));
                    String line;
                    //Log.v(MainActivity.TAG, "Beginning trie insertions");
                    //long start = System.nanoTime();
                    while ((line = reader.readLine()) != null) {
                        if(line.length() > maxWordLength)
                            maxWordLength = line.length();
                        trie.insert(line.toLowerCase());
                    }
                    //long stop = System.nanoTime();
                    //Log.v(MainActivity.TAG,"Trie insertion time: "+(((stop-start)/(double)1000000))+" ms");
                    reader.close();

                    //Write trie to file. So we skip to build it every time
                    FileOutputStream fos = context.get().openFileOutput(MainActivity.TRIE_FILENAME, Context.MODE_PRIVATE);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(trie);
                    oos.close();
                    fos.close();

//                    Log.v(MainActivity.TAG, "Finished writing trie to file");
                }
                return trie;
            } catch (Exception ex) {
                ex.printStackTrace();
                //Log.v(MainActivity.TAG, "exception while reading/writing trie file");
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
            //Log.v(MainActivity.TAG, "setValue(trie) called in onPostExecute of CreateTrie");
        }

        private interface CreateTrieTaskListener {

            void onTrieCreate(DoubleArrayTrie trie, int maxWordLength);

        }
    }

    /**
     * This listener is to inform the TrieViewModel when the Trie has been created
     */
    public interface OnCreateTrieListener {

        void onCreateTrie();

    }

    protected interface DictionaryUnzipListener {

        void onDictionaryUnzip();

    }

}
