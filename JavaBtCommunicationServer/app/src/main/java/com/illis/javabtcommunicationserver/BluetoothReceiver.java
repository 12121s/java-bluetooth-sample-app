package com.illis.javabtcommunicationserver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.illis.javabtcommunicationserver.util.LogUtil;

public class BluetoothReceiver extends BroadcastReceiver {

    private BluetoothController mBtController;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {

        LogUtil.d("BluetoothA2dp = " + intent.getAction());
        mBtController = BluetoothController.getInstance();

        if (intent.getAction() == BluetoothDevice.ACTION_BOND_STATE_CHANGED && mBtController != null) {
            mBtController.mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (mBtController.mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                //means device paired
                LogUtil.d("ACTION_BOND_STATE_CHANGED = BOND_BONDED");
                mBtController.WaitToSelectedDevice();
            } else {
                LogUtil.d("ACTION_BOND_STATE_CHANGED =" + mBtController.mDevice.getBondState());
            }
        } else if (intent.getAction() == BluetoothDevice.ACTION_ACL_CONNECTED && mBtController != null) {
            mBtController.mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (mBtController.mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                //means device paired
                LogUtil.d("ACTION_BOND_STATE_CHANGED = BOND_BONDED");
                mBtController.WaitToSelectedDevice();
            } else {
                LogUtil.d("ACTION_BOND_STATE_CHANGED =" + mBtController.mDevice.getBondState());
            }
        } else if (intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
            LogUtil.d("Bluetooth connect close");

        }
    }
}
