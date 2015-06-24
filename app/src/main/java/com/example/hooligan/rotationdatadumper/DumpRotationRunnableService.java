package com.example.hooligan.rotationdatadumper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.hooligan.accelerometerdatadumper.AccelerometerDataDumperRunnable;

/**
 * Created by Hooligan on 6/1/2015.
 */
public class DumpRotationRunnableService extends Service {

    private RotationDataDumperRunnable mRotDataDumperRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("DumpRotRunnableService", "Start Dumping Rotational data");
        Toast.makeText(this, "Rotation Dumper Service Starting", Toast.LENGTH_SHORT).show();

        mRotDataDumperRunnable = new RotationDataDumperRunnable((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        mRotDataDumperRunnable.startDumping();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mRotDataDumperRunnable.stopDumping();
        super.onDestroy();
    }
}
