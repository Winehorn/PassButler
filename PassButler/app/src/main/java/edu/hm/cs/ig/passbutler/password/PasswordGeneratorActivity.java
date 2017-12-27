package edu.hm.cs.ig.passbutler.password;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.gui.PostAuthActivity;
import edu.hm.cs.ig.passbutler.util.ArrayUtil;
import edu.hm.cs.ig.passbutler.util.ClipboardUtil;
import edu.hm.cs.ig.passbutler.util.PasswordUtil;

public class PasswordGeneratorActivity extends PostAuthActivity {

    private DiscreteSeekBar passwordLengthSeekBar;

    private Switch lowerSwitch;
    private Switch upperSwitch;
    private Switch numbersSwitch;
    private Switch specialSwitch;
    private Switch expertSwitch;

    private EditText passwordEditText;
    private ExpandableLayout expertModeEL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        passwordEditText = findViewById(R.id.et_password_generator_password);
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
        char[] password;

        if (expertSwitch.isChecked()) {


            int length = passwordLengthSeekBar.getProgress();

            if (!lowerSwitch.isChecked() && !upperSwitch.isChecked() &&
                    !numbersSwitch.isChecked() && !specialSwitch.isChecked()) {
                Toast.makeText(this, getString(R.string.password_generator_no_switch_msg), Toast.LENGTH_LONG).show();
                return;
            } else {
                password = PasswordUtil.generatePassword(lowerSwitch.isChecked(), upperSwitch.isChecked(),
                        numbersSwitch.isChecked(), specialSwitch.isChecked(),
                        length, this);
            }
        } else {
            password = PasswordUtil.generatePassword(this);
        }

        // TODO: implement own passwordcheck
/*        int strength = PasswordUtil.checkPassword(password);
        switch (strength) {
            case 0:
            case 1:
                passwordEditText.setBackgroundColor(getResources().getColor(R.color.weakPassword));
                break;
            case 2:
            case 3:
                passwordEditText.setBackgroundColor(getResources().getColor(R.color.okayPassword));
                break;
            case 4:
                passwordEditText.setBackgroundColor(getResources().getColor(R.color.goodPassword));

        }*/

        passwordEditText.setText(password, 0, password.length);
        ArrayUtil.clear(password);
    }

    public void copyButtonOnClick(View view) {
        ClipboardUtil clipboardUtil = new ClipboardUtil(this);
        Editable password = passwordEditText.getText();
        clipboardUtil.copyAndDelete(getString(R.string.nfc_app_mime_type),
                password, getResources().getInteger(R.integer.copy_delete_duration));
        password.clear();
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
