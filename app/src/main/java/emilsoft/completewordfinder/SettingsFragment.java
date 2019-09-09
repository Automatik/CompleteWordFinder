package emilsoft.completewordfinder;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import emilsoft.completewordfinder.utils.ThemeHelper;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_ui, rootKey);

        Preference dictionaryPref = findPreference(getString(R.string.sharedpref_current_dictionary));
        Preference wordOrderPref = findPreference(getString(R.string.sharedpref_word_order));
        Preference themePref = findPreference(getString(R.string.sharedpref_theme));

        if (dictionaryPref != null) {
            updateDictionarySummary(dictionaryPref,
                    dictionaryPref.getSharedPreferences().getString(getString(R.string.sharedpref_current_dictionary), null));
            dictionaryPref.setOnPreferenceChangeListener(this);
        }

        if(wordOrderPref != null) {
            updateWordOrderSummary(wordOrderPref,
                    wordOrderPref.getSharedPreferences().getString(getString(R.string.sharedpref_word_order), null));
            wordOrderPref.setOnPreferenceChangeListener(this);
        }

        if(themePref != null) {
            updateThemeSummary(themePref,
                    themePref.getSharedPreferences().getString(getString(R.string.sharedpref_theme), null));
            themePref.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals(getString(R.string.sharedpref_current_dictionary)))
            updateDictionarySummary(preference, newValue.toString());
        if(preference.getKey().equals(getString(R.string.sharedpref_word_order)))
            updateWordOrderSummary(preference, newValue.toString());
        if(preference.getKey().equals(getString(R.string.sharedpref_theme)))
            updateThemeSummary(preference, newValue.toString());
        return true;
    }

    private void updateDictionarySummary(Preference preference, String value) {
        String[] entries = getResources().getStringArray(R.array.dictionaries);
        String[] values = getResources().getStringArray(R.array.dictionariesAlias);
        if(value == null) {
            //No dictionary set, use the default dictionary
            //preference.setSummary(DICTIONARY_SUMMARY_TEXT+getString(R.string.sharedpref_default_dictionary));
            preference.setSummary(values[0]); //English
        } else {
            //preference.setSummary(DICTIONARY_SUMMARY_TEXT+value);
            int i = 0;
            while(i < entries.length && !entries[i].equals(value))
                i++;
            preference.setSummary(values[i]);
        }
    }

    private void updateWordOrderSummary(Preference preference, String value) {
        String[] entries = getResources().getStringArray(R.array.wordOrders);
        String[] values = getResources().getStringArray(R.array.wordOrdersAlias);
        if(value == null) {
            //No word order set
            preference.setSummary(values[1]); //Descending
        } else {
            int i = 0;
            while(i < entries.length && !entries[i].equals(value))
                i++;
            preference.setSummary(values[i]);
        }
    }

    private void updateThemeSummary(Preference preference, String value) {
        String[] entries = getResources().getStringArray(R.array.themeList);
        String[] values = getResources().getStringArray(R.array.themeListAlias);
        if(value == null) {
            //No theme set
            preference.setSummary(values[2]); //Default
        } else {
            int i = 0;
            while(i < entries.length && !entries[i].equals(value))
                i++;
            preference.setSummary(values[i]);
        }
    }
}
