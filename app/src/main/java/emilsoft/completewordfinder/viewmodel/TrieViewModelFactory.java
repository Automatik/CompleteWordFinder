package emilsoft.completewordfinder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TrieViewModelFactory implements ViewModelProvider.Factory {

    private Application application;
    private String filename;

    public TrieViewModelFactory(Application application, String filename) {
        this.application = application;
        this.filename = filename;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TrieViewModel(application, filename);
    }
}
