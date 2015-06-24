package com.example.hooligan.foregroundactivitydumper;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.hooligan.DataToFileWriter;
import com.example.hooligan.SensorDataDumperActivity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ForegroundDumperService extends Service {

    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private static final String mLogTag = "ForegroundService";
    private DataToFileWriter mDataToFileWriter;

    public ForegroundDumperService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Foreground Dumper Service Starting", Toast.LENGTH_SHORT).show();
        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mPackageManager = this.getPackageManager();
        mDataToFileWriter = new DataToFileWriter("foreground.txt");
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                List<ActivityManager.RunningAppProcessInfo> processes = mActivityManager.getRunningAppProcesses();
                if (processes.size() > 0) {
                    mDataToFileWriter.writeToFile(">>>>>>>>>>>>>>>Running processes: ");
                    Log.i(mLogTag, "Running Processes:");
                    for (int i = 0; i < processes.size(); i ++) {
                        ActivityManager.RunningAppProcessInfo process = processes.get(i);
                        try {
                            CharSequence appName = mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(process.processName, PackageManager.GET_META_DATA));
                            String toDump = Integer.toString(i) + ": " + (String) appName;
                            Log.i(mLogTag, toDump);
                            mDataToFileWriter.writeToFile(toDump);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        mTimer = new Timer(mLogTag);
        mTimer.schedule(mTimerTask, 0, 3000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mDataToFileWriter.closeFile();
    }
}
