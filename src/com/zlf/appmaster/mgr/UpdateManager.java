package com.zlf.appmaster.mgr;


import android.os.Environment;

import com.zlf.appmaster.client.OnRequestListener;
import com.zlf.appmaster.mgr.impl.UpdateManagerImpl;

import java.io.File;

/**
 * WIFI安全
 * Created by Jasper on 2015/9/28.
 */
public abstract class UpdateManager extends Manager {

    public final static String TAG = "UpdateManager";
    public final static String CHECK_UPDATE = "check_update";
    /* Internet stuff */
    public static final int RETRY_COUNT = 2;
    public static final int CONNECTION_TIMEOUT = 5 * 1000; // 5 seconds
    public static final int READ_TIMEOUT = 5 * 1000; // 5 seconds
    public final static  String FILEPATH = Environment.getExternalStorageDirectory()
            .getPath()
            + File.separator
            + ".zlf"
            + File.separator
            + "update"
            + File.separator;

    @Override
    public String description() {
        return MgrContext.MGR_WIFI_SECURITY;
    }

    public abstract String checkUpdate(OnRequestListener onRequestListener);

    public abstract int startDownload(String urlStr, String filepath, int filesize,
                                      int completedSize, UpdateManagerImpl.DownLoadListener downLoadListener);

    public abstract void cancelDownload();

    public abstract void cancelCancelDownload();

    public abstract void installApk(String filePath);

}
