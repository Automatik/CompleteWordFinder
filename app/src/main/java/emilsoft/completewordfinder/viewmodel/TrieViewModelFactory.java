package emilsoft.completewordfinder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import emilsoft.completewordfinder.utils.Dictionary;

public class TrieViewModelFactory implements ViewModelProvider.Factory {

    private Application application;
    private Dictionary dictionary;
    private TrieViewModel.MaxWordLengthListener listener;
    //private String filename;

    public TrieViewModelFactory(Application application, Dictionary dictionary, TrieViewModel.MaxWordLengthListener listener) {
        this.application = application;
        this.dictionary = dictionary;
        this.listener = listener;
        //this.filename = filename;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TrieViewModel(application, dictionary);
        //return (T) new TrieViewModel(application, dictionary, listener);
    }
}
