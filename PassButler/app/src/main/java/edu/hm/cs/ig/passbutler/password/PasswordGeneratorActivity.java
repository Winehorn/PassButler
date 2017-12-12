package edu.hm.cs.ig.passbutler.password;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.util.ClipboardUtil;
import edu.hm.cs.ig.passbutler.util.PasswordUtil;

public class PasswordGeneratorActivity extends AppCompatActivity {

    private DiscreteSeekBar passwordLengthSeekBar;

    private Switch lowerSwitch;
    private Switch upperSwitch;
    private Switch numbersSwitch;
    private Switch specialSwitch;
    private Switch expertSwitch;

    private TextView passwordTextView;
    private ExpandableLayout expertModeEL;

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

        passwordLengthSeekBar = findViewById(R.id.sb_password_generator_length);

        lowerSwitch = findViewById(R.id.sw_password_generator_lowercase);
        upperSwitch = findViewById(R.id.sw_password_generator_uppercase);
        numbersSwitch = findViewById(R.id.sw_password_generator_numbers);
        specialSwitch = findViewById(R.id.sw_password_generator_special);
        expertSwitch = findViewById(R.id.sw_password_generator_expert_mode);

        passwordTextView = findViewById(R.id.tv_password_generator_password);
        expertModeEL = findViewById(R.id.el_password_generator_expert_mode);

        expertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    expertModeEL.expand();
                } else {
                    expertModeEL.collapse();
                }
            }
        });
    }

    public void generateButtonOnClick(View view) {
        String password;

        if (expertSwitch.isChecked()) {


            int length = passwordLengthSeekBar.getProgress();

            password = PasswordUtil.generatePassword(lowerSwitch.isChecked(), upperSwitch.isChecked(),
                    numbersSwitch.isChecked(), specialSwitch.isChecked(),
                    length, this);

            if (password.isEmpty()) {
                Toast.makeText(this, getString(R.string.password_generator_no_switch_msg), Toast.LENGTH_LONG).show();
            }
        } else {
            password = PasswordUtil.generatePassword(this);
        }

        int strength = PasswordUtil.checkPassword(password);
        switch (strength) {
            case 0:
            case 1:
                passwordTextView.setBackgroundColor(getResources().getColor(R.color.weakPassword));
                break;
            case 2:
            case 3:
                passwordTextView.setBackgroundColor(getResources().getColor(R.color.okayPassword));
                break;
            case 4:
                passwordTextView.setBackgroundColor(getResources().getColor(R.color.goodPassword));

        }

        passwordTextView.setText(password);
    }

    public void copyButtonOnClick(View view) {
        ClipboardUtil clipboardUtil = new ClipboardUtil(this);
        clipboardUtil.copyAndDelete(getString(R.string.nfc_app_mime_type), passwordTextView.getText().toString(), 20);
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
