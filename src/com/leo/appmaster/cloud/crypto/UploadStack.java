package com.leo.appmaster.cloud.crypto;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HttpStack;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jasper on 2015/12/10.
 */
public class UploadStack implements HttpStack {
    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        String url = request.getUrl();
        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);

        URL requestUrl = new URL(url);
        HttpURLConnection connection = openConnection(requestUrl, request);
        for (String headerName : map.keySet()) {
            connection.addRequestProperty(headerName, map.get(headerName));
        }
        return null;
    }

    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(request.getTimeoutMs());
        connection.setReadTimeout(request.getTimeoutMs());
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");

        return connection;
    }
}
