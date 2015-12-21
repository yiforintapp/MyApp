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
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.imageloader.utils.IoUtils;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 封装CallFilterInfo
     *
     * @param id           -1
     * @param name         null
     * @param number       null
     * @param backId       -1（无该属性）
     * @param filterNumber -1（无该属性）
     * @param date         0
     * @param duration     0
     * @param callType     -1（无该属性） 通话类型
     * @param readState    false
     * @param filterType   0（无） ，标记类型
     * @param filterGrId   -1（无该属性）
     * @return
     */
    public static CallFilterInfo getFilterInfo(int id,
                                               String name,
                                               String number,
                                               String numberArea,
                                               int backId,
                                               int filterNumber,
                                               long date,
                                               long duration,
                                               int callType,
                                               boolean readState,
                                               int filterType,
                                               int filterGrId) {
        CallFilterInfo filter = new CallFilterInfo();
        if (id != -1) {
            filter.setId(id);
        }
        if (!TextUtils.isEmpty(name)) {
            filter.setNumberName(name);
        }
        if (!TextUtils.isEmpty(number)) {
            filter.setNumber(number);
        }
        if (!TextUtils.isEmpty(numberArea)) {
            filter.setNumberType(numberArea);
        }
        if (backId != -1) {
            filter.setBlackId(backId);
        }
        if (filterNumber > 0) {
            filter.setFilterCount(filterNumber);
        }
        filter.setTimeLong(date);
        filter.setDuration(duration);
        if (callType != -1) {
            filter.setCallType(callType);
        }
        filter.setReadState(readState);
        filter.setFilterType(filterType);
        if (filterGrId != -1) {
            filter.setFilterGrId(filterGrId);
        }
        return filter;
    }

    /**
     * 封装StrangerInfo
     *
     * @param id          -1
     * @param number      null
     * @param numberArea  null
     * @param duration    0
     * @param date        0
     * @param callType    -1(无该属性) 通话类型
     * @param starCount   -1(无该属性)
     * @param tipState    false
     * @param removeState false
     * @param readState   false
     * @param starGrId    -1(无该属性)
     * @return
     */
    public static StrangerInfo getStrangInfo(int id,
                                             String number,
                                             String numberArea,
                                             long duration,
                                             long date,
                                             int callType,
                                             int starCount,
                                             boolean tipState,
                                             boolean removeState,
                                             boolean readState,
                                             int starGrId) {
        StrangerInfo info = new StrangerInfo();

        if (id != -1) {
            info.setId(id);
        }
        if (!TextUtils.isEmpty(number)) {
            info.setNumber(number);
        }
        if (!TextUtils.isEmpty(numberArea)) {
            info.setNumberArea(numberArea);
        }
        info.setCallDuration(duration);
        info.setDate(date);
        if (callType != -1) {
            info.setCallType(callType);
        }
        if (starCount != -1) {
            info.setStarCount(starCount);
        }
        info.setTipState(tipState);
        info.setReadState(removeState);
        info.setReadState(readState);
        if (starGrId != -1) {
            info.setStrangeGrId(starGrId);
        }

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
    public static void addData() {
        CallFilterContextManagerImpl mp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
//        List<BlackListInfo> list = new ArrayList<BlackListInfo>();
//        for (int i = 0; i < 10; i++) {
//            BlackListInfo info = getBlackListInfo(-1, "021544", "测试", 1, null, "深圳", 123, 321, true, false, false);
//            list.add(info);
//        }


//        List<CallFilterInfo> list = new ArrayList<CallFilterInfo>();
//        for (int i = 0; i < 10; i++) {
//            long date = Long.valueOf("5454545554");
//            CallFilterInfo info = getFilterInfo(-1, "en", "021544", "测试", 1, 20, date, 123, -1, false, 1, 2);
//            list.add(info);
//        }

        List<StrangerInfo> list = new ArrayList<StrangerInfo>();
        for (int i = 0; i < 10; i++) {
            long date = Long.valueOf("5454545554");
            StrangerInfo info = getStrangInfo(-1, "34143", "测试", date, date,
                    0, 20, false, false, false, -1);

            list.add(info);
        }
        mp.addStrangerDet(list, false);
    }

    public static void queryData(Context context) {
        CallFilterContextManagerImpl mp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        int count = 0;
        List<StrangerInfo> strangerInfos = mp.getStrangerDetList();
        if (strangerInfos != null) {
            count = mp.getStrangerDetList().size();
        }

        Toast.makeText(context, "" + count, Toast.LENGTH_SHORT).show();
    }

    public static void removeDate(Context context) {
        CallFilterContextManagerImpl mp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        List<StrangerInfo> list = new ArrayList<StrangerInfo>();
        for (int i = 1; i <= 10; i++) {
            long date = Long.valueOf("5454545554");
            StrangerInfo info = getStrangInfo(i, "34143", "测试", date, date,
                    0, 20, false, false, false, -1);
            list.add(info);
        }
        mp.removeStrangerDet(list);

    }

    public static boolean checkIsHaveBlackNum(String blackNums, String PhoneNum) {
        String[] strings = blackNums.split(":");
        for (int i = 0; i < strings.length; i++) {
            String mNumber = getNumber(i, strings);
            if (!Utilities.isEmpty(mNumber) && mNumber.equals(PhoneNum)) {
                return true;
            }
        }
        return false;
    }

    private static String getNumber(int i, String[] strings) {
        String mStr = strings[i];
        String number = mStr.split("_")[1];
        return number;
    }

    /**
     * 查询该号码是否被隐私联系人使用
     *
     * @param number
     * @return true，已经使用，false，未使用
     */
    public static boolean isNumberUsePrivacy(String number) {
        if (TextUtils.isEmpty(number)) {
            return true;
        }
        Context context = AppMasterApplication.getInstance();
        PrivacyContactManager pm = PrivacyContactManager.getInstance(context);
        List<ContactBean> priContacts = pm.getPrivateContacts();
        if (priContacts == null || priContacts.size() <= 0) {
            return false;
        }

        for (ContactBean contact : priContacts) {
            String priNumber = PrivacyContactUtils.simpleFromateNumber(contact.getContactNumber());
            String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
            if (priNumber.contains(formateNumber)) {
                return true;
            }
        }
        return false;
    }

}