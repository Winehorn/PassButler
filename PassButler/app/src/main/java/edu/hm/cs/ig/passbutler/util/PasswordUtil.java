package edu.hm.cs.ig.passbutler.util;

import android.content.Context;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import edu.hm.cs.ig.passbutler.R;

/**
 * Created by Florian Kraus on 28.11.2017.
 */

public class PasswordUtil {
    public static int checkPassword(String password) {
        Zxcvbn zxcvbn = new Zxcvbn();
        Strength strength = zxcvbn.measure(password);
        return strength.getScore();
    }

    public static char[] generatePassword(Context context) {
        final String lower = context.getString(R.string.password_generator_chars_lowercase);
        final String upper = context.getString(R.string.password_generator_chars_uppercase);
        final String numbers = context.getString(R.string.password_generator_chars_numbers);
        final String special = context.getString(R.string.password_generator_chars_special);

        final int length = context.getResources().getInteger(R.integer.password_generator_default_length);
        char[] password = new char[length];

        SecureRandom random = new SecureRandom();


        // TODO: concatenate strings and chose random char from that string
        List<String> charCategories = new ArrayList<>(4);
        charCategories.add(lower);
        charCategories.add(upper);
        charCategories.add(numbers);
        charCategories.add(special);

        for (int i = 0; i < length; i++) {
            String charCategory = charCategories.get(random.nextInt(charCategories.size()));
            int position = random.nextInt(charCategory.length());
            password[i] = charCategory.charAt(position);
        }

        return password;
    }

    public static char[] generatePassword(boolean useLower, boolean useUpper,
                                          boolean useNumbers, boolean useSpecial,
                                          int length, Context context) {
        final String lower = context.getString(R.string.password_generator_chars_lowercase);
        final String upper = context.getString(R.string.password_generator_chars_uppercase);
        final String numbers = context.getString(R.string.password_generator_chars_numbers);
        final String special = context.getString((R.string.password_generator_chars_special));

        char[] password = new char[length];

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
                password[i] = charCategory.charAt(position);
            }


        return password;
    }
}
