package com.example.hooligan.accelerometerdatadumper;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.hooligan.rotationdatadumper.RotationDataDumperRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Hooligan on 5/28/2015.
 */
public class DumpAccelerometerRunnableService extends Service {

    private AccelerometerDataDumperRunnable mAccDataDumperRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AccDataDumperService", "Start Dumping Accelerometer data");
        Toast.makeText(this, "Accelerometer Dumper Service Starting", Toast.LENGTH_SHORT).show();

        mAccDataDumperRunnable = new AccelerometerDataDumperRunnable((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        mAccDataDumperRunnable.startDumping();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mAccDataDumperRunnable.stopDumping();
        super.onDestroy();
    }
}
