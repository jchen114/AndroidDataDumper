package com.example.hooligan.accelerometerdatadumper;

import android.view.View;

/**
 * Created by Hooligan on 5/28/2015.
 */
public interface AccelerometerFragmentInterface {

    void turnOnService();
    void turnOffService();
    void didPressDumpAccelerometerButton(View v);

}
