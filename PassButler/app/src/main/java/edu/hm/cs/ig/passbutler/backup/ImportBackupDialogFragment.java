package edu.hm.cs.ig.passbutler.backup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.util.FileUtil;
import edu.hm.cs.ig.passbutler.util.NavigationUtil;

/**
 * Created by Florian Kraus on 16.12.2017.
 */

public class ImportBackupDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        final String TAG = this.getTag();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_title_backup_import);
        builder.setMessage(R.string.dialog_message_backup_import)
                .setPositiveButton(R.string.dialog_option_import, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Check if internal file exists
                        if (FileUtil.internalStorageFileExists(context, getString(R.string.accounts_file_path))) {
                            // Import backup
                            try {
                                File destFile = FileUtil.importFile(new File(FileUtil.getExternalAppDir(context).getAbsolutePath() +
                                                "/" + getString(R.string.backup_file_name)),
                                        FileUtil.getInternalStorageFile(context,
                                                getString(R.string.accounts_file_path)));

                                Log.d(TAG, "importBackup: internalFile: " + destFile.getAbsolutePath());
                                Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show();
                                NavigationUtil.goToLogoActivity(context);

                            } catch (FileNotFoundException e) {
                                Toast.makeText(context, "Could not find " + getString(R.string.backup_file_name) +
                                        " in " + FileUtil.getExternalAppDir(context).getAbsolutePath() +
                                        "/ !", Toast.LENGTH_LONG).show();

                                Log.e(TAG, "createBackup: Could not find file.");
                                e.printStackTrace();
                            } catch (IOException e) {
                                Toast.makeText(context, "Error while importing Backup!", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "createBackup: Error while reading or writing.");
                                e.printStackTrace();
                            }
                        } else {
                            Log.w(TAG, "onClick: Internal file does not exist.");
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_option_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}