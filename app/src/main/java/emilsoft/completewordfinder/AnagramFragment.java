package emilsoft.completewordfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import emilsoft.completewordfinder.utils.Dictionary;
import emilsoft.completewordfinder.utils.KeyboardHelper;
import emilsoft.completewordfinder.utils.WordUtils;
import emilsoft.completewordfinder.viewmodel.AnagramViewModel;

import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;


public class AnagramFragment extends Fragment {

    private Button find;
    private TextInputEditText textinput;
    private TextInputLayout textInputLayout;
    private RecyclerView wordslist;
    private TextView textDescription, textNoWordsFound;
    private AnagramRecyclerViewAdapter adapter;
    private AnagramViewModel anagramViewModel;
    private ProgressBar progressBarLoadingWords;
    private FindAnagrams findAnagramsTask;
    private ReadDictionary readDictionaryTask;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    //TODO Extends for alphabets with more letters (e.g Swedish)
    private static final int[] PRIMES = new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31,
            37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103,
            107, 109, 113 };

    private static final String TEXT_INSERTED_STATE = "textInserted";
    private static final String TEXT_NO_WORDS_FOUND_STATE = "textNoWordsFound";
    private static final String TEXT_PROGRESSBAR_LOADING_WORDS_STATE = "textNoWordsFound";
    private static final String IS_DICTIONARY_READ_STATE = "isDictionaryRead";
    private static final String DICTIONARY_FILENAME = "dictionaryFilename";

    private String textInserted, dictionaryFilename;
    private boolean isTextNoWordsFoundVisible = false, isProgressBarLoadingWordsVisible = false;
    private boolean isDictionaryRead, findAnagramPending;

    public AnagramFragment() {
        isDictionaryRead = false;
        findAnagramPending = false;
    }

    public static AnagramFragment newInstance(Dictionary dictionary) {
        
        Bundle args = new Bundle();
        args.putString(DICTIONARY_FILENAME, dictionary.getFilename());
        AnagramFragment fragment = new AnagramFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        anagramViewModel = ViewModelProviders.of(this).get(AnagramViewModel.class);
        if(getArguments() != null) {
            dictionaryFilename = getArguments().getString(DICTIONARY_FILENAME);
            readDictionary(dictionaryFilename);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_anagram, container, false);
        textinput = (TextInputEditText) view.findViewById(R.id.textinput);
        textInputLayout = (TextInputLayout) view.findViewById(R.id.textinput_layout);
        find = (Button) view.findViewById(R.id.find_button);
        wordslist = (RecyclerView) view.findViewById(R.id.words_list);
        textDescription = (TextView) view.findViewById(R.id.text_description);
        textNoWordsFound = (TextView) view.findViewById(R.id.text_no_words_found);
        progressBarLoadingWords = (ProgressBar) view.findViewById(R.id.progressBarLoadingWords);
        find.setOnClickListener(onClickListener);
        textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters()));
        textDescription.setText(R.string.text_description_anagrams);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null) {
            textInserted = savedInstanceState.getString(TEXT_INSERTED_STATE);
            textinput.setText(textInserted);
            if(anagramViewModel.wordsFound != null && adapter == null) {
                adapter = new AnagramRecyclerViewAdapter(anagramViewModel.wordsFound);
                wordslist.setAdapter(adapter);
            }
            isTextNoWordsFoundVisible = savedInstanceState.getBoolean(TEXT_NO_WORDS_FOUND_STATE);
            if(isTextNoWordsFoundVisible)
                textNoWordsFound.setVisibility(View.VISIBLE);
            else
                textNoWordsFound.setVisibility(View.INVISIBLE);
            isProgressBarLoadingWordsVisible = savedInstanceState.getBoolean(TEXT_PROGRESSBAR_LOADING_WORDS_STATE);
            if(isProgressBarLoadingWordsVisible && !isTextNoWordsFoundVisible)
                progressBarLoadingWords.setVisibility(View.VISIBLE);
            else {
                isProgressBarLoadingWordsVisible = false;
                progressBarLoadingWords.setVisibility(View.INVISIBLE);
            }

            dictionaryFilename = savedInstanceState.getString(DICTIONARY_FILENAME);
            boolean isDictRead = savedInstanceState.getBoolean(IS_DICTIONARY_READ_STATE);
            if(isDictRead && !anagramViewModel.dictionary.isEmpty()) {
                isDictionaryRead = true;
                textInputLayout.setCounterEnabled(true);
                textInputLayout.setCounterMaxLength(anagramViewModel.maxWordLength);
                textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), anagramViewModel.maxWordLength));
            } else {
                isDictionaryRead = false;
                textInputLayout.setCounterEnabled(false);
                //readDictionary(dictionaryFilename); could already be reading the dict
            }
        }

        onSharedPreferenceChangeListener = ((sharedPreferences, key) -> {
            if(key.equals(getString(R.string.sharedpref_current_dictionary))) {
                //read dictionary is already done when recreating this fragment
                //disable counter before the new dictionary is read
                textInputLayout.setCounterEnabled(false);
                textinput.setFilters(WordUtils.addMyInputFilters(new InputFilter[]{}));
            }
        });
        if(getActivity() != null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }

        //Keyboard Done button event
        textinput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    //https://stackoverflow.com/questions/9596010/android-use-done-button-on-keyboard-to-click-button
                    //Write logic here that will be executed when user taps next button
                    find.performClick();
                    return false; //return false to hide keyboard
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() != null)
            getActivity().setTitle(getString(R.string.nav_item_anagrams));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(textinput != null && textinput.getText() != null)
            outState.putString(TEXT_INSERTED_STATE,textinput.getText().toString());
        outState.putBoolean(TEXT_NO_WORDS_FOUND_STATE, isTextNoWordsFoundVisible);
        outState.putBoolean(TEXT_PROGRESSBAR_LOADING_WORDS_STATE, isProgressBarLoadingWordsVisible);
        outState.putString(DICTIONARY_FILENAME, dictionaryFilename);
        outState.putBoolean(IS_DICTIONARY_READ_STATE, isDictionaryRead);
    }

    @Override
    public void onDestroy() {
        if(findAnagramsTask != null)
            findAnagramsTask.setListener(null);
        if(readDictionaryTask != null)
            readDictionaryTask.setListener(null);
        if(sharedPreferences != null)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        super.onDestroy();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(getActivity() != null)
                KeyboardHelper.hideKeyboard(getActivity());
            if(textinput.getText() != null) {
                textInserted = textinput.getText().toString().toLowerCase();
                isProgressBarLoadingWordsVisible = true;
                progressBarLoadingWords.setVisibility(View.VISIBLE);
                isTextNoWordsFoundVisible = false;
                textNoWordsFound.setVisibility(View.INVISIBLE);

                int size = anagramViewModel.wordsFound.size();
                anagramViewModel.wordsFound.clear();
                if (adapter == null) {
                    adapter = new AnagramRecyclerViewAdapter(anagramViewModel.wordsFound);
                    wordslist.setAdapter(adapter);
                } else
                    adapter.notifyItemRangeRemoved(0, size);

                if (isDictionaryRead) {
                    findAnagramPending = false;
                    findAnagrams(textInserted);
                } else
                    //the read dictionary task should already being executed
                    findAnagramPending = true;
            }
        }
    };

    private void readDictionary(String dictionaryFilename) {
        if(dictionaryFilename == null) {
            Toast.makeText(getContext(), getString(R.string.toast_error_retrieving_dictionary), Toast.LENGTH_SHORT).show();
            return;
        }
        readDictionaryTask = new ReadDictionary(getContext(), dictionaryFilename);
        readDictionaryTask.setListener(new ReadDictionary.ReadDictionaryTaskListener() {
            @Override
            public void onDictionaryReadFinish(ArrayList<String> dictionary, int maxWordLength) {
                anagramViewModel.dictionary = dictionary;
                anagramViewModel.maxWordLength = maxWordLength;
                isDictionaryRead = true;
                textInputLayout.setCounterEnabled(true);
                textInputLayout.setCounterMaxLength(anagramViewModel.maxWordLength);
                textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), maxWordLength));
                if(findAnagramPending) {
                    findAnagramPending = false;
                    findAnagrams(textInserted);
                }
            }

            @Override
            public void onDictionaryReadError() {
                Toast.makeText(getContext(), getString(R.string.toast_error_retrieving_dictionary), Toast.LENGTH_SHORT).show();
            }
        });
        readDictionaryTask.execute();
    }

    private void findAnagrams(String textInserted) {
        //findAnagramsTask = new FindAnagrams(getContext(), MainActivity.DICTIONARY, anagramViewModel.dictionary);
        if(textInserted.length() > anagramViewModel.maxWordLength){
            Toast.makeText(getContext(), getString(R.string.toast_max_digits_exceeded), Toast.LENGTH_SHORT).show();
            return;
        }
        findAnagramsTask = new FindAnagrams(anagramViewModel.dictionary);
        findAnagramsTask.setListener(new FindAnagrams.FindAnagramsTaskListener() {
            @Override
            public void onFindAnagramsTaskNewWordFound(String newWord) {
                if (isProgressBarLoadingWordsVisible) {
                    isProgressBarLoadingWordsVisible = false;
                    progressBarLoadingWords.setVisibility(View.INVISIBLE);
                }
                if (!anagramViewModel.wordsFound.contains(newWord)) {
                    anagramViewModel.wordsFound.add(newWord);
                    adapter.notifyItemInserted(anagramViewModel.wordsFound.size() - 1);
                }
            }

            @Override
            public void onFindAnagramsTaskFinished() {
                if (anagramViewModel.wordsFound.isEmpty()) {
                    isProgressBarLoadingWordsVisible = false;
                    progressBarLoadingWords.setVisibility(View.INVISIBLE);
                    isTextNoWordsFoundVisible = true;
                    textNoWordsFound.setVisibility(View.VISIBLE);
                } else {
                    isTextNoWordsFoundVisible = false;
                    textNoWordsFound.setVisibility(View.INVISIBLE);
                }
            }
        });
        findAnagramsTask.execute(textInserted);
    }

    private static long calculateProduct(char[] letters) {
        long result = 1L;
        for(char c: letters){
            if(c < 65)
                return -1;
            int pos = c - 65; //65 is the position of A in ASCII Table
            result *= PRIMES[pos];
        }
        return result;
    }

    private static class FindAnagrams extends AsyncTask<String, String, Void> {

        private ArrayList<String> words;
        private FindAnagramsTaskListener listener;

        public FindAnagrams(ArrayList<String> words) {
            this.words = words;
        }

        @Override
        protected Void doInBackground(String... strings) {
            String word = strings[0];
            long pw = calculateProduct(word.toUpperCase().toCharArray()); //Product of the word to match
            for(String w : words) {
                w = w.toUpperCase();
                char[] letters = w.toCharArray();
                long product = calculateProduct(letters);
                if(product == pw)
                    publishProgress(w);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String newWord = values[0];
            if(listener != null)
                listener.onFindAnagramsTaskNewWordFound(newWord);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(listener != null)
                listener.onFindAnagramsTaskFinished();
        }

        public void setListener(FindAnagramsTaskListener listener) {
            this.listener = listener;
        }

        public interface FindAnagramsTaskListener {

            void onFindAnagramsTaskNewWordFound(String newWord);

            void onFindAnagramsTaskFinished();

        }
    }

    private static class ReadDictionary extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<Context> context;
        private String filename;
        private ReadDictionaryTaskListener listener;
        private ArrayList<String> words;
        private int maxWordLength;

        public ReadDictionary(Context context, String filename) {
            this.context = new WeakReference<>(context);
            this.filename = filename;
            words = new ArrayList<>();
            maxWordLength = 0;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                //Log.v(MainActivity.TAG, "Starting reading dictionary");
                BufferedReader reader = new BufferedReader(new InputStreamReader(context.get().getAssets().open(filename)));
                String line;
                while ((line = reader.readLine()) != null) {
                        if(line.length() > maxWordLength)
                            maxWordLength = line.length();
                        words.add(line.toLowerCase());
                }
                reader.close();
                //Log.v(MainActivity.TAG, "Finished reading dictionary");
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
                //Log.v(MainActivity.TAG, "AnagramFragment/ReadDictionary: error reading dictionary");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(listener != null) {
                if(result)
                    listener.onDictionaryReadFinish(words, maxWordLength);
                else
                    listener.onDictionaryReadError();
            }
        }

        public void setListener(ReadDictionaryTaskListener listener) {
            this.listener = listener;
        }

        public interface ReadDictionaryTaskListener {

            void onDictionaryReadFinish(ArrayList<String> dictionary, int maxWordLength);

            void onDictionaryReadError();

        }
    }

}
