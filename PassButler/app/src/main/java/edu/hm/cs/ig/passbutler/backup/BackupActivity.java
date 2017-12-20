package edu.hm.cs.ig.passbutler.backup;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.gui.PostAuthActivity;
import edu.hm.cs.ig.passbutler.util.FileUtil;

public class BackupActivity extends PostAuthActivity {

    private TextView textView;
    private static final String TAG = BackupActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        textView = findViewById(R.id.backup_textview);
    }

    public void createBackupButtonClick(View view) {
        if (FileUtil.internalStorageFileExists(this, getString(R.string.accounts_file_path))) {
            String internalFile = FileUtil.readFromInternalStorage(this, getString(R.string.accounts_file_path));
            textView.setText(internalFile);

            if (FileUtil.isExternalStorageWritable()) {
                try {
                    File expFile = FileUtil.exportFile(this,
                            FileUtil.getInternalStorageFile(this, getString(R.string.accounts_file_path)),
                            FileUtil.getExternalAppDir(this));

                    Log.i(TAG, "createBackup: Filepath:" + expFile.getAbsolutePath());
                    Toast.makeText(this, "Backup created!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "createBackup: internalFile: " + FileUtil.getInternalStorageFile(this, getString(R.string.accounts_file_path)).getAbsolutePath());

                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "Error while creating Backup!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "createBackup: Could not find file.");
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(this, "Error while creating Backup!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "createBackup: Error while reading or writing.");
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "createBackup: External Storage is not writable");
                Toast.makeText(this, "External Storage not writable!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "createBackup: Internal file does not exist.");
        }
    }

    public void importBackupButtonClick(View view) {
        ImportBackupDialogFragment dialog = new ImportBackupDialogFragment();
        dialog.show(getFragmentManager(), ImportBackupDialogFragment.class.getName());
    }
}

