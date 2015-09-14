package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.FileRequest;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.db.MsgCenterTable;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.msgcenter.Message;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * 消息中心抓取任务
 * Created by Jasper on 2015/9/8.
 */
public class MsgCenterFetchJob extends FetchScheduleJob {
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

                Message message = new Message();
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
                    checkCacheAndRequest(message);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MsgCenterTable table = new MsgCenterTable();
        table.insertMsgList(list);
    }

    @Override
    protected int getPeriod() {
        return 10 * 1000;
    }

    @Override
    protected int getFailPeriod() {
        return 10 * 1000;
    }

    public static void checkCacheAndRequest(Message msg) {
        if (msg == null) {
            MsgCenterTable table = new MsgCenterTable();
            msg = table.getUpdateMessage();
            if (msg == null) return;
        }

        String fileNameNoSuffix = getFileName(msg.jumpUrl);
        String htmlFileName = fileNameNoSuffix + ".html";
        File htmlFile = new File(getFilePath(htmlFileName));
        if (msg.jumpUrl != null && !htmlFile.exists()) {
            doRequestCache(msg, htmlFile.getAbsolutePath(), msg.jumpUrl);
        }

        String resFileName = fileNameNoSuffix + ".zip";
        File resFile = new File(getFilePath(resFileName));
        if (msg.resUrl != null && !resFile.exists()) {
            doRequestCache(msg, resFile.getAbsolutePath(), msg.resUrl);
        }
    }

    private static void doRequestCache(Message msg, String filePath, String url) {
        Context ctx = AppMasterApplication.getInstance();
        File file = new File(filePath);
        if (file.exists()) {
            // 文件存在，不继续请求
            return;
        }
        
        UpdateCacheListener listener = new UpdateCacheListener(msg);
        FileRequest request = new FileRequest(url, file.getAbsolutePath(), listener, listener);
        HttpRequestAgent.getInstance(ctx).getRequestQueue().add(request);
    }

    public static String getFileName(String url) {
        return url.hashCode() + "";
    }

    public static String getFilePath(String fileName) {
        Context ctx = AppMasterApplication.getInstance();
        return ctx.getExternalCacheDir() + "/msgcenter/" + fileName;
    }

    private static class UpdateCacheListener implements Response.Listener<File>, Response.ErrorListener {
        Message msg;
        UpdateCacheListener(Message msg) {
            this.msg = msg;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            LeoLog.i("MsgCenterFetchJob", "UpdateCacheListener, onErrorResponse: " + error);
        }

        @Override
        public void onResponse(File response, boolean noMidify) {
            LeoLog.i("MsgCenterFetchJob", "UpdateCacheListener, onResponse: " + response);
            String nameString = response.getName();
            if (!nameString.endsWith("html")) {
//                try {
//                    ZipFile zipFile = new ZipFile(response);
//                    Enumeration enumeration = zipFile.entries();
//
//                    String zipName = zipFile.getName();
//                    String outputDir = zipName.substring(0, zipName.lastIndexOf("."));
//                    while (enumeration.hasMoreElements()) {
//                        ZipEntry entry = (ZipEntry) enumeration.nextElement();
//                        unzip(zipFile, entry, outputDir);
//                    }
//                } catch (ZipException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                
            }
        }

        private void unzip(ZipFile zipFile, ZipEntry entry, String outputDir) {
            if (entry.isDirectory()) {
                File file = new File(entry.getName());
                if (!file.exists()) {
                    file.mkdirs();
                }
                return;
            }
            File outputFile = new File(outputDir, entry.getName());
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            InputStream inputStream = null;
            BufferedInputStream bufferedInputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                inputStream = zipFile.getInputStream(entry);
                bufferedInputStream = new BufferedInputStream(inputStream);
                fileOutputStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[1024];
                int n;
                while ((n = bufferedInputStream.read(buffer, 0, 1024)) != -1) {
                    fileOutputStream.write(buffer, 0, n);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IoUtils.closeSilently(fileOutputStream);
                IoUtils.closeSilently(bufferedInputStream);
            }

        }
    }

}
