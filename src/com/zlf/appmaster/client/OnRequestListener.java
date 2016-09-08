package com.zlf.appmaster.client;


public interface OnRequestListener {
	void onDataFinish(Object object);
	void onError(int errorcode , String errorString);
}
