package emilsoft.completewordfinder.viewmodel;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class AnagramViewModel extends ViewModel {

    //Words contained in the dictionary file
    public ArrayList<String> dictionary;

    //Words found from FindAnagrams
    public ArrayList<String> wordsFound;

    public AnagramViewModel() {
        super();
        wordsFound = new ArrayList<>();
        dictionary = new ArrayList<>();
    }
}
