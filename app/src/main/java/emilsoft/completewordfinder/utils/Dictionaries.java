package emilsoft.completewordfinder.utils;

import android.util.Log;

import androidx.core.util.Supplier;

import java.util.HashMap;
import java.util.Locale;

import emilsoft.completewordfinder.MainActivity;

public class Dictionaries {

    public static final String ITALIAN = "italian_dictionary";
    public static final String ENGLISH = "english_dictionary";
    public static final String FRENCH = "french_dictionary";
    public static final String SWEDISH = "swedish_dictionary";
    private static final String ITALIAN_DICT = "280000_parole_italiane.txt";
    private static final String ENGLISH_DICT = "english_sowpods.txt";
    private static final String FRENCH_DICT = "french_ods4.txt";
    private static final String SWEDISH_DICT = "swedish.txt";
    private static HashMap<String, Dictionary> dictionaries;
    private static Supplier<Dictionary> dictSupplier;

    static {
        Dictionary italian = new Dictionary(ITALIAN_DICT, 26); //26 because the dictionary contains english letters
        Dictionary english = new Dictionary(ENGLISH_DICT, 26);
        Dictionary french = new Dictionary(FRENCH_DICT, 26);
        Dictionary swedish = new Dictionary(SWEDISH_DICT, 29);
        dictionaries = new HashMap<>();
        dictionaries.put(ENGLISH, english);
        dictionaries.put(ITALIAN, italian);
        dictionaries.put(FRENCH, french);
        dictionaries.put(SWEDISH, swedish);
    }

    public static Dictionary get(String key) {
        return dictionaries.get(key);
    }

    public static String getDictionaryFromDefaultLocaleOnStartup() {
        String lang = Locale.getDefault().getLanguage();
        String dict;
        switch (lang) {
            case "en" : dict = ENGLISH; break;
            case "it" : dict = ITALIAN; break;
            case "fr" : dict = FRENCH; break;
            case "sv" : dict = SWEDISH; break;
            default: dict = ENGLISH;
        }
        return dict;
    }

    public static void setDictionarySupplier(Supplier<Dictionary> dictionarySupplier) {
        dictSupplier = dictionarySupplier;
    }

    public static Dictionary getCurrentDictionary() {
        Log.v(MainActivity.TAG, "Dict: "+dictSupplier.get().getFilename());
        return (dictSupplier == null) ? null : dictSupplier.get();
    }

    public static String getCurrentDictionaryName() {
        Log.v(MainActivity.TAG, "Dict: "+dictSupplier.get().getFilename());
        if (dictSupplier == null)
            return null;
        Dictionary dict = dictSupplier.get();
        if (dict == null)
            return null;
        switch (dict.getFilename()) {
            case ITALIAN_DICT: return ITALIAN;
            case ENGLISH_DICT: return ENGLISH;
            case FRENCH_DICT: return FRENCH;
            case SWEDISH_DICT: return SWEDISH;
            default: return null;
        }
    }

}
