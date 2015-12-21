package com.leo.appmaster.mgr.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.leo.appmaster.R;
import com.leo.appmaster.callfilter.BlackListInfo;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.callfilter.CallFilterInfo;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.callfilter.StrangerInfo;
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
    @Override
    public List<BlackListInfo> getBlackList() {
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = null;
        List<BlackListInfo> blackListInfoList = null;
        try {
            Uri uri = CallFilterConstants.BLACK_LIST_URI;
            String sortOrder = CallFilterConstants.BLACK_ID + " " + CallFilterConstants.DESC;
            cursor = cr.query(uri, null, null, null, sortOrder);
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
                    int removeStateColum = cursor.getColumnIndex(CallFilterConstants.BLACK_REMOVE_STATE);
                    int readStateColum = cursor.getColumnIndex(CallFilterConstants.BLACK_READ_STATE);

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
                        int size = (int) mContext.getResources().getDimension(R.dimen.contact_icon_scale_size);
                        icon = PrivacyContactUtils.getScaledContactIcon(icon, size);
                    }
                    String numberArea = cursor.getString(areaColum);
                    int addBlackNum = cursor.getInt(addNumColum);
                    int markerType = cursor.getInt(markerTypeColum);
                    int markerNum = cursor.getInt(markerNumColum);
                    int uploadStateType = cursor.getInt(uploadStateColum);
                    int removeStateType = cursor.getInt(removeStateColum);
                    int readStateType = cursor.getInt(readStateColum);
                    boolean uploadState = false;
                    boolean removeState = false;
                    boolean readState = false;
                    switch (uploadStateType) {
                        case CallFilterConstants.UPLOAD:
                            uploadState = true;
                            break;
                        case CallFilterConstants.UPLOAD_NO:
                            uploadState = false;
                            break;
                        default:
                            break;
                    }
                    switch (removeStateType) {
                        case CallFilterConstants.REMOVE:
                            removeState = true;
                            break;
                        case CallFilterConstants.REMOVE_NO:
                            removeState = false;
                            break;
                        default:
                            break;
                    }
                    switch (readStateType) {
                        case CallFilterConstants.READ:
                            readState = true;
                            break;
                        case CallFilterConstants.READ_NO:
                            readState = false;
                            break;
                        default:
                            break;
                    }
                    int existState = -1;
                    BlackListInfo info = CallFilterUtils.getBlackListInfo(id, number, name, markerType, icon,
                            numberArea, addBlackNum, markerNum, uploadState, removeState, readState,existState);
                    blackListInfoList.add(info);
                }
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    @Override
    public int getBlackListCount() {
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = null;
        int count = 0;
        try {
            Uri uri = CallFilterConstants.BLACK_LIST_URI;
            String sortOrder = CallFilterConstants.BLACK_ID + " " + CallFilterConstants.DESC;
            cursor = cr.query(uri, null, null, null, sortOrder);
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
            String name = info.getNumberName();
            String number = PrivacyContactUtils.simpleFromateNumber(info.getNumber());
            boolean isContactUse = CallFilterUtils.isNumberUsePrivacy(number);
            if (isContactUse) {
                continue;
            }
            Bitmap icon = info.getIcon();
            String area = info.getNumberArea();
            int addBlackNumber = info.getAddBlackNumber();
            int markerType = info.getMarkerType();
            int markerNum = info.getMarkerNumber();
            boolean uploadState = info.isUploadState();
            boolean removeState = info.isRemoveState();
            boolean readState = info.isReadState();

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
            value.put(CallFilterConstants.BLACK_ADD_NUMBER, addBlackNumber);
            value.put(CallFilterConstants.MARKER_TYPE, markerType);
            value.put(CallFilterConstants.MARKER_NUMBER, markerNum);
            if (uploadState) {
                value.put(CallFilterConstants.BLACK_UPLOAD_STATE, CallFilterConstants.UPLOAD);
            } else {
                value.put(CallFilterConstants.BLACK_UPLOAD_STATE, CallFilterConstants.UPLOAD_NO);
            }

            if (removeState) {
                value.put(CallFilterConstants.BLACK_REMOVE_STATE, CallFilterConstants.REMOVE);
            } else {
                value.put(CallFilterConstants.BLACK_REMOVE_STATE, CallFilterConstants.REMOVE_NO);
            }


            if (readState) {
                value.put(CallFilterConstants.BLACK_READ_STATE, CallFilterConstants.READ);
            } else {
                value.put(CallFilterConstants.BLACK_READ_STATE, CallFilterConstants.READ_NO);
            }

            try {
                if (update) {
                    String table = CallFilterConstants.BLACK_LIST_TAB;
                    String colum = CallFilterConstants.BLACK_PHONE_NUMBER;
                    boolean isKeyExist = CallFilterUtils.isDbKeyExist(table, colum, number);
                    if (isKeyExist) {
                        cr.update(CallFilterConstants.BLACK_LIST_URI, value, null, null);
                    }
                } else {
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
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        String selection = null;
        String[] selectionArgs = null;
        for (BlackListInfo info : blackList) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
            int id = info.getId();
            String number = info.getNumber();
            if (id >= 0) {
                selection = CallFilterConstants.BLACK_ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(id)};
            } else if (!TextUtils.isEmpty(number)) {
                selection = CallFilterConstants.BLACK_PHONE_NUMBER + " LIKE ? ";
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                selectionArgs = new String[]{formateNumber};
            }
            cr.delete(uri, selection, selectionArgs);
        }

        return true;
    }

    @Override
    public List<BlackListInfo> getNoUploadBlackList() {
        ContentResolver cr = mContext.getContentResolver();
        List<BlackListInfo> blackListInfoList = null;
        Cursor cursor = null;
        try {
            Uri uri = CallFilterConstants.BLACK_LIST_URI;
            String selection = CallFilterConstants.BLACK_UPLOAD_STATE + " = ? ";
            String[] selectionArgs = new String[]{String.valueOf(CallFilterConstants.UPLOAD_NO)};
            cursor = cr.query(uri, null, selection, selectionArgs, null);
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
                    int removeStateColum = cursor.getColumnIndex(CallFilterConstants.BLACK_REMOVE_STATE);
                    int readStateColum = cursor.getColumnIndex(CallFilterConstants.BLACK_READ_STATE);

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
                        int size = (int) mContext.getResources().getDimension(R.dimen.contact_icon_scale_size);
                        icon = PrivacyContactUtils.getScaledContactIcon(icon, size);
                    }
                    String numberArea = cursor.getString(areaColum);
                    int addBlackNum = cursor.getInt(addNumColum);
                    int markerType = cursor.getInt(markerTypeColum);
                    int markerNum = cursor.getInt(markerNumColum);
                    int uploadStateType = cursor.getInt(uploadStateColum);
                    int removeStateType = cursor.getInt(removeStateColum);
                    int readStateType = cursor.getInt(readStateColum);
                    boolean uploadState = false;
                    boolean removeState = false;
                    boolean readState = false;
                    switch (uploadStateType) {
                        case CallFilterConstants.UPLOAD:
                            uploadState = true;
                            break;
                        case CallFilterConstants.UPLOAD_NO:
                            uploadState = false;
                            break;
                        default:
                            break;
                    }
                    switch (removeStateType) {
                        case CallFilterConstants.REMOVE:
                            removeState = true;
                            break;
                        case CallFilterConstants.REMOVE_NO:
                            removeState = false;
                            break;
                        default:
                            break;
                    }
                    switch (readStateType) {
                        case CallFilterConstants.READ:
                            readState = true;
                            break;
                        case CallFilterConstants.READ_NO:
                            readState = false;
                            break;
                        default:
                            break;
                    }
                    int existState = -1;
                    BlackListInfo info = CallFilterUtils.getBlackListInfo(id, number, name, markerType, icon,
                            numberArea, addBlackNum, markerNum, uploadState, removeState, readState,existState);
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
                    int filterTypeColum = cursor.getColumnIndex(CallFilterConstants.FIL_GR_TYPE);

                    int id = cursor.getInt(idColum);
                    String name = cursor.getString(nameColum);
                    String number = cursor.getString(numberColum);
                    if (TextUtils.isEmpty(name)) {
                        name = number;
                    }
                    String numberArea = cursor.getString(numberAreaColum);
                    int blackId = cursor.getInt(blackIdColum);
                    int filterNumber = cursor.getInt(filterNumberColum);
                    long date = cursor.getLong(dateColum);
                    long duration = cursor.getLong(durationColum);
                    int callType = cursor.getInt(callTypeColum);
                    int readState = cursor.getInt(readStateColum);
                    boolean isRead = false;
                    switch (readState) {
                        case CallFilterConstants.READ:
                            isRead = true;
                            break;
                        case CallFilterConstants.READ_NO:
                            isRead = false;
                            break;
                        default:
                            break;

                    }
                    int filterType = cursor.getInt(filterTypeColum);
                    /*默认值-1*/
                    int filterGrId = -1;
                    CallFilterInfo filterInfo = CallFilterUtils.getFilterInfo(id, name, number, numberArea, blackId,
                            filterNumber, date, duration, callType, isRead, filterType, filterGrId);
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
            boolean isRead = info.isReadState();

            values.put(CallFilterConstants.FIL_GR_NAME, name);
            values.put(CallFilterConstants.FIL_GR_PH_NUMB, number);
            values.put(CallFilterConstants.FIL_GR_TO_BLACK_ID, blackId);
            values.put(CallFilterConstants.FIL_NUMBER, filterCount);
            values.put(CallFilterConstants.FIL_GR_DATE, date);
            values.put(CallFilterConstants.FIL_CALL_DURATION, duration);
            values.put(CallFilterConstants.FIL_CALL_TYPE, callType);
            values.put(CallFilterConstants.FIL_GR_TYPE, filterType);
            values.put(CallFilterConstants.FIL_GR_NUM_AREA, numberType);
            if (isRead) {
                values.put(CallFilterConstants.FIL_READ_STATE, CallFilterConstants.READ);
            } else {
                values.put(CallFilterConstants.FIL_READ_STATE, CallFilterConstants.READ_NO);
            }

            try {
                if (update) {
                    String table = CallFilterConstants.FILTER_GROUP_TAB;
                    String colum = CallFilterConstants.FIL_GR_PH_NUMB;
                    boolean isKeyExist = CallFilterUtils.isDbKeyExist(table, colum, number);
                    if (isKeyExist) {
                        cr.update(uri, values, where, selectionArgs);
                    }
                } else {
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
            int id = info.getId();
            String number = info.getNumber();

            if (id >= 0) {
                selection = CallFilterConstants.FIL_GR_ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(id)};
            } else if (!TextUtils.isEmpty(number)) {
                selection = CallFilterConstants.FIL_GR_PH_NUMB + " LIKE ? ";
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                selectionArgs = new String[]{formateNumber};
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
                    boolean isRead = false;
                    switch (readState) {
                        case CallFilterConstants.READ:
                            isRead = true;
                            break;
                        case CallFilterConstants.READ_NO:
                            isRead = false;
                            break;
                        default:
                            break;

                    }
                    int filterType = cursor.getInt(filterTypeColum);

                    int filterNumber = -1;
                    int blackId = -1;
                    CallFilterInfo filterInfo = CallFilterUtils.getFilterInfo(id, name, number, numberArea, blackId,
                            filterNumber, date, duration, callType, isRead, filterType, filterGrId);
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
            boolean isRead = info.isReadState();

            values.put(CallFilterConstants.FIL_DET_PHONE_NUMBER, number);
            values.put(CallFilterConstants.FIL_DET_NAME, name);
            values.put(CallFilterConstants.FIL_DET_NUM_AREA, numberType);
            values.put(CallFilterConstants.FIL_DET_TO_GR_ID, filterGrId);
            values.put(CallFilterConstants.FIL_DET_DATE, date);
            values.put(CallFilterConstants.FIL_DET_DURATION, duration);
            values.put(CallFilterConstants.FIL_DET_CALL_TYPE, callType);
            values.put(CallFilterConstants.FIL_DET_TYPE, filterType);
            if (isRead) {
                values.put(CallFilterConstants.FIL_DET_READ_STATE, CallFilterConstants.READ);
            } else {
                values.put(CallFilterConstants.FIL_DET_READ_STATE, CallFilterConstants.READ_NO);
            }

            try {
                if (update) {
                    String table = CallFilterConstants.FILTER_DETAIL_TAB;
                    String colum = CallFilterConstants.FIL_DET_PHONE_NUMBER;
                    boolean isKeyExist = CallFilterUtils.isDbKeyExist(table, colum, number);
                    if (isKeyExist) {
                        cr.update(uri, values, where, selectionArgs);
                    }
                } else {
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

            if (id >= 0) {
                selection = CallFilterConstants.FIL_DET_ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(id)};
            } else if (!TextUtils.isEmpty(number)) {
                selection = CallFilterConstants.FIL_DET_PHONE_NUMBER + " LIKE ? ";
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                selectionArgs = new String[]{formateNumber};
            }
            cr.delete(uri, selection, selectionArgs);
        }

        return true;
    }

    @Override
    public List<StrangerInfo> getStrangerGrList() {
        List<StrangerInfo> starInfos = null;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_GROUP_URI;
        Cursor cursor = null;
        String where = null;
        String[] selectionArgs = null;
        String sortOrder = CallFilterConstants.STR_GR_CALL_DATE + " " + CallFilterConstants.DESC;
        try {
            cursor = cr.query(uri, null, where, selectionArgs, sortOrder);

            if (cursor != null) {
                starInfos = new ArrayList<StrangerInfo>();
                while (cursor.moveToNext()) {

                    int idColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_ID);
                    int nmberColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_PHO_NUM);
                    int numberAreaColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_NUM_AREA);
                    int durationColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_CALL_DURATION);
                    int dateColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_CALL_DATE);
                    int callTypeColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_CALL_TYPE);
                    int callCountColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_NUMBER);
                    int tipColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_TIP_STATE);
                    int removeColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_REMOVE_STATE);
                    int readColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_READ_STATE);

                    int id = cursor.getInt(idColum);
                    String number = cursor.getString(nmberColum);
                    String area = cursor.getString(numberAreaColum);
                    long duration = cursor.getLong(durationColum);
                    long date = cursor.getLong(dateColum);
                    int callType = cursor.getInt(callTypeColum);
                    int callCount = cursor.getInt(callCountColum);
                    int tipInt = cursor.getInt(tipColum);
                    int removeInt = cursor.getInt(removeColum);
                    int readInt = cursor.getInt(readColum);
                    boolean tipState = false;
                    boolean removeState = false;
                    boolean readState = false;
                    switch (tipInt) {
                        case CallFilterConstants.FILTER_TIP:
                            tipState = true;
                            break;
                        case CallFilterConstants.FILTER_TIP_NO:
                            tipState = false;
                            break;
                        default:
                            break;
                    }
                    switch (removeInt) {
                        case CallFilterConstants.REMOVE:
                            removeState = true;
                            break;
                        case CallFilterConstants.REMOVE_NO:
                            removeState = false;
                            break;
                        default:
                            break;
                    }
                    switch (readInt) {
                        case CallFilterConstants.READ:
                            readState = true;
                            break;
                        case CallFilterConstants.READ_NO:
                            readState = false;
                            break;
                        default:
                            break;
                    }
                    int starGrId = -1;
                    StrangerInfo straInfo = CallFilterUtils.getStrangInfo(id, number, area, duration, date,
                            callType, callCount, tipState, removeState, readState, starGrId);
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
        Uri uri = CallFilterConstants.STRANGER_GROUP_URI;
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
    public boolean addStrangerGr(List<StrangerInfo> infos, boolean update) {
        if (infos == null || infos.size() <= 0) {
            return false;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_GROUP_URI;
        ContentValues values = new ContentValues();
        String where = null;
        String[] selectionArgs = null;

        try {
            for (StrangerInfo info : infos) {
                if (TextUtils.isEmpty(info.getNumber())) {
                    continue;
                }

                String number = PrivacyContactUtils.simpleFromateNumber(info.getNumber());
                long duration = info.getCallDuration();
                int callType = info.getCallType();
                long date = info.getDate();
                String numberArea = info.getNumberArea();
                int starCount = info.getStarCount();
                boolean removeState = info.isRemoveState();
                boolean readState = info.isReadState();
                boolean tipState = info.isTipState();

                values.put(CallFilterConstants.STR_GR_PHO_NUM, number);
                values.put(CallFilterConstants.STR_GR_NUM_AREA, numberArea);
                values.put(CallFilterConstants.STR_GR_CALL_DURATION, duration);
                values.put(CallFilterConstants.STR_GR_CALL_TYPE, callType);
                values.put(CallFilterConstants.STR_GR_CALL_DATE, date);
                values.put(CallFilterConstants.STR_GR_NUMBER, starCount);
                if (removeState) {
                    values.put(CallFilterConstants.STR_GR_REMOVE_STATE, CallFilterConstants.REMOVE);
                } else {
                    values.put(CallFilterConstants.STR_GR_REMOVE_STATE, CallFilterConstants.REMOVE_NO);
                }

                if (readState) {
                    values.put(CallFilterConstants.STR_GR_READ_STATE, CallFilterConstants.READ);
                } else {
                    values.put(CallFilterConstants.STR_GR_READ_STATE, CallFilterConstants.READ_NO);
                }

                if (tipState) {
                    values.put(CallFilterConstants.STR_GR_TIP_STATE, CallFilterConstants.FILTER_TIP);
                } else {
                    values.put(CallFilterConstants.STR_GR_TIP_STATE, CallFilterConstants.FILTER_TIP_NO);
                }

                if (update) {
                    String table = CallFilterConstants.STRANGER_GROUP_TAB;
                    String colum = CallFilterConstants.STR_GR_PHO_NUM;
                    boolean isKeyExist = CallFilterUtils.isDbKeyExist(table, colum, number);
                    if (isKeyExist) {
                        cr.update(uri, values, where, selectionArgs);
                    }
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
        Uri uri = CallFilterConstants.STRANGER_GROUP_URI;
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
                selection = CallFilterConstants.STR_GR_PHO_NUM + " LIKE ? ";
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                selectionArgs = new String[]{formateNumber};
            }
            cr.delete(uri, selection, selectionArgs);
        }

        return true;
    }

    @Override
    public List<StrangerInfo> getStrangerDetList() {

        List<StrangerInfo> starInfos = null;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_DETAIL_URI;
        Cursor cursor = null;
        String where = null;
        String[] selectionArgs = null;
//        String sortOrder = CallFilterConstants.STR_DET_CALL_DATE + " " + CallFilterConstants.DESC;
        String sortOrder = null;
        try {
            cursor = cr.query(uri, null, where, selectionArgs, sortOrder);

            if (cursor != null) {
                starInfos = new ArrayList<StrangerInfo>();
                while (cursor.moveToNext()) {
                    int idColum = cursor.getColumnIndex(CallFilterConstants.STR_DET_ID);
                    int nmberColum = cursor.getColumnIndex(CallFilterConstants.STR_DET_PHO_NUM);
                    int straGrIdColum = cursor.getColumnIndex(CallFilterConstants.STR_DET_TO_GR_ID);
                    int numberAreaColum = cursor.getColumnIndex(CallFilterConstants.STR_DET_NUM_AREA);
                    int durationColum = cursor.getColumnIndex(CallFilterConstants.STR_DET_CALL_DURATION);
                    int dateColum = cursor.getColumnIndex(CallFilterConstants.STR_DET_CALL_DATE);
                    int callTypeColum = cursor.getColumnIndex(CallFilterConstants.STR_DET_CALL_TYPE);
                    int readColum = cursor.getColumnIndex(CallFilterConstants.STR_GR_READ_STATE);

                    int id = cursor.getInt(idColum);
                    String number = cursor.getString(nmberColum);
                    String area = cursor.getString(numberAreaColum);
                    long duration = cursor.getLong(durationColum);
                    long date = cursor.getLong(dateColum);
                    int callType = cursor.getInt(callTypeColum);
                    int readInt = cursor.getInt(readColum);
                    int straGrId = cursor.getInt(straGrIdColum);
                    boolean readState = false;

                    switch (readInt) {
                        case CallFilterConstants.READ:
                            readState = true;
                            break;
                        case CallFilterConstants.READ_NO:
                            readState = false;
                            break;
                        default:
                            break;
                    }

                    int callCount = -1;
                    boolean tipState = false;
                    boolean removeState = false;
                    StrangerInfo straInfo = CallFilterUtils.getStrangInfo(id, number, area, duration, date,
                            callType, callCount, tipState, removeState, readState, straGrId);
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
    public int getStrangerDetCount() {
        int count = 0;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_DETAIL_URI;
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
    public boolean addStrangerDet(List<StrangerInfo> infos, boolean update) {

        if (infos == null || infos.size() <= 0) {
            return false;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_DETAIL_URI;
        ContentValues values = new ContentValues();
        String where = null;
        String[] selectionArgs = null;
        try {
            for (StrangerInfo info : infos) {
                if (TextUtils.isEmpty(info.getNumber())) {
                    continue;
                }

                String number = PrivacyContactUtils.simpleFromateNumber(info.getNumber());
                int straGrId = info.getStrangeGrId();
                String numberArea = info.getNumberArea();
                long duration = info.getCallDuration();
                long date = info.getDate();
                int callType = info.getCallType();
                boolean readState = info.isReadState();

                values.put(CallFilterConstants.STR_GR_PHO_NUM, number);
                values.put(CallFilterConstants.STR_GR_NUM_AREA, numberArea);
                values.put(CallFilterConstants.STR_GR_CALL_DURATION, duration);
                values.put(CallFilterConstants.STR_GR_CALL_TYPE, callType);
                values.put(CallFilterConstants.STR_GR_CALL_DATE, date);
                values.put(CallFilterConstants.STR_DET_TO_GR_ID, straGrId);

                if (readState) {
                    values.put(CallFilterConstants.STR_GR_READ_STATE, CallFilterConstants.READ);
                } else {
                    values.put(CallFilterConstants.STR_GR_READ_STATE, CallFilterConstants.READ_NO);
                }

                if (update) {
                    String table = CallFilterConstants.STRANGER_DETAIL_TAB;
                    String colum = CallFilterConstants.STR_GR_PHO_NUM;
                    boolean isKeyExist = CallFilterUtils.isDbKeyExist(table, colum, number);
                    if (isKeyExist) {
                        cr.update(uri, values, where, selectionArgs);
                    }
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
    public boolean removeStrangerDet(List<StrangerInfo> infos) {
        if (infos == null || infos.size() <= 0) {
            return false;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CallFilterConstants.STRANGER_DETAIL_URI;
        String selection = null;
        String[] selectionArgs = null;

        for (StrangerInfo info : infos) {
            if (TextUtils.isEmpty(info.getNumber())) {
                continue;
            }
            int id = info.getId();
            String number = info.getNumber();

            if (id >= 0) {
                selection = CallFilterConstants.STR_DET_ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(id)};
            } else if (!TextUtils.isEmpty(number)) {
                selection = CallFilterConstants.STR_DET_PHO_NUM + " LIKE ? ";
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                selectionArgs = new String[]{formateNumber};
            }
            cr.delete(uri, selection, selectionArgs);
        }

        return false;
    }

    @Override
    public boolean getFilterOpenState() {
        PreferenceTable pre = PreferenceTable.getInstance();
        return pre.getBoolean(PrefConst.KEY_FIL_OP_STA, false);
    }

    @Override
    public void setFilterOpenState(boolean flag) {
        PreferenceTable pre = PreferenceTable.getInstance();
        boolean state = pre.getBoolean(PrefConst.KEY_FIL_OP_STA, false);
        if (state != flag) {
            pre.putBoolean(PrefConst.KEY_FIL_OP_STA, flag);
        }
    }

    @Override
    public int[] isCallFilterTip(String number) {
        int[] type = new int[2];
        boolean isUse = CallFilterUtils.isNumberUsePrivacy(number);
        if (isUse) {
            type[0] = 1;
        } else {
            type[0] = 0;
        }

        int filUser = getFilterUserNumber();
        int filUserPar = getFilterTipFroUser();
        if (filUserPar > 0) {
            if (filUser >= filUserPar) {
                //TODO
            }
        }


        return new int[0];
    }

    @Override
    public long getCallDurationMax() {
        return 0;
    }

    @Override
    public int getStraNotiTipParam() {
        return 0;
    }

    @Override
    public int getBlackMarkTipParam() {
        return 0;
    }

    @Override
    public int getFilterUserNumber() {
        return 0;
    }

    @Override
    public List<BlackListInfo> getServerBlackList() {
        return null;
    }

    @Override
    public int getFilterTipFroUser() {
        return 0;
    }

    @Override
    public boolean addSerBlackList(List<BlackListInfo> infos) {
        return false;
    }

    @Override
    public boolean removeSerBlackList(List<BlackListInfo> infos) {
        return false;
    }

    @Override
    public List<BlackListInfo> getSerBlackList() {
        return null;
    }
}
