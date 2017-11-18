package edu.hm.cs.ig.passbutler.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import edu.hm.cs.ig.passbutler.R;

/**
 * Created by dennis on 16.11.17.
 */

public class AccountItemHandler {

    private static final String TAG = AccountItemHandler.class.getName();
    private JSONObject accountItemAsJson;
    private final String accountName;

    public AccountItemHandler(Context context, String accountName) throws JSONException
    {
        this.accountName = accountName;
        this.accountItemAsJson = new JSONObject();
        this.accountItemAsJson.put(context.getString(R.string.json_key_account_attribute_list), new JSONObject());
    }

    public AccountItemHandler(String accountName, JSONObject accountItemAsJson) {
        this.accountName = accountName;
        this.accountItemAsJson = accountItemAsJson;
    }

    public String getAccountName(Context context) {
        return this.accountName;
    }

    public JSONObject getAccountItemAsJson() {
        return accountItemAsJson;
    }

    public boolean attributeExists(Context context, String attributeKey) {
        try {
            JSONObject attributes = accountItemAsJson.getJSONObject(context.getString(R.string.json_key_account_attribute_list));
            return attributes.has(attributeKey);
        }
        catch(JSONException e) {
            Log.e(TAG, "List of account attribute keys could not be retrieved.");
            return false;
        }
    }

    public boolean addAttribute(Context context, RecyclerView.Adapter adapter, String attributeKey, String attributeValue) {
        try {
            JSONObject attributes = accountItemAsJson.getJSONObject(context.getString(R.string.json_key_account_attribute_list));
            JSONObject newAttribute = new JSONObject();
            newAttribute.put(context.getString(R.string.json_key_account_attribute_value), attributeValue);
            // TODO: Add "lastEdited"-date
            attributes.put(attributeKey, newAttribute);
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not add new attribute to list.");
            return false;
        }
        return true;
    }

    public boolean removeAttribute(Context context, RecyclerView.Adapter adapter, String attributeKey) {
        try {
            JSONObject attributes = accountItemAsJson.getJSONObject(context.getString(R.string.json_key_account_attribute_list));
            attributes.remove(attributeKey);
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not remove the specified attribute from list.");
            return false;
        }
        return true;
    }

    public Iterator<String> getAttributeKeys(Context context) {
        try {
            return accountItemAsJson.getJSONObject(context.getString(R.string.json_key_account_attribute_list)).keys();
        }
        catch(JSONException e) {
            Log.e(TAG, "List of account attribute keys could not be retrieved.");
            return null;
        }
    }

    public String getAttributeKey(Context context, int index) {
        Iterator<String> keyIterator = getAttributeKeys(context);
        int i = 0;
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            if(i == index) {
                return key;
            }
            i++;
        }
        return null;
    }

    public String getAttributeValue(Context context, String attributeKey) {
        try {
            JSONObject attributes = accountItemAsJson.getJSONObject(context.getString(R.string.json_key_account_attribute_list));
            JSONObject attribute = attributes.getJSONObject(attributeKey);
            return attribute.getString(context.getString(R.string.json_key_account_attribute_value));
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not retrieve attribute value.");
            return null;
        }
    }

    public int getAttributeCount(Context context) {
        try {
            return accountItemAsJson.getJSONObject(context.getString(R.string.json_key_account_attribute_list)).length();
        }
        catch(JSONException e) {
            Log.e(TAG, "List of account attributes could not be retrieved for determining its length.");
            return 0;
        }
    }
}
