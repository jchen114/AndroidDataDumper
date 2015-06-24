package com.example.hooligan.proximitydatadumper;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

public class ProximityDataService extends Service {

    private ProximityDataRunnable mProximityDataRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "Proximity Service Starting", Toast.LENGTH_SHORT).show();

        mProximityDataRunnable = new ProximityDataRunnable((SensorManager) getSystemService(SENSOR_SERVICE));
        mProximityDataRunnable.startDumping();

        return START_STICKY;
    }

    public ProximityDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
