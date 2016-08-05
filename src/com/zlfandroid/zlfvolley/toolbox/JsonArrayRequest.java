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

import com.zlfandroid.zlfvolley.ParseError;
import com.zlfandroid.zlfvolley.Request;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * A request for retrieving a {@link JSONArray} response body at a given URL.
 */
public class JsonArrayRequest extends JsonRequest<JSONArray> {

    /**
     * Creates a new request.
     * @param url URL to fetch the JSON from
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonArrayRequest(String url, com.zlfandroid.zlfvolley.Response.Listener<JSONArray> listener, com.zlfandroid.zlfvolley.Response.ErrorListener errorListener) {
        super(Request.Method.GET, url, null, listener, errorListener);
    }

    @Override
    protected com.zlfandroid.zlfvolley.Response<JSONArray> parseNetworkResponse(com.zlfandroid.zlfvolley.NetworkResponse response) {
        try {
            String jsonString =
                new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return com.zlfandroid.zlfvolley.Response.success(new JSONArray(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response), response.notModified);
        } catch (UnsupportedEncodingException e) {
            return com.zlfandroid.zlfvolley.Response.error(new ParseError(e));
        } catch (JSONException je) {
            return com.zlfandroid.zlfvolley.Response.error(new ParseError(je));
        }
    }
}
