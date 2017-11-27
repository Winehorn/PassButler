package edu.hm.cs.ig.passbutler.data;

import android.widget.EditText;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Created by dennis on 23.11.17.
 */

public class ArrayUtil {

    public static void clear (byte[] array) {
        Arrays.fill(array, (byte) 0);
    }

    public static char[] getContentAsCharArray(EditText editText) {
        int length = editText.length();
        char[] content = new char[length];
        editText.getText().getChars(0, length, content, 0);
        return content;
    }

    public static byte[] getContentAsByteArray(EditText editText) {
        char[] content = getContentAsCharArray(editText);
        return convertArrayAscii(content);
    }

    public static byte[] convertArrayAscii(char[] chars) {
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) chars[i];
        }
        return bytes;
    }

    public static byte[] concatAndClearSrc(byte[] a1, byte[] a2) {
        byte[] a3 = ArrayUtils.addAll(a1, a2);
        clear(a1);
        clear(a2);
        return a3;
    }

    public static byte[] concatAndClearSrc(byte[]... arrays) {
        byte[] ret = arrays[0];
        for(int i = 1; i < arrays.length; i++) {
            ret = concatAndClearSrc(ret, arrays[i]);
        }
        return ret;
    }
}
