package com.example.hooligan.magneticdatadumper;

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
public class MagneticDumperRunnable extends Thread implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    private final float[] values = new float[3];

    private String mLogTag = "MagneticRunnable";
    DataToFileWriter mDataToFileWriter;

    public MagneticDumperRunnable(SensorManager sensorManager) {
        this.mSensorManager = sensorManager;
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mDataToFileWriter = new DataToFileWriter("Magnetic.txt");
        mDataToFileWriter.writeToFile("Time\tX\tY\tZ", false);
    }


    @Override
    public void run() {
        Looper.prepare();
        mSensorManager.registerListener(this,mMagneticSensor,2000000);
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
        values[0] = event.values[0];
        values[1] = event.values[1];
        values[2] = event.values[2];
        String toDump = Float.toString(values[0])
                + Float.toString(values[1])
                + Float.toString(values[2]);
        Log.i(mLogTag, toDump);
        mDataToFileWriter.writeToFile(toDump);
    }
}
