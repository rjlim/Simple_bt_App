/*===============================================
* Program Name: simple bluetooth                *
*               code for android -	            *
* 				Bluetooth Thread                *
* -----------------------------------------------
* Version : 1.0 							    *
* -----------------------------------------------
* Author : Rey John Lim                         *
* -----------------------------------------------
* Description: Thread that read incoming       *
*              data from a bluetooth device     *
* =============================================**/

package com.example.pc01.simple_bt_app;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.example.pc01.simple_bt_app.MainActivity.btHandler;

/**
 * Created by pc01 on 5/10/2016.
 */
public class BlueThread extends Thread{
    private InputStream btInputStream;
    private OutputStream btOutputStream ;

    private byte[] buffData = new byte[256];  // buffer store for the stream
    private int bytes; // bytes returned from read()

    public BlueThread(BluetoothSocket btSocket) {

        try {
            btInputStream = btSocket.getInputStream();
            btOutputStream = btSocket.getOutputStream();
        } catch (IOException iOe) {
            btInputStream = null;
            btOutputStream = null;
        }
    }

    public void run() {

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = btInputStream.read(buffData);        // Get number of bytes and message in "buffer"
                btHandler.obtainMessage(1, bytes, -1, buffData).sendToTarget();		// Send to message queue Handler
            } catch (IOException e) {
                break;
            }
        }
    }


    /* Call this from the main activity to send data to the remote device */
    public void SendData(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            btOutputStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d(" ", "...Error data send: " + e.getMessage() + "...");
        }
    }
}
