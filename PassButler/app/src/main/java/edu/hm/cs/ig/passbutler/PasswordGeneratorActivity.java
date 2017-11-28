package edu.hm.cs.ig.passbutler;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import edu.hm.cs.ig.passbutler.data.ClipboardUtil;
import edu.hm.cs.ig.passbutler.data.PasswordUtil;

// TODO: add Copybutton
// TODO: use own Random

public class PasswordGeneratorActivity extends AppCompatActivity {

    private SeekBar passwordLengthSeekBar;
    private Integer minLength, maxLength;

    private Switch lowerSwitch;
    private Switch upperSwitch;
    private Switch numbersSwitch;
    private Switch specialSwitch;

    private TextView passwordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevent screen capturing on non-rooted devices
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

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

        lowerSwitch = findViewById(R.id.sw_password_generator_lowercase);
        upperSwitch = findViewById(R.id.sw_password_generator_uppercase);
        numbersSwitch = findViewById(R.id.sw_password_generator_numbers);
        specialSwitch = findViewById(R.id.sw_password_generator_special);

        passwordTextView = findViewById(R.id.tv_password_generator_password);
    }

    public void generateButtonOnClick(View view) {
        String password;

        int length = passwordLengthSeekBar.getProgress() + minLength;

        password = generatePassword(lowerSwitch.isChecked(), upperSwitch.isChecked(),
                numbersSwitch.isChecked(), specialSwitch.isChecked(),
                length);

        if(password.isEmpty()) {
            Toast.makeText(this, getString(R.string.password_generator_no_switch_msg), Toast.LENGTH_LONG).show();
        }
        int strength = PasswordUtil.checkPassword(password);
        switch (strength) {
            case 0:
            case 1: passwordTextView.setBackgroundColor(getResources().getColor(R.color.weakPassword));
                    break;
            case 2:
            case 3: passwordTextView.setBackgroundColor(getResources().getColor(R.color.okayPassword));
                    break;
            case 4: passwordTextView.setBackgroundColor(getResources().getColor(R.color.goodPassword));

        }

        passwordTextView.setText(password);
    }

    public void copyButtonOnClick(View view) {
        ClipboardUtil clipboardUtil = new ClipboardUtil(this);
        clipboardUtil.copyAndDelete("Passwordgenerator", passwordTextView.getText().toString(), 20);
    }

    private String generatePassword(boolean useLower, boolean useUpper,
                                    boolean useNumbers, boolean useSpecial,
                                    int length) {
        final String lower = getString(R.string.password_generator_chars_lowercase);
        final String upper = getString(R.string.password_generator_chars_uppercase);
        final String numbers = getString(R.string.password_generator_chars_numbers);
        final String special = getString((R.string.password_generator_chars_special));

        StringBuilder passwordBuilder = new StringBuilder(length);
        SecureRandom random = new SecureRandom();

        List<String> charCategories = new ArrayList<>(4);
        if (useLower) {
            charCategories.add(lower);
        }
        if (useUpper) {
            charCategories.add(upper);
        }
        if (useNumbers) {
            charCategories.add(numbers);
        }
        if (useSpecial) {
            charCategories.add(special);
        }

        if (!charCategories.isEmpty()) {
            for (int i = 0; i < length; i++) {
                String charCategory = charCategories.get(random.nextInt(charCategories.size()));
                int position = random.nextInt(charCategory.length());
                passwordBuilder.append(charCategory.charAt(position));
            }
        } else {
            return "";
        }

        return new String(passwordBuilder);
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
}
