package emilsoft.completewordfinder.utils;

import java.util.HashMap;
import java.util.Locale;

public class Dictionaries {

    public static final String ITALIAN = "italian_dictionary";
    public static final String ENGLISH = "english_dictionary";
    public static final String FRENCH = "french_dictionary";
    public static final String SWEDISH = "swedish_dictionary";
    private static HashMap<String, Dictionary> dictionaries;

    static {
        Dictionary italian = new Dictionary("280000_parole_italiane.txt", 26); //26 because the dictionary contains english letters
        Dictionary english = new Dictionary("english_sowpods.txt", 26);
        Dictionary french = new Dictionary("french_ods4.txt", 26);
        Dictionary swedish = new Dictionary("swedish.txt", 29);
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

}
