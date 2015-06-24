package com.example.hooligan.proximitydatadumper;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;
import com.example.hooligan.temperaturedatadumper.TemperatureDataRunnableService;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProximityDataDumperFragment extends Fragment implements ProximityFragmentInterface {

    private Boolean isDumping = false;
    Button dumpProximityButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public ProximityDataDumperFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_proximity_data_dumper, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        dumpProximityButton = (Button) getView().findViewById(R.id.proximity_button);
        setButtonText();
    }

    private void setButtonText() {
        if (isDumping) {
            dumpProximityButton.setText("Stop Dumping Proximity");
        } else {
            dumpProximityButton.setText("Start Dumping Proximity");
        }
    }

    @Override
    public void didPressProximityButton(View v) {
        mIntent = new Intent(getActivity().getApplicationContext(), ProximityDataService.class);
        if (!isDumping) {
            getActivity().startService(mIntent);
        } else {
            getActivity().stopService(mIntent);
        }
        isDumping = !isDumping;
        setButtonText();
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), ProximityDataService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), ProximityDataService.class);
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
}
