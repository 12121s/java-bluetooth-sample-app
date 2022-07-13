package com.illis.javabtcommunicationserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.core.app.ActivityCompat;

import com.illis.javabtcommunicationserver.util.LogUtil;
import com.illis.javabtcommunicationserver.util.OrderOfObjectsAfterGCMain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class BluetoothController {
    private static BluetoothController mController;

    public BluetoothDevice mDevice;
    Thread mWorkerThread;
    private int readBufferPositon;      //버퍼 내 수신 문자 저장 위치
    private byte[] readBuffer;      //수신 버퍼
    private byte mDelimiter = 10;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private BluetoothSocket mSocket;
    private BluetoothServerSocket tmp;

    public static BluetoothController getInstance() {
        LogUtil.d(".getInstance() called.");
        if (mController == null) {
            mController = new BluetoothController();
        }

        return mController;
    }

    //블루투스 데이터 수신 Listener
    protected void beginListenForData() {
        final Handler handler = new Handler();
        readBuffer = new byte[1024];  //  수신 버퍼
        readBufferPositon = 0;        //   버퍼 내 수신 문자 저장 위치
        LogUtil.d("beginListenForData called ");
        // 문자열 수신 쓰레드
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d("workthread run");
                while ((mSocket != null && mSocket.isConnected())) {
                    try {
                        if (mInputStream != null && (mSocket != null && mSocket.isConnected())) {
                            int bytesAvailable = mInputStream.available();
                            if (bytesAvailable > 0) { //데이터가 수신된 경우
                                LogUtil.d("[17] current work thread name: " + Thread.currentThread().getName() + " & hashcode: " + Thread.currentThread().hashCode());


                                OrderOfObjectsAfterGCMain.printAddresses("[25] (close 이후..) 리모콘 앱 연결중 server socket =", tmp);

                                LogUtil.d("[18] beginListenForData mInputStream = " + mInputStream);
                                LogUtil.d("[19] beginListenForData mInputStream hashcode = " + mInputStream.hashCode());

                                OrderOfObjectsAfterGCMain.printAddresses("[26] 리모콘 앱 연결중 bt socket = ", mSocket);
                                OrderOfObjectsAfterGCMain.printAddresses("[27] 리모콘 앱 연결중 inputstream = ", mInputStream);

                                byte[] packetBytes = new byte[bytesAvailable];
                                mInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == mDelimiter) {
                                        byte[] encodedBytes = new byte[readBufferPositon];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "UTF-8");
                                        readBufferPositon = 0;
                                        handler.post(new Runnable() {
                                            public void run() {
                                                //수신된 데이터는 data 변수에 string으로 저장!! 이 데이터를 이용하면 된다.
                                                LogUtil.d("beginListenForData = " + data.toString());
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPositon++] = b;
                                    }
                                }
                            }
                        } else {
                            LogUtil.d("[20] inputstream is null");
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

    //handler는 thread에서 던지는 메세지를 보고 다음 동작을 수행시킨다.
    final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == 1) // 연결 완료
            {
                LogUtil.d("BT 연결 성공");
                try {
                    OrderOfObjectsAfterGCMain.printAddresses("[23] server socket close 후?", tmp);

                    LogUtil.d("WaitToSelectedDevice mSocket = " + mSocket);
                    LogUtil.d("WaitToSelectedDevice mSocket hashcode = " + mSocket.hashCode());

                    OrderOfObjectsAfterGCMain.printAddresses("[24] bluetooth socket 연결 완료", mSocket);

                    //연결이 완료되면 소켓에서 outstream과 inputstream을 얻는다. 블루투스를 통해
                    //데이터를 주고 받는 통로가 된다.
                    mOutputStream = mSocket.getOutputStream();
                    mInputStream = mSocket.getInputStream();

                    LogUtil.d("WaitToSelectedDevice mInputStream = " + mInputStream);
                    LogUtil.d("WaitToSelectedDevice mInputStream hashcode = " + mInputStream.hashCode());

                    beginListenForData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {    //연결 실패
                LogUtil.d("BT 연결 실패");
                try {
                    OrderOfObjectsAfterGCMain.printAddresses("bluetooth socket close 전", mSocket);

                    if (mSocket != null)
                        mSocket.close();

                    OrderOfObjectsAfterGCMain.printAddresses("bluetooth socket close 후", mSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void WaitToSelectedDevice() {
        //연결과정을 수행할 thread 생성
        Thread thread = new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                LogUtil.d("[0] current thread name: " + Thread.currentThread().getName() + " & hashcode: " + Thread.currentThread().hashCode());

                //선택된 기기의 이름을 갖는 bluetooth device의 object
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//                UUID uuid = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb");
                LogUtil.d("[1] WaitToSelectedDevice =" + mDevice.getBondState());
/*
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    for (ParcelUuid check_uuid : mDevice.getUuids())
                        LogUtil.d("device uuid: " + check_uuid);
                }
*/
                try {
                    LogUtil.d("[2] WaitToSelectedDevice bluetooth server socket connect 시작");

                    // 소켓 생성
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

                    LogUtil.d("[3] WaitToSelectedDevice bluetooth adapter = " + adapter);
                    LogUtil.d("[4] WaitToSelectedDevice bluetooth address = " + mDevice.getAddress());

                    LogUtil.d("[5_0] null일 때만 할당하자 = " + tmp);

                    OrderOfObjectsAfterGCMain.printAddresses("server socket listen 전", tmp);

                    LogUtil.d("[5_1] WaitToSelectedDevice bluetooth server socket 할당");
                    tmp = adapter.listenUsingRfcommWithServiceRecord(mDevice.getAddress(), uuid);

                    String test = "test";
                    String adb = test;
                    OrderOfObjectsAfterGCMain.printAddresses(test);
                    LogUtil.d("[5_2] test = " + test);
                    LogUtil.d("[6] test hashcode = " + test.hashCode());

                    OrderOfObjectsAfterGCMain.printAddresses(adb);
                    LogUtil.d("[5_2] adb = " + adb);
                    LogUtil.d("[6] adb hashcode = " + adb.hashCode());

                    adb = "adb";
                    LogUtil.d("[5_2] test = " + test);
                    LogUtil.d("[6] test hashcode = " + test.hashCode());
                    LogUtil.d("[5_2] adb = " + adb);
                    LogUtil.d("[6] adb hashcode = " + adb.hashCode());

                    OrderOfObjectsAfterGCMain.printAddresses(test, tmp);
                    System.out.println(test);
                    LogUtil.d("[5_2] WaitToSelectedDevice bluetooth server socket = " + tmp);
                    LogUtil.d("[6] WaitToSelectedDevice bluetooth server socket hashcode = " + tmp.hashCode());

                    OrderOfObjectsAfterGCMain.printAddresses("server socket listen 후 bt socket: ", mSocket);
                    mSocket = tmp.accept(3000);
                    // RFCOMM 채널을 통한 연결, socket에 connect하는데 시간이 걸린다. 따라서 ui에 영향을 주지 않기 위해서는
                    // Thread로 연결 과정을 수행해야 한다.

                    OrderOfObjectsAfterGCMain.printAddresses("server socket accept 후", tmp);
                    OrderOfObjectsAfterGCMain.printAddresses("server socket accept 후 bt socket", mSocket);
                    if(mSocket != null) {
                        OrderOfObjectsAfterGCMain.printAddresses("bluetooth socket accept 성공", mSocket);
                        LogUtil.d("[7] WaitToSelectedDevice mSocket = " + mSocket);
                        LogUtil.d("[8] WaitToSelectedDevice mSocket hashcode = " + mSocket.hashCode());
                        mHandler.sendEmptyMessage(1);
                    } else
                        LogUtil.e("[9] WaitToSelectedDevice = mSocket is null");
                } catch (IOException e) {
                    LogUtil.e("[10] WaitToSelectedDevice =" +  e.getLocalizedMessage());
                    // 블루투스 연결 중 오류 발생
                    mHandler.sendEmptyMessage(-1);
                } catch (Exception e) {
                    LogUtil.e("[11] WaitToSelectedDevice =" +  e.getLocalizedMessage());
                    // 블루투스 연결 중 오류 발생
                    mHandler.sendEmptyMessage(-1);
                } finally {
                    LogUtil.d("finally");
                }

                try {
                    LogUtil.d("[12] close bluetooth server socket = " + tmp);
                    if (tmp != null) {
                        LogUtil.d("[13] close bluetooth server socket hashcode = " + tmp.hashCode());

                        OrderOfObjectsAfterGCMain.printAddresses("[14] server socket close 전", tmp);

                        tmp.close();

                        OrderOfObjectsAfterGCMain.printAddresses("[15] server socket close 후", tmp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        LogUtil.e("[16] [Thread " + t + "]UncaughtException: " + e.getLocalizedMessage());
                    }
                }
        );

        //연결 thread를 수행한다
        thread.start();
    }

    public void disconnectBt() {
        try {
            LogUtil.d("disconnectBluetooth");
            if (mSocket != null) {
                LogUtil.d("mSocket close");
                mSocket.close();
            }
            if (mInputStream != null) {
                LogUtil.d("mInputStream close");
                mInputStream.close();
            }
            if (mOutputStream != null) {
                LogUtil.d("mOutputStream close");
                mOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
