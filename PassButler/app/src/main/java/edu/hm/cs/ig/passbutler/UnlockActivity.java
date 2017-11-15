package edu.hm.cs.ig.passbutler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
        if(AccountList.accountFileExists(this))
        {
            // TODO: Check if password is right
            AccountList accountList;
            try {
                String jsonString = FileUtil.loadStringFromInternalStorageFile(
                        this,
                        getString(R.string.accounts_file_name));
                accountList = new AccountList(jsonString);
            }
            catch(JSONException e) {
                Toast.makeText(this, getString(R.string.json_error_msg), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Could not create JSON object from string.");
                return;
            }
            Log.i(TAG, "Accounts file loaded from internal storage.");
            Intent intent = new Intent(this, AccountListActivity.class);
            intent.putExtra(getString(R.string.account_list_key), accountList);
            startActivity(intent);
            Log.i(TAG, "Proceeding to " + AccountListActivity.class.getSimpleName() + ".");
        }
        else
        {
            Toast.makeText(this, getString(R.string.accounts_file_does_not_exist_error_msg), Toast.LENGTH_SHORT).show();
            Log.wtf(TAG, "An accounts file must exist when unlocking it.");
            return;
        }
    }
}
