package com.example.hooligan.touchdatadumper;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Hooligan on 6/8/2015.
 */
public class TouchDataDumperRunnable extends Thread implements View.OnTouchListener{

    private View v;
    private WindowManager mgr;

    private static final int DUMPING = 0;
    private static final int STOP = 1;

    public TouchDataDumperRunnable(Context context, WindowManager windowManager) {
        v = new View(context);
        mgr = windowManager;
    }

    @Override
    public void run() {
        Looper.prepare();
        v.setOnTouchListener(this);
        WindowManager.LayoutParams params
                =new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSPARENT);
        params.gravity=Gravity.FILL_HORIZONTAL| Gravity.FILL_VERTICAL;
        mgr.addView(v, params);
        Looper.loop();
    }

    public void startDumping() {
        start();
    }

    public void stopDumping() {
        interrupt();
        v = null;
        mgr.removeViewImmediate(v);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i("TouchRunnable", String.valueOf(event.getX()) + ":" + String.valueOf(event.getY()));
        return false;
    }
}
