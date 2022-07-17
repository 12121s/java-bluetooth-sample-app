package com.illis.javabtcommunicationclient.receiver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.illis.javabtcommunicationclient.BluetoothController;
import com.orhanobut.logger.Logger;

//블루투스 검색 BroadcastReceiver
public class BluetoothSearchReceiver extends BroadcastReceiver {
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logger.d("bluetooth action receive: " + action);
        switch(action){
            //블루투스 디바이스 검색 시작
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                Logger.d("블루투스 검색 시작");
                BluetoothController.getInstance().mDataDevice.clear();
                // todo 블루투스 검색 로딩 팝업
                break;
            //블루투스 디바이스 찾음
            case BluetoothDevice.ACTION_FOUND:
                //검색한 블루투스 디바이스의 객체
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Logger.d("블루투스 기기 발견\n" +
                        " name: " + device.getName() +
                        " address:" + device.getAddress() +
                        " uuid:" + (device.getUuids() != null && device.getUuids().length > 0? device.getUuids()[0] : "") +
                        " bt class:" + device.getBluetoothClass());
                if (!BluetoothController.getInstance().mDataDevice.containsKey(device.getAddress()))
                    BluetoothController.getInstance().mDataDevice.put(device.getAddress(), device);
                break;
            //블루투스 디바이스 검색 종료
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                Logger.d("블루투스 검색 종료 count = " + BluetoothController.getInstance().mDataDevice.size());

                // todo 검색 완료 후 검색된 장치가 있으면 리스트 보여주기 vs 없으면 재시도 안내 팝업
                break;
            //블루투스 디바이스 페어링 상태 변화
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                BluetoothDevice paired = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(paired != null && paired.getBondState()==BluetoothDevice.BOND_BONDED){
                    Logger.d("블루투스 BONDED" + paired.getName());

                    if (BluetoothController.getInstance().mHandler != null) {
                        BluetoothController.getInstance().mHandler.sendEmptyMessage(1);
                    }
                }
                break;
        }
    }
}
