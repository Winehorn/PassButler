package edu.hm.cs.ig.passbutler.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.backup.ImportBackupDialogFragment;

/**
 * Created by flori on 19.12.2017.
 */

public class BackupUtil {
    private static final String TAG = BackupUtil.class.getName();
    public static void createBackup(Context context) {
        if (FileUtil.internalStorageFileExists(context, context.getString(R.string.accounts_file_path))) {

            if (FileUtil.isExternalStorageWritable()) {
                try {
                    File expFile = FileUtil.exportFile(context,
                            FileUtil.getInternalStorageFile(context, context.getString(R.string.accounts_file_path)),
                            FileUtil.getExternalAppDir(context));

                    Log.i(TAG, "createBackup: Filepath:" + expFile.getAbsolutePath());
                    Toast.makeText(context, "Backup created!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "createBackup: internalFile: " + FileUtil.getInternalStorageFile(context, context.getString(R.string.accounts_file_path)).getAbsolutePath());

                } catch (FileNotFoundException e) {
                    Toast.makeText(context, "Backup not found!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "createBackup: Could not find file.");
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(context, "Error while creating Backup!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "createBackup: Error while reading or writing.");
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "createBackup: External Storage is not writable");
                Toast.makeText(context,
                        "External Storage not writable! Please add the Storage Permission!", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "createBackup: Internal file does not exist.");
        }
    }

    public static void importBackup(Activity activity) {
        ImportBackupDialogFragment dialog = new ImportBackupDialogFragment();
        dialog.show(activity.getFragmentManager(), ImportBackupDialogFragment.class.getName());
    }
}
