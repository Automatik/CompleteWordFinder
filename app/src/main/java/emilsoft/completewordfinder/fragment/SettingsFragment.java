package emilsoft.completewordfinder.fragment;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import emilsoft.completewordfinder.R;
import emilsoft.completewordfinder.utils.Dictionaries;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_ui, rootKey);

        Preference dictionaryPref = findPreference(getString(R.string.sharedpref_current_dictionary));
        Preference wordOrderPref = findPreference(getString(R.string.sharedpref_word_order));
        Preference themePref = findPreference(getString(R.string.sharedpref_theme));


        if (dictionaryPref != null) {
            String dict = dictionaryPref.getSharedPreferences().getString(getString(R.string.sharedpref_current_dictionary), null);
            updateDictionarySummary(dictionaryPref,dict);
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
            value = Dictionaries.getDictionaryFromDefaultLocaleOnStartup();
            preference.setDefaultValue(value);
        }
        int i = 0;
        while(i < entries.length && !entries[i].equals(value))
            i++;
        preference.setSummary(values[i]);
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
