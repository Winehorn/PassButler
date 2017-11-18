package edu.hm.cs.ig.passbutler;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

public class PasswordGeneratorActivity extends AppCompatActivity {

    private SeekBar passwordLengthSeekBar;
    private Integer minLength, maxLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_generator);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set number of values in seekbar according to integers.xml
        passwordLengthSeekBar = findViewById(R.id.sb_password_generator_length);
        minLength = getResources().getInteger(R.integer.password_generator_min_length);
        maxLength = getResources().getInteger(R.integer.password_generator_max_length);
        passwordLengthSeekBar.setMax(maxLength - minLength);

    }

    public void generateButtonOnClick(View view) {
        Toast.makeText(this, "Generation in progress!", Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
