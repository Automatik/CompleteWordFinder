package emilsoft.completewordfinder.viewmodel;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class WordsInPatternViewModel extends ViewModel {

    public ArrayList<String> wordsFound;
    public int[] headersIndex;

    public WordsInPatternViewModel() {
        wordsFound = new ArrayList<>();
        headersIndex = new int[0];
    }

}
