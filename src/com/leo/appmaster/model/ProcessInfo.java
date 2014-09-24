package com.leo.appmaster.model;

public class ProcessInfo extends BaseInfo {

	private int mPid;
	private int mMemory;
	private boolean isCheck;

	public int getId() {
		return mPid;
	}

	public void setId(int id) {
		this.mPid = id;
	}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}

	public int getMemory() {
		return mMemory;
	}

	public void setMemory(int memory) {
		this.mMemory = memory;
	}

}
