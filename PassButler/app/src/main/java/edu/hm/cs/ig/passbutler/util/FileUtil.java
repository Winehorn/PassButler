package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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
import java.nio.channels.FileChannel;

import edu.hm.cs.ig.passbutler.R;
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
        return getInternalStorageFile(context, filePath).exists();
    }

    public static File getInternalStorageFile(Context context, String filePath) {
        String basePath = context.getFilesDir().getAbsolutePath();
        return new File(basePath, filePath);
    }

    public static File getExternalAppDir(Context context) {
        File extDir = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.external_directory_name));
        return extDir;
    }

    public static String getFileHash(Context context, String filePath) {
        return getFileHash(context, getInternalStorageFile(context, filePath));
    }

    public static String getFileHash(Context context, File file) {
        byte[] fileContent = readBytesFromFile(file);
        return CryptoUtil.digestToString(context.getString(R.string.hash_func_for_digest), fileContent);
    }

    public static boolean isFileManipulated(Context context, String filePath) {
        Log.i(TAG, "Checking file " + filePath + " for manipulation.");
        String savedFileHash = SyncContentProviderUtil.getFileHash(context, filePath);
        if(savedFileHash != null) {
            String actualFileHash = FileUtil.getFileHash(context, filePath);
            boolean isFileManipulated = !savedFileHash.equals(actualFileHash);
            Log.i(TAG, "Result of check if file is manipulated: " + isFileManipulated + ".");
            return  isFileManipulated;
        }
        Log.i(TAG, "File has no recorded hash value so manipulation is excluded.");
        return false;
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
        return writeToFile(
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
        return writeToFile(
                context,
                new File(combinePaths(context.getFilesDir().getPath(), fileMetaData.getFilePath())),
                fileMetaData,
                bytes,
                persistMetaData);
    }

    public static boolean writeToFile(
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

    public static boolean writeToFile(
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

    public static byte[] readBytesFromInternalStorage(Context context, String filePath) {
        return readBytesFromFile(getInternalStorageFile(context, filePath));
    }

    public static byte[] readBytesFromFile(File file) {
        try {
            return readBytesFromInputStream(new FileInputStream(file));
        }
        catch(FileNotFoundException e) {
            Log.e(TAG, "Could not find file to read from.");
            return null;
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading from.");
            return null;
        }
    }

    public static byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        try (InputStream streamToRead = inputStream) {
            return IOUtils.toByteArray(streamToRead);
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading from input stream.");
            throw e;
        }
    }

    public static String readStringFromInternalStorage(Context context, String filePath) {
        return readStringFromInternalStorage(getInternalStorageFile(context, filePath));
    }

    public static String readStringFromInternalStorage(File file) {
        try {
            return readStringFromInputStream(new FileInputStream(file));
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

    public static String readStringFromInputStream(InputStream inputStream) throws IOException {
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

    public static String getFullInternalPath(Context context, String filePath) {
        return combinePaths(context.getFilesDir().getAbsolutePath(), filePath);
    }

    public static File importFile(File srcFile, File dstFile) throws IOException {


        if(!srcFile.exists()) {
            throw new FileNotFoundException("srcFile not found!");
        }
        
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(srcFile).getChannel();
            outChannel = new FileOutputStream(dstFile).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

        return dstFile;
    }

    public static File exportFile(Context context, File srcFile, File dstDir) throws IOException {

        //if folder does not exist
        if (!dstDir.exists()) {
            if (!dstDir.mkdir()) {
                Log.d(TAG, "exportFile: dstDir is null");
                return null;
            }
        }

        File expFile = new File(dstDir.getPath() + "/" + context.getString(R.string.backup_file_name));

        if(expFile.exists()) {
            Log.i(TAG, "exportFile: External file exists.");
            if(expFile.delete()) {
                Log.i(TAG, "exportFile: External file deleted.");
            }
        }

        FileChannel inChannel = null;
        FileChannel outChannel = null;


        inChannel = new FileInputStream(srcFile).getChannel();
        outChannel = new FileOutputStream(expFile).getChannel();


        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

        return expFile;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
