package com.example.hooligan.screenstatedumper;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScreenStateDumperFragment extends Fragment implements ScreenStateFragmentInterface{

    private Boolean isDumping = false;
    Button mDumpScreenButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public ScreenStateDumperFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_screen_state_dumper, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mDumpScreenButton = (Button) getView().findViewById(R.id.screen_button);
        setButtonText();
    }

    private void setButtonText() {
        if (isDumping) {
            mDumpScreenButton.setText("Stop Dumping Screen State");
        } else {
            mDumpScreenButton.setText("Start Dumping Screenn State");
        }
    }

    @Override
    public void didPressScreenButton(View v) {
        mIntent = new Intent(getActivity().getApplicationContext(), ScreenStateDumperService.class);
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
            mIntent = new Intent(getActivity().getApplicationContext(), ScreenStateDumperService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), ScreenStateDumperService.class);
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
