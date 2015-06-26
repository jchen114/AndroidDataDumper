package com.example.hooligan.connectivitydatadumper;


import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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
public class ConnectivityDumperFragment extends Fragment implements ConnectivityFragmentInterface {

    private boolean isDumping = false;
    private Button mConnectivityButton;
    private static String KEY_IS_DUMPING = "KEY_IS_DUMPING";
    Intent mIntent;

    public ConnectivityDumperFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (savedInstanceState != null) {
            isDumping = savedInstanceState.getBoolean(KEY_IS_DUMPING);
        }

        return inflater.inflate(R.layout.fragment_connectivity_dumper, container, false);
    }

    @Override
    public void didPressConnectivityButton(View v) {
        Intent mIntent = new Intent(getActivity().getApplicationContext(), ConnectivityDumperService.class);
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
        mConnectivityButton = (Button)getView().findViewById(R.id.connectivity_button);
        setButtonText();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_DUMPING, isDumping);
    }

    private void setButtonText() {
        if (!isDumping) {
            mConnectivityButton.setText("Start Dumping Connectivity");
        } else {
            mConnectivityButton.setText("Stop Dumping Connectivity");
        }
    }

    @Override
    public void turnOnService() {

        if (!isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), ConnectivityDumperService.class);
            getActivity().startService(mIntent);
            isDumping = true;
            setButtonText();
        }
    }

    @Override
    public void turnOffService() {
        if (isDumping) {
            mIntent = new Intent(getActivity().getApplicationContext(), ConnectivityDumperService.class);
            getActivity().stopService(mIntent);
            isDumping = false;
            setButtonText();
        }
    }

    public void displayEnableDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Must Enable Bluetooth")
                .setMessage("Please enable bluetooth in settings")
                .setCancelable(true).show();
    }

    public void displayAngryDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("No Bluetooth device")
                .setMessage("Cannot start service")
                .setCancelable(true).show();
    }

}
