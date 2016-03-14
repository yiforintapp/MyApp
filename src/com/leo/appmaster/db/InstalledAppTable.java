package com.leo.appmaster.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.imageloader.utils.IoUtils;

/**
 * Created by Jasper on 2015/10/13.
 */
public class InstalledAppTable extends BaseTable {

    private static final byte[] LOCK = new byte[1];
    protected static final String TABLE_NAME = "installed_app";

    protected static final String COL_PKG = "pkg";
    protected static final String COL_IGNORED = "ignored";

    private static InstalledAppTable sInstance;
    private List<String> mPkgList;

    public static InstalledAppTable getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new InstalledAppTable();
                }
            }
        }

        return sInstance;
    }

    public InstalledAppTable() {}

    @Override
    public void createTable(SQLiteDatabase db) {
//        if (BuildProperties.isZTEAndApiLevel14()) return;

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "( _id INTEGER PRIMARY KEY," +
                COL_IGNORED + " INTEGER," +
                COL_PKG + " TEXT);");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (BuildProperties.isZTEAndApiLevel14()) return;

        if (oldVersion <= 7 && newVersion >= 8) {
            // 版本8才加入此表
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    "( _id INTEGER PRIMARY KEY," +
                    COL_IGNORED + " INTEGER," +
                    COL_PKG + " TEXT);");
        }
    }

    public void removePackageList(List<AppItemInfo> itemInfos) {
        if (itemInfos == null || itemInfos.size() <= 0) {
            return;
        }

        initPkgListIfNeed();
        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) return;

        db.beginTransaction();
        try {
            for (AppItemInfo info : itemInfos) {
                if (info == null || TextUtils.isEmpty(info.packageName)) {
                    continue;
                }
                mPkgList.remove(info.packageName);
                try {
                    db.delete(TABLE_NAME, COL_PKG + " = ?", new String[]{ info.packageName });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void insertIgnoreItemList(List<AppItemInfo> itemInfos) {
        if (itemInfos == null) return;

        List<String> pkgList = new ArrayList<String>();
        for (AppItemInfo itemInfo : itemInfos) {
            pkgList.add(itemInfo.packageName);
        }

        insertIgnoreList(pkgList);
    }

    public void insertIgnoreList(List<String> list) {
        if (list == null) return;
        initPkgListIfNeed();

        List<String> needToInsert = new ArrayList<String>();
        for (String pkg : list) {
            if (!mPkgList.contains(pkg)) {
                needToInsert.add(pkg);
                mPkgList.add(pkg);
            }
        }
        insertListInner(needToInsert);

    }

    public synchronized List<String> getIgnoredList() {
        List<String> result = new ArrayList<String>();
        if (BuildProperties.isApiLevel14()) {
            synchronized (LOCK) {
                initPkgListIfNeed();

                if (mPkgList != null) {
                    result.addAll(mPkgList);
                }
            }
        } else {
            initPkgListIfNeed();

            if (mPkgList != null) {
                result.addAll(mPkgList);
            }
        }

        return result;
    }

    public void insertListInner(List<String> list) {
        if (list == null || list.isEmpty()) return;

        if (BuildProperties.isApiLevel14()) {
            synchronized (LOCK) {
                doInsertList(list);
            }
        } else {
            doInsertList(list);
        }
    }

    private void doInsertList(List<String> list) {
        SQLiteDatabase db = getHelper().getWritableDatabase();
        if (db == null) return;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            for (String pkg : list) {
                values.clear();
                values.put(COL_PKG, pkg);

                db.insert(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private void initPkgListIfNeed() {
        if (mPkgList != null) return;

        List<String> list = new ArrayList<String>();
        SQLiteDatabase db = getHelper().getReadableDatabase();
        if (db == null) return;

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, new String[] { COL_PKG }, null,
                    null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    String pkg = cursor.getString(cursor.getColumnIndex(COL_PKG));
                    list.add(pkg);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(cursor);
        }

        mPkgList = new ArrayList<String>();
        if (list != null) {
            mPkgList.addAll(list);
        }
    }

}
