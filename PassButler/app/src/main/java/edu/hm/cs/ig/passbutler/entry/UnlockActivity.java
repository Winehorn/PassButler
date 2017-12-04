package edu.hm.cs.ig.passbutler.entry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.DestroyFailedException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.account_list.AccountListActivity;
import edu.hm.cs.ig.passbutler.data.AccountListHandler;
import edu.hm.cs.ig.passbutler.util.ArrayUtil;
import edu.hm.cs.ig.passbutler.util.CryptoUtil;
import edu.hm.cs.ig.passbutler.encryption.KeyHolder;

public class UnlockActivity extends AppCompatActivity {

    private static final String TAG = UnlockActivity.class.getName();
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
    }

    public void unlockButtonOnClick(View view) {
        if(passwordEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.password_empty_error_msg), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Inserted password is empty.");
            return;
        }
        try {
            if (!isPasswordCorrect()) {
                Toast.makeText(this, getString(R.string.wrong_password_error_msg), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Wrong password was entered.");
                return;
            }
        }
        catch (JSONException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | NoSuchPaddingException
                | DestroyFailedException
                | IOException e) {
            Toast.makeText(this, getString(R.string.unlock_error_msg), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Could not check password for correctness.");
            return;
        }
        Log.i(TAG, "Correct password was entered.");
        Intent intent = new Intent(this, AccountListActivity.class);
        startActivity(intent);
        Log.i(TAG, "Proceeding to " + AccountListActivity.class.getSimpleName() + ".");
    }

    private boolean isPasswordCorrect() throws DestroyFailedException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, JSONException, FileNotFoundException, IOException {
        byte[] password = ArrayUtil.getContentAsByteArray(passwordEditText);
        byte[] nfcKey = {7, 7, 7};    // TODO: Insert value from nfc tag here.
        try {
            /*
             * CAUTION: The arrays from which the key is derived are cleared during key generation
             * and should not be used afterwards!
             */
            KeyHolder.getInstance().setKeyAndClearOld(this, CryptoUtil.generateKey(
                    getString(R.string.hash_func),
                    getString(R.string.encryption_alg),
                    password,
                    nfcKey));
            AccountListHandler.getFromFile(this, getString(R.string.accounts_file_name), KeyHolder.getInstance().getKey());
            return true;
        }
        catch (IOException e) {
            if(e.getCause() instanceof BadPaddingException) {
                Log.i(TAG, "I/O error was caused by bad padding.");
                return false;
            }
            throw e;
        }
    }
}
