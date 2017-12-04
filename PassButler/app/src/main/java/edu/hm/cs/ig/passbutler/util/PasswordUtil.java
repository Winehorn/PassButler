package edu.hm.cs.ig.passbutler.util;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

/**
 * Created by Florian Kraus on 28.11.2017.
 */

public class PasswordUtil {
    public static int checkPassword(String password) {
        Zxcvbn zxcvbn = new Zxcvbn();
        Strength strength = zxcvbn.measure(password);
        return strength.getScore();
    }
}
