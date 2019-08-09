package emilsoft.completewordfinder;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private static final String DICTIONARY_SUMMARY_TEXT = "Current selected: ";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_ui, rootKey);

        Preference dictionaryPref = findPreference(getString(R.string.sharedpref_current_dictionary));
//        updateDictionarySummary(dictionaryPref,
//                dictionaryPref.getSharedPreferences().getString(getString(R.string.sharedpref_current_dictionary),
//                        null));
        updateDictionarySummary(dictionaryPref,
                dictionaryPref.getSharedPreferences().getString(getString(R.string.sharedpref_current_dictionary), null));
        dictionaryPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals(getString(R.string.sharedpref_current_dictionary)))
            updateDictionarySummary(preference, newValue.toString());
        return true;
    }

    private void updateDictionarySummary(Preference preference, String value) {
        String[] entries = getResources().getStringArray(R.array.dictionaries);
        String[] values = getResources().getStringArray(R.array.dictionariesAlias);
        if(value == null) {
            //No dictionary set, use the default dictionary
            //preference.setSummary(DICTIONARY_SUMMARY_TEXT+getString(R.string.sharedpref_default_dictionary));
            preference.setSummary(DICTIONARY_SUMMARY_TEXT+values[0]); //English
        } else {
            //preference.setSummary(DICTIONARY_SUMMARY_TEXT+value);
            int i = 0;
            while(i < entries.length && !entries[i].equals(value))
                i++;
            preference.setSummary(DICTIONARY_SUMMARY_TEXT+values[i]);
        }
    }
}
