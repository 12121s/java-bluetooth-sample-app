package com.illis.javabtcommunicationclient;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

//블루투스 검색 가능 확인 BroadcastReceiver
public class BluetoothScanmodeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
        switch (state){
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
            case BluetoothAdapter.SCAN_MODE_NONE:
                Logger.d("검색응답 모드 종료");
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                Logger.d("다른 블루투스 기기에서 내 휴대폰을 찾을 수 있습니다.");
                break;
        }
    }
}
