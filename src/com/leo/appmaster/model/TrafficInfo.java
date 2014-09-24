package com.leo.appmaster.model;

public class TrafficInfo {
	private long mReceivedData;
	private long mRransmittedData;

	public long getReceivedData() {
		return mReceivedData;
	}

	public void setReceivedData(long mReceivedData) {
		this.mReceivedData = mReceivedData;
	}

	public long getMtransmittedData() {
		return mRransmittedData;
	}

	public void setTransmittedData(long mTransmittedData) {
		this.mRransmittedData = mTransmittedData;
	}

}
