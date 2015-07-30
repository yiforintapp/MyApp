
package com.leo.appmaster.applocker.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.utils.BitmapUtils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * DOA for Lock mode, Time lock and location lock
 * 
 * @author zhangwenyang
 */
public class LockModeDao {
    private final ContentResolver mResolver;

    public LockModeDao(Context context) {
        super();
        mResolver = context.getContentResolver();
    }

    // ====== LockMode DOA ============
    public List<LockMode> querryLockModeList() {
        Cursor cursor = mResolver.query(Constants.LOCK_MODE_URI, null, null, null, null);
        List<LockMode> modeList = new LinkedList<LockMode>();
        if (cursor != null) {
            LockMode mode;
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_LOCK_MODE_ID));
                    String name = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_LOCK_MODE_NAME));
                    String lockPacks = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_LOCKED_LIST));
                    List<String> lockList;
                    if (lockPacks.equals("")) {
                        lockList = Collections.synchronizedList(new ArrayList<String>(0));
                    } else {
                        lockList = Collections.synchronizedList(new ArrayList<String>(Arrays.asList(lockPacks.split(";"))));
                    }

                    byte[] bytes = cursor.getBlob(cursor
                            .getColumnIndex(Constants.COLUMN_MODE_ICON));
                    int defaultFlag = cursor.getInt(cursor
                            .getColumnIndex(Constants.COLUMN_DEFAULT_MODE_FLAG));
                    int curUsed = cursor.getInt(cursor
                            .getColumnIndex(Constants.COLUMN_CURRENT_USED));
                    int opened = cursor.getInt(cursor
                            .getColumnIndex(Constants.COLUMN_OPENED));
                    mode = new LockMode();
                    mode.modeId = id;
                    mode.modeName = name;
                    mode.lockList = lockList;
                    mode.defaultFlag = defaultFlag;
                    mode.isCurrentUsed = curUsed == 0 ? true : false;
                    mode.haveEverOpened = opened == 0 ? true : false;
                    if (bytes != null) {
                        mode.modeIcon = BitmapUtils.bytes2Bimap(bytes);
                    }
                    modeList.add(mode);
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return modeList;
    }

    public synchronized void insertLockMode(LockMode lockMode) {
        if (lockMode == null)
            return;
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_LOCK_MODE_NAME, lockMode.modeName);
        String lockList = "";
        if (lockMode.lockList != null) {
            for (String pkg : lockMode.lockList) {
                lockList += pkg + ";";
            }
        }
        byte[] bitmap = null;
        if (lockMode.modeIcon != null) {
            bitmap = BitmapUtils.Bitmap2Bytes(lockMode.modeIcon);
        }
        values.put(Constants.COLUMN_MODE_ICON, bitmap);
        values.put(Constants.COLUMN_LOCKED_LIST, lockList);
        values.put(Constants.COLUMN_DEFAULT_MODE_FLAG, lockMode.defaultFlag);
        values.put(Constants.COLUMN_CURRENT_USED, lockMode.isCurrentUsed ? 0 : 1);
        values.put(Constants.COLUMN_OPENED, lockMode.haveEverOpened ? 0 : 1);
        Uri uri = mResolver.insert(Constants.LOCK_MODE_URI, values);
        lockMode.modeId = (int) ContentUris.parseId(uri);
    }

    public void updateLockMode(LockMode lockMode) {
        if (lockMode == null)
            return;
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_LOCK_MODE_NAME, lockMode.modeName);
        String lockList = "";
        for (String pkg : lockMode.lockList) {
            lockList += pkg + ";";
        }
        values.put(Constants.COLUMN_LOCKED_LIST, lockList);
        values.put(Constants.COLUMN_DEFAULT_MODE_FLAG, lockMode.defaultFlag);
        values.put(Constants.COLUMN_CURRENT_USED, lockMode.isCurrentUsed ? 0 : 1);
        values.put(Constants.COLUMN_OPENED, lockMode.haveEverOpened ? 0 : 1);
        mResolver.update(Constants.LOCK_MODE_URI, values, "_id=" + lockMode.modeId, null);
    }

    public void deleteLockMode(LockMode lockMode) {
        if (lockMode == null)
            return;
        mResolver.delete(Constants.LOCK_MODE_URI, "_id=" + lockMode.modeId, null);
    }

    // ====== TimeLock DOA ============
    public List<TimeLock> querryTimeLockList() {
        Cursor cursor = mResolver.query(Constants.TIME_LOCK_URI, null, null, null, null);
        List<TimeLock> timeLockList = new LinkedList<TimeLock>();
        if (cursor != null) {
            TimeLock timeLock;
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_TIME_LOCK_ID));
                    String name = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_TIME_LOCK_NAME));
                    String lockTime = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_LOCK_TIME));
                    int modeId = cursor
                            .getInt(cursor.getColumnIndex(Constants.COLUMN_LOCK_MODE));
                    String modeName = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_LOCK_MODE_NAME));
                    byte repeatMode = (byte) cursor.getInt(cursor
                            .getColumnIndex(Constants.COLUMN_REPREAT_MODE));
                    int using = (byte) cursor.getInt(cursor
                            .getColumnIndex(Constants.COLUMN_TIME_LOCK_USING));

                    timeLock = new TimeLock(id, name, lockTime, modeId, modeName, repeatMode);
                    timeLock.using = (using == 0);
                    timeLockList.add(timeLock);
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return timeLockList;
    }

    public void insertTimeLock(TimeLock timeLock) {
        if (timeLock == null)
            return;
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_TIME_LOCK_NAME, timeLock.name);
        values.put(Constants.COLUMN_LOCK_MODE, timeLock.lockModeId);
        values.put(Constants.COLUMN_LOCK_MODE_NAME, timeLock.lockModeName);
        values.put(Constants.COLUMN_LOCK_TIME, timeLock.time.toString());
        values.put(Constants.COLUMN_REPREAT_MODE, timeLock.repeatMode.toInt());
        values.put(Constants.COLUMN_TIME_LOCK_USING, timeLock.using ? 0 : 1);
        Uri uri = mResolver.insert(Constants.TIME_LOCK_URI, values);
        timeLock.id = (int) ContentUris.parseId(uri);
    }

    public void updateTimeLock(TimeLock timeLock) {
        if (timeLock == null)
            return;
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_TIME_LOCK_NAME, timeLock.name);
        values.put(Constants.COLUMN_LOCK_MODE, timeLock.lockModeId);
        values.put(Constants.COLUMN_LOCK_MODE_NAME, timeLock.lockModeName);
        values.put(Constants.COLUMN_LOCK_TIME, timeLock.time.toString());
        values.put(Constants.COLUMN_REPREAT_MODE, timeLock.repeatMode.toInt());
        values.put(Constants.COLUMN_TIME_LOCK_USING, timeLock.using ? 0 : 1);
        mResolver.update(Constants.TIME_LOCK_URI, values, "_id=" + timeLock.id, null);
    }

    public void deleteTimeLock(TimeLock timeLock) {
        if (timeLock == null)
            return;
        mResolver.delete(Constants.TIME_LOCK_URI, "_id=" + timeLock.id, null);
    }

    // ====== LocationLock DOA ============
    public List<LocationLock> querryLocationLockList() {
        Cursor cursor = mResolver.query(Constants.LOCATION_LOCK_URI, null, null, null, null);
        List<LocationLock> locationLockList = new LinkedList<LocationLock>();
        if (cursor != null) {
            LocationLock locationLock;
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor
                            .getColumnIndex(Constants.COLUMN_LOCATION_LOCK_ID));
                    String name = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_LOCATION_LOCK_NAME));
                    String ssid = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_WIFF_NAME));
                    int entranceId = cursor.getInt(cursor
                            .getColumnIndex(Constants.COLUMN_ENTRANCE_MODE));
                    String entanceModeName = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_ENTRANCE_MODE_NAME));
                    int quitId = cursor.getInt(cursor
                            .getColumnIndex(Constants.COLUMN_QUITE_MODE));
                    String quitModeName = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_QUITE_MODE_NAME));
                    boolean using = cursor.getInt(cursor
                            .getColumnIndex(Constants.COLUMN_LOCATION_LOCK_USING)) == 0 ? true
                            : false;

                    locationLock = new LocationLock();
                    locationLock.id = id;
                    locationLock.name = name;
                    locationLock.ssid = ssid;
                    locationLock.entranceModeId = entranceId;
                    locationLock.entranceModeName = entanceModeName;
                    locationLock.quitModeId = quitId;
                    locationLock.quitModeName = quitModeName;
                    locationLock.using = using;

                    locationLockList.add(locationLock);
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return locationLockList;
    }

    public void insertLocationLock(LocationLock locationLock) {
        if (locationLock == null)
            return;
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_LOCATION_LOCK_NAME, locationLock.name);
        values.put(Constants.COLUMN_WIFF_NAME, locationLock.ssid);
        values.put(Constants.COLUMN_ENTRANCE_MODE, locationLock.entranceModeId);
        values.put(Constants.COLUMN_ENTRANCE_MODE_NAME, locationLock.entranceModeName);
        values.put(Constants.COLUMN_QUITE_MODE, locationLock.quitModeId);
        values.put(Constants.COLUMN_QUITE_MODE_NAME, locationLock.quitModeName);
        values.put(Constants.COLUMN_LOCATION_LOCK_USING, locationLock.using ? 0 : 1);
        Uri uri = mResolver.insert(Constants.LOCATION_LOCK_URI, values);
        locationLock.id = ContentUris.parseId(uri);
    }

    public void updateLocationLock(LocationLock locationLock) {
        if (locationLock == null)
            return;
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_LOCATION_LOCK_NAME, locationLock.name);
        values.put(Constants.COLUMN_WIFF_NAME, locationLock.ssid);
        values.put(Constants.COLUMN_ENTRANCE_MODE, locationLock.entranceModeId);
        values.put(Constants.COLUMN_ENTRANCE_MODE_NAME, locationLock.entranceModeName);
        values.put(Constants.COLUMN_QUITE_MODE, locationLock.quitModeId);
        values.put(Constants.COLUMN_QUITE_MODE_NAME, locationLock.quitModeName);
        values.put(Constants.COLUMN_LOCATION_LOCK_USING, locationLock.using ? 0 : 1);
        mResolver.update(Constants.LOCATION_LOCK_URI, values, "_id=" + locationLock.id, null);
    }

    public void deleteLocationLock(LocationLock locationLock) {
        if (locationLock == null)
            return;
        mResolver.delete(Constants.LOCATION_LOCK_URI, "_id=" + locationLock.id, null);
    }

}
