package com.example.hooligan.cameradatadumper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;

import com.example.hooligan.Devices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Hooligan on 6/25/2015.
 */
public class ImageSaver implements Runnable {

    /**
     * The JPEG image
     */
    private final Image mImage;
    /**
     * The file we save the image into.
     */
    private final File mFile;
    private boolean mUsingFrontCamera;
    private static String mLogTag = "ImageSaver";

    public ImageSaver(Image image, File file, boolean usingFrontCamera) {
        mImage = image;
        mUsingFrontCamera = usingFrontCamera;
        if (!mUsingFrontCamera && isNexus5()) {
            mFile = new File(file.getPath() + "-flipped.jpg");
        } else {
            mFile = new File(file.getPath() + ".jpg");
        }
    }

    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        /* Rotating takes time
        if (isNexus5() && !mUsingFrontCamera) {
            // Rotating image
            Log.i(mLogTag, "Rotating the image");
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(180);
            Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, false);
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.PNG, 0, blob);
            bytes = blob.toByteArray();
            // end rotation
        }
        */

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

    private static Boolean isNexus5() {
        String deviceName = Devices.getDeviceName();
        return deviceName.toLowerCase().contains("nexus 5");
    }

}