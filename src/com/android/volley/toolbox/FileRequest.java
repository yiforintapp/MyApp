/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import java.io.File;
import java.io.FileOutputStream;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class FileRequest extends Request<File> {
    private final Listener<File> mListener;
    private String mFile;

    /**
     * Creates a new request with the given method.
     * 
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public FileRequest(int method, String url, String file, Listener<File> listener,
            ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mFile = file;
    }

    /**
     * Creates a new GET request.
     * 
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public FileRequest(String url, String file, Listener<File> listener, ErrorListener errorListener) {
        this(Method.GET, url, file, listener, errorListener);
    }

    @Override
    protected void deliverResponse(File response, boolean noMidify) {
        mListener.onResponse(response, noMidify);
    }

    @Override
    protected Response<File> parseNetworkResponse(NetworkResponse response) {
        try {
            FileOutputStream fout = new FileOutputStream(mFile);
            byte[] bytes = response.data;
            fout.write(bytes);
            fout.close();
            return Response.success(new File(mFile), HttpHeaderParser.parseCacheHeaders(response),
                    response.notModified);
        }catch (Exception e) {
            return Response.error(new ParseError(response));
        }
    }
}
