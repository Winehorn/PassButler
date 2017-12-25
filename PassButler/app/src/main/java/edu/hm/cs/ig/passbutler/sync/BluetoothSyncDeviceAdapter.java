package edu.hm.cs.ig.passbutler.sync;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.data.SyncContract;

/**
 * Created by dennis on 03.12.17.
 */

public class BluetoothSyncDeviceAdapter extends RecyclerView.Adapter<BluetoothSyncDeviceAdapter.BluetoothSyncDeviceViewHolder>{

    private static final String TAG = BluetoothSyncDeviceAdapter.class.getName();
    private Context context;
    private Cursor cursor;
    BluetoothSyncDeviceAdapterOnMenuItemClickHandler menuItemClickHandler;

    public BluetoothSyncDeviceAdapter(Context context, BluetoothSyncDeviceAdapterOnMenuItemClickHandler menuItemClickHandler) {
        this.context = context;
        this.menuItemClickHandler = menuItemClickHandler;
    }

    @Override
    public BluetoothSyncDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.bluetooth_sync_device_item, parent, false);
        return new BluetoothSyncDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BluetoothSyncDeviceViewHolder holder, int position) {
        int idIndex =
                cursor.getColumnIndex(SyncContract.BluetoothSyncDeviceEntry._ID);
        int deviceNameIndex =
                cursor.getColumnIndex(SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_NAME);
        int deviceHardwareAddressIndex =
                cursor.getColumnIndex(SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_HARDWARE_ADDRESS);
        cursor.moveToPosition(position);
        final int id = cursor.getInt(idIndex);
        final String deviceName = cursor.getString(deviceNameIndex);
        final String deviceHardwareAddress = cursor.getString(deviceHardwareAddressIndex);
        holder.setDeviceId(String.valueOf(id));
        holder.deviceNameTextView.setText(deviceName);
        holder.deviceHardwareAddressTextView.setText(deviceHardwareAddress);
    }

    @Override
    public int getItemCount() {
        if (cursor == null) {
            return 0;
        }
        return cursor.getCount();
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    class BluetoothSyncDeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private String deviceId;
        public final TextView deviceNameTextView;
        public final TextView deviceHardwareAddressTextView;

        public BluetoothSyncDeviceViewHolder(View view) {
            super(view);
            deviceNameTextView = view.findViewById(R.id.bluetooth_sync_device_name_text_view);
            deviceHardwareAddressTextView = view.findViewById(R.id.bluetooth_sync_device_hardware_address_text_view);
            ImageButton imageButton = view.findViewById(R.id.bluetooth_sync_device_image_button);
            imageButton.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        public void setDeviceId(String newDeviceId) {
            this.deviceId = newDeviceId;
        }

        public String getDeviceId() {
            return this.deviceId;
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.bluetooth_sync_device_image_button) {
                final PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.bluetooth_sync_more_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return menuItemClickHandler.onMenuItemClick(item, this.deviceId);
        }
    }
}
