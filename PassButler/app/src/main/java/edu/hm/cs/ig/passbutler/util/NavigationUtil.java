package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.content.Intent;

import edu.hm.cs.ig.passbutler.entry.LogoActivity;
import edu.hm.cs.ig.passbutler.entry.UnlockActivity;

/**
 * Created by dennis on 12.12.17.
 */

public class NavigationUtil {

    public static void goToHomeScreen(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void goToUnlockActivity(Context context) {
        Intent intent = new Intent(context, UnlockActivity.class);
        context.startActivity(intent);
    }

    public static void goToLogoActivity(Context context) {
        Intent intent = new Intent(context, LogoActivity.class);
        context.startActivity(intent);
    }
}
