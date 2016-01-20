package com.leo.appmaster.db;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.telecom.Call;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.callfilter.BlackListInfo;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.cloud.crypto.CryptoUtils;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.schedule.BlackListFileFetchJob;
import com.leo.appmaster.utils.DataUtils;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2016/1/14.
 */
public class BlacklistTab extends BaseTable {
    private static final String TAG = "BlacklistTab";
    private static BlacklistTab sInstance;
    private List<String> mPhoneList;

    public static synchronized BlacklistTab getInstance() {
        if (sInstance == null) {
            sInstance = new BlacklistTab();
        }

        return sInstance;
    }

    public BlacklistTab() {
        mPhoneList = new ArrayList<String>();
    }

    public void initEncryptList() {
        LeoLog.d(TAG, "initEncryptList start..");
        SQLiteDatabase database = getHelper().getReadableDatabase();
        if (database == null) {
            LeoLog.d(TAG, "initEncryptList database is null..");
            return;
        }
        Cursor cursor = null;
        List<String> list = new ArrayList<String>();
        try {
            cursor = database.query(CallFilterConstants.TAB_SERVER_BLACK_LIST,
                    new String[]{CallFilterConstants.COL_SERVER_NUMBER}, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    String encryptedNumber = cursor.getString(cursor.getColumnIndex(CallFilterConstants.COL_SERVER_NUMBER));
                    if (TextUtils.isEmpty(encryptedNumber)) {
                        LeoLog.d(TAG, "initEncryptList, encryptedNumber is null.");
                        continue;
                    }
                    try {
                        String decryptedNumber = CryptoUtils.decrypt(encryptedNumber);
                        list.add(decryptedNumber);
                    } catch (Exception e) {
                        LeoLog.e(TAG, "initEncryptList decrypte number ex, " + e.toString());
                        continue;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LeoLog.e(TAG, "initEncryptList ex. " + e.toString());
        } finally {
            IoUtils.closeSilently(cursor);
        }
        LeoLog.d(TAG, "initEncryptList end..");
        if (!list.isEmpty()) {
            mPhoneList.clear();
            mPhoneList.addAll(list);
        }
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL(CallFilterConstants.CREATE_BLACK_LIST_TAB);

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + CallFilterConstants.TAB_SERVER_BLACK_LIST
                + " ("
                + CallFilterConstants.COL_BLACK_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CallFilterConstants.COL_SERVER_NUMBER
                + " TEXT,"
                + CallFilterConstants.COL_SERVER_BLACK_NUM
                + " INTEGER,"
                + CallFilterConstants.COL_SERVER_MARK_TYPE
                + " INTEGER,"
                + CallFilterConstants.COL_SERVER_MARK_NUM
                + " INTEGER);");

        db.execSQL("CREATE INDEX IF NOT EXISTS phone_idx on server_black_list(phone_number);");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        LeoLog.d(TAG, "upgradeTable oldVer: " + oldVersion + " | newVer: " + newVersion);
        if (oldVersion <= 9 && newVersion == 10) {
            List<BlackListInfo> oldList = null;
            if (oldVersion == 9) {
                // 数据迁移
                oldList = getOldBlackList(db);
                // 3.2版本需要先把拉取任务时间及状态重置一下
                BlackListFileFetchJob.resetTimesAndCounts();
            }
            db.execSQL("DROP TABLE IF EXISTS " + CallFilterConstants.TAB_SERVER_BLACK_LIST);
            db.execSQL("DROP TABLE IF EXISTS " + CallFilterConstants.TAB_BLACK_LIST);

            db.execSQL("DROP INDEX IF EXISTS phone_idx");
            createTable(db);

            LeoLog.d(TAG, "upgradeTable, oldList size is: " + (oldList == null ? 0 : oldList.size()));
            if (oldList != null && oldList.size() > 0) {
                addBlackListInner(oldList, db);
            }
        }
    }

    private List<BlackListInfo> getOldBlackList(SQLiteDatabase database) {
        List<BlackListInfo> result = new ArrayList<BlackListInfo>();
        String sortOrder = CallFilterConstants.COL_TIME + " " + CallFilterConstants.DESC;
        StringBuilder sb = new StringBuilder();
        sb.append("loc_hd = ? and ");
        sb.append(CallFilterConstants.COL_BLACK_REMOVE_STATE + " = ? ");
        String selects = sb.toString();
        String[] selectArgs = new String[]{ "1", String.valueOf(CallFilterConstants.REMOVE_NO)};
        Cursor cursor = null;
        try {
            cursor = database.query(CallFilterConstants.TAB_BLACK_LIST, null, selects, selectArgs, null, null, sortOrder);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    BlackListInfo info = readFromOldCursor(cursor);
                    result.add(info);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LeoLog.e(TAG, "getOldBlackList ex." + e.toString());
        } finally {
            IoUtils.closeSilently(cursor);
        }

        return result;
    }

    public void updateIntercept(List<BlackListInfo> listInfos) {
        if (listInfos == null || listInfos.isEmpty()) {
            LeoLog.d(TAG, "updateIntercept, listInfos if empty.");
            return;
        }

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) {
            LeoLog.d(TAG, "updateIntercept, db is null.");
            return;
        }

        db.beginTransaction();
        try {
            for (BlackListInfo listInfo : listInfos) {
                updateInterceptInner(listInfo, db);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            LeoLog.e(TAG, "updateUploadState list ex. " + e.toString());
        } finally {
            db.endTransaction();
        }
    }

    public void updateIntercept(BlackListInfo info) {
        if (info == null || (info.id == -1 && TextUtils.isEmpty(info.number))) {
            LeoLog.d(TAG, "updateIntercept, info id is -1.");
            return;
        }
        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) {
            LeoLog.d(TAG, "updateIntercept, db is null.");
            return;
        }

        updateInterceptInner(info, db);
    }

    private void updateInterceptInner(BlackListInfo info, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        if (info.filtUpState == -1) {
            LeoLog.d(TAG, "updateIntercept, fillup is -1.");
            return;
        }
        values.put(CallFilterConstants.COL_BLACK_FIL_UP, info.filtUpState);

        if (info.id != -1) {
            db.update(CallFilterConstants.TAB_BLACK_LIST, values, "_id = ?", new String[]{info.id + ""});
        } else {
            String number = info.number;
            String selection = CallFilterConstants.COL_BLACK_NUMBER;
            String selectionArgs = null;
            if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                number = PrivacyContactUtils.formatePhoneNumber(number);
                selection += " LIKE ? ";
                selectionArgs = "%" + number;
            } else {
                selection += " = ? ";
                selectionArgs = number;
            }
            try {
                db.update(CallFilterConstants.TAB_BLACK_LIST, values, selection, new String[]{selectionArgs});
            } catch (Exception e) {
                LeoLog.e(TAG, "updateMarkType ex." + e.toString());
            }
        }
    }

    public void updateMarkType(BlackListInfo info, int markType) {
        if (info.id == -1 && TextUtils.isEmpty(info.number)) {
            LeoLog.d(TAG, "updateMarkType, info id is -1.");
            return;
        }

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) {
            LeoLog.d(TAG, "updateMarkType, db is null.");
            return;
        }
        ContentValues values = new ContentValues();
        values.put(CallFilterConstants.COL_BLACK_MARK_TYPE, markType);

        if (info.id != -1) {
            try {
                db.update(CallFilterConstants.TAB_BLACK_LIST, values, "_id = ?", new String[]{info.id + ""});
            } catch (Exception e) {
                LeoLog.e(TAG, "updateMarkType ex. id: " + info.id + " | " + e.toString());
            }
        } else {
            String number = info.number;
            String selection = CallFilterConstants.COL_BLACK_NUMBER;
            String selectionArgs = null;
            if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                number = PrivacyContactUtils.formatePhoneNumber(number);
                selection += " LIKE ? ";
                selectionArgs = "%" + number;
            } else {
                selection += " = ? ";
                selectionArgs = number;
            }
            try {
                db.update(CallFilterConstants.TAB_BLACK_LIST, values, selection, new String[]{selectionArgs});
            } catch (Exception e) {
                LeoLog.e(TAG, "updateMarkType ex." + e.toString());
            }
        }
    }

    public void updateUploadState(List<BlackListInfo> listInfos) {
        if (listInfos == null || listInfos.isEmpty()) {
            LeoLog.d(TAG, "updateUploadState, listInfos if empty.");
            return;
        }

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) {
            LeoLog.d(TAG, "updateUploadState, db is null.");
            return;
        }

        db.beginTransaction();
        try {
            for (BlackListInfo listInfo : listInfos) {
                updateUploadStateInner(listInfo, db);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            LeoLog.e(TAG, "updateUploadState list ex. " + e.toString());
        } finally {
            db.endTransaction();
        }
    }

    private void updateUploadStateInner(BlackListInfo info, SQLiteDatabase db) {
        if (info.uploadState == -1) {
            LeoLog.d(TAG, "updateUploadState, uploadState is -1.");
            return;
        }
        ContentValues values = new ContentValues();
        values.put(CallFilterConstants.COL_BLACK_UPLOAD_STATE, info.uploadState);

        if (info.id != -1) {
            try {
                db.update(CallFilterConstants.TAB_BLACK_LIST, values, "_id = ?", new String[]{info.id + ""});
            } catch (Exception e) {
                LeoLog.e(TAG, "updateUploadState ex. id: " + info.id + " | " + e.toString());
            }
        } else {
            String number = info.number;
            String selection = CallFilterConstants.COL_BLACK_NUMBER;
            String selectionArgs = null;
            if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                number = PrivacyContactUtils.formatePhoneNumber(number);
                selection += " LIKE ? ";
                selectionArgs = "%" + number;
            } else {
                selection += " = ? ";
                selectionArgs = number;
            }
            try {
                db.update(CallFilterConstants.TAB_BLACK_LIST, values, selection, new String[]{selectionArgs});
            } catch (Exception e) {
                LeoLog.e(TAG, "updateMarkType ex." + e.toString());
            }
        }
    }

    public void updateUploadState(BlackListInfo info) {
        if (info.id == -1 && TextUtils.isEmpty(info.number)) {
            LeoLog.d(TAG, "updateUploadState, info id is -1.");
            return;
        }

        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) {
            LeoLog.d(TAG, "updateUploadState, db is null.");
            return;
        }

        updateUploadStateInner(info, db);

    }

    public void deleteBlackList(List<BlackListInfo> listInfos) {
        if (listInfos == null || listInfos.isEmpty()) {
            LeoLog.d(TAG, "deleteBlackList, listinfos is empty.");
            return;
        }
        SQLiteDatabase database = getHelper().getWritableDatabase();
        if (database == null) {
            LeoLog.d(TAG, "deleteBlackList, database is null.");
            return;
        }
        ContentValues values = new ContentValues();
        database.beginTransaction();
        try {
            for (BlackListInfo info : listInfos) {
                if (info == null || (TextUtils.isEmpty(info.number) && info.id == -1)) {
                    continue;
                }

                values.clear();
                values.put(CallFilterConstants.COL_BLACK_REMOVE_STATE, CallFilterConstants.REMOVE);
                values.put(CallFilterConstants.COL_BLACK_MARK_TYPE, CallFilterConstants.MK_BLACK_LIST);
                if (info.id != -1) {
                    database.update(CallFilterConstants.TAB_BLACK_LIST, values, "_id = ?", new String[]{info.id + ""});
                } else {
                    String number = PrivacyContactUtils.simpleFromateNumber(info.number);
                    String selection = CallFilterConstants.COL_BLACK_NUMBER;
                    String[] selectionArgs = null;
                    if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                        selection += " LIKE ? ";
                        selectionArgs = new String[]{"%" + number};
                    } else {
                        selection += " = ? ";
                        selectionArgs = new String[]{number};
                    }
                    database.update(CallFilterConstants.TAB_BLACK_LIST, values, selection, selectionArgs);
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            LeoLog.e(TAG, "deleteBlackList ex." + e.toString());
        } finally {
            database.endTransaction();
        }
    }

    private void addBlackListInner(List<BlackListInfo> listInfos, SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        String selection = null;
        String[] selectionArgs = null;

        database.beginTransaction();
        try {
            for (BlackListInfo info : listInfos) {
                if (info == null || TextUtils.isEmpty(info.number)) {
                    continue;
                }
                String number = PrivacyContactUtils.simpleFromateNumber(info.number);

                values.clear();
                values.put(CallFilterConstants.COL_BLACK_NUMBER, number);
                if (!TextUtils.isEmpty(info.name)) {
                    values.put(CallFilterConstants.COL_BLACK_NAME, info.name);
                }
                values.put(CallFilterConstants.COL_BLACK_REMOVE_STATE, CallFilterConstants.REMOVE_NO);
                values.put(CallFilterConstants.COL_TIME, System.currentTimeMillis());
                selection = CallFilterConstants.COL_BLACK_NUMBER;
                if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
                    selection += " LIKE ? ";
                    selectionArgs = new String[]{"%" + number};
                } else {
                    selection += " = ? ";
                    selectionArgs = new String[]{number};
                }
                int rows = database.update(CallFilterConstants.TAB_BLACK_LIST, values, selection, selectionArgs);
                if (rows <= 0) {
                    // 不存在这条记录，需要把所有状态都初始化一遍

                    values.put(CallFilterConstants.COL_BLACK_FIL_UP, CallFilterConstants.FIL_UP_NO);
                    values.put(CallFilterConstants.COL_BLACK_UPLOAD_STATE, CallFilterConstants.UPLOAD_NO);
                    values.put(CallFilterConstants.COL_BLACK_MARK_TYPE, CallFilterConstants.MK_BLACK_LIST);

                    long rowId = database.insert(CallFilterConstants.TAB_BLACK_LIST, null, values);
                    LeoLog.d(TAG, "addBlackListInner, insert rowid: " + rowId);
                }

            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            LeoLog.e(TAG, "addBlackListInner ex." + e.toString());
        } finally {
            database.endTransaction();
        }
    }

    public void addBlackList(List<BlackListInfo> listInfos) {
        if (listInfos == null || listInfos.isEmpty()) {
            LeoLog.d(TAG, "addBlackList, listinfos is empty.");
            return;
        }
        SQLiteDatabase database = getHelper().getWritableDatabase();
        if (database == null) {
            LeoLog.d(TAG, "addBlackList, database is null.");
            return;
        }
        addBlackListInner(listInfos, database);
    }

    public void addServerBlackList(List<BlackListInfo> listInfos) {
        if (listInfos == null || listInfos.isEmpty()) {
            LeoLog.d(TAG, "addServerBlackList, listinfos is empty.");
            return;
        }
        SQLiteDatabase database = getHelper().getWritableDatabase();
        if (database == null) {
            LeoLog.d(TAG, "addServerBlackList, database is null.");
            return;
        }
        ContentValues values = new ContentValues();

        database.beginTransaction();
        try {
            for (BlackListInfo info : listInfos) {
                if (info == null || TextUtils.isEmpty(info.number)) {
                    continue;
                }
                values.clear();
                String encryptedNumber = CryptoUtils.encrypt(info.number);
                values.put(CallFilterConstants.COL_SERVER_NUMBER, encryptedNumber);
                values.put(CallFilterConstants.COL_SERVER_BLACK_NUM, info.blackNum);
                values.put(CallFilterConstants.COL_SERVER_MARK_TYPE, info.markType);
                values.put(CallFilterConstants.COL_SERVER_MARK_NUM, info.markNum);

                int rows = database.update(CallFilterConstants.TAB_SERVER_BLACK_LIST, values,
                        CallFilterConstants.COL_SERVER_NUMBER + " = ?", new String[]{encryptedNumber});
                if (rows <= 0) {
                    long rowId = database.insert(CallFilterConstants.TAB_SERVER_BLACK_LIST, null, values);
                    LeoLog.d(TAG, "addServerBlackList, insert rowid: " + rowId);
                } else {
                    LeoLog.d(TAG, "addServerBlackList, update rows: " + rows);
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    public int getBlackListCount(String selection, String[] selectionArgs) {
        SQLiteDatabase database = getHelper().getReadableDatabase();
        if (database == null) {
            return 0;
        }
        Cursor cursor = null;
        try {
            cursor = database.query(CallFilterConstants.TAB_BLACK_LIST, new String[]{"_id"}, selection, selectionArgs, null, null, null);
            if (cursor != null) {
                int count = cursor.getCount();
                return count;
            }
        } catch (Exception e) {
            LeoLog.e(TAG, "getBlackListCount ex, " + e.toString());
        } finally {
            IoUtils.closeSilently(cursor);
        }

        return 0;
    }

    public BlackListInfo getServerBlackInfo(String number) {
        LeoLog.d(TAG, "getServerBlackInfo, number: " + number);
        if (TextUtils.isEmpty(number)) {
            LeoLog.d(TAG, "getServerBlackInfo, number is null");
            return null;
        }
        if (mPhoneList.isEmpty()) {
            LeoLog.d(TAG, "getServerBlackInfo, " + " phonelist is empty.");
            return null;
        }
        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) {
            LeoLog.d(TAG, "getServerBlackInfo, db is null.");
            return null;
        }

        Cursor cursor = null;
        try {
            cursor = db.query(CallFilterConstants.TAB_SERVER_BLACK_LIST, null, CallFilterConstants.COL_SERVER_NUMBER + " = ?",
                    new String[]{number}, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                return null;
            }

            cursor.moveToFirst();
            BlackListInfo info = readFromServerCursor(cursor);
            return info;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(cursor);
        }

        return null;
    }

    public List<BlackListInfo> getBlackList(String selection, String[] selectionArgs, String sortOrder) {
        List<BlackListInfo> result = new ArrayList<BlackListInfo>();
        SQLiteDatabase database = getHelper().getReadableDatabase();
        if (database == null) {
            return result;
        }
        Cursor cursor = null;
        try {
            cursor = database.query(CallFilterConstants.TAB_BLACK_LIST, null, selection, selectionArgs, null, null, sortOrder);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    BlackListInfo info = readFromCursor(cursor);
                    result.add(info);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(cursor);
        }
        return result;
    }

    public List<String> getServerNumberList() {
        return mPhoneList;
    }

    public boolean isBlackInfoExist(String number) {
        if (TextUtils.isEmpty(number)) {
            LeoLog.d(TAG, "isBlackInfoExist, number is null.");
            return false;
        }
        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) {
            LeoLog.d(TAG, "isBlackInfoExist, db is null.");
            return false;
        }
        String selection = null;
        String selectionArgs = null;
        if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
            number = PrivacyContactUtils.formatePhoneNumber(number);
            selection = " LIKE ? ";
            selectionArgs = "%" + number;
        } else {
            selection = " = ? ";
            selectionArgs = number;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.COL_BLACK_REMOVE_STATE + " = ? AND ");
        sb.append(CallFilterConstants.COL_BLACK_NUMBER + selection);
        String selects = sb.toString();
        String[] selectArgs = new String[]{String.valueOf(CallFilterConstants.REMOVE_NO), selectionArgs};

        Cursor cursor = null;
        try {
            cursor = db.query(CallFilterConstants.TAB_BLACK_LIST, new String[]{CallFilterConstants.COL_BLACK_ID},
                    selects, selectArgs, null, null, null);
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

    public BlackListInfo getBlackInfoByNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            LeoLog.d(TAG, "getBlackInfoByNumber, number is null.");
            return null;
        }
        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) {
            LeoLog.d(TAG, "getBlackInfoByNumber, db is null.");
            return null;
        }
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        String sortOrder = CallFilterConstants.COL_BLACK_ID + " " + CallFilterConstants.DESC;
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
        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterConstants.COL_BLACK_REMOVE_STATE + " = ? and ");
        sb.append(CallFilterConstants.COL_BLACK_NUMBER + numSelcts);
        String selects = sb.toString();
        String[] selectArgs = new String[]{String.valueOf(CallFilterConstants.REMOVE_NO), selArgs};
        Cursor cursor = null;
        try {
            cursor = db.query(CallFilterConstants.TAB_BLACK_LIST, null, selects, selectArgs, null, null, sortOrder);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    BlackListInfo info = readFromCursor(cursor);
                    return info;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(cursor);
        }

        return null;
    }

    private BlackListInfo readFromServerCursor(Cursor cursor) {
        BlackListInfo info = new BlackListInfo();
        info.id = cursor.getInt(cursor.getColumnIndex(CallFilterConstants.COL_BLACK_ID));
        info.number = cursor.getString(cursor.getColumnIndex(CallFilterConstants.COL_SERVER_NUMBER));
        info.markType = cursor.getInt(cursor.getColumnIndex(CallFilterConstants.COL_SERVER_MARK_TYPE));
        info.markNum = cursor.getInt(cursor.getColumnIndex(CallFilterConstants.COL_SERVER_MARK_NUM));
        info.blackNum = cursor.getInt(cursor.getColumnIndex(CallFilterConstants.COL_SERVER_BLACK_NUM));

        return info;
    }

    private BlackListInfo readFromCursor(Cursor cursor) {
        Resources res = AppMasterApplication.getInstance().getResources();
        int iconSize = res.getDimensionPixelSize(R.dimen.contact_icon_scale_size);

        int nameIndex = cursor.getColumnIndex(CallFilterConstants.COL_BLACK_NAME);
        int numberIndex = cursor.getColumnIndex(CallFilterConstants.COL_BLACK_NUMBER);
        int iconIndex = cursor.getColumnIndex(CallFilterConstants.COL_BLACK_ICON);
        int uploadIndex = cursor.getColumnIndex(CallFilterConstants.COL_BLACK_UPLOAD_STATE);
        int removeIndex = cursor.getColumnIndex(CallFilterConstants.COL_BLACK_REMOVE_STATE);
        int filUpIndex = cursor.getColumnIndex(CallFilterConstants.COL_BLACK_FIL_UP);
        int markTypeIndex = cursor.getColumnIndex(CallFilterConstants.COL_BLACK_MARK_TYPE);

        BlackListInfo info = new BlackListInfo();
        info.id = cursor.getInt(cursor.getColumnIndex(CallFilterConstants.COL_BLACK_ID));
        info.name = cursor.getString(nameIndex);
        info.number = cursor.getString(numberIndex);
        if (TextUtils.isEmpty(info.name)) {
            info.name = info.number;
        }
        Bitmap icon = null;
        byte[] iconByte = cursor.getBlob(iconIndex);
        if (iconByte != null && iconByte.length > 0) {
            try {
                icon = PrivacyContactUtils.getBmp(iconByte);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (icon != null) {
                icon = PrivacyContactUtils.getScaledContactIcon(icon, iconSize);
                info.icon = icon;
            }
        }
        info.uploadState = cursor.getInt(uploadIndex);
        info.removeState = cursor.getInt(removeIndex);
        info.filtUpState = cursor.getInt(filUpIndex);
        info.markType = cursor.getInt(markTypeIndex);

        return info;
    }

    private BlackListInfo readFromOldCursor(Cursor cursor) {
        Resources res = AppMasterApplication.getInstance().getResources();
        int iconSize = res.getDimensionPixelSize(R.dimen.contact_icon_scale_size);

        int nameIndex = cursor.getColumnIndex("name");
        int numberIndex = cursor.getColumnIndex("phone_number");
        int iconIndex = cursor.getColumnIndex("icon");
        int uploadIndex = cursor.getColumnIndex("upload_state");
        int removeIndex = cursor.getColumnIndex("remove_state");
        int markTypeIndex = cursor.getColumnIndex("marker_type");

        BlackListInfo info = new BlackListInfo();
        info.name = cursor.getString(nameIndex);
        info.number = cursor.getString(numberIndex);
        if (TextUtils.isEmpty(info.name)) {
            info.name = info.number;
        }
        Bitmap icon = null;
        byte[] iconByte = cursor.getBlob(iconIndex);
        if (iconByte != null && iconByte.length > 0) {
            try {
                icon = PrivacyContactUtils.getBmp(iconByte);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (icon != null) {
                icon = PrivacyContactUtils.getScaledContactIcon(icon, iconSize);
                info.icon = icon;
            }
        }
        info.uploadState = cursor.getInt(uploadIndex);
        info.removeState = cursor.getInt(removeIndex);
        info.markType = cursor.getInt(markTypeIndex);

        return info;
    }
}
