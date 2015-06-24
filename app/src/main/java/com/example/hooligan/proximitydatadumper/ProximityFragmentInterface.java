package com.example.hooligan.proximitydatadumper;

import android.view.View;

/**
 * Created by Hooligan on 6/22/2015.
 */
public interface ProximityFragmentInterface {

    void didPressProximityButton(View v);
    void turnOnService();
    void turnOffService();

}
