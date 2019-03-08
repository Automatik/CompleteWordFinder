package emilsoft.completewordfinder;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import viewmodel.TrieViewModel;
import viewmodel.TrieViewModelFactory;
import viewmodel.WordsInPatternViewModel;

public class WordsInPatternFragment extends Fragment {

    private Button find;
    private TextInputEditText textinput;
    private RecyclerView wordslist;
    private AnagramRecyclerViewAdapter adapter;
    private TrieViewModel trieViewModel;
    private WordsInPatternViewModel wordsInPatternViewModel;

    private static final String TEXT_INSERTED_STATE = "textInserted";

    public static WordsInPatternFragment newInstance() {

        Bundle args = new Bundle();

        WordsInPatternFragment fragment = new WordsInPatternFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wordsInPatternViewModel = ViewModelProviders.of(this).get(WordsInPatternViewModel.class);
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
        find.setOnClickListener(onClickListener);
        textinput.setOnFocusChangeListener(onFocusChangeListener);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null) {
            String textInserted = savedInstanceState.getString(TEXT_INSERTED_STATE);
            textinput.setText(textInserted);
            if(wordsInPatternViewModel.wordsFound != null && adapter == null) {
                adapter = new AnagramRecyclerViewAdapter(wordsInPatternViewModel.wordsFound);
                wordslist.setAdapter(adapter);
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
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                String textInserted = textinput.getText().toString();
                Log.v(MainActivity.TAG,"Text Inserted: "+textInserted);
                //new FindWordsStartWith().execute(textInserted);

                trieViewModel.getTrie().observe(getActivity(), new Observer<Trie>() {
                    @Override
                    public void onChanged(Trie trie) {
                        wordsInPatternViewModel.wordsFound = trie.startsWith(textInserted);
                        Log.v(MainActivity.TAG, "Words[0]:"+wordsInPatternViewModel.wordsFound.get(0));
                        Log.v(MainActivity.TAG, "Words["+Integer.toString(wordsInPatternViewModel.wordsFound.size()-1)+"]:"+wordsInPatternViewModel.wordsFound.get(wordsInPatternViewModel.wordsFound.size()-1));
                        adapter = new AnagramRecyclerViewAdapter(wordsInPatternViewModel.wordsFound);
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
