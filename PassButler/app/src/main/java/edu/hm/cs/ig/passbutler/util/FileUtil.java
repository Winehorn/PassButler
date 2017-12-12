package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import edu.hm.cs.ig.passbutler.data.FileMetaData;

/**
 * Created by dennis on 15.11.17.
 */

public class FileUtil {

    private static final String TAG = FileUtil.class.getName();

    public static String combinePaths(String path1, String path2) {
        File file = new File(path1, path2);
        return file.getPath();
    }

    public static String combinePaths(String path1, String path2, String path3) {
        String newPath = combinePaths(path1, path2);
        return combinePaths(newPath, path3);
    }

    public static boolean internalStorageFileExists(Context context, String filePath) {
        return getFromInternalStorage(context, filePath).exists();
    }

    public static File getFromInternalStorage(Context context, String filePath) {
        String basePath = context.getFilesDir().getAbsolutePath();
        File file = new File(basePath, filePath);
        return file;
    }

    public static boolean writeToInternalStorage(
            Context context,
            String filePath,
            String s) {
        return writeToInternalStorage(
                context,
                new FileMetaData(filePath, null, null),
                s,
                false);
    }

    public static boolean writeToInternalStorage(
            Context context,
            String filePath,
            byte[] bytes) {
        return writeToInternalStorage(
                context,
                new FileMetaData(filePath, null, null),
                bytes,
                false);
    }

    public static boolean writeToInternalStorage(
            Context context,
            FileMetaData fileMetaData,
            String s,
            boolean persistMetaData)
    {
        return writeToInternalStorage(
                context,
                new File(combinePaths(context.getFilesDir().getPath(), fileMetaData.getFilePath())),
                fileMetaData,
                s,
                persistMetaData);
    }

    public static boolean writeToInternalStorage(
            Context context,
            FileMetaData fileMetaData,
            byte[] bytes,
            boolean persistMetaData)
    {
        return writeToInternalStorage(
                context,
                new File(combinePaths(context.getFilesDir().getPath(), fileMetaData.getFilePath())),
                fileMetaData,
                bytes,
                persistMetaData);
    }

    public static boolean writeToInternalStorage(
            Context context,
            File file,
            FileMetaData fileMetaData,
            String s,
            boolean persistMetaData)
    {
        file.getParentFile().mkdirs();
        try {
            boolean isWritten = writeToOutputStream(
                    context,
                    new FileOutputStream(file),
                    s);
            if(isWritten && persistMetaData) {
                SyncContentProviderUtil.persistFileMetaData(context, fileMetaData);
            }
            return isWritten;
        }
        catch(FileNotFoundException e) {
            Log.e(TAG, "Could not find file to write to in internal storage.");
            return false;
        }
    }

    public static boolean writeToInternalStorage(
            Context context,
            File file,
            FileMetaData fileMetaData,
            byte[] bytes,
            boolean persistMetaData)
    {
        file.getParentFile().mkdirs();
        try {
            FileUtils.writeByteArrayToFile(file, bytes);
            if(persistMetaData) {
                SyncContentProviderUtil.persistFileMetaData(context, fileMetaData);
            }
            return true;
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while writing byte array to file.");
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

    public static String readFromInternalStorage(Context context, String filePath) {
        return readFromInternalStorage(getInternalStorageFile(context, filePath));
    }

    public static String readFromInternalStorage(File file) {
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

    public static File getInternalStorageFile(Context context, String filePath) {
        String basePath = context.getFilesDir().getAbsolutePath();
        return new File(basePath, filePath);
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
