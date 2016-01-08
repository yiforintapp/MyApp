package com.leo.appmaster.callfilter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.db.AppMasterDBHelper;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;
import com.leo.appmaster.utils.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
     * @param uploadState    -1(无该属性)
     * @param readState      -1(无该属性)
     * @param locHd          -1(无该属性)(0:未处理，1：已处理)
     * @param locHdType      -1(无该属性)
     * @param removeState    -1(无该属性)
     * @return
     */
    public static BlackListInfo getBlackListInfo(int id, String number,
                                                 String numberName,
                                                 int markerType,
                                                 Bitmap icon,
                                                 String numberArea,
                                                 int addBlackNumber,
                                                 int markerNumber,
                                                 int uploadState,
                                                 int locHd,
                                                 int locHdType,
                                                 int readState,
                                                 int removeState,
                                                 int filUpState) {
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
        if (locHd != -1) {
            info.setLocHandler(locHd);
        }
        if (locHdType != -1) {
            info.setLocHandlerType(locHdType);
        }
        if (removeState != -1) {
            info.setRemoveState(removeState);
        }
        if (filUpState != -1) {
            info.setFiltUpState(filUpState);
        }
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
     * @param readState    -1（无该属性）
     * @param filterType   0（无） ，标记类型
     * @param filterGrId   -1（无该属性）
     * @param icon         null
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
                                               int readState,
                                               int filterType,
                                               int filterGrId,
                                               Bitmap icon) {
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
        if (readState != -1) {
            filter.setReadState(readState);
        }
        filter.setFilterType(filterType);
        if (filterGrId != -1) {
            filter.setFilterGrId(filterGrId);
        }
        if (icon != null) {
            filter.setIcon(icon);
        }
        return filter;
    }

    /**
     * 封装StrangerInfo
     *
     * @param id       -1
     * @param number   null
     * @param date     0
     * @param tipState -1(无该属性)
     * @param type     (0)
     * @return
     */
    public static StrangerInfo getStrangInfo(int id,
                                             String number,
                                             long date,
                                             int tipState,
                                             int type) {
        StrangerInfo info = new StrangerInfo();

        if (id != -1) {
            info.setId(id);
        }
        if (!TextUtils.isEmpty(number)) {
            info.setNumber(number);
        }

        info.setDate(date);
        if (tipState != -1) {
            info.setTipState(tipState);
        }
        info.setTipType(type);

        return info;
    }

    public static Cursor getCursor(String table, String[] colums, String number) {
        SQLiteOpenHelper dbHelper = AppMasterDBHelper.getInstance(AppMasterApplication.getInstance());
        SQLiteDatabase sd = dbHelper.getReadableDatabase();
        if (sd == null) {
            return null;
        }
        Cursor cursor = null;
        try {
            number = PrivacyContactUtils.formatePhoneNumber(number);
            cursor = sd.query(table, colums, colums[0] + " LIKE ? ",
                    new String[]{"%" + number}, null, null, null);
            if (cursor != null) {
                return cursor;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isDbKeyExist(String table, String[] colums, String number) {
        Cursor cursor = null;
        try {
            cursor = getCursor(table, colums, number);
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
        List<BlackListInfo> list = new ArrayList<BlackListInfo>();
        for (int i = 0; i < 10; i++) {
            BlackListInfo info = CallFilterUtils.getBlackListInfo(-1, "110" + i, "测试", 0, null,
                    null, 23, 25, 0, 1, 1, 0, 1, -1);
            list.add(info);
        }
//        BlackUploadFetchJob.getJsonString(list);


//        List<CallFilterInfo> list = new ArrayList<CallFilterInfo>();
//        for (int i = 0; i < 10; i++) {
//            long date = Long.valueOf("5454545554");
//            CallFilterInfo info = getFilterInfo(-1, "en", "021544", "测试", 1, 20, date, 123, -1, false, 1, 2);
//            list.add(info);
//        }

//        List<StrangerInfo> list = new ArrayList<StrangerInfo>();
//        for (int i = 0; i < 10; i++) {
//            long date = Long.valueOf("5454545554");
//            StrangerInfo info = getStrangInfo(-1, "34143", "测试", date, date,
//                    0, 20, false, false, false, -1);
//
//            list.add(info);
//        }
//        List<StrangerInfo> list = new ArrayList<StrangerInfo>();
//        for (int i = 1; i <= 10 ; i++) {
//            long date = Long.valueOf("5454545554");
//            StrangerInfo info = getStrangInfo(-1,"110",date,1,2);
//            list.add(info);
//        }
        mp.addBlackList(list, false);
    }

    public static void queryData(Context context) {
        CallFilterContextManagerImpl mp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        int count = 0;
        List<BlackListInfo> strangerInfos = mp.getServerBlackList();
        if (strangerInfos != null) {
            count = mp.getServerBlackList().size();
        }

        Toast.makeText(context, "" + count, Toast.LENGTH_SHORT).show();
    }

    public static void removeDate(Context context) {
        CallFilterContextManagerImpl mp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        List<StrangerInfo> list = new ArrayList<StrangerInfo>();
        for (int i = 1; i <= 10; i++) {
            long date = Long.valueOf("5454545554");
            StrangerInfo info = getStrangInfo(i, "110", date, 1, 2);
            list.add(info);
        }
        mp.removeStrangerGr(list);

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

    public static List<BlackListInfo> getBlackList(Uri uri, String[] projection, String selects, String[] selectArgs, String sortOrder) {
        Context context = AppMasterApplication.getInstance();
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        List<BlackListInfo> blackListInfoList = null;
        try {
            cursor = cr.query(uri, projection, selects, selectArgs, sortOrder);
            if (cursor != null) {
                blackListInfoList = new ArrayList<BlackListInfo>();
                while (cursor.moveToNext()) {
                    int idColum = cursor.getColumnIndex(CallFilterConstants.BLACK_ID);
                    int nameColum = cursor.getColumnIndex(CallFilterConstants.BLACK_NAME);
                    int numberColum = cursor.getColumnIndex(CallFilterConstants.BLACK_PHONE_NUMBER);
                    int iconColum = cursor.getColumnIndex(CallFilterConstants.BLACK_ICON);
                    int areaColum = cursor.getColumnIndex(CallFilterConstants.BLACK_NUMBER_AREA);
                    int addNumColum = cursor.getColumnIndex(CallFilterConstants.BLACK_ADD_NUMBER);
                    int markerTypeColum = cursor.getColumnIndex(CallFilterConstants.MARKER_TYPE);
                    int markerNumColum = cursor.getColumnIndex(CallFilterConstants.MARKER_NUMBER);
                    int uploadStateColum = cursor.getColumnIndex(CallFilterConstants.BLACK_UPLOAD_STATE);
                    int locHdColum = cursor.getColumnIndex(CallFilterConstants.BLACK_LOC_HD);
                    int locHdTypeColum = cursor.getColumnIndex(CallFilterConstants.BLACK_LOC_HD_TYPE);
                    int readStateColum = cursor.getColumnIndex(CallFilterConstants.BLACK_READ_STATE);
                    int removeColum = cursor.getColumnIndex(CallFilterConstants.BLACK_REMOVE_STATE);
                    int filUpCOlum = cursor.getColumnIndex(CallFilterConstants.BLACK_FIL_UP);

                    int id = cursor.getInt(idColum);
                    String name = cursor.getString(nameColum);
                    String number = cursor.getString(numberColum);
                    if (TextUtils.isEmpty(name)) {
                        name = number;
                    }
                    Bitmap icon = null;
                    byte[] iconByte = cursor.getBlob(iconColum);
                    if (iconByte != null && iconByte.length > 0) {
                        icon = PrivacyContactUtils.getBmp(iconByte);
                        int size = (int) context.getResources().getDimension(R.dimen.contact_icon_scale_size);
                        icon = PrivacyContactUtils.getScaledContactIcon(icon, size);
                    }
                    String numberArea = cursor.getString(areaColum);
                    int addBlackNum = cursor.getInt(addNumColum);
                    int markerType = cursor.getInt(markerTypeColum);
                    int markerNum = cursor.getInt(markerNumColum);
                    int uploadStateType = cursor.getInt(uploadStateColum);
                    int locHd = cursor.getInt(locHdColum);
                    int locHdType = cursor.getInt(locHdTypeColum);
                    int readStateType = cursor.getInt(readStateColum);
                    int removeStateType = cursor.getInt(removeColum);
                    int filUpState = cursor.getInt(filUpCOlum);

                    BlackListInfo info = CallFilterUtils.getBlackListInfo(id, number, name, markerType, icon,
                            numberArea, addBlackNum, markerNum, uploadStateType, locHd, locHdType, readStateType, removeStateType, filUpState);
                    blackListInfoList.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return blackListInfoList;
    }

    public static List<BlackListInfo> getNoUpBlack(Uri uri, String selection, String[] selectionArgs, String sortOrder) {
        Context context = AppMasterApplication.getInstance();
        ContentResolver cr = context.getContentResolver();
        List<BlackListInfo> blackListInfoList = null;
        Cursor cursor = null;
        try {
            cursor = cr.query(uri, null, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                blackListInfoList = new ArrayList<BlackListInfo>();
                while (cursor.moveToNext()) {
                    int idColum = cursor.getColumnIndex(CallFilterConstants.BLACK_ID);
                    int nameColum = cursor.getColumnIndex(CallFilterConstants.BLACK_NAME);
                    int numberColum = cursor.getColumnIndex(CallFilterConstants.BLACK_PHONE_NUMBER);
                    int iconColum = cursor.getColumnIndex(CallFilterConstants.BLACK_ICON);
                    int areaColum = cursor.getColumnIndex(CallFilterConstants.BLACK_NUMBER_AREA);
                    int addNumColum = cursor.getColumnIndex(CallFilterConstants.BLACK_ADD_NUMBER);
                    int markerTypeColum = cursor.getColumnIndex(CallFilterConstants.MARKER_TYPE);
                    int markerNumColum = cursor.getColumnIndex(CallFilterConstants.MARKER_NUMBER);
                    int uploadStateColum = cursor.getColumnIndex(CallFilterConstants.BLACK_UPLOAD_STATE);
                    int locHdColum = cursor.getColumnIndex(CallFilterConstants.BLACK_LOC_HD);
                    int locHdTypeColum = cursor.getColumnIndex(CallFilterConstants.BLACK_LOC_HD_TYPE);
                    int readStateColum = cursor.getColumnIndex(CallFilterConstants.BLACK_READ_STATE);
                    int removeColum = cursor.getColumnIndex(CallFilterConstants.BLACK_REMOVE_STATE);
                    int filUpColum = cursor.getColumnIndex(CallFilterConstants.BLACK_FIL_UP);
                    int id = cursor.getInt(idColum);
                    String name = cursor.getString(nameColum);
                    String number = cursor.getString(numberColum);
                    if (TextUtils.isEmpty(name)) {
                        name = number;
                    }

                    Bitmap icon = null;
                    byte[] iconByte = cursor.getBlob(iconColum);
                    if (iconByte != null && iconByte.length > 0) {
                        icon = PrivacyContactUtils.getBmp(iconByte);
                        int size = (int) context.getResources().getDimension(R.dimen.contact_icon_scale_size);
                        icon = PrivacyContactUtils.getScaledContactIcon(icon, size);
                    }
                    String numberArea = cursor.getString(areaColum);
                    int addBlackNum = cursor.getInt(addNumColum);
                    int markerType = cursor.getInt(markerTypeColum);
                    int markerNum = cursor.getInt(markerNumColum);
                    int uploadStateType = cursor.getInt(uploadStateColum);
                    int locHd = cursor.getInt(locHdColum);
                    int locHdType = cursor.getInt(locHdTypeColum);
                    int readStateType = cursor.getInt(readStateColum);
                    int removeStateType = cursor.getInt(removeColum);
                    int filUpState = cursor.getInt(filUpColum);
                    BlackListInfo info = CallFilterUtils.getBlackListInfo(id, number, name, markerType, icon,
                            numberArea, addBlackNum, markerNum, uploadStateType, locHd, locHdType, readStateType,
                            removeStateType, filUpState);
                    blackListInfoList.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return blackListInfoList;
    }

    /* 获取黑名单保存路径*/
    public static String getBlackPath() {
        if (FileOperationUtil.isSDReady()) {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            path += CallFilterConstants.BLACK_FILE_PATH;
            File backupDir = new File(path);
            if (!backupDir.exists()) {
                boolean success = backupDir.mkdirs();
                if (!success) {
                    return null;
                }
            }
            return path;
        }
        return null;
    }

    /**
     * 解析黑名单列表文件
     */
    public static void parseBlactList(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            return;
        }
        List<BlackListInfo> infos = new ArrayList<BlackListInfo>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String temp = null;
            StringBuffer sb = new StringBuffer();
            temp = br.readLine();
            while (temp != null) {
                String[] dates = temp.split(",");
                final String number = dates[0];
                String blackCountStr = dates[1];
                String markTypeStr = dates[2];
                String markCountStr = dates[3];
                final int blackCount = Float.valueOf(blackCountStr).intValue();
                final int markType = Float.valueOf(markTypeStr).intValue();
                final int markCount = Float.valueOf(markCountStr).intValue();

                LeoLog.i("BlackListFileFetchJob", "number-" + number);
                LeoLog.i("BlackListFileFetchJob", "blackCount-" + blackCount);
                LeoLog.i("BlackListFileFetchJob", "markType-" + markType);
                LeoLog.i("BlackListFileFetchJob", "markCount-" + markCount);

                BlackListInfo info = new BlackListInfo();
                info.setNumber(number);
                info.setAddBlackNumber(blackCount);
                info.setMarkerType(markType);
                info.setMarkerNumber(markCount);
                infos.add(info);
                temp = br.readLine();
            }
            CallFilterManager cm = CallFilterManager.getInstance(AppMasterApplication.getInstance());
            cm.addFilterFroParse(infos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            /*删除文件*/
//            String filePaht = BlackListFileFetchJob.getBlackFilePath();
//            deleteFile(filePaht);
        }
    }

    /**
     * 删除文件
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        try {
            file.delete();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }


}
