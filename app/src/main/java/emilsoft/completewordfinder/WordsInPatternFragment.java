package emilsoft.completewordfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

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

    private static final String TEXT_INSERTED_STATE = "textInserted";
    private static final String TEXT_NO_WORDS_FOUND_STATE = "textNoWordsFound";
    private static final String TEXT_PROGRESSBAR_LOADING_WORDS_STATE = "textNoWordsFound";
    private boolean isTextNoWordsFoundVisible = false, isProgressBarLoadingWordsVisible = false;
    private String dictionaryFilename;
    private int dictionaryAlphabetSize, maxWordLength;

    public static WordsInPatternFragment newInstance(Dictionary dictionary) {

        Bundle args = new Bundle();
        args.putString(MainActivity.DICTIONARY_FILENAME, dictionary.getFilename());
        args.putInt(MainActivity.DICTIONARY_ALPHABET_SIZE, dictionary.getAlphabetSize());
        args.putInt(MainActivity.DICTIONARY_MAX_WORD, dictionary.getMaxWordLength());
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
        }
        //this is only an extra check
        if(maxWordLength == MainActivity.MAX_WORD_LENGTH_DEFAULT_VALUE) {
            SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            maxWordLength = sharedPreferences.getInt(getString(R.string.sharedpref_current_dictionary_max_word_length), MainActivity.MAX_WORD_LENGTH_DEFAULT_VALUE);
        }
        Dictionary dictionary = new Dictionary(dictionaryFilename, dictionaryAlphabetSize, maxWordLength);
        trieViewModel = ViewModelProviders.of(getActivity(),
                new TrieViewModelFactory(getActivity().getApplication(), dictionary, null)).get(TrieViewModel.class);
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
        textinput.setOnFocusChangeListener(onFocusChangeListener);
        //textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters()));
        textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), maxWordLength));
        textDescription.setText(R.string.text_description_words_contained);
        if(maxWordLength != 0) {
            textInputLayout.setCounterEnabled(true);
            textInputLayout.setCounterMaxLength(maxWordLength);
        }
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
        }
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
    }

    @Override
    public void onDestroy() {
        if(task != null)
            task.setListener(null);
        super.onDestroy();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                KeyboardHelper.hideKeyboard(getActivity());
                String textInserted = textinput.getText().toString().toLowerCase();
                Log.v(MainActivity.TAG,"Text Inserted: "+textInserted);

                if(textInserted.length() > maxWordLength) {
                    Toast.makeText(getContext(), getString(R.string.toast_max_digits_exceeded), Toast.LENGTH_SHORT).show();
                    return;
                }

                isProgressBarLoadingWordsVisible = true;
                progressBarLoadingWords.setVisibility(View.VISIBLE);
                isTextNoWordsFoundVisible = false;
                textNoWordsFound.setVisibility(View.INVISIBLE);

                trieViewModel.getTrie().observe(getActivity(), new Observer<DoubleArrayTrie>() {
                    @Override
                    public void onChanged(DoubleArrayTrie trie) {
                        task = new FindWordsInPattern(trie);
                        task.setListener((wordsFound, headersIndex) -> {

                            if(isProgressBarLoadingWordsVisible) {
                                isProgressBarLoadingWordsVisible = false;
                                progressBarLoadingWords.setVisibility(View.INVISIBLE);
                            }
                            if(wordsFound.isEmpty()) {
                                isTextNoWordsFoundVisible = true;
                                textNoWordsFound.setVisibility(View.VISIBLE);
                            }
                            //headersIndex.length is the number of headers to insert in the recyclerview
                            int size = wordsInPatternViewModel.wordsFound.size() + wordsInPatternViewModel.headersIndex.length;
                            wordsInPatternViewModel.wordsFound.clear();
                            if(adapter == null) {
                                adapter = new HeaderRecyclerViewAdapter(wordsInPatternViewModel.wordsFound, wordsInPatternViewModel.headersIndex);
                                wordslist.setAdapter(adapter);
                            }
                            else
                                adapter.notifyItemRangeRemoved(0, size);
                            wordsInPatternViewModel.wordsFound.addAll(wordsFound);
                            wordsInPatternViewModel.headersIndex = headersIndex;
                            adapter.setHeadersIndex(headersIndex);
                            adapter.notifyItemRangeInserted(0, wordsFound.size() + headersIndex.length);
                        });
                        task.execute(textInserted);
                    }
                });
            } catch (NullPointerException ex) {
                Log.v(MainActivity.TAG, "text inserted is null");
            }
        }
    };

    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                ConstraintLayout.LayoutParams parameter = (ConstraintLayout.LayoutParams) find.getLayoutParams();
                int pixel_topMargin = AnagramFragment.convertDPtoPX(Objects.requireNonNull(getActivity()), AnagramFragment.findButtonShiftDownDP);
                parameter.setMargins(parameter.leftMargin, pixel_topMargin, parameter.rightMargin, parameter.bottomMargin);
                find.setLayoutParams(parameter);
                find.requestLayout();
            }
        }
    };

    private static class FindWordsInPattern extends AsyncTask<String, Void, Void> {

        private DoubleArrayTrie trie;
        private ArrayList<String> words;
        private int[] headersIndex;
        private FindWordsInPatternTaskListener listener;

        public FindWordsInPattern(DoubleArrayTrie trie) {
            this.trie = trie;
        }

        @Override
        protected Void doInBackground(String... strings) {
            String textInserted = strings[0];
            Log.v(MainActivity.TAG,"Beginning match");
            long start = System.nanoTime();
            words = (ArrayList<String>) trie.match(textInserted);
            if(!words.isEmpty()) {
                //WordUtils.sortAndRemoveDuplicates(words);
                headersIndex = WordUtils.sortByWordLength(words);
                WordUtils.wordsToUpperCase(words);
            }
            long stop = System.nanoTime();
            Log.v(MainActivity.TAG,"WordsInPattern time: "+(((stop-start)/(double)1000000))+" ms");
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
