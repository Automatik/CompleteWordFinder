package emilsoft.completewordfinder.adapter;

import androidx.annotation.NonNull;
import androidx.core.os.BuildCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import emilsoft.completewordfinder.R;
import emilsoft.completewordfinder.utils.MyClipboardManager;

public class AnagramRecyclerViewAdapter extends RecyclerView.Adapter<AnagramRecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> mWords;
    private Context context;

    public AnagramRecyclerViewAdapter(ArrayList<String> words) {
        if (words == null)
            mWords = new ArrayList<>(0);
        else mWords = words;
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
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                holder.mText.setTextAppearance(R.style.RecyclerViewItemDefaultTheme);
            else {
                if(context != null)
                    holder.mText.setTextAppearance(context, R.style.RecyclerViewItemDefaultTheme);
            }
            holder.mText.setBackgroundResource(R.drawable.list_item_default_background);
        }
        else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                holder.mText.setTextAppearance(R.style.RecyclerViewItemAlternativeTheme);
            else {
                if(context != null)
                    holder.mText.setTextAppearance(context, R.style.RecyclerViewItemAlternativeTheme);
            }
            holder.mText.setBackgroundResource(R.drawable.list_item_alternative_background);
        }
    }

    @Override
    public int getItemCount() {
        if (mWords != null)
            return mWords.size();
        return 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public final View mView;
        public final TextView mText;
        public String mWord;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mText = (TextView) view.findViewById(R.id.anagram_list_word);
            view.setHapticFeedbackEnabled(true);
            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            // Return true to indicate the click was handled
            String text = mText.getText().toString();
            Context context = v.getContext();
            MyClipboardManager.copyToClipboard(context, text);
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            Toast.makeText(context, context.getString(R.string.toast_copied_to_clipboard), Toast.LENGTH_SHORT).show();
            return true;
        }
    }

}
