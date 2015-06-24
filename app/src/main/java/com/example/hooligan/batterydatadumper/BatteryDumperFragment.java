package com.example.hooligan.batterydatadumper;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BatteryDumperFragment extends Fragment implements BatteryFragmentInterface {

    private Boolean isDumping = false;
    Button dumpBatteryButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public BatteryDumperFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_battery__dumper, container, false);
    }

    @Override
    public void didPressBatteryButton(View v) {

        Intent mIntent = new Intent(getActivity().getApplicationContext(), BatteryDumperService.class);

        if (!isDumping) {
            getActivity().startService(mIntent);
        } else {
            getActivity().stopService(mIntent);
        }

        isDumping = !isDumping;
        setButtonText();

    }

    @Override
    public void onStart() {
        super.onStart();
        dumpBatteryButton = (Button) getView().findViewById(R.id.battery_button);
        setButtonText();
    }

    private void setButtonText() {
        if (isDumping) {
            dumpBatteryButton.setText("Stop Dumping Battery Info");
        } else  {
            dumpBatteryButton.setText("Start Dumping Battery Info");
        }
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), BatteryDumperService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), BatteryDumperService.class);
            getActivity().stopService(mIntent);
            isDumping = false;
            setButtonText();
        }
    }
    
}
