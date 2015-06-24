package com.example.hooligan.touchdatadumper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.hooligan.rotationdatadumper.RotationDataDumperRunnable;

public class DumpTouchRunnableService extends Service {

    private TouchDataDumperRunnable mTouchRunnable;

    public DumpTouchRunnableService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TouchRunnableService", "Start Dumping Touch data");
        Toast.makeText(this, "Touch Dumper Service Starting", Toast.LENGTH_SHORT).show();

        mTouchRunnable = new TouchDataDumperRunnable(this, (WindowManager)getSystemService(WINDOW_SERVICE));
        mTouchRunnable.startDumping();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mTouchRunnable.stopDumping();
        super.onDestroy();
    }
}
