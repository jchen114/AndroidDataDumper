package com.example.hooligan;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Hooligan on 6/15/2015.
 */
public class DataToFileWriter {

    private File mFile;
    private FileWriter mFileWriter;
    private static final String mLogTag = "DataToFileWriter";

    public DataToFileWriter(String fileName) {
        try {

            mFile = new File(SensorDataDumperActivity.mParentDir, fileName);
            mFile.createNewFile();

            if (!mFile.canWrite()) {
                mFile.setWritable(true);
            }
            mFileWriter = new FileWriter(mFile, true);
            Log.i(mLogTag, mFile.getPath());
        } catch (IOException | NullPointerException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public boolean writeToFile(String toWrite, Boolean timestamp) {
        try {
            Date date = new Date();
            if (mFileWriter == null) {
                mFileWriter = new FileWriter(mFile, true);
            }
            if (timestamp) {
                mFileWriter.append(new Timestamp(date.getTime()).getTime() + "\t" + toWrite + "\r\n");
            } else {
                mFileWriter.append(toWrite + "\r\n");
            }
            mFileWriter.flush();
        } catch (IOException | NullPointerException e) {
            Log.i(mLogTag, "Error writing to file");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeToFile(String toWrite) {
        try {
            Date date = new Date();
            if (mFileWriter == null) {
                mFileWriter = new FileWriter(mFile, true);
            }
            mFileWriter.append(new Timestamp(date.getTime()).getTime() + "\t" + toWrite + "\r\n");
            mFileWriter.flush();
        } catch (IOException | NullPointerException e) {
            Log.i(mLogTag, "Error writing to file");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean closeFile() {
        try {
            mFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
