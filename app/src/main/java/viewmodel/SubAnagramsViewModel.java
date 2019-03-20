package viewmodel;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class SubAnagramsViewModel extends ViewModel {

    //Words found from FindAnagrams
    public ArrayList<String> wordsFound;

    public SubAnagramsViewModel() {
        super();
    }
}
