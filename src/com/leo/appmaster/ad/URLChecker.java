
package com.leo.appmaster.ad;

import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;


public class URLChecker {
	
	private final static int MAX_URL_CHECK_TIMES = 1;
	
	private final static int URL_CHECK_TIMEMOUT = 10 * 1000;
    
    private final static String TAG = "URLChecker";

    public interface URLCheckCallback {
        public void onURLAvailable();
        public void onURLUnavailable();
    }

    public static void checkURL(final String urlStr, final URLCheckCallback callback) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                int counts = 0;
                URL url = null;
                HttpURLConnection con = null;
                int state = -1;
                while (counts < MAX_URL_CHECK_TIMES) {
                    try {
                        url = new URL(urlStr);
                        con = (HttpURLConnection) url.openConnection();
                        con.setConnectTimeout(URL_CHECK_TIMEMOUT);
                        state = con.getResponseCode();
                        Log.d(TAG, "[" +urlStr+ "]state = " + state);
                        break;
                    } catch (Exception ex) {
                        counts++;
                        Log.d(TAG, "[" +urlStr+ "] unavailable, try " + counts + " times");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
                if (con != null) {
                    con.disconnect();
                }
                if(callback != null){
                	if(state > 0){
                		callback.onURLAvailable();
                	}else{
                		callback.onURLUnavailable();
                	}
                }
            }
        }).start();
    }
}
