package com.zlf.appmaster.schedule;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;

import com.zlfandroid.zlfvolley.DefaultRetryPolicy;
import com.zlfandroid.zlfvolley.Response;
import com.zlfandroid.zlfvolley.VolleyError;
import com.zlfandroid.zlfvolley.toolbox.FileRequest;
import com.zlfandroid.zlfvolley.toolbox.HttpStack;
import com.zlfandroid.zlfvolley.toolbox.HurlStack;
import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.HttpRequestAgent;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.db.MsgCenterTable;
import com.zlf.appmaster.eventbus.LeoEventBus;
import com.zlf.appmaster.eventbus.event.MsgCenterEvent;
import com.zlf.appmaster.msgcenter.Message;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.imageloader.utils.IoUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息中心抓取任务
 * Created by Jasper on 2015/9/8.
 */
public class MsgCenterFetchJob extends com.zlf.appmaster.schedule.FetchScheduleJob {
    private static final String TAG = "MsgCenterFetchJob";

    private static final int REQUEST_FAIL = -1;

    private static HashMap<String, AtomicInteger> sAtomicIntegerHashMap = new HashMap<String, AtomicInteger>();

    public static void startImmediately() {
        LeoLog.i(TAG, "startImmediately.....");
//        if (BuildProperties.isApiLevel14()) return;
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
        long start = SystemClock.elapsedRealtime();
        super.onFetchSuccess(response, noMidify);
        if (response == null || !(response instanceof JSONArray)) {
            LeoLog.i(TAG, "response: " + response);
            return;
        }

        Context ctx = AppMasterApplication.getInstance();

        JSONArray array = (JSONArray) response;
        LeoLog.i(TAG, "onFetchSuccess, response: " + array.toString() + " | noModify: " + noMidify);

        MsgCenterTable table = new MsgCenterTable();
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
                if (message.isOffline()) continue;

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

        if (array.length() == 0) {
            table.clear();
        } else {
            table.insertMsgList(list);
        }
        LeoLog.i(TAG, "cost, onFetchSuccess: " + (SystemClock.elapsedRealtime() - start));
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
            AtomicInteger atomicInteger = new AtomicInteger(0);
            sAtomicIntegerHashMap.put(msg.jumpUrl, atomicInteger);

            String fileNameNoSuffix = getFileName(msg.jumpUrl);
            String htmlFileName = fileNameNoSuffix + ".html";
            File htmlFile = new File(getFilePath(htmlFileName));
            if (!TextUtils.isEmpty(msg.jumpUrl) && !htmlFile.exists()) {
                requestHtml(htmlFile.getAbsolutePath(), msg.jumpUrl, msg);
            }

            String resFileName = fileNameNoSuffix + ".zip";
            File resFile = new File(getFilePath(resFileName));
            if (!TextUtils.isEmpty(msg.resUrl) && !resFile.exists()) {
                // Volley不支持stream保存，蛋疼，自己写一套先
                requestResFile(resFile.getAbsolutePath(), msg.resUrl, msg);
            }
        }
    }

    private static void requestHtml(String filePath, String url, Message msg) {
        Context ctx = AppMasterApplication.getInstance();
        File file = new File(filePath);
        if (file.exists()) {
            // 文件存在，不继续请求
            return;
        }

        int retryCount = 3;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        HtmlListener listener = new HtmlListener(msg, file);
        FileRequest request = new FileRequest(url, file.getAbsolutePath(), listener, listener);
        request.setRetryPolicy(policy);

        HttpRequestAgent.getInstance(ctx).getRequestQueue().add(request);
    }

    public static String getFileName(String url) {
        return url.hashCode() + "";
    }

    public static String getFilePath(String fileName) {
        Context ctx = AppMasterApplication.getInstance();
        File cacheDir = ctx.getExternalCacheDir();
        if (cacheDir == null) {
            File srcDir = Environment.getExternalStorageDirectory();
            if (srcDir == null) {
                cacheDir = ctx.getCacheDir();
            } else {
                cacheDir = new File(srcDir.getAbsolutePath() + "/appmaster");
            }
        }
        return cacheDir + "/msgcenter/" + fileName;
    }

    public static String getFilePathByUrl(String url) {
        return getFilePath(getFileName(url));
    }

    /**
     * 下载资源包
     * @param filePath
     * @param url
     */
    private static void requestResFile(String filePath, String url, Message msg) {
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
        boolean success = false;
        try {
            while (retryCount < maxRetryCount) {
                try {
                    HttpResponse response = stack.performRequest(request, new HashMap<String, String>());

                    HttpEntity entity = response.getEntity();
                    inputStream = entity.getContent();
                    fos = new FileOutputStream(file);
                    if (!file.exists()) {
                        File parentFile = file.getParentFile();
                        if (!parentFile.exists()) {
                            parentFile.mkdirs();
                        }
                        file.createNewFile();
                    }

                    int size = 2048;
                    byte[] buffer = new byte[size];
                    int n;
                    while ((n = inputStream.read(buffer, 0, size)) != -1) {
                        fos.write(buffer, 0, n);
                    }
                    LeoLog.i(TAG, "download res file succ. file：" + file.getAbsolutePath());
                    LeoEventBus.getDefaultBus().post(new MsgCenterEvent(MsgCenterEvent.ID_RES));
                    success = true;
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
                    LeoLog.e(TAG, "socket timeout ex, e: " + e.toString());
                    e.printStackTrace();
                    break;
                }
            }
        } finally {
            IoUtils.closeSilently(inputStream);
            IoUtils.closeSilently(fos);
            if (!success) {
                file.delete();
            }
            checkAndAddResSuccEvent(msg, success);
        }
    }

    private static class HtmlListener implements Response.Listener<File>, Response.ErrorListener {

        private File file;
        private Message msg;
        public HtmlListener(Message msg, File file) {
            this.file = file;
            this.msg = msg;
//            if (!file.exists()) {
//                File parentFile = file.getParentFile();
//                if (!parentFile.exists()) {
//                    parentFile.mkdirs();
//                }
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            LeoLog.i(TAG, "HtmlListener, onErrorResponse: " + error);
            file.delete();
            checkAndAddResSuccEvent(msg, false);
        }

        @Override
        public void onResponse(File response, boolean noMidify) {
            LeoLog.i(TAG, "HtmlListener, onResponse: " + (response != null ? response.getAbsolutePath() : null));
            LeoEventBus.getDefaultBus().post(new MsgCenterEvent(MsgCenterEvent.ID_HTML));
            checkAndAddResSuccEvent(msg, true);
        }
    }

    /**
     * 更新日志缓存信息的上报接口
     * @param success
     */
    private static void checkAndAddResSuccEvent(Message msg, boolean success) {
        Context ctx = AppMasterApplication.getInstance();

        AtomicInteger atomicInteger = sAtomicIntegerHashMap.get(msg.jumpUrl);
        LeoLog.i(TAG, "checkAndAddResSuccEvent, value: " + atomicInteger.get() + " | success:" + success);
        if (success && msg.hasCacheFile()) {
            // 所有资源文件都缓存成功
        } else if (!success) {
            // 只要有一个失败，则都认为失败
            if (atomicInteger.get() != REQUEST_FAIL) {
                // 只报一次
                atomicInteger.set(REQUEST_FAIL);
            }
        }
    }

}
