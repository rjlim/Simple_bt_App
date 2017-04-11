/*===============================================
* Program Name: simple bluetooth                *
*               code for android -	            *
* 				Initialize User Interface       *
* -----------------------------------------------
* Version : 1.0 							    *
* -----------------------------------------------
* Author : Rey John Lim                         *
* -----------------------------------------------
* Description: initializing class for main
*              activity
* =============================================**/



package com.example.pc01.simple_bt_app;

import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by pc01 on 5/10/2016.
 */
// class for initializing user Interface
public class InitUi {


    Switch sw_EnBt, sw_ConBt;           // switch for turning on/off bluetooth and connecting to the bluetooth device
    Spinner spin_pairedDevice;          // spinner that contain all the list of paired devices
    Button  btnOn, btnOff;              // buttons to send a data to the bluetooth device
    TextView txV_mac_addr;              // display the MAC address of the selected device in the spinner


    //
    public void initializeUserInterface(View vnBt, View vnCtrl)
    {
        sw_EnBt = (Switch) vnBt.findViewById(R.id.sw_en_bt);
        sw_ConBt = (Switch) vnBt.findViewById(R.id.sw_cn_bt);
        txV_mac_addr = (TextView) vnBt.findViewById(R.id.txv_mac);
        spin_pairedDevice = (Spinner) vnBt.findViewById(R.id.spn_pairedDev);
        btnOn = (Button) vnCtrl.findViewById(R.id.btn_On);
        btnOff = (Button) vnCtrl.findViewById(R.id.btn_Off);
    }
}
