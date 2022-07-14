package com.illis.javabtcommunicationserver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

public class BluetoothReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {

        Logger.d("BluetoothA2dp = " + intent.getAction());
        BluetoothController mBtController = BluetoothController.getInstance();

        if (intent.getAction() == BluetoothDevice.ACTION_BOND_STATE_CHANGED && mBtController != null) {
            mBtController.mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (mBtController.mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                //means device paired
                Logger.d("ACTION_BOND_STATE_CHANGED = BOND_BONDED");
                mBtController.WaitToSelectedDevice();
            } else {
                Logger.d("ACTION_BOND_STATE_CHANGED =" + mBtController.mDevice.getBondState());
            }
        } else if (intent.getAction() == BluetoothDevice.ACTION_ACL_CONNECTED && mBtController != null) {
            mBtController.mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (mBtController.mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                //means device paired
                Logger.d("ACTION_BOND_STATE_CHANGED = BOND_BONDED");
                mBtController.WaitToSelectedDevice();
            } else {
                Logger.d("ACTION_BOND_STATE_CHANGED =" + mBtController.mDevice.getBondState());
            }
        } else if (intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
            Logger.d("Bluetooth connect close");

        }
    }
}
