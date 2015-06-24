package com.example.hooligan.ambientlightdatadumper;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;
import com.example.hooligan.accelerometerdatadumper.DumpAccelerometerRunnableService;

/**
 * A simple {@link Fragment} subclass.
 */
public class AmbientDataDumperFragment extends Fragment implements AmbientLightFragmentInterface {

    private Boolean isDumping = false;
    Button dumpAmbientButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    Intent mIntent;

    public AmbientDataDumperFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_ambient_data_dumper, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        dumpAmbientButton = (Button) getView().findViewById(R.id.ambient_light_button);
        setButtonText();
    }

    @Override
    public void didPressAmbientButton(View v) {
        Intent mIntent = new Intent(getActivity().getApplicationContext(), DumpAmbientLightService.class);
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
            dumpAmbientButton.setText("Stop Dumping Ambient Light");
        } else  {
            dumpAmbientButton.setText("Start Dumping Ambient Light");
        }
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), DumpAmbientLightService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), DumpAmbientLightService.class);
            getActivity().stopService(mIntent);
            isDumping = false;
            setButtonText();
        }
    }

}
