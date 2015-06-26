package com.example.hooligan.airpressuredatadumper;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

import com.example.hooligan.SensorDataDumperActivity;

public class AirPressureDataService extends Service {

    private AirPressureDataRunnable mAirPressureDataRunnable;
    private static final String mLogTag = "AirPressureService";

    public AirPressureDataService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Air Pressure Dump Service Starting",Toast.LENGTH_SHORT).show();
        SensorDataDumperActivity.writeLogs(mLogTag + " Starting");
        mAirPressureDataRunnable = new AirPressureDataRunnable((SensorManager) getSystemService(SENSOR_SERVICE));
        mAirPressureDataRunnable.startDumping();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        SensorDataDumperActivity.writeLogs(mLogTag + " Stopping");
        mAirPressureDataRunnable.stopDumping();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
