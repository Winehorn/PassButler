package edu.hm.cs.ig.passbutler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        if(AccountHandler.accountFileExists(this))
        {
            Toast.makeText(this, getString(R.string.accounts_file_exists_error_msg), Toast.LENGTH_SHORT).show();
            Log.wtf(TAG, "An accounts file must not exist when creating a new one.");
            return;
        }
        else
        {
            AccountHandler accountHandler = new AccountHandler();
            accountHandler.saveToInternalStorage(this, getString(R.string.accounts_file_name));
            Log.i(TAG, "New accounts file created.");
            Intent intent = new Intent(this, AccountListActivity.class);
            intent.putExtra(getString(R.string.intent_key_account_handler), accountHandler);
            startActivity(intent);
            Log.i(TAG, "Proceeding " + AccountListActivity.class.getSimpleName() + ".");
        }
    }
}
