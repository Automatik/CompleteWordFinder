package emilsoft.completewordfinder.utils;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

public class DigitsInputFilter implements InputFilter {

    // Use this class instead of
    // android:digits="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    // in the layout xml TextInputEditText

    //According to the alphabet used, the letters with accent should not pass the input filter
    //Otherwise, when implementing dictionaries with accents look at this:
    //https://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette/15191508

    // https://stackoverflow.com/a/20325852/4008428
    // https://developer.android.com/reference/android/text/InputFilter.html#filter(java.lang.CharSequence

    private boolean allowWildcard; //the "?" character is allowed

    private static final char wildcard = '?';

    private static final int a = 'a';
    private static final int A = 'A';
    private static final int z = 'z';
    private static final int Z = 'Z';

    public DigitsInputFilter(boolean allowWildcard) {
        this.allowWildcard = allowWildcard;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        boolean keepOriginal = true;
        StringBuilder sb = new StringBuilder(end - start);
        for(int i = start; i < end; i++) {
            char c = source.charAt(i);
            if(isCharAllowed(c))
                sb.append(c);
            else
                keepOriginal = false;
        }

        if(keepOriginal)
            return null;
        else {
            if(source instanceof Spanned) {
                SpannableString sp = new SpannableString(sb);
                TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                return sp;
            } else
                return sb;
        }
    }

    private boolean isCharAllowed(char c) {
        //return Character.isLetter(c) || (allowWildcard && c == wildcard);
        int ch = (int) c;
        if((ch >= a && ch <= z) || (ch >= A && ch <= Z))
            return true;
        return allowWildcard && c == wildcard;
    }
}
