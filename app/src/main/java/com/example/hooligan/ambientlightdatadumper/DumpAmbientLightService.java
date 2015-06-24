package com.example.hooligan.ambientlightdatadumper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
public class DumpAmbientLightService extends Service {

    private DumpAmbientLightRunnable mAmbientLightDumperRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AmbientLightService", "Start Dumping Ambient data");
        Toast.makeText(this, "Ambient Light Dumper Service Starting", Toast.LENGTH_SHORT).show();
        mAmbientLightDumperRunnable = new DumpAmbientLightRunnable((SensorManager) getSystemService(SENSOR_SERVICE));
        mAmbientLightDumperRunnable.startDumping();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mAmbientLightDumperRunnable.stopDumping();
        super.onDestroy();
    }

}
