package emilsoft.completewordfinder.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import emilsoft.completewordfinder.MainActivity;
import emilsoft.completewordfinder.trie.DoubleArrayTrie;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TrieViewModel extends AndroidViewModel {

    private final TrieLiveData mTrie;

    public TrieViewModel(Application application, String filename) {
        super(application);
        Log.v(MainActivity.TAG, "TrieViewModel() called");
        mTrie = new TrieLiveData(application, filename);
        boolean b = mTrie.getValue() == null;
        if (b)
            Log.v(MainActivity.TAG, "Is mTrie null? True");
        else Log.v(MainActivity.TAG, "Is mTrie null? False");
    }

    public LiveData<DoubleArrayTrie> getTrie() {
        return mTrie;
    }

    public class TrieLiveData extends LiveData<DoubleArrayTrie> {

        private final Context context;

        public TrieLiveData(Context context, String filename) {
            this.context = context;
            new CreateTrie().execute(filename);
            Log.v(MainActivity.TAG, "TrieLiveData() called");
        }

        private class CreateTrie extends AsyncTask<String, Void, DoubleArrayTrie> {

            @Override
            protected DoubleArrayTrie doInBackground(String... strings) {
                String filename = strings[0];
                DoubleArrayTrie trie = new DoubleArrayTrie(); //TODO do we need to specific alphabet size?

                try {
                    // If trie already exists, read it
                    if (fileExists(context, MainActivity.TRIE_FILENAME)) {

                        FileInputStream fis = context.openFileInput(MainActivity.TRIE_FILENAME);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        Log.v(MainActivity.TAG, "Reading trie from file");
                        trie = (DoubleArrayTrie) ois.readObject();
                        Log.v(MainActivity.TAG, "Finished reading trie from file");
                        ois.close();
                        fis.close();

                    } else {
                        //Read dictionary file
                        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename))); //context or getApplication() ?
                        String line;
                        Log.v(MainActivity.TAG, "Beginning trie insertions");
                        long start = System.nanoTime();
                        while ((line = reader.readLine()) != null) {
                            trie.insert(line); //Should add .toLowerCase()?
                        }
                        long stop = System.nanoTime();
                        Log.v(MainActivity.TAG,"Trie insertion time: "+(((stop-start)/(double)1000000))+" ms");
                        reader.close();

                        //Write trie to file. So we skip to build it every time
                        FileOutputStream fos = context.openFileOutput(MainActivity.TRIE_FILENAME, Context.MODE_PRIVATE);
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
                setValue(trie);
                Log.v(MainActivity.TAG, "setValue(trie) called in onPostExecute of CreateTrie");
            }
        }

    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        Log.v(MainActivity.TAG, "File Length = "+file.length());
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }

}
