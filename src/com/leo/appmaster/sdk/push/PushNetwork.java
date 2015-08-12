
package com.leo.appmaster.sdk.push;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;


public class PushNetwork {

    private final static int CONNECTION_TIMEOUT = 5 * 1000;
    private final static int READ_TIMEOUT = 5 * 1000;
    private final static String TAG = "PushNetwork";

    public static void sendHttpReq(BasePushHttpReq req) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(
                    CoreConnectionPNames.CONNECTION_TIMEOUT,
                    CONNECTION_TIMEOUT);
            httpclient.getParams()
                    .setParameter(CoreConnectionPNames.SO_TIMEOUT,
                            READ_TIMEOUT);
            HttpPost httppost = new HttpPost(req.getURL());
            /* transfer Device Info in header for every post request */
            httppost.addHeader("device", SDKWrapper.getEncodedDeviceInfo());
            httppost.setEntity(new UrlEncodedFormEntity(req.getKVData(), HTTP.UTF_8));
            LeoLog.d(TAG, "==[target URL=" + req.getURL() + "]===");
            HttpResponse response;
            response = httpclient.execute(httppost);
            int code = response.getStatusLine().getStatusCode();
            if (code == 200) {
                LeoLog.d(TAG, "[" + req.getURL() + "] receive 200 OK");
                req.onReqSuccessed(EntityUtils.toString(response
                        .getEntity()));
            } else {
                LeoLog.d(TAG, "[" + req.getURL()
                        + "] receive failed with code " + code);
                req.onReqFailed(code,
                        EntityUtils.toString(response.getEntity()));
            }
        } catch (Exception e) {
            LeoLog.e(TAG, "Exception in sendHttpReq e=" + e.getMessage());
            req.onReqFailed(-1, "exception");
        }
    }
}
