package emilsoft.completewordfinder.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import emilsoft.completewordfinder.adapter.HeaderRecyclerViewAdapter;
import emilsoft.completewordfinder.MainActivity;
import emilsoft.completewordfinder.R;
import emilsoft.completewordfinder.trie.DoubleArrayTrie;
import emilsoft.completewordfinder.utils.Dictionary;
import emilsoft.completewordfinder.utils.KeyboardHelper;
import emilsoft.completewordfinder.utils.WordUtils;
import emilsoft.completewordfinder.viewmodel.TrieViewModel;
import emilsoft.completewordfinder.viewmodel.TrieViewModelFactory;
import emilsoft.completewordfinder.viewmodel.WordsInPatternViewModel;

public class WordsInPatternFragment extends Fragment {

    private Button find;
    private TextInputEditText textinput;
    private TextInputLayout textInputLayout;
    private RecyclerView wordslist;
    private TextView textDescription, textNoWordsFound;
    private HeaderRecyclerViewAdapter adapter;
    private TrieViewModel trieViewModel;
    private WordsInPatternViewModel wordsInPatternViewModel;
    private ProgressBar progressBarLoadingWords;
    private FindWordsInPattern task;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    private static final String TEXT_INSERTED_STATE = "textInserted";
    private static final String TEXT_NO_WORDS_FOUND_STATE = "textNoWordsFound";
    private static final String TEXT_PROGRESSBAR_LOADING_WORDS_STATE = "textNoWordsFound";
    private static final String MAX_WORD_LENGTH_STATE = "maxWordLengthState";
    private static final String WORD_ORDER_ASCENDING_STATE = "isWordOrderAscendingState";
    private boolean isTextNoWordsFoundVisible = false, isProgressBarLoadingWordsVisible = false;
    private String dictionaryFilename;
    private int dictionaryAlphabetSize, maxWordLength;
    private boolean isWordOrderAscending;

    public WordsInPatternFragment() {
        isWordOrderAscending = MainActivity.WORD_ORDER_DEFAULT;
    }

    public static WordsInPatternFragment newInstance(Dictionary dictionary, boolean isWordOrderAscending) {

        Bundle args = new Bundle();
        args.putString(MainActivity.DICTIONARY_FILENAME, dictionary.getFilename());
        args.putInt(MainActivity.DICTIONARY_ALPHABET_SIZE, dictionary.getAlphabetSize());
        args.putInt(MainActivity.DICTIONARY_MAX_WORD, dictionary.getMaxWordLength());
        args.putBoolean(MainActivity.IS_WORD_ORDER_ASCENDING, isWordOrderAscending);
        WordsInPatternFragment fragment = new WordsInPatternFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wordsInPatternViewModel = ViewModelProviders.of(this).get(WordsInPatternViewModel.class);
        if(getArguments() != null) {
            dictionaryFilename = getArguments().getString(MainActivity.DICTIONARY_FILENAME);
            dictionaryAlphabetSize = getArguments().getInt(MainActivity.DICTIONARY_ALPHABET_SIZE);
            maxWordLength = getArguments().getInt(MainActivity.DICTIONARY_MAX_WORD);
            isWordOrderAscending = getArguments().getBoolean(MainActivity.IS_WORD_ORDER_ASCENDING);
        }
        Dictionary dictionary = new Dictionary(dictionaryFilename, dictionaryAlphabetSize, maxWordLength);
        if(getActivity() != null) {
            trieViewModel = ViewModelProviders.of(getActivity(),
                    new TrieViewModelFactory(getActivity().getApplication(), dictionary)).get(TrieViewModel.class);
            trieViewModel.addMaxWordLengthListener(maxWordLengthListener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anagram, container, false);
        textinput = (TextInputEditText) view.findViewById(R.id.textinput);
        textInputLayout = (TextInputLayout) view.findViewById(R.id.textinput_layout);
        find = (Button) view.findViewById(R.id.find_button);
        wordslist = (RecyclerView) view.findViewById(R.id.words_list);
        textDescription = (TextView) view.findViewById(R.id.text_description);
        textNoWordsFound = (TextView) view.findViewById(R.id.text_no_words_found);
        progressBarLoadingWords = (ProgressBar) view.findViewById(R.id.progressBarLoadingWords);
        find.setOnClickListener(onClickListener);
        //textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters()));
        //textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), maxWordLength));
        textDescription.setText(R.string.text_description_words_contained);
        if(maxWordLength != 0) {
            textInputLayout.setCounterEnabled(true);
            textInputLayout.setCounterMaxLength(maxWordLength);
            textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), maxWordLength));
        } else
            textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null) {
            String textInserted = savedInstanceState.getString(TEXT_INSERTED_STATE);
            textinput.setText(textInserted);
            if(wordsInPatternViewModel.wordsFound != null && adapter == null) {
                adapter = new HeaderRecyclerViewAdapter(wordsInPatternViewModel.wordsFound, wordsInPatternViewModel.headersIndex);
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
            isWordOrderAscending = savedInstanceState.getBoolean(WORD_ORDER_ASCENDING_STATE);
            maxWordLength = savedInstanceState.getInt(MAX_WORD_LENGTH_STATE);
        }

        onSharedPreferenceChangeListener = ((sharedPreferences, key) -> {
           if(key.equals(getString(R.string.sharedpref_current_dictionary))) {
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
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    //https://stackoverflow.com/questions/9596010/android-use-done-button-on-keyboard-to-click-button
                    //Write logic here that will be executed when user taps next button
                    findButtonClick();
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
            getActivity().setTitle(getString(R.string.nav_item_words_contained));
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
        outState.putBoolean(WORD_ORDER_ASCENDING_STATE, isWordOrderAscending);
        outState.putInt(MAX_WORD_LENGTH_STATE, maxWordLength);
    }

    @Override
    public void onDestroy() {
        if(task != null)
            task.setListener(null);
        if(sharedPreferences != null)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        super.onDestroy();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            findButtonClick();
        }
    };

    private void findButtonClick() {
        if(getActivity() != null)
            KeyboardHelper.hideKeyboard(getActivity());
        if(textinput.getText() != null) {
            String textInserted = textinput.getText().toString().toLowerCase();

            if (textInserted.length() > maxWordLength && maxWordLength != MainActivity.MAX_WORD_LENGTH_DEFAULT_VALUE) {
                Toast.makeText(getContext(), getString(R.string.toast_max_digits_exceeded), Toast.LENGTH_SHORT).show();
                return;
            }

            if(textInserted.length() == 0)
                return;

            isProgressBarLoadingWordsVisible = true;
            progressBarLoadingWords.setVisibility(View.VISIBLE);
            isTextNoWordsFoundVisible = false;
            textNoWordsFound.setVisibility(View.INVISIBLE);

            trieViewModel.getTrie().observe(getActivity(), new Observer<DoubleArrayTrie>() {
                @Override
                public void onChanged(DoubleArrayTrie trie) {
                    task = new FindWordsInPattern(trie, isWordOrderAscending);
                    task.setListener((wordsFound, headersIndex) -> {

                        if (isProgressBarLoadingWordsVisible) {
                            isProgressBarLoadingWordsVisible = false;
                            progressBarLoadingWords.setVisibility(View.INVISIBLE);
                        }
                        if (wordsFound.isEmpty()) {
                            isTextNoWordsFoundVisible = true;
                            textNoWordsFound.setVisibility(View.VISIBLE);
                        }
                        //headersIndex.length is the number of headers to insert in the recyclerview
                        int size = wordsInPatternViewModel.wordsFound.size() + wordsInPatternViewModel.headersIndex.length;
                        wordsInPatternViewModel.wordsFound.clear();
                        if (adapter == null) {
                            adapter = new HeaderRecyclerViewAdapter(wordsInPatternViewModel.wordsFound, wordsInPatternViewModel.headersIndex);
                            wordslist.setAdapter(adapter);
                        } else
                            adapter.notifyItemRangeRemoved(0, size);
                        wordsInPatternViewModel.wordsFound.addAll(wordsFound);
                        wordsInPatternViewModel.headersIndex = headersIndex;
                        adapter.setHeadersIndex(headersIndex);
                        adapter.notifyItemRangeInserted(0, wordsFound.size() + headersIndex.length);
                    });
                    task.execute(textInserted);
                }
            });
        }
    }

    private TrieViewModel.MaxWordLengthListener maxWordLengthListener = (maxWordLength -> {
        //Log.v(MainActivity.TAG, "WordsInPatternFragment/ maxWordLengthListener called");
        this.maxWordLength = maxWordLength;
        if(maxWordLength != 0) {
            textInputLayout.setCounterEnabled(true);
            textInputLayout.setCounterMaxLength(maxWordLength);
            textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), maxWordLength));
        }
    });

    private static class FindWordsInPattern extends AsyncTask<String, Void, Void> {

        private DoubleArrayTrie trie;
        private ArrayList<String> words;
        private int[] headersIndex;
        private FindWordsInPatternTaskListener listener;
        private boolean isWordOrderAscending;

        public FindWordsInPattern(DoubleArrayTrie trie, boolean isWordOrderAscending) {
            this.trie = trie;
            this.isWordOrderAscending = isWordOrderAscending;
        }

        @Override
        protected Void doInBackground(String... strings) {
            String textInserted = strings[0];
            words = (ArrayList<String>) trie.match(textInserted);
            if(!words.isEmpty()) {
                //WordUtils.sortAndRemoveDuplicates(words);
                headersIndex = WordUtils.sortByWordLength(words, isWordOrderAscending);
                WordUtils.wordsToUpperCase(words);
            } else
                headersIndex = new int[]{};
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(listener != null)
                listener.onFindWordsInPatternTaskFinished(words, headersIndex);
        }

        public void setListener(FindWordsInPatternTaskListener listener) {
            this.listener = listener;
        }

        public interface FindWordsInPatternTaskListener {

            void onFindWordsInPatternTaskFinished(ArrayList<String> wordsFound, int[] headersIndex);

        }
    }

}
