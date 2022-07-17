package com.illis.javabtcommunicationclient.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

//블루투스 상태 변화 BroadcastReceiver
public class BluetoothStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //BluetoothAdapter.EXTRA_STATE : 블루투스의 현재상태 변화
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

        if(state == BluetoothAdapter.STATE_ON)
            Logger.d("블루투스 활성화");
        else if(state == BluetoothAdapter.STATE_TURNING_ON)
            Logger.d("블루투스 활성화 중");
        else if(state == BluetoothAdapter.STATE_OFF)
            Logger.d("블루투스 비활성화");
        else if(state == BluetoothAdapter.STATE_TURNING_OFF)
            Logger.d("블루투스 비활성화중");
    }
}