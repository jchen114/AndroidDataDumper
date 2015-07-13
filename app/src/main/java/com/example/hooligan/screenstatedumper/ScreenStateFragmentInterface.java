package com.example.hooligan.screenstatedumper;

import android.view.View;

/**
 * Created by Hooligan on 7/13/2015.
 */
public interface ScreenStateFragmentInterface {

    void didPressScreenButton(View v);
    void turnOnService();
    void turnOffService();

}
