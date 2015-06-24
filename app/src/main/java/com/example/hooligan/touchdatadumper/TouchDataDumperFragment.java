package com.example.hooligan.touchdatadumper;


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
public class TouchDataDumperFragment extends Fragment implements TouchFragmentInterface{

    private boolean isDumping;
    Button touchButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";

    public TouchDataDumperFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_touch_data_dumper, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        touchButton = (Button) getView().findViewById(R.id.touch_button);
    }

    @Override
    public void didPressTouchButton(View v) {
        Intent touchIntent = new Intent(getActivity().getApplicationContext(), TouchDataDumperService.class);
        if (!isDumping) {
            getActivity().startService(touchIntent);
        } else {
            getActivity().stopService(touchIntent);
        }
        isDumping = !isDumping;
        setButtonText();
    }

    private void setButtonText() {
        if (isDumping) {
            touchButton.setText("Stop Dumping Touch");
        } else {
            touchButton.setText("Start Dumping Touch");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_DUMPING, isDumping);
    }

}
