package com.example.hooligan.callstatedumper;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.hooligan.DataToFileWriter;

public class CallStateMonitorService extends Service {

    private static String mLogTag = "CallStateService";
    private DataToFileWriter mDataToFileWriter;

    private BroadcastReceiver mPhoneStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(new CustomPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Call state starting", Toast.LENGTH_SHORT).show();
        IntentFilter filterCall = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mPhoneStateBroadcastReceiver, filterCall);
        mDataToFileWriter = new DataToFileWriter("Call-state.txt");
        mDataToFileWriter.writeToFile("Time, State", false);
        mDataToFileWriter.writeToFile("Idle");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPhoneStateBroadcastReceiver);
        mDataToFileWriter.closeFile();
    }

    public CallStateMonitorService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class CustomPhoneStateListener extends PhoneStateListener {

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
                    //Toast.makeText(context, "Phone state Idle", Toast.LENGTH_SHORT).show();
                    Log.i(mLogTag, "Idle");
                    mDataToFileWriter.writeToFile("Idle");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //when Off hook i.e in call
                    //Make intent and start your service here
                    //Toast.makeText(context, "Phone state Off hook", Toast.LENGTH_SHORT).show();
                    mDataToFileWriter.writeToFile("In call");
                    Log.i(mLogTag, "In call");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //when Ringing
                    //Toast.makeText(context, "Phone state Ringing", Toast.LENGTH_SHORT).show();
                    Log.i(mLogTag, "Ringing");
                    mDataToFileWriter.writeToFile("Ringing");
                    break;
                default:
                    break;
            }
        }
    }

}

