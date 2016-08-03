package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.zlf.appmaster.bean.Account;
import com.zlf.appmaster.utils.QLog;

/**
 * 用户 -- DB
 * @author Yushian
 *
 */
public class UserTable extends BaseDB {
	
	public static final String TAG = "UserTable";
    
    /**
     * 配置表 当前用户
     */
    public static final String TABLE_NAME = "user";
    public static final String SETTING_NEWFRIEND_CP = "newfriend_cp"; //新股友 cp 该用户上次同步的最新版本号
    public static final String SETTING_BIRTHDAY = "birthday";
    public static final String SETTING_JOB = "job";
    public static final String SETTING_STOCKAGE = "stockage";
	public static final String SETTING_DISTURB = "disturb";
	public static final String SETTING_DISTURB_BEGIN_HOUR = "begin_hour";
	public static final String SETTING_DISTURB_BEGIN_MINUTE = "begin_minute";
	public static final String SETTING_DISTURB_STOP_HOUR = "stop_hour";
	public static final String SETTING_DISTURB_STOP_MINUTE = "stop_minute";
    public static final String SETTING_UPLOAD_CONTACTS = "upload_contacts";//是否上传过通讯录
    public static final String SETTING_UPLOAD_WEIBO = "upload_weibo";//是否上传过weibo Token
    public static final String SETTING_IM_LAST_VERSION = "im_last_version";	//即时消息的版本号

	public static final String SETTING_JOB_NAME= "job_name";	//即时消息的版本号
	public static final String SETTING_JOB_CARD = "job_card";	//即时消息的版本号
	public static final String SETTING_COMPANY_NAME= "company_name";	//即时消息的版本号
//	public static final String SETTING_POST_USER_TEXT_INFO = "post_user_text_info";//上传用户文本信息
//	public static final String SETTING_SESSION_ID = "session_id";

	public static String getCreateSettingTableString() {
		String createString =
	    		"CREATE TABLE IF NOT EXISTS "
	    		+ TABLE_NAME +" (id integer primary key autoincrement, "
	    		+ USER_UIN + 				" INTEGER, "
//				+ SETTING_SESSION_ID + 		" INTEGER, "
	    		+ SETTING_NEWFRIEND_CP + 	" INTEGER,"
	    		+ SETTING_BIRTHDAY+ 		" TEXT,"
	    		+ SETTING_JOB+ 				" TEXT,"
	    		+ SETTING_STOCKAGE+ 		" TEXT,"
				+ SETTING_DISTURB+ 			" TEXT,"
				+ SETTING_DISTURB_BEGIN_HOUR 	+ " INTEGER,"
				+ SETTING_DISTURB_BEGIN_MINUTE 	+ " INTEGER,"
				+ SETTING_DISTURB_STOP_HOUR   	+ " INTEGER,"
				+ SETTING_DISTURB_STOP_MINUTE	+ " INTEGER,"
	    		+ SETTING_UPLOAD_CONTACTS + " INTEGER,"
	    		+ SETTING_UPLOAD_WEIBO + 	" INTEGER,"
	    		
	    		+ USER_NAME + 				" TEXT,"
	    		+ USER_SIGN + 				" TEXT,"
				+ USER_RESUME + 			" TEXT,"
	    		+ USER_LEVEL + 				" TEXT,"
	    		+ USER_MONEY + 				" INTEGER,"
	    		+ USER_SEX +				" INTEGER,"
	    		+ USER_HEADURL +			" TEXT,"
//				+ SETTING_POST_USER_TEXT_INFO + " INTEGER,"
				+ SETTING_IM_LAST_VERSION +	" INTEGER,"
				+ SETTING_JOB_NAME +			" TEXT,"
				+ SETTING_JOB_CARD +			" TEXT,"
				+ SETTING_COMPANY_NAME+	" TEXT)"
	    		
	    		;
		return createString;
	}
	public static final String WHERE_ROW = USER_UIN + " = ? ";
	
	private SQLiteDatabase db;
	private UserDBHelper m_dbHelper;

	public UserTable(Context context) {
		m_dbHelper = UserDBHelper.getInstance(context);
		createTable();
	}

	private void createTable() {
		synchronized (m_dbHelper) {
			db = m_dbHelper.getWritableDatabase();
			db.execSQL(getCreateSettingTableString());
			db.close();
		}
	}
	
	/**
	 * 根据uin获取本地保存的用户信息
	 * @param uin
	 * @return
	 */
	public Account getUserInfoByUin(String uin) {
		
		if (TextUtils.isEmpty(uin)) {
			return null;
		}
		synchronized (m_dbHelper) {
			db = m_dbHelper.getReadableDatabase();
			Account user = new Account();
			Cursor cursor = db.query(TABLE_NAME,
					null,
					USER_UIN + "=?", new String[]{uin},
					null, null, FRIEND_SORT_ORDER);
			if (cursor.moveToNext()) {
				QLog.i(TAG, "有查询到该数据");
				user.setUin(uin);
				user.setBirthday(cursor.getString(cursor.getColumnIndex(SETTING_BIRTHDAY)));
				user.setJob(cursor.getString(cursor.getColumnIndex(SETTING_JOB)));
				user.setStockAge(cursor.getString(cursor.getColumnIndex(SETTING_STOCKAGE)));
				user.setDisturb(cursor.getInt(cursor.getColumnIndex(SETTING_DISTURB)));
				user.setBeginHour(cursor.getInt(cursor.getColumnIndex(SETTING_DISTURB_BEGIN_HOUR)));
				user.setBeginMinute(cursor.getInt(cursor.getColumnIndex(SETTING_DISTURB_BEGIN_MINUTE)));
				user.setStopHour(cursor.getInt(cursor.getColumnIndex(SETTING_DISTURB_STOP_HOUR)));
				user.setStopMinute(cursor.getInt(cursor.getColumnIndex(SETTING_DISTURB_STOP_MINUTE)));
				user.setName(cursor.getString(cursor.getColumnIndex(USER_NAME)));
				user.setMoney(cursor.getLong(cursor.getColumnIndex(USER_MONEY)));
				user.setSign(cursor.getString(cursor.getColumnIndex(USER_SIGN)));
				user.setLevel(cursor.getString(cursor.getColumnIndex(USER_LEVEL)));
				user.setSex(cursor.getInt(cursor.getColumnIndex(USER_SEX)));
				user.setOHeadImg(cursor.getString(cursor.getColumnIndex(USER_HEADURL)));
				user.setResume(cursor.getString(cursor.getColumnIndex(USER_RESUME)));
				user.setJobName(cursor.getString(cursor.getColumnIndex(SETTING_JOB_NAME)));
				user.setJobCard(cursor.getString(cursor.getColumnIndex(SETTING_JOB_CARD)));
				user.setCompanyName(cursor.getString(cursor.getColumnIndex(SETTING_COMPANY_NAME)));
				if (cursor.getInt(cursor.getColumnIndex(SETTING_UPLOAD_CONTACTS)) == 1) {
					user.setIsUploadContacts(true);
				} else {
					user.setIsUploadContacts(false);
				}

				if (cursor.getInt(cursor.getColumnIndex(SETTING_UPLOAD_WEIBO)) == 1) {
					user.setIsUploadWeibo(true);
				} else {
					user.setIsUploadWeibo(false);
				}
				/*if (cursor.getInt(cursor.getColumnIndex(SETTING_POST_USER_TEXT_INFO)) == 1) {
					user.setIsPostUserTextInfo(true);
				} else {
					user.setIsPostUserTextInfo(false);
				}*/
			}
			cursor.close();
			db.close();
			return user;
		}
	}
	
	/**
	 * 修改用户信息 保存上传通讯录标记
	 */
	public void saveUploadContactsFlag(String uinString, Boolean flag) {
		db = m_dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		if (flag) {
			values.put(SETTING_UPLOAD_CONTACTS, 1);	
		}else {
			values.put(SETTING_UPLOAD_CONTACTS, 0);
		}
		
		db.update(TABLE_NAME,
				values, USER_UIN + "=?", new String[]{uinString});
	}
	
	/**
	 * 修改用户信息 保存上传微博标记
	 */
	public void saveUploadWeiboFlag(String uinString, Boolean flag) {
		db = m_dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		
		if (flag) {
			values.put(SETTING_UPLOAD_WEIBO, 1);	
		}else {
			values.put(SETTING_UPLOAD_WEIBO, 0);
		}
		
		
		db.update(TABLE_NAME,
				values, USER_UIN + "=?", new String[]{uinString});
	}
	
	
	/**
	 * 保存用户的级别与资产
	 */
	public void saveUserLevelAndMoney(String levelString, long money, String uinString) {
		
		db = m_dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		
		values.put(USER_LEVEL, levelString);
		values.put(USER_MONEY, money);
		
		db.update(TABLE_NAME,
				values, USER_UIN + "=?", new String[]{uinString});
	}
	


	private boolean isExist(long uin){
		synchronized (db){
			Cursor c = db.query(TABLE_NAME, null, WHERE_ROW,
					new String[] { String.valueOf(uin)}, null, null, null);
			boolean ret = c.moveToFirst();
			c.close();
			return ret;
		}
	}

	/**
	 * 保存用户信息 
	 */
	public void saveUserInfo(Account user) {
		if (user == null || TextUtils.isEmpty(user.getUin())) {
			return;
		}
		ContentValues values = new ContentValues();
		values.put(USER_UIN, Long.parseLong(user.getUin()));
		
		if (!TextUtils.isEmpty(user.getBirthday())) {
			values.put(SETTING_BIRTHDAY, user.getBirthday());
		}
		if (!TextUtils.isEmpty(user.getJob())) {
			values.put(SETTING_JOB, user.getJob());
		}
		if (!TextUtils.isEmpty(user.getLevel())) {
			values.put(USER_LEVEL, user.getLevel());
		}

		if (!TextUtils.isEmpty(user.getOHeadImg())) {
			values.put(USER_HEADURL, user.getOHeadImg());
		}


		//values.put(SETTING_POST_USER_TEXT_INFO, user.getIsPostUserTextInfo());
		values.put(USER_MONEY, user.getMoney());
		
		if (!TextUtils.isEmpty(user.getName())) {
			values.put(USER_NAME, user.getName());
		}
		values.put(USER_SEX, user.getSex());
		values.put(USER_SIGN, user.getSign());
		values.put(USER_RESUME, user.getResume());
		values.put(SETTING_JOB_NAME, user.getJobName());
		values.put(SETTING_JOB_CARD, user.getJobCard());
		values.put(SETTING_COMPANY_NAME, user.getCompanyName());
		if (!TextUtils.isEmpty(user.getStockAge())) {
			values.put(SETTING_STOCKAGE, user.getStockAge());
		}
		values.put(SETTING_DISTURB, user.getDisturb());
		values.put(SETTING_DISTURB_BEGIN_HOUR, user.getBeginHour());
		values.put(SETTING_DISTURB_BEGIN_MINUTE, user.getBeginMinute());
		values.put(SETTING_DISTURB_STOP_HOUR, user.getStopHour());
		values.put(SETTING_DISTURB_STOP_MINUTE, user.getStopMinute());

		synchronized (m_dbHelper) {
			db = m_dbHelper.getWritableDatabase();
			if (isExist(Long.valueOf(user.getUin()))) {
				db.update(TABLE_NAME,
						values, USER_UIN + "=?", new String[]{user.getUin()});
			} else {
				db.insert(TABLE_NAME, null, values);
			}
			db.close();
		}

	}
	
	// 保存IM的版本号
	public void saveIMLastVersion(String userUin, long lastCheckPoint){
		ContentValues values = new ContentValues();
		values.put(SETTING_IM_LAST_VERSION, lastCheckPoint);
		db = m_dbHelper.getWritableDatabase();
		db.update(TABLE_NAME,
				values, USER_UIN + "=?", new String[]{userUin});
		QLog.i(TAG, "db im save lastCheckPoint:"+lastCheckPoint);
	}
	// 获取IM版本号
	public long readIMLastVersion(String userUin){
		db = m_dbHelper.getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, null, USER_UIN+" =? ",
				new String[]{userUin}, null, null, null);
		long ret = 0;
		if(c.moveToFirst()){
			ret = c.getLong((c.getColumnIndex(SETTING_IM_LAST_VERSION)));	
		}
		QLog.i(TAG, "db im lastCheckPoint2:"+ret);	
		c.close();
		return ret;
	}
}

