package com.example.hooligan.microphonedumper;

import android.view.View;

/**
 * Created by Hooligan on 6/11/2015.
 */
public interface MicrophoneFragmentInterface {

    void didPressMicrophoneButton(View v);
    void turnOnService();
    void turnOffService();

}
