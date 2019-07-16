package emilsoft.completewordfinder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AnagramRecyclerViewAdapter extends RecyclerView.Adapter<AnagramRecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> mWords;
    private int defaultBackgroundColor, alternativeBackgroundColor, whiteColor, blackColor;

    public AnagramRecyclerViewAdapter(ArrayList<String> words) {
        if (words == null)
            mWords = new ArrayList<>(0);
        else mWords = words;
        defaultBackgroundColor = Color.parseColor("#FAFAFA");
        alternativeBackgroundColor = Color.parseColor("#008577");
        whiteColor = Color.parseColor("#FFFFFF");
        blackColor = Color.parseColor("#000000");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_anagram_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mWord = mWords.get(position);
        holder.mText.setText(holder.mWord);
        if(position % 2 == 0) {
            holder.mText.setBackgroundColor(defaultBackgroundColor);
            holder.mText.setTextColor(blackColor);
        }
        else {
            holder.mText.setBackgroundColor(alternativeBackgroundColor);
            holder.mText.setTextColor(whiteColor);
        }
    }

    @Override
    public int getItemCount() {
        if (mWords != null)
            return mWords.size();
        return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView mText;
        public String mWord;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mText = (TextView) view.findViewById(R.id.anagram_list_word);

        }
    }

}
