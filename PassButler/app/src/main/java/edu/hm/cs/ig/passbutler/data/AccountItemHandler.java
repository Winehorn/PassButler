package edu.hm.cs.ig.passbutler.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.security.KeyHolder;
import edu.hm.cs.ig.passbutler.security.MissingKeyException;
import edu.hm.cs.ig.passbutler.util.CryptoUtil;
import edu.hm.cs.ig.passbutler.util.NavigationUtil;
import edu.hm.cs.ig.passbutler.util.StringUtil;

/**
 * Created by dennis on 16.11.17.
 */

public class AccountItemHandler {

    private static final String TAG = AccountItemHandler.class.getName();
    private JSONObject accountItemAsJson;
    private final String accountName;
    private Date lastModified;

    public AccountItemHandler(Context context, String accountName, Date lastModified) throws JSONException {
        this.accountName = accountName;
        this.lastModified = lastModified;
        this.accountItemAsJson = new JSONObject();
        this.accountItemAsJson.put(context.getString(R.string.json_key_account_attribute_list), new JSONObject());
        this.accountItemAsJson.put(context.getString(R.string.json_key_account_last_modified), lastModified.getTime());
    }

    public AccountItemHandler(Context context, String accountName, JSONObject accountItemAsJson) {
        this.accountName = accountName;
        this.accountItemAsJson = accountItemAsJson;
        this.lastModified = getLastModified(context);
    }

    public String getAccountName(Context context) {
        return this.accountName;
    }

    public JSONObject getAccountItemAsJson() {
        return accountItemAsJson;
    }

    public Date getLastModified(Context context) {
        if(this.lastModified == null) {
            try {
                this.lastModified = new Date(accountItemAsJson.getLong(context.getString(R.string.json_key_account_last_modified)));
            }
            catch(JSONException e) {
                Log.e(TAG, "Could not retrieve date of last modification.");
                return null;
            }
        }
        return this.lastModified;
    }

    public void setLastModified(Context context, Date newLastModified) throws JSONException {
        try {
            this.accountItemAsJson.put(context.getString(R.string.json_key_account_last_modified), newLastModified.getTime());
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not set date of last modification.");
            throw e;
        }
        this.lastModified = newLastModified;
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

    public boolean addAttribute(
            Context context,
            RecyclerView.Adapter adapter,
            String attributeKey,
            String attributeValue,
            Date lastModified,
            boolean overrideAccountLastModified) {
        try {
            JSONObject attributes = accountItemAsJson.getJSONObject(context.getString(R.string.json_key_account_attribute_list));
            JSONObject newAttribute = new JSONObject();
            Key key = KeyHolder.getInstance().getKey();
            String salt = StringUtil.randomString(
                    true,
                    true,
                    true,
                    true,
                    context.getResources().getInteger(R.integer.salt_length),
                    context);
            String encryptedAttributeValue = CryptoUtil.encryptToString(attributeValue + salt, key, context.getString(R.string.encryption_alg));
            String encryptedLastModified = CryptoUtil.encryptToString(lastModified.getTime() + salt, key, context.getString(R.string.encryption_alg));
            newAttribute.put(context.getString(R.string.json_key_account_attribute_value), encryptedAttributeValue);
            newAttribute.put(context.getString(R.string.json_key_account_attribute_last_modified), encryptedLastModified);
            newAttribute.put(context.getString(R.string.json_key_account_attribute_salt), salt);
            attributes.put(attributeKey, newAttribute);
            if(overrideAccountLastModified) {
                setLastModified(context, lastModified);
            }
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not add new attribute to list.");
            return false;
        } catch (MissingKeyException e) {
            Toast.makeText(context, context.getString(R.string.missing_key_error_msg), Toast.LENGTH_SHORT).show();
            NavigationUtil.goToUnlockActivity(context);
            return false;
        }
        return true;
    }

    public boolean removeAttribute(
            Context context,
            RecyclerView.Adapter adapter,
            String attributeKey,
            Date lastModified,
            boolean overrideAccountLastModified) {
        try {
            JSONObject attributes = accountItemAsJson.getJSONObject(context.getString(R.string.json_key_account_attribute_list));
            attributes.remove(attributeKey);
            if(overrideAccountLastModified) {
                setLastModified(context, lastModified);
            }
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

    public boolean changeAttributeValue(
            Context context,
            RecyclerView.Adapter adapter,
            String attributeKey,
            String newAttributeValue,
            Date lastModified,
            boolean overrideAccountLastModified) {
        return addAttribute(context, adapter, attributeKey, newAttributeValue, lastModified, overrideAccountLastModified);
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

    public String getAttributeValue(Context context, String attributeKey) throws MissingKeyException {
        return getDecryptedAttributeStringProperty(
                context,
                attributeKey,
                context.getString(R.string.json_key_account_attribute_value));
    }

    public Date getAttributeLastModified(Context context, String attributeKey) throws MissingKeyException {
        return getDecryptedAttributeDateProperty(
                context,
                attributeKey,
                context.getString(R.string.json_key_account_attribute_last_modified));
    }

    public String getAttributeSalt(Context context, String attributeKey) {
        return getEncryptedAttributeProperty(
                context,
                attributeKey,
                context.getString(R.string.json_key_account_attribute_salt));
    }

    private String getDecryptedAttributeStringProperty(Context context, String attributeKey, String attributePropertyKey) throws MissingKeyException {
        String encryptedStringProperty = getEncryptedAttributeProperty(
                context,
                attributeKey,
                attributePropertyKey);
        if(encryptedStringProperty != null) {
            String decryptedStringProperty = CryptoUtil.decryptToString(
                    encryptedStringProperty,
                    KeyHolder.getInstance().getKey(),
                    context.getString(R.string.encryption_alg));
            String salt = getAttributeSalt(context, attributeKey);
            return decryptedStringProperty.substring(0, decryptedStringProperty.length() - salt.length());
        }
        return null;
    }

    private Date getDecryptedAttributeDateProperty(Context context, String attributeKey, String attributePropertyKey) throws MissingKeyException {
        String encryptedDateProperty = getEncryptedAttributeProperty(
                context,
                attributeKey,
                attributePropertyKey);
        if(encryptedDateProperty != null) {
            String decryptedDateProperty = CryptoUtil.decryptToString(
                    encryptedDateProperty,
                    KeyHolder.getInstance().getKey(),
                    context.getString(R.string.encryption_alg));
            String salt = getAttributeSalt(context, attributeKey);
            return new Date(Long.parseLong(decryptedDateProperty.substring(0, decryptedDateProperty.length() - salt.length())));
        }
        return null;
    }

    private String getEncryptedAttributeProperty(Context context, String attributeKey, String attributePropertyKey) {
        try {
            JSONObject attributes = accountItemAsJson.getJSONObject(context.getString(R.string.json_key_account_attribute_list));
            JSONObject attribute = attributes.getJSONObject(attributeKey);
            return attribute.getString(attributePropertyKey);
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not retrieve attribute property.");
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

    public boolean merge(Context context, AccountItemHandler accountToMerge, Date mergeDate) throws MissingKeyException, JSONException {
        Log.i(TAG, "Starting to merge account with name " + accountToMerge.getAccountName(context) + " with existing data.");
        final Iterator<String> localAttributesIterator = getAttributeKeys(context);
        final Iterator<String> mergeAttributesIterator = accountToMerge.getAttributeKeys(context);
        final Set<String> processedCommonAttributes = new HashSet<>();
        boolean somethingChanged = false;

        // Process attributes that have been deleted or have changed.
        while(localAttributesIterator.hasNext()) {
            final String attributeKey = localAttributesIterator.next();
            final Date localLastModified = getAttributeLastModified(context, attributeKey);
            final String mergeValue = accountToMerge.getAttributeValue(context, attributeKey);
            final Date mergeLastModified = accountToMerge.getAttributeLastModified(context, attributeKey);
            // Deleted
            if(mergeValue == null || mergeLastModified == null) {
                if(accountToMerge.getLastModified(context).after(localLastModified)) {
                    Log.i(TAG, "Deleting attribute " + attributeKey + ".");
                    localAttributesIterator.remove();
                    removeAttribute(context, null, attributeKey, mergeDate, false);
                    somethingChanged = true;
                }
            }
            // Changed
            else {
                if (mergeLastModified.after(localLastModified)) {
                    Log.i(TAG, "Changing value of attribute " + attributeKey + " to " + mergeValue + ".");
                    addAttribute(context, null, attributeKey, mergeValue, mergeDate, false);
                    somethingChanged = true;
                }
                processedCommonAttributes.add(attributeKey);
            }
        }

        // Process attributes that are new.
        while(mergeAttributesIterator.hasNext()) {
            final String attributeKey = mergeAttributesIterator.next();
            if(!processedCommonAttributes.contains(attributeKey)) {
                if(accountToMerge.getAttributeLastModified(context, attributeKey).after(this.lastModified)) {
                    String value = accountToMerge.getAttributeValue(context, attributeKey);
                    Log.i(TAG, "Adding new attribute " + attributeKey + " with value " + value + ".");
                    addAttribute(context, null, attributeKey, value, mergeDate, false);
                    somethingChanged = true;
                }
            }
        }

        /*
         * Set date of last modification at the end of merging because otherwise it would
         * interfere with the process of determining which attributes should get deleted, changed
         * or newly created.
         */
        if(somethingChanged) {
            setLastModified(context, mergeDate);
        }
        Log.i(TAG, "Merge of account completed. Changes have been made: " + somethingChanged + ".");
        return somethingChanged;
    }
}
