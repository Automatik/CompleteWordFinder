package emilsoft.completewordfinder.utils;

import android.os.Build;
import android.text.InputFilter;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public final class WordUtils {

    private WordUtils() {}

    public static int[] sortByWordLength(List<String> words) {
        return sortByWordLength(words, false);
    }

    public static int[] sortByWordLength(List<String> words, boolean ascendingOrder) {
        SparseArray<ArrayList<String>> arr = divideByWordLength(words);
        sortAndRemoveDuplicates(arr);
        int numKeys = arr.size();
        //The headersIndex array keeps the indexes in the arraylist where the word's length change, and where will go the header
        int[] headersIndex = new int[numKeys];
        int index = 0;
        words.clear();
        ArrayList<String> temp;
        //Order the list by descending length
        for (int i = numKeys - 1; i >= 0; i--) {
            headersIndex[numKeys -1 - i] = index;
            if(ascendingOrder)
                temp = arr.valueAt(numKeys -1 - i);
            else
                temp = arr.valueAt(i);
            index += temp.size();
            words.addAll(temp);
        }
        return headersIndex;
    }

    public static SparseArray<ArrayList<String>> divideByWordLength(List<String> words) {
        SparseArray<ArrayList<String>> arr = new SparseArray<>();
        for(String w : words) {
            int len = w.length();
            if(arr.get(len) == null)
                //Create new key
                arr.put(len, new ArrayList<>());
            ArrayList<String> temp = arr.get(len);
            temp.add(w);
        }
        return arr;
    }

    public static void sortAndRemoveDuplicates(SparseArray<ArrayList<String>> words) {
        for(int i = 0; i < words.size(); i++)
            sortAndRemoveDuplicates(words.valueAt(i), false);
    }

    public static void sortAndRemoveDuplicates(List<String> words) {
        sortAndRemoveDuplicates(words, false);
    }

    public static void sortAndRemoveDuplicates(List<String> words, boolean removeWordsWithSingleLetters) {
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
        //return new ArrayList<String>(sortedWords);
        //words = new ArrayList<String>(sortedWords);
        words.clear();
        words.addAll(sortedWords);
    }

    public static void wordsToUpperCase(SparseArray<ArrayList<String>> words) {
        for(int i = 0; i < words.size(); i++)
            wordsToUpperCase(words.valueAt(i));
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

    public static InputFilter[] addMyInputFilters(InputFilter[] inputFilters, int maxWordLength) {
        return addMyInputFilters(inputFilters, false, maxWordLength);
    }

    public static InputFilter[] addMyInputFilters(InputFilter[] inputFilters, boolean allowWildcard) {
        InputFilter[] newInputFilters = new InputFilter[inputFilters.length + 2];
        System.arraycopy(inputFilters, 0, newInputFilters, 0, inputFilters.length);
        newInputFilters[inputFilters.length] = new DigitsInputFilter(allowWildcard);
        newInputFilters[inputFilters.length + 1] = new InputFilter.AllCaps();
        return newInputFilters;
    }

    public static InputFilter[] addMyInputFilters(InputFilter[] inputFilters, boolean allowWildcard, int maxWordLength) {
        InputFilter[] newInputFilters = new InputFilter[inputFilters.length + 3];
        System.arraycopy(inputFilters, 0, newInputFilters, 0, inputFilters.length);
        newInputFilters[inputFilters.length] = new DigitsInputFilter(allowWildcard);
        newInputFilters[inputFilters.length + 1] = new InputFilter.AllCaps();
        newInputFilters[inputFilters.length + 2] = new InputFilter.LengthFilter(maxWordLength);
        return newInputFilters;
    }

}
