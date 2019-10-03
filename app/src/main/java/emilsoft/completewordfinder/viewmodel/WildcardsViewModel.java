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
    //This list is for the user when he removes the filter so the all the words can be shown again,
    //or if another filter on the same words is applied
    public ArrayList<String> wordsBackup;

    public WildcardsViewModel() {
        super();
        wordsFound = new ArrayList<>();
        wordsBackup = new ArrayList<>();
        isFilterApplied = false;
    }
}
