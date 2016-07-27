package com.zlf.appmaster.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.zlf.appmaster.utils.LeoLog;

import java.util.ArrayList;

public class AppMasterProvider extends ContentProvider {
	private static final String TAG = "AppMasterProvider";
	public static final String SQL_INSERT_OR_REPLACE = "__sql_insert_or_replace__";

	private AppMasterDBHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = AppMasterDBHelper.getInstance(getContext());
		return true;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int numValues = 0;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		db.beginTransaction();
		try {
			numValues = values.length;
			for (int i = 0; i < numValues; i++) {
				insert(uri, values[i]);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return numValues;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String orderBy) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = null;

		try {
			// if (Constants.TABLE_DOWNLOAD.equals(args.table)) {
			c = db.query(args.table, projection, selection, selectionArgs,
					null, null, orderBy);
			// }
		} catch (Exception e) {
			LeoLog.e(TAG, e.getMessage());
		}

		if (c != null) {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}

		return c;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SqlArguments args = new SqlArguments(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		long rowId = 0;
		try {
			boolean replace = false;
            if (values.containsKey(SQL_INSERT_OR_REPLACE)) {
                replace = values.getAsBoolean(SQL_INSERT_OR_REPLACE);
            }
            values.remove(SQL_INSERT_OR_REPLACE);
			if (replace) {
				rowId = db.replace(args.table, null, values);
			} else {
				rowId = db.insert(args.table, null, values);
			}
		} catch (Exception e) {
			LeoLog.e(TAG, e.getMessage());
		}

		getContext().getContentResolver().notifyChange(uri, null);

		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		LeoLog.e(TAG, "insert error " + uri.toString());
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;

		db.beginTransaction();

		try {
			count = db.delete(args.table, selection, selectionArgs);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LeoLog.e(TAG, e.getMessage());
		} finally {
			db.endTransaction();
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;

		db.beginTransaction();
		try {
			count = db.update(args.table, values, selection, selectionArgs);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LeoLog.e(TAG, e.getMessage());
		} finally {
			db.endTransaction();
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static class SqlArguments {
		public final String table;
		public final String where;
		public final String[] args;

		SqlArguments(Uri url, String where, String[] args) {
			if (url.getPathSegments().size() == 1) {
				this.table = url.getPathSegments().get(0);
				this.where = where;
				this.args = args;
			} else if (url.getPathSegments().size() != 2) {
				throw new IllegalArgumentException("Invalid URI: " + url);
			} else if (!TextUtils.isEmpty(where)) {
				throw new UnsupportedOperationException(
						"WHERE clause not supported: " + url);
			} else {
				this.table = url.getPathSegments().get(0);
				this.where = "_id=" + ContentUris.parseId(url);
				this.args = null;
			}
		}

		SqlArguments(Uri url) {
			if (url.getPathSegments().size() == 1) {
				table = url.getPathSegments().get(0);
				where = null;
				args = null;
			} else {
				throw new IllegalArgumentException("Invalid URI: " + url);
			}
		}
	}

	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			ContentProviderResult[] results = super.applyBatch(operations);
			db.setTransactionSuccessful();
			return results;
		} finally {
			db.endTransaction();
		}
	}

}
