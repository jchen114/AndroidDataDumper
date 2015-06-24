package com.example.hooligan.connectivitydatadumper;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.hooligan.DataToFileWriter;
import com.example.hooligan.SensorDataDumperActivity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectivityDumperService extends Service {

    private TimerTask mTimerTask;
    private Timer mTimer;
    ConnectivityManager mConnectionManager;
    NetworkInfo mWifi;
    NetworkInfo mMobile;
    NetworkInfo mBlueTooth;
    WifiManager mWifiManager;
    BluetoothAdapter mBluetoothAdapter;
    TelephonyManager mTelephonyManager;
    DataToFileWriter mDataToFileWriter;
    private static final String mLogTag = "ConnectivityService";

    public ConnectivityDumperService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Connectivity Dumper Service Starting", Toast.LENGTH_SHORT).show();
        try {
            mDataToFileWriter = new DataToFileWriter("connectivity.txt");
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }
            mConnectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Information about wifi
        mWifi = mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mMobile = mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        mBlueTooth = mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.i(mLogTag, "Service Provider Name: " + mTelephonyManager.getSimOperatorName());

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(mLogTag, "Checking connections...");
                if (mWifi.isConnected()) {
                    Log.i(mLogTag, "Wifi is connected");
                    wifiConnected();
                }
                scanWifi();
                if (mMobile.isConnected()) {
                    Log.i(mLogTag, "Mobile is connected");
                    mobileConnected();
                }
                if (mBlueTooth.isConnected()) {
                    Log.i(mLogTag, "Bluetooth is connected");
                    bluetoothConnected();
                }
                scanBluetooth();
                cellularData();
            }
        };
        mTimer = new Timer("Connectivity Timer");
        mTimer.schedule(mTimerTask, 0, 5000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void wifiConnected() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String toDump = "Current SSID: " + wifiInfo.getSSID();
        Log.i(mLogTag, toDump);
        mDataToFileWriter.writeToFile(toDump);

    }

    private void scanWifi() {
        Log.i(mLogTag, "Scanning Wifi");
        mDataToFileWriter.writeToFile("Scanning Wifi");
        List<android.net.wifi.ScanResult> scanresults = mWifiManager.getScanResults();
        mDataToFileWriter.writeToFile("ambient network:");
        for (android.net.wifi.ScanResult scanResult : scanresults) {
            String dump = scanResult.SSID + " last seen:" + Long.toString(scanResult.timestamp);
            mDataToFileWriter.writeToFile(dump);
            Log.i(mLogTag, dump);
        }
    }

    private void scanBluetooth() {
        Log.i(mLogTag, "Scanning Bluetooth");
        mDataToFileWriter.writeToFile("Scanning Bluetooth");
        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mDataToFileWriter.writeToFile("ambient bluetooth:");
        bluetoothLeScanner.startScan(new ScanCallback() {
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    String dump = result.getDevice().getName();
                    mDataToFileWriter.writeToFile(dump);
                    Log.i(mLogTag, dump);
                }
            }
        });
    }

    private void bluetoothConnected() {

    }

    private void mobileConnected() {

    }

    private void cellularData() {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mDataToFileWriter.closeFile();
    }
}
