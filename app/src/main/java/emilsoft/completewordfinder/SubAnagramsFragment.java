package emilsoft.completewordfinder;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import utils.KeyboardHelper;
import utils.WordUtils;
import viewmodel.SubAnagramsViewModel;
import viewmodel.TrieViewModel;
import viewmodel.TrieViewModelFactory;

public class SubAnagramsFragment extends Fragment {

    private Button find;
    private TextInputEditText textinput;
    private RecyclerView wordslist;
    private TextView textDescription;
    private AnagramRecyclerViewAdapter adapter;
    private TrieViewModel trieViewModel;
    private SubAnagramsViewModel subAnagramsViewModel;

    private static final String TEXT_INSERTED_STATE = "textInserted";

    public static SubAnagramsFragment newInstance() {

        Bundle args = new Bundle();

        SubAnagramsFragment fragment = new SubAnagramsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subAnagramsViewModel = ViewModelProviders.of(this).get(SubAnagramsViewModel.class);
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
        find.setOnClickListener(onClickListener);
        textinput.setOnFocusChangeListener(onFocusChangeListener);
        textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters()));
        textDescription.setText(R.string.text_description_sub_anagrams);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null) {
            String textInserted = savedInstanceState.getString(TEXT_INSERTED_STATE);
            textinput.setText(textInserted);
            if(subAnagramsViewModel.wordsFound != null && adapter == null) {
                adapter = new AnagramRecyclerViewAdapter(subAnagramsViewModel.wordsFound);
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
                KeyboardHelper.hideKeyboard(getActivity());
                String textInserted = textinput.getText().toString().toLowerCase();
                Log.v(MainActivity.TAG,"Text Inserted: "+textInserted);

                trieViewModel.getTrie().observe(getActivity(), new Observer<Trie>() {
                    @Override
                    public void onChanged(Trie trie) {
                        subAnagramsViewModel.wordsFound = (ArrayList<String>) trie.permute(textInserted.toCharArray());
                        subAnagramsViewModel.wordsFound = (ArrayList<String>) WordUtils.sortAndRemoveDuplicates(subAnagramsViewModel.wordsFound);
                        WordUtils.wordsToUpperCase(subAnagramsViewModel.wordsFound);
                        Log.v(MainActivity.TAG, "Words[0]:"+subAnagramsViewModel.wordsFound.get(0));
                        Log.v(MainActivity.TAG, "Words["+Integer.toString(subAnagramsViewModel.wordsFound.size()-1)+"]:"+subAnagramsViewModel.wordsFound.get(subAnagramsViewModel.wordsFound.size()-1));
                        adapter = new AnagramRecyclerViewAdapter(subAnagramsViewModel.wordsFound);
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
