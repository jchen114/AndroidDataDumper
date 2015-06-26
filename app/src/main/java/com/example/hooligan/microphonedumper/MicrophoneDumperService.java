package com.example.hooligan.microphonedumper;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.hooligan.SensorDataDumperActivity;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MicrophoneDumperService extends Service {

    private static final String mLogTag = "MicDumpService";
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private boolean isRecording = false;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private File mDir;

    public MicrophoneDumperService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Microphone Dumper Service Starting", Toast.LENGTH_SHORT).show();

        mDir = new File(SensorDataDumperActivity.mParentDir, "microphone");
        if (!mDir.exists()) {
            mDir.mkdir();
        }
        isRecording = false;
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isRecording) {
                    // Stop recording
                    Log.i(mLogTag, "Stop recording audio");
                    stopRecording();
                } else {
                    // Start recording
                    Log.i(mLogTag, "Start recording audio");
                    Log.i(mLogTag, "Recording to:" + mFileName);
                    startRecording();
                }
                isRecording = !isRecording;
            }
        };
        mTimer = new Timer("MicrophoneTimer");
        mTimer.schedule(mTimerTask, 0, 10000);
        return START_STICKY;
    }

    private void startRecording() {

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        long time = new Timestamp(new Date().getTime()).getTime();
        mFileName = mDir.getPath() + "/" + Integer.toString(mDir.listFiles().length) + " " + Long.toString(time);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(mLogTag, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(mLogTag, "On Destroy");
        mTimer.cancel();
        mTimer.purge();
        stopRecording();
    }
}
