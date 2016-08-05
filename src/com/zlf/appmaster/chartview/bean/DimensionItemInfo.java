package com.zlf.appmaster.chartview.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class DimensionItemInfo
        implements Serializable, Comparable {

    private String a;
    private String b;
    private float c;
    private float d;
    private long e;
    private String f;
    private int g;

    public DimensionItemInfo() {
        g = 0;
    }

    public int getNo() {
        return g;
    }

    public void setNo(int i) {
        g = i;
    }

    public long getTime() {
        return e;
    }

    public void setTime(long l) {
        e = l;
    }

    public String getKey() {
        return a;
    }

    public void setKey(String s) {
        a = s;
    }

    public String getName() {
        return b;
    }

    public void setName(String s) {
        b = s;
    }

    public float getAverage() {
        return c;
    }

    public void setAverage(float f1) {
        c = f1;
    }

    public float getScore() {
        return d;
    }

    public void setScore(float f1) {
        d = f1;
    }

    public String getShowNumString() {
        return f;
    }

    public void setShowNumString(String s) {
        f = s;
    }

    public static DimensionItemInfo resloveItemInfo(JSONObject jsonobject) {
        DimensionItemInfo dimensioniteminfo = new DimensionItemInfo();
        try {
            dimensioniteminfo.setScore((float) jsonobject.optDouble("value"));
            dimensioniteminfo.setTime(jsonobject.optLong("date"));
            dimensioniteminfo.setShowNumString(jsonobject.optString("showStr"));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return dimensioniteminfo;
    }

    public static ArrayList resloveCompareLineData(JSONObject jsonobject) {
        ArrayList arraylist = new ArrayList();
        try {
            JSONObject jsonobject1 = jsonobject.getJSONObject("data");
            JSONObject jsonobject2 = jsonobject1.getJSONObject("datas");
            String s = jsonobject1.getString("self");
            JSONArray jsonarray = jsonobject2.getJSONArray(s);
            for (int j = 0; j < jsonarray.length(); j++) {
                DimensionItemInfo dimensioniteminfo = new DimensionItemInfo();
                JSONObject jsonobject3 = jsonarray.getJSONObject(j);
                dimensioniteminfo.setScore((float) jsonobject3.optDouble("value"));
                dimensioniteminfo.setTime(jsonobject3.optLong("date"));
                arraylist.add(dimensioniteminfo);
            }

            Collections.sort(arraylist);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (arraylist.size() > 10) {
            ArrayList arraylist1 = new ArrayList();
            for (int i = 0; i < 10; i++)
                arraylist1.add(arraylist.get((arraylist.size() - 10) + i));

            return arraylist1;
        } else {
            return arraylist;
        }
    }

    public static ArrayList resloveOneCompareLineData(JSONObject jsonobject, String s) {
        ArrayList arraylist = new ArrayList();
        try {
            JSONArray jsonarray = jsonobject.getJSONObject("data").getJSONArray(s);
            for (int i = 0; i < jsonarray.length(); i++) {
                DimensionItemInfo dimensioniteminfo = new DimensionItemInfo();
                JSONObject jsonobject1 = jsonarray.getJSONObject(i);
                dimensioniteminfo.setScore((float) jsonobject1.optDouble("value"));
                dimensioniteminfo.setTime(jsonobject1.optLong("date"));
                arraylist.add(dimensioniteminfo);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (arraylist != null && arraylist.size() > 0)
            Collections.sort(arraylist);
        return arraylist;
    }

    public int compareTo(Object obj) {
        DimensionItemInfo dimensioniteminfo = (DimensionItemInfo) obj;
        if (g == dimensioniteminfo.getNo()) {
            if (e > dimensioniteminfo.getTime())
                return 1;
            return e >= dimensioniteminfo.getTime() ? 0 : -1;
        }
        return g <= dimensioniteminfo.getNo() ? -1 : 1;
    }
}
