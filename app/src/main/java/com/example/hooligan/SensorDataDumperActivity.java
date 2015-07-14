package com.example.hooligan;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.MultipleFileUpload;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.regions.Regions;
import com.example.hooligan.accelerometerdatadumper.AccelerometerFragmentInterface;
import com.example.hooligan.airpressuredatadumper.AirPressureFragmentInterface;
import com.example.hooligan.ambientlightdatadumper.AmbientLightFragmentInterface;
import com.example.hooligan.batterydatadumper.BatteryFragmentInterface;
import com.example.hooligan.callstatedumper.CallStateDataFragmentInterface;
import com.example.hooligan.cameradatadumper.CameraFragmentInterface;
import com.example.hooligan.connectivitydatadumper.ConnectivityFragmentInterface;
import com.example.hooligan.foregroundactivitydumper.ForegroundFragmentInterface;
import com.example.hooligan.locationdumper.LocationDumperFragmentInterface;
import com.example.hooligan.magneticdatadumper.MagneticFragmentInterface;
import com.example.hooligan.microphonedumper.MicrophoneFragmentInterface;
import com.example.hooligan.proximitydatadumper.ProximityFragmentInterface;
import com.example.hooligan.rotationdatadumper.RotationFragmentInterface;
import com.example.hooligan.screenstatedumper.ScreenStateFragmentInterface;
import com.example.hooligan.temperaturedatadumper.TemperatureFragmentInterface;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class SensorDataDumperActivity
        extends FragmentActivity
        implements View.OnClickListener {

    public static DataToFileWriter mDataToFileWriter;

    private AccelerometerFragmentInterface acc_fragment;
    private RotationFragmentInterface rot_fragment;
    private CameraFragmentInterface cam_fragment;
    private ConnectivityFragmentInterface conn_fragment;
    private LocationDumperFragmentInterface loc_fragment;
    private EditText mEditText;
    private AmbientLightFragmentInterface ambient_light_fragment;
    //private MicrophoneFragmentInterface microphone_fragment;
    private ForegroundFragmentInterface foreground_fragment;
    private BatteryFragmentInterface battery_fragment;
    private MagneticFragmentInterface magnetic_fragment;
    private TemperatureFragmentInterface temperature_fragment;
    private AirPressureFragmentInterface air_fragment;
    private ProximityFragmentInterface proximity_fragment;
    private CallStateDataFragmentInterface call_fragment;
    private ScreenStateFragmentInterface screen_fragment;

    private Boolean didEnterName = false;

    public static SensorDataDumperActivity mSensorDataDumperActivity;
    public static String mUserName = "";
    public static File mParentDir;
    private Boolean textChanged = false;
    private static String mLogTag = "SensorActivity";

    // Amazon Web Services
    private CognitoCachingCredentialsProvider mCredentialsProvider;
    private static TransferManager mTransferManager;
    private String mBucketName = "umcp.chellapa.activeauthentication";
    private List <File> mQueuedFiles = new ArrayList<>();
    private List <File> mDeleteFiles = new ArrayList<>();

    private Timer mUploadTimer;
    private TimerTask mUploadTimerTask;
    public Handler mHandler;

    private static Integer timePeriod = 1000 * 60 * 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_data_dumper);

        acc_fragment = (AccelerometerFragmentInterface) getFragmentManager().findFragmentById(R.id.accel_fragment);
        rot_fragment = (RotationFragmentInterface) getFragmentManager().findFragmentById(R.id.rotation_fragment);
        cam_fragment = (CameraFragmentInterface) getFragmentManager().findFragmentById(R.id.cam_fragment);
        conn_fragment = (ConnectivityFragmentInterface) getFragmentManager().findFragmentById(R.id.conn_fragment);
        loc_fragment = (LocationDumperFragmentInterface) getFragmentManager().findFragmentById(R.id.loc_fragment);
        ambient_light_fragment = (AmbientLightFragmentInterface) getFragmentManager().findFragmentById(R.id.ambient_light_fragment);
        //microphone_fragment = (MicrophoneFragmentInterface) getFragmentManager().findFragmentById(R.id.microphone_fragment);
        foreground_fragment = (ForegroundFragmentInterface) getFragmentManager().findFragmentById(R.id.foreground_fragment);
        battery_fragment = (BatteryFragmentInterface) getFragmentManager().findFragmentById(R.id.battery_fragment);
        magnetic_fragment = (MagneticFragmentInterface) getFragmentManager().findFragmentById(R.id.magnetic_fragment);
        temperature_fragment = (TemperatureFragmentInterface) getFragmentManager().findFragmentById(R.id.temperature_fragment);
        air_fragment = (AirPressureFragmentInterface) getFragmentManager().findFragmentById(R.id.air_fragment);
        proximity_fragment = (ProximityFragmentInterface) getFragmentManager().findFragmentById(R.id.proximity_fragment);
        call_fragment = (CallStateDataFragmentInterface) getFragmentManager().findFragmentById(R.id.call_fragment);
        screen_fragment = (ScreenStateFragmentInterface) getFragmentManager().findFragmentById(R.id.screen_fragment);

        mEditText = (EditText)findViewById(R.id.name_text_field);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mUserName = s.toString();

                }
                didEnterName = false;
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE ) {
                    Log.i(mLogTag, "Done");
                    // the user is done typing.
                    didEnterName = true;
                    //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                }
                return false; // pass on to other listeners.
            }
        });

        Switch toggleServices = (Switch) findViewById(R.id.toggle_services);

        mSensorDataDumperActivity = this;
        mHandler = new Handler(getMainLooper());

        toggleServices.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (didEnterName) {
                    if (isChecked) {
                        makeDir();
                        turnOnServices();
                        mUploadTimerTask = new TimerTask() {
                            @Override
                            public void run() {
                                if (buttonView.isChecked()) {
                                    Log.i(mLogTag, "Upload timer task");
                                    // Run some code on the main thread
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            turnOffServices();
                                            uploadToAWS();
                                            // Make a delay a little bit so that everything can shut off nicely
                                            mHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    makeDir();
                                                    turnOnServices();
                                                }
                                            }, 2000);
                                        }
                                    });
                                }
                            }
                        };
                        mUploadTimer = new Timer("Upload Timer");
                        mUploadTimer.schedule(mUploadTimerTask, timePeriod, timePeriod);
                    } else {
                        turnOffServices();
                        uploadToAWS();
                        mUploadTimer.cancel();
                        mUploadTimer.purge();
                        mParentDir = null;
                    }
                } else {
                    displayAngryDialog();
                    buttonView.setChecked(false);
                }
            }
        });

        setupAWS();

    }

    private void turnOnServices() {
        // Start All services
        Log.i(mLogTag, "Start all services");
        acc_fragment.turnOnService();
        rot_fragment.turnOnService();
        cam_fragment.turnOnService();
        conn_fragment.turnOnService();
        loc_fragment.turnOnService();
        ambient_light_fragment.turnOnService();
        //microphone_fragment.turnOnService();
        foreground_fragment.turnOnService();
        battery_fragment.turnOnService();
        magnetic_fragment.turnOnService();
        temperature_fragment.turnOnService();
        air_fragment.turnOnService();
        proximity_fragment.turnOnService();
        call_fragment.turnOnService();
        screen_fragment.turnOnService();
    }

    private void turnOffServices() {
        // Stop all services
        Log.i(mLogTag, "Stop all services");
        acc_fragment.turnOffService();
        rot_fragment.turnOffService();
        cam_fragment.turnOffService();
        conn_fragment.turnOffService();
        loc_fragment.turnOffService();
        ambient_light_fragment.turnOffService();
        //microphone_fragment.turnOffService();
        foreground_fragment.turnOffService();
        battery_fragment.turnOffService();
        magnetic_fragment.turnOffService();
        temperature_fragment.turnOffService();
        air_fragment.turnOffService();
        proximity_fragment.turnOffService();
        call_fragment.turnOffService();
        screen_fragment.turnOffService();
    }

    private void setupAWS() {
        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                this.getApplicationContext(), // Context
                "us-east-1:598b05c6-be01-42e5-8fb2-2a18a0d9b673", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        mTransferManager = new TransferManager(mCredentialsProvider);
    }

    private void uploadToAWS() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            try {
                Log.i(mLogTag, "Uploading to AWS");
                Toast.makeText(this, "Uploading to AWS", Toast.LENGTH_SHORT).show();
                mQueuedFiles.add(mParentDir);
                Log.i(mLogTag, "QueuedFiles length: " + Integer.toString(mQueuedFiles.size()));
                for (final File toUpload : mQueuedFiles) {
                    Log.i(mLogTag, "toUpload: " + toUpload.getName());
                    final MultipleFileUpload upload = mTransferManager.uploadDirectory(mBucketName, toUpload.getName(), toUpload, true);
                    upload.addProgressListener(new ProgressListener() {
                        @Override
                        public void progressChanged(ProgressEvent progressEvent) {

                            if (upload.isDone()) {
                                try {
                                    Log.i(mLogTag, "Completed transfer");
                                    // Delete the file
                                    if(deleteRecursively(toUpload)) {
                                        Log.i(mLogTag, "Successfully deleted");
                                    } else {
                                        Log.i(mLogTag, "Failed to delete");
                                    }
                                    mQueuedFiles.remove(toUpload);
                                    Log.i(mLogTag, "Queued Files length:" + Integer.toString(mQueuedFiles.size()));
                                } catch (SecurityException e) {
                                    Log.i(mLogTag, "Security Exception on file delete: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                }

            } catch (AmazonServiceException e) {
                Log.i(mLogTag, "Amazon Service Exception: " + e.getErrorMessage());
            } catch (AmazonClientException e) {
                Log.i(mLogTag, "Amazon Client Exception: " + e.getMessage());
            }
        } else {
            // Add the directory to the queue to be uploaded
            mQueuedFiles.add(mParentDir);
        }
    }

    private boolean deleteRecursively(File dir)
    {
        //Log.d(mLogTag, "DELETEPREVIOUS TOP" + dir.getPath());
        boolean result = true;
        if (dir.isDirectory())
        {
            String[] children = dir.list();

            for (String child : children) {
                File temp = new File(dir,child);

                if (temp.isDirectory())
                {
                    Log.d(mLogTag, "Recursive Call" + temp.getPath());
                    result = result && deleteRecursively(temp);
                }
                else
                {
                    Log.d(mLogTag, "Delete File" + temp.getPath());
                    result = result && temp.delete();
                }

            }
        }

        return result && dir.delete();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accelerometer_data_dumper, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataToFileWriter.closeFile();
    }

    @Override
    public void onClick(View v) {

        if (!didEnterName) {
            displayAngryDialog();
            return;
        }

        if (mParentDir == null) {
            makeDir();
        }

        final int id = v.getId();
        switch (id) {
            case R.id.accel_button:
                acc_fragment.didPressDumpAccelerometerButton(v);
                break;
            case R.id.rotation_button:
                rot_fragment.didPressRotationButton(v);
                break;
            /*case R.id.microphone_button:
                microphone_fragment.didPressMicrophoneButton(v);
                break;*/
            case R.id.cam_button:
                cam_fragment.didPressCameraButton(v);
                break;
            case R.id.location_button:
                loc_fragment.didPressLocationButton(v);
                break;
            case R.id.connectivity_button:
                conn_fragment.didPressConnectivityButton(v);
                break;
            case R.id.ambient_light_button:
                ambient_light_fragment.didPressAmbientButton(v);
                break;
            case R.id.foreground_button:
                foreground_fragment.didPressForegroundButton(v);
                break;
            case R.id.battery_button:
                battery_fragment.didPressBatteryButton(v);
                break;
            case R.id.magnetic_button:
                magnetic_fragment.didPressMagneticButton(v);
                break;
            case R.id.temperature_button:
                temperature_fragment.didPressTemperatureButton(v);
                break;
            case R.id.air_button:
                air_fragment.didPressAirPressureButton(v);
                break;
            case R.id.proximity_button:
                proximity_fragment.didPressProximityButton(v);
                break;
            case R.id.call_button:
                call_fragment.didPressCallButton(v);
                break;
            default:
                break;
        }
    }

    public void makeDir() {
        Date date = new Date();
        String dirPath = mSensorDataDumperActivity.getExternalFilesDir(null).getPath()
                + "/" + mUserName + "_" + Long.toString(new Timestamp(date.getTime()).getTime());
        mParentDir = new File(dirPath);
        if (!mParentDir.exists()) {
            mParentDir.mkdir();
            mParentDir.setWritable(true);
        }
    }

    public void displayAngryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Must input a name")
                .setMessage("Input a name in order to begin collecting data")
                .setCancelable(true).show();
    }

    public static synchronized  void writeLogs(String log) {
        if (mDataToFileWriter == null) {
            mDataToFileWriter = new DataToFileWriter("logFile.txt");
        }
        mDataToFileWriter.writeToFile(log);
    }

}
