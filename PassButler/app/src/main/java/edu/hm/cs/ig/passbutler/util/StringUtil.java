package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.util.Base64;

/**
 * Created by dennis on 23.12.17.
 */

public class StringUtil {

    public static String randomString(
            boolean useLower,
            boolean useUpper,
            boolean useNumbers,
            boolean useSpecial,
            int length,
            Context context) {
        return PasswordUtil.generatePassword(
                useLower,
                useUpper,
                useNumbers,
                useSpecial,
                length,
                context);
    }

    public static String fromBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] toBase64(String s) {
        return Base64.decode(s, Base64.DEFAULT);
    }
}
