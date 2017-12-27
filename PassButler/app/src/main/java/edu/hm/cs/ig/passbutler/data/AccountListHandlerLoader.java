package edu.hm.cs.ig.passbutler.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.security.KeyHolder;
import edu.hm.cs.ig.passbutler.security.MissingKeyException;
import edu.hm.cs.ig.passbutler.util.NavigationUtil;

/**
 * Created by dennis on 17.11.17.
 */

public class AccountListHandlerLoader extends AsyncTaskLoader<AccountListHandler> {

    private static final String TAG = AccountListHandlerLoader.class.getName();
    private final String fileName;
    private AccountListHandler cachedData;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received broadcast message to load.");
            forceLoad();
        }
    };

    public AccountListHandlerLoader(Context context, String fileName) {
        super(context);
        this.fileName = fileName;
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter(getContext().getString(R.string.account_list_handler_loader_reload_action));
        Log.i(TAG, "Registering broadcast receiver.");
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStartLoading() {
        if(cachedData == null) {
            Log.i(TAG, "Starting loading.");
            forceLoad();
        }
        else {
            Log.i(TAG, "Delivering cached data.");
            super.deliverResult(cachedData);
        }
    }

    @Override
    public AccountListHandler loadInBackground() {
        try {
            return AccountListHandler.getFromInternalStorage(getContext(), fileName, KeyHolder.getInstance().getKey());
        }
        catch(MissingKeyException e) {
            Toast.makeText(getContext(), getContext().getString(R.string.missing_key_error_msg), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Could not retrieve key from " + KeyHolder.class.getSimpleName()
                    + " for decryption. Returning empty "
                    + AccountListHandler.class.getSimpleName()
                    + " instead.");
            NavigationUtil.goToUnlockActivity(getContext());
            return new AccountListHandler(getContext());
        }
        catch(JSONException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | NoSuchPaddingException
                | IOException e) {
            Toast.makeText(getContext(), getContext().getString(R.string.loader_error_msg), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Could not load " + AccountListHandler.class.getSimpleName()
                    + " from file. Returning empty "
                    + AccountListHandler.class.getSimpleName()
                    + " instead.");
            return new AccountListHandler(getContext());
        }
        catch(IllegalStateException e) {
            Log.e(TAG, "Could not get key from " + KeyHolder.class.getSimpleName()
                    + " to decrypt account list. Returning empty "
                    + AccountListHandler.class.getSimpleName()
                    + " instead.");
            return new AccountListHandler(getContext());
        }
    }

    @Override
    public void deliverResult(AccountListHandler data) {
        cachedData = data;
        Log.i(TAG, "Delivering result of loading.");
        super.deliverResult(data);
    }

    @Override
    protected void onReset() {
        super.onReset();
        Log.i(TAG, "Unregistering broadcast receiver.");
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }
}
