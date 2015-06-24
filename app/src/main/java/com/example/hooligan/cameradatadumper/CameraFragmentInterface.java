package com.example.hooligan.cameradatadumper;

import android.view.View;

/**
 * Created by Hooligan on 6/1/2015.
 */
public interface CameraFragmentInterface {

    void turnOnService();
    void turnOffService();
    void didPressCameraButton(View v);

}
