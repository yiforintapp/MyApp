package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.FileRequest;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.MsgCenterTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.MsgCenterEvent;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.msgcenter.Message;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 消息中心抓取任务
 * Created by Jasper on 2015/9/8.
 */
public class MsgCenterFetchJob extends FetchScheduleJob {
    private static final String TAG = "MsgCenterFetchJob";
    private static final boolean DBG = true;

    public static void startByPush() {
        MsgCenterFetchJob job = new MsgCenterFetchJob();
        FetchScheduleListener listener = job.newJsonArrayListener();
        Context ctx = AppMasterApplication.getInstance();
        HttpRequestAgent.getInstance(ctx).loadMessageCenterList(listener, listener);
    }

    @Override
    protected void work() {
        LeoLog.i(getJobKey(), "do work.....");

        Context ctx = AppMasterApplication.getInstance();

        FetchScheduleListener listener = newJsonArrayListener();
        HttpRequestAgent.getInstance(ctx).loadMessageCenterList(listener, listener);
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        LeoLog.i(getJobKey(), "onFetchFail, error: " + (error == null ? null : error.toString()));
        super.onFetchFail(error);
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
//        LeoLog.i(getJobKey(), "onFetchSuccess, response: " +array+ " | noModify: " + noMidify);
        if (response == null || !(response instanceof JSONArray)) return;

        JSONArray array = (JSONArray) response;
        List<Message> list = new ArrayList<Message>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.get(i);

                final Message message = new Message();
                message.time = obj.getString("activity_time");
                message.categoryName = obj.getString("category_name");
                message.categoryCode = obj.getString("category_code");
                message.description = obj.getString("description");
                message.imageUrl = obj.getString("image_url");
                message.jumpUrl = obj.getString("link");
                message.offlineTime = obj.getString("offline_time");
                message.title = obj.getString("title");
                message.resUrl =obj.getString("resource");
                message.msgId = obj.getInt("id");

                list.add(message);

                if (message.isCategoryUpdate()) {
                    // 如果是更新日志，开启更新日志下载功能
                    ThreadManager.executeOnNetworkThread(new Runnable() {
                        @Override
                        public void run() {
                            checkCacheAndRequest(message);
                        }
                    });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MsgCenterTable table = new MsgCenterTable();
        table.insertMsgList(list);
    }

//    @Override
//    protected int getPeriod() {
//        return 10 * 1000;
//    }
//
//    @Override
//    protected int getFailPeriod() {
//        return 10 * 1000;
//    }

    public static void checkCacheAndRequest(Message message) {
        List<Message> list = new ArrayList<Message>();
        if (message == null) {
            MsgCenterTable table = new MsgCenterTable();
            list.addAll(table.getUpdateMessage());
            if (list.isEmpty()) return;
        } else {
            list.add(message);
        }

        for (Message msg : list) {
            String fileNameNoSuffix = getFileName(msg.jumpUrl);
            String htmlFileName = fileNameNoSuffix + ".html";
            File htmlFile = new File(getFilePath(htmlFileName));
            if (msg.jumpUrl != null && !htmlFile.exists()) {
                requestHtml(htmlFile.getAbsolutePath(), msg.jumpUrl);
            }

            String resFileName = fileNameNoSuffix + ".zip";
            File resFile = new File(getFilePath(resFileName));
            if (msg.resUrl != null && !resFile.exists()) {
                // Volley不支持stream保存，蛋疼，自己写一套先
                requestResFile(resFile.getAbsolutePath(), msg.resUrl);
            }
        }
    }

    private static void requestHtml(String filePath, String url) {
        Context ctx = AppMasterApplication.getInstance();
        File file = new File(filePath);
        if (file.exists()) {
            // 文件存在，不继续请求
            return;
        }

        int retryCount = 3;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        HtmlListener listener = new HtmlListener(file);
        FileRequest request = new FileRequest(url, file.getAbsolutePath(), listener, listener);
        request.setRetryPolicy(policy);

        HttpRequestAgent.getInstance(ctx).getRequestQueue().add(request);
    }

    public static String getFileName(String url) {
        return url.hashCode() + "";
    }

    public static String getFilePath(String fileName) {
        Context ctx = AppMasterApplication.getInstance();
        return ctx.getExternalCacheDir() + "/msgcenter/" + fileName;
    }

    public static String getFilePathByUrl(String url) {
        return getFilePath(getFileName(url));
    }

    public static boolean hasCacheFile(String url) {
        String fileName = getFileName(url);
        String htmlFileStr = getFilePath(fileName + ".html");
        File htmlFile = new File(htmlFileStr);
        if (!htmlFile.exists()) return false;

        String zipFileStr = getFilePath(fileName + ".zip");
        File zipFile = new File(zipFileStr);
        if (!zipFile.exists()) return false;

        return true;
    }

    /**
     * 下载资源包
     * @param filePath
     * @param url
     */
    private static void requestResFile(String filePath, String url) {
        File file = new File(filePath);
        if (file.exists()) {
            // 文件存在，不继续请求
            return;
        }
        HttpStack stack = new HurlStack();
        FileRequest request = new FileRequest(url, null, null, null);

        int maxRetryCount = 3;
        int retryCount = 0;

        FileOutputStream fos = null;
        InputStream inputStream = null;
        try {
            while (retryCount < maxRetryCount) {
                try {
                    HttpResponse response = stack.performRequest(request, new HashMap<String, String>());
                    if (!file.exists()) {
                        File parentFile = file.getParentFile();
                        if (!parentFile.exists()) {
                            parentFile.mkdirs();
                        }
                        file.createNewFile();
                    }

                    HttpEntity entity = response.getEntity();
                    inputStream = entity.getContent();
                    fos = new FileOutputStream(file);

                    int size = 2048;
                    byte[] buffer = new byte[size];
                    int n;
                    while ((n = inputStream.read(buffer, 0, size)) != -1) {
                        fos.write(buffer, 0, n);
                    }
                    LeoLog.i(TAG, "download res file succ.");
                    LeoEventBus.getDefaultBus().post(new MsgCenterEvent(MsgCenterEvent.ID_RES));
                    break;
                } catch (SocketTimeoutException e) {
                    // retry
                    retryCount++;
                    LeoLog.e(TAG, "socket timeout ex, retrycount: " + retryCount);
                } catch (ConnectTimeoutException e) {
                    // retry
                    retryCount++;
                    LeoLog.e(TAG, "connect timeout ex, retrycount: " + retryCount);
                } catch (Exception e) {
                    e.printStackTrace();
                    file.delete();
                    break;
                }
            }
        } finally {
            IoUtils.closeSilently(inputStream);
            IoUtils.closeSilently(fos);
        }
    }

    private static class HtmlListener implements Response.Listener<File>, Response.ErrorListener {

        private File file;
        public HtmlListener(File file) {
            this.file = file;
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            LeoLog.i(TAG, "HtmlListener, onErrorResponse: " + error);
            file.delete();
        }

        @Override
        public void onResponse(File response, boolean noMidify) {
            LeoLog.i(TAG, "HtmlListener, onResponse: " + response);
            LeoEventBus.getDefaultBus().post(new MsgCenterEvent(MsgCenterEvent.ID_HTML));
        }
    }

}
