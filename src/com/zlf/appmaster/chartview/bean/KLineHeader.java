package com.zlf.appmaster.chartview.bean;

import com.zlf.appmaster.utils.QLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class KLineHeader {

    KLineKey a;
    KLineKey b;
    KLineKey c;
    KLineKey d;
    KLineKey e;
    KLineKey f;
    KLineKey g;
    int h;

    public String toString() {
        return (new StringBuilder()).append("KLineHeader [time=").append(a).append(", low=").append(b).append(", high=").append(c).append(", open=").append(d).append(", close=").append(e).append(", volume=").append(f).append("]").toString();
    }

    public KLineHeader(KLineKey klinekey, KLineKey klinekey1, KLineKey klinekey2, KLineKey klinekey3, KLineKey klinekey4, KLineKey klinekey5, KLineKey klinekey6,
                       int i) {
        a = klinekey;
        b = klinekey1;
        c = klinekey2;
        d = klinekey3;
        e = klinekey4;
        f = klinekey5;
        h = i;
        g = klinekey6;
    }

    public static KLineKey resloveItemJsonArray(JSONArray jsonarray) {
        KLineKey klinekey = new KLineKey();
        if (null != klinekey) {
            klinekey.a = jsonarray.optInt(0);
            klinekey.b = jsonarray.optInt(1);
        }
        return klinekey;
    }

    public static KLineHeader resloveJsonObject(JSONObject jsonobject) {
        int i = 0;
        Iterator iterator = jsonobject.keys();
        do {
            if (!iterator.hasNext())
                break;
            String s = (String) iterator.next();
            JSONArray jsonarray = jsonobject.optJSONArray(s);
            if (jsonarray != null) {
                int j = jsonarray.optInt(1);
                QLog.i("KLineHeader", (new StringBuilder()).append("max:").append(j).toString());
                if (i < j)
                    i = j;
            }
        } while (true);
        KLineKey klinekey = resloveItemJsonArray(jsonobject.optJSONArray("time"));
        KLineKey klinekey1 = resloveItemJsonArray(jsonobject.optJSONArray("low"));
        KLineKey klinekey2 = resloveItemJsonArray(jsonobject.optJSONArray("high"));
        KLineKey klinekey3 = resloveItemJsonArray(jsonobject.optJSONArray("open"));
        KLineKey klinekey4 = resloveItemJsonArray(jsonobject.optJSONArray("close"));
        KLineKey klinekey5 = resloveItemJsonArray(jsonobject.optJSONArray("volume"));
        KLineKey klinekey6 = resloveItemJsonArray(jsonobject.optJSONArray("preClose"));
        return new KLineHeader(klinekey, klinekey1, klinekey2, klinekey3, klinekey4, klinekey5, klinekey6, i);
    }

    public static class KLineKey {

        int a;
        int b;

        public String toString() {
            return (new StringBuilder()).append("(").append(a).append(",").append(b).append(")").toString();
        }

        public KLineKey() {
        }

        public KLineKey(int i, int j) {
            a = i;
            b = j;
        }
    }

}