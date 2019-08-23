package emilsoft.completewordfinder.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class MyClipboardManager {

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied Text", text);
        clipboardManager.setPrimaryClip(clipData);
    }

}
