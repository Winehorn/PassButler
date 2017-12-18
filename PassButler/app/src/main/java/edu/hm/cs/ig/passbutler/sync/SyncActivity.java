package edu.hm.cs.ig.passbutler.sync;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Set;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.data.SyncContract;
import edu.hm.cs.ig.passbutler.gui.PostAuthActivity;
import edu.hm.cs.ig.passbutler.util.ServiceUtil;

public class SyncActivity extends PostAuthActivity implements LoaderManager.LoaderCallbacks<Cursor>, BluetoothSyncDeviceAdapterOnMenuItemClickHandler {

    public static final String TAG = SyncActivity.class.getName();
    private Switch bluetoothSyncSwitch;
    private RecyclerView recyclerView;
    private BluetoothSyncDeviceAdapter bluetoothSyncDeviceAdapter;
    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        bluetoothSyncSwitch = findViewById(R.id.bluetooth_sync_switch);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Log.i(TAG, "No Bluetooth support detected. Stopping activity.");
            Toast.makeText(this, getString(R.string.no_bluetooth_support_sync_error_msg), Toast.LENGTH_SHORT).show();
            NavUtils.navigateUpFromSameTask(this);
        }

        // Build up recycler view.
        recyclerView = findViewById(R.id.bluetooth_sync_devices_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        bluetoothSyncDeviceAdapter = new BluetoothSyncDeviceAdapter(this, this);
        recyclerView.setAdapter(bluetoothSyncDeviceAdapter);

        // Set up loader.
        getSupportLoaderManager().initLoader(
                getResources().getInteger(R.integer.bluetooth_sync_device_loader_id),
                null,
                this);

        // TODO: Display loading indicator until loader has finished.
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == getResources().getInteger(R.integer.enable_bluetooth_request_code) || requestCode == getResources().getInteger(R.integer.enable_bluetooth_and_add_sync_device_request_code)) {
            if(resultCode == RESULT_OK) {
                Log.i(TAG, "Bluetooth has been enabled.");
                if(requestCode == getResources().getInteger(R.integer.enable_bluetooth_and_add_sync_device_request_code)) {
                    addBluetoothSyncDevice();
                }
            }
            else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Bluetooth has not been enabled.");
            }
            else {
                Log.e(TAG, "The result code must be valid.");
                Toast.makeText(this, getString(R.string.enable_bluetooth_error_msg), Toast.LENGTH_SHORT).show();
                NavUtils.navigateUpFromSameTask(this);
            }
        }
    }

    public void syncNowButtonOnClick(View view) {
        //new BluetoothSyncSenderJobService.BluetoothSyncSenderAsyncTask(getApplicationContext(), null).execute();

        // TODO: Remove
        Toast.makeText(this, "Sync try", Toast.LENGTH_LONG).show();
    }

    public void pairDevicesButtonOnClick(View view) {
        openBluetoothSettings();
    }

    public void bluetoothSyncSwitchOnClick(View view) {
        // TODO: Wird switch state bei erzeugen neuer activity auf default gesetzt?

        if(bluetoothSyncSwitch.isChecked()) {
            ServiceUtil.startSyncReceiverService(this);
            ServiceUtil.startSyncSenderService(this);
        }
        else {
            ServiceUtil.cancelSyncReceiverService(this);
            ServiceUtil.cancelSyncSenderService(this);
        }
    }

    public void addBluetoothSyncDeviceFabOnClick(View view) {
        addBluetoothSyncDevice();
    }

    public void addBluetoothSyncDevice() {
        if(!bluetoothAdapter.isEnabled()) {
            enableBluetooth(getResources().getInteger(R.integer.enable_bluetooth_and_add_sync_device_request_code));
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_bluetooth_sync_device));
        builder.setItems(getBondedDeviceNames(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BluetoothDevice device = getBondedDevice(which);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                ContentValues contentValues = new ContentValues();
                contentValues.put(SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_NAME, deviceName);
                contentValues.put(SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_HARDWARE_ADDRESS, deviceHardwareAddress);
                Uri uri = getContentResolver().insert(SyncContract.BluetoothSyncDeviceEntry.CONTENT_URI, contentValues);
                if(uri == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_bluetooth_sync_device_error_msg), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Bluetooth device with name "
                            + deviceName
                            + " and hardware address "
                            + deviceHardwareAddress
                            + " could not be added to list of sync devices.");
                }
                else {
                    Log.i(TAG, "Added bluetooth device with name "
                            + deviceName
                            + " and hardware address "
                            + deviceHardwareAddress
                            + " to list of sync devices at URI "
                            + uri.toString()
                            + ".");
                }
            }
        });
        builder.show();
    }

    private void enableBluetooth(int requestCode) {
        Log.i(TAG, "Enabling Bluetooth.");
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, requestCode);
    }

    private void openBluetoothSettings() {
        Log.i(TAG, "Opening Bluetooth settings.");
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else {
            Log.i(TAG, "Could not open Bluetooth settings. Device does not support Bluetooth.");
            Toast.makeText(this, getString(R.string.no_bluetooth_support_pairing_error_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private String[] getBondedDeviceNames() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        String[] deviceNames = new String[pairedDevices.size()];
        int i = 0;
        for(BluetoothDevice device : pairedDevices) {
            deviceNames[i] = device.getName();
            ++i;
        }
        return deviceNames;
    }

    private BluetoothDevice getBondedDevice(int index) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        int i = 0;
        for(BluetoothDevice device : pairedDevices) {
            if(i == index) {
                return device;
            }
            ++i;
        }
        return null;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item, final String deviceId) {
        switch(item.getItemId()) {
            case R.id.delete_bluetooth_sync_device: {
                AlertDialog.Builder builder = new AlertDialog.Builder(SyncActivity.this);
                builder.setTitle(getString(R.string.dialog_title_delete_bluetooth_sync_device));
                builder.setPositiveButton(getString(R.string.dialog_option_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Uri uri = SyncContract.BluetoothSyncDeviceEntry.CONTENT_URI.buildUpon().appendPath(deviceId).build();
                        getContentResolver().delete(uri, null, null);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == getResources().getInteger(R.integer.bluetooth_sync_device_loader_id)) {
            Uri uri = SyncContract.BluetoothSyncDeviceEntry.CONTENT_URI;
            return new CursorLoader(
                    this,
                    uri,
                    null,
                    null,
                    null,
                    null);
        }
        else {
            throw new IllegalArgumentException("The loader ID must be valid.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "Processing data of finished loader.");
        bluetoothSyncDeviceAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(TAG, "Processing loader reset.");
    }
}
