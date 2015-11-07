package com.leo.appmaster.mgr.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

/**
 * 第三方App接入服务，完成以下几件事情
 *  1、解析intent
 *  2、参数、包名等校验
 *  3、分发请求各个业务层处理
 */
public class IpcRequestService extends Service {
    private IpcHandleLayer mHandleLayer;

    public IpcRequestService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandleLayer = new IpcHandleLayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private IpcRequestInterface.Stub mBinder = new IpcRequestInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble,
                               String aString) throws RemoteException {
            // do nothing.
        }

        @Override
        public void request(Intent request, Intent reply) throws RemoteException {
            if (request == null || reply == null) return;

            IpcRequest ipcRequest = new IpcRequest();
            ipcRequest.id = request.getIntExtra(IpcConst.KEY_REQUEST_ID, 0);
            ipcRequest.command = request.getStringExtra(IpcConst.KEY_REQUEST_COMMAND);
            ipcRequest.pkgName = request.getStringExtra(IpcConst.KEY_REQUEST_PKG);
            ipcRequest.data = request;

            // 包名、命令字不能为空
            if (TextUtils.isEmpty(ipcRequest.pkgName)
                    || TextUtils.isEmpty(ipcRequest.command)) return;

            Intent response = mHandleLayer.handleRequest(ipcRequest);
            if (response == null) return;

            reply.putExtras(response);
            reply.putExtra(IpcConst.KEY_REQUEST_ID, ipcRequest.id);
            reply.putExtra(IpcConst.KEY_REQUEST_COMMAND, ipcRequest.command);
            reply.putExtra(IpcConst.KEY_REQUEST_PKG, ipcRequest.pkgName);
        }

    };
}
