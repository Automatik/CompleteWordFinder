package emilsoft.completewordfinder.viewmodel;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class BeginsWithViewModel extends ViewModel {

    //Words found from FindAnagrams
    public ArrayList<String> wordsFound;
    public int[] headersIndex;

    public BeginsWithViewModel() {
        super();
        wordsFound = new ArrayList<>();
        headersIndex = new int[0];
    }
}
