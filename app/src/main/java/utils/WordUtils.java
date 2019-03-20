package utils;

import android.os.Build;
import android.text.InputFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public final class WordUtils {

    private WordUtils() {}

    public static List<String> sortAndRemoveDuplicates(List<String> words) {
        return sortAndRemoveDuplicates(words, false);
    }

    public static List<String> sortAndRemoveDuplicates(List<String> words, boolean removeWordsWithSingleLetters) {
        Collection<String> sortedWords;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            //Should pass to Collator the context.getResources.getConfiguration().getLocales().get(0) instead?
            //Should do multiple APKs according to https://developer.android.com/guide/topics/resources/internationalization ?
            sortedWords = new TreeSet<String>(android.icu.text.Collator.getInstance());
        else //should pass context.getResources.getConfiguration().locale instead?
            sortedWords = new TreeSet<String>(java.text.Collator.getInstance());
        if(removeWordsWithSingleLetters) {
            for(String word : words)
                if(word.length() > 1)
                    sortedWords.add(word);
        } else
            sortedWords.addAll(words);
        return new ArrayList<String>(sortedWords);
    }

    public static void wordsToUpperCase(List<String> words) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            words.replaceAll(String::toUpperCase);
        } else {
            for(int i = 0; i<words.size(); i++)
                words.set(i, words.get(i).toUpperCase());
        }
    }

    public static InputFilter[] addMyInputFilters(InputFilter[] inputFilters) {
        return addMyInputFilters(inputFilters, false);
    }

    public static InputFilter[] addMyInputFilters(InputFilter[] inputFilters, boolean allowWildcard) {
        InputFilter[] newInputFilters = new InputFilter[inputFilters.length + 2];
        System.arraycopy(inputFilters, 0, newInputFilters, 0, inputFilters.length);
        newInputFilters[inputFilters.length] = new MyInputFilter(allowWildcard);
        newInputFilters[inputFilters.length + 1] = new InputFilter.AllCaps();
        return newInputFilters;
    }

}
