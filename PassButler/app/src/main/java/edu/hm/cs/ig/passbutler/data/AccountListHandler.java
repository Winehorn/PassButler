package edu.hm.cs.ig.passbutler.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import javax.crypto.NoSuchPaddingException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.util.CryptoUtil;

/**
 * Created by dennis on 15.11.17.
 */

public class AccountListHandler implements Parcelable {

    private static final String TAG = AccountListHandler.class.getName();
    private JSONObject accountListAsJson;

    public AccountListHandler(Context context)
    {
        Log.i(TAG, "Creating empty " + AccountListHandler.class.getSimpleName() + ".");
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
        Log.i(TAG, "Creating " + AccountListHandler.class.getSimpleName() + " from JSON string.");
        this.accountListAsJson = new JSONObject(accountListAsJson);
    }

    public static AccountListHandler getFromFile(Context context, String fileName, Key key) throws JSONException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, FileNotFoundException, IOException {
        Log.i(TAG, "Creating " + AccountListHandler.class.getSimpleName() + " from file content.");
        try {
            Log.i(TAG, "File found. Returning "
                    + AccountListHandler.class.getSimpleName()
                    + " with data from the file.");
            String jsonString = CryptoUtil.readFromInternalStorage(
                    context,
                    fileName,
                    key,
                    context.getString(R.string.encryption_alg));
            return new AccountListHandler(jsonString);
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not create " + AccountListHandler.class.getSimpleName() + " from string loaded from file.");
            throw e;
        }
        catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "The algorithm for decrypting the file must be valid.");
            throw e;
        }
        catch (InvalidKeyException e) {
            Log.e(TAG, "The key for decrypting the file must be valid.");
            throw e;
        }
        catch (NoSuchPaddingException e) {
            Log.e(TAG, "The padding for decrypting the file must be valid.");
            throw e;
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "Could not find file to create " + AccountListHandler.class.getSimpleName() + " from.");
            throw e;
        }
        catch (IOException e) {
            Log.e(TAG, "I/O error while creating " + AccountListHandler.class.getSimpleName() + " from file.");
            throw e;
        }
    }

    public AccountListHandler(Parcel parcel) throws JSONException
    {
        this.accountListAsJson = new JSONObject(parcel.readString());
    }

    public boolean saveToInternalStorage(Context context, String fileName, Key key)
    {
        return CryptoUtil.writeToInternalStorage(
                context,
                fileName,
                accountListAsJson.toString(),
                key,
                context.getString(R.string.encryption_alg));
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

    public boolean addAccount(Context context, RecyclerView.Adapter adapter, AccountItemHandler accountItemHandler, boolean persistChange, String fileName, Key key) {
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
            boolean changePersisted = saveToInternalStorage(context, fileName, key);
            if(!changePersisted) {
                Log.e(TAG, "Could not persist newly added account.");
            }
            return changePersisted;
        }
        return true;
    }

    public boolean removeAccount(Context context, RecyclerView.Adapter adapter, String accountName, boolean persistChange, String fileName, Key key) {
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
            boolean changePersisted = saveToInternalStorage(context, fileName, key);
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
