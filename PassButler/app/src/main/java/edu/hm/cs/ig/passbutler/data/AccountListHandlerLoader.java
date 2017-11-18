package edu.hm.cs.ig.passbutler.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;

import edu.hm.cs.ig.passbutler.R;

/**
 * Created by dennis on 17.11.17.
 */

public class AccountListHandlerLoader extends AsyncTaskLoader<AccountListHandler> {

    private static final String TAG = AccountListHandlerLoader.class.getName();
    private AccountListHandler cachedData;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received broadcast message to load.");
            forceLoad();
        }
    };

    public AccountListHandlerLoader(Context context) {
        super(context);
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
            if(AccountListHandler.accountFileExists(getContext())) {
                Log.i(TAG, "Account file found. Returning "
                        + AccountListHandler.class.getSimpleName()
                        + " with data from the file.");
                String jsonString = FileUtil.loadStringFromInternalStorageFile(
                        getContext(),
                        getContext().getString(R.string.accounts_file_name));
                return new AccountListHandler(jsonString);
            }
            Log.i(TAG, "No account file found. Returning empty "
                    + AccountListHandler.class.getSimpleName()
                    + ".");
            return new AccountListHandler(getContext());
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not create JSON object from string loaded from file "
                            + getContext().getString(R.string.accounts_file_name)
                            + ". Returning empty "
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
