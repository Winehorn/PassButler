package edu.hm.cs.ig.passbutler.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.crypto.BadPaddingException;

import edu.hm.cs.ig.passbutler.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by dennis on 15.11.17.
 */

public class FileUtil {

    private static final String TAG = FileUtil.class.getName();

    public static String combinePaths(String path1, String path2) {
        File file = new File(path1, path2);
        return file.getPath();
    }

    public static boolean fileExists(Context context, String fileName) {
        String basePath = context.getFilesDir().getAbsolutePath();
        File file = new File(basePath, fileName);
        return file.exists();
    }

    public static boolean writeToInternalStorage(Context context, String fileName, String s)
    {
        try {
            return writeToOutputStream(context, context.openFileOutput(fileName, MODE_PRIVATE), s);
        }
        catch(FileNotFoundException e) {
            Log.e(TAG, "Could not find file to write to in internal storage.");
            return false;
        }
    }

    public static boolean writeToOutputStream(Context context, OutputStream outputStream, String s) {
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
            outputStreamWriter.write(s);
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while writing to output stream.");
            return false;
        }
        return true;
    }

    public static String readFromInternalStorage(Context context, String fileName) {
        File file = getInternalStorageFile(context, fileName);
        try {
            return readFromInputStream(new FileInputStream(file));
        }
        catch(FileNotFoundException e) {
            Log.e(TAG, "Could not find file to read from in internal storage.");
            return null;
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading from internal storage.");
            return null;
        }
    }

    public static File getInternalStorageFile(Context context, String fileName) {
        String basePath = context.getFilesDir().getAbsolutePath();
        return new File(basePath, fileName);
    }

    public static String readFromInputStream(InputStream inputStream) throws IOException {
        try (InputStream streamToRead = inputStream) {
            String ret = convertStreamToString(streamToRead);
            return ret;
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading from input stream.");
            throw e;
        }
    }

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading from buffered reader.");
            throw e;
        }
    }
}
