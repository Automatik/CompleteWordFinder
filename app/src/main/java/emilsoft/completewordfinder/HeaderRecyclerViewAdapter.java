package emilsoft.completewordfinder;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HeaderRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> mWords;
    private int[] mHeadersIndex;
    private int defaultBackgroundColor, alternativeBackgroundColor, whiteColor, blackColor;
    private final static int TYPE_HEADER = 0, TYPE_ITEM = 1;

    public HeaderRecyclerViewAdapter(ArrayList<String> words, int[] headersIndex) {
        if (words == null)
            mWords = new ArrayList<>(0);
        else mWords = words;
        if(headersIndex == null)
            mHeadersIndex = new int[0];
        else mHeadersIndex = headersIndex;
        defaultBackgroundColor = Color.parseColor("#FAFAFA");
        alternativeBackgroundColor = Color.parseColor("#008577");
        whiteColor = Color.parseColor("#FFFFFF");
        blackColor = Color.parseColor("#000000");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_anagram_list_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            //viewType = TYPE_ITEM
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_anagram_list_item, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType;
        int i = binarySearch(mHeadersIndex, position);
        if(mHeadersIndex[i] + i == position)
            viewType = TYPE_HEADER;
        else viewType = TYPE_ITEM;

        if(viewType == TYPE_HEADER && holder instanceof HeaderViewHolder) {
            //Get the number of letters
            int numLetters = mWords.get(mHeadersIndex[i]).length();
            String text = numLetters + " LETTERS";
            HeaderViewHolder mHolder = (HeaderViewHolder) holder;
            mHolder.mText.setText(text);
            mHolder.mText.setTextColor(blackColor);
            mHolder.mText.setBackgroundColor(defaultBackgroundColor);
        } else if(viewType == TYPE_ITEM && holder instanceof ItemViewHolder) {
            //Get the correct position in mWords
            int mPos = position - (i + 1);
            String text = mWords.get(mPos);
            ItemViewHolder mHolder = (ItemViewHolder) holder;
            mHolder.mWord = text;
            mHolder.mText.setText(text);
            if(mPos % 2 == mHeadersIndex[i] % 2) { //instead of mPos % 2 == 0
                mHolder.mText.setBackgroundColor(defaultBackgroundColor);
                mHolder.mText.setTextColor(blackColor);
            } else {
                mHolder.mText.setBackgroundColor(alternativeBackgroundColor);
                mHolder.mText.setTextColor(whiteColor);
            }
        } else {
            throw new RuntimeException("No match for viewType "+viewType+" and holder");
        }

    }

    @Override
    public int getItemCount() {
        if(mWords != null && mHeadersIndex != null) {
            return mWords.size() + mHeadersIndex.length;
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType, i;
        i = binarySearch(mHeadersIndex, position);
        if(mHeadersIndex[i] + i == position)
            viewType = TYPE_HEADER;
        else viewType = TYPE_ITEM;
        return viewType;
    }

    public void setHeadersIndex(int[] headersIndex) {
        mHeadersIndex = headersIndex;
    }

    private static int binarySearch(int[] indexArray, int position) {
        int low = 0;
        int high = indexArray.length - 1;
        while(low < high) {
            int mid = (low + high) >>> 1; //Zero fill right shift
            int midVal = indexArray[mid];
            if(position >= indexArray[mid+1] + mid+1)
                low = mid + 1;
            else if(position < midVal + mid)
                high = mid - 1;
            else
                return mid;
        }
        return low;

    }

    private static int iterativeSearch(int[] indexArray, int position) {
        int i = 0;
        while(i < indexArray.length - 1 && !(position >= (indexArray[i] + i) && position < (indexArray[i+1] + i+1)))
            i++;
        return i;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView mText;
        public String mWord;

        public ItemViewHolder(View view) {
            super(view);
            mView = view;
            mText = (TextView) view.findViewById(R.id.anagram_list_word);

        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView mText;

        public HeaderViewHolder(View view) {
            super(view);
            mView = view;
            mText = (TextView) view.findViewById(R.id.anagram_list_header);

        }
    }
}
