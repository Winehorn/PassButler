package edu.hm.cs.ig.passbutler;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import edu.hm.cs.ig.passbutler.data.AccountListHandler;
import edu.hm.cs.ig.passbutler.data.AccountListHandlerLoader;
import edu.hm.cs.ig.passbutler.data.BroadcastFileObserver;
import edu.hm.cs.ig.passbutler.data.FileUtil;
import edu.hm.cs.ig.passbutler.encryption.KeyHolder;

public class AccountListActivity extends AppCompatActivity implements AccountListAdapterOnClickHandler, AccountListAdapterOnMenuItemClickHandler, LoaderManager.LoaderCallbacks<AccountListHandler> {

    private static final String TAG = AccountListActivity.class.getName();
    private RecyclerView recyclerView;
    private AccountListAdapter accountListAdapter;
    private AccountListHandler accountListHandler;
    private BroadcastFileObserver broadcastFileObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Build up recycler view.
        recyclerView = findViewById(R.id.account_list_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        accountListAdapter = new AccountListAdapter(this, this, this);
        recyclerView.setAdapter(accountListAdapter);

        // Set data source of the adapter.
        accountListAdapter.setAccountListHandler(accountListHandler);

        // Set up loader.
        getSupportLoaderManager().initLoader(
                getResources().getInteger(R.integer.account_list_handler_loader_id),
                null,
                this);
        broadcastFileObserver = new BroadcastFileObserver(
                getApplicationContext(),
                getString(R.string.account_list_handler_loader_reload_action),
                FileUtil.combinePaths(getFilesDir().getAbsolutePath(), getString(R.string.accounts_file_name)),
                FileObserver.MODIFY);
        broadcastFileObserver.startWatching();

        // TODO: Display loading indicator until loader has finished.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.account_list_action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home || id == R.id.lock_menu_item) {
            Intent intent = new Intent(this, UnlockActivity.class);
            startActivity(intent);
            // TODO: Remove saved decryption key (aka lock PassButler again). Better with an own Button?
            return true;
        }
        else if(id == R.id.password_generator_menu_item) {
            Intent intent = new Intent(this, PasswordGeneratorActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.sync_menu_item) {
            Intent intent = new Intent(this, SyncActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.settings_menu_item) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addAccountFabOnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_account_name));

        // Set up the input for the dialog
        final EditText input = new EditText(this);

        // Specify the type of input expected.
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the button options for the dialog.
        builder.setPositiveButton(getString(R.string.dialog_option_create), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String accountName = input.getText().toString();
                if(accountName == null || accountName.isEmpty()) {
                    dialog.cancel();
                    Toast.makeText(
                            AccountListActivity.this,
                            AccountListActivity.this.getString(R.string.dialog_empty_name_error_msg),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    if(accountListHandler.accountExists(getApplicationContext(), accountName)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AccountListActivity.this);
                        builder.setTitle(getString(R.string.dialog_title_overwrite_account));

                        // Set up the button options for the dialog.
                        builder.setPositiveButton(getString(R.string.dialog_option_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String accountName = input.getText().toString();
                                dialog.dismiss();
                                Intent intent = new Intent(AccountListActivity.this, AccountDetailActivity.class);
                                intent.putExtra(AccountListActivity.this.getString(R.string.intent_extras_key_account_name), accountName);
                                intent.putExtra(getString(R.string.intent_extras_key_create_new_account_item), true);
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton(getString(R.string.dialog_option_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        // Show the dialog.
                        builder.show();
                    }
                    else {
                        dialog.dismiss();
                        Intent intent = new Intent(AccountListActivity.this, AccountDetailActivity.class);
                        intent.putExtra(AccountListActivity.this.getString(R.string.intent_extras_key_account_name), accountName);
                        intent.putExtra(getString(R.string.intent_extras_key_create_new_account_item), true);
                        startActivity(intent);
                    }
                }
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_option_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog.
        builder.show();
    }

    @Override
    public void onClick(View v, String accountName) {
        Intent intent = new Intent(this, AccountDetailActivity.class);
        intent.putExtra(AccountListActivity.this.getString(R.string.intent_extras_key_account_name), accountName);
        intent.putExtra(getString(R.string.intent_extras_key_create_new_account_item), false);
        startActivity(intent);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item, final String accountName) {
        switch(item.getItemId()) {
            case R.id.delete_account_menu_item: {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountListActivity.this);
                builder.setTitle(getString(R.string.dialog_title_delete_account));
                builder.setPositiveButton(getString(R.string.dialog_option_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        accountListHandler.removeAccount(
                                getApplicationContext(),
                                accountListAdapter,
                                accountName,
                                true,
                                getString(R.string.accounts_file_name),
                                KeyHolder.getInstance().getKey());
                    }
                });
                builder.setNegativeButton(getString(R.string.dialog_option_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return true;
            }
            default: {
                return false;
            }
        }
    }

    @Override
    public Loader<AccountListHandler> onCreateLoader(int id, Bundle args) {
        if(id == getResources().getInteger(R.integer.account_list_handler_loader_id)) {
            Log.i(TAG, "Creating new " + AccountListHandlerLoader.class.getSimpleName() + ".");
            return new AccountListHandlerLoader(this, getString(R.string.accounts_file_name));
        }
        else {
            throw new IllegalArgumentException("The loader ID must be valid.");
        }
    }

    @Override
    public void onLoadFinished(Loader<AccountListHandler> loader, AccountListHandler data) {
        Log.i(TAG, "Processing data of finished loader.");
        accountListAdapter.setAccountListHandler(data);
        accountListHandler = data;
    }

    @Override
    public void onLoaderReset(Loader<AccountListHandler> loader) {
        Log.i(TAG, "Processing loader reset.");
    }
}
