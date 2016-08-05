package com.zlf.appmaster.chartview.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class DimensionChartInfo
        implements Serializable, Comparable {

    private String a;
    private ArrayList b;
    private String c;
    private String d;
    private int e;
    private int f;
    private int g;

    public DimensionChartInfo() {
    }

    public void setSubItemList(ArrayList arraylist) {
        b = arraylist;
    }

    public ArrayList getSubItemList() {
        return b;
    }

    public void setColor(int i, int j) {
        f = i;
        g = j;
    }

    public int getBaseColor() {
        return f;
    }

    public int getCompareColor() {
        return g;
    }

    public String getTitle() {
        return a;
    }

    public void setTitle(String s) {
        a = s;
    }

    public String getType() {
        return c;
    }

    public void setType(String s) {
        c = s;
    }

    public String getDesc() {
        return d;
    }

    public void setDesc(String s) {
        d = s;
    }

    public static ArrayList resloveArrayList(JSONObject jsonobject) {
        ArrayList arraylist = new ArrayList();
        try {
            JSONObject jsonobject1 = jsonobject.getJSONObject("data");
            JSONObject jsonobject2 = jsonobject1.getJSONObject("data");
            HashMap hashmap = new HashMap();
            JSONObject jsonobject3 = jsonobject1.getJSONObject("avgData");
            String s;
            for (Iterator iterator = jsonobject3.keys(); iterator.hasNext(); hashmap.put(s, Float.valueOf((float) jsonobject3.getJSONObject(s).optDouble("value"))))
                s = (String) iterator.next();

            JSONArray jsonarray = jsonobject1.getJSONArray("charts");
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject4 = jsonarray.getJSONObject(i);
                int j = jsonobject4.optInt("sort", 1) - 1;
                DimensionChartInfo dimensionchartinfo = new DimensionChartInfo();
                dimensionchartinfo.setType(jsonobject4.optString("chartId"));
                dimensionchartinfo.setTitle(jsonobject4.optString("name"));
                dimensionchartinfo.setDesc(jsonobject4.optString("description"));
                dimensionchartinfo.setSortNo(j);
                dimensionchartinfo.setColor(jsonobject4.optInt("color1"), jsonobject4.optInt("color2"));
                JSONArray jsonarray1 = jsonobject4.optJSONArray("weiDus");
                ArrayList arraylist1 = new ArrayList();
                for (int k = 0; k < jsonarray1.length(); k++) {
                    JSONObject jsonobject5 = jsonarray1.getJSONObject(k);
                    int l = jsonobject5.optInt("sort", 1) - 1;
                    DimensionItemInfo dimensioniteminfo = new DimensionItemInfo();
                    dimensioniteminfo.setName(jsonobject5.optString("name"));
                    String s1 = jsonobject5.optString("weiDuId");
                    dimensioniteminfo.setScore((float) jsonobject2.getJSONObject(s1).optDouble("value"));
                    dimensioniteminfo.setAverage(((Float) hashmap.get(s1)).floatValue());
                    dimensioniteminfo.setTime(jsonobject5.optLong("date"));
                    dimensioniteminfo.setShowNumString(jsonobject2.getJSONObject(s1).optString("showStr"));
                    dimensioniteminfo.setKey(s1);
                    dimensioniteminfo.setNo(l);
                    arraylist1.add(dimensioniteminfo);
                }

                if (arraylist1 != null)
                    Collections.sort(arraylist1);
                dimensionchartinfo.setSubItemList(arraylist1);
                arraylist.add(dimensionchartinfo);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (arraylist != null && arraylist.size() > 0)
            Collections.sort(arraylist);
        return arraylist;
    }

    public static HashMap resloveCompareInfo(JSONObject jsonobject) {
        HashMap hashmap = new HashMap();
        try {
            JSONObject jsonobject1 = jsonobject.getJSONObject("data").getJSONObject("data");
            String s;
            for (Iterator iterator = jsonobject1.keys(); iterator.hasNext(); hashmap.put(s, DimensionItemInfo.resloveItemInfo(jsonobject1.getJSONObject(s))))
                s = (String) iterator.next();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return hashmap;
    }

    public int getSortNo() {
        return e;
    }

    public void setSortNo(int i) {
        e = i;
    }

    public int compareTo(Object obj) {
        DimensionChartInfo dimensionchartinfo = (DimensionChartInfo) obj;
        if (e > dimensionchartinfo.getSortNo())
            return 1;
        return e >= dimensionchartinfo.getSortNo() ? 0 : -1;
    }
}