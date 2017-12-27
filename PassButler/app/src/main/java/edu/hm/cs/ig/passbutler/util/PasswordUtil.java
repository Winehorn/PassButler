package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.util.Log;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import org.apache.commons.lang3.NotImplementedException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.password.PassphraseDB;

/**
 * Created by Florian Kraus on 28.11.2017.
 */

public class PasswordUtil {
    private static String TAG = PasswordUtil.class.getName();

    public static int checkPassword(String password) {
        Zxcvbn zxcvbn = new Zxcvbn();
        Strength strength = zxcvbn.measure(password);
        return strength.getScore();
    }

    public static String generatePassword(boolean useLower, boolean useUpper,
                                          boolean useNumbers, boolean useSpecial,
                                          int length, Context context) {
        final String lower = context.getString(R.string.password_generator_chars_lowercase);
        final String upper = context.getString(R.string.password_generator_chars_uppercase);
        final String numbers = context.getString(R.string.password_generator_chars_numbers);
        final String special = context.getString((R.string.password_generator_chars_special));

        StringBuilder builder = new StringBuilder();

        SecureRandom random = new SecureRandom();

        List<String> charCategories = new ArrayList<>(4);
        if (useLower) {
            charCategories.add(lower);
        }
        if (useUpper) {
            charCategories.add(upper);
        }
        if (useNumbers) {
            charCategories.add(numbers);
        }
        if (useSpecial) {
            charCategories.add(special);
        }

            for (int i = 0; i < length; i++) {
                String charCategory = charCategories.get(random.nextInt(charCategories.size()));
                int position = random.nextInt(charCategory.length());
                builder.append(charCategory.charAt(position));
            }


        return builder.toString();
    }

    public static String generatePassphrase(int length, Context context) {
        final int numberOfDice = 5;
        final String delimiter = " ";

        Log.i(TAG, "generatePassphrase: Generating phrase with " + length + " words.");

        PassphraseDB db = new PassphraseDB(context);

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int id = getDiceRoll(numberOfDice);
            builder.append(db.getWordById(id))
                    .append(delimiter);
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    private static int getDiceRoll(int numberOfDices) {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < numberOfDices; i++) {
            builder.append(secureRandom.nextInt(6) + 1);
        }
        return Integer.parseInt(builder.toString());
    }
}
