package com.example.hooligan.accelerometerdatadumper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.hooligan.DataToFileWriter;
import com.example.hooligan.SensorDataDumperActivity;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Hooligan on 5/28/2015.
 */
public class AccelerometerDataDumperRunnable extends Thread implements SensorEventListener {

    Queue<AccelerometerData> mQueue = new LinkedList<>();

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private double[] gravity = new double[3];
    private double[] linear_acceleration = new double[3];

    public DataToFileWriter mDataWriter;
    private static final int DUMPING = 0;
    private static final int STOP = 1;

    private final static String mLogTag = "AccelerometerRunnable";

    public AccelerometerDataDumperRunnable(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        try {
            mDataWriter = new DataToFileWriter("accelerometer.txt");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mSensorManager.registerListener(this,mAccelerometer,2000000);
        Looper.loop();
    }

    public synchronized void startDumping() {
        start();
    }

    public synchronized void stopDumping() {
        mSensorManager.unregisterListener(this);
        if (mDataWriter.closeFile()) {
            Log.i(mLogTag, "File closed");
        } else {
            Log.i(mLogTag, "File closing error");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        final double alpha = 0.8;
        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
        String toDump = String.format("x:%f, y:%f, z:%f", linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]);
        Log.i(mLogTag, toDump);
        mDataWriter.writeToFile(toDump);
    }


}
