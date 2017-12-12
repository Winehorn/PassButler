package edu.hm.cs.ig.passbutler.entry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

import javax.security.auth.DestroyFailedException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.account_list.AccountListActivity;
import edu.hm.cs.ig.passbutler.data.AccountListHandler;
import edu.hm.cs.ig.passbutler.util.ArrayUtil;
import edu.hm.cs.ig.passbutler.util.FileUtil;
import edu.hm.cs.ig.passbutler.util.CryptoUtil;
import edu.hm.cs.ig.passbutler.encryption.KeyHolder;

public class CreatePersistenceActivity extends AppCompatActivity {

    private static final String TAG = CreatePersistenceActivity.class.getName();
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_persistence);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        repeatPasswordEditText = (EditText) findViewById(R.id.repeat_password_edit_text);
    }

    public void createButtonOnClick(View view) {
        if(!passwordEditText.getText().toString().equals(repeatPasswordEditText.getText().toString())) {
            Toast.makeText(this, getString(R.string.password_match_error_msg), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Inserted passwords do not match.");
            return;
        }
        if(FileUtil.internalStorageFileExists(this, getString(R.string.accounts_file_path)))
        {
            Toast.makeText(this, getString(R.string.accounts_file_exists_error_msg), Toast.LENGTH_SHORT).show();
            Log.wtf(TAG, "An accounts file must not exist when creating a new one.");
            return;
        }
        else
        {
            createPersistence();
            Intent intent = new Intent(this, AccountListActivity.class);
            startActivity(intent);
            Log.i(TAG, "Proceeding " + AccountListActivity.class.getSimpleName() + ".");
        }
    }

    private void createPersistence() {
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
        } catch (DestroyFailedException e) {
            Toast.makeText(this, getString(R.string.destroy_failed_error_msg), Toast.LENGTH_SHORT).show();
            Log.wtf(TAG, "Could not set new key in " + KeyHolder.class.getSimpleName() + ".");
            return;
        }
        AccountListHandler accountListHandler = new AccountListHandler(this);
        accountListHandler.saveToInternalStorage(
                this,
                getString(R.string.accounts_file_path),
                new Date(0L),   // Initialize with oldest date so that sync is possible with every other newer version.
                KeyHolder.getInstance().getKey(),
                false);
        Log.i(TAG, "New accounts file created.");
    }
}
