package com.leo.appmaster.callfilter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.db.AppMasterDBHelper;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.imageloader.utils.IoUtils;

/**
 * 骚扰拦截工具
 * Created by runlee on 15-12-18.
 */
public class CallFilterUtils {

    /**
     * 封装BlackListInfo
     *
     * @param id             -1
     * @param number         null
     * @param numberName     null
     * @param markerType     0
     * @param icon           null
     * @param numberArea     null
     * @param addBlackNumber 0
     * @param markerNumber   0
     * @param uploadState    false
     * @param removeState    false
     * @param readState      false
     * @return
     */
    public static BlackListInfo getBlackListInfo(int id, String number,
                                                 String numberName,
                                                 int markerType,
                                                 Bitmap icon,
                                                 String numberArea,
                                                 int addBlackNumber,
                                                 int markerNumber,
                                                 boolean uploadState,
                                                 boolean removeState,
                                                 boolean readState) {
        BlackListInfo info = new BlackListInfo();
        if (id != -1) {
            info.setId(id);
        }
        if (!TextUtils.isEmpty(number)) {
            info.setNumber(number);
        }
        if (!TextUtils.isEmpty(numberName)) {
            info.setNumberName(numberName);
        }
        info.setMarkerType(markerType);
        if (icon != null) {
            info.setIcon(icon);
        }
        if (!TextUtils.isEmpty(numberArea)) {
            info.setNumberArea(numberArea);
        }
        info.setAddBlackNumber(addBlackNumber);
        info.setMarkerNumber(markerNumber);
        info.setUploadState(uploadState);
        info.setRemoveState(removeState);
        info.setReadState(readState);
        return info;
    }

    public static boolean isDbKeyExist(String table, String colum, String number) {
        SQLiteOpenHelper dbHelper = AppMasterDBHelper.getInstance(AppMasterApplication.getInstance());
        SQLiteDatabase sd = dbHelper.getReadableDatabase();
        if (sd == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            number = PrivacyContactUtils.formatePhoneNumber(number);
            cursor = sd.query(table, new String[]{colum}, colum + " LIKE ? ",
                    new String[]{"%" + number}, null, null, null);
            if (cursor != null) {
                return cursor.getCount() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(cursor);
        }

        return false;
    }

    /**
     * 测试用
     */
    public static  void addData() {
        CallFilterContextManagerImpl mp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        for (int i = 0; i < 10; i++) {
            BlackListInfo info = getBlackListInfo(-1, "021544", "测试", 1, null, "深圳", 123, 321, true, false, false);
            mp.addBlackList(info, false);
        }
    }
    public static void queryData(Context context){
        CallFilterContextManagerImpl mp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
      int count =   mp.getBlackListCount();
        Toast.makeText(context,""+count,Toast.LENGTH_LONG).show();
    }

}
