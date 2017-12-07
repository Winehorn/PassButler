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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.DestroyFailedException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.account_list.AccountListActivity;
import edu.hm.cs.ig.passbutler.data.AccountListHandler;
import edu.hm.cs.ig.passbutler.util.ArrayUtil;
import edu.hm.cs.ig.passbutler.util.CryptoUtil;
import edu.hm.cs.ig.passbutler.encryption.KeyHolder;

public class UnlockActivity extends AppCompatActivity {

    private static final String TAG = UnlockActivity.class.getName();
    private EditText passwordEditText;

    private byte[] nfcKey = new byte[128];

    private boolean nfcAvailable = false;
    private boolean nfcKeyRead = false;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);

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

    public void unlockButtonOnClick(View view) {
        if(passwordEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.password_empty_error_msg), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Inserted password is empty.");
            return;
        }
        try {
            if (!isPasswordCorrect()) {
                Toast.makeText(this, getString(R.string.wrong_password_error_msg), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Wrong password was entered.");
                return;
            }
        }
        catch (JSONException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | NoSuchPaddingException
                | DestroyFailedException
                | IOException e) {
            Toast.makeText(this, getString(R.string.unlock_error_msg), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Could not check password for correctness.");
            return;
        }
        Log.i(TAG, "Correct password was entered.");
        Intent intent = new Intent(this, AccountListActivity.class);
        startActivity(intent);
        Log.i(TAG, "Proceeding to " + AccountListActivity.class.getSimpleName() + ".");
    }

    private boolean isPasswordCorrect() throws DestroyFailedException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, JSONException, FileNotFoundException, IOException {
        byte[] password = ArrayUtil.getContentAsByteArray(passwordEditText);

        // If NFC is not available, use key substitute
        if(!nfcAvailable || !nfcKeyRead) {
            nfcKey = null;
            nfcKey = new byte[]{7, 7, 7};
        }

        try {
            /*
             * CAUTION: The arrays from which the key is derived are cleared during key generation
             * and should not be used afterwards!
             */
            KeyHolder.getInstance().setKeyAndClearOld(this, CryptoUtil.generateKey(
                    getString(R.string.hash_func),
                    getString(R.string.encryption_alg),
                    password,
                    nfcKey));
            AccountListHandler.getFromFile(this, getString(R.string.accounts_file_name), KeyHolder.getInstance().getKey());
            nfcKeyRead = false;
            return true;
        }
        catch (IOException e) {
            if(e.getCause() instanceof BadPaddingException) {
                Log.i(TAG, "I/O error was caused by bad padding.");
                nfcKeyRead = false;
                return false;
            }
            throw e;
        }
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

        if(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                Ndef ndef = Ndef.get(tag);
                if (ndef != null) {
                    try {
                        ndef.connect();
                        NdefMessage ndefMessage = ndef.getNdefMessage();
                        Log.i(TAG, "onNewIntent: MIME type: " + ndefMessage.getRecords()[0].toMimeType());
                        if(ndefMessage.getRecords()[0].toMimeType().equals(getString(R.string.nfc_app_mime_type))) {
                            nfcKey = ndefMessage.getRecords()[0].getPayload();
                            nfcKeyRead = true;
                            Toast.makeText(this, "Read success!", Toast.LENGTH_LONG).show();
                        } else {
                            throw new IOException("Wrong MIME type: " + ndefMessage.getRecords()[0].toMimeType());
                        }
                        ndef.close();

                    } catch (IOException | FormatException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "This is the wrong tag or it's corrupted!", Toast.LENGTH_LONG).show();
                    }
                }
            }

        } else {
            Toast.makeText(this, "This is the wrong tag or it's corrupted!", Toast.LENGTH_LONG).show();
        }
    }
}
