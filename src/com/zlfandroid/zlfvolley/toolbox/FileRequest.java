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

package com.zlfandroid.zlfvolley.toolbox;

import java.io.File;
import java.io.FileOutputStream;

import com.zlfandroid.zlfvolley.ParseError;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class FileRequest extends com.zlfandroid.zlfvolley.Request<File> {
    private final com.zlfandroid.zlfvolley.Response.Listener<File> mListener;
    private String mFile;

    /**
     * Creates a new request with the given method.
     * 
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public FileRequest(int method, String url, String file, com.zlfandroid.zlfvolley.Response.Listener<File> listener,
            com.zlfandroid.zlfvolley.Response.ErrorListener errorListener) {
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
    public FileRequest(String url, String file, com.zlfandroid.zlfvolley.Response.Listener<File> listener, com.zlfandroid.zlfvolley.Response.ErrorListener errorListener) {
        this(Method.GET, url, file, listener, errorListener);
    }

    @Override
    protected void deliverResponse(File response, boolean noMidify) {
        mListener.onResponse(response, noMidify);
    }

    @Override
    protected com.zlfandroid.zlfvolley.Response<File> parseNetworkResponse(com.zlfandroid.zlfvolley.NetworkResponse response) {
        try {
            File file = new File(mFile);
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }

            FileOutputStream fout = new FileOutputStream(mFile);
            byte[] bytes = response.data;
            fout.write(bytes);
            fout.close();
            return com.zlfandroid.zlfvolley.Response.success(new File(mFile), HttpHeaderParser.parseCacheHeaders(response),
                    response.notModified);
        }catch (Exception e) {
            return com.zlfandroid.zlfvolley.Response.error(new ParseError(response));
        }
    }
}
