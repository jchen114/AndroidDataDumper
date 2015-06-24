package com.example.hooligan.callstatedumper;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class CustomPhoneStateListener extends PhoneStateListener {

    //private static final String TAG = "PhoneStateChanged";
    Context context; //Context to make Toast if required
    public CustomPhoneStateListener(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                //when Idle i.e no call
                Toast.makeText(context, "Phone state Idle", Toast.LENGTH_LONG).show();
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //when Off hook i.e in call
                //Make intent and start your service here
                Toast.makeText(context, "Phone state Off hook", Toast.LENGTH_LONG).show();
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                //when Ringing
                Toast.makeText(context, "Phone state Ringing", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }
}
