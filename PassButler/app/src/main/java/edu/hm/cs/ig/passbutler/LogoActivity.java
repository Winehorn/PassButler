package edu.hm.cs.ig.passbutler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);


        // TODO: Start different activities depending on if there is a persistence file available.
        Intent intent = new Intent(this, UnlockActivity.class);
        startActivity(intent);
    }
}
