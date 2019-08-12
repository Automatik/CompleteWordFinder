package emilsoft.completewordfinder;

import android.content.Context;
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
import emilsoft.completewordfinder.viewmodel.SubAnagramsViewModel;
import emilsoft.completewordfinder.viewmodel.TrieViewModel;
import emilsoft.completewordfinder.viewmodel.TrieViewModelFactory;

public class SubAnagramsFragment extends Fragment {

    private Button find;
    private TextInputEditText textinput;
    private TextInputLayout textInputLayout;
    private RecyclerView wordslist;
    private TextView textDescription, textNoWordsFound;
    private HeaderRecyclerViewAdapter adapter;
    private TrieViewModel trieViewModel;
    private SubAnagramsViewModel subAnagramsViewModel;
    private ProgressBar progressBarLoadingWords;
    private FindSubAnagrams task;

    private static final String TEXT_INSERTED_STATE = "textInserted";
    private static final String TEXT_NO_WORDS_FOUND_STATE = "textNoWordsFound";
    private static final String TEXT_PROGRESSBAR_LOADING_WORDS_STATE = "textNoWordsFound";
    private static final int MAX_WORD_LENGTH = 15; //due to computation reason
    private boolean isTextNoWordsFoundVisible = false, isProgressBarLoadingWordsVisible = false;
    private String dictionaryFilename;
    private int dictionaryAlphabetSize;
    private boolean isWordOrderAscending;

    public SubAnagramsFragment() {
        isWordOrderAscending = MainActivity.WORD_ORDER_DEFAULT;
    }

    public static SubAnagramsFragment newInstance(Dictionary dictionary, boolean isWordOrderAscending) {

        Bundle args = new Bundle();
        args.putString(MainActivity.DICTIONARY_FILENAME, dictionary.getFilename());
        args.putInt(MainActivity.DICTIONARY_ALPHABET_SIZE, dictionary.getAlphabetSize());
        args.putInt(MainActivity.DICTIONARY_MAX_WORD, dictionary.getMaxWordLength());
        args.putBoolean(MainActivity.IS_WORD_ORDER_ASCENDING, isWordOrderAscending);
        SubAnagramsFragment fragment = new SubAnagramsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subAnagramsViewModel = ViewModelProviders.of(this).get(SubAnagramsViewModel.class);
        if(getArguments() != null) {
            dictionaryFilename = getArguments().getString(MainActivity.DICTIONARY_FILENAME);
            dictionaryAlphabetSize = getArguments().getInt(MainActivity.DICTIONARY_ALPHABET_SIZE);
            isWordOrderAscending = getArguments().getBoolean(MainActivity.IS_WORD_ORDER_ASCENDING);
        }
        Dictionary dictionary = new Dictionary(dictionaryFilename, dictionaryAlphabetSize);
        trieViewModel = ViewModelProviders.of(getActivity(),
                new TrieViewModelFactory(getActivity().getApplication(), dictionary)).get(TrieViewModel.class);
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
        textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), MAX_WORD_LENGTH));
        textDescription.setText(R.string.text_description_sub_anagrams);
        textInputLayout.setCounterEnabled(true);
        textInputLayout.setCounterMaxLength(MAX_WORD_LENGTH);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null) {
            String textInserted = savedInstanceState.getString(TEXT_INSERTED_STATE);
            textinput.setText(textInserted);
            if(subAnagramsViewModel.wordsFound != null && adapter == null) {
                adapter = new HeaderRecyclerViewAdapter(subAnagramsViewModel.wordsFound, subAnagramsViewModel.headersIndex);
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
    public void onResume() {
        super.onResume();
        if(getActivity() != null)
            getActivity().setTitle(getString(R.string.nav_item_sub_anagrams));
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

                if(textInserted.length() > MAX_WORD_LENGTH) {
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
                        task = new FindSubAnagrams(trie, isWordOrderAscending);
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
                            int size = subAnagramsViewModel.wordsFound.size() + subAnagramsViewModel.headersIndex.length;

                            subAnagramsViewModel.wordsFound.clear();
                            if(adapter == null) {
                                //adapter = new AnagramRecyclerViewAdapter(subAnagramsViewModel.wordsFound);
                                adapter = new HeaderRecyclerViewAdapter(subAnagramsViewModel.wordsFound, subAnagramsViewModel.headersIndex);
                                wordslist.setAdapter(adapter);
                            }
                            else
                                adapter.notifyItemRangeRemoved(0, size);

                            subAnagramsViewModel.wordsFound.addAll(wordsFound);
                            subAnagramsViewModel.headersIndex = headersIndex;
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

    private static class FindSubAnagrams extends AsyncTask<String, Void, Void> {

        private DoubleArrayTrie trie;
        private ArrayList<String> words;
        private int[] headersIndex;
        private FindSubAnagramsTaskListener listener;
        private boolean isWordOrderAscending;

        public FindSubAnagrams(DoubleArrayTrie trie, boolean isWordOrderAscending) {
            this.trie = trie;
            this.isWordOrderAscending = isWordOrderAscending;
        }

        @Override
        protected Void doInBackground(String... strings) {
            String textInserted = strings[0];
            Log.v(MainActivity.TAG,"Beginning permute");
            long start = System.nanoTime();
            words = (ArrayList<String>) trie.permute(textInserted.toCharArray());
            if(!words.isEmpty()) {
                headersIndex = WordUtils.sortByWordLength(words, isWordOrderAscending);
                //WordUtils.sortAndRemoveDuplicates(words);
                WordUtils.wordsToUpperCase(words);
            }
            long stop = System.nanoTime();
            Log.v(MainActivity.TAG,"SubAnagrams time: "+(((stop-start)/(double)1000000))+" ms");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(listener != null)
                listener.onFindSubAnagramsTaskFinished(words, headersIndex);
        }

        public void setListener(FindSubAnagramsTaskListener listener) {
            this.listener = listener;
        }

        public interface FindSubAnagramsTaskListener {

            void onFindSubAnagramsTaskFinished(ArrayList<String> wordsFound, int[] headersIndex);

        }
    }
}
