package edu.hm.cs.ig.passbutler.security;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.util.ArrayUtil;
import edu.hm.cs.ig.passbutler.util.NavigationUtil;

/**
 * Created by dennis on 23.11.17.
 */

public class KeyHolder {

    public static final String TAG = KeyHolder.class.getName();
    private static final KeyHolder instance = new KeyHolder();
    private Key key;

    private KeyHolder() { }

    public static KeyHolder getInstance() {
        return instance;
    }

    public void setKeyAndClearOld(Context context, Key newKey) throws DestroyFailedException {
        clearKey(context);
        key = newKey;
        Log.i(TAG, "New key was set in " + KeyHolder.class.getSimpleName() + " and old key was cleared.");
    }

    public Key getKey() throws MissingKeyException {
        Log.i(TAG, "Getting key from " + KeyHolder.class.getSimpleName() + ".");
        if(key != null) {
            Log.i(TAG, "A key is present.");
            return key;
        }
        Log.e(TAG, "No key is present.");
        throw new MissingKeyException("The " + KeyHolder.class.getSimpleName() + " must always contain a key when one is requested.");
    }

    public void clearKey(Context context) {
        Log.i(TAG, "Clearing key from " + KeyHolder.class.getSimpleName() + ".");
        if(key != null) {
            /*
             * This "hack" via reflection is needed since the destroy method of SecretKeySpec is
             * not yet implemented in Java 8 (For details see
             * https://stackoverflow.com/questions/38276866/destroying-secretkey-throws-destroyfailedexception)
             */
            if(key instanceof SecretKeySpec) {
                Log.i(TAG, "Key of type " + SecretKeySpec.class.getSimpleName() + " found.");
                try {
                    Field field = SecretKeySpec.class.getDeclaredField(
                            context.getString(R.string.secret_key_spec_internal_field_name));
                    field.setAccessible(true);
                    byte[] internalKeyField = (byte[]) field.get(key);
                    ArrayUtil.clear(internalKeyField);
                } catch(NoSuchFieldException | IllegalAccessException e) {
                    Log.e(TAG, "Could not get internal key field of " + SecretKeySpec.class.getSimpleName() + ". The clearing of the key failed.");
                    Toast.makeText(context, context.getString(R.string.destroy_failed_error_msg), Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Log.e(TAG, "The type of the key to clear is unknown.");
                Toast.makeText(context, context.getString(R.string.destroy_failed_error_msg), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Log.i(TAG, "No key is available. Skipping clearing.");
        }
        key = null;
        Log.i(TAG, "The clearing of the key finished.");
    }
}
