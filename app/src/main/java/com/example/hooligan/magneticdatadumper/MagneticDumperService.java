package com.example.hooligan.magneticdatadumper;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MagneticDumperService extends Service {

    private MagneticDumperRunnable mMagneticDumperRunnable;
    private String mLogTag = "DumpMagService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(mLogTag, "Start Dumping Magnetic data");
        Toast.makeText(this, "Magnetic Dumper Service Starting", Toast.LENGTH_SHORT).show();

        mMagneticDumperRunnable = new MagneticDumperRunnable((SensorManager) getSystemService(SENSOR_SERVICE));
        mMagneticDumperRunnable.startDumping();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mMagneticDumperRunnable.stopDumping();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
