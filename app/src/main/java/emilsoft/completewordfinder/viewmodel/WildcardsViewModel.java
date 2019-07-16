package emilsoft.completewordfinder.viewmodel;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class WildcardsViewModel extends ViewModel {

    //Words found from FindAnagrams
    public ArrayList<String> wordsFound;

    public WildcardsViewModel() {
        super();
        wordsFound = new ArrayList<>();
    }
}
