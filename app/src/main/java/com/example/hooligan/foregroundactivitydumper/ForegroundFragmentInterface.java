package com.example.hooligan.foregroundactivitydumper;

import android.view.View;

/**
 * Created by Hooligan on 6/17/2015.
 */
public interface ForegroundFragmentInterface {

    void didPressForegroundButton(View v);
    void turnOnService();
    void turnOffService();

}
