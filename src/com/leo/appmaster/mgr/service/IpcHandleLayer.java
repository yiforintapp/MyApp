package com.leo.appmaster.mgr.service;

import android.content.Intent;

import java.util.HashMap;

/**
 * Created by Jasper on 2015/9/29.
 */
class IpcHandleLayer {
    private HashMap<String, String> mCmdToClazz;

    IpcHandleLayer() {
        mCmdToClazz = new HashMap<String, String>();

        mCmdToClazz.put(IpcConst.IPC_ISWIPE, "com.leo.appmaster.");
    }

    public Intent handleRequest(IpcRequest request) {
        if (request == null) return null;

        IpcHandler handler = null;
        if (IpcConst.IPC_ISWIPE.equals(request.command)) {
            handler = new ISwipeIpcHandler();
        } else if (IpcConst.IPC_THEME.equals(request.command)) {
            handler = new ThemeIpcHandler();
        }

        if (handler == null) return null;

        return handler.handleRequest(request);
    }
}
