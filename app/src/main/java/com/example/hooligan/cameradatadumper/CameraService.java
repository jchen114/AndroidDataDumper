package com.example.hooligan.cameradatadumper;

import android.app.Activity;
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
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.hooligan.SensorDataDumperActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class CameraService extends Service {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    CameraManager mCameraManager;
    String mCameraId;
    private CameraDevice mCameraDevice;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private ImageReader mImageReader;
    private File mFile;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }
    };
    private WindowManager mWindowManager;
    private CameraCaptureSession mCameraCaptureSession;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private static final String mLogTag = "FrontCameraService";
    private File mDir;
    private File mPrivateDir;
    private Boolean mCameraDeviceOpened = false;
    private StreamConfigurationMap map;

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            Log.i(mLogTag, "On Opened");
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            // Capture the image
            beginCapture();
            mCameraDeviceOpened = true;
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

    public CameraService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Toast.makeText(this, "Camera Dumper Service Starting", Toast.LENGTH_SHORT).show();
            Log.i(mLogTag, "OnStartCommand");
            startBackgroundThread();
            mWindowManager = SensorDataDumperActivity.mSensorDataDumperActivity.getWindowManager();
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String dirPath = SensorDataDumperActivity.mSensorDataDumperActivity.getExternalFilesDir(null).getPath() + "/front_camera";
            mDir = new File(dirPath);
            if (!mDir.exists()) {
                mDir.mkdir();
            }

            String[] cameraIds = mCameraManager.getCameraIdList();
            for (String cameraId : cameraIds) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                // We don't use a back facing camera in this sample.
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    continue;
                }
                map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);
                mCameraId = cameraId;
                mPrivateDir = new File(mDir, SensorDataDumperActivity.mUserName);
                if (!mPrivateDir.exists()) {
                    mPrivateDir.mkdir();
                }
                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        // Keep polling the camera
                        if (!mCameraDeviceOpened) {
                            // Open it
                            try {
                                mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                Log.e(mLogTag, "Timer Task Camera Access Exception");
                                Toast.makeText(getApplicationContext(), "Camera Error", Toast.LENGTH_SHORT).show();
                                mCameraDeviceOpened = false;
                                e.printStackTrace();
                            }
                        } else {
                            Log.i(mLogTag, "Camera Device is opened");
                        }
                    }
                };

                mTimer = new Timer(mLogTag);
                mTimer.schedule(mTimerTask, 0, 3500);

                return START_STICKY;
            }
        } catch (CameraAccessException | SecurityException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(mLogTag, sw.toString());
            Toast.makeText(getApplicationContext(), "Camera Error", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Camera Error", Toast.LENGTH_SHORT).show();
        }
        return START_FLAG_RETRY;
    }

    private void beginCapture() {
        mFile = new File(mPrivateDir, Integer.toString(mPrivateDir.listFiles().length) + ".jpg");
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
                            Toast.makeText(getApplicationContext(), "Camera Error", Toast.LENGTH_SHORT).show();
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
        if (mTimer != null) {
            mTimer.cancel();
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

    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (FileNotFoundException e) {
                Log.i(mLogTag, "FileNotFoundException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i(mLogTag, "IOException");
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
