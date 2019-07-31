package emilsoft.completewordfinder.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import emilsoft.completewordfinder.MainActivity;
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
    private List<MaxWordLengthListener> listeners;

    //public TrieViewModel(Application application, Dictionary dictionary, MaxWordLengthListener listener) {
    public TrieViewModel(Application application, Dictionary dictionary) {
        super(application);
        Log.v(MainActivity.TAG, "TrieViewModel() called");
        //this.listener = listener;
        listeners = new ArrayList<>();
        //listeners.add(listener);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        //mTrie = new TrieLiveData(application, dictionary, listener);
        mTrie = new TrieLiveData(application, dictionary, internalMaxWordLengthListener);
    }

    public LiveData<DoubleArrayTrie> getTrie() {
        return mTrie;
    }

    public void addMaxWordLengthListener(MaxWordLengthListener listener) {
        listeners.add(listener);
        //should also implement removeListener() ?
    }

    private InternalMaxWordLengthListener internalMaxWordLengthListener = (maxWordLength -> {
         for(MaxWordLengthListener listener : listeners)
             listener.onGetMaxWordLength(maxWordLength);
    });

    @Override
    protected void onCleared() {
        Log.v(MainActivity.TAG, "TrieViewModel/onCleared call");
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(MainActivity.TAG, "TrieViewModel/onSharedPreferenceChanged called");
        if(key.equals("dictionary")) {
            Application app = getApplication();
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
