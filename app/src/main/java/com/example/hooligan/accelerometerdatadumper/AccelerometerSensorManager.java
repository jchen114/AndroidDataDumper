package com.example.hooligan.accelerometerdatadumper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Hooligan on 5/28/2015.
 */
public class AccelerometerSensorManager implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    final double alpha = 0.8;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {



    }
}
