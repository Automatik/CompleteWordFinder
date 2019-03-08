package viewmodel;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class AnagramViewModel extends ViewModel {

    //Words found from FindAnagrams
    public ArrayList<String> wordsFound;

    public AnagramViewModel() {
        super();
    }
}
