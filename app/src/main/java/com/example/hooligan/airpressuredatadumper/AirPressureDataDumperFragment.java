package com.example.hooligan.airpressuredatadumper;


import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;
import com.example.hooligan.accelerometerdatadumper.DumpAccelerometerRunnableService;

/**
 * A simple {@link Fragment} subclass.
 */
public class AirPressureDataDumperFragment extends Fragment implements AirPressureFragmentInterface {

    private Boolean isDumping = false;
    Button dumpAirButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private String mLogTag = "AirPressureService";
    Intent mIntent;
    public static AirPressureDataDumperFragment mAirPressureDataDumperFragment;

    public AirPressureDataDumperFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        mAirPressureDataDumperFragment = this;
        return inflater.inflate(R.layout.fragment_air_pressure_data_dumper, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        dumpAirButton = (Button) getView().findViewById(R.id.air_button);
        setButtonText();
    }

    @Override
    public void didPressAirPressureButton(View v) {

        //Intent accelIntent = new Intent(getActivity().getApplicationContext(), DumpAccelerometerService.class);
        mIntent = new Intent(getActivity().getApplicationContext(), AirPressureDataService.class);
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
            dumpAirButton.setText("Stop Dumping Air Pressure");
        } else  {
            dumpAirButton.setText("Start Dumping Air Pressure");
        }
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), AirPressureDataService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), AirPressureDataService.class);
            getActivity().stopService(mIntent);
            isDumping = false;
            setButtonText();
        }
    }

    @Override
    public void onDestroy() {
        Log.i(mLogTag, "OnDestroy");
        super.onDestroy();
    }
}
