package com.example.hooligan.accelerometerdatadumper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DumpAccelerometerService extends Service implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private double[] gravity = new double[3];
    private double[] linear_acceleration = new double[3];

    public DumpAccelerometerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Accelerometer Dumper Service Starting", Toast.LENGTH_SHORT).show();
        Log.i("DumpAccelService", "Dump Accelerometer Service starting");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, 100);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
        Log.i("AccelerometerService", String.format("%f, %f, %f", linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]));
    }

    @Override
    public void onDestroy() {
        Log.i("AccelerometerService", "On Destroy");
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }
}
