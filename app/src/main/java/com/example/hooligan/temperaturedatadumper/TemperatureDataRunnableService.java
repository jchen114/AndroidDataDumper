package com.example.hooligan.temperaturedatadumper;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

public class TemperatureDataRunnableService extends Service {

    private TemperatureDataRunnable mTemperatureDataRunnable;

    public TemperatureDataRunnableService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mTemperatureDataRunnable = new TemperatureDataRunnable((SensorManager) getSystemService(SENSOR_SERVICE));
            mTemperatureDataRunnable.startDumping();
            return START_STICKY;
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, "No temperature sensor found", Toast.LENGTH_LONG).show();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTemperatureDataRunnable != null) {
            mTemperatureDataRunnable.stopDumping();
        }
    }
}
