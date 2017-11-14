package edu.hm.cs.ig.passbutler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class UnlockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
    }

    public void unlockButtonOnClick(View view)
    {
        // TODO: Check if password is right
        Intent intent = new Intent(this, AccountListActivity.class);
        startActivity(intent);
    }
}