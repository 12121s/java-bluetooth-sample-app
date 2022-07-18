package com.illis.javabtcommunicationserver.receiver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.illis.javabtcommunicationserver.BluetoothController;
import com.orhanobut.logger.Logger;

public class BluetoothStateReceiver extends BroadcastReceiver {
    private BluetoothController mBtController;
    
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("BluetoothA2dp = " + intent.getAction());
        mBtController = BluetoothController.getInstance();

        // paired
        if(intent.getAction() == BluetoothDevice.ACTION_BOND_STATE_CHANGED && mBtController != null) {
            mBtController.mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Logger.d("ACTION_BOND_STATE_CHANGED =" + mBtController.mDevice.getBondState());

            if (mBtController.mDevice.getBondState() == BluetoothDevice.BOND_BONDED)
                mBtController.WaitToSelectedDevice();
        }
        // connected
        else if(intent.getAction() == BluetoothDevice.ACTION_ACL_CONNECTED && mBtController != null) {
            mBtController.mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Logger.d("ACTION_BOND_STATE_CHANGED =" + mBtController.mDevice.getBondState());

            if (mBtController.mDevice.getBondState() == BluetoothDevice.BOND_BONDED)
                mBtController.WaitToSelectedDevice();
        }
        // disconnected
        else if (intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
            mBtController.mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Logger.d("ACTION_BOND_STATE_CHANGED =" + mBtController.mDevice.getBondState());

            if (mBtController.mDevice.getBondState() == BluetoothDevice.BOND_BONDED)
                mBtController.disconnectBt();
        }
    }
}
