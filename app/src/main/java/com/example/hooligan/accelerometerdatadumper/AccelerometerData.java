package com.example.hooligan.accelerometerdatadumper;

import android.util.Log;
import android.util.Pair;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hooligan on 5/28/2015.
 */
public class AccelerometerData {

    private List<Pair<ArrayList<Float>, Timestamp>> mDataList = new ArrayList<>();
    private String mFileName = "";

    public AccelerometerData(String fileName) {
        this.mFileName = fileName;
    }

    public void putData(ArrayList<Float> accelerations, Timestamp ts) {
        mDataList.add(new Pair(accelerations, ts));
    }

    public void dumpData() {
        for (Pair <ArrayList<Float>, Timestamp> acc_data : mDataList) {
            // Print or Dump here
            ArrayList<Float> accs = acc_data.first;
            Timestamp ts = acc_data.second;
            String toDump = String.format("%d: x: %f, y: %f, z: %f", ts.getNanos(), accs.get(0), accs.get(1), accs.get(2));
            Log.i("AccelerometerData", toDump);
        }
    }

}
