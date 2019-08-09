package emilsoft.completewordfinder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import emilsoft.completewordfinder.utils.Dictionary;

public class TrieViewModelFactory implements ViewModelProvider.Factory {

    private Application application;
    private Dictionary dictionary;

    public TrieViewModelFactory(Application application, Dictionary dictionary) {
        this.application = application;
        this.dictionary = dictionary;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TrieViewModel(application, dictionary);
    }
}
