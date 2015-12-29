package com.leo.appmaster.mgr.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.CallLog;
import android.telecom.Call;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.callfilter.BlackListInfo;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.callfilter.CallFilterInfo;
import com.leo.appmaster.callfilter.CallFilterManager;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.callfilter.StrangerInfo;
import com.leo.appmaster.db.AppMasterDBHelper;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by runlee on 15-12-18.
 */
public class CallFilterContextManagerImpl extends CallFilterContextManager {
    public static final int PAGE_SIZE = 100;

    @Override
    public List<BlackListInfo> getBlackList() {
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        String sortOrder = CallFilterConstants.BLACK_ID + " " + CallFilterConstants.DESC;
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
        sb.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ? ");
        String selects = sb.toString();
        String[] selectArgs = new String[]{String.valueOf(CallFilterConstants.LOC_HD),
                String.valueOf(CallFilterConstants.REMOVE_NO)};
        return CallFilterUtils.getBlackList(uri, null, selects, selectArgs, sortOrder);
    }

    @Override
    public int getBlackListCount() {
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = null;
        int count = 0;
        try {
            Uri uri = CallFilterConstants.BLACK_LIST_URI;
            String sortOrder = CallFilterConstants.BLACK_ID + " " + CallFilterConstants.DESC;
            StringBuilder sb = new StringBuilder();
            sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
            sb.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ? ");
            String selects = sb.toString();
            String[] selectArgs = new String[]{String.valueOf(CallFilterConstants.LOC_HD),
                    String.valueOf(CallFilterConstants.REMOVE_NO)};
            cursor = cr.query(uri, null, selects, selectArgs, sortOrder);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    @Override
    public boolean addBlackList(List<BlackListInfo> blackList, boolean update) {
        if (blackList == null || blackList.size() <= 0) {
            return false;
        }

        for (BlackListInfo info : blackList) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
            int id = info.getId();
            String name = info.getNumberName();
            String number = PrivacyContactUtils.simpleFromateNumber(info.getNumber());
            Bitmap icon = info.getIcon();
            String area = info.getNumberArea();
            int uploadState = info.getUploadState();
            int locHd = info.getLocHandler();
            int locHdType = info.getLocHandlerType();
            int readState = info.getReadState();
            int removeState = info.getRemoveState();
            int filUpState = info.getFiltUpState();

            ContentResolver cr = mContext.getContentResolver();
            Uri uri = CallFilterConstants.BLACK_LIST_URI;
            ContentValues value = new ContentValues();

            if (!TextUtils.isEmpty(name)) {
                value.put(CallFilterConstants.BLACK_NAME, name);
            }
            if (!TextUtils.isEmpty(number)) {
                value.put(CallFilterConstants.BLACK_PHONE_NUMBER, number);
            }
            if (icon != null) {
                byte[] iconByte = PrivacyContactUtils.formateImg(icon);
                value.put(CallFilterConstants.BLACK_ICON, iconByte);
            }
            if (!TextUtils.isEmpty(area)) {
                value.put(CallFilterConstants.BLACK_NUMBER_AREA, area);
            }

            if (locHd != -1) {
                value.put(CallFilterConstants.BLACK_LOC_HD, locHd);
            }
            if (locHdType != -1) {
                value.put(CallFilterConstants.BLACK_LOC_HD_TYPE, locHdType);
            }
            //是否上传
            if (uploadState != -1) {
                value.put(CallFilterConstants.BLACK_UPLOAD_STATE, uploadState);
            }
            //是否删除
            if (removeState != -1) {
                value.put(CallFilterConstants.BLACK_REMOVE_STATE, CallFilterConstants.REMOVE);
            }
            //是否已读
            if (readState != -1) {
                value.put(CallFilterConstants.BLACK_READ_STATE, readState);
            }
            //是否为拦截上传
            if (filUpState != -1) {
                value.put(CallFilterConstants.BLACK_FIL_UP, filUpState);
            }

            try {
                String table = CallFilterConstants.BLACK_LIST_TAB;
                String numbColum = CallFilterConstants.BLACK_PHONE_NUMBER;
                String upColum = CallFilterConstants.BLACK_UPLOAD_STATE;
                String locHdTypeColum = CallFilterConstants.BLACK_LOC_HD_TYPE;
                String removeColum = CallFilterConstants.BLACK_REMOVE_STATE;
                SQLiteOpenHelper dbHelper = AppMasterDBHelper.getInstance(AppMasterApplication.getInstance());
                SQLiteDatabase sd = dbHelper.getReadableDatabase();
                StringBuilder sb = new StringBuilder();
                sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
                sb.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ? ");
                String sel = sb.toString();
                number = PrivacyContactUtils.formatePhoneNumber(number);
                Cursor cur = sd.query(table, new String[]{numbColum, upColum, locHdTypeColum, removeColum}, numbColum + " LIKE ? and " + sel,
                        new String[]{"%" + number, String.valueOf(CallFilterConstants.LOC_HD),
                                String.valueOf(CallFilterConstants.REMOVE_NO)}, null, null, null);

                if (cur != null && cur.getCount() > 0) {
                    while (cur.moveToNext()) {
                        int locHdTypeCom = cur.getColumnIndex(CallFilterConstants.BLACK_LOC_HD_TYPE);
                        int locHdTypeFlag = cur.getInt(locHdTypeCom);
                        //本地标记类型
                        if (locHdType != -1 && (locHdTypeFlag != locHdType)) {
                            value.put(CallFilterConstants.BLACK_UPLOAD_STATE, CallFilterConstants.UPLOAD_NO);
                        }
                        String where = null;
                        String[] selectArgs = null;
                        if (id > 0) {
                            where = CallFilterConstants.BLACK_ID + " = ? ";
                            selectArgs = new String[]{String.valueOf(id)};
                        } else {
                            where = CallFilterConstants.BLACK_PHONE_NUMBER + " LIKE ? ";
                            selectArgs = new String[]{"%" + number};
                        }
                        cr.update(CallFilterConstants.BLACK_LIST_URI, value, where, selectArgs);
                    }
                } else {
                    value.put(CallFilterConstants.BLACK_LOC_HD, CallFilterConstants.LOC_HD);
                    value.put(CallFilterConstants.BLACK_UPLOAD_STATE, CallFilterConstants.UPLOAD_NO);
                    value.put(CallFilterConstants.BLACK_REMOVE_STATE, CallFilterConstants.REMOVE_NO);
                    value.put(CallFilterConstants.BLACK_READ_STATE, CallFilterConstants.READ_NO);
                    value.put(CallFilterConstants.BLACK_FIL_UP, CallFilterConstants.FIL_UP_NO);
                    cr.insert(uri, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public boolean removeBlackList(List<BlackListInfo> blackList) {
        if (blackList == null || blackList.size() <= 0) {
            return false;
        }
        for (BlackListInfo info : blackList) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
            String number = info.getNumber();
            if (!TextUtils.isEmpty(number)) {
                List<BlackListInfo> infos = new ArrayList<BlackListInfo>();
                BlackListInfo black = new BlackListInfo();
                black.setId(info.getId());
                black.setNumber(number);
                black.setRemoveState(CallFilterConstants.REMOVE);
                infos.add(black);
                addBlackList(infos, true);
            }
        }

        return true;
    }

    @Override
    public boolean isExistBlackList(String number) {
//        SQLiteOpenHelper dbHelper = AppMasterDBHelper.getInstance(AppMasterApplication.getInstance());
//        SQLiteDatabase sd = dbHelper.getReadableDatabase();
//        String table = CallFilterConstants.BLACK_LIST_TAB;
//        String colum = CallFilterConstants.BLACK_PHONE_NUMBER;
//        CallFilterUtils.isDbKeyExist(table, new String[]{colum}, number);
//        number = PrivacyContactUtils.formatePhoneNumber(number);
//        StringBuilder sb = new StringBuilder();
//        sb.append(colum + " LIKE ? and ");
//        sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
//        sb.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ? ");
//        String selects = sb.toString();
//        String[] selectArgs = new String[]{"%" + number,
//                String.valueOf(CallFilterConstants.LOC_HD),
//                String.valueOf(CallFilterConstants.REMOVE_NO)};
//        Cursor cursor = sd.query(table, new String[]{colum}, selects, selectArgs, null, null, null);
//        if (cursor != null) {
//            if (cursor.getCount() > 0) {
//                return true;
//            }
//        }
        List<BlackListInfo> blacks = CallFilterManager.getInstance(mContext).getBlackList();
        if (blacks != null && blacks.size() > 0 && !TextUtils.isEmpty(number)) {
            String formateNum = PrivacyContactUtils.formatePhoneNumber(number);
            for (BlackListInfo info : blacks) {
                if (info.getNumber().contains(formateNum)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<BlackListInfo> getNoUploadBlackList() {
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.BLACK_UPLOAD_STATE + " = ? and ");
        sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
        sb.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ?");
        String selection = sb.toString();
        String[] selectionArgs = new String[]{String.valueOf(CallFilterConstants.UPLOAD_NO),
                String.valueOf(CallFilterConstants.LOC_HD), String.valueOf(CallFilterConstants.REMOVE_NO)};

        return CallFilterUtils.getNoUpBlack(uri, selection, selectionArgs, null);
    }

    @Override
    public BlackListInfo getBlackListFroNum(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        String formateNum = PrivacyContactUtils.formatePhoneNumber(number);
        BlackListInfo info = new BlackListInfo();
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        String sortOrder = CallFilterConstants.BLACK_ID + " " + CallFilterConstants.DESC;
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
        sb.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ? and ");
        sb.append(CallFilterConstants.BLACK_PHONE_NUMBER + " LIKE ? ");
        String selects = sb.toString();
        String[] selectArgs = new String[]{String.valueOf(CallFilterConstants.LOC_HD),
                String.valueOf(CallFilterConstants.REMOVE_NO), ("%" + formateNum)};
        List<BlackListInfo> infos = CallFilterUtils.getBlackList(uri, null, selects, selectArgs, sortOrder);
        if (infos != null && infos.size() > 0) {
            for (BlackListInfo black : infos) {
                info = black;
                break;
            }
        }

        return info;
    }

    @Override
    public List<BlackListInfo> getNoUpBlackListLimit(int page) {
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.BLACK_UPLOAD_STATE + " = ? and ");
        sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
        sb.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ?");
        String selection = sb.toString();
        String[] selectionArgs = new String[]{String.valueOf(CallFilterConstants.UPLOAD_NO),
                String.valueOf(CallFilterConstants.LOC_HD), String.valueOf(CallFilterConstants.REMOVE_NO)};
        int pageSize = PAGE_SIZE;
        int currentOffset = (page - 1) * PAGE_SIZE;
        StringBuilder sbOr = new StringBuilder();
        sbOr.append(CallFilterConstants.BLACK_ID);
        sbOr.append(" " + CallFilterConstants.DESC);
        sbOr.append(" limit  " + pageSize + " offset " + currentOffset);
        String sortOrder = sbOr.toString();
        return CallFilterUtils.getNoUpBlack(uri, selection, selectionArgs, sortOrder);
    }

    @Override
    public List<CallFilterInfo> getCallFilterGrList() {
        ContentResolver cr = mContext.getContentResolver();
        List<CallFilterInfo> infoList = null;
        Cursor cursor = null;
        try {
            Uri uri = CallFilterConstants.FILTER_GROUP_URI;
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = CallFilterConstants.FIL_GR_DATE + " " + CallFilterConstants.DESC;
            cursor = cr.query(uri, null, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                infoList = new ArrayList<CallFilterInfo>();
                while (cursor.moveToNext()) {
                    int idColum = cursor.getColumnIndex(CallFilterConstants.FIL_GR_ID);
                    int nameColum = cursor.getColumnIndex(CallFilterConstants.FIL_GR_NAME);
                    int numberColum = cursor.getColumnIndex(CallFilterConstants.FIL_GR_PH_NUMB);
                    int numberAreaColum = cursor.getColumnIndex(CallFilterConstants.FIL_GR_NUM_AREA);
                    int blackIdColum = cursor.getColumnIndex(CallFilterConstants.FIL_GR_TO_BLACK_ID);
                    int filterNumberColum = cursor.getColumnIndex(CallFilterConstants.FIL_NUMBER);
                    int dateColum = cursor.getColumnIndex(CallFilterConstants.FIL_GR_DATE);
                    int durationColum = cursor.getColumnIndex(CallFilterConstants.FIL_CALL_DURATION);
                    int callTypeColum = cursor.getColumnIndex(CallFilterConstants.FIL_CALL_TYPE);
                    int readStateColum = cursor.getColumnIndex(CallFilterConstants.FIL_READ_STATE);
//                    int filterTypeColum = cursor.getColumnIndex(CallFilterConstants.FIL_GR_TYPE);

                    int id = cursor.getInt(idColum);
//                    String name = cursor.getString(nameColum);
                    String number = cursor.getString(numberColum);
                    String numberArea = cursor.getString(numberAreaColum);
                    int blackId = cursor.getInt(blackIdColum);
                    int filterNumber = cursor.getInt(filterNumberColum);
                    long date = cursor.getLong(dateColum);
                    long duration = cursor.getLong(durationColum);
                    int callType = cursor.getInt(callTypeColum);
                    int readState = cursor.getInt(readStateColum);
                    int filterType = -1;
                    String name = null;
                    BlackListInfo black = getBlackListFroNum(number);
                    if (black != null) {
                        filterType = black.getLocHandlerType();
                        name = black.getNumberName();
                        if (!TextUtils.isEmpty(black.getNumberName())) {
                            name = black.getNumberName();
                        }
                    }
                    Bitmap icon = getBlackIcon(number);

                    /*默认值-1*/
                    int filterGrId = -1;
                    CallFilterInfo filterInfo = CallFilterUtils.getFilterInfo(id, name, number, numberArea, blackId,
                            filterNumber, date, duration, callType, readState, filterType, filterGrId, icon);
                    infoList.add(filterInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return infoList;
    }

    @Override
    public int getCallFilterGrCount() {
        ContentResolver cr = mContext.getContentResolver();
        int count = 0;
        Cursor cursor = null;
        try {
            Uri uri = CallFilterConstants.FILTER_GROUP_URI;
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;
            cursor = cr.query(uri, null, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    @Override
    public boolean addFilterGr(List<CallFilterInfo> infos, boolean update) {
        if (infos == null || infos.size() <= 0) {
            return false;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.FILTER_GROUP_URI;
        ContentValues values = new ContentValues();
        String where = null;
        String[] selectionArgs = null;
        for (CallFilterInfo info : infos) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
            String name = info.getNumberName();
            String number = PrivacyContactUtils.simpleFromateNumber(info.getNumber());
            int blackId = info.getBlackId();
            int callType = info.getCallType();
            long duration = info.getDuration();
            int filterCount = info.getFilterCount();
            int filterType = info.getFilterType();
            String numberType = info.getNumberType();
            long date = info.getTimeLong();
            int isRead = info.getReadState();

            values.put(CallFilterConstants.FIL_GR_NAME, name);
            values.put(CallFilterConstants.FIL_GR_PH_NUMB, number);
            values.put(CallFilterConstants.FIL_GR_TO_BLACK_ID, blackId);
            values.put(CallFilterConstants.FIL_NUMBER, filterCount);
            values.put(CallFilterConstants.FIL_GR_DATE, date);
            values.put(CallFilterConstants.FIL_CALL_DURATION, duration);
            values.put(CallFilterConstants.FIL_CALL_TYPE, callType);
            values.put(CallFilterConstants.FIL_GR_TYPE, filterType);
            values.put(CallFilterConstants.FIL_GR_NUM_AREA, numberType);
            if (isRead != -1) {
                values.put(CallFilterConstants.FIL_READ_STATE, isRead);
            }

            try {
                if (update) {
                    String table = CallFilterConstants.FILTER_GROUP_TAB;
                    String colum1 = CallFilterConstants.FIL_GR_PH_NUMB;
                    String colum2 = CallFilterConstants.FIL_GR_DATE;
                    String colum3 = CallFilterConstants.FIL_CALL_TYPE;
                    String colum4 = CallFilterConstants.FIL_NUMBER;
                    Cursor cur = CallFilterUtils.getCursor(table, new String[]{colum1, colum2, colum3, colum4}, number);
                    boolean isKeyExist = (cur != null) ? cur.getCount() > 0 : false;
                    if (isKeyExist) {
                        while (cur.moveToNext()) {
                            int dateColum = cur.getColumnIndex(CallFilterConstants.FIL_GR_DATE);
                            int callTypeColum = cur.getColumnIndex(CallFilterConstants.FIL_CALL_TYPE);
                            int filterNumberColum = cur.getColumnIndex(CallFilterConstants.FIL_NUMBER);
                            long grDate = cur.getLong(dateColum);
                            int grCallType = cur.getInt(callTypeColum);
                            int filterCountT = cur.getInt(filterNumberColum);
                            filterCountT = filterCountT + 1;
                            where = CallFilterConstants.FIL_GR_PH_NUMB + " LIKE ? ";
                            selectionArgs = new String[]{"%" + PrivacyContactUtils.formatePhoneNumber(number)};
                            ContentValues upValues = new ContentValues();
                            upValues.put(CallFilterConstants.FIL_GR_PH_NUMB, number);
                            upValues.put(CallFilterConstants.FIL_NUMBER, filterCountT);
                            if (date > grDate) {
                                upValues.put(CallFilterConstants.FIL_GR_DATE, date);
                                if (grCallType != -1) {
                                    upValues.put(CallFilterConstants.FIL_CALL_TYPE, grCallType);
                                }
                            }
                            cr.update(uri, upValues, where, selectionArgs);
                        }
                    } else {
                        values.put(CallFilterConstants.FIL_NUMBER, 1);
                        cr.insert(uri, values);
                    }
                } else {
                    values.put(CallFilterConstants.FIL_NUMBER, 1);
                    cr.insert(uri, values);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }

        return true;
    }

    @Override
    public boolean removeFilterGr(List<CallFilterInfo> infos) {
        if (infos == null || infos.size() <= 0) {
            return false;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.FILTER_GROUP_URI;
        String selection = null;
        String[] selectionArgs = null;

        for (CallFilterInfo info : infos) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
//            int id = info.getId();
            String number = info.getNumber();

//            if (id >= 0) {
//                selection = CallFilterConstants.FIL_GR_ID + " = ? ";
//                selectionArgs = new String[]{String.valueOf(id)};
//                List<CallFilterInfo> calls = new ArrayList<CallFilterInfo>();
//                CallFilterInfo call = new CallFilterInfo();
//                call.setId(id);
//                calls.add(call);
//                removeFilterDet(calls);
//            } else
            if (!TextUtils.isEmpty(number)) {
                selection = CallFilterConstants.FIL_GR_PH_NUMB + " LIKE ? ";
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                selectionArgs = new String[]{"%" + formateNumber};
                List<CallFilterInfo> calls = new ArrayList<CallFilterInfo>();
                CallFilterInfo call = new CallFilterInfo();
                call.setNumber(number);
                calls.add(call);
                removeFilterDet(calls);
            }
            cr.delete(uri, selection, selectionArgs);

        }
        return true;
    }

    @Override
    public List<CallFilterInfo> getFilterDetList() {
        List<CallFilterInfo> filterInfos = null;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.FILTER_DETAIL_URI;
        Cursor cursor = null;
        String where = null;
        String[] selectionArgs = null;
        String sortOrder = CallFilterConstants.FIL_DET_DATE + " " + CallFilterConstants.DESC;
        try {
            cursor = cr.query(uri, null, where, selectionArgs, sortOrder);
            if (cursor != null) {
                filterInfos = new ArrayList<CallFilterInfo>();
                while (cursor.moveToNext()) {

                    int idColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_ID);
                    int numberColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_PHONE_NUMBER);
                    int nameColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_NAME);
                    int numberAreaColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_NUM_AREA);
                    int filterGrIdColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_TO_GR_ID);
                    int dateColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_DATE);
                    int durationColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_DURATION);
                    int callTypeColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_CALL_TYPE);
                    int readStateColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_READ_STATE);
                    int filterTypeColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_TYPE);

                    int id = cursor.getInt(idColum);
                    String name = cursor.getString(nameColum);
                    String number = cursor.getString(numberColum);
                    if (TextUtils.isEmpty(name)) {
                        name = number;
                    }
                    String numberArea = cursor.getString(numberAreaColum);
                    int filterGrId = cursor.getInt(filterGrIdColum);
                    long date = cursor.getLong(dateColum);
                    long duration = cursor.getLong(durationColum);
                    int callType = cursor.getInt(callTypeColum);
                    int readState = cursor.getInt(readStateColum);
                    int filterType = cursor.getInt(filterTypeColum);
                    Bitmap icon = getBlackIcon(number);
                    int filterNumber = -1;
                    int blackId = -1;
                    CallFilterInfo filterInfo = CallFilterUtils.getFilterInfo(id, name, number, numberArea, blackId,
                            filterNumber, date, duration, callType, readState, filterType, filterGrId, icon);
                    filterInfos.add(filterInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return filterInfos;
    }

    @Override
    public List<CallFilterInfo> getFilterDetListFroNum(String number) {
        List<CallFilterInfo> filterInfos = null;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.FILTER_DETAIL_URI;
        String numberFromate = PrivacyContactUtils.formatePhoneNumber(number);
        Cursor cursor = null;
        String where = CallFilterConstants.FIL_DET_PHONE_NUMBER + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + numberFromate};
//        String where = null;
//        String[] selectionArgs = null;
        String sortOrder = CallFilterConstants.FIL_DET_DATE + " " + CallFilterConstants.DESC;
        try {
            cursor = cr.query(uri, null, where, selectionArgs, sortOrder);
            if (cursor != null && cursor.getCount() > 0) {
                filterInfos = new ArrayList<CallFilterInfo>();
                while (cursor.moveToNext()) {

                    int idColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_ID);
                    int numberColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_PHONE_NUMBER);
                    int nameColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_NAME);
                    int numberAreaColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_NUM_AREA);
                    int filterGrIdColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_TO_GR_ID);
                    int dateColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_DATE);
                    int durationColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_DURATION);
                    int callTypeColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_CALL_TYPE);
                    int readStateColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_READ_STATE);
                    int filterTypeColum = cursor.getColumnIndex(CallFilterConstants.FIL_DET_TYPE);

                    int id = cursor.getInt(idColum);
                    String name = cursor.getString(nameColum);
                    String numberN = cursor.getString(numberColum);
                    if (TextUtils.isEmpty(name)) {
                        name = number;
                    }
                    String numberArea = cursor.getString(numberAreaColum);
                    int filterGrId = cursor.getInt(filterGrIdColum);
                    long date = cursor.getLong(dateColum);
                    long duration = cursor.getLong(durationColum);
                    int callType = cursor.getInt(callTypeColum);
                    int readState = cursor.getInt(readStateColum);
//                    int filterType = cursor.getInt(filterTypeColum);
                    int filterType = -1;
                    BlackListInfo black = getBlackListFroNum(number);
                    if (black != null) {
                        filterType = black.getLocHandlerType();
                    }
                    Bitmap icon = getBlackIcon(number);
                    int filterNumber = -1;
                    int blackId = -1;
                    CallFilterInfo filterInfo = CallFilterUtils.getFilterInfo(id, name, numberN, numberArea, blackId,
                            filterNumber, date, duration, callType, readState, filterType, filterGrId, icon);
                    filterInfos.add(filterInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return filterInfos;
    }

    @Override
    public int getFilterDetCount() {
        int count = 0;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.FILTER_DETAIL_URI;
        Cursor cursor = null;
        String where = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        try {
            cursor = cr.query(uri, null, where, selectionArgs, sortOrder);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    @Override
    public boolean addFilterDet(List<CallFilterInfo> infos, boolean update) {
        if (infos == null || infos.size() <= 0) {
            return false;
        }
        List<CallFilterInfo> addFilterGr = new ArrayList<CallFilterInfo>();
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.FILTER_DETAIL_URI;
        ContentValues values = new ContentValues();
        String where = null;
        String[] selectionArgs = null;
        for (CallFilterInfo info : infos) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
            String name = info.getNumberName();
            String number = PrivacyContactUtils.simpleFromateNumber(info.getNumber());
            int filterGrId = info.getFilterGrId();
            int callType = info.getCallType();
            long duration = info.getDuration();
            int filterType = info.getFilterType();
            String numberType = info.getNumberType();
            long date = info.getTimeLong();
            int isRead = info.getReadState();
            if (!TextUtils.isEmpty(number)) {
                values.put(CallFilterConstants.FIL_DET_PHONE_NUMBER, number);
            }
            if (!TextUtils.isEmpty(name)) {
                values.put(CallFilterConstants.FIL_DET_NAME, name);
            }
            if (!TextUtils.isEmpty(numberType)) {
                values.put(CallFilterConstants.FIL_DET_NUM_AREA, numberType);
            }
            if (filterGrId != -1) {
                values.put(CallFilterConstants.FIL_DET_TO_GR_ID, filterGrId);
            }
            if (date > 0) {
                values.put(CallFilterConstants.FIL_DET_DATE, date);
            }
            if (duration > 0) {
                values.put(CallFilterConstants.FIL_DET_DURATION, duration);
            }
            if (callType != -1) {
                values.put(CallFilterConstants.FIL_DET_CALL_TYPE, callType);
            }
            values.put(CallFilterConstants.FIL_DET_TYPE, filterType);
            if (isRead != -1) {
                values.put(CallFilterConstants.FIL_DET_READ_STATE, isRead);
            }
            try {
                String table = CallFilterConstants.FILTER_DETAIL_TAB;
                String colum1 = CallFilterConstants.FIL_DET_PHONE_NUMBER;
                String colum2 = CallFilterConstants.FIL_DET_DATE;

                boolean isKeyExist = CallFilterUtils.isDbKeyExist(table, new String[]{colum1, colum2}, number);
                if (update && isKeyExist) {
                    where = CallFilterConstants.FIL_DET_PHONE_NUMBER + " LIKE ? ";
                    String[] selectArgs = new String[]{String.valueOf("%" + number)};
                    cr.update(uri, values, where, selectionArgs);
                } else {
                    cr.insert(uri, values);
                }
                addFilterGr.add(info);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
        if (addFilterGr.size() > 0) {
            addFilterGr(addFilterGr, true);
        }
        return true;
    }

    @Override
    public boolean removeFilterDet(List<CallFilterInfo> infos) {
        if (infos == null || infos.size() <= 0) {
            return false;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.FILTER_DETAIL_URI;
        String selection = null;
        String[] selectionArgs = null;

        for (CallFilterInfo info : infos) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
            int id = info.getId();
            String number = info.getNumber();

//            if (id >= 0) {
//                selection = CallFilterConstants.FIL_DET_ID + " = ? ";
//                selectionArgs = new String[]{String.valueOf(id)};
//            } else
            if (!TextUtils.isEmpty(number)) {
                selection = CallFilterConstants.FIL_DET_PHONE_NUMBER + " LIKE ? ";
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                selectionArgs = new String[]{"%" + formateNumber};
            }
            cr.delete(uri, selection, selectionArgs);
        }

        return true;
    }

    @Override
    public List<StrangerInfo> getStrangerGrList() {
        List<StrangerInfo> starInfos = null;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_TP_URI;
        Cursor cursor = null;
        String where = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        try {
            cursor = cr.query(uri, null, where, selectionArgs, sortOrder);

            if (cursor != null) {
                starInfos = new ArrayList<StrangerInfo>();
                while (cursor.moveToNext()) {

                    int idColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_ID);
                    int nmberColum = cursor.getColumnIndex(CallFilterConstants.STR_TP_NUM);
                    int dateColum = cursor.getColumnIndex(CallFilterConstants.STR_TP_DATE);
                    int tipColum = cursor.getColumnIndex(CallFilterConstants.STR_TP_STATE);
                    int tipTypeColum = cursor.getColumnIndex(CallFilterConstants.STR_TP_TYPE);

                    int id = cursor.getInt(idColum);
                    String number = cursor.getString(nmberColum);
                    long date = cursor.getLong(dateColum);
                    int tipState = cursor.getInt(tipColum);
                    int type = cursor.getInt(tipTypeColum);

                    StrangerInfo straInfo = CallFilterUtils.getStrangInfo(id, number, date, tipState, type);
                    starInfos.add(straInfo);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return starInfos;
    }

    @Override
    public int getStranagerGrCount() {
        int count = 0;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_TP_URI;
        Cursor cursor = null;
        String where = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        try {
            cursor = cr.query(uri, null, where, selectionArgs, sortOrder);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    @Override
    public boolean addStrangerGr(List<StrangerInfo> infos) {
        if (infos == null || infos.size() <= 0) {
            return false;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_TP_URI;
        ContentValues values = new ContentValues();
        String where = null;
        String[] selectionArgs = null;

        try {
            for (StrangerInfo info : infos) {
                if (TextUtils.isEmpty(info.getNumber())) {
                    continue;
                }

                String number = PrivacyContactUtils.simpleFromateNumber(info.getNumber());
                long date = info.getDate();
                int tipState = info.getTipState();
                int tipType = info.getTipType();

                values.put(CallFilterConstants.STR_TP_NUM, number);
                values.put(CallFilterConstants.STR_TP_DATE, date);

                if (tipState != -1) {
                    values.put(CallFilterConstants.STR_TP_STATE, tipState);
                }
                values.put(CallFilterConstants.STR_TP_TYPE, tipType);

                String table = CallFilterConstants.STRANGER_TP_TAB;
                String colum = CallFilterConstants.STR_TP_NUM;

                boolean isKeyExist = CallFilterUtils.isDbKeyExist(table, new String[]{colum}, number);
                if (isKeyExist) {
                    String numFormate = PrivacyContactUtils.formatePhoneNumber(number);
                    where = CallFilterConstants.STR_TP_NUM + " LIKE ? ";
                    selectionArgs = new String[]{"%" + numFormate};
                    cr.update(uri, values, where, selectionArgs);
                } else {
                    cr.insert(uri, values);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean removeStrangerGr(List<StrangerInfo> infos) {
        if (infos == null || infos.size() <= 0) {
            return false;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_TP_URI;
        String selection = null;
        String[] selectionArgs = null;

        for (StrangerInfo info : infos) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
            int id = info.getId();
            String number = info.getNumber();

            if (id >= 0) {
                selection = CallFilterConstants.STR_GR_ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(id)};
            } else if (!TextUtils.isEmpty(number)) {
                selection = CallFilterConstants.STR_TP_NUM + " LIKE ? ";
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                selectionArgs = new String[]{"%" + formateNumber};
            }
            cr.delete(uri, selection, selectionArgs);
        }

        return true;
    }


    @Override
    public boolean getFilterOpenState() {
        PreferenceTable pre = PreferenceTable.getInstance();
        return pre.getBoolean(PrefConst.KEY_FIL_OP_STA, true);
    }

    @Override
    public void setFilterOpenState(boolean flag) {
        PreferenceTable pre = PreferenceTable.getInstance();
        pre.putBoolean(PrefConst.KEY_FIL_OP_STA, flag);
    }

    @Override
    public boolean getFilterNotiOpState() {
        PreferenceTable pre = PreferenceTable.getInstance();
        return pre.getBoolean(PrefConst.KEY_FIL_NOTI_STATE, true);
    }

    @Override
    public void setFilterNotiOpState(boolean flag) {
        PreferenceTable pre = PreferenceTable.getInstance();
        pre.putBoolean(PrefConst.KEY_FIL_NOTI_STATE, flag);
    }

    @Override
    public int[] isCallFilterTip(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        boolean isUse = CallFilterUtils.isNumberUsePrivacy(number);
//        boolean isLocBlack =
        if (isUse) {
            return null;
        }
        CallFilterManager cm = CallFilterManager.getInstance(mContext);
        boolean isTip = cm.isIsCallFilterTip();
        cm.setIsCallFilterTip(true);
        int[] type = new int[4];
//        if (!isTip) {
        type[0] = -1;
        int addBlackCount = 0;
        int markCount = 0;
        int markType = -1;
        BlackListInfo blacks = cm.getSerBlackForNum(number);
        if (blacks != null) {
            addBlackCount = blacks.getAddBlackNumber();
            markCount = blacks.getMarkerNumber();
            markType = blacks.getMarkerType();
        } else {
            return null;
        }

        int filUser = getFilterUserNumber();
        int filUserPar = getFilterTipFroUser();
        int blackTip = getSerBlackTipCount();
        int markerTip = getSerMarkTipCount();
        int showPar = getBlackMarkTipParam();
        type[1] = -1;
        if ((filUser >= filUserPar) && (blackTip > 0 || markerTip > 0)) {
            if (markCount >= markerTip) {
                //优先显示标记
                type[1] = CallFilterConstants.DIALOG_TYPE[0];
                type[0] = CallFilterConstants.IS_TIP_DIA[1];
                type[2] = markCount * showPar;
                type[3] = markType;
            } else {
                if (addBlackCount >= blackTip) {
                    //显示黑名单
                    type[1] = CallFilterConstants.DIALOG_TYPE[1];
                    type[0] = CallFilterConstants.IS_TIP_DIA[1];
                    type[2] = addBlackCount * showPar;
                    type[3] = markType;
                } else {
                    type[0] = CallFilterConstants.IS_TIP_DIA[0];
                }
            }
        } else {
            type[0] = CallFilterConstants.IS_TIP_DIA[0];
        }
        cm.setIsCallFilterTip(false);
//        }
        return type;
    }

    @Override
    public long getCallDurationMax() {
        PreferenceTable pt = PreferenceTable.getInstance();
        return pt.getLong(PrefConst.KEY_FIL_TIME_PAR, 0);
    }

    @Override
    public long setCallDurationMax(long duration) {
        PreferenceTable pt = PreferenceTable.getInstance();
        pt.putLong(PrefConst.KEY_FIL_TIME_PAR, duration);
        return 0;
    }

    @Override
    public int getStraNotiTipParam() {
        PreferenceTable pt = PreferenceTable.getInstance();
        return pt.getInt(PrefConst.KEY_STRA_NOTI_PAR, -1);
    }

    @Override
    public void setStraNotiTipParam(int params) {
        PreferenceTable pt = PreferenceTable.getInstance();
        pt.putInt(PrefConst.KEY_STRA_NOTI_PAR, params);
    }

    @Override
    public int getBlackMarkTipParam() {
        PreferenceTable pt = PreferenceTable.getInstance();
        return pt.getInt(PrefConst.KEY_BLK_MARK_TIP, -1);
    }

    @Override
    public void setBlackMarkTipParam(int number) {
        PreferenceTable pt = PreferenceTable.getInstance();
        pt.putInt(PrefConst.KEY_BLK_MARK_TIP, number);
    }

    @Override
    public int getFilterUserNumber() {
        PreferenceTable pt = PreferenceTable.getInstance();
        return pt.getInt(PrefConst.KEY_FILTER_USER, -1);
    }

    @Override
    public int getFilterTipFroUser() {
        PreferenceTable pt = PreferenceTable.getInstance();
        return pt.getInt(PrefConst.KEY_FILTER_TIP_USER, -1);
    }

    @Override
    public List<BlackListInfo> getServerBlackList() {
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        String sortOrder = CallFilterConstants.BLACK_ID + " " + CallFilterConstants.DESC;
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? ");
        String selects = sb.toString();
        String[] selectArgs = new String[]{String.valueOf(CallFilterConstants.NO_LOC_HD)};
        return CallFilterUtils.getBlackList(uri, null, selects, selectArgs, sortOrder);
    }

    @Override
    public boolean addSerBlackList(List<BlackListInfo> infos) {
        if (infos == null || infos.size() <= 0) {
            return false;
        }

        for (BlackListInfo info : infos) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
            String name = info.getNumberName();
            String number = PrivacyContactUtils.simpleFromateNumber(info.getNumber());
            boolean isContactUse = CallFilterUtils.isNumberUsePrivacy(number);
            if (isContactUse) {
                continue;
            }
            int addBlackNumber = info.getAddBlackNumber();
            int markerType = info.getMarkerType();
            int markerNum = info.getMarkerNumber();

            ContentResolver cr = mContext.getContentResolver();
            Uri uri = CallFilterConstants.BLACK_LIST_URI;
            ContentValues value = new ContentValues();
            if (!TextUtils.isEmpty(name)) {
                value.put(CallFilterConstants.BLACK_NAME, name);
            }
            if (!TextUtils.isEmpty(number)) {
                value.put(CallFilterConstants.BLACK_PHONE_NUMBER, number);
            }
            value.put(CallFilterConstants.BLACK_ADD_NUMBER, addBlackNumber);
            value.put(CallFilterConstants.MARKER_TYPE, markerType);
            value.put(CallFilterConstants.MARKER_NUMBER, markerNum);

            try {
                String table = CallFilterConstants.BLACK_LIST_TAB;
                String colum = CallFilterConstants.BLACK_PHONE_NUMBER;
                String removeColum = CallFilterConstants.BLACK_REMOVE_STATE;
                Cursor cur = CallFilterUtils.getCursor(table, new String[]{colum, removeColum}, number);
                if (cur != null && cur.getCount() > 0) {
                    while (cur.moveToNext()) {
                        int remove = cur.getInt(cur.getColumnIndex(removeColum));
                        if (CallFilterConstants.REMOVE == remove) {
                            value.put(CallFilterConstants.BLACK_LOC_HD, CallFilterConstants.NO_LOC_HD);
                        }
                        String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                        String where = CallFilterConstants.BLACK_PHONE_NUMBER + " LIKE ? ";
                        String[] selectArgs = new String[]{String.valueOf("%" + formateNumber)};
                        cr.update(CallFilterConstants.BLACK_LIST_URI, value, where, selectArgs);
                    }
                } else {
                    value.put(CallFilterConstants.BLACK_LOC_HD, CallFilterConstants.NO_LOC_HD);
                    cr.insert(uri, value);
                }
                if (cur != null) {
                    cur.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return false;
    }

    @Override
    public boolean removeSerBlackList(List<BlackListInfo> infos) {

        return false;
    }

    @Override
    public List<BlackListInfo> getSerBlackListFroNum(String number) {
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        String sortOrder = CallFilterConstants.BLACK_ID + " " + CallFilterConstants.DESC;
        String formateNum = PrivacyContactUtils.formatePhoneNumber(number);
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.BLACK_PHONE_NUMBER + " LIKE ? and ");
        sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? ");
        String selects = sb.toString();
        String[] selectArgs = new String[]{"%" + String.valueOf(formateNum), String.valueOf(CallFilterConstants.NO_LOC_HD)};
        return CallFilterUtils.getBlackList(uri, null, selects, selectArgs, sortOrder);
    }

    @Override
    public int getSerBlackNumFroNum(String number) {
        List<BlackListInfo> infos = getSerBlackListFroNum(number);
        int addBlackNum = 0;
        for (BlackListInfo info : infos) {
            addBlackNum = info.getAddBlackNumber();
            break;
        }
        int params = getBlackMarkTipParam();
        return addBlackNum * params;
    }

    @Override
    public int getSerMarkerNumFroNum(String number) {
        List<BlackListInfo> infos = getSerBlackListFroNum(number);
        int addMarkerNum = 0;
        for (BlackListInfo info : infos) {
            addMarkerNum = info.getMarkerNumber();
            break;
        }
        int params = getBlackMarkTipParam();
        return addMarkerNum * params;
    }

    @Override
    public int getSerBlackTipCount() {
        PreferenceTable pt = PreferenceTable.getInstance();
        return pt.getInt(PrefConst.KEY_BLACK_TIP, -1);
    }

    @Override
    public int getSerMarkTipCount() {
        PreferenceTable pt = PreferenceTable.getInstance();
        return pt.getInt(PrefConst.KEY_MARKER_TIP, -1);
    }

    @Override
    public void setSerMarkTipNum(int num) {
        PreferenceTable pt = PreferenceTable.getInstance();
        pt.putInt(PrefConst.KEY_MARKER_TIP, num);
    }

    @Override
    public void setSerBlackTipNum(int num) {
        PreferenceTable pt = PreferenceTable.getInstance();
        pt.putInt(PrefConst.KEY_BLACK_TIP, num);
    }

    @Override
    public void setFilterTipFroUser(int number) {
        PreferenceTable pt = PreferenceTable.getInstance();
        pt.putInt(PrefConst.KEY_FILTER_TIP_USER, number);
    }

    @Override
    public void setFilterUserNumber(int number) {
        PreferenceTable pt = PreferenceTable.getInstance();
        pt.putInt(PrefConst.KEY_FILTER_USER, number);
    }

    @Override
    public void setSerBlackFilePath(String filePath) {
        PreferenceTable pt = PreferenceTable.getInstance();
        pt.putString(PrefConst.KEY_SER_BLK_PATH, filePath);
    }

    @Override
    public String getSerBlackFilePath() {
        PreferenceTable pt = PreferenceTable.getInstance();
        return pt.getString(PrefConst.KEY_SER_BLK_PATH);
    }

    @Override
    public boolean isPrivacyConUse(String number) {
        return CallFilterUtils.isNumberUsePrivacy(number);
    }

    @Override
    public boolean insertCallToSys(CallFilterInfo info) {
        ContentResolver cr = mContext.getContentResolver();
        try {
//            int nameColum = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            ContentValues values = new ContentValues();
            values.put(CallLog.Calls.NUMBER, info.getNumber());
            values.put(CallLog.Calls.TYPE, info.getCallType());
            values.put(CallLog.Calls.DATE, info.getTimeLong());
            values.put(CallLog.Calls.DURATION, info.getDuration());
            cr.insert(PrivacyContactUtils.CALL_LOG_URI, values);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public BlackListInfo getSerBlackFroNum(String number) {
        CallFilterManager cm = CallFilterManager.getInstance(mContext);
        return cm.getSerBlackForNum(number);
    }

    @Override
    public Bitmap getBlackIcon(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        CallFilterManager cm = CallFilterManager.getInstance(mContext);
        BlackListInfo info = cm.getBlackFroNum(number);
        if (info != null) {
            Bitmap icon = info.getIcon();
            if (icon != null) {
                return icon;
            }
        }
        return null;
    }

    @Override
    public List<BlackListInfo> getUpBlackListLimit(int page) {
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.BLACK_UPLOAD_STATE + " = ? and ");
        sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
        sb.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ?");
        String selection = sb.toString();
        String[] selectionArgs = new String[]{String.valueOf(CallFilterConstants.UPLOAD),
                String.valueOf(CallFilterConstants.LOC_HD), String.valueOf(CallFilterConstants.REMOVE_NO)};
        int pageSize = PAGE_SIZE;
        int currentOffset = (page - 1) * PAGE_SIZE;
        StringBuilder sbOr = new StringBuilder();
        sbOr.append(CallFilterConstants.BLACK_ID);
        sbOr.append(" " + CallFilterConstants.DESC);
        sbOr.append(" limit  " + pageSize + " offset " + currentOffset);
        String sortOrder = sbOr.toString();
        return CallFilterUtils.getNoUpBlack(uri, selection, selectionArgs, sortOrder);
    }

    @Override
    public List<BlackListInfo> getUploadBlackList() {
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.BLACK_UPLOAD_STATE + " = ? and ");
        sb.append(CallFilterConstants.BLACK_LOC_HD + " = ? and ");
        sb.append(CallFilterConstants.BLACK_REMOVE_STATE + " = ?");
        String selection = sb.toString();
        String[] selectionArgs = new String[]{String.valueOf(CallFilterConstants.UPLOAD),
                String.valueOf(CallFilterConstants.LOC_HD), String.valueOf(CallFilterConstants.REMOVE_NO)};
        return CallFilterUtils.getNoUpBlack(uri, selection, selectionArgs, null);
    }
}
