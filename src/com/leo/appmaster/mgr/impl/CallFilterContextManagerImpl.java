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
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;

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

                    BlackListInfo info = CallFilterUtils.getBlackListInfo(id, number, name, markerType, icon,
                            numberArea, addBlackNum, markerNum, uploadState, removeState, readState);
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
    public boolean addBlackList(BlackListInfo info, boolean update) {
        if (info == null || info.getNumber() == null) {
            return false;
        }

        String name = info.getNumberName();
        String number = info.getNumber();
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
                    int curCount = cr.update(CallFilterConstants.BLACK_LIST_URI, value, null, null);
                    if (curCount > 0) {
                        return true;
                    }
                }
            } else {
                Uri uriCr = cr.insert(uri, value);
                if (uriCr != null) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeBlackList(BlackListInfo info) {
        return false;
    }

    @Override
    public List<BlackListInfo> getNoUploadBlackList() {
        return null;
    }

    @Override
    public List<CallFilterInfo> getCallFilterGrList() {
        return null;
    }

    @Override
    public int getCallFilterGrCount() {
        return 0;
    }

    @Override
    public boolean addFilterGr(CallFilterInfo info) {
        return false;
    }

    @Override
    public boolean removeFilterGr(CallFilterInfo info) {
        return false;
    }

    @Override
    public List<CallFilterInfo> getFilterDetList() {
        return null;
    }

    @Override
    public int getFilterDetCount() {
        return 0;
    }

    @Override
    public boolean addFilterDet(CallFilterInfo info) {
        return false;
    }

    @Override
    public boolean removeFilterDet(CallFilterInfo info) {
        return false;
    }

    @Override
    public List<StrangerInfo> getStrangerGrList() {
        return null;
    }

    @Override
    public int getStranagerGrCount() {
        return 0;
    }

    @Override
    public boolean addStrangerGr(StrangerInfo info) {
        return false;
    }

    @Override
    public boolean removeStrangerGr(StrangerInfo info) {
        return false;
    }

    @Override
    public List<StrangerInfo> getStrangerDetList() {
        return null;
    }

    @Override
    public int getStrangerDetCount() {
        return 0;
    }

    @Override
    public boolean addStrangerDet(StrangerInfo info) {
        return false;
    }

    @Override
    public boolean removeStrangerDet(StrangerInfo info) {
        return false;
    }

    @Override
    public boolean getFilterOpenState() {
        return false;
    }

    @Override
    public void setFilterOpenState(boolean flag) {

    }

    @Override
    public int[] isCallFilterTip(String number) {
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
}
