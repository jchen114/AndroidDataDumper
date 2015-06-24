package com.example.hooligan.locationdumper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.example.hooligan.DataToFileWriter;
import com.example.hooligan.SensorDataDumperActivity;

public class DumpLocationRunnableService extends Service {

    private DumperLocationThread mLocationDumperThread;
    private static final String mLogTag = "DumpLocationService";

    public DumpLocationRunnableService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(mLogTag, "Starting Service");
        Toast.makeText(this, "Location Dumper Service Starting", Toast.LENGTH_SHORT).show();
        mLocationDumperThread = new DumperLocationThread();
        mLocationDumperThread.startDumping();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mLocationDumperThread.stopDumping();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
