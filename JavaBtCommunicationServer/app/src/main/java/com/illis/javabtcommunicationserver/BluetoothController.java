package com.illis.javabtcommunicationserver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.illis.javabtcommunicationserver.util.LogUtil;
import com.illis.javabtcommunicationserver.util.OrderOfObjectsAfterGCMain;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothController {
    private static BluetoothController mController;

    public BluetoothDevice mDevice;
    Thread mWorkerThread;
    private int readBufferPosition;     //버퍼 내 수신 문자 저장 위치
    private byte[] readBuffer;          //수신 버퍼
    private byte mDelimiter = 10;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private BluetoothSocket mSocket;
    private BluetoothServerSocket tmp;

    //handler는 thread에서 던지는 메세지를 보고 다음 동작을 수행시킨다.
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == 1) // 연결 완료
            {
                Logger.d("BT 연결 성공");
                try {
                    LogUtil.logForCheckMemoryAddressAndHashCode("Connect Success.. Socket", mSocket);
                    LogUtil.logForCheckMemoryAddressAndHashCode("Connect Success & Closed BluetoothServerSocket", tmp);

                    //연결이 완료되면 소켓에서 outstream과 inputstream을 얻는다. 블루투스를 통해
                    //데이터를 주고 받는 통로가 된다.
                    mOutputStream = mSocket.getOutputStream();
                    mInputStream = mSocket.getInputStream();

                    LogUtil.logForCheckMemoryAddressAndHashCode("Connect Success.. InputStream", mInputStream);

                    beginListenForData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else { //연결 실패
                Logger.d("BT 연결 실패");
                try {
                    LogUtil.logForCheckMemoryAddressAndHashCode("Before close BluetoothSocket", mSocket);
                    if (mSocket != null) {
                        mSocket.close();
                    }
                    LogUtil.logForCheckMemoryAddressAndHashCode("After close BluetoothSocket", mSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    public static BluetoothController getInstance() {
        Logger.d(".getInstance() called.");
        if (mController == null) {
            mController = new BluetoothController();
        }

        return mController;
    }

    //블루투스 데이터 수신 Listener
    protected void beginListenForData() {
        Logger.d("beginListenForData called ");
        final Handler handler = new Handler();
        readBuffer = new byte[1024];  // 수신 버퍼
        readBufferPosition = 0;       // 버퍼 내 수신 문자 저장 위치
        // 문자열 수신 쓰레드
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while ((mSocket != null && mSocket.isConnected())) {
                    try {
                        Logger.d("Current work thread is : " + Thread.currentThread().getName());
                        if (mInputStream != null && (mSocket != null && mSocket.isConnected())) {
                            int bytesAvailable = mInputStream.available();
                            if (bytesAvailable > 0) { //데이터가 수신된 경우
                                LogUtil.logForCheckMemoryAddressAndHashCode("Connecting.. InputStream ", mInputStream);
                                LogUtil.logForCheckMemoryAddressAndHashCode("Connecting.. Socket", mSocket);
                                LogUtil.logForCheckMemoryAddressAndHashCode("Connecting.. BluetoothSererSocket tmp", tmp);

                                byte[] packetBytes = new byte[bytesAvailable];
                                mInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == mDelimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "UTF-8");
                                        readBufferPosition = 0;
                                        handler.post(new Runnable() {
                                            public void run() {
                                                //수신된 데이터
                                                Logger.d("Read data = " + data.toString());
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        } else {
                            Logger.d("InputStream is null & Bluetooth disconnected");
                            disconnectBt();
                            mWorkerThread.interrupt();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //데이터 수신 thread 시작
        mWorkerThread.start();
    }


    public void WaitToSelectedDevice() {
        Logger.d("WaitToSelectedDevice called ");
        //연결과정을 수행할 thread 생성
        // RFCOMM 채널을 통한 연결, socket에 connect하는데 시간이 걸린다. 따라서 ui에 영향을 주지 않기 위해서는
        // Thread로 연결 과정을 수행해야 한다.
        Thread thread = new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                Logger.d("Current work thread is : " + Thread.currentThread().getName());

                //선택된 기기의 이름을 갖는 bluetooth device의 object
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                Logger.d("Device bonded state =" + mDevice.getBondState());

                try {
                    // 소켓 생성
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    Logger.d("Bluetooth adapter = " + adapter);
                    Logger.d("BluetoothDevice address = " + mDevice.getAddress());

                    LogUtil.logForCheckMemoryAddressAndHashCode("Before Create BluetoothSererSocket tmp", tmp);
                    LogUtil.logForCheckMemoryAddressAndHashCode("Before Create Socket", mSocket);
                    tmp = adapter.listenUsingRfcommWithServiceRecord(mDevice.getAddress(), uuid);

                    LogUtil.logForCheckMemoryAddressAndHashCode("Listen Device Address BluetoothSererSocket tmp", tmp);
                    LogUtil.logForCheckMemoryAddressAndHashCode("Listen Device Address Socket", mSocket);

                    mSocket = tmp.accept(3000);

                    LogUtil.logForCheckMemoryAddressAndHashCode("After accept BluetoothSererSocket tmp", tmp);
                    LogUtil.logForCheckMemoryAddressAndHashCode("After accept Socket", mSocket);

                    if(mSocket != null) {
                        Logger.d("bluetooth socket accept 성공");
                        mHandler.sendEmptyMessage(1);
                    } else
                        Logger.e("bluetooth socket accept 실패 : mSocket is null");
                } catch (Exception e) {
                    // 블루투스 연결 중 오류 발생
                    Logger.e("error occurred: "+ e.getLocalizedMessage());
                    mHandler.sendEmptyMessage(-1);
                } finally {
                    Logger.d("finally");
                    try {
                        Logger.d("try close bluetoothServerSocket");
                        if (tmp != null) {
                            LogUtil.logForCheckMemoryAddressAndHashCode("Before BluetoothServerSocket tmp close", tmp);
                            LogUtil.logForCheckMemoryAddressAndHashCode("Before BluetoothServerSocket tmp close mSocket", mSocket);
                            tmp.close();
                            LogUtil.logForCheckMemoryAddressAndHashCode("After BluetoothServerSocket tmp close", tmp);
                            LogUtil.logForCheckMemoryAddressAndHashCode("After BluetoothServerSocket tmp close mSocket", mSocket);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.setUncaughtExceptionHandler((t, e) -> {
            Logger.e("[16] [Thread " + t + "]UncaughtException: " + e.getLocalizedMessage());
        });

        //연결 thread를 수행한다
        thread.start();
    }

    public void disconnectBt() {
        try {
            Logger.d("try close BluetoothSocket");

            LogUtil.logForCheckMemoryAddressAndHashCode("Before BluetoothSocket close tmp", tmp);
            LogUtil.logForCheckMemoryAddressAndHashCode("Before BluetoothSocket close mSocket", mSocket);
            if (mSocket != null) {
                Logger.d("mSocket close");
                mSocket.close();
            }
            if (mInputStream != null) {
                Logger.d("mInputStream close");
                mInputStream.close();
            }
            if (mOutputStream != null) {
                Logger.d("mOutputStream close");
                mOutputStream.close();
            }
            LogUtil.logForCheckMemoryAddressAndHashCode("After BluetoothSocket close tmp", tmp);
            LogUtil.logForCheckMemoryAddressAndHashCode("After BluetoothSocket close mSocket", mSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
