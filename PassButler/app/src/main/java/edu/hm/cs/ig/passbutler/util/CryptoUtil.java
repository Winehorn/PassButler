package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import edu.hm.cs.ig.passbutler.data.FileMetaData;

/**
 * Created by dennis on 24.11.17.
 */

public class CryptoUtil {

    public static final String TAG = CryptoUtil.class.getName();

    public static Key generateKey(String hashFunc, String encryptionAlg, byte[]... baseData) {
        if (baseData.length == 0) {
            throw new IllegalArgumentException("Valid base data for key creation must be specified.");
        }
        byte[] bytes = ArrayUtil.concatAndClearSrc(baseData);
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(hashFunc);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "The hash function must be valid.");
            return null;
        }
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digestedBytes = messageDigest.digest();
        Key key = new SecretKeySpec(digestedBytes, encryptionAlg);
        Log.i(TAG, "Key created created.");
        ArrayUtil.clear(bytes);
        ArrayUtil.clear(digestedBytes);
        return key;
    }

    public static boolean writeToInternalStorage(
            Context context,
            String filePath,
            String s,
            Key encryptionKey,
            String encryptionAlg) {
        return writeToInternalStorage(
                context,
                new FileMetaData(filePath, null, null),
                s,
                encryptionKey,
                encryptionAlg,
                false);
    }

    public static boolean writeToInternalStorage(
            Context context,
            FileMetaData fileMetaData,
            String s,
            Key encryptionKey,
            String encryptionAlg,
            boolean persistMetaData) {
        try {
            FileOutputStream fileOutputStream =
                    new FileOutputStream(FileUtil.combinePaths(context.getFilesDir().getPath(), fileMetaData.getFilePath()));
            Cipher cipher = Cipher.getInstance(encryptionAlg);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            boolean isWritten = FileUtil.writeToOutputStream(context, new CipherOutputStream(fileOutputStream, cipher), s);
            if(isWritten && persistMetaData) {
                SyncContentProviderUtil.persistFileMetaData(context, fileMetaData);
            }
            return isWritten;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not find file to write to in internal storage.");
            return false;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "The encryption algorithm must be valid.");
            return false;
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "The padding must be valid.");
            return false;
        } catch (InvalidKeyException e) {
            Log.e(TAG, "The key must be valid.");
            return false;
        }
    }

    public static String readFromInternalStorage(
            Context context,
            String filePath,
            Key decryptionKey,
            String encryptionAlg) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, FileNotFoundException, IOException {
        return readFromInternalStorage(
                FileUtil.getInternalStorageFile(context, filePath),
                decryptionKey,
                encryptionAlg);
    }

    public static String readFromInternalStorage(
            File file,
            Key decryptionKey,
            String encryptionAlg) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, FileNotFoundException, IOException {
        try {
            Cipher cipher = Cipher.getInstance(encryptionAlg);
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey);
            return FileUtil.readFromInputStream(new CipherInputStream(new FileInputStream(file), cipher));
        }
        catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "The encryption algorithm must be valid.");
            throw e;
        }
        catch (InvalidKeyException e) {
            Log.e(TAG, "The key must be valid.");
            throw e;
        }
        catch (NoSuchPaddingException e) {
            Log.e(TAG, "The padding must be valid.");
            throw e;
        }
        catch(FileNotFoundException e) {
            Log.e(TAG, "Could not find file to read from in internal storage.");
            throw e;
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading from internal storage.");
            throw e;
        }
    }
}
