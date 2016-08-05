package com.zlf.appmaster.model.combination;

import java.io.Serializable;

/**
 * Created by yu on 15-1-30.
 */
public class CombinationGain implements Serializable,Comparable {
    private float gain;
    private long time;

    public CombinationGain(float gain, long time) {
        this.gain = gain;
        this.time = time;
    }

    public String getGainFormat() {
        return String.format("%.2f%%",gain*100);
    }

    public String getValueFormat() {
        return String.format("%.4f",gain+1);
    }
    public float getGain() {
        return gain;
    }

    public long getTime() {
        return time;
    }

    //从小到大
    @Override
    public int compareTo(Object another) {
        long otherTime = ((CombinationGain)another).getTime();
        return getTime() < otherTime ? -1 : (getTime() == otherTime ? 0 : 1);
    }
}
