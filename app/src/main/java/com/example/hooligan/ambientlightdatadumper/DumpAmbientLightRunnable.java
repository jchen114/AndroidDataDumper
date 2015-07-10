package com.example.hooligan.ambientlightdatadumper;

import android.content.ContentResolver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.util.Log;

import com.example.hooligan.DataToFileWriter;
import com.example.hooligan.SensorDataDumperActivity;

/**
 * Created by Hooligan on 6/10/2015.
 */
public class DumpAmbientLightRunnable extends Thread implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private DataToFileWriter mDataToFileWriter;

    private static final String mLogTag = "AmbientLightRunnable";

    public DumpAmbientLightRunnable(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        try {
            mDataToFileWriter = new DataToFileWriter("Ambient-light.txt");
            mDataToFileWriter.writeToFile("Time, Luminance, Screen Brightness", false);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mSensorManager.registerListener(this, mLightSensor, 3000000);
        Looper.loop();
    }

    public synchronized void startDumping() {
        Log.i(mLogTag, "Start Dumping");
        start();
    }

    public synchronized void stopDumping() {
        Log.i(mLogTag, "Stop dumping");
        mSensorManager.unregisterListener(this);
        mDataToFileWriter.closeFile();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ContentResolver contentResolver = SensorDataDumperActivity.mSensorDataDumperActivity.getApplicationContext().getContentResolver();
        int curBrightnessValue = android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS,-1);
        float luminance = event.values[0];
        String toDump = Float.toString(luminance) + " , " + Integer.toString(curBrightnessValue);
        Log.i(mLogTag, toDump);
        mDataToFileWriter.writeToFile(toDump);
    }
}
