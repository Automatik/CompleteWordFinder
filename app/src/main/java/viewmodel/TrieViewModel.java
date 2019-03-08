package viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import emilsoft.completewordfinder.MainActivity;
import emilsoft.completewordfinder.Trie;
import emilsoft.completewordfinder.TrieNode;
import emilsoft.completewordfinder.TrieNodeSerializer;
import emilsoft.completewordfinder.UnsafeMemory;
import emilsoft.completewordfinder.UnsafeUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

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
                start = System.nanoTime();
                FSTConfiguration conf = FSTConfiguration.createAndroidDefaultConfiguration();
                stop = System.nanoTime();
                Log.v(MainActivity.TAG,"create configuration time: "+(((stop-start)/(double)1000000))+" ms");
                conf.registerClass(TrieNode.class, TrieNode[].class);
//                Kryo kryo = new Kryo();
//                kryo.register(Trie.class, 12);
//                kryo.register(TrieNode.class, 13);
//                kryo.register(TrieNode[].class, 14);
//                HashMapReferenceResolver referenceResolver = new HashMapReferenceResolver();
//                kryo.setReferenceResolver(referenceResolver);

                //UnsafeMemory buffer = new UnsafeMemory(new byte[20601783]);
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

//                        FileInputStream fis = context.openFileInput(MainActivity.TRIE_FILENAME);
//                        ByteBufferInput input = new ByteBufferInput(fis);
//                        start = System.nanoTime();
//                        TrieNode root = kryo.readObject(input, TrieNode.class);
//                        trie = new Trie(root);
//                        stop = System.nanoTime();
//                        Log.v(MainActivity.TAG,"Kryo reading time: "+(((stop-start)/(double)1000000))+" ms");
//                        input.close();

                        FileInputStream fis = context.openFileInput(MainActivity.TRIE_FILENAME);
                        FSTObjectInput input = new FSTObjectInput(fis);
                        start = System.nanoTime();
                        TrieNode root = (TrieNode) input.readObject(TrieNode.class, TrieNode[].class);
                        stop = System.nanoTime();
                        Log.v(MainActivity.TAG,"FST reading time: "+(((stop-start)/(double)1000000))+" ms");
                        input.close();
                        trie = new Trie(root);
                    } else {
                        //Read dictionary file
                        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename))); //context or getApplication() ?
                        String line;
                        Log.v(MainActivity.TAG, "Beginning trie insertions");
                        while ((line = reader.readLine()) != null) {
                            trie.insert(line); //Should add .toLowerCase()?
                        }
                        reader.close();


//                        Log.v(MainActivity.TAG, "Building trie's suffix links");
//                        trie.buildSuffixLinks();
//                        Log.v(MainActivity.TAG, "Finished building the trie");


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



//                        FileOutputStream fos = context.openFileOutput(MainActivity.TRIE_FILENAME, Context.MODE_PRIVATE);
//                        ByteBufferOutput output = new ByteBufferOutput(fos);
//                        long start = System.nanoTime();
//                        kryo.writeObject(output, trie.getRoot());
//                        long stop = System.nanoTime();
//                        Log.v(MainActivity.TAG,"Kryo writing time: "+(((stop-start)/(double)1000000))+" ms");
//                        output.close();

//                        Log.v(MainActivity.TAG, "Trie's root SizeOf: "+UnsafeUtils.sizeOf(trie.getRoot()));
//                        Log.v(MainActivity.TAG, "Trie's children length: "+ trie.getRoot().getChildren().length + " bytesToCopy: "+(trie.getRoot().getChildren().length << 3));
//                        Log.v(MainActivity.TAG, "Trie SizeOf: "+UnsafeUtils.sizeOf(trie));
//                        Log.v(MainActivity.TAG, "Trie children sizeOf: "+ UnsafeUtils.sizeOf(trie.getRoot().getChildren()));
//                        Log.v(MainActivity.TAG, "FirstFieldOffset: "+UnsafeUtils.firstFieldOffset(TrieNode.class));
//                        Log.v(MainActivity.TAG, "ElementSize: "+(UnsafeUtils.sizeOf(TrieNode.class) - UnsafeUtils.firstFieldOffset(TrieNode.class)));
//                        Log.v(MainActivity.TAG, "toByteArray Length: "+UnsafeUtils.toByteArray(trie.getRoot()).length);


                        FileOutputStream fos = context.openFileOutput(MainActivity.TRIE_FILENAME, Context.MODE_PRIVATE);
                        FSTObjectOutput out = new FSTObjectOutput(fos);
                        start = System.nanoTime();
                        out.writeObject(trie.getRoot(), TrieNode.class, TrieNode[].class);
                        //out.write(array);
                        //conf.getObjectOutput(fos).write(array);
                        stop = System.nanoTime();
                        out.close();
                        Log.v(MainActivity.TAG,"FST writing time: "+(((stop-start)/(double)1000000))+" ms");
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
