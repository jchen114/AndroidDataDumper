package com.example.hooligan.camerabackdatadumper;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;
import com.example.hooligan.cameradatadumper.CameraService;

/**
 * A simple {@link Fragment} subclass.
 */
public class BackCameraFragment extends Fragment implements BackCameraFragmentInterface{

    private boolean isDumping = false;
    private Button mBackCamButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";

    public BackCameraFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_back_camera, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBackCamButton = (Button) getView().findViewById(R.id.back_cam_button);
        setButtonText();
    }

    @Override
    public void didPressBackCameraButton(View v) {
        Intent backCameraIntent = new Intent(getActivity().getApplicationContext(), BackCameraDumperService.class);
        if (isDumping) {
            Log.i("BackCameraFragment", "Stop Back Camera Service");
            getActivity().stopService(backCameraIntent);
        } else {
            Log.i("BackCameraFragment", "Start Back Camera Service");
            getActivity().startService(backCameraIntent);
        }
        isDumping = !isDumping;
        setButtonText();
    }

    private void setButtonText() {
        if (isDumping) {
            mBackCamButton.setText("Stop Dumping Back Camera");
        } else {
            mBackCamButton.setText("Start Dumping Back Camera");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_DUMPING, isDumping);
    }

}
