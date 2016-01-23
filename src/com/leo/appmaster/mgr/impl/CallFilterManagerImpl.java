package com.leo.appmaster.mgr.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.CallLog;
import android.text.TextUtils;

import com.leo.appmaster.callfilter.BlackListInfo;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.callfilter.CallFilterInfo;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.callfilter.StrangerInfo;
import com.leo.appmaster.cloud.crypto.CryptoUtils;
import com.leo.appmaster.db.BlacklistTab;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.CallFilterManager;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by runlee on 15-12-18.
 */
public class CallFilterManagerImpl extends CallFilterManager {
    public static final int PAGE_SIZE = 100;
    private static final String TAG = "CallFilterManager";

    @Override
    public List<BlackListInfo> getBlackList() {
        String selection = CallFilterConstants.COL_BLACK_REMOVE_STATE + " = ?";
        String[] selectionArgs = new String[]{CallFilterConstants.REMOVE_NO + ""};

        String sortOrder = CallFilterConstants.COL_TIME + " " + CallFilterConstants.DESC;
        return BlacklistTab.getInstance().getBlackList(selection, selectionArgs, sortOrder);
    }

    @Override
    public int getBlackListCount() {
        String selection = CallFilterConstants.COL_BLACK_REMOVE_STATE + " = ?";
        String[] selectionArgs = new String[]{CallFilterConstants.REMOVE_NO + ""};
        return BlacklistTab.getInstance().getBlackListCount(selection, selectionArgs);
    }

    @Override
    public boolean addBlackList(List<BlackListInfo> blackList, boolean update) {
        if (blackList == null || blackList.size() <= 0) {
            LeoLog.d(TAG, "addBlackList is empty. ");
            return false;
        }
        LeoLog.d(TAG, "addBlackList....size: " + blackList.size());

        BlacklistTab.getInstance().addBlackList(blackList);
        return true;
    }

    @Override
    public boolean removeBlackList(List<BlackListInfo> blackList) {
        BlacklistTab.getInstance().deleteBlackList(blackList);
        return true;
    }

    @Override
    public boolean isExistBlackList(String number) {
        return BlacklistTab.getInstance().isBlackInfoExist(number);
    }

    @Override
    public List<BlackListInfo> getNoUploadBlackList() {
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.COL_BLACK_UPLOAD_STATE + " = ? AND ");
        sb.append(CallFilterConstants.COL_BLACK_REMOVE_STATE + " = ?");
        String selection = sb.toString();
        String[] selectionArgs = new String[]{String.valueOf(CallFilterConstants.UPLOAD_NO),
                String.valueOf(CallFilterConstants.REMOVE_NO)};

        return BlacklistTab.getInstance().getBlackList(selection, selectionArgs, null);
    }

    @Override
    public BlackListInfo getBlackListFroNum(String number) {
        return BlacklistTab.getInstance().getBlackInfoByNumber(number);
    }

    @Override
    public List<BlackListInfo> getNoUpBlackListLimit(int page) {
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.COL_BLACK_UPLOAD_STATE + " = ? and ");
        sb.append(CallFilterConstants.COL_BLACK_REMOVE_STATE + " = ?");
        String selection = sb.toString();
        String[] selectionArgs = new String[]{String.valueOf(CallFilterConstants.UPLOAD_NO), String.valueOf(CallFilterConstants.REMOVE_NO)};
        int pageSize = PAGE_SIZE;
        StringBuilder sbOr = new StringBuilder();
        sbOr.append(CallFilterConstants.COL_BLACK_ID);
        sbOr.append(" " + CallFilterConstants.DESC);
        sbOr.append(" limit  " + pageSize);
        String sortOrder = sbOr.toString();

        return BlacklistTab.getInstance().getBlackList(selection, selectionArgs, sortOrder);
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
                        filterType = black.markType;
                        if (!TextUtils.isEmpty(black.name)) {
                            name = black.name;
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

            Cursor cur = null;
            try {
                if (update) {
                    String table = CallFilterConstants.TAB_FILTER_GROUP;
                    String colum1 = CallFilterConstants.FIL_GR_PH_NUMB;
                    String colum2 = CallFilterConstants.FIL_GR_DATE;
                    String colum3 = CallFilterConstants.FIL_CALL_TYPE;
                    String colum4 = CallFilterConstants.FIL_NUMBER;
                    cur = CallFilterUtils.getCursor(table, new String[]{colum1, colum2, colum3, colum4}, number);
                    boolean isKeyExist = (cur != null) ? cur.getCount() > 0 : false;
                    if (isKeyExist) {
                        String numSelcts = null;
                        String selArgs = null;
                        String froNum = null;
                        if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                            froNum = PrivacyContactUtils.formatePhoneNumber(number);
                            numSelcts = " LIKE ? ";
                            selArgs = "%" + froNum;
                        } else {
                            numSelcts = " = ? ";
                            selArgs = number;
                        }
                        while (cur.moveToNext()) {
                            int dateColum = cur.getColumnIndex(CallFilterConstants.FIL_GR_DATE);
                            int callTypeColum = cur.getColumnIndex(CallFilterConstants.FIL_CALL_TYPE);
                            int filterNumberColum = cur.getColumnIndex(CallFilterConstants.FIL_NUMBER);
                            long grDate = cur.getLong(dateColum);
                            int grCallType = cur.getInt(callTypeColum);
                            int filterCountT = cur.getInt(filterNumberColum);
                            filterCountT = filterCountT + 1;
                            where = CallFilterConstants.FIL_GR_PH_NUMB + numSelcts;
                            selectionArgs = new String[]{selArgs};
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
                if (cur != null) {
                    cur.close();
                }
            }
        }

        return true;
    }

    @Override
    public boolean removeFilterGr(List<CallFilterInfo> infos) {
        LeoLog.d(TAG, "removeFilterGr, 1111 ");
        if (infos == null || infos.size() <= 0) {
            return false;
        }
        LeoLog.d(TAG, "removeFilterGr, 22222 size: " + infos.size());
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
                String numSelcts = null;
                String selArgs = null;
                if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                    number = PrivacyContactUtils.formatePhoneNumber(number);
                    numSelcts = " LIKE ? ";
                    selArgs = "%" + number;
                } else {
                    numSelcts = " = ? ";
                    selArgs = number;
                }
                selection = CallFilterConstants.FIL_GR_PH_NUMB + numSelcts;
                selectionArgs = new String[]{selArgs};
                List<CallFilterInfo> calls = new ArrayList<CallFilterInfo>();
                CallFilterInfo call = new CallFilterInfo();
                call.setNumber(number);
                calls.add(call);
                removeFilterDet(calls);
            }
            int rows = cr.delete(uri, selection, selectionArgs);
            LeoLog.d(TAG, "removeFilterGr, del rows: " + rows);

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
        String numSelcts = null;
        String selArgs = null;
        if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
            number = PrivacyContactUtils.formatePhoneNumber(number);
            numSelcts = " LIKE ? ";
            selArgs = "%" + number;
        } else {
            numSelcts = " = ? ";
            selArgs = number;
        }
        String where = CallFilterConstants.FIL_DET_PHONE_NUMBER + numSelcts;
        String[] selectionArgs = new String[]{selArgs};
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
                        filterType = black.markType;
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
                String table = CallFilterConstants.TAB_FILTER_DETAIL;
                String colum1 = CallFilterConstants.FIL_DET_PHONE_NUMBER;
                String colum2 = CallFilterConstants.FIL_DET_DATE;

                boolean isKeyExist = CallFilterUtils.isDbKeyExist(table, new String[]{colum1, colum2}, number);
                if (update && isKeyExist) {
                    String numSelcts = null;
                    String selArgs = null;
                    if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                        number = PrivacyContactUtils.formatePhoneNumber(number);
                        numSelcts = " LIKE ? ";
                        selArgs = "%" + number;
                    } else {
                        numSelcts = " = ? ";
                        selArgs = number;
                    }
                    where = CallFilterConstants.FIL_DET_PHONE_NUMBER + numSelcts;
                    String[] selectArgs = new String[]{String.valueOf(selArgs)};
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
                String numSelcts = null;
                String selArgs = null;
                if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                    number = PrivacyContactUtils.formatePhoneNumber(number);
                    numSelcts = " LIKE ? ";
                    selArgs = "%" + number;
                } else {
                    numSelcts = " = ? ";
                    selArgs = number;
                }
                selection = CallFilterConstants.FIL_DET_PHONE_NUMBER + numSelcts;
                selectionArgs = new String[]{selArgs};
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

                String table = CallFilterConstants.TAB_STRANGER_TP;
                String colum = CallFilterConstants.STR_TP_NUM;

                boolean isKeyExist = CallFilterUtils.isDbKeyExist(table, new String[]{colum}, number);
                if (isKeyExist) {
                    String numSelcts = null;
                    String selArgs = null;
                    if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                        number = PrivacyContactUtils.formatePhoneNumber(number);
                        numSelcts = " LIKE ? ";
                        selArgs = "%" + number;
                    } else {
                        numSelcts = " = ? ";
                        selArgs = number;
                    }
//                    String numFormate = PrivacyContactUtils.formatePhoneNumber(number);
                    where = CallFilterConstants.STR_TP_NUM + numSelcts;
                    selectionArgs = new String[]{selArgs};
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
                String numSelcts = null;
                String selArgs = null;
                if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                    number = PrivacyContactUtils.formatePhoneNumber(number);
                    numSelcts = " LIKE ? ";
                    selArgs = "%" + number;
                } else {
                    numSelcts = " = ? ";
                    selArgs = number;
                }
                selection = CallFilterConstants.STR_TP_NUM + numSelcts;
//                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                selectionArgs = new String[]{selArgs};
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
    public BlackListInfo getSerBlackForNum(String number) {
        if (TextUtils.isEmpty(number)) {
            LeoLog.d(TAG, "getSerBlackForNum, number is null.");
            return null;
        }
        String simpleNumber = PrivacyContactUtils.formatePhoneNumber(number);
        List<String> serverList = BlacklistTab.getInstance().getServerNumberList();
        if (serverList == null || serverList.isEmpty()) {
            return null;
        }

        String savedNumber = null;
        for (String serverNumber : serverList) {
            if (TextUtils.isEmpty(serverNumber)) {
                continue;
            }
            if (!serverNumber.endsWith(simpleNumber)) {
                continue;
            }

            savedNumber = serverNumber;
            break;
        }
        if (savedNumber == null) {
            LeoLog.d(TAG, "getSerBlackForNum, savedNumber is null.");
            return null;
        }
        String encryptedNumber = null;
        try {
            encryptedNumber = CryptoUtils.encrypt(savedNumber);
        } catch (Throwable e) {
            LeoLog.e(TAG, "getSerBlackForNum, encrypt number ex." + e.toString());
            return null;
        }
        return BlacklistTab.getInstance().getServerBlackInfo(encryptedNumber);
    }

    @Override
    public int[] isCallFilterTip(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        boolean isUse = CallFilterUtils.isNumberUsePrivacy(number);
        if (isUse) {
            return null;
        }
        int[] type = new int[4];
        type[0] = -1;
        int addBlackCount = 0;
        int markCount = 0;
        int markType = -1;
        BlackListInfo blacks = getSerBlackForNum(number);
        if (blacks != null) {
            addBlackCount = blacks.blackNum;
            markCount = blacks.markNum;
            markType = blacks.markType;
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
    public BlackListInfo getSerBlackListFroNum(String number) {
        return BlacklistTab.getInstance().getServerBlackInfo(number);
    }

    @Override
    public int getSerBlackNumFroNum(String number) {
        BlackListInfo info = getSerBlackListFroNum(number);
        int addBlackNum = 0;
        if (info != null) {
            addBlackNum = info.blackNum;
        }
        int params = getBlackMarkTipParam();
        return addBlackNum * params;
    }

    @Override
    public int getSerMarkerNumFroNum(String number) {
        BlackListInfo info = getSerBlackListFroNum(number);
        int addMarkerNum = 0;
        if (info != null) {
            addMarkerNum = info.markNum;
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
    public BlackListInfo getBlackFroNum(String number) {
        return BlacklistTab.getInstance().getBlackInfoByNumber(number);
    }

    @Override
    public Bitmap getBlackIcon(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        BlackListInfo info = getBlackFroNum(number);
        if (info != null) {
            return info.icon;
        }
        return null;
    }

    @Override
    public List<BlackListInfo> getUpBlackListLimit(int page) {
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.COL_BLACK_UPLOAD_STATE + " = ? and ");
        sb.append(CallFilterConstants.COL_BLACK_REMOVE_STATE + " = ?");
        String selection = sb.toString();
        String[] selectionArgs = new String[]{String.valueOf(CallFilterConstants.UPLOAD), String.valueOf(CallFilterConstants.REMOVE_NO)};
        int pageSize = PAGE_SIZE;
        StringBuilder sbOr = new StringBuilder();
        sbOr.append(CallFilterConstants.COL_BLACK_ID);
        sbOr.append(" " + CallFilterConstants.DESC);
        sbOr.append(" limit  " + pageSize);
        String sortOrder = sbOr.toString();
        return BlacklistTab.getInstance().getBlackList(selection, selectionArgs, sortOrder);
    }

//    @Override
//    public List<BlackListInfo> getUploadBlackList() {
//        Uri uri = CallFilterConstants.BLACK_LIST_URI;
//        StringBuilder sb = new StringBuilder();
//        sb.append(CallFilterConstants.COL_BLACK_UPLOAD_STATE + " = ? and ");
//        sb.append(CallFilterConstants.COL_BLACK_REMOVE_STATE + " = ?");
//        String selection = sb.toString();
//        String[] selectionArgs = new String[]{String.valueOf(CallFilterConstants.UPLOAD), String.valueOf(CallFilterConstants.REMOVE_NO)};
//
//        return BlacklistTab.getInstance().getBlackList(selection, selectionArgs, null);
//    }

    @Override
    public void markBlackInfo(BlackListInfo info, int markType) {
        if (info == null || markType == CallFilterConstants.MK_BLACK_LIST) {
            return;
        }

        BlacklistTab.getInstance().updateMarkType(info, markType);
    }

    @Override
    public void interceptCall(BlackListInfo info) {
        if (info == null) {
            return;
        }

        info.filtUpState = CallFilterConstants.FIL_UP;
        BlacklistTab.getInstance().updateIntercept(info);
    }
}
