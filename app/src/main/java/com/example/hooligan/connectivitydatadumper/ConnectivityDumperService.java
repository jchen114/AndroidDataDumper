package com.example.hooligan.connectivitydatadumper;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectivityDumperService extends Service {

    private TimerTask mTimerTask;
    private Timer mTimer;
    private TimerTask mTimerTaskBT;
    private Timer mTimerBT;
    ConnectivityManager mConnectionManager;
    NetworkInfo mWifi;
    NetworkInfo mMobile;
    WifiManager mWifiManager;
    BluetoothAdapter mBluetoothAdapter;
    TelephonyManager mTelephonyManager;
    DataToFileWriter mDataToFileWriterWifi;
    DataToFileWriter mDataToFileWriterBluetooth;
    DataToFileWriter mDataToFileWriterCell;
    private StringBuilder wifiToDump;
    private StringBuilder blueToothToDump;
    private StringBuilder cellularToDump;

    private ArrayList<BluetoothDevice> mBluetoothDevices;
    private Boolean mBTScanning = false;

    // Begin Discovery
    private final BroadcastReceiver mStartedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(mBTLogTag, "Discovery began");
            //Log.i(mBTLogTag, blueToothToDump.toString());
            mBTScanning = true;
            mBluetoothDevices = new ArrayList<>();
        }
    };

    // Found BT Device
    private final BroadcastReceiver mFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(mBTLogTag, "BT device: " + device.getName() + " " + device.getAddress());
                mBluetoothDevices.add(device);
            }
        }
    };

    // End Discovery
    private final BroadcastReceiver mFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(mBTLogTag, "Discovery Finished");

            for (BluetoothDevice device : mBluetoothDevices) {
                blueToothToDump.append(device.getName() + ", ");
            }

            //Log.i(mBTLogTag, blueToothToDump.toString());

            mDataToFileWriterBluetooth.writeToFile(blueToothToDump.toString());

            mBTScanning = false;
        }
    };

    private static final String mLogTag = "ConnectivityService";
    private static final String mBTLogTag = "BluetoothService";
    private static final String mCellLogTag = "CellInfoService";

    public ConnectivityDumperService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Connectivity Dumper Service Starting", Toast.LENGTH_SHORT).show();
        try {
            mDataToFileWriterWifi = new DataToFileWriter("Wifi.txt");
            mDataToFileWriterBluetooth = new DataToFileWriter("Bluetooth.txt");
            mDataToFileWriterCell = new DataToFileWriter("CellTower.txt");

            mDataToFileWriterWifi.writeToFile("Time, [Current_SSID, Current_BSSID, Current_RSSI], [Ambient_SSID, Ambient_BSSID, Ambient_RSSI]", false);
            mDataToFileWriterBluetooth.writeToFile("Time, Own, Paired, Ambient", false);
            mDataToFileWriterCell.writeToFile("Time, Network Code, Network Name, Signal Strength, Cell Identity, Tracking Area Code", false);
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }
        mConnectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mFoundReceiver, filter); // Don't forget to unregister during onDestroy

        IntentFilter filterBegan = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mStartedReceiver, filterBegan);

        IntentFilter filterDone = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mFinishedReceiver, filterDone);

        mTimerTask = new TimerTask() {
            @Override
            public void run() {

                // Information about wifi
                mWifi = mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                mMobile = mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                Log.i(mLogTag, "Service Provider Name: " + mTelephonyManager.getSimOperatorName());

                mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

                Log.i(mLogTag, "Checking connections...");
                wifiToDump = new StringBuilder();
                if (mWifi.isConnected()) {
                    Log.i(mLogTag, "Wifi is connected");
                    wifiConnected();
                } else {
                    wifiToDump.append("[0], ");
                }
                scanWifi();
                if (mMobile.isConnected()) {
                    Log.i(mLogTag, "Mobile is connected");
                    mobileConnected();
                }

                cellularData();
            }
        };
        mTimer = new Timer("Connectivity Timer");
        mTimer.schedule(mTimerTask, 0, 5000); /// Wifi and Cellular

        mTimerTaskBT = new TimerTask() {
            @Override
            public void run() {
                // Time, Own, Paired, Ambient
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (mBluetoothAdapter == null) { // Device does not support bluetooth.
                    blueToothToDump = new StringBuilder();
                    Log.i(mBTLogTag, "Device does not support bluetooth");
                    blueToothToDump.append("0, 0, 0");
                    mDataToFileWriterBluetooth.writeToFile(blueToothToDump.toString());
                } else {
                    if (!mBluetoothAdapter.isEnabled()) { // Bluetooth not enabled
                        blueToothToDump = new StringBuilder();
                        Log.i(mBTLogTag, "Bluetooth not enabled");
                        blueToothToDump.append("0, 0, 0");
                    } else {
                        if (!mBTScanning) {
                            blueToothToDump = new StringBuilder();
                            Log.i(mBTLogTag, "Not scanning, make a new entry");
                            blueToothToDump.append("1, ");
                            bluetoothConnected();
                            scanBluetooth();
                        } else {
                            Log.i(mBTLogTag, "Still scanning");
                        }
                    }
                }

            }
        };

        mTimerBT = new Timer("Bluetooth Timer");
        mTimerBT.schedule(mTimerTaskBT, 0, 5000);

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
                + "," + wifiInfo.getBSSID() + ", "
                + Integer.toString(wifiInfo.getRssi()) + "], ");
        Log.i(mLogTag, wifiToDump.toString());
        //mDataToFileWriterWifi.writeToFile(toDump);
    }

    private void scanWifi() {
        Log.i(mLogTag, "Scanning Wifi");
        List<android.net.wifi.ScanResult> scanresults = mWifiManager.getScanResults();
        for (android.net.wifi.ScanResult scanResult : scanresults) {
            String dump = scanResult.SSID + " last seen:" + Long.toString(scanResult.timestamp);
            //Log.i(mLogTag, dump);
            wifiToDump.append("[" + scanResult.SSID
                    + "," + scanResult.BSSID + ","
                    + Integer.toString(scanResult.level) + "], ");
        }
        mDataToFileWriterWifi.writeToFile(wifiToDump.toString());
    }

    private void bluetoothConnected() {
        Log.i(mBTLogTag, "Bluetooth Connected");
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() == 0) {
            blueToothToDump.append("0, ");
        } else {
            for (BluetoothDevice bluetoothDevice : bondedDevices) {
                blueToothToDump.append(bluetoothDevice.getName() + ", ");
            }
        }
    }

    private void scanBluetooth() {
        Log.i(mLogTag, "Scanning Bluetooth");
        mBluetoothAdapter.startDiscovery();
    }

    private void mobileConnected() {

    }

    private void cellularData() {
        if (mTelephonyManager != null) {
            try {
                //Type:[Time, Network Code, Country Code, Network Name, Signal Strength, Cell Identity, Physical Cell Id, Tracking Area Code]

                List<?> cellInfos = mTelephonyManager.getAllCellInfo();
                Log.i(mCellLogTag, "Getting Cell Tower information");
                cellularToDump = new StringBuilder();
                for (int i = 0; i < cellInfos.size(); i ++) {

                    String cellClassName = mTelephonyManager.getAllCellInfo().get(i).getClass().getName().toLowerCase();
                    if (cellClassName.contains("wcdma")) {
                        Log.i(mCellLogTag, "wcdma");
                        cellularToDump.append("wcdma: [");
                        CellInfoWcdma cellinfowcdma = (CellInfoWcdma) mTelephonyManager.getAllCellInfo().get(i);
                        CellIdentityWcdma cellId = cellinfowcdma.getCellIdentity();
                        if (cellId.getMnc() != Integer.MAX_VALUE) { // Mobile Network Code
                            cellularToDump.append(Integer.toString(cellId.getMnc()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        if(cellId.getMcc() != Integer.MAX_VALUE) {
                            cellularToDump.append(Integer.toString(cellId.getMcc()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        String name = mTelephonyManager.getSimOperatorName();
                        cellularToDump.append(name + ", ");
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellinfowcdma.getCellSignalStrength();
                        Integer signalStrength = cellSignalStrengthWcdma.getDbm();
                        if (signalStrength == Integer.MAX_VALUE || signalStrength == Integer.MIN_VALUE) {
                            cellularToDump.append("0, ");
                        } else {
                            cellularToDump.append(signalStrength.toString() + ", ");
                        }
                        if (cellId.getCid() != Integer.MAX_VALUE) { // Cell Identity
                            cellularToDump.append(Integer.toString(cellId.getCid()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        cellularToDump.append("0, "); // No physical cell id
                        if (cellId.getLac() != Integer.MAX_VALUE) { // Location Area Code
                            cellularToDump.append(Integer.toString(cellId.getLac()));
                        } else {
                            cellularToDump.append("0");
                        }
                        cellularToDump.append("],");

                    } else if (cellClassName.contains("gsm")) {
                        Log.i(mCellLogTag, "gsm");
                        cellularToDump.append("gsm: [");
                        CellInfoGsm cellinfogsm = (CellInfoGsm) mTelephonyManager.getAllCellInfo().get(i);
                        CellIdentityGsm cellId = cellinfogsm.getCellIdentity();
                        if (cellId.getMnc() != Integer.MAX_VALUE) { // Mobile Network Code
                            cellularToDump.append(Integer.toString(cellId.getMnc()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        if(cellId.getMcc() != Integer.MAX_VALUE) {
                            cellularToDump.append(Integer.toString(cellId.getMcc()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        String name = mTelephonyManager.getSimOperatorName();
                        cellularToDump.append(name + ", "); // Name
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
                        Integer signalStrength = cellSignalStrengthGsm.getDbm();
                        if (signalStrength == Integer.MAX_VALUE || signalStrength == Integer.MIN_VALUE) {
                            cellularToDump.append("0, ");
                        } else {
                            cellularToDump.append(signalStrength.toString() + ", ");
                        }
                        if (cellId.getCid() != Integer.MAX_VALUE) { // Cell Identity
                            cellularToDump.append(Integer.toString(cellId.getCid()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        cellularToDump.append("0, "); // No physical cell id
                        if (cellId.getLac() != Integer.MAX_VALUE) { // Location Area Code
                            cellularToDump.append(Integer.toString(cellId.getLac()));
                        } else {
                            cellularToDump.append("0");
                        }
                        cellularToDump.append("],");
                    } else if (cellClassName.contains("lte")) {
                        Log.i(mCellLogTag, "lte");
                        cellularToDump.append("lte: [");
                        CellInfoLte cellinfolte = (CellInfoLte) mTelephonyManager.getAllCellInfo().get(i);
                        CellIdentityLte cellId = cellinfolte.getCellIdentity();
                        if (cellId.getMnc() != Integer.MAX_VALUE) { // Mobile Network Code
                            cellularToDump.append(Integer.toString(cellId.getMnc()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        if(cellId.getMcc() != Integer.MAX_VALUE) {
                            cellularToDump.append(Integer.toString(cellId.getMcc()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        String name = mTelephonyManager.getSimOperatorName();
                        cellularToDump.append(name + ", "); // Name
                        CellSignalStrengthLte cellSignalStrengthLte = cellinfolte.getCellSignalStrength();
                        Integer signalStrength = cellSignalStrengthLte.getDbm(); // Signal Strength
                        if (signalStrength == Integer.MAX_VALUE || signalStrength == Integer.MIN_VALUE) {
                            cellularToDump.append("0, ");
                        } else {
                            cellularToDump.append(signalStrength.toString() + ", ");
                        }
                        if (cellId.getCi() != Integer.MAX_VALUE) { // Cell Identity
                            cellularToDump.append(Integer.toString(cellId.getCi()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        if (cellId.getPci() != Integer.MAX_VALUE) { // Physical Cell Identity
                            cellularToDump.append(Integer.toString(cellId.getPci()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        if (cellId.getTac() != Integer.MAX_VALUE) { // Location Area Code
                            cellularToDump.append(Integer.toString(cellId.getTac()));
                        } else {
                            cellularToDump.append("0");
                        }
                        cellularToDump.append("],");

                    } else if (cellClassName.contains("cdma")) {
                        Log.i(mCellLogTag, "cdma");
                        cellularToDump.append("cdma: [");
                        CellInfoCdma cellinfocdma = (CellInfoCdma) mTelephonyManager.getAllCellInfo().get(i);
                        CellIdentityCdma cellId = cellinfocdma.getCellIdentity();
                        if (cellId.getNetworkId() != Integer.MAX_VALUE) { // Mobile Network Code
                            cellularToDump.append(Integer.toString(cellId.getNetworkId()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        cellularToDump.append("0, "); // Mobile Country Code
                        String name = mTelephonyManager.getSimOperatorName();
                        cellularToDump.append(name + ", "); // Name
                        CellSignalStrengthCdma cellSignalStrengthCdma = cellinfocdma.getCellSignalStrength();
                        Integer signalStrength = cellSignalStrengthCdma.getDbm(); // Signal Strength
                        if (signalStrength == Integer.MAX_VALUE || signalStrength == Integer.MIN_VALUE) {
                            cellularToDump.append("0, ");
                        } else {
                            cellularToDump.append(signalStrength.toString() + ", ");
                        }
                        if (cellId.getBasestationId() != Integer.MAX_VALUE) { // Cell Identity
                            cellularToDump.append(Integer.toString(cellId.getBasestationId()) + ", ");
                        } else {
                            cellularToDump.append("0, ");
                        }
                        cellularToDump.append("0, "); // No physical cell id
                        if (cellId.getLongitude() != Integer.MAX_VALUE) { // Location of basestation
                            cellularToDump.append("Lat: " + Integer.toString(cellId.getLatitude())
                                    + ", Long: " + Integer.toString(cellId.getLongitude()));
                        } else {
                            cellularToDump.append("0");
                        }
                        cellularToDump.append("],");
                    }
                }
                Log.i(mCellLogTag, cellularToDump.toString());
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
        mTimerBT.cancel();
        mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(mFoundReceiver);
        unregisterReceiver(mStartedReceiver);
        unregisterReceiver(mFinishedReceiver);
        mDataToFileWriterWifi.closeFile();
        mDataToFileWriterBluetooth.closeFile();
        mDataToFileWriterCell.closeFile();
    }
}
