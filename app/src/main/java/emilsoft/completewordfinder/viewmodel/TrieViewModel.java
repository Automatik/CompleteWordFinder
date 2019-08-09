package emilsoft.completewordfinder.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import emilsoft.completewordfinder.MainActivity;
import emilsoft.completewordfinder.R;
import emilsoft.completewordfinder.trie.DoubleArrayTrie;
import emilsoft.completewordfinder.utils.Dictionaries;
import emilsoft.completewordfinder.utils.Dictionary;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TrieViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final TrieLiveData mTrie;
    private final SharedPreferences sharedPreferences;
    //private MaxWordLengthListener listener;
    private static List<MaxWordLengthListener> listeners;

    //public TrieViewModel(Application application, Dictionary dictionary, MaxWordLengthListener listener) {
    public TrieViewModel(Application application, Dictionary dictionary) {
        super(application);
        Log.v(MainActivity.TAG, "TrieViewModel() called");
        //this.listener = listener;
        listeners = new ArrayList<>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        //mTrie = new TrieLiveData(application, dictionary, listener);
        InternalMaxWordLengthListener internalMaxWordLengthListener = (maxWordLength -> {
            for (MaxWordLengthListener listener : listeners)
                listener.onGetMaxWordLength(maxWordLength);
        });
        mTrie = new TrieLiveData(application, dictionary, internalMaxWordLengthListener);
    }

    public LiveData<DoubleArrayTrie> getTrie() {
        return mTrie;
    }

    public void addMaxWordLengthListener(MaxWordLengthListener listener) {
        Log.v(MainActivity.TAG, "Listeners "+listeners.toString()+": "+listeners.size()+" adding "+listener.toString());
        listeners.add(listener);
        //should also implement removeListener() ?
    }

    @Override
    protected void onCleared() {
        Log.v(MainActivity.TAG, "TrieViewModel/onCleared call");
        Log.v(MainActivity.TAG, "Listeners clearing "+listeners.toString());
        listeners.clear();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(MainActivity.TAG, "TrieViewModel/onSharedPreferenceChanged called");
        Application app = getApplication();
        if(key.equals(app.getString(R.string.sharedpref_current_dictionary))) {
//            String currentDict = app.getString(R.string.sharedpref_current_dictionary);
//            String dictionaryFile = sharedPreferences.getString(currentDict, null);

            String dictionaryName = sharedPreferences.getString(key, null);
            if(dictionaryName != null) {
                Dictionary dictionary = Dictionaries.get(dictionaryName);
                //Dictionary dictionary = new Dictionary(dictionaryFile, alphabetSize);
                mTrie.createTrie(app, dictionary, true);
            }
        }
    }

    public interface MaxWordLengthListener {

        void onGetMaxWordLength(int maxWordLength);

    }

    protected interface InternalMaxWordLengthListener {

        void onGetMaxWordLength(int maxWordLength);

    }

}
