package emilsoft.completewordfinder.utils;

import java.util.HashMap;

public class Dictionaries {

    public static final String ITALIAN = "italian_dictionary";
    public static final String ENGLISH = "english_dictionary";
    private static HashMap<String, Dictionary> dictionaries;

    static {
        Dictionary italian = new Dictionary("280000_parole_italiane.txt", 26); //26 because the dictionary contains english letters
        Dictionary english = new Dictionary("words_english.txt", 26);
        dictionaries = new HashMap<>();
        dictionaries.put(ENGLISH, english);
        dictionaries.put(ITALIAN, italian);
    }

    public static Dictionary get(String key) {
        return dictionaries.get(key);
    }

}
