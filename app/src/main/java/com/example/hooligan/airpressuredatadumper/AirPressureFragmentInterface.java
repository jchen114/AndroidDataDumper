package com.example.hooligan.airpressuredatadumper;

import android.view.View;

/**
 * Created by Hooligan on 6/22/2015.
 */
public interface AirPressureFragmentInterface {

    void didPressAirPressureButton(View v);
    void turnOnService();
    void turnOffService();

}
