package edu.hm.cs.ig.passbutler;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by dennis on 15.11.17.
 */

public class AccountList implements Parcelable {

    private static final String TAG = AccountList.class.getName();
    private JSONObject accountListAsJson;

    public AccountList()
    {
        this.accountListAsJson = new JSONObject();
        // TODO: Insert fixed element that serves to check if decryption key is right.
    }

    public AccountList(String accountListAsJson) throws JSONException
    {
        this.accountListAsJson = new JSONObject(accountListAsJson);
    }

    public AccountList(Parcel parcel) throws JSONException
    {
        this.accountListAsJson = new JSONObject(parcel.readString());
    }

    public static boolean accountFileExists(Context context) {
        String basePath = context.getFilesDir().getAbsolutePath();
        File file = new File(basePath, context.getString(R.string.accounts_file_name));
        return file.exists();
    }

    public boolean saveToInternalSorage(Context context, String fileName)
    {
        return FileUtil.saveStringToInternalStorageFile(context, fileName, accountListAsJson.toString());
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
        public AccountList createFromParcel(Parcel in) {
            try {
                return new AccountList(in);
            }
            catch(JSONException e) {
                Log.wtf(TAG, "Could not create JSONObject from string.");
                return null;
            }
        }

        public AccountList[] newArray(int size) {
            return new AccountList[size];
        }
    };
}
