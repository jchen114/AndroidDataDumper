package com.example.hooligan.accelerometerdatadumper;

import android.content.Intent;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class AccelerometerDataDumperFragment extends Fragment implements AccelerometerFragmentInterface {

    private Boolean isDumping = false;
    Button dumpAccButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public AccelerometerDataDumperFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }

        return inflater.inflate(R.layout.fragment_accelerometer_data_dumper, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        dumpAccButton = (Button) getView().findViewById(R.id.accel_button);
        setButtonText();
    }

    @Override
    public void didPressDumpAccelerometerButton(View v) {

        //Intent accelIntent = new Intent(getActivity().getApplicationContext(), DumpAccelerometerService.class);
        mIntent = new Intent(getActivity().getApplicationContext(), DumpAccelerometerRunnableService.class);
        if (!isDumping) {
            getActivity().startService(mIntent);
        } else {
            getActivity().stopService(mIntent);
        }
        isDumping = !isDumping;
        setButtonText();
    }

    private void setButtonText() {
        if (isDumping) {
            dumpAccButton.setText("Stop Dumping Accelerometer");
        } else  {
            dumpAccButton.setText("Start Dumping Accelerometer");
        }
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), DumpAccelerometerRunnableService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), DumpAccelerometerRunnableService.class);
            getActivity().stopService(mIntent);
            isDumping = false;
            setButtonText();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_DUMPING, isDumping);
    }

    @Override
    public void onDestroy() {
        Log.i("AccDumperFragment", "OnDestroy");
        super.onDestroy();
    }
}
