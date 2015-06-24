package com.example.hooligan.temperaturedatadumper;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;
import com.example.hooligan.rotationdatadumper.DumpRotationRunnableService;

/**
 * A simple {@link Fragment} subclass.
 */
public class TemperatureDataFragment extends Fragment implements TemperatureFragmentInterface {

    private Boolean isDumping = false;
    Button dumpTempButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public TemperatureDataFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_temperature_data, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        dumpTempButton = (Button) getView().findViewById(R.id.temperature_button);
        setButtonText();
    }

    private void setButtonText() {
        if (isDumping) {
            dumpTempButton.setText("Stop Dumping Temperature");
        } else {
            dumpTempButton.setText("Start Dumping Temperature");
        }
    }

    @Override
    public void didPressTemperatureButton(View v) {
        mIntent = new Intent(getActivity().getApplicationContext(), TemperatureDataRunnableService.class);
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
            mIntent = new Intent(getActivity().getApplicationContext(), TemperatureDataRunnableService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), TemperatureDataRunnableService.class);
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
