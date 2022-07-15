package com.illis.javabtcommunicationclient;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;

public class BluetoothController {
    
    private static BluetoothController instance;
    
    private BluetoothAdapter mBluetoothAdapter;
    //list - Device 목록 저장
    public HashMap<String, BluetoothDevice> mDataDevice;
    public Set<BluetoothDevice> mDevices;

    /* Bluetooth connection */
    BluetoothSocket mSocket;
    InputStream mInputStream;
    OutputStream mOutputStream;
    BluetoothDevice mRemoteDevice;

    public static BluetoothController getInstance() {
        if (instance == null) {
            instance = new BluetoothController();
        }
        return instance;
    }

    public BluetoothController() {
        //블루투스 지원 유무 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //블루투스를 지원하지 않으면 리턴
        if(mBluetoothAdapter == null || mBluetoothAdapter.isEnabled()){
            return;
        }
        getListPairedDevice();
    }

    //handler는 thread에서 던지는 메세지를 보고 다음 동작을 수행시킨다.
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == 1) // 연결 완료
            {
                try {
                    if (mSocket.isConnected()) { // 진짜 연결이 완료된 상태인지 한번 더 체크
                        mOutputStream = mSocket.getOutputStream();
                        mInputStream = mSocket.getInputStream();

                        Logger.d("connectedDevice " + mSocket.getRemoteDevice().getAddress());
                        Logger.d("connectedDevice " + mSocket.isConnected());
                        mHandler = null; // 초기화
                        // todo 연결 완료 동작
                    } else {
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(-1);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(-1);
                    }
                }
            } else {    //연결 실패
                try {
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    @SuppressLint("MissingPermission")
    public void getListPairedDevice() {
        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();

        mDataDevice = new HashMap<>();

        // todo 기연결 디바이스 정보 가져오기
        String strPairingDevice = "";

        String strBluetoothMac = "";
        if (strPairingDevice != null && strPairingDevice.length() > 0) {
            try {
                JSONObject obj = new JSONObject(strPairingDevice);
                strBluetoothMac = obj.getString("mBluetoothMac");
                Logger.d("기연결 정보 " + strBluetoothMac);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 블루투스 기기의 mac Address key로 이용하여 기연결 셋톱의 mac Address와 비교하기 위해 hashmap에 pairedDevice를 담는 과정
        for(BluetoothDevice device : pairedDevice){
            if (!mDataDevice.containsKey(device.getAddress()))
            {
                mDataDevice.put(device.getAddress(), device);
                // pairedDevice 리스트 중에 기연결 셋톱의 macAddress와 동일한 Address를 가진 기기가
                // 하나라도 존재하면 바로 연결할 수 있도록 리스트 초기화하고 해당 기기만 담음
                if (device.getAddress().equalsIgnoreCase(strBluetoothMac)) {
                    mDataDevice.clear();
                    mDataDevice.put(device.getAddress(), device);
                    break;
                }
            }
        }

        if (mDataDevice.size() == 1 && strPairingDevice != null && strPairingDevice.length() > 0) {

            final BluetoothDevice sPairedDevice = mDataDevice.get(strBluetoothMac); // 기연결 디바이스 connect

            if (sPairedDevice != null) {
                // todo 기 연결 디바이스 하나이므로 바로 연결
            } else {
                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }
                mBluetoothAdapter.startDiscovery();
            }
        } else {
            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
        }
    }
}
