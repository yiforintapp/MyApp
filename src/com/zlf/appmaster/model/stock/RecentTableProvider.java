package com.zlf.appmaster.model.stock;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.zlf.appmaster.BuildConfig;
import com.zlf.appmaster.db.stock.QiNiuDBHelper;

public class RecentTableProvider extends ContentProvider {
	
	private final String TAG = RecentTableProvider.class.getSimpleName();
	private QiNiuDBHelper dbOpenHelper;


	public static final String RECENT_TABLE_NAME = "msg_recent";
	public static final String COLNAME_ID = "_id";						// Primary Key
	public static final String COLNAME_RECT_TYPE = "rectType";
	public static final String COLNAME_SUB_TYPE = "subType";         	// 子类型（可无）
	public static final String COLNAME_SUB_TYPE_ID = "subTypeID";       // 子类型ID（可无）
	public static final String COLNAME_TITLE = "title";
	public static final String COLNAME_SUMMARY = "summary";				// 显示信息的摘要
	public static final String COLNAME_UPDATE_TIME = "updateTime";
	public static final String COLNAME_UNREAD_NUM = "un_read_num";		// 新消息数量

	/**
	 * 列索引直接定义
	 */
	public static final int COLNAME_RECT_TYPE_INDEX = 1;
	public static final int COLNAME_SUB_TYPE_INDEX = 2;
	public static final int COLNAME_SUB_TYPE_ID_INDEX = 3;
	public static final int COLNAME_TITLE_INDEX = 4;
	public static final int COLNAME_SUMMARY_INDEX = 5;
	public static final int COLNAME_UPDATE_TIME_INDEX = 6;
	public static final int COLNAME_UNREAD_NUM_INDEX = 7;

	private Context mContext;
	
	// 建表  通过uin和消息类型来确认唯一性
	public static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS " + RECENT_TABLE_NAME
			+ "(" 
			+ COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
			+ COLNAME_RECT_TYPE 		+ 	 "	INTEGER NOT NULL ,"
			+ COLNAME_SUB_TYPE 			+    "  TEXT,"
			+ COLNAME_SUB_TYPE_ID 		+    "  TEXT,"
			+ COLNAME_TITLE				+	 "	TEXT, "
			+ COLNAME_SUMMARY 			+	 "	TEXT,"
			+ COLNAME_UPDATE_TIME 		+	 "	INTEGER,"
            + COLNAME_UNREAD_NUM		+ 	 "  INTEGER"
			+ ")";

	public static final String WHERE_ROW = COLNAME_RECT_TYPE +" = ? AND "  + COLNAME_SUB_TYPE + " = ? AND " + COLNAME_SUB_TYPE_ID + " = ?";
	public static final String WHERE_UNREAD_NUM_ROW = COLNAME_RECT_TYPE +" = "+ RecentItem.TYPE_MSG;


	public static final String AUTHORITY = BuildConfig.APPLICATION_ID/*com.iqiniu.qiniu.debug*/ + ".provider.recent";
	public static final String TABLE_NAME = RECENT_TABLE_NAME;
	//Uri，外部程序需要访问就是通过这个Uri访问的，这个Uri必须的唯一的。  
	public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/recent");
	// 数据集的MIME类型字符串则应该以vnd.android.cursor.dir/开头    
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/recent";
	// 单一数据的MIME类型字符串应该以vnd.android.cursor.item/开头    
	public static final String CONTENT_TYPE_ITME = "vnd.android.cursor.item/recent";
	/* 自定义匹配码 */    
	public static final int RECENTS = 1;     
	public static final int RECENT = 2;    

	 public static final UriMatcher uriMatcher;
	 static {    
		 // 常量UriMatcher.NO_MATCH表示不匹配任何路径的返回码    
		 uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		 // 如果match()方法匹配content://com.iqiniu.qiniu.provider/recent路径,返回匹配码为RECENTS    
		 uriMatcher.addURI(AUTHORITY, "recent", RECENTS);    
		 // 如果match()方法匹配content://com.iqiniu.qiniu.provider/recent/230,路径，返回匹配码为RECENT    
		 uriMatcher.addURI(AUTHORITY, "recent/#", RECENT);    
	 }  
	 
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
        return true;  
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		dbOpenHelper = QiNiuDBHelper.getInstance(getContext());
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case RECENTS:
			return db.query(RECENT_TABLE_NAME, projection, selection, selectionArgs,
					null, null, sortOrder);
		case RECENT:
			// 进行解析，返回值为10
			long personid = ContentUris.parseId(uri);
			String where = "_ID=" + personid;// 获取指定id的记录
			where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")"
					: "";// 把其它条件附加上
			return db.query(RECENT_TABLE_NAME, projection, where, selectionArgs, null,
					null, sortOrder);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
        switch (uriMatcher.match(uri)) {  
        case RECENTS:  
            return CONTENT_TYPE;  
        case RECENT:  
            return CONTENT_TYPE_ITME;  
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);
        }  
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		//获得一个可写的数据库引用，如果数据库不存在，则根据onCreate的方法里创建；
		dbOpenHelper = QiNiuDBHelper.getInstance(getContext());
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        long id = 0; 
        
        switch (uriMatcher.match(uri)) {  
        case RECENTS:  
            id = db.insert(RECENT_TABLE_NAME, null, values);	// 返回的是记录的行号，主键为int，实际上就是主键值  
            //db.close();
            return ContentUris.withAppendedId(uri, id);
        case RECENT:  
            id = db.insert(RECENT_TABLE_NAME, null, values);
           // db.close();
            String path = uri.toString();
            return Uri.parse(path.substring(0, path.lastIndexOf("/"))+id); // 替换掉id
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		dbOpenHelper = QiNiuDBHelper.getInstance(getContext());
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int count = 0;  
        switch (uriMatcher.match(uri)) {  
        case RECENTS:  
            count = db.delete(RECENT_TABLE_NAME, selection, selectionArgs);  
            break;  
        case RECENT:  
            // 下面的方法用于从URI中解析出id，对这样的路径content://hb.android.teacherProvider/teacher/10  
            // 进行解析，返回值为10  
            long personid = ContentUris.parseId(uri);
            String where = "_ID=" + personid;	// 删除指定id的记录
            where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";	// 把其它条件附加上
            count = db.delete(RECENT_TABLE_NAME, where, selectionArgs);  
            break;  
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);
        }  
        //db.close();  
        return count; 
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		// TODO Auto-generated method stub
		dbOpenHelper = QiNiuDBHelper.getInstance(getContext());
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int count = 0;  
        switch (uriMatcher.match(uri)) {  
        case RECENTS:  
            count = db.update(RECENT_TABLE_NAME, values, selection, selectionArgs);  
            break;  
        case RECENT:  
            // 下面的方法用于从URI中解析出id，对这样的路径content://com.ljq.provider.personprovider/person/10  
            // 进行解析，返回值为10  
            long personid = ContentUris.parseId(uri);
            String where = "_ID=" + personid;// 获取指定id的记录
            where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";// 把其它条件附加上
            count = db.update(RECENT_TABLE_NAME, values, where, selectionArgs);  
            break;  
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);
        }  
        //db.close();  
        return count;
	}
	
	/**
	 * 批量插入的接口
	 */
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		// TODO Auto-generated method stub
		dbOpenHelper = QiNiuDBHelper.getInstance(getContext());
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		int numValues = 0;
		db.beginTransaction(); // 开始事务
		try {
			// 数据库操作
			numValues = values.length;
			for (int i = 0; i < numValues; i++) {
				switch (uriMatcher.match(uri)) {
		        case RECENTS:{
		        	int type = values[i].getAsInteger(COLNAME_RECT_TYPE);
					String subType = values[i].getAsString(COLNAME_SUB_TYPE);
					String subTypeID = values[i].getAsString(COLNAME_SUB_TYPE_ID);
		        	
		        	Cursor c = db.query(RECENT_TABLE_NAME, null, WHERE_ROW,
		        			new String[] {String.valueOf(type), subType, subTypeID}, null, null, null);
		        	if(!c.moveToFirst()){	// 无数据则插入
		        		db.insert(RECENT_TABLE_NAME, null, values[i]);
		        	}
		        	else{
		        		// 有此条数据，但它的时间小于记录时间，则不更新最近的内容，只更新未读数量
		        		long time  = values[i].getAsLong(COLNAME_UPDATE_TIME);
		        		if(time < c.getLong(COLNAME_UPDATE_TIME_INDEX)){
		        			ContentValues tmpValues = new ContentValues();
		        			tmpValues.put(COLNAME_UNREAD_NUM, values[i].getAsInteger(COLNAME_UNREAD_NUM));
		        			db.update(RECENT_TABLE_NAME, tmpValues, RecentTableProvider.WHERE_ROW, new String[] {String.valueOf(type), subType, subTypeID});
		        		}
		        		else{
		        			db.update(RECENT_TABLE_NAME, values[i], RecentTableProvider.WHERE_ROW, new String[] {String.valueOf(type), subType, subTypeID});
		        		} 		
		        	}
		        	c.close();
		        }
		            break;
		        case RECENT:  
		            db.insert(RECENT_TABLE_NAME, null, values[i]);
		            break;
		        default:  
		            throw new IllegalArgumentException("Unknown URI " + uri);
		        }
			}
			db.setTransactionSuccessful(); // 别忘了这句 Commit
		} finally {
			db.endTransaction(); // 结束事务
			//db.close();
		}

		return numValues;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		//super.shutdown();
	}
	
	
}
