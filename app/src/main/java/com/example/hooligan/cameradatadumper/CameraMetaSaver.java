package com.example.hooligan.cameradatadumper;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.RggbChannelVector;
import android.location.Location;
import android.media.ExifInterface;
import android.util.Log;
import android.util.Pair;
import android.util.Range;
import android.util.Rational;

import com.example.hooligan.DataToFileWriter;
import com.example.hooligan.SensorDataDumperActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by Hooligan on 6/26/2015.
 */
public class CameraMetaSaver implements Runnable {

    private TotalCaptureResult mCaptureResult;
    private File mFile;
    private FileWriter mFileWriter;
    private String imgFilePath;
    private final static String mLogTag = "CameraMetaSaver";
    private Timestamp mTimeStamp;

    public CameraMetaSaver() {

    }

    public CameraMetaSaver(TotalCaptureResult captureResult, boolean front, Timestamp timestamp, String filePath){
        try {
            mCaptureResult = captureResult;
            File parentDir = new File(SensorDataDumperActivity.mParentDir.getPath()
                    + "/camera/meta");
            if (!parentDir.exists()) {
                parentDir.mkdir();
            }
            StringBuilder fileName = front ? new StringBuilder("front") : new StringBuilder("back");
            fileName.append("_" + Long.toString(timestamp.getTime()) + ".txt");
            /*
            mFile = front ?
                    new File(parentDir, "front" + parentDir.list().length + ".txt")
                    : new File(parentDir, "back" + parentDir.list().length + ".txt");
            */
            imgFilePath = filePath;
            mFile = new File(parentDir, fileName.toString());
            mFile.createNewFile();
            mFileWriter = new FileWriter(mFile,false);
        } catch (IOException e) {
            Log.i(mLogTag, "IOException");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Log.i(mLogTag, "run");
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            List<CaptureResult.Key<?>> keys = mCaptureResult.getKeys();
            for (CaptureResult.Key<?> key : keys) {
                String name = key.getName();
                String[] tokens = name.split("\\.");
                String keyName = tokens[tokens.length-1];

                columns.append(keyName + ", ");

                if (keyName.equalsIgnoreCase("testpatterndata")) {
                    values.append("[");
                    int[] datas = (int[]) mCaptureResult.get(key);
                    for (int data : datas) {
                        values.append(Integer.toString(data)+ ", ");
                    }
                    values.append("], ");
                } else {

                    String className = mCaptureResult.get(key).getClass().getName().toLowerCase();

                    if (className.contains("meteringrectangle")) {
                        MeteringRectangle[] mrs = (MeteringRectangle[]) mCaptureResult.get(key);
                        values.append("[");
                        for (MeteringRectangle mr : mrs) {
                            values.append(mr.toString());
                        }
                        values.append("], ");
                    } else if (className.contains("rect")) {
                        values.append(((Rect) mCaptureResult.get(key)).toShortString() + "\t");
                    } else if (className.contains("rational")) {
                        Rational[] rs = (Rational[]) mCaptureResult.get(key);
                        values.append("[");
                        for (Rational r : rs) {
                            values.append(r.toString() + ", ");
                        }
                        values.append("], ");
                    } else if (className.contains("point")) {
                        Point[] pts = (Point[]) mCaptureResult.get(key);
                        values.append("[");
                        for (Point p : pts) {
                            values.append(p.toString());
                        }
                        values.append("], ");
                    } else if (className.contains("face")) {

                    } else if (className.contains("pair")) {
                        Log.i(mLogTag, className);

                        if (className.contains("focus")) {
                            Pair pair = (Pair) mCaptureResult.get(key);
                            values.append("[" + pair.first.toString() + ", " + pair.second.toString() +"], ");
                        }

                        if (className.contains("noise")) {
                            Pair[] pairs = (Pair[]) mCaptureResult.get(key);
                            values.append("[");
                            for (Pair p : pairs) {
                                values.append("(" + p.first.toString() + ", " + p.second.toString() + "),");
                            }
                            values.append("], ");
                        }
                    } else {
                        values.append("[" + mCaptureResult.get(key).toString() + "], ");
                    }
                }
            }

            /*
            // iso, aperture, exposure time, white balance
            ExifInterface exifInterface = new ExifInterface(imgFilePath);
            columns.append("iso, aperture, exposure, time, white balance");

            String iso = exifInterface.getAttribute(ExifInterface.TAG_ISO);
            String aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
            String exposure = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            String wb = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);

            values.append(iso + ", " + aperture + ", " + exposure + ", " + wb);
            */
            columns.append("\r\n");
            //Log.i(mLogTag, columns.toString());
            //Log.i(mLogTag, values.toString());
            mFileWriter.write(columns.toString());
            mFileWriter.flush();
            mFileWriter.write(values.toString());
            mFileWriter.flush();
            mFileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
