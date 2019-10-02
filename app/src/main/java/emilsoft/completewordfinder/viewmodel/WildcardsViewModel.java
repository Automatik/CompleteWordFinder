package emilsoft.completewordfinder.viewmodel;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class WildcardsViewModel extends ViewModel {

    //Words found from FindAnagrams
    public ArrayList<String> wordsFound;
    public String filteredLetters;
    public boolean isFilterApplied;
    //This is the text inserted and the text relative to the results current showing in the recyclerview
    //In case the user edit the text inserted without pressing the button FIND
    public String textPressed;

    public WildcardsViewModel() {
        super();
        wordsFound = new ArrayList<>();
        isFilterApplied = false;
    }
}
