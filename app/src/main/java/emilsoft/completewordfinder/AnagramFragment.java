package emilsoft.completewordfinder;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputEditText;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import viewmodel.AnagramViewModel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;


public class AnagramFragment extends Fragment {

    private Button find;
    private TextInputEditText textinput;
    private RecyclerView wordslist;
    private AnagramRecyclerViewAdapter adapter;
    private AnagramViewModel anagramViewModel;

    private static final int[] PRIMES = new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31,
            37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103,
            107, 109, 113 };

    public static final int findButtonShiftDownDP = 12;
    private static final String TEXT_INSERTED_STATE = "textInserted";

    public AnagramFragment() {

    }

    public static AnagramFragment newInstance() {
        
        Bundle args = new Bundle();
        
        AnagramFragment fragment = new AnagramFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        anagramViewModel = ViewModelProviders.of(this).get(AnagramViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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
            if(anagramViewModel.wordsFound != null && adapter == null) {
                adapter = new AnagramRecyclerViewAdapter(anagramViewModel.wordsFound);
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
                new FindAnagrams(getContext(), MainActivity.DICTIONARY).execute(textInserted);
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
                int pixel_topMargin = convertDPtoPX(Objects.requireNonNull(getActivity()), findButtonShiftDownDP);
                parameter.setMargins(parameter.leftMargin, pixel_topMargin, parameter.rightMargin, parameter.bottomMargin);
                find.setLayoutParams(parameter);
                find.requestLayout();
            }
        }
    };

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

    public static int convertDPtoPX(Context context, int dp) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }

    private class FindAnagrams extends AsyncTask<String, String, Void> {

        private Context context;
        private String filename;
        private ArrayList<String> words;

        public FindAnagrams(Context context, String filename) {
            this.context = context;
            this.filename = filename;
            words = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            if (wordslist != null) {
                adapter = new AnagramRecyclerViewAdapter(words);
                wordslist.setAdapter(adapter);
            }
        }

        @Override
        protected Void doInBackground(String... strings) {
            String word = strings[0];
            long pw = calculateProduct(word.toUpperCase().toCharArray()); //Product of the word to match
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
                String line;
                while ((line = reader.readLine()) != null) {
                    char[] letters = line.toUpperCase().toCharArray();
                    long product = calculateProduct(letters);
                    if (product == pw) {
                        //words.add(line);
                        //int lastInsertedPosition = words.size() - 1;
                        publishProgress(line);
                    }
                }
                reader.close();
                return null;
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.v(MainActivity.TAG, "exception in asynctask FindAnagrams");
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String newWord = values[0];
            words.add(newWord);
            adapter.notifyItemInserted(words.size()-1);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            anagramViewModel.wordsFound = words;
        }
    }

}
