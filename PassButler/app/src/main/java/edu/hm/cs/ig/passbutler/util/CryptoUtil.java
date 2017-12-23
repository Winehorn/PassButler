package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.util.Log;

import com.google.common.primitives.Longs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
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

    public static String encryptToString(String data, Key encryptionKey, String encryptionAlg) {
        return StringUtil.fromBase64(encryptToBytes(data.getBytes(), encryptionKey, encryptionAlg));
    }

    public static String encryptToString(long data, Key encryptionKey, String encryptionAlg) {
        return StringUtil.fromBase64(encryptToBytes(Longs.toByteArray(data), encryptionKey, encryptionAlg));
    }

    public static byte[] encryptToBytes(String data, Key encryptionKey, String encryptionAlg) {
        return encryptToBytes(data.getBytes(), encryptionKey, encryptionAlg);
    }

    public static byte[] encryptToBytes(long data, Key encryptionKey, String encryptionAlg) {
        return encryptToBytes(Longs.toByteArray(data), encryptionKey, encryptionAlg);
    }

    public static byte[] encryptToBytes(byte[] data, Key encryptionKey, String encryptionAlg) {
        return useCipher(data, encryptionKey, encryptionAlg, Cipher.ENCRYPT_MODE);
    }

    public static String decryptToString(String data, Key decryptionKey, String decryptionAlg) {
        byte[] decryptedBytes = decryptToBytes(data, decryptionKey, decryptionAlg);
        String decryptedString = new String(decryptedBytes);
        ArrayUtil.clear(decryptedBytes);
        return decryptedString;
    }

    public static char[] decryptToChars(String data, Key decryptionKey, String decryptionAlg) {
        byte[] decryptedBytes = decryptToBytes(data, decryptionKey, decryptionAlg);
        char[] decryptedChars = ArrayUtil.toAsciiChars(decryptedBytes);
        ArrayUtil.clear(decryptedBytes);
        return decryptedChars;
    }

    public static long decryptToLong(String data, Key decryptionKey, String decryptionAlg) {
        byte[] decryptedBytes = decryptToBytes(data, decryptionKey, decryptionAlg);
        long decryptedLong = Longs.fromByteArray(decryptedBytes);
        ArrayUtil.clear(decryptedBytes);
        return decryptedLong;
    }

    public static byte[] decryptToBytes(String data, Key decryptionKey, String decryptionAlg) {
        byte[] decodedData = StringUtil.toBase64(data);
        return decryptToBytes(decodedData, decryptionKey, decryptionAlg);
    }

    public static byte[] decryptToBytes(long data, Key decryptionKey, String decryptionAlg) {
        return decryptToBytes(Longs.toByteArray(data), decryptionKey, decryptionAlg);
    }

    public static byte[] decryptToBytes(byte[] data, Key decryptionKey, String decryptionAlg) {
        return useCipher(data, decryptionKey, decryptionAlg, Cipher.DECRYPT_MODE);
    }

    private static byte[] useCipher(byte[] data, Key key, String alg, int mode) {
        try {
            Cipher cipher = Cipher.getInstance(alg);
            cipher.init(mode, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "The encryption algorithm must be valid.");
            return null;
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "The padding must be valid.");
            return null;
        } catch (InvalidKeyException e) {
            Log.e(TAG, "The key must be valid.");
            return null;
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "The block size must be valid.");
            return null;
        } catch (BadPaddingException e) {
            Log.e(TAG, "The padding must not be bad.");
            return null;
        }
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
