package com.example.hooligan.connectivitydatadumper;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.hooligan.DataToFileWriter;
import com.example.hooligan.SensorDataDumperActivity;

import java.util.List;
import java.util.Set;
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
    DataToFileWriter mDataToFileWriterWifi;
    DataToFileWriter mDataToFileWriterBluetooth;
    DataToFileWriter mDataToFileWriterCell;
    private StringBuilder wifiToDump;
    private StringBuilder blueToothToDump;
    private StringBuilder cellularToDump;
    private static final String mLogTag = "ConnectivityService";

    public ConnectivityDumperService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Connectivity Dumper Service Starting", Toast.LENGTH_SHORT).show();
        try {
            mDataToFileWriterWifi = new DataToFileWriter("Wifi.txt");
            mDataToFileWriterBluetooth = new DataToFileWriter("Bluetooth.txt");
            mDataToFileWriterCell = new DataToFileWriter("CellTower.txt");

            mDataToFileWriterWifi.writeToFile("Time\t[Current_SSID Current_BSSID Current_RSSI]\t[Ambient_SSID Ambient_BSSID Ambient_RSSI]", false);
            mDataToFileWriterBluetooth.writeToFile("Time\tOwn\tPaired\tAmbient", false);
            mDataToFileWriterCell.writeToFile("Time\tNetwork Code\tNetwork Name\tSignal Strength\tCell Identity\tTracking Area Code", false);
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
                wifiToDump = new StringBuilder();
                if (mWifi.isConnected()) {
                    Log.i(mLogTag, "Wifi is connected");
                    wifiConnected();
                }
                scanWifi();
                if (mMobile.isConnected()) {
                    Log.i(mLogTag, "Mobile is connected");
                    mobileConnected();
                }
                blueToothToDump = new StringBuilder();
                if (mBluetoothAdapter.getBluetoothLeScanner() != null) {
                    if (mBlueTooth.isConnected()) {
                        Log.i(mLogTag, "Bluetooth is connected");
                        blueToothToDump.append("1\t");
                        bluetoothConnected();
                    }
                    scanBluetooth();
                } else {
                    blueToothToDump.append("0\t0\t0");
                    mDataToFileWriterBluetooth.writeToFile(blueToothToDump.toString());
                }
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
        wifiToDump.append("[" + wifiInfo.getSSID()
                + "," + wifiInfo.getBSSID()
                + Integer.toString(wifiInfo.getRssi()) + "]");
        Log.i(mLogTag, wifiToDump.toString());
        //mDataToFileWriterWifi.writeToFile(toDump);
    }

    private void scanWifi() {
        Log.i(mLogTag, "Scanning Wifi");
        List<android.net.wifi.ScanResult> scanresults = mWifiManager.getScanResults();
        wifiToDump.append(",\t");
        for (android.net.wifi.ScanResult scanResult : scanresults) {
            String dump = scanResult.SSID + " last seen:" + Long.toString(scanResult.timestamp);
            Log.i(mLogTag, dump);
            wifiToDump.append("[" + scanResult.SSID
                    + "," + scanResult.BSSID + ","
                    + Integer.toString(scanResult.level) + "],");
        }
        wifiToDump.append("]");
        mDataToFileWriterWifi.writeToFile(wifiToDump.toString());
    }

    private void bluetoothConnected() {
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            blueToothToDump.append(bluetoothDevice + ",");
        }
    }

    private void scanBluetooth() {
        Log.i(mLogTag, "Scanning Bluetooth");
        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(new ScanCallback() {
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    blueToothToDump.append(result.getDevice().getName() + ",");
                }
                Log.i(mLogTag, blueToothToDump.toString().trim());
                mDataToFileWriterBluetooth.writeToFile(blueToothToDump.toString().trim());
            }
        });
    }

    private void mobileConnected() {

    }

    private void cellularData() {
        if (mTelephonyManager != null) {
            try {
                //Time Network Code Network Name Signal Strength Cell Identity Physical Cell Id Tracking Area Code"

                List<?> cellInfos = mTelephonyManager.getAllCellInfo();

                for (int i = 0; i < cellInfos.size(); i ++) {
                    String cellClassName = mTelephonyManager.getAllCellInfo().get(i).getClass().getName().toLowerCase();
                    if (cellClassName.contains("wcdma")) {
                        CellInfoWcdma cellinfowcdma = (CellInfoWcdma) mTelephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellinfowcdma.getCellSignalStrength();
                        Integer signalStrength = cellSignalStrengthWcdma.getDbm();

                        cellularToDump = new StringBuilder();
                        cellularToDump.append(
                                Integer.toString(cellinfowcdma.getCellIdentity().getMnc())
                                        + mTelephonyManager.getSimOperatorName() + ","
                                        + signalStrength.toString() + ","
                                        + Integer.toString(cellinfowcdma.getCellIdentity().getCid()) + ","
                                        + Integer.toString(cellinfowcdma.getCellIdentity().getLac())
                        );

                    } else if (cellClassName.contains("gsm")) {
                        CellInfoGsm cellinfogsm = (CellInfoGsm) mTelephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
                        Integer signalStrength = cellSignalStrengthGsm.getDbm();

                        cellularToDump = new StringBuilder();
                        cellularToDump.append(
                                Integer.toString(cellinfogsm.getCellIdentity().getMnc())
                                        + mTelephonyManager.getSimOperatorName() + ","
                                        + signalStrength.toString() + ","
                                        + Integer.toString(cellinfogsm.getCellIdentity().getCid()) + ","
                                        + Integer.toString(cellinfogsm.getCellIdentity().getLac())
                        );
                    } else if (cellClassName.contains("lte")) {
                        CellInfoLte cellinfolte = (CellInfoLte) mTelephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthLte cellSignalStrengthLte = cellinfolte.getCellSignalStrength();
                        Integer signalStrength = cellSignalStrengthLte.getDbm();

                        cellularToDump = new StringBuilder();
                        cellularToDump.append(
                                Integer.toString(cellinfolte.getCellIdentity().getMnc())
                                        + mTelephonyManager.getSimOperatorName() + ","
                                        + signalStrength.toString() + ","
                                        + Integer.toString(cellinfolte.getCellIdentity().getCi()) + ","
                                        + Integer.toString(cellinfolte.getCellIdentity().getTac())
                        );
                    } else if (cellClassName.contains("cdma")) {
                        CellInfoCdma cellinfocdma = (CellInfoCdma) mTelephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthCdma cellSignalStrengthCdma = cellinfocdma.getCellSignalStrength();
                        Integer signalStrength = cellSignalStrengthCdma.getDbm();

                        cellularToDump = new StringBuilder();
                        cellularToDump.append(
                                Integer.toString(cellinfocdma.getCellIdentity().getBasestationId())
                                + mTelephonyManager.getSimOperatorName() + ","
                                + signalStrength.toString() + ","
                                + Integer.toString(cellinfocdma.getCellIdentity().getNetworkId()) + ","
                                + "(" + Integer.toString(cellinfocdma.getCellIdentity().getLatitude()) + ","
                                + Integer.toString(cellinfocdma.getCellIdentity().getLongitude()) + ")"
                        );
                    }
                }



                mDataToFileWriterCell.writeToFile(cellularToDump.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mDataToFileWriterWifi.closeFile();
        mDataToFileWriterBluetooth.closeFile();
        mDataToFileWriterCell.closeFile();
    }
}
