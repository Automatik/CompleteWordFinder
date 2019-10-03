package emilsoft.completewordfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.kobakei.ratethisapp.RateThisApp;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import emilsoft.completewordfinder.fragment.AnagramFragment;
import emilsoft.completewordfinder.fragment.BeginsWithFragment;
import emilsoft.completewordfinder.fragment.SubAnagramsFragment;
import emilsoft.completewordfinder.fragment.WildcardsFragment;
import emilsoft.completewordfinder.fragment.WordsInPatternFragment;
import emilsoft.completewordfinder.utils.Dictionaries;
import emilsoft.completewordfinder.utils.Dictionary;
import emilsoft.completewordfinder.utils.HelpDialog;
import emilsoft.completewordfinder.utils.KeyboardHelper;
import emilsoft.completewordfinder.utils.ThemeHelper;
import emilsoft.completewordfinder.viewmodel.TrieViewModel;
import emilsoft.completewordfinder.viewmodel.TrieViewModelFactory;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        TrieViewModel.MaxWordLengthListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "Emil";
    public static final String TRIE_FILENAME = "trie.dat";
    public static final int MAX_WORD_LENGTH_DEFAULT_VALUE = 0;
    public static final String DICTIONARY_FILENAME = "dictionaryFilename";
    public static final String DICTIONARY_ALPHABET_SIZE = "dictionaryAlphabetSize";
    public static final String DICTIONARY_MAX_WORD = "dictionaryMaxWord";
    public static final String IS_WORD_ORDER_ASCENDING = "isWordOrderAscending";

    public static final boolean WORD_ORDER_DEFAULT = false; //descending
    public static final int CRITERIA_INSTALL_DAYS = 7;
    public static final int CRITERIA_LAUNCH_TIMES = 7;

    private Dictionary dict; //Dictionary in use
    private SharedPreferences sharedPreferences;
    private boolean isWordOrderAscending = WORD_ORDER_DEFAULT;
    private boolean isDrawerFixed; //it's a tablet with 600dp screen width

    private TrieViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        isDrawerFixed = getResources().getBoolean(R.bool.isDrawerFixed);
        if(isDrawerFixed) {
            //it's a tablet with 600dp screen width
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, GravityCompat.START);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    KeyboardHelper.hideKeyboard(MainActivity.this);
                }

            };
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        //Read Dictionary in use
        dict = readDictionary();

        //Read Word Order in use
        isWordOrderAscending = readWordOrder();

        //Only create fragment if activity is started for the first time
        if (savedInstanceState == null) {
            setTitle(getResources().getString(R.string.nav_item_anagrams));
            Fragment fragment = AnagramFragment.newInstance(dict);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }

        //TrieViewModel model = ViewModelProviders.of(this).get(TrieViewModel.class);
        mModel = ViewModelProviders.of(this,
                new TrieViewModelFactory(this.getApplication(), dict)).get(TrieViewModel.class);
        mModel.addMaxWordLengthListener(this);

        rateThisApp();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.action_rate_app) {
            String appPackage = getPackageName();
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackage));
                intent.setPackage("com.android.vending");
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException ex) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                startActivity(intent);
            }
            RateThisApp.stopRateDialog(this);
        }

        if (id == R.id.action_help) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            HelpDialog helpDialog;
            if(currentFragment instanceof AnagramFragment)
                helpDialog = new HelpDialog(this, getString(R.string.nav_item_anagrams),
                        getString(R.string.text_help_anagrams), getString(R.string.text_help_example_anagrams));
            else if(currentFragment instanceof BeginsWithFragment)
                helpDialog = new HelpDialog(this, getString(R.string.nav_item_begins_with),
                        getString(R.string.text_help_begins_with), getString(R.string.text_help_example_begins_with));
            else if(currentFragment instanceof WordsInPatternFragment)
                helpDialog = new HelpDialog(this, getString(R.string.nav_item_words_contained),
                        getString(R.string.text_help_words_contained), getString(R.string.text_help_example_words_contained));
            else if(currentFragment instanceof SubAnagramsFragment)
                helpDialog = new HelpDialog(this, getString(R.string.nav_item_sub_anagrams),
                        getString(R.string.text_help_sub_anagrams), getString(R.string.text_help_example_sub_anagrams));
            else //wildcard
                helpDialog = new HelpDialog(this, getString(R.string.nav_item_wildcards),
                        getString(R.string.text_help_wildcards), getString(R.string.text_help_example_wildcards));
            helpDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        if (id == R.id.nav_anagrams) {
            setTitle(getResources().getString(R.string.nav_item_anagrams));
            fragment = AnagramFragment.newInstance(dict);
        } else if (id == R.id.nav_begins_with) {
            setTitle(getResources().getString(R.string.nav_item_begins_with));
            fragment = BeginsWithFragment.newInstance(dict, isWordOrderAscending);
        } else if (id == R.id.nav_words_contained) {
            setTitle(getResources().getString(R.string.nav_item_words_contained));
            fragment = WordsInPatternFragment.newInstance(dict, isWordOrderAscending);
        } else if (id == R.id.nav_sub_anagrams) {
            setTitle(getResources().getString(R.string.nav_item_sub_anagrams));
            fragment = SubAnagramsFragment.newInstance(dict, isWordOrderAscending);
        } else if (id == R.id.nav_wildcards) {
            setTitle(getResources().getString(R.string.nav_item_wildcards));
            fragment = WildcardsFragment.newInstance(dict);
        } else {
            fragment = AnagramFragment.newInstance(dict);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(!isDrawerFixed)
            drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        if(sharedPreferences != null)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    private Dictionary readDictionary() {
        String dictName = sharedPreferences.getString(getString(R.string.sharedpref_current_dictionary), null);
        if(dictName == null) {
            dictName = Dictionaries.getDictionaryFromDefaultLocaleOnStartup();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.sharedpref_current_dictionary), dictName);
            editor.apply();
        }
        int maxWordLength = sharedPreferences.getInt(getString(R.string.sharedpref_current_dictionary_max_word_length), MAX_WORD_LENGTH_DEFAULT_VALUE);
        Dictionary dict = Dictionaries.get(dictName);
        if(dict != null && maxWordLength != MAX_WORD_LENGTH_DEFAULT_VALUE)
            dict.setMaxWordLength(maxWordLength);
        if(dict == null)
            dict = Dictionaries.get(Dictionaries.ENGLISH);
        return dict;
    }

    private boolean readWordOrder() {
        String wordOrder = sharedPreferences.getString(getString(R.string.sharedpref_word_order), null);
        boolean isWordOrderAscending;
        if(wordOrder != null) {
            switch (wordOrder) {
                //values from R.array.wordOrders
                case "ascending": isWordOrderAscending = true; break;
                case "descending": isWordOrderAscending = false; break;
                default: isWordOrderAscending = WORD_ORDER_DEFAULT;
            }
        } else
            isWordOrderAscending = WORD_ORDER_DEFAULT;
        return isWordOrderAscending;
    }

    @Override
    public void onGetMaxWordLength(int maxWordLength) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.sharedpref_current_dictionary_max_word_length), maxWordLength);
        editor.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Log.v(TAG, "MainActivity/ onSharedPreferenceChanged called");
        if(key.equals(getString(R.string.sharedpref_current_dictionary))) {
            String dictName = sharedPreferences.getString(key, null);
            if(dictName != null)
                dict = Dictionaries.get(dictName);
            //Set to zero the maxWordLength, because it doesn't correspond to the new dictionary
            onGetMaxWordLength(MAX_WORD_LENGTH_DEFAULT_VALUE); //possibly dangerous
        }
        if(key.equals(getString(R.string.sharedpref_word_order))) {
            isWordOrderAscending = readWordOrder();
        }
        if(key.equals(getString(R.string.sharedpref_theme)))
            ThemeHelper.applyTheme(sharedPreferences.getString(key, ThemeHelper.DEFAULT_MODE));
    }

    public void rateThisApp() {
        RateThisApp.Config config = new RateThisApp.Config(CRITERIA_INSTALL_DAYS, CRITERIA_LAUNCH_TIMES);
        //Other translations are already available in the RateThisApp's jar
        config.setTitle(R.string.rate_app_title);
        config.setMessage(R.string.rate_app_message);
        config.setYesButtonText(R.string.rate_app_yes_button);
        config.setNoButtonText(R.string.rate_app_no_button);
        config.setCancelButtonText(R.string.rate_app_cancel_button);
        RateThisApp.init(config);
        //Monitor launch times and interval from installation
        RateThisApp.onCreate(this);
        //If the condition is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }
}
