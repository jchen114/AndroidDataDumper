package com.example.hooligan.magneticdatadumper;

import android.view.View;

/**
 * Created by Hooligan on 6/22/2015.
 */
public interface MagneticFragmentInterface {

    void didPressMagneticButton(View v);
    void turnOnService();
    void turnOffService();

}
