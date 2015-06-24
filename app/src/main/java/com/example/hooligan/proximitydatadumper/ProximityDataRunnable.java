package com.example.hooligan.proximitydatadumper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.util.Log;

import com.example.hooligan.DataToFileWriter;

/**
 * Created by Hooligan on 6/22/2015.
 */
public class ProximityDataRunnable extends Thread implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DataToFileWriter mDataToFileWriter;
    private String mLogTag = "ProximityRunnable";

    public ProximityDataRunnable(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mDataToFileWriter = new DataToFileWriter("Proximity.txt");
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    public void run() {
        Looper.prepare();
        mSensorManager.registerListener(this, mSensor, 2000000);
        Looper.loop();
    }

    public void startDumping() {
        start();
    }

    public void stopDumping() {
        mSensorManager.unregisterListener(this);
        mDataToFileWriter.closeFile();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float distance = event.values[0];
        String toDump = distance > 0 ? "far" : "near";
        Log.i(mLogTag, toDump);
        mDataToFileWriter.writeToFile(toDump);
    }
}
