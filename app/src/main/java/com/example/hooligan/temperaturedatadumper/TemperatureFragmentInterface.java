package com.example.hooligan.temperaturedatadumper;

import android.view.View;

/**
 * Created by Hooligan on 6/22/2015.
 */
public interface TemperatureFragmentInterface {

    void didPressTemperatureButton(View v);
    void turnOnService();
    void turnOffService();

}
