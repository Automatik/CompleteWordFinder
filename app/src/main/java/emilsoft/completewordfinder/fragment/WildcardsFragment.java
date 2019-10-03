package emilsoft.completewordfinder.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.Iterator;
import java.util.TreeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import emilsoft.completewordfinder.adapter.AnagramRecyclerViewAdapter;
import emilsoft.completewordfinder.MainActivity;
import emilsoft.completewordfinder.R;
import emilsoft.completewordfinder.dialog.FilterDialog;
import emilsoft.completewordfinder.trie.DoubleArrayTrie;
import emilsoft.completewordfinder.utils.Dictionary;
import emilsoft.completewordfinder.utils.KeyboardHelper;
import emilsoft.completewordfinder.utils.WordUtils;
import emilsoft.completewordfinder.viewmodel.TrieViewModel;
import emilsoft.completewordfinder.viewmodel.TrieViewModelFactory;
import emilsoft.completewordfinder.viewmodel.WildcardsViewModel;

public class WildcardsFragment extends Fragment {

    private Button find;
    private TextInputEditText textinput;
    private TextInputLayout textInputLayout;
    private RecyclerView wordslist;
    private TextView textDescription, textNoWordsFound, textFilter;
    private AnagramRecyclerViewAdapter adapter;
    private TrieViewModel trieViewModel;
    private WildcardsViewModel wildcardsViewModel;
    private ProgressBar progressBarLoadingWords;
    private FindWildcards task;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    private static final String TEXT_INSERTED_STATE = "textInserted";
    private static final String TEXT_NO_WORDS_FOUND_STATE = "textNoWordsFound";
    private static final String TEXT_PROGRESSBAR_LOADING_WORDS_STATE = "textNoWordsFound";
    private static final String MAX_WORD_LENGTH_STATE = "maxWordLengthState";
    private static final String TEXT_FILTER_STATE = "textFilter";
    private static final int MAX_WILDCARDS = 15; //due to computation reason
    private static final int TEXT_INPUT_OK = 0;
    private static final int TEXT_INPUT_TOO_MANY_WILDCARDS = 1;
    private static final int TEXT_INPUT_ONLY_WILDCARDS = 2;
    private static final int TEXT_INPUT_TOO_MANY_DIGITS = 3;
    private static final int TEXT_INPUT_EMPTY = 4;
    private boolean isTextNoWordsFoundVisible = false, isProgressBarLoadingWordsVisible = false;
    private String dictionaryFilename;
    private int dictionaryAlphabetSize, maxWordLength;

    public static WildcardsFragment newInstance(Dictionary dictionary) {

        Bundle args = new Bundle();
        args.putString(MainActivity.DICTIONARY_FILENAME, dictionary.getFilename());
        args.putInt(MainActivity.DICTIONARY_ALPHABET_SIZE, dictionary.getAlphabetSize());
        args.putInt(MainActivity.DICTIONARY_MAX_WORD, dictionary.getMaxWordLength());
        WildcardsFragment fragment = new WildcardsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        wildcardsViewModel = ViewModelProviders.of(this).get(WildcardsViewModel.class);
        if(getArguments() != null) {
            dictionaryFilename = getArguments().getString(MainActivity.DICTIONARY_FILENAME);
            dictionaryAlphabetSize = getArguments().getInt(MainActivity.DICTIONARY_ALPHABET_SIZE);
            maxWordLength = getArguments().getInt(MainActivity.DICTIONARY_MAX_WORD);
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
        View view = inflater.inflate(R.layout.fragment_wildcards, container, false);
        textinput = (TextInputEditText) view.findViewById(R.id.wildcards_textinput);
        textInputLayout = (TextInputLayout) view.findViewById(R.id.wildcards_textinput_layout);
        textFilter = (TextView) view.findViewById(R.id.wildcards_filter_text);
        find = (Button) view.findViewById(R.id.wildcards_find_button);
        wordslist = (RecyclerView) view.findViewById(R.id.wildcards_words_list);
        textDescription = (TextView) view.findViewById(R.id.wildcards_text_description);
        textNoWordsFound = (TextView) view.findViewById(R.id.wildcards_text_no_words_found);
        progressBarLoadingWords = (ProgressBar) view.findViewById(R.id.wildcards_progressBarLoadingWords);
        find.setOnClickListener(onClickListener);
        //textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters()));
        //textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), true,  maxWordLength));
        textDescription.setText(R.string.text_description_wildcards);
        textFilter.setText(getString(R.string.wildcards_fragment_no_filter));
        if(maxWordLength != 0) {
            textInputLayout.setCounterEnabled(true);
            textInputLayout.setCounterMaxLength(maxWordLength);
            textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), true,  maxWordLength));
        } else
            textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), true));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null) {
            String textInserted = savedInstanceState.getString(TEXT_INSERTED_STATE);
            textinput.setText(textInserted);
            String stringFilter = savedInstanceState.getString(TEXT_FILTER_STATE);
            textFilter.setText(stringFilter);
            if(wildcardsViewModel.wordsFound != null && adapter == null) {
                adapter = new AnagramRecyclerViewAdapter(wildcardsViewModel.wordsFound);
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
            getActivity().setTitle(getString(R.string.nav_item_wildcards));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(textinput != null && textinput.getText() != null)
            outState.putString(TEXT_INSERTED_STATE, textinput.getText().toString());
        if(textFilter != null && textFilter.getText() != null)
            outState.putString(TEXT_FILTER_STATE, textFilter.getText().toString());
        outState.putBoolean(TEXT_NO_WORDS_FOUND_STATE, isTextNoWordsFoundVisible);
        outState.putBoolean(TEXT_PROGRESSBAR_LOADING_WORDS_STATE, isProgressBarLoadingWordsVisible);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.wildcards_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_filter) {
            if (getActivity() != null) {
                FilterDialog filterDialog = new FilterDialog(getActivity(),
                        getString(R.string.filter_dialog_title),
                        getString(R.string.filter_dialog_message),
                        wildcardsViewModel.filteredLetters,
                        dictionaryAlphabetSize);
                filterDialog.setOnButtonApplyListener(onButtonApplyListener);
                filterDialog.show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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
            wildcardsViewModel.textPressed = textInserted;
            //Maybe move this check in the input filter
            int check = checkTextInserted(textInserted);
            switch (check) {
                case TEXT_INPUT_OK:
                    isProgressBarLoadingWordsVisible = true;
                    progressBarLoadingWords.setVisibility(View.VISIBLE);
                    isTextNoWordsFoundVisible = false;
                    textNoWordsFound.setVisibility(View.INVISIBLE);

                    trieViewModel.getTrie().observe(getActivity(), new Observer<DoubleArrayTrie>() {
                        @Override
                        public void onChanged(DoubleArrayTrie trie) {
                            if(wildcardsViewModel.isFilterApplied)
                                task = new FindWildcards(trie, wildcardsViewModel.filteredLetters);
                            else
                                task = new FindWildcards(trie);
                            task.setListener(wordsFound -> {

                                if (isProgressBarLoadingWordsVisible) {
                                    isProgressBarLoadingWordsVisible = false;
                                    progressBarLoadingWords.setVisibility(View.INVISIBLE);
                                }
                                if (wordsFound.isEmpty()) {
                                    isTextNoWordsFoundVisible = true;
                                    textNoWordsFound.setVisibility(View.VISIBLE);
                                }

                                int size = wildcardsViewModel.wordsFound.size();
                                wildcardsViewModel.wordsFound.clear();
                                if (adapter == null) {
                                    adapter = new AnagramRecyclerViewAdapter(wildcardsViewModel.wordsFound);
                                    wordslist.setAdapter(adapter);
                                } else
                                    adapter.notifyItemRangeRemoved(0, size);
                                wildcardsViewModel.wordsFound.addAll(wordsFound);
                                wildcardsViewModel.wordsBackup.addAll(wordsFound);
                                adapter.notifyItemRangeInserted(0, wordsFound.size());
                            });
                            task.execute(textInserted);
                        }
                    });
                    break;
                case TEXT_INPUT_TOO_MANY_WILDCARDS:
                    Toast.makeText(getContext(), getString(R.string.wildcards_fragment_toast_too_many_wildcards), Toast.LENGTH_SHORT).show();
                    break;
                case TEXT_INPUT_ONLY_WILDCARDS:
                    Toast.makeText(getContext(), getString(R.string.wildcards_fragment_toast_only_wildcards), Toast.LENGTH_SHORT).show();
                    break;
                case TEXT_INPUT_TOO_MANY_DIGITS:
                    Toast.makeText(getContext(), getString(R.string.toast_max_digits_exceeded), Toast.LENGTH_SHORT).show();
                    break;
                default: //TEXT_INPUT_EMPTY
                    break; //do nothing
            }
        }
    }

    private TrieViewModel.MaxWordLengthListener maxWordLengthListener = (maxWordLength -> {
        //Log.v(MainActivity.TAG, "WildcardsFragment/ maxWordLengthListener called");
        this.maxWordLength = maxWordLength;
        if(maxWordLength != 0) {
            textInputLayout.setCounterEnabled(true);
            textInputLayout.setCounterMaxLength(maxWordLength);
            textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), true, maxWordLength));
        }
    });

    private FilterDialog.OnButtonApplyListener onButtonApplyListener = (filteredLetters) -> {
        if(!filteredLetters.isEmpty()) {
            wildcardsViewModel.isFilterApplied = true;
            //Remove possible duplicates
            TreeSet<Character> letters = new TreeSet<>();
            for(char c : filteredLetters.toCharArray())
                letters.add(c);
            Iterator<Character> it = letters.iterator();
            StringBuilder sb = new StringBuilder();
            while(it.hasNext())
                sb.append(it.next());
            wildcardsViewModel.filteredLetters = sb.toString().toLowerCase();

            //Update Filter TextView
            String filterText = getString(R.string.wildcards_fragment_yes_filter) + " " + wildcardsViewModel.filteredLetters.toUpperCase();
            textFilter.setText(filterText);

            //Refresh current RecyclerView Items
            if(!wildcardsViewModel.wordsBackup.isEmpty() && checkTextInserted(wildcardsViewModel.textPressed) == TEXT_INPUT_OK && adapter != null) {
                //Apply filter to current words
                if(wildcardsViewModel.wordsFound.size() != wildcardsViewModel.wordsBackup.size()) {
                    wildcardsViewModel.wordsFound.clear();
                    wildcardsViewModel.wordsFound.addAll(wildcardsViewModel.wordsBackup);
                }
                applyFilterOnItems(wildcardsViewModel.wordsFound);
            }
        } else {
            wildcardsViewModel.isFilterApplied = false;
            wildcardsViewModel.filteredLetters = null;
            textFilter.setText(getString(R.string.wildcards_fragment_no_filter));

            //Bring back all words
            if(wildcardsViewModel.wordsFound.size() != wildcardsViewModel.wordsBackup.size() && adapter != null) {
                wildcardsViewModel.wordsFound.clear();
                wildcardsViewModel.wordsFound.addAll(wildcardsViewModel.wordsBackup);
                adapter.notifyDataSetChanged();
            }
        }
    };

    private void applyFilterOnItems(ArrayList<String> words) {
        int[] wildcardsPositions = WordUtils.getWildcardsPositions(wildcardsViewModel.textPressed, DoubleArrayTrie.WILDCARD);
        char[] filter = wildcardsViewModel.filteredLetters.toCharArray();
        Iterator<String> it2 = words.iterator();
        while(it2.hasNext())
            if(WordUtils.isWordToBeFiltered(it2.next().toLowerCase(), wildcardsPositions, filter))
                it2.remove();
        adapter.notifyDataSetChanged();
    }

    /**
     * Check if there is at least one letter (and not only wildcards)
     * and if there aren't more wildcards than
     * @see #MAX_WILDCARDS
     * @return {@link #TEXT_INPUT_OK} if text inserted is correct,
     * {@link #TEXT_INPUT_ONLY_WILDCARDS} if there isn't at least one letter and
     * {@link #TEXT_INPUT_TOO_MANY_WILDCARDS} if there are more wildcards than
     * {@link #MAX_WILDCARDS}
     */
    private int checkTextInserted(String text) {
        if(text.length() == 0)
            return TEXT_INPUT_EMPTY;
        if(text.length() > maxWordLength  && maxWordLength != MainActivity.MAX_WORD_LENGTH_DEFAULT_VALUE)
            return TEXT_INPUT_TOO_MANY_DIGITS;
        int wildcardsCount = 0;
        int i = 0;
        char[] cs = text.toCharArray();
        while(i < cs.length) {
            if (cs[i] == DoubleArrayTrie.WILDCARD)
                wildcardsCount++;
            if(wildcardsCount > MAX_WILDCARDS)
                return TEXT_INPUT_TOO_MANY_WILDCARDS;
            i++;
        }
        return wildcardsCount < cs.length ? TEXT_INPUT_OK : TEXT_INPUT_ONLY_WILDCARDS;
    }

    private static class FindWildcards extends AsyncTask<String, Void, Void> {

        private DoubleArrayTrie trie;
        private ArrayList<String> words;
        private FindWildcardsTaskListener listener;
        private boolean isFilterApplied;
        private String filteredLetters;

        public FindWildcards(DoubleArrayTrie trie) {
            this.trie = trie;
            isFilterApplied = false;
        }

        public FindWildcards(DoubleArrayTrie trie, String filteredLetters) {
            this.trie = trie;
            isFilterApplied = true;
            this.filteredLetters = filteredLetters;
        }

        @Override
        protected Void doInBackground(String... strings) {
            String textInserted = strings[0];
            if(isFilterApplied)
                words = (ArrayList<String>) trie.query(textInserted, filteredLetters.toCharArray());
            else
                words = (ArrayList<String>) trie.query(textInserted);
            if(!words.isEmpty()) {
                WordUtils.sortAndRemoveDuplicates(words);
                WordUtils.wordsToUpperCase(words);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(listener != null)
                listener.onFindWildcardsTaskFinished(words);
        }

        public void setListener(FindWildcardsTaskListener listener) {
            this.listener = listener;
        }

        public interface FindWildcardsTaskListener {

            void onFindWildcardsTaskFinished(ArrayList<String> wordsFound);

        }
    }
}
