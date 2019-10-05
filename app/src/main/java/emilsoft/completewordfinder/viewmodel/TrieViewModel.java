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
    private boolean isTrieVersionChanged;

    private static List<MaxWordLengthListener> listeners;

    public TrieViewModel(Application application, Dictionary dictionary) {
        super(application);
        listeners = new ArrayList<>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        //Check Trie's version
        int version = sharedPreferences.getInt(application.getString(R.string.sharedpref_trie_version), 0);
        isTrieVersionChanged = version < DoubleArrayTrie.TRIE_VERSION;

        InternalMaxWordLengthListener internalMaxWordLengthListener = (maxWordLength -> {
            for (MaxWordLengthListener listener : listeners)
                listener.onGetMaxWordLength(maxWordLength);
        });

        TrieLiveData.OnCreateTrieListener onCreateTrieListener = () -> {
            if (isTrieVersionChanged) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getApplication().getString(R.string.sharedpref_trie_version), DoubleArrayTrie.TRIE_VERSION);
                editor.apply();
            }
        };

        mTrie = new TrieLiveData(application, dictionary, isTrieVersionChanged, internalMaxWordLengthListener, onCreateTrieListener);
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
                mTrie.createTrie(app, dictionary, true, isTrieVersionChanged);
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
