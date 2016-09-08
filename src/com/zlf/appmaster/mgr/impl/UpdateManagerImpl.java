package com.zlf.appmaster.mgr.impl;


import android.content.Intent;
import android.net.Uri;
import android.os.Message;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.client.QStringRequest;
import com.zlf.appmaster.mgr.UpdateManager;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.VolleyTool;
import com.zlf.appmaster.utils.WifiAdmin;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


public class UpdateManagerImpl extends UpdateManager {
    private WifiAdmin updateManager;
    private int completeSize = 0;
    private boolean isCancelDownload = false;

    public interface DownLoadListener {
        public void Progress(int completeSize,int endPos);
        public void DownloadErr();
        public void LoadDone();
    }

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }
    };

    public UpdateManagerImpl() {
        updateManager = WifiAdmin.getInstance(mContext);
    }

    @Override
    public void onDestory() {

    }


    @Override
    public String checkUpdate(final OnRequestListener requestFinished) {

        String url = Constants.ADDRESS + Constants.SEVLET + Constants.DATA + CHECK_UPDATE;
        LeoLog.d(TAG,"check update url is : " + url);

        QStringRequest stringRequest = new QStringRequest(
                Request.Method.GET, url, null, new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                if (requestFinished != null) {
                    LeoLog.d(TAG,"check update requestFinished version is : " + s);
                    requestFinished.onDataFinish(s);
                }
            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                LeoLog.d(TAG,"check update err");
                requestFinished.onError(1,"checkUpdate err");
            }

        });

        // callAll的时候使用
        VolleyTool.getInstance(mContext).getRequestQueue()
                .add(stringRequest);

        return null;
    }

    @Override
    public int startDownload(String urlStr, String filepath, int filesize, int completedSize, DownLoadListener downLoadListener) {
        LeoLog.d(TAG, "url=" + urlStr + "; filepath=" + filepath + "; filesize="
                + filesize + ";  completedSize=" + completedSize);
        this.completeSize = completedSize;
        try {
            File file = new File(filepath);
            if (!file.getParentFile().exists()) {
                LeoLog.d(TAG, "mkdir -> "
                        + file.getParentFile().getAbsolutePath());
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }/* new file */
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.setLength(filesize);
            raf.close();
        } catch (Exception e) {
            return -1;
        }

        new MyThread(filesize, urlStr, filepath,downLoadListener).start();
        return 0;
    }

    @Override
    public void cancelDownload() {
        isCancelDownload = true;
    }

    @Override
    public void cancelCancelDownload() {
        isCancelDownload = false;
    }

    @Override
    public void installApk(String mFileAbsName) {
            LeoLog.d(TAG, "download done, installing ....");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + mFileAbsName),
                    "application/vnd.android.package-archive");
            mContext.startActivity(intent);
    }



    public class MyThread extends Thread {
        private int endPos;
        private String filepath;
        private String urlstr;
        DownLoadListener listener;

        public MyThread(int endPos, String urlstr, String filepath, DownLoadListener downLoadListener) {
            this.endPos = endPos;
            this.urlstr = urlstr;
            this.filepath = filepath;
            this.listener = downLoadListener;
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile = null;
            InputStream is = null;
            try {
                URL url = new URL(urlstr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setRequestProperty("Range", "bytes="
                        + UpdateManagerImpl.this.completeSize + "-" + endPos);

                if (isCancelDownload) {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    return;
                }

                /* we should initial the progress in the beginning */
                if(listener != null){
                    listener.Progress(UpdateManagerImpl.this.completeSize,endPos);
                }


                int fileSize = connection.getContentLength();
                if (fileSize != endPos - UpdateManagerImpl.this.completeSize) {
                    LeoLog.d(TAG, "filesize=" + fileSize + "; complete="
                            + UpdateManagerImpl.this.completeSize + "; total="
                            + endPos);
                    throw new Exception();
                }/* server will send wrong length data */
                LeoLog.d(TAG, "[requesting file] bytes="
                        + UpdateManagerImpl.this.completeSize + "-" + endPos
                        + "; length from server=" + fileSize);
                randomAccessFile = new RandomAccessFile(filepath, "rwd");
                randomAccessFile.seek(UpdateManagerImpl.this.completeSize);

                is = connection.getInputStream();
                byte[] buffer = new byte[4096];
                int length;
                int count = 0;
                int step = this.endPos / 4096 / 100;
                step = step > 0 ? step : 1;
                while ((length = is.read(buffer)) != -1) {
                    randomAccessFile.write(buffer, 0, length);
                    UpdateManagerImpl.this.completeSize += length;
                    if (isCancelDownload) {
                        break;
                    }/* user cancel this download process */
                    if (count++ % step == 0) {
                        if(listener != null){
                            listener.Progress(UpdateManagerImpl.this.completeSize,endPos);
                        }
                        LeoLog.d(TAG, "; complete="
                                + UpdateManagerImpl.this.completeSize + "; total="
                                + endPos);
                    }/* do not update progress with high frequency */
                    if (completeSize > endPos) {
                        listener.DownloadErr();
                        LeoLog.e(TAG, "[ERROR] server push too much data!");
                        break;
                    }/* for robust, this must be a broken file */
                }/* download file */
//                mHelper.setCompleteSize(completeSize);
                if (!isCancelDownload) {
                    LeoLog.d(TAG, "[quit loop]completeSize = " + completeSize
                            + "; total=" + endPos);
                    if (completeSize != endPos) {
                        listener.DownloadErr();
                    } else {
                        listener.LoadDone();
                        LeoLog.e(TAG, "set  mManager.isDownloading = false;");
                    }
                }
            } catch (Exception e) {
                listener.DownloadErr();
                LeoLog.e(TAG, "exception in downloading run()");
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
