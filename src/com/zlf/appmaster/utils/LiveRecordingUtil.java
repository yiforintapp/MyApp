package com.zlf.appmaster.utils;

/**
 * Created by liuhm on 2016/5/11.
 */
public class LiveRecordingUtil {
    private boolean isStock;
    private String stockCode;
    private String stockName;
    private boolean mute;
    private boolean liveRecording;
    private int recordingMode;
    private static LiveRecordingUtil mInstance;
    public static final int TYPE_STOCK = 0;
    public static final int TYPE_INDEX= 1;

    public LiveRecordingUtil() {
    }

    public static LiveRecordingUtil getInstance() {
        if (mInstance == null) {
            mInstance = new LiveRecordingUtil();
        }
        return mInstance;
    }


    public boolean isStock() {
        return isStock;
    }

    public void setStock(boolean stock) {
        isStock = stock;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isLiveRecording() {
        return liveRecording;
    }

    public void setLiveRecording(boolean liveRecording) {
        this.liveRecording = liveRecording;
    }

    public int getRecordingMode() {
        return recordingMode;
    }

    public void setRecordingMode(int recordingMode) {
        this.recordingMode = recordingMode;
    }
}
