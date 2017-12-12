package edu.hm.cs.ig.passbutler.sync;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.util.ArrayUtil;
import edu.hm.cs.ig.passbutler.util.FileUtil;

/**
 * Created by dennis on 07.12.17.
 */

public class BluetoothConnection implements Closeable {

    public static final String TAG = BluetoothConnection.class.getName();
    private Context context;
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;

    public BluetoothConnection(
            Context context,
            BluetoothDevice bluetoothDevice,
            UUID bluetoothChannelUuid) throws IOException {
        this(context, bluetoothDevice.createRfcommSocketToServiceRecord(bluetoothChannelUuid));
        try {
            socket.connect();
        } catch (IOException e) {
            Log.e(TAG, "I/O error while connecting Bluetooth socket to remote device.");
            throw e;
        }
    }

    public BluetoothConnection(Context context, BluetoothSocket socket) throws IOException {
        Log.i(TAG, "Creating Bluetooth connection.");
        this.context= context;
        this.socket = socket;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "I/O error while getting input and output streams from Bluetooth socket.");
            throw e;
        }
        bufferedReader = new BufferedReader(new InputStreamReader(in));
        printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
    }

    @Override
    public void close() throws IOException {
        Log.i(TAG, "Closing Bluetooth connection.");
        socket.close();
    }

    public int readInt() throws IOException, NumberFormatException {
        try {
            Log.i(TAG, "Reading integer from Bluetooth connection.");
            String s = bufferedReader.readLine();
            return Integer.parseInt(s);
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading integer from input stream.");
            throw e;
        }
        catch(NumberFormatException e) {
            Log.e(TAG, "Could not parse value from input stream to integer.");
            throw e;
        }
    }

    public long readLong() throws IOException, NumberFormatException {
        try {
            Log.i(TAG, "Reading long from Bluetooth connection.");
            String s = bufferedReader.readLine();
            return Long.parseLong(s);
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading long from input stream.");
            throw e;
        }
        catch(NumberFormatException e) {
            Log.e(TAG, "Could not parse value from input stream to long.");
            throw e;
        }
    }

    public boolean readBoolean() throws IOException {
        try {
            Log.i(TAG, "Reading boolean from Bluetooth connection.");
            String s = bufferedReader.readLine();
            return Boolean.parseBoolean(s);
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading boolean from input stream.");
            throw e;
        }
    }

    public String readString() throws IOException {
        try {
            Log.i(TAG, "Reading string from Bluetooth connection.");
            return bufferedReader.readLine();
        }
        catch(IOException e) {
            Log.e(TAG, "I/O error while reading string from input stream.");
            throw e;
        }
    }

    public byte[] readFileContent() throws IOException {
        Log.i(TAG, "Reading file content from Bluetooth connection.");
        String s = readString();
        byte[] b = ArrayUtil.reverseToString(s);
        return b;
    }

    public void writeInt(int i) {
        Log.i(TAG, "Writing integer to Bluetooth connection.");
        printWriter.println(i);
        printWriter.flush();
    }

    public void writeLong(long l) {
        Log.i(TAG, "Writing long to Bluetooth connection.");
        printWriter.println(l);
        printWriter.flush();
    }

    public void writeBoolean(boolean b) {
        Log.i(TAG, "Writing boolean to Bluetooth connection.");
        printWriter.println(b);
        printWriter.flush();
    }

    public void writeString(String s) {
        Log.i(TAG, "Writing string to Bluetooth connection.");
        printWriter.println(s);
        printWriter.flush();
    }

    public void writeFile(File f) throws FileNotFoundException, IOException {
        Log.i(TAG, "Writing file content to Bluetooth connection.");
        byte[] b = IOUtils.toByteArray(new FileInputStream(f));
        String s = Arrays.toString(b);
        writeString(s);
    }
}
