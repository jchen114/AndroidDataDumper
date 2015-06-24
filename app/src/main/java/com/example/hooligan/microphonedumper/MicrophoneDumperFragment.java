package com.example.hooligan.microphonedumper;


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
public class MicrophoneDumperFragment extends Fragment implements MicrophoneFragmentInterface{

    private Boolean isDumping = false;
    Button dumpMicButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public MicrophoneDumperFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_microphone_dumper, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        dumpMicButton = (Button) getView().findViewById(R.id.microphone_button);
        setButtonText();
    }

    @Override
    public void didPressMicrophoneButton(View v) {
        mIntent = new Intent(getActivity().getApplicationContext(), MicrophoneDumperService.class);
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
            dumpMicButton.setText("Stop Dumping Microphone");
        } else {
            dumpMicButton.setText("Start Dumping Microphone");
        }
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), MicrophoneDumperService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), MicrophoneDumperService.class);
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
