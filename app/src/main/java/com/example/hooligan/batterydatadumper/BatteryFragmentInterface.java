package com.example.hooligan.batterydatadumper;

import android.view.View;

/**
 * Created by Hooligan on 6/17/2015.
 */
public interface BatteryFragmentInterface {

    void turnOnService();
    void turnOffService();
    void didPressBatteryButton(View v);

}
