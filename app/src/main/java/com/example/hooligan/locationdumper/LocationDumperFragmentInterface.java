package com.example.hooligan.locationdumper;

import android.view.View;

/**
 * Created by Hooligan on 6/4/2015.
 */
public interface LocationDumperFragmentInterface {

    void didPressLocationButton(View v);
    void turnOnService();
    void turnOffService();
}
