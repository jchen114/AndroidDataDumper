package com.example.hooligan.cameradatadumper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import com.example.hooligan.DataToFileWriter;
import com.example.hooligan.SensorDataDumperActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class FrontBackCameraService_2 extends Service {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    CameraManager mCameraManager;
    String mFrontCameraId;
    String mBackCameraId;
    private CameraDevice mCameraDevice;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private ImageReader mImageReader;
    private File mFile;
    private Boolean mUsingFrontCamera = false;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile, mUsingFrontCamera));
        }
    };
    private WindowManager mWindowManager;
    private CameraCaptureSession mCameraCaptureSession;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private static final String mLogTag = "FBCameraService_2";
    private File mDir;
    private Boolean mCameraDeviceOpened = false;
    private StreamConfigurationMap map;
    private Timestamp mTimeStamp;


    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            Log.i(mLogTag, "On Opened");
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            // Capture the image
            mCameraDeviceOpened = true;
            beginCapture();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Log.i(mLogTag, "onClosed");
            mCameraDeviceOpened = false;
            super.onClosed(camera);
            try {
                // switch to the Front camera now
                if (mUsingFrontCamera) {
                    mCameraManager.openCamera(mFrontCameraId, mStateCallback, mBackgroundHandler);
                }
            } catch (CameraAccessException e) {
                Log.e(mLogTag, "Error accessing front camera");
                e.printStackTrace();
            }

        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            Log.e(mLogTag, "onError");
            switch (error) {
                case ERROR_CAMERA_DEVICE:
                    Log.e(mLogTag, "Camera device error. needs to be reopened");
                    break;
                case ERROR_CAMERA_DISABLED:
                    Log.e(mLogTag, "Camera could not be opened due to device policy");
                    break;
                case ERROR_CAMERA_IN_USE:
                    Log.e(mLogTag, "Camera device is already in use");
                    break;
                case ERROR_MAX_CAMERAS_IN_USE:
                    Log.e(mLogTag, "There are too many other open camera devices");
                    break;
            }
            mCameraDeviceOpened = false;
            mCameraOpenCloseLock.release();
            closeCamera();
        }
    };

    public FrontBackCameraService_2() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.i(mLogTag, "OnStartCommand");
            startBackgroundThread();
            mWindowManager = SensorDataDumperActivity.mSensorDataDumperActivity.getWindowManager();
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String dirPath = SensorDataDumperActivity.mParentDir.getPath()
                    + "/camera";
            mDir = new File(dirPath);
            if (!mDir.exists()) {
                mDir.mkdir();
            }

            String[] cameraIds = mCameraManager.getCameraIdList();
            for (String cameraId : cameraIds) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                // We don't use a back facing camera in this sample.
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    mBackCameraId = cameraId;
                    continue;
                }
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    mFrontCameraId = cameraId;
                }
                map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);
                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        // Keep polling the camera
                        if (!mCameraDeviceOpened) {
                            // Open it
                            try {
                                if (!mUsingFrontCamera) {
                                    // Begin with the back camera
                                    mCameraManager.openCamera(mBackCameraId, mStateCallback, mBackgroundHandler);
                                }
                            } catch (CameraAccessException e) {
                                Log.e(mLogTag, "Timer Task Camera Access Exception");
                                mCameraDeviceOpened = false;
                                e.printStackTrace();
                            }
                        }
                    }
                };

                mTimer = new Timer(mLogTag);
                mTimer.schedule(mTimerTask, 0, 6000);

                return START_STICKY;
            }
        } catch (CameraAccessException | SecurityException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(mLogTag, sw.toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return START_FLAG_RETRY;
    }

    private void beginCapture() {

        StringBuilder fileName;
        fileName = mUsingFrontCamera ?
                new StringBuilder("front")
                : new StringBuilder("back");

        /*
        fileName.append(Integer.toString(mDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        }).length));
        */
        Date date = new Date();
        mTimeStamp = new Timestamp(date.getTime());
        fileName.append("_" + Long.toString(mTimeStamp.getTime()));
        mFile = new File(mDir, fileName.toString());
        try {

            mCameraDevice.createCaptureSession(
                    Arrays.asList(mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }
                            // When the session is ready, we start displaying the preview.
                            mCameraCaptureSession = cameraCaptureSession;
                            Log.i(mLogTag, "Create Capture Session. OnConfigured");
                            createCaptureRequest();
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            Log.i(mLogTag, "Failed Camera Capture Session");
                            closeCamera();
                        }
                    },
                    mBackgroundHandler
            );
        } catch (CameraAccessException e) {
            Log.e(mLogTag, "Camera Access Exception");
            e.printStackTrace();
        }
    }


    private void createCaptureRequest() {
        try {

            // For still image captures, we use the largest available size.

            CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            requestBuilder.addTarget(mImageReader.getSurface());

            // Focus
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Orientation
            int rotation = mWindowManager.getDefaultDisplay().getRotation();
            requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            CameraCaptureSession.CaptureCallback captureCallback
                    = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                               TotalCaptureResult result) {
                    Log.i(mLogTag, "File written to: " + mFile.getPath());
                    closeCamera();
                    Thread cameraMetaSaver = new Thread(new CameraMetaSaver(result, mUsingFrontCamera, mTimeStamp, mFile.getPath()));
                    cameraMetaSaver.start();
                    mUsingFrontCamera = !mUsingFrontCamera;
                }
            };
            Log.i(mLogTag, "Camera Capture Session");
            mCameraCaptureSession.capture(requestBuilder.build(), captureCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Log.i(mLogTag, "On Destroy");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        stopBackgroundThread();
        closeCamera();
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("FrontCameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraCaptureSession) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}
