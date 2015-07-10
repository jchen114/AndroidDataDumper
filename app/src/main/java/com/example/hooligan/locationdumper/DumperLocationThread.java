package com.example.hooligan.locationdumper;

import android.hardware.Sensor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.hooligan.DataToFileWriter;
import com.example.hooligan.SensorDataDumperActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Hooligan on 6/4/2015.
 */
public class DumperLocationThread extends Thread
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private String mUpdateTime;
    DataToFileWriter mDataToFileWriter;
    private static final String mLogTag = "DumperLocationThread";

    public void startDumping() {
        start();
    }

    public void stopDumping() {
        Log.i(mLogTag, "Stop the dumping");
        mDataToFileWriter.closeFile();
        stopLocationUpdates();
    }

    @Override
    public void run() {
        Looper.prepare();
        mDataToFileWriter = new DataToFileWriter("Location.txt");
        buildGoogleApiClient();
        createLocationRequest();
        Looper.loop();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(SensorDataDumperActivity.mSensorDataDumperActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        String toDump = "Connecting client";
        Log.i(mLogTag, toDump);
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        String toDump = "Connection successful";
        Log.i(mLogTag, toDump);
        mDataToFileWriter.writeToFile("Time, Lat, Long, altitude, acc", false);
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        String toDump = "Failed to connect to google play: " + Integer.toString(connectionResult.getErrorCode());
        Log.e(mLogTag, toDump);
        mDataToFileWriter.writeToFile(toDump);
        GoogleApiAvailability.getInstance().getErrorDialog(SensorDataDumperActivity.mSensorDataDumperActivity, connectionResult.getErrorCode(),1).show();
        stopDumping();
        LocationDumperFragment.mLocationDumperFragment.connectionRefused();
    }

    @Override
    public void onConnectionSuspended(int i) {
        String toDump = "Suspended connection to google play: " + Integer.toString(i);
        Log.e(mLogTag, toDump);
        mDataToFileWriter.writeToFile(toDump);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(mLogTag, "OnLocationChanged");
        mCurrentLocation = location;
        float accuracy = location.getAccuracy();
        mUpdateTime = DateFormat.getTimeInstance().format(new Date());
        String toDump = "";
        if (location.hasAltitude()) {
            toDump = Double.toString(location.getLatitude()) + ", "
                    + Double.toString(location.getLongitude()) + ", "
                    + Double.toString(location.getAltitude()) + ", "
                    + Float.toString(accuracy);
        } else {
            toDump = Double.toString(location.getLatitude()) + ", "
                    + Double.toString(location.getLongitude()) + ", "
                    + "- , "
                    + Float.toString(accuracy);
        }
        Log.i(mLogTag, toDump);
        mDataToFileWriter.writeToFile(toDump);
    }

    protected void stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        } catch (IllegalStateException e) {
            Log.e(mLogTag, "Illegal State exception");
            e.printStackTrace();
        }
    }
}
