package edu.hm.cs.ig.passbutler.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.crypto.NoSuchPaddingException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.security.MissingKeyException;
import edu.hm.cs.ig.passbutler.util.CryptoUtil;
import edu.hm.cs.ig.passbutler.util.FileUtil;
import edu.hm.cs.ig.passbutler.util.SyncContentProviderUtil;

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
        }
        catch(JSONException e) {
            Log.wtf(TAG, "Could not create new " + AccountListHandler.class.getSimpleName() + ".");
            throw new IllegalStateException("Inserting JSON test values for accounts failed.");
        }
    }

    public AccountListHandler(String accountListAsJson) throws JSONException
    {
        Log.i(TAG, "Creating " + AccountListHandler.class.getSimpleName() + " from JSON string.");
        this.accountListAsJson = new JSONObject(accountListAsJson);
    }

    public static AccountListHandler getFromInternalStorage(Context context, String internalStorageFilePath, Key key) throws JSONException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, FileNotFoundException, IOException {
        return getFromFile(context, FileUtil.getInternalStorageFile(context, internalStorageFilePath), key);
    }

    public static AccountListHandler getFromFile(Context context, File file, Key key) throws JSONException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, FileNotFoundException, IOException {
        Log.i(TAG, "Creating " + AccountListHandler.class.getSimpleName() + " from file content.");
        try {
            Log.i(TAG, "File found. Returning "
                    + AccountListHandler.class.getSimpleName()
                    + " with data from the file.");
            String jsonString = CryptoUtil.readFromFile(
                    file,
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

    public synchronized boolean saveToInternalStorage(Context context, String filePath, Date lastModified, Key key, boolean persistMetaData)
    {
        byte[] encryptedData = CryptoUtil.encryptToBytes(
                accountListAsJson.toString(),
                key,
                context.getString(R.string.encryption_alg));
        String fileHash = CryptoUtil.digestToString(context.getString(R.string.hash_func_for_digest), encryptedData);
        return FileUtil.writeToInternalStorage(
                context,
                new FileMetaData(filePath, lastModified, fileHash),
                encryptedData,
                persistMetaData);
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

    public boolean addAccount(
            Context context,
            RecyclerView.Adapter adapter,
            AccountItemHandler accountItemHandler,
            boolean persistChange,
            String filePath,
            Date modificationDate,
            Key key,
            boolean persistMetaData) {
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
            boolean changePersisted = saveToInternalStorage(context, filePath, modificationDate, key, persistMetaData);
            if(!changePersisted) {
                Log.e(TAG, "Could not persist newly added account.");
            }
            return changePersisted;
        }
        return true;
    }

    public boolean removeAccount(
            Context context,
            RecyclerView.Adapter adapter,
            String accountName,
            boolean persistChange,
            String filePath,
            Date modificationDate,
            Key key,
            boolean persistMetaData) {
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
            boolean changePersisted = saveToInternalStorage(context, filePath, modificationDate, key, persistMetaData);
            if(!changePersisted) {
                Log.e(TAG, "Could not persist removal of account.");
            }
            return changePersisted;
        }
        return true;
    }

    public List<AccountItemHandler> getAccounts(Context context) {
        try {
            JSONObject accounts = accountListAsJson.getJSONObject(context.getString(R.string.json_key_account_list));
            List<AccountItemHandler> accountItemHandlers = new ArrayList<>();
            Iterator<String> iterator = accounts.keys();
            String accountName;
            while(iterator.hasNext()) {
                accountName = iterator.next();
                accountItemHandlers.add(getAccount(context, accountName));
            }
            return accountItemHandlers;
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not retrieve list of accounts.");
            return null;
        }
    }

    public AccountItemHandler getAccount(Context context, String accountName) {
        try {
            JSONObject accounts = accountListAsJson.getJSONObject(context.getString(R.string.json_key_account_list));
            return new AccountItemHandler(context, accountName, accounts.getJSONObject(accountName));
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not retrieve requested account.");
            return null;
        }
    }

    public boolean merge(
            Context context,
            String accountsFilePath,
            AccountListHandler accountsToMerge,
            Date mergeDataVersion,
            Date mergeDate) throws MissingKeyException {
        Log.i(TAG, "Starting to merge account list with existing data.");
        final List<AccountItemHandler> localAccounts = getAccounts(context);
        final Set<String> processedCommonAccounts = new HashSet<>();
        boolean somethingChanged = false;

        // Process accounts that have been deleted or have changed.
        for(final AccountItemHandler localAccount : localAccounts) {
            final AccountItemHandler accountToMerge = accountsToMerge.getAccount(context, localAccount.getAccountName(context));
            // Deleted
            if(accountToMerge == null) {
                if(mergeDataVersion.after(localAccount.getLastModified(context))) {
                    Log.i(TAG, "Deleting account with name " + localAccount.getAccountName(context) + ".");
                    removeAccount(
                            context,
                            null,
                            localAccount.getAccountName(context),
                            false,
                            null,
                            null,
                            null,
                            false);
                    somethingChanged = true;
                }
            }
            // Changed
            else {
                if (accountToMerge.getLastModified(context).after(localAccount.getLastModified(context))) {
                    Log.i(TAG, "The account with name " + localAccount.getAccountName(context) + " needs a merge for itself.");
                    somethingChanged |= localAccount.merge(context, accountToMerge, mergeDate);
                }
                processedCommonAccounts.add(localAccount.getAccountName(context));
            }
        }

        // Process accounts that are new.
        for(final AccountItemHandler accountToMerge : accountsToMerge.getAccounts(context)) {
            if(!processedCommonAccounts.contains(accountToMerge.getAccountName(context))) {
                if(accountToMerge.getLastModified(context).after(SyncContentProviderUtil.getLastModified(context, accountsFilePath))) {
                    Log.i(TAG, "Adding new account with name " + accountToMerge.getAccountName(context) + ".");
                    addAccount(
                            context,
                            null,
                            accountToMerge,
                            false,
                            null,
                            null,
                            null,
                            false);
                    somethingChanged = true;
                }
            }
        }

        Log.i(TAG, "Merge of account list completed. Changes have been made: " + somethingChanged + ".");
        return somethingChanged;
    }

    @Override
    public String toString() {
        return this.accountListAsJson.toString();
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
