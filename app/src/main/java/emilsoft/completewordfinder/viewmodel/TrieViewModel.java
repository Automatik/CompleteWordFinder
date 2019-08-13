package emilsoft.completewordfinder.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

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

    private static List<MaxWordLengthListener> listeners;

    public TrieViewModel(Application application, Dictionary dictionary) {
        super(application);
        listeners = new ArrayList<>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

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
        listeners.add(listener);
        //should also implement removeListener() ?
    }

    @Override
    protected void onCleared() {
        listeners.clear();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Application app = getApplication();
        if(key.equals(app.getString(R.string.sharedpref_current_dictionary))) {
            String dictionaryName = sharedPreferences.getString(key, null);
            if(dictionaryName != null) {
                Dictionary dictionary = Dictionaries.get(dictionaryName);
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
