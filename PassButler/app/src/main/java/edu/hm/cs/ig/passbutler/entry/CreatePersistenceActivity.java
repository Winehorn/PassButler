package edu.hm.cs.ig.passbutler.entry;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;

import javax.security.auth.DestroyFailedException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.data.AccountListHandler;
import edu.hm.cs.ig.passbutler.gui.PreAuthActivity;
import edu.hm.cs.ig.passbutler.security.KeyHolder;
import edu.hm.cs.ig.passbutler.util.ArrayUtil;
import edu.hm.cs.ig.passbutler.util.CryptoUtil;
import edu.hm.cs.ig.passbutler.util.FileUtil;
import edu.hm.cs.ig.passbutler.util.NavigationUtil;

public class CreatePersistenceActivity extends PreAuthActivity {

    private static final String TAG = CreatePersistenceActivity.class.getName();
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;

    private byte[] nfcKey = new byte[128];

    private boolean nfcAvailable = false;
    private boolean nfcKeySet = false;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_persistence);

        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        repeatPasswordEditText = (EditText) findViewById(R.id.repeat_password_edit_text);

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(nfcKey);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // TODO: nfcAvailable nutzen um Persistencezeug ohne NFC zu machen
            // TODO: Strings durch Resourcestrings ersetzen
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        } else {
            nfcAvailable = true;
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "Please activate NFC.", Toast.LENGTH_LONG).show();
            }
        }

        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);

        // Setup an intent filter for text/plain based dispatches
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        //IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        mFilters = new IntentFilter[] {
                ndef, tag,
        };

        // Setup a tech list for all NfcF tags
        mTechLists = new String[][] { new String[] { NfcF.class.getName() } };
    }

    @Override
    public void onBackPressed() {
        NavigationUtil.goToHomeScreen(this);
    }

    public void createButtonOnClick(View view) {
        if(!passwordEditText.getText().toString().equals(repeatPasswordEditText.getText().toString())) {
            Toast.makeText(this, getString(R.string.password_match_error_msg), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Inserted passwords do not match.");
            return;
        }
        if(FileUtil.internalStorageFileExists(this, getString(R.string.accounts_file_path)))
        {
            Toast.makeText(this, getString(R.string.accounts_file_exists_error_msg), Toast.LENGTH_SHORT).show();
            Log.wtf(TAG, "An accounts file must not exist when creating a new one.");
            return;
        }
        else
        {
            createPersistence();
            NavigationUtil.goToAccountListActivity(this);
        }
    }

    private void createPersistence() {
        byte[] password = ArrayUtil.getContentAsByteArray(passwordEditText);

        // If NFC is not available, use key substitute
        if(!nfcAvailable || !nfcKeySet) {
            nfcKey = null;
            nfcKey = new byte[]{7, 7, 7};
        }

        Log.d(TAG, "createPersistence: nfcKey length: " + nfcKey.length);

        try {
            /*
             * CAUTION: The arrays from which the key is derived are cleared during key generation
             * and should not be used afterwards!
             */
            KeyHolder.getInstance().setKeyAndClearOld(this, CryptoUtil.generateKey(
                    getString(R.string.hash_func_for_key_gen),
                    getString(R.string.encryption_alg),
                    password,
                    nfcKey));
        } catch (DestroyFailedException e) {
            Toast.makeText(this, getString(R.string.destroy_failed_error_msg), Toast.LENGTH_SHORT).show();
            Log.wtf(TAG, "Could not set new key in " + KeyHolder.class.getSimpleName() + ".");
            return;
        }
        AccountListHandler accountListHandler = new AccountListHandler(this);
        accountListHandler.saveToInternalStorage(
                this,
                getString(R.string.accounts_file_path),
                new Date(0L),   // Initialize with oldest date so that sync is possible with every other newer version.
                KeyHolder.getInstance().getKey(),
                false);
        Log.i(TAG, "New accounts file created.");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
                mTechLists);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag in activity " + TAG);

        // TODO: replace strings with string resources
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if(action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if(ndefFormatable != null) {
                try {
                    ndefFormatable.connect();
                    NdefRecord mimeRecord = NdefRecord.createMime(getString(R.string.nfc_app_mime_type), nfcKey);
                    NdefMessage ndefMessage = new NdefMessage(mimeRecord);
                    ndefFormatable.format(ndefMessage);
                    ndefFormatable.close();
                    nfcKeySet = true;
                    Toast.makeText(this, "Write success!", Toast.LENGTH_LONG).show();
                } catch (IOException | FormatException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Write failed!", Toast.LENGTH_LONG).show();
                }
            }
        } else if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Ndef ndef = Ndef.get(tag);
            if(ndef != null) {
                try {
                    ndef.connect();
                    NdefRecord mimeRecord = NdefRecord.createMime(getString(R.string.nfc_app_mime_type), nfcKey);
                    NdefMessage ndefMessage = new NdefMessage(mimeRecord);
                    ndef.writeNdefMessage(ndefMessage);
                    ndef.close();
                    nfcKeySet = true;
                    Toast.makeText(this, "Write success!", Toast.LENGTH_LONG).show();
                } catch (IOException | FormatException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Write failed!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}

