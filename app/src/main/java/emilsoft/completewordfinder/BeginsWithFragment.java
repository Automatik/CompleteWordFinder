package emilsoft.completewordfinder;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import utils.KeyboardHelper;
import utils.MyInputFilter;
import utils.WordUtils;
import viewmodel.BeginsWithViewModel;
import viewmodel.TrieViewModel;
import viewmodel.TrieViewModelFactory;

public class BeginsWithFragment extends Fragment {

    private Button find;
    private TextInputEditText textinput;
    private RecyclerView wordslist;
    private TextView textDescription, textNoWordsFound;
    private AnagramRecyclerViewAdapter adapter;
    private TrieViewModel trieViewModel;
    private BeginsWithViewModel beginsWithViewModel;
    private ProgressBar progressBarLoadingWords;

    private static final String TEXT_INSERTED_STATE = "textInserted";
    private static final String TEXT_NO_WORDS_FOUND_STATE = "textNoWordsFound";
    private static final String TEXT_PROGRESSBAR_LOADING_WORDS_STATE = "textNoWordsFound";
    private boolean isTextNoWordsFoundVisible = false, isProgressBarLoadingWordsVisible = false;

    public BeginsWithFragment() {}

    public static BeginsWithFragment newInstance() {

        Bundle args = new Bundle();

        BeginsWithFragment fragment = new BeginsWithFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beginsWithViewModel = ViewModelProviders.of(this).get(BeginsWithViewModel.class);
        trieViewModel = ViewModelProviders.of(getActivity(),
                new TrieViewModelFactory(getActivity().getApplication(), MainActivity.DICTIONARY)).get(TrieViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anagram, container, false);
        textinput = (TextInputEditText) view.findViewById(R.id.textinput);
        find = (Button) view.findViewById(R.id.find_button);
        wordslist = (RecyclerView) view.findViewById(R.id.words_list);
        textDescription = (TextView) view.findViewById(R.id.text_description);
        textNoWordsFound = (TextView) view.findViewById(R.id.text_no_words_found);
        progressBarLoadingWords = (ProgressBar) view.findViewById(R.id.progressBarLoadingWords);
        find.setOnClickListener(onClickListener);
        textinput.setOnFocusChangeListener(onFocusChangeListener);
        textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters()));
        textDescription.setText(R.string.text_description_begins_with);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null) {
            String textInserted = savedInstanceState.getString(TEXT_INSERTED_STATE);
            textinput.setText(textInserted);
            if(beginsWithViewModel.wordsFound != null && adapter == null) {
                adapter = new AnagramRecyclerViewAdapter(beginsWithViewModel.wordsFound);
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                KeyboardHelper.hideKeyboard(getActivity());
                String textInserted = textinput.getText().toString().toLowerCase();
                Log.v(MainActivity.TAG,"Text Inserted: "+textInserted);

                isProgressBarLoadingWordsVisible = true;
                progressBarLoadingWords.setVisibility(View.VISIBLE);
                isTextNoWordsFoundVisible = false;
                textNoWordsFound.setVisibility(View.INVISIBLE);

                trieViewModel.getTrie().observe(getActivity(), new Observer<Trie>() {
                    @Override
                    public void onChanged(Trie trie) {
                        beginsWithViewModel.wordsFound = trie.startsWith(textInserted);
                        //No need to sort and remove duplicates
                        WordUtils.wordsToUpperCase(beginsWithViewModel.wordsFound);
                        Log.v(MainActivity.TAG, "Words[0]:"+beginsWithViewModel.wordsFound.get(0));
                        Log.v(MainActivity.TAG, "Words["+Integer.toString(beginsWithViewModel.wordsFound.size()-1)+"]:"+beginsWithViewModel.wordsFound.get(beginsWithViewModel.wordsFound.size()-1));
                        adapter = new AnagramRecyclerViewAdapter(beginsWithViewModel.wordsFound);
                        wordslist.setAdapter(adapter);
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

}
