package viewmodel;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class BeginsWithViewModel extends ViewModel {

    //Words found from FindAnagrams
    public ArrayList<String> wordsFound;

    public BeginsWithViewModel() {
        super();
    }
}
