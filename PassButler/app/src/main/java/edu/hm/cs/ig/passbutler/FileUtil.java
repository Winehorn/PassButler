package edu.hm.cs.ig.passbutler;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by dennis on 15.11.17.
 */

public class FileUtil {

    private static final String TAG = FileUtil.class.getName();

    public static boolean saveStringToInternalStorageFile(Context context, String fileName, String s)
    {
        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(context.openFileOutput(fileName, MODE_PRIVATE));
            outputStreamWriter.write(s);
            outputStreamWriter.close();
        }
        catch(FileNotFoundException e) {
            Log.e(TAG, "File to save to not found in internal storage.");
            return false;
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while saving to file in internal storage.");
            return false;
        }
        return true;
    }

    public static String loadStringFromInternalStorageFile(Context context, String fileName) {
        String basePath = context.getFilesDir().getAbsolutePath();
        File file = new File(basePath, fileName);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String ret = convertStreamToString(fileInputStream);
            return ret;
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while loading from file in internal storage.");
            return null;
        }
    }

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        }
    }
}
