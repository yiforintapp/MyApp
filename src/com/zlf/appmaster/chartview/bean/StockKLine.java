package com.zlf.appmaster.chartview.bean;

import android.content.Context;
import android.util.Log;

import com.zlf.appmaster.utils.ByteUtil;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.StringUtil;
import com.zlf.appmaster.utils.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class StockKLine extends StockVolume {

    public static final int MAX_SHOW_NUM_NORMAL = 60;
    public static final int MAX_SHOW_NUM_BIG = 104;
    public static final int MAX_SCALE_SHOW_NUM = 300;
    public static final int MIN_SCALE_SHOW_NUM = 30;
    public static final long DAY = 0x5265c00L;
    public static final long WEEK = 0x240c8400L;
    public static final long MONTH = 0x9a7ec800L;
    private float a;
    private float b;
    private float c;
    private float d;
    private float e;
    private float f;
    private float g;
    private float h;
    private float i;
    private float j;
    private float k;
    private long l;

    public float getOpen() {
        return a;
    }

    public void setOpen(float f1) {
        a = f1;
    }

    public float getClose() {
        return b;
    }

    public void setClose(float f1) {
        b = f1;
    }

    public float getHigh() {
        return c;
    }

    public void setHigh(float f1) {
        c = f1;
    }

    public float getLow() {
        return d;
    }

    public void setLow(float f1) {
        d = f1;
    }

    public long getDataTime() {
        return l;
    }

    public void setDataTime(long l1) {
        l = l1;
    }

    public float getPreClose() {
        return e;
    }

    public float getPercent() {
        if (e == 0.0F) {
            QLog.e("StockKLine", "mPreClose == 0");
            return 0.0F;
        } else {
            return (b - e) / e;
        }
    }

    public void setPreClose(float f1) {
        e = f1;
    }

    public float getMa5() {
        return f;
    }

    public void setMa5(float f1) {
        f = (f1 + b) / 5F;
    }

    public float getMa10() {
        return g;
    }

    public void setMa10(float f1) {
        g = (f1 + b) / 10F;
    }

    public float getMa20() {
        return h;
    }

    public void setMa20(float f1) {
        h = (f1 + b) / 20F;
    }

    public float getMa30() {
        return i;
    }

    public void setMa30(float f1) {
        i = (f1 + b) / 30F;
    }

    public StockKLine(StockKLine stockkline, boolean flag) {
        this(stockkline.getOpen(), stockkline.getClose(), stockkline.getHigh(), stockkline.getLow(), stockkline.getPreClose(), stockkline.getDataTime(), stockkline.getTradeCount(), flag);
    }

    public StockKLine(float f1, float f2, float f3, float f4, float f5, long l1,
                      long l2, boolean flag) {
        f = -1F;
        g = -1F;
        h = -1F;
        i = -1F;
        j = 1.0F;
        k = 1.0F;
        a = f1;
        b = f2;
        c = f3;
        d = f4;
        e = f5;
        l = l1;
        setTradeCount(l2);
        setUp(flag);
    }

    public String toString() {
        return (new StringBuilder()).append("StockKLine [mOpen=").append(a).append(", mClose=").append(b).append(", mHigh=").append(c).append(", mLow=").append(d).append(", mDataTime=").append(l).append(", getTradeCount()=").append(getTradeCount()).append("]").toString();
    }

    public static int getInt(byte abyte0[], int i1) {
        byte abyte1[] = new byte[4];
        for (int j1 = 0; j1 < 4; j1++)
            abyte1[j1] = abyte0[i1 + j1];

        return ByteUtil.byte2int(abyte1);
    }

    public static long getLong(byte abyte0[], int i1) {
        byte abyte1[] = new byte[8];
        for (int j1 = 0; j1 < 8; j1++)
            abyte1[j1] = abyte0[i1 + j1];

        return ByteUtil.byte2long(abyte1);
    }

    public static float getFloat(byte abyte0[], int i1) {
        byte abyte1[] = new byte[4];
        for (int j1 = 0; j1 < 4; j1++)
            abyte1[j1] = abyte0[i1 + j1];

        return ByteUtil.byte2float(abyte1);
    }

    public static byte[] getKLineBytes(ArrayList arraylist) {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        KLineHeader klineheader = getDefaultKLineHeader();
        for (int i1 = 0; i1 < arraylist.size(); i1++) {
            byte abyte0[] = getItemBytes((StockKLine) arraylist.get(i1), klineheader);
            bytearrayoutputstream.write(abyte0, 0, abyte0.length);
        }

        return StringUtil.gZip(bytearrayoutputstream.toByteArray());
    }

    public static KLineHeader getDefaultKLineHeader() {
        return new KLineHeader(new KLineHeader.KLineKey(0, 3), new KLineHeader.KLineKey(4, 7), new KLineHeader.KLineKey(8, 11), new KLineHeader.KLineKey(12, 15), new KLineHeader.KLineKey(16, 19), new KLineHeader.KLineKey(24, 31), new KLineHeader.KLineKey(20, 23), 31);
    }

    static int ll = 0;
    public static byte[] getItemBytes(StockKLine stockkline, KLineHeader klineheader) {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        ll ++;
        if (ll > 30) {
            Log.e("hfgjfgj", "time:  " + stockkline.l + "");
            Date date = new Date(stockkline.l);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateStr = sdf.format(date);
            Log.e("hfgjfgj", "SimpleDateFormat:  " + dateStr);
            Log.e("hfgjfgj", "low:  " + stockkline.d + "");
            Log.e("hfgjfgj", "high:  " + stockkline.c + "");
            Log.e("hfgjfgj", "open:  " + stockkline.a + "");
            Log.e("hfgjfgj", "close:  " + stockkline.b + "");
            Log.e("hfgjfgj", "preClose:  " + stockkline.e + "");
            Log.e("hfgjfgj", "stockkline.getTradeCount():  " + stockkline.getTradeCount() + "");
            Log.e("hfgjfgj", "------------------------------------");
        }
        bytearrayoutputstream.write(ByteUtil.int2byte((int) (stockkline.l / 1000L)), 0, 4);
        bytearrayoutputstream.write(ByteUtil.float2byte(stockkline.d), 0, 4);
        bytearrayoutputstream.write(ByteUtil.float2byte(stockkline.c), 0, 4);
        bytearrayoutputstream.write(ByteUtil.float2byte(stockkline.a), 0, 4);
        bytearrayoutputstream.write(ByteUtil.float2byte(stockkline.b), 0, 4);
        bytearrayoutputstream.write(ByteUtil.float2byte(stockkline.e), 0, 4);
        bytearrayoutputstream.write(ByteUtil.long2byte(stockkline.getTradeCount()), 0, 8);
        return bytearrayoutputstream.toByteArray();
    }

    public static StockKLine resloveKLineString(byte abyte0[], KLineHeader klineheader) {
        long l1 = (long) getInt(abyte0, klineheader.a.a) * 1000L;
        float f1 = getFloat(abyte0, klineheader.b.a);
        float f2 = getFloat(abyte0, klineheader.c.a);
        float f3 = getFloat(abyte0, klineheader.d.a);
        float f4 = getFloat(abyte0, klineheader.e.a);
        float f5 = getFloat(abyte0, klineheader.g.a);
        long l2 = getLong(abyte0, klineheader.f.a);
        boolean flag = getKLineIsUp(f3, f4, f5);
        return new StockKLine(f3, f4, f2, f1, f5, l1, l2, flag);
    }

    public static void addMaLine(ArrayList arraylist) {
        addMaLine(arraylist, 0);
    }

    public static void addMaLine(ArrayList arraylist, int i1) {
        float f1 = 0.0F;
        float f2 = 0.0F;
        float f3 = 0.0F;
        float f4 = 0.0F;
        float f5 = 0.0F;
        byte byte0 = 5;
        byte byte1 = 10;
        byte byte2 = 20;
        byte byte3 = 30;
        for (int j1 = i1; j1 < arraylist.size(); j1++) {
            StockKLine stockkline = (StockKLine) arraylist.get(j1);
            if (j1 == (byte0 + i1) - 1)
                stockkline.setMa5(f1);
            else if (j1 > (byte0 + i1) - 1) {
                f1 -= ((StockKLine) arraylist.get(j1 - byte0)).getClose();
                stockkline.setMa5(f1);
                if (j1 == (byte1 + i1) - 1)
                    stockkline.setMa10(f2);
                else if (j1 > (byte1 + i1) - 1) {
                    f2 -= ((StockKLine) arraylist.get(j1 - byte1)).getClose();
                    stockkline.setMa10(f2);
                    if (j1 == (byte2 + i1) - 1)
                        stockkline.setMa20(f3);
                    else if (j1 > (byte2 + i1) - 1) {
                        f3 -= ((StockKLine) arraylist.get(j1 - byte2)).getClose();
                        stockkline.setMa20(f3);
                        if (j1 == (byte3 + i1) - 1)
                            stockkline.setMa30(f4);
                        else if (j1 > (byte3 + i1) - 1) {
                            f4 -= ((StockKLine) arraylist.get(j1 - byte3)).getClose();
                            stockkline.setMa30(f4);
                        }
                    }
                }
            }
            float f6 = stockkline.getClose();
            f1 += f6;
            f2 += f6;
            f3 += f6;
            f4 += f6;
        }

    }

    public static ArrayList bytes2KLineList(byte abyte0[], int i1, KLineHeader klineheader) {
        ArrayList arraylist = new ArrayList();
        int j1 = klineheader.h + 1;

        for (int k1 = 0; i1 + j1 <= abyte0.length; k1++) {
            Log.e("dfhdhdfhd", "j1:  "  + j1);
            byte abyte1[] = new byte[j1];
            for (int l1 = 0; l1 < j1; l1++)
                abyte1[l1] = abyte0[i1 + l1];
            try {
                Log.e("dfhdhdfhd", "for:  "  + new String(abyte1));
            } catch (Exception e) {

            }
            StockKLine stockkline = resloveKLineString(abyte1, klineheader);
            arraylist.add(stockkline);
            i1 += j1;
        }
        addMaLine(arraylist);
        return arraylist;
    }


    public static ArrayList resloveKLineFromSql(byte abyte0[], Context context) {
        byte abyte1[] = StringUtil.unGZip(abyte0);
        return bytes2KLineList(abyte1, 0, getDefaultKLineHeader());
    }

    public static ArrayList resolveKLineZip(Object obj, Context context) {
        byte abyte0[] = StringUtil.unGZip((byte[]) (byte[]) obj);

        Log.e("dfhdhdfhd", "abyte0[]:  "  + abyte0);
        if (abyte0 == null || abyte0.length == 0) {
            QLog.e("StockKLine", "没有获取到K线数据");
            return null;
        }
        KLineHeader klineheader = null;
        int i1 = getInt(abyte0, 0);
        if (i1 == 0) {
            QLog.e("StockKLine", "解析头信息出错！");
            return null;
        }
        if ((long) i1 > 0x100000L) {
            QLog.w("StockKLine", "超过1M");
            for (int j1 = 0; j1 < 4; j1++)
                QLog.i("StockKLine", (new StringBuilder()).append(",").append(abyte0[j1]).toString());

            return null;
        }
        byte abyte1[] = new byte[i1];
        for (int k1 = 0; k1 < i1; k1++)
            abyte1[k1] = abyte0[k1 + 4];

        try {
            String s = new String(abyte1, "utf-8");
            Log.e("dfhdhdfhd", "title:  "  + s);
            JSONObject jsonobject = new JSONObject(s);
            klineheader = KLineHeader.resloveJsonObject(jsonobject);
        } catch (JSONException jsonexception) {
            jsonexception.printStackTrace();
        } catch (UnsupportedEncodingException unsupportedencodingexception) {
            unsupportedencodingexception.printStackTrace();
        }
        if (klineheader == null) {
            QLog.e("StockKLine", "解析头信息出错！");
            return null;
        } else {
            int l1 = 4 + i1;
            return bytes2KLineList(abyte0, l1, klineheader);
        }
    }

    public static ArrayList changeDaily2Week(ArrayList arraylist) {
        ArrayList arraylist1 = new ArrayList();
        int i1 = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(2);
        boolean flag = false;
        float f1 = 0.0F;
        int k1;
        for (; i1 < arraylist.size(); i1 += k1) {
            calendar.setTime(new Date(((StockKLine) arraylist.get(i1)).getDataTime()));
            int j1 = calendar.get(3);
            int l1 = 7;
            if (i1 + l1 >= arraylist.size())
                l1 = arraylist.size() - i1;
            k1 = 1;
            do {
                if (k1 >= l1)
                    break;
                calendar.setTime(new Date(((StockKLine) arraylist.get(i1 + k1)).getDataTime()));
                int i2 = calendar.get(3);
                if (j1 != i2)
                    break;
                k1++;
            } while (true);
            StockKLine stockkline = new StockKLine((StockKLine) arraylist.get(i1), true);
            for (int j2 = 1; j2 < k1; j2++) {
                StockKLine stockkline1 = (StockKLine) arraylist.get(i1 + j2);
                if (stockkline.getHigh() < stockkline1.getHigh())
                    stockkline.setHigh(stockkline1.getHigh());
                if (stockkline.getLow() > stockkline1.getLow())
                    stockkline.setLow(stockkline1.getLow());
                stockkline.setTradeCount(stockkline.getTradeCount() + stockkline1.getTradeCount());
            }

            stockkline.setClose(((StockKLine) arraylist.get((i1 + k1) - 1)).getClose());
            stockkline.setDataTime(((StockKLine) arraylist.get((i1 + k1) - 1)).getDataTime());
            boolean flag1 = getKLineIsUp(stockkline.getOpen(), stockkline.getClose(), f1);
            f1 = stockkline.getClose();
            stockkline.setUp(flag1);
            arraylist1.add(stockkline);
        }

        addMaLine(arraylist1);
        return arraylist1;
    }

    public static ArrayList changeDaily2Month(ArrayList arraylist) {
        ArrayList arraylist1 = new ArrayList();
        int i1 = 0;
        Calendar calendar = Calendar.getInstance();
        boolean flag = false;
        float f1 = 0.0F;
        int k1;
        for (; i1 < arraylist.size(); i1 += k1) {
            calendar.setTime(new Date(((StockKLine) arraylist.get(i1)).getDataTime()));
            int j1 = calendar.get(2);
            int l1 = 31;
            if (i1 + l1 >= arraylist.size())
                l1 = arraylist.size() - i1;
            k1 = 1;
            do {
                if (k1 >= l1)
                    break;
                calendar.setTime(new Date(((StockKLine) arraylist.get(i1 + k1)).getDataTime()));
                int i2 = calendar.get(2);
                if (j1 != i2)
                    break;
                k1++;
            } while (true);
            StockKLine stockkline = new StockKLine((StockKLine) arraylist.get(i1), true);
            for (int j2 = 1; j2 < k1; j2++) {
                StockKLine stockkline1 = (StockKLine) arraylist.get(i1 + j2);
                if (stockkline.getHigh() < stockkline1.getHigh())
                    stockkline.setHigh(stockkline1.getHigh());
                if (stockkline.getLow() > stockkline1.getLow())
                    stockkline.setLow(stockkline1.getLow());
                stockkline.setTradeCount(stockkline.getTradeCount() + stockkline1.getTradeCount());
            }

            stockkline.setClose(((StockKLine) arraylist.get((i1 + k1) - 1)).getClose());
            stockkline.setDataTime(((StockKLine) arraylist.get((i1 + k1) - 1)).getDataTime());
            boolean flag1 = getKLineIsUp(stockkline.getOpen(), stockkline.getClose(), f1);
            f1 = stockkline.getClose();
            stockkline.setUp(flag1);
            arraylist1.add(stockkline);
        }

        addMaLine(arraylist1);
        return arraylist1;
    }

    public static ArrayList addKLine(ArrayList arraylist, ArrayList arraylist1) {
        long l1 = ((StockKLine) arraylist1.get(0)).getDataTime();
        String s = TimeUtil.getYearAndDay(l1);
        int i1 = 0;
        do {
            if (i1 >= arraylist.size())
                break;
            long l2 = ((StockKLine) arraylist.get(arraylist.size() - 1)).getDataTime();
            String s1 = TimeUtil.getYearAndDay(l2);
            if (!s1.equals(s))
                break;
            arraylist.remove(arraylist.size() - 1);
            i1++;
        } while (true);
        arraylist.addAll(arraylist1);
        int j1 = arraylist.size();
        i1 = j1 - arraylist1.size() - 30;
        if (i1 < 0)
            i1 = 0;
        addMaLine(arraylist, i1);
        return arraylist;
    }

    public static boolean getKLineIsUp(float f1, float f2, float f3) {
        boolean flag = true;
        if (f2 < f1)
            flag = false;
        else if (f2 > f1)
            flag = true;
        else if (f1 > f3)
            flag = true;
        else if (f1 < f3)
            flag = false;
        return flag;
    }

    public boolean getStockIsUp() {
        return getClose() > getPreClose();
    }

    public static ArrayList getForwardList(ArrayList arraylist) {
        ArrayList arraylist1 = new ArrayList();
        StockKLine stockkline1;
        for (Iterator iterator = arraylist.iterator(); iterator.hasNext(); arraylist1.add(stockkline1)) {
            StockKLine stockkline = (StockKLine) iterator.next();
            stockkline1 = new StockKLine(stockkline.getOpen() * stockkline.getForwardAdjust(), stockkline.getClose() * stockkline.getForwardAdjust(), stockkline.getHigh() * stockkline.getForwardAdjust(), stockkline.getLow() * stockkline.getForwardAdjust(), stockkline.getPreClose() * stockkline.getForwardAdjust(), stockkline.getDataTime(), stockkline.getTradeCount(), stockkline.getKLineIsUp(stockkline.getOpen(), stockkline.getClose(), stockkline.getPreClose()));
        }

        addMaLine(arraylist1);
        return arraylist1;
    }

    public static ArrayList getBackwardList(ArrayList arraylist) {
        ArrayList arraylist1 = new ArrayList();
        StockKLine stockkline1;
        for (Iterator iterator = arraylist.iterator(); iterator.hasNext(); arraylist1.add(stockkline1)) {
            StockKLine stockkline = (StockKLine) iterator.next();
            stockkline1 = new StockKLine(stockkline.getOpen() * stockkline.getBackwardAdjust(), stockkline.getClose() * stockkline.getBackwardAdjust(), stockkline.getHigh() * stockkline.getBackwardAdjust(), stockkline.getLow() * stockkline.getBackwardAdjust(), stockkline.getPreClose() * stockkline.getBackwardAdjust(), stockkline.getDataTime(), stockkline.getTradeCount(), stockkline.getKLineIsUp(stockkline.getOpen(), stockkline.getClose(), stockkline.getPreClose()));
        }

        addMaLine(arraylist1);
        return arraylist1;
    }

    public static void calcAdjust(ArrayList arraylist) {
        float f1 = 1.0F;
        for (int i1 = arraylist.size() - 1; i1 > 0; i1--) {
            ((StockKLine) arraylist.get(i1 - 1)).setForwardAdjust(f1);
            if (((StockKLine) arraylist.get(i1 - 1)).getClose() != ((StockKLine) arraylist.get(i1)).getPreClose()) {
                f1 *= ((StockKLine) arraylist.get(i1)).getPreClose() / ((StockKLine) arraylist.get(i1 - 1)).getClose();
                ((StockKLine) arraylist.get(i1 - 1)).setForwardAdjust(f1);
            }
        }

        float f2 = 1.0F;
        for (int j1 = 1; j1 < arraylist.size(); j1++) {
            ((StockKLine) arraylist.get(j1)).setBackwardAdjust(f2);
            if (((StockKLine) arraylist.get(j1 - 1)).getClose() != ((StockKLine) arraylist.get(j1)).getPreClose()) {
                f2 *= ((StockKLine) arraylist.get(j1 - 1)).getClose() / ((StockKLine) arraylist.get(j1)).getPreClose();
                ((StockKLine) arraylist.get(j1)).setBackwardAdjust(f2);
            }
        }

    }

    public float getForwardAdjust() {
        return j;
    }

    public void setForwardAdjust(float f1) {
        j = f1;
    }

    public float getBackwardAdjust() {
        return k;
    }

    public void setBackwardAdjust(float f1) {
        k = f1;
    }

    public static ArrayList<StockKLine> resloveNewKLineData(Object object) {
        ArrayList<StockKLine> arrayList = new ArrayList<StockKLine>();

        try {
            JSONArray jsonArray = (JSONArray) object;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                float open = (float) jsonObject.optDouble("open");
                float close = (float) jsonObject.optDouble("close");
                float high = (float) jsonObject.optDouble("high");
                float low = (float) jsonObject.optDouble("low");
                long time = jsonObject.optLong("statisticsTime");
                long volume = jsonObject.optLong("volume");
                long count = 100000;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return arrayList;
    }
}