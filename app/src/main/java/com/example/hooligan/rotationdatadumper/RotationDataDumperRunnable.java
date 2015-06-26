package com.example.hooligan.rotationdatadumper;

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
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Hooligan on 6/1/2015.
 */
public class RotationDataDumperRunnable extends Thread implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mRotationSensor;
    private final float[] mRotationMatrix = new float[16];

    private static final int DUMPING = 0;
    private static final int STOP = 1;
    private static final String mLogTag = "RotationRunnable";
    DataToFileWriter mDataToFileWriter;

    public RotationDataDumperRunnable(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mDataToFileWriter = new DataToFileWriter("Rotation.txt");
        mDataToFileWriter.writeToFile("Time\t1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16", false);
    }

    @Override
    public void run() {
        Looper.prepare();
        mSensorManager.registerListener(this,mRotationSensor,2000000);
        Looper.loop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
        String toDump = String.format("%s", Arrays.toString(mRotationMatrix));
        mDataToFileWriter.writeToFile(toDump);
        Log.i(mLogTag, toDump);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public synchronized void startDumping(){
        mRotationMatrix[0] = 1;
        mRotationMatrix[4] = 1;
        mRotationMatrix[8] = 1;
        mRotationMatrix[12] = 1;
        start();
    }

    public synchronized void stopDumping() {
        mSensorManager.unregisterListener(this);
        mDataToFileWriter.closeFile();
    }
}
