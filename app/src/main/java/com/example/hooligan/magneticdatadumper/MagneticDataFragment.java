package com.example.hooligan.magneticdatadumper;


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
public class MagneticDataFragment extends Fragment implements MagneticFragmentInterface{

    private Boolean isDumping = false;
    Button dumpMagButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public MagneticDataFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_magnetic_data, container, false);
    }

    @Override
    public void didPressMagneticButton(View v) {
        mIntent = new Intent(getActivity().getApplicationContext(), MagneticDumperService.class);
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
        dumpMagButton = (Button) getView().findViewById(R.id.magnetic_button);
    }

    private void setButtonText() {
        if (isDumping) {
            dumpMagButton.setText("Stop Dumping Magnetic");
        } else {
            dumpMagButton.setText("Start Dumping Magnetic");
        }
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), MagneticDumperService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), MagneticDumperService.class);
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
