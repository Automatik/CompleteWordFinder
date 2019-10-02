package emilsoft.completewordfinder.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import emilsoft.completewordfinder.R;
import emilsoft.completewordfinder.utils.WordUtils;

public class FilterDialog {

    private AlertDialog filterDialog;
    private OnButtonApplyListener listener;

    public FilterDialog(Activity activity, String title, String message, String filteredLetters, int dictionaryAlphabetSize) {
        ViewGroup viewGroup = activity.findViewById(R.id.content_frame);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.filter_dialog, viewGroup, false);
        TextView titleText = dialogView.findViewById(R.id.filter_title);
        TextView messageText = dialogView.findViewById(R.id.filter_message);
        TextInputLayout textInputLayout = dialogView.findViewById(R.id.textinput_filter_layout);
        TextInputEditText textinput = dialogView.findViewById(R.id.textinput_filter);
        Button applyButton = dialogView.findViewById(R.id.filter_button_apply);

        titleText.setText(title);
        messageText.setText(message);
        textInputLayout.setCounterEnabled(true);
        textInputLayout.setCounterMaxLength(dictionaryAlphabetSize);
        textinput.setFilters(WordUtils.addMyInputFilters(textinput.getFilters(), false, dictionaryAlphabetSize));
        if(filteredLetters != null)
            textinput.setText(filteredLetters);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
        filterDialog = builder.create();
        applyButton.setOnClickListener(view -> {
            if(listener != null && textinput.getText() != null) {
                listener.onClick(textinput.getText().toString());
            }
            filterDialog.dismiss();
        });
    }

    public void show() {
        filterDialog.show();
    }

    public void setOnButtonApplyListener(OnButtonApplyListener listener) {
        this.listener = listener;
    }

    public interface OnButtonApplyListener {

        void onClick(String filteredLetters);

    }

}
