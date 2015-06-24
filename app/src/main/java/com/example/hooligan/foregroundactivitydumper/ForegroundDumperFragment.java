package com.example.hooligan.foregroundactivitydumper;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;
import com.example.hooligan.locationdumper.DumpLocationRunnableService;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForegroundDumperFragment extends Fragment implements ForegroundFragmentInterface {

    private boolean isDumping = false;
    private Button mForegroundButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public ForegroundDumperFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_foreground_dumper, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mForegroundButton = (Button) getView().findViewById(R.id.foreground_button);
        setButtonText();
    }

    public void setButtonText() {
        if (isDumping) {
            mForegroundButton.setText("Stop Dumping Foreground");
        } else {
            mForegroundButton.setText("Start Dumping Foreground");
        }
    }

    @Override
    public void didPressForegroundButton(View v) {
        mIntent = new Intent(getActivity().getApplicationContext(), ForegroundDumperService.class);
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
            mIntent = new Intent(getActivity().getApplicationContext(), ForegroundDumperService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), ForegroundDumperService.class);
            getActivity().stopService(mIntent);
            isDumping = false;
            setButtonText();
        }
    }
}
