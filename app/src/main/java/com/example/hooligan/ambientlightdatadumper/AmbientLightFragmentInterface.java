package com.example.hooligan.ambientlightdatadumper;

import android.view.View;

/**
 * Created by Hooligan on 6/10/2015.
 */
public interface AmbientLightFragmentInterface {

    void turnOnService();
    void turnOffService();
    void didPressAmbientButton(View v);
}
