package edu.hm.cs.ig.passbutler.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

import edu.hm.cs.ig.passbutler.R;

/**
 * Created by dennis on 15.11.17.
 */

public class AccountListHandler implements Parcelable {

    private static final String TAG = AccountListHandler.class.getName();
    private JSONObject accountListAsJson;

    public AccountListHandler(Context context)
    {
        try {
            this.accountListAsJson = new JSONObject();
            JSONObject accounts = new JSONObject();
            this.accountListAsJson.put(context.getString(R.string.json_key_account_list), accounts);






            // TODO: Just for testing purposes
            JSONObject amazon = new JSONObject();
            amazon.put(context.getString(R.string.json_key_account_attribute_list), new JSONObject());
            accounts.put("AMAZON", amazon);

            JSONObject google = new JSONObject();
            google.put(context.getString(R.string.json_key_account_attribute_list), new JSONObject());
            accounts.put("GOOGLE", google);

            JSONObject paypal = new JSONObject();
            paypal.put(context.getString(R.string.json_key_account_attribute_list), new JSONObject());
            accounts.put("PayPal", paypal);

            JSONObject battleNet = new JSONObject();
            battleNet.put(context.getString(R.string.json_key_account_attribute_list), new JSONObject());
            accounts.put("BattleNet", battleNet);







            accountListAsJson.put(context.getString(R.string.json_key_account_list), accounts);
        }
        catch(JSONException e) {
            // TODO
            throw new IllegalStateException("Inserting JSON test values for accounts failed.");
        }







        // TODO: Insert fixed element that serves to check if decryption key is right.
    }

    public AccountListHandler(String accountListAsJson) throws JSONException
    {
        this.accountListAsJson = new JSONObject(accountListAsJson);
    }

    public AccountListHandler(Parcel parcel) throws JSONException
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

    public boolean accountExists(Context context, String accountName) {
        try {
            JSONObject accounts = accountListAsJson.getJSONObject(context.getString(R.string.json_key_account_list));
            return accounts.has(accountName);
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not retrieve list of accounts in JSON file.");
            return false;
        }
    }

    public int getAccountCount(Context context) {
        try {
            return accountListAsJson.getJSONObject(context.getString(R.string.json_key_account_list)).length();
        }
        catch(JSONException e) {
            Log.e(TAG, "List of accounts could not be retrieved for determining its length.");
            return 0;
        }
    }

    public String getAccountName(Context context, int index) {
        try {
            JSONObject accounts = accountListAsJson.getJSONObject(context.getString(R.string.json_key_account_list));
            Iterator<String> keyIterator = accounts.keys();
            int i = 0;
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                if(i == index) {
                    return key;
                }
                i++;
            }
            Log.i(TAG, "Could not retrieve name of account with specified index");
            return context.getString(R.string.default_account_name);
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not retrieve list of accounts in JSON file.");
            return context.getString(R.string.default_account_name);
        }
    }

    public boolean addAccount(Context context, RecyclerView.Adapter adapter, AccountItemHandler accountItemHandler, boolean persistChange) {
        try {
            JSONObject accounts = accountListAsJson.getJSONObject(context.getString(R.string.json_key_account_list));
            accounts.put(
                    accountItemHandler.getAccountName(context),
                    accountItemHandler.getAccountItemAsJson());
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
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

    public boolean removeAccount(Context context, RecyclerView.Adapter adapter, String accountName, boolean persistChange) {
        try {
            JSONObject accounts = accountListAsJson.getJSONObject(context.getString(R.string.json_key_account_list));
            accounts.remove(accountName);
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
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

    public AccountItemHandler getAccount(Context context, String accountName) {
        try {
            JSONObject accounts = accountListAsJson.getJSONObject(context.getString(R.string.json_key_account_list));
            return new AccountItemHandler(accountName, accounts.getJSONObject(accountName));
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not retrieve requested account.");
            return null;
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
        public AccountListHandler createFromParcel(Parcel in) {
            try {
                return new AccountListHandler(in);
            }
            catch(JSONException e) {
                Log.wtf(TAG, "Could not create JSONObject from string.");
                return null;
            }
        }

        public AccountListHandler[] newArray(int size) {
            return new AccountListHandler[size];
        }
    };
}
