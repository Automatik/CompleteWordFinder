package emilsoft.completewordfinder.viewmodel;

import android.util.SparseArray;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class SubAnagramsViewModel extends ViewModel {

    //Words found from FindAnagrams
    public ArrayList<String> wordsFound;
    public int[] headersIndex;

    public SubAnagramsViewModel() {
        wordsFound = new ArrayList<>();
        headersIndex = new int[0];
    }
}
