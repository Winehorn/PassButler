package edu.hm.cs.ig.passbutler.util;

import android.util.Base64;

/**
 * Created by dennis on 23.12.17.
 */

public class StringUtil {

    public static String fromBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] toBase64(String s) {
        return Base64.decode(s, Base64.DEFAULT);
    }
}
