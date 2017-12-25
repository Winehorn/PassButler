package edu.hm.cs.ig.passbutler.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import edu.hm.cs.ig.passbutler.R;

/**
 * Created by dennis on 09.12.17.
 */

public class BluetoothSyncSenderJobService extends JobService {

    public static final String JOB_TAG = "bluetooth_sync_sender_service";
    public static BluetoothSyncSender bluetoothSyncSender;
    private BluetoothSyncSenderAsyncTask backgroundTask;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothSyncSender = new BluetoothSyncSender(
                getApplicationContext(),
                getString(R.string.accounts_file_path));
    }

    @Override
    public boolean onStartJob(JobParameters jobParams) {
        backgroundTask = new BluetoothSyncSenderAsyncTask(getApplicationContext(), jobParams);
        backgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (backgroundTask != null) {
            backgroundTask.cancel(false);
        }
        return true;
    }

    public class BluetoothSyncSenderAsyncTask extends AsyncTask<Void, Void, Void> {

        Context context;
        JobParameters jobParams;

        BluetoothSyncSenderAsyncTask(Context context, JobParameters jobParams) {
            this.context = context;
            this.jobParams = jobParams;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            bluetoothSyncSender.syncAllDevices();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if(jobParams != null) {
                jobFinished(jobParams, false);
            }
        }
    }
}
