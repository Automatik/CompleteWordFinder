package emilsoft.completewordfinder;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import emilsoft.completewordfinder.utils.ThemeHelper;

public class CompleteWordFinderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themePref = sharedPreferences.getString(getString(R.string.sharedpref_theme), ThemeHelper.DEFAULT_MODE);
        ThemeHelper.applyTheme(themePref);
    }
}
