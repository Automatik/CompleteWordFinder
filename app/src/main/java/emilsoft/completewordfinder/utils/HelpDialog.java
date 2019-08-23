package emilsoft.completewordfinder.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import emilsoft.completewordfinder.R;

public class HelpDialog {

    private AlertDialog helpDialog;

    public HelpDialog(Activity activity, String title, String helpMessage, String helpExample) {
        ViewGroup viewGroup = activity.findViewById(R.id.content_frame);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.help_dialog, viewGroup, false);
        TextView titleText = dialogView.findViewById(R.id.help_title);
        TextView messageText = dialogView.findViewById(R.id.help_message);
        Button buttonOk = dialogView.findViewById(R.id.help_button_ok);
        //The example title is already fixed
        TextView exampleText = dialogView.findViewById(R.id.help_example_message);
        titleText.setText(title);
        messageText.setText(helpMessage);
        exampleText.setText(helpExample);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
        helpDialog = builder.create();
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog.dismiss();
            }
        });
    }

    public void show() {
        helpDialog.show();
    }

}
