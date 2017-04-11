/*===============================================
* Program Name: simple bluetooth                *
*               code for android -	            *
* 				Main Activity                   *
* -----------------------------------------------
* Version : 1.0 							    *
* -----------------------------------------------
* Author : Rey John Lim                         *
* -----------------------------------------------
* Description: search bluetooth device for
*              and connect to a device mainly
*              bluetooth module such as hc-05
* =============================================**/

package com.example.pc01.simple_bt_app;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
//

public class MainActivity extends AppCompatActivity {


    private InitUi btUI;
    static Handler btHandler;                           // handler for receiving message from bluetooth device
    private static final int RECIEVE_MESSAGE = 1;
    private static final UUID my_Bt_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter blue_Adapter;              // Bluetooth adapter for the device
    private ArrayAdapter<String> pair_Device_Adapter;   // array adapter for paired devices
    private BroadcastReceiver blue_State;               // broadcast receiver for status of the bluetooth in the device
    private BluetoothSocket blueSocket;                 // Socket for connecting bluetooth
    private BlueThread bt_connect_thread;               //  thread class (see BlueThread.java)

    private StringBuilder sb = new StringBuilder();

    private String bt_mac_Addr;                         // MAC address of blue tooth
    private String logTag = "RJLbt";                    // log name for debugging

    /*===========================================================
    ||  I N I T I A L I Z E    T H E     A C T I V I T Y        ||
     *==========================================================*/
    // initialize some of the
    @SuppressLint("HandlerLeak")
    private void init_activity() {

        View viewbt = findViewById(R.id.bt_container);          // view layout for bluetooth connection
        View viewCtrl = findViewById(R.id.ctrl_container);      // view layout for sending command visa bluetooth

        btUI = new InitUi();                                    // Initialize User Interface
        btUI.initializeUserInterface(viewbt, viewCtrl);         // Add view to the User Interface

        blue_Adapter = BluetoothAdapter.getDefaultAdapter();    // set Bluetooth adapter

        bt_support_check();                                     // Check if the phone/table is capable of supporting
                                                                // bluetooth connectivity

        // set array adapter containing list of all paired bluetooth device
        pair_Device_Adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, 0);
        // add something just to be sure that spinner works propery
        pair_Device_Adapter.add("sample 1");
        pair_Device_Adapter.add("sample 2");
        pair_Device_Adapter.add("sample 3");

        // add array adapter to the spinner
        btUI.spin_pairedDevice.setAdapter(pair_Device_Adapter);


        // broadcast the state of the bluetooth device of the tablet/phone
        blue_State = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int btCurrentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                //  int prevBtState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);

                //check state of the bluetooth and display a message of the current state of the bluetooth
                switch (btCurrentState) {
                    case (BluetoothAdapter.STATE_TURNING_ON): {
                        // Toast.makeText(getBaseContext(), "Bluetooth is turning ON", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case (BluetoothAdapter.STATE_ON): {
                        Toast.makeText(getBaseContext(), "Bluetooth is ON", Toast.LENGTH_SHORT).show();
                        get_bt_devices();
                        break;
                    }
                    case (BluetoothAdapter.STATE_TURNING_OFF): {
                        // Toast.makeText(getBaseContext(), "Bluetooth is turning OFF", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case (BluetoothAdapter.STATE_OFF): {
                        Toast.makeText(getBaseContext(), "Bluetooth is OFF", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case (BluetoothAdapter.STATE_CONNECTED): {
                        Toast.makeText(getBaseContext(), "Bluetooth is Connected ", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case (BluetoothAdapter.STATE_DISCONNECTED): {
                        Toast.makeText(getBaseContext(), "Bluetooth Disconnected ", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };

        // bluetooth handler for receiving message from bluetooth
        btHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                             // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);           // create string from bytes array
                        sb.append(strIncom);                                          // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                      // determine the end-of-line
                        if (endOfLineIndex > 0) {                                     // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);         // extract string

                            try {
                                // StringBreak(sbprint);
                                Log.i(logTag, sbprint);
                            } catch (NumberFormatException nfe) {
                                Toast.makeText(getBaseContext(), "Bluetooth Data Error", Toast.LENGTH_SHORT).show();
                            }
                            sb.delete(0, sb.length());                                        // and clear
                        }
                        //  Log.i(logTag, "...String:"+ sb.toString() );
                        break;
                }
            }
        };

    }

    /*===========================================================
    ||     B L U E T O O T H   S U P P O R T   C H E C K        ||
    *==========================================================*/
    // check if the device has bluetooth support
    private void bt_support_check() {
        if (blue_Adapter == null) {
            Toast.makeText(getBaseContext(), "This device does not support Bluetooth", Toast.LENGTH_LONG).show();
            btUI.sw_EnBt.setClickable(false);
        } else {
            btUI.sw_EnBt.setClickable(true);
            if (!blue_Adapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth is OFF", Toast.LENGTH_LONG).show();
            } else {
                btUI.sw_EnBt.setChecked(true);
                Toast.makeText(getBaseContext(), "Bluetooth is ON", Toast.LENGTH_LONG).show();
                get_bt_devices();
            }
        }
    }

    /*=======================================================================
    ||           G E T    P A I R E D     D E V I C E S                     ||
    *========================================================================*/
    // check if the device has bluetooth support if yes the get all the paired devices
    private void get_bt_devices() {
        pair_Device_Adapter.clear();
        Set<BluetoothDevice> setBtDevice = blue_Adapter.getBondedDevices();
        if (setBtDevice.size() > 0) {
            for (BluetoothDevice btxDevice : setBtDevice) {
                pair_Device_Adapter.add(btxDevice.getName() + "   " + btxDevice.getAddress());
            }
        }
    }

    /*======================================================================//
    ||           C R E A T E    B L U E T O O T H    S O C K E T            ||
    =======================================================================*/
    private BluetoothSocket create_bt_socket(BluetoothDevice btDevice) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = btDevice.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
                return (BluetoothSocket) m.invoke(btDevice, my_Bt_UUID);
            } catch (Exception e) {
                Log.e(logTag, "Could not create Insecure RFComm Connection",e);
            }
        }
        return btDevice.createRfcommSocketToServiceRecord(my_Bt_UUID);
    }

    /* ======================================================================//
    ||           C O N N E C T    B L U E T O O T H    D E V I C E           ||
    =========================================================================*/
    // connect the bluetooth to the selected paired device
    private void connect_bt_device() {

        //create a bluetooth device using the selected mac address;
        BluetoothDevice btDeviceConnect = blue_Adapter.getRemoteDevice(bt_mac_Addr);
        // try to connect device to socket if it is available
        try {
            blueSocket = create_bt_socket(btDeviceConnect);
        } catch (IOException ebt) {
            // if cannot connect to the said device display a message
            Toast.makeText(getBaseContext(), "Cannot connect to device", Toast.LENGTH_LONG).show();
            //Log.i(logTag, "Something Wrong here - connect remote");
        }

        // disable discovery, it is resource intensive task
        blue_Adapter.cancelDiscovery();

        //display in log cat when trying to establish a connection
        //Log.i(logTag, "Establishing Connection....");
        Toast.makeText(getBaseContext(), "Establishing Connection....", Toast.LENGTH_LONG).show();

        try {
            blueSocket.connect();
           // Log.i(logTag, "Connected...");
            Toast.makeText(getBaseContext(), "Connected", Toast.LENGTH_LONG).show();
        } catch (IOException Esocket) {
            try {
                blueSocket.close();
            } catch (IOException e) {
                Log.i(logTag, "FATAL ERROR: unable to close socket during " +
                              "connection failure - connecte_bt_device() method " + e.getMessage());
            }
        }
        // Initialize Bluetooth Thread
        bt_connect_thread = new BlueThread(blueSocket);
        bt_connect_thread.start();                             //start bluetooth Thread
    }
    /* ======================================================================//
    ||              O N    C R E A T E    M E T H O D                        ||
    =========================================================================*/

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize User Interface
        init_activity();

        //  button that will turn off/on the bluetooth of the phone/tablet
        btUI.sw_EnBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // get the latest paired devices
                    get_bt_devices();
                    // Intent filter for checking the state of bluetooth
                    IntentFilter btStateChange = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

                    // string for enabling the bluetooth
                    String eN_btIntent = BluetoothAdapter.ACTION_REQUEST_ENABLE;
                    registerReceiver(blue_State, btStateChange);
                    startActivityForResult(new Intent(eN_btIntent), 0);
                }
                if (!b && blue_Adapter.isEnabled()) {
                    blue_Adapter.disable();
                }
            }
        });

        // after the bluetooth is turned on select for the paired devices
        btUI.spin_pairedDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                String[] MAC_addr;  // create temporary string array for extracting MAC address

                // check if the bluetooth is enabled
                if (blue_Adapter.getState() == BluetoothAdapter.STATE_ON) {

                    // refresh the list of paired device
                    get_bt_devices();

                    // check if the list contains a 3 space
                    if (pair_Device_Adapter.getItem(pos).contains("   ")) {

                        // split the contents into two part and store it in a array
                        MAC_addr = pair_Device_Adapter.getItem(pos).split("   ");

                        // get the second element of the array which contain the mac address of the bluetooth
                        bt_mac_Addr = MAC_addr[1];

                        // display the mac address of the selected device
                        btUI.txV_mac_addr.setText(bt_mac_Addr);
                        // for logcat debugging
                        //Log.i(logTag, "Selected Bt Address ->> " + bt_mac_Addr);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // button to connect to the bluetooth device
        btUI.sw_ConBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (blue_Adapter.getState() == BluetoothAdapter.STATE_ON) {
                        btUI.sw_ConBt.setText("Bluetooth Connected");
                        connect_bt_device();
                        //enableControls();
                    }
                    // connect to the remote bluetooth device
                } else {
                    btUI.sw_ConBt.setText("Bluetooth Disconnected");
                    if (blue_Adapter.getState() == BluetoothAdapter.STATE_OFF) {
                        // disableControls();
                        bt_connect_thread.interrupt();
                    }
                }
            }
        });

        // send On message to bluetooth
        btUI.btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blue_Adapter.getState() == BluetoothAdapter.STATE_ON) {
                    bt_connect_thread.SendData("a");
                }

            }
        });

        // send Off message to bluetooth
        btUI.btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  bt_connect_thread.write("b");
                if (blue_Adapter.getState() == BluetoothAdapter.STATE_ON) {
                    bt_connect_thread.SendData("b");
                }
            }
        });


    }

    // disable bluetooth upon exiting the application
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (blue_Adapter.getState() == BluetoothAdapter.STATE_ON)
        {
            blue_Adapter.disable();
        }
    }
}
