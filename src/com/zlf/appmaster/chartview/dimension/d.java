package com.zlf.appmaster.chartview.dimension;

import java.util.ArrayList;

public class d {

    ArrayList a;
    int b;
    int c;
    int d;
    boolean e;

    public d() {
        e = true;
    }

    public d a() {
        d d1 = new d();
        d1.a = (ArrayList) a.clone();
        d1.b = b;
        d1.c = c;
        d1.d = d;
        d1.e = e;
        return d1;
    }
}