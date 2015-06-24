package com.example.hooligan.temperaturedatadumper;

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
public class TemperatureDataRunnable extends Thread implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mTemperatureSensor;
    private DataToFileWriter mDataToFileWriter;
    private String mLogTag = "TemperatureRunnable";

    public TemperatureDataRunnable(SensorManager sensorManager) throws NullPointerException {
        Log.i(mLogTag, "Runnable create");
        mDataToFileWriter = new DataToFileWriter("Temperature.txt");
        mSensorManager = sensorManager;
        mTemperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (mTemperatureSensor == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mSensorManager.registerListener(this, mTemperatureSensor, 2000000);
        Looper.loop();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void startDumping() {
        start();
    }

    public void stopDumping() {
        mSensorManager.unregisterListener(this);
        mDataToFileWriter.closeFile();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Float temp = event.values[0];
        String toDump = "ambient temperature: " + Float.toString(temp) + "C";
        Log.i(mLogTag, toDump);
        mDataToFileWriter.writeToFile(toDump);
    }
}
