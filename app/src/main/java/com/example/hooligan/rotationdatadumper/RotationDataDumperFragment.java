package com.example.hooligan.rotationdatadumper;


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
public class RotationDataDumperFragment extends Fragment implements RotationFragmentInterface{

    private Boolean isDumping = false;
    Button dumpRotButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public RotationDataDumperFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }

        return inflater.inflate(R.layout.fragment_rotation_data_dumper, container, false);
    }

    public void onStart() {
        super.onStart();
        dumpRotButton = (Button) getView().findViewById(R.id.rotation_button);
        setButtonText();
    }

    @Override
    public void didPressRotationButton(View v) {
        mIntent = new Intent(getActivity().getApplicationContext(), DumpRotationRunnableService.class);
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
            dumpRotButton.setText("Stop Dumping Rotation");
        } else {
            dumpRotButton.setText("Start Dumping Rotation");
        }
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), DumpRotationRunnableService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), DumpRotationRunnableService.class);
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
