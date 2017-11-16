package edu.hm.cs.ig.passbutler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AccountListActivity extends AppCompatActivity {

    private static final String TAG = AccountListActivity.class.getName();
    private RecyclerView recyclerView;
    private AccountAdapter accountAdapter;
    private AccountHandler accountHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Get account list from intent.
        Intent intent = getIntent();
        if(!intent.hasExtra(getString(R.string.intent_key_account_handler))) {
            Log.wtf(TAG, "Intent must contain an account list.");
            throw new IllegalStateException("Intent must contain an account list.");
        }
        accountHandler = intent.getExtras().getParcelable(getString(R.string.intent_key_account_handler));

        // Build up recycler view.
        recyclerView = (RecyclerView) findViewById(R.id.accounts_recyclerview);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        accountAdapter = new AccountAdapter(this);
        recyclerView.setAdapter(accountAdapter);

        // Set data source of the adapter.
        accountAdapter.setAccountHandler(accountHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.account_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
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
}
