package com.leo.appmaster.cloud.crypto;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Jasper on 2015/12/19.
 */
public class CryptoJsonObjectRequest extends JsonRequest<JSONObject> {
    public CryptoJsonObjectRequest(int method, String url, String requestBody,
                                   Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
        setBodyNeedEncrypt();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            String decrypted = CryptoUtils.decrypt(jsonString);
            return Response.success(new JSONObject(decrypted),
                    HttpHeaderParser.parseCacheHeaders(response), response.notModified);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
