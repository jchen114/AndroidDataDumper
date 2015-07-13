package com.example.hooligan.screenstatedumper;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import com.example.hooligan.DataToFileWriter;

public class ScreenStateDumperService extends Service {

    DataToFileWriter mDataToFileWriter;


    private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mDataToFileWriter.writeToFile("On");
        }
    };

    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mDataToFileWriter.writeToFile("Off");
        }
    };

    public ScreenStateDumperService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(getApplicationContext(), "Screen State service starting", Toast.LENGTH_SHORT).show();

        mDataToFileWriter = new DataToFileWriter("Screen-state.txt");
        mDataToFileWriter.writeToFile("Time, State");
        mDataToFileWriter.writeToFile("On");

        IntentFilter screenOnFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        registerReceiver(mScreenOnReceiver, screenOnFilter);

        IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOffReceiver, screenOffFilter);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenOnReceiver);
        unregisterReceiver(mScreenOffReceiver);
        mDataToFileWriter.closeFile();
    }

}
