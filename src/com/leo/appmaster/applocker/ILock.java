package com.leo.appmaster.applocker;

public interface ILock {

	void onActivityCreate();
	
	void onActivityRestart();
	
	void onActivityResault(int requestCode, int resultCode);
	
}
