package viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import emilsoft.completewordfinder.MainActivity;
import emilsoft.completewordfinder.Trie;
import emilsoft.completewordfinder.TrieNode;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.util.HashMapReferenceResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

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

    public LiveData<Trie> getTrie() {
        return mTrie;
    }

    public class TrieLiveData extends LiveData<Trie> {

        private final Context context;

        public TrieLiveData(Context context, String filename) {
            this.context = context;
            new CreateTrie().execute(filename);
            Log.v(MainActivity.TAG, "TrieLiveData() called");
        }

        private class CreateTrie extends AsyncTask<String, Void, Trie> {

            @Override
            protected Trie doInBackground(String... strings) {
                String filename = strings[0];
                Trie trie = new Trie();
                long start, stop;

                Kryo kryo = new Kryo();
                kryo.register(Trie.class, 12);
                kryo.register(TrieNode.class, 13);
                kryo.register(TrieNode[].class, 14);
                HashMapReferenceResolver referenceResolver = new HashMapReferenceResolver();
                kryo.setReferenceResolver(referenceResolver);

                try {
                    // If trie already exists, read it
                    if (fileExists(context, MainActivity.TRIE_FILENAME)) {

//                        FileInputStream fis = context.openFileInput(MainActivity.TRIE_FILENAME);
//                        BufferedInputStream bis = new BufferedInputStream(fis);
//                        ObjectInputStream ois = new ObjectInputStream(bis);
//                        Log.v(MainActivity.TAG, "Reading trie from file");
//                        trie = (Trie) ois.readObject();
//                        Log.v(MainActivity.TAG, "Finished reading trie from file");
//                        ois.close();
//                        fis.close();
//
//                        Log.v(MainActivity.TAG, "Building trie's suffix links");
//                        trie.buildSuffixLinks();
//                        Log.v(MainActivity.TAG, "Finished building the trie");

                        FileInputStream fis = context.openFileInput(MainActivity.TRIE_FILENAME);
                        ByteBufferInput input = new ByteBufferInput(fis);
                        start = System.nanoTime();
                        TrieNode root = kryo.readObject(input, TrieNode.class);
                        trie = new Trie(root);
                        stop = System.nanoTime();
                        Log.v(MainActivity.TAG,"Kryo reading time: "+(((stop-start)/(double)1000000))+" ms");
                        input.close();

                        Log.v(MainActivity.TAG, "Building trie's suffix links");
                        trie.buildSuffixLinks();
                        Log.v(MainActivity.TAG, "Finished building the trie");

                    } else {
                        //Read dictionary file
                        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename))); //context or getApplication() ?
                        String line;
                        Log.v(MainActivity.TAG, "Beginning trie insertions");
                        while ((line = reader.readLine()) != null) {
                            trie.insert(line); //Should add .toLowerCase()?
                        }
                        reader.close();


/*                        FileOutputStream fos = context.openFileOutput(MainActivity.TRIE_FILENAME, Context.MODE_PRIVATE);
                        WriteTrieRunnable runnable = new WriteTrieRunnable(trie, fos);
                        long stackSize = 10485760;
                        Thread thread = new Thread(getApplication().getMainLooper().getThread().getThreadGroup(),
                                runnable, "WriteTrieThread", stackSize);
                        thread.start();*/


                        //Write trie to file. So we skip to build it every time
//                        FileOutputStream fos = context.openFileOutput(MainActivity.TRIE_FILENAME, Context.MODE_PRIVATE);
//                        BufferedOutputStream bos = new BufferedOutputStream(fos);
//                        ObjectOutputStream oos = new ObjectOutputStream(bos);
//                        oos.writeObject(trie);
//                        oos.close();
//                        fos.close();



                        FileOutputStream fos = context.openFileOutput(MainActivity.TRIE_FILENAME, Context.MODE_PRIVATE);
                        ByteBufferOutput output = new ByteBufferOutput(fos);
                        start = System.nanoTime();
                        kryo.writeObject(output, trie.getRoot());
                        stop = System.nanoTime();
                        Log.v(MainActivity.TAG,"Kryo writing time: "+(((stop-start)/(double)1000000))+" ms");
                        output.close();

                        //After writing the trie, builds the suffix links
                        Log.v(MainActivity.TAG, "Building trie's suffix links");
                        trie.buildSuffixLinks();
                        Log.v(MainActivity.TAG, "Finished building the trie");

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
            protected void onPostExecute(Trie trie) {
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
