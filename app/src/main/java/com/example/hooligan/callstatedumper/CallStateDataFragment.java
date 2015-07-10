package com.example.hooligan.callstatedumper;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.hooligan.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CallStateDataFragment extends Fragment implements CallStateDataFragmentInterface{

    private Boolean isDumping = false;
    Button dumpCallStateButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    private Intent mIntent;

    public CallStateDataFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_call_state_data, container, false);
    }

    @Override
    public void didPressCallButton(View v) {
        mIntent = new Intent(getActivity().getApplicationContext(), CallStateMonitorService.class);

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
        dumpCallStateButton = (Button) getView().findViewById(R.id.call_button);
        setButtonText();
    }

    private void setButtonText() {
        if (isDumping) {
            dumpCallStateButton.setText("Stop Dumping Call Info");
        } else  {
            dumpCallStateButton.setText("Start Dumping Call Info");
        }
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), CallStateMonitorService.class);
            getActivity().startService(mIntent);
            Toast.makeText(getActivity().getApplicationContext(), "Starting Call service", Toast.LENGTH_SHORT).show();
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), CallStateMonitorService.class);
            getActivity().stopService(mIntent);
            isDumping = false;
            setButtonText();
        }
    }
}