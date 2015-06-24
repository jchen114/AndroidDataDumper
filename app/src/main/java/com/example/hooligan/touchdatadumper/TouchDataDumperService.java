package com.example.hooligan.touchdatadumper;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class TouchDataDumperService extends Service implements View.OnTouchListener {

    private View v=null;
    private WindowManager mgr=null;

    @Override
    public void onCreate() {
        super.onCreate();
        // stopSelf(); -- uncomment for "component-less" operation
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TouchDumperService", "On Start Command");
        v = new View(this);
        mgr=(WindowManager)getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params
                =new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSPARENT);

        params.gravity= Gravity.FILL_HORIZONTAL|Gravity.FILL_VERTICAL;
        mgr.addView(v, params);

        //v.setBackgroundColor(Color.GREEN);
        /*
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("TouchDumperService", String.valueOf(event.getX()) + ":" + String.valueOf(event.getY()));
                return false;
            }
        });
        */
        v.setOnTouchListener(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return(null);
    }

    @Override
    public void onDestroy() {
        mgr.removeView(v);  // comment out for "component-less" operation
        super.onDestroy();
    }

    public boolean onTouch(View v, MotionEvent event) {
        Log.i("TouchDumperService", "X coord: " + String.valueOf(event.getX()) + ", Y coord: " + String.valueOf(event.getY()));
        return false;
    }

}
