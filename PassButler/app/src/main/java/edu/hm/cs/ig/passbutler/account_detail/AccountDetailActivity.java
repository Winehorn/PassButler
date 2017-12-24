package edu.hm.cs.ig.passbutler.account_detail;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.Date;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.data.AccountItemHandler;
import edu.hm.cs.ig.passbutler.data.AccountListHandler;
import edu.hm.cs.ig.passbutler.data.AccountListHandlerLoader;
import edu.hm.cs.ig.passbutler.data.BroadcastFileObserver;
import edu.hm.cs.ig.passbutler.gui.InstantAutoCompleteTextView;
import edu.hm.cs.ig.passbutler.gui.PostAuthActivity;
import edu.hm.cs.ig.passbutler.security.KeyHolder;
import edu.hm.cs.ig.passbutler.util.ClipboardUtil;
import edu.hm.cs.ig.passbutler.util.FileUtil;

public class AccountDetailActivity extends PostAuthActivity implements AccountDetailAdapterOnMenuItemClickHandler, LoaderManager.LoaderCallbacks<AccountListHandler> {

    private static final String TAG = AccountDetailActivity.class.getName();
    private AccountListHandler accountListHandler;
    private String accountName;
    private boolean createNewAccountItem;
    private AccountItemHandler accountItemHandler;
    private RecyclerView recyclerView;
    private TextView emptyAttributeListCommentTextView;
    private AccountDetailAdapter accountDetailAdapter;
    private BroadcastFileObserver broadcastFileObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        final Intent intent = getIntent();
        if(!intent.hasExtra(getString(R.string.bundle_key_account_name))
                || !intent.hasExtra(getString(R.string.bundle_key_create_new_account_item))) {
            throw new IllegalStateException("The intent must contain the required extras.");
        }
        accountName = intent.getStringExtra(getString(R.string.bundle_key_account_name));
        createNewAccountItem = intent.getBooleanExtra(
                getString(R.string.bundle_key_create_new_account_item),
                getResources().getBoolean(R.bool.intent_extras_default_value_create_new_account_item));
        setTitle(accountName);
        emptyAttributeListCommentTextView = findViewById(R.id.empty_attribute_list_comment_text_view);

        // Build up recycler view.
        recyclerView = findViewById(R.id.account_detail_recycler_view);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        accountDetailAdapter = new AccountDetailAdapter(this, this);
        recyclerView.setAdapter(accountDetailAdapter);

        // Set data source of the adapter.
        accountDetailAdapter.setAccountItemHandler(accountItemHandler);

        // Set up loader.
        getSupportLoaderManager().initLoader(
                getResources().getInteger(R.integer.account_list_handler_loader_id),
                null,
                this);
        broadcastFileObserver = new BroadcastFileObserver(
                getApplicationContext(),
                getString(R.string.account_list_handler_loader_reload_action),
                FileUtil.combinePaths(getFilesDir().getAbsolutePath(), getString(R.string.accounts_file_path)),
                FileObserver.MODIFY);
        broadcastFileObserver.startWatching();

        // TODO: Show loading indicator until loader has finished loading attributes
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addAccountAttributeFabOnClick(View view) {
        showAddAttributeDialog();
    }

    private void showAddAttributeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_add_account_attribute));

        // Set up the input for the dialog
        final InstantAutoCompleteTextView keyInput = new InstantAutoCompleteTextView(this);
        final EditText valueInput = new EditText(this);

        // Specify the type of input expected.
        keyInput.setInputType(InputType.TYPE_CLASS_TEXT);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.account_attribute_keys));
        keyInput.setAdapter(adapter);
        keyInput.setHint(R.string.dialog_account_attribute_key_hint);
        valueInput.setInputType(InputType.TYPE_CLASS_TEXT);
        valueInput.setHint(R.string.dialog_account_attribute_value_hint);

        // Add the input to the dialog.
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(keyInput);
        layout.addView(valueInput);
        builder.setView(layout);

        // Set up the button options for the dialog.
        builder.setPositiveButton(getString(R.string.dialog_option_create), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String key = keyInput.getText().toString();
                String value = valueInput.getText().toString();
                if(key == null || key.isEmpty() || value == null || value.isEmpty()) {
                    dialog.cancel();
                    Toast.makeText(
                            AccountDetailActivity.this,
                            AccountDetailActivity.this.getString(R.string.dialog_add_attribute_empty_input_error_msg),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    if(accountItemHandler.attributeExists(getApplicationContext(), key)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailActivity.this);
                        builder.setTitle(getString(R.string.dialog_title_overwrite_account_attribute));

                        // Set up the button options for the dialog.
                        builder.setPositiveButton(getString(R.string.dialog_option_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                String key = keyInput.getText().toString();
                                String value = valueInput.getText().toString();
                                if(accountItemHandler.addAttribute(AccountDetailActivity.this, accountDetailAdapter, key, value, new Date())) {
                                    accountListHandler.addAccount(
                                            AccountDetailActivity.this,
                                            null,
                                            accountItemHandler,
                                            true,
                                            getString(R.string.accounts_file_path),
                                            new Date(),
                                            KeyHolder.getInstance().getKey(),
                                            true);
                                }
                                else {
                                    Toast.makeText(
                                            AccountDetailActivity.this,
                                            AccountDetailActivity.this.getString(R.string.add_attribute_error_msg),
                                            Toast.LENGTH_SHORT).show();
                                }
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
                        if(accountItemHandler.addAttribute(AccountDetailActivity.this, accountDetailAdapter, key, value, new Date())) {
                            accountListHandler.addAccount(
                                    AccountDetailActivity.this,
                                    null,
                                    accountItemHandler,
                                    true,
                                    getString(R.string.accounts_file_path),
                                    new Date(),
                                    KeyHolder.getInstance().getKey(),
                                    true);
                        }
                        else {
                            Toast.makeText(
                                    AccountDetailActivity.this,
                                    AccountDetailActivity.this.getString(R.string.add_attribute_error_msg),
                                    Toast.LENGTH_SHORT).show();
                        }
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
    public boolean onMenuItemClick(MenuItem item, final String attributeKey) {
        switch(item.getItemId()) {
            case R.id.copy_attribute_menu_item: {
                ClipboardUtil clipboardUtil = new ClipboardUtil(this);
                clipboardUtil.copyAndDelete(getString(R.string.nfc_app_mime_type),
                        accountItemHandler.getAttributeValue(this, attributeKey),
                        getResources().getInteger(R.integer.copy_delete_duration));
                return true;
            }
            case R.id.edit_attribute_menu_item: {
                showEditAttributeDialog(attributeKey, accountItemHandler.getAttributeValue(this, attributeKey));
                return true;
            }
            case R.id.delete_attribute_menu_item: {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailActivity.this);
                builder.setTitle(getString(R.string.dialog_title_delete_attribute));
                builder.setPositiveButton(getString(R.string.dialog_option_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        accountItemHandler.removeAttribute(
                                getApplicationContext(),
                                accountDetailAdapter,
                                attributeKey,
                                new Date());
                        accountListHandler.addAccount(
                                AccountDetailActivity.this,
                                null,
                                accountItemHandler,
                                true,
                                getString(R.string.accounts_file_path),
                                new Date(),
                                KeyHolder.getInstance().getKey(),
                                true);
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

    private void showEditAttributeDialog(final String attributeKey, String oldAttributeValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_edit_account_attribute));

        // Set up the input for the dialog
        final EditText valueInput = new EditText(this);
        valueInput.setText(oldAttributeValue);

        // Specify the type of input expected.
        valueInput.setInputType(InputType.TYPE_CLASS_TEXT);
        valueInput.setHint(R.string.dialog_account_attribute_value_hint);

        // Add the input to the dialog.
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(valueInput);
        builder.setView(layout);

        // Set up the button options for the dialog.
        builder.setPositiveButton(getString(R.string.dialog_option_edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newAttributeValue = valueInput.getText().toString();
                if(newAttributeValue == null || newAttributeValue.isEmpty()) {
                    dialog.cancel();
                    Toast.makeText(
                            AccountDetailActivity.this,
                            AccountDetailActivity.this.getString(R.string.dialog_edit_attribute_empty_input_error_msg),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    if(accountItemHandler.changeAttributeValue(AccountDetailActivity.this, accountDetailAdapter, attributeKey, newAttributeValue, new Date())) {
                        accountListHandler.addAccount(
                                AccountDetailActivity.this,
                                null,
                                accountItemHandler,
                                true,
                                getString(R.string.accounts_file_path),
                                new Date(),
                                KeyHolder.getInstance().getKey(),
                                true);
                    }
                    else {
                        Toast.makeText(
                                AccountDetailActivity.this,
                                AccountDetailActivity.this.getString(R.string.edit_attribute_error_msg),
                                Toast.LENGTH_SHORT).show();
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

    private void showEmptyAttributeListComment() {
        if(accountDetailAdapter.getItemCount() > 0) {
            emptyAttributeListCommentTextView.setVisibility(View.INVISIBLE);
        }
        else {
            emptyAttributeListCommentTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<AccountListHandler> onCreateLoader(int id, Bundle args) {
        if(id == getResources().getInteger(R.integer.account_list_handler_loader_id)) {
            Log.i(TAG, "Creating new " + AccountListHandlerLoader.class.getSimpleName() + ".");
            return new AccountListHandlerLoader(this, getString(R.string.accounts_file_path));
        }
        else {
            throw new IllegalArgumentException("The loader ID must be valid.");
        }
    }

    @Override
    public void onLoadFinished(Loader<AccountListHandler> loader, AccountListHandler data) {
        Log.i(TAG, "Processing data of finished loader.");
        accountListHandler = data;
        try {
            if(createNewAccountItem && accountItemHandler == null) {
                accountItemHandler = new AccountItemHandler(getApplicationContext(), accountName, new Date());
            }
            else if(!createNewAccountItem) {
                accountItemHandler = accountListHandler.getAccount(getApplicationContext(), accountName);
                if(accountItemHandler == null) {
                    this.accountItemHandler = new AccountItemHandler(getApplicationContext(), accountName, new Date());
                }
            }
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not create " + AccountItemHandler.class.getSimpleName() + " from account name.");
            Toast.makeText(this, getString(R.string.json_error_msg), Toast.LENGTH_SHORT).show();
            finish();
        }
        accountDetailAdapter.setAccountItemHandler(accountItemHandler);
        showEmptyAttributeListComment();
    }

    @Override
    public void onLoaderReset(Loader<AccountListHandler> loader) {
        Log.i(TAG, "Processing loader reset.");
    }
}
