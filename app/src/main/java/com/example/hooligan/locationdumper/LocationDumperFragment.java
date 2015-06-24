package com.example.hooligan.locationdumper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.hooligan.R;

public class LocationDumperFragment extends Fragment implements LocationDumperFragmentInterface{

    private boolean isDumping = false;
    private Button mLocationButton;
    private static final String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    public static LocationDumperFragment mLocationDumperFragment;
    Intent mIntent;

    public LocationDumperFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }
        return inflater.inflate(R.layout.fragment_location_dumper, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationDumperFragment = this;
        mLocationButton = (Button) getView().findViewById(R.id.location_button);
        setButtonText();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void didPressLocationButton(View v) {
        mIntent = new Intent(getActivity().getApplicationContext(), DumpLocationRunnableService.class);
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
            mLocationButton.setText("Stop Dumping Location");
        } else {
            mLocationButton.setText("Start Dumping Location");
        }
    }

    public void connectionRefused() {
        isDumping = false;
        setButtonText();
    }

    @Override
    public void turnOnService() {
        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), DumpLocationRunnableService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), DumpLocationRunnableService.class);
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
