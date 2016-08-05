package com.zlf.appmaster.chartview.bean;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StockKDJ {

    private float a;
    private float b;
    private float c;

    public StockKDJ() {
    }

    public static List getDataByStockKLine(List list) {
        ArrayList arraylist = new ArrayList();
        float f = 0.0F;
        float f2 = 3.402823E+038F;
        float f3 = 1.401298E-045F;
        float f4 = 0.0F;
        StockKDJ stockkdj = new StockKDJ();
        stockkdj.setK(0.0F);
        stockkdj.setD(0.0F);
        StockKDJ stockkdj1;
        for (Iterator iterator = list.iterator(); iterator.hasNext(); arraylist.add(stockkdj1)) {
            StockKLine stockkline = (StockKLine) iterator.next();
            stockkdj1 = new StockKDJ();
            float f1 = stockkline.getClose();
            if (stockkline.getLow() < f2)
                f2 = stockkline.getLow();
            if (stockkline.getHigh() > f3)
                f3 = stockkline.getHigh();
            if (f3 == f2)
                f2 -= 1E-007F;
            float f5 = ((f1 - f2) / (f3 - f2)) * 100F;
            stockkdj1.a = 0.6666667F * stockkdj.getK() + 0.3333333F * f5;
            stockkdj1.b = 0.6666667F * stockkdj.getD() + 0.3333333F * stockkdj1.a;
            stockkdj1.c = 3F * stockkdj1.a - 2.0F * stockkdj1.b;
            stockkdj = stockkdj1;
        }

        return arraylist;
    }

    public float getK() {
        return a;
    }

    public void setK(float f) {
        a = f;
    }

    public float getD() {
        return b;
    }

    public void setD(float f) {
        b = f;
    }

    public float getJ() {
        return c;
    }

    public void setJ(float f) {
        c = f;
    }
}