package com.example.hooligan.airpressuredatadumper;

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
public class AirPressureDataRunnable extends Thread implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DataToFileWriter mDataToFileWriter;
    private String mLogTag = "AirPressureRunnable";

    public AirPressureDataRunnable(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    public void run() {
        Looper.prepare();
        mSensorManager.registerListener(this, mSensor, 2000000);
        mDataToFileWriter = new DataToFileWriter("Pressure.txt");
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
        float pressure = event.values[0];
        String toDump = "pressure: " + Float.toString(pressure);
        Log.i(mLogTag, toDump);
        mDataToFileWriter.writeToFile(toDump);
    }
}
