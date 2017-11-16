package edu.hm.cs.ig.passbutler;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by dennis on 15.11.17.
 */

public class AccountHandler implements Parcelable {

    private static final String TAG = AccountHandler.class.getName();
    private JSONObject accountListAsJson;

    public AccountHandler()
    {
        this.accountListAsJson = new JSONObject();






        // TODO: Just for testing purposes
        try {
            JSONObject amazon = new JSONObject();
            amazon.put("account_name", "Amazon");

            JSONObject google = new JSONObject();
            google.put("account_name", "Google");

            JSONObject paypal = new JSONObject();
            paypal.put("account_name", "Paypal");

            JSONObject battleNet = new JSONObject();
            battleNet.put("account_name", "BattleNet");

            JSONArray accounts = new JSONArray();
            accounts.put(amazon);
            accounts.put(google);
            accounts.put(paypal);
            accounts.put(battleNet);

            accountListAsJson.put("account_list", accounts);
        }
        catch(JSONException e) {
            throw new IllegalStateException("Inserting JSON test values for accounts failed.");
        }







        // TODO: Insert fixed element that serves to check if decryption key is right.
    }

    public AccountHandler(String accountListAsJson) throws JSONException
    {
        this.accountListAsJson = new JSONObject(accountListAsJson);
    }

    public AccountHandler(Parcel parcel) throws JSONException
    {
        this.accountListAsJson = new JSONObject(parcel.readString());
    }

    public static boolean accountFileExists(Context context) {
        String basePath = context.getFilesDir().getAbsolutePath();
        File file = new File(basePath, context.getString(R.string.accounts_file_name));
        return file.exists();
    }

    public boolean saveToInternalStorage(Context context, String fileName)
    {
        return FileUtil.saveStringToInternalStorageFile(context, fileName, accountListAsJson.toString());
    }

    public int getAccountCount(Context context) {
        try {
            return accountListAsJson.getJSONArray(context.getString(R.string.json_key_account_list)).length();
        }
        catch(JSONException e) {
            Log.e(TAG, "List of accounts could not be retrieved for determining its length.");
            return 0;
        }
    }

    public String getAccountName(Context context, int position) {
        try {
            JSONArray accounts = accountListAsJson.getJSONArray(context.getString(R.string.json_key_account_list));
            return accounts.getJSONObject(position).getString(context.getString(R.string.json_key_account_name));
        }
        catch(JSONException e) {
            Log.e(TAG, "Name of a nameless account requested.");
            return context.getString(R.string.default_account_name);
        }
    }

    public boolean addAccount(Context context, String accountName, boolean persistChange) {
        try {
            JSONArray accounts = accountListAsJson.getJSONArray(context.getString(R.string.json_key_account_list));
            JSONObject newAccount = new JSONObject();
            newAccount.put(context.getString(R.string.json_key_account_name), accountName);
            accounts.put(newAccount);
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not add new account to list.");
            return false;
        }
        if(persistChange) {
            boolean changePersisted = saveToInternalStorage(context, context.getString(R.string.accounts_file_name));
            if(!changePersisted) {
                Log.e(TAG, "Could not persist newly added account.");
            }
            return changePersisted;
        }
        return true;
    }

    public boolean removeAccount(Context context, String accountName, boolean persistChange) {
        try {
            JSONArray accounts = accountListAsJson.getJSONArray(context.getString(R.string.json_key_account_list));
            accounts.remove(getAccountIndex(context, accountName));
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not remove the specified account from list.");
            return false;
        }
        if(persistChange) {
            boolean changePersisted = saveToInternalStorage(context, context.getString(R.string.accounts_file_name));
            if(!changePersisted) {
                Log.e(TAG, "Could not persist removal of account.");
            }
            return changePersisted;
        }
        return true;
    }

    public JSONObject getAccount(Context context, String accountName) {
        try {
            JSONArray accounts = accountListAsJson.getJSONArray(context.getString(R.string.json_key_account_list));
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject tmpAccount = accounts.getJSONObject(i);
                String tmpAccountName = tmpAccount.getString(context.getString(R.string.json_key_account_name));
                if(accountName.equals(tmpAccountName)) {
                    return tmpAccount;
                }
            }
            return null;
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not retrieve requested account.");
            return null;
        }
    }

    public int getAccountIndex(Context context, String accountName) {
        try {
            JSONArray accounts = accountListAsJson.getJSONArray(context.getString(R.string.json_key_account_list));
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject tmpAccount = accounts.getJSONObject(i);
                String tmpAccountName = tmpAccount.getString(context.getString(R.string.json_key_account_name));
                if(accountName.equals(tmpAccountName)) {
                    return i;
                }
            }
            return -1;
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not retrieve requested account index.");
            return -1;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accountListAsJson.toString());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public AccountHandler createFromParcel(Parcel in) {
            try {
                return new AccountHandler(in);
            }
            catch(JSONException e) {
                Log.wtf(TAG, "Could not create JSONObject from string.");
                return null;
            }
        }

        public AccountHandler[] newArray(int size) {
            return new AccountHandler[size];
        }
    };
}
