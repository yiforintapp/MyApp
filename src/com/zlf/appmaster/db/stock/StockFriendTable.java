package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.zlf.appmaster.bean.StockFriend;
import com.zlf.appmaster.utils.QLog;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * 通讯录股友 -- DB  区分用户
 * @author Yushian
 *
 */
public class StockFriendTable extends BaseDB{

	public static final String TAG = "QiNiuDBHelper";
	
	public static final String TABLE_STOCK_FRIEND = "friend";
    /**
     * 股友表 区分账号
     */
    public static final String FRIEND_INDEX = "search_index";
    public static final String FRIEND_RELATION = "relation";
    public static final String FRIEND_GROUP = "group";//群组..
    public static final String FRIEND_PHONE = "phone";

   
	public static String getCreateFriendTableString(String tableFriendName){
	    // 注意空格
		String createString =
	    		"CREATE TABLE IF NOT EXISTS "
	    		+ tableFriendName+" (id integer primary key autoincrement, "
	    		+ USER_UIN +		" INTEGER,"
	    		+ USER_NAME + 		" TEXT,"
	    		+ FRIEND_INDEX + 	" TEXT,"
	    		+ USER_SIGN + 		" TEXT,"
	    		+ USER_LEVEL + 		" TEXT,"
	    		+ FRIEND_RELATION + " INTEGER, "
	    		+ USER_MONEY + 		" INTEGER,"
	    		+ USER_SEX +		" TEXT,"
	    		+ USER_HEADPIC +	" BLOB,"
	    		+ USER_HEADURL +	" TEXT)";
		return createString;
	}
	
    public static final String[] FRIEND_ALL_PROJECTION = {
		USER_ID,
		USER_UIN,
		USER_NAME,
		FRIEND_INDEX,
		USER_MONEY,
		USER_SIGN,
		USER_LEVEL,
		FRIEND_RELATION,
//		FRIEND_GROUP,
		USER_SEX,
		USER_HEADURL,
		USER_HEADPIC
 };
	
	private SQLiteDatabase mDatabase;
	private QiNiuDBHelper qiNiuDataBase;
	private String mTableFriendName;
	
	public StockFriendTable(Context context) {
		
		qiNiuDataBase = QiNiuDBHelper.getInstance(context);
		mTableFriendName = TABLE_STOCK_FRIEND;
	}
	
	/**
	 * 添加好友
	 * @param friend
	 * @return
	 */
	public Boolean addFriend(StockFriend friend) {
		
		if (friend == null) {
			QLog.e(TAG,"添加失败，传入数据为空！");
			return false;
		}
		
		mDatabase = qiNiuDataBase.getWritableDatabase();
		
		ContentValues values = getValuesFromStockFriend(friend);
		
		mDatabase.insert(mTableFriendName,
				USER_ID, values);
		
		//mDatabase.close();
		return true;
	}
	
	
	/**
	 * 通过好友uin 删除好友  
	 * @param uin
	 * @return
	 */
	public Boolean deleteFriend(String uin) {
		if (TextUtils.isEmpty(uin)) {
			return false;
		}
		mDatabase = qiNiuDataBase.getWritableDatabase();
		mDatabase.delete(mTableFriendName,
				 USER_UIN+"=?", new String[]{uin});
		//mDatabase.close();
		return true;
	}
	
	/**
	 * 批量删除好友
	 * @param friends
	 * @return
	 */
	public Boolean batchDeleteFriend(String[] friends) {
		if (friends == null) {
			return false;
		}
		mDatabase = qiNiuDataBase.getWritableDatabase();
		mDatabase.delete(mTableFriendName,
				 USER_UIN+"=?",friends);
		//mDatabase.close();
		return true;
	}
	
	/**
	 * 更新好友 
	 * @param friend
	 * @return
	 */
	public Boolean updateFriend(StockFriend friend) {
		ContentValues values = getValuesFromStockFriend(friend);
		
		mDatabase = qiNiuDataBase.getWritableDatabase();
		mDatabase.update(mTableFriendName,
				values, USER_UIN+"=?", new String[]{friend.getUin()});
		//mDatabase.close();
		return true;
				
	}
	
	private ContentValues getValuesFromStockFriend(StockFriend friend) {
		ContentValues values = new ContentValues();
		
		values.put(USER_UIN, Long.parseLong(friend.getUin()));
		values.put(FRIEND_INDEX, friend.getItemForIndex());
		values.put(USER_NAME, friend.getName());
		values.put(USER_LEVEL, friend.getLevel());
		values.put(USER_SIGN, friend.getSignature());
		values.put(USER_MONEY, friend.getVirtualMoney());
		values.put(USER_SEX, friend.getSex());
		return values;
	}
	
	//获取好友
	public HashSet<String> getFriends(ArrayList<StockFriend> friends) {
		HashSet<String> uinHashSet = new HashSet<String>();
		
		mDatabase = qiNiuDataBase.getReadableDatabase();
		
		Cursor cursor = mDatabase.query(mTableFriendName,
				FRIEND_ALL_PROJECTION, null, null, null, null, FRIEND_SORT_ORDER);
		while (cursor.moveToNext()) {
			StockFriend friend = new StockFriend();
			long uin = cursor.getInt(cursor.getColumnIndex(USER_UIN));
			friend.setUin(Long.toString(uin));
			friend.setLevel(cursor.getString(cursor.getColumnIndex(USER_LEVEL)));
			//要在设置名字之前
			friend.setIndex(cursor.getString(cursor.getColumnIndex(FRIEND_INDEX)));
			friend.setName(cursor.getString(cursor.getColumnIndex(USER_NAME)),true);
			friend.setSignature(cursor.getString(cursor.getColumnIndex(USER_SIGN)));
			friend.setVirtualMoney(cursor.getLong(cursor.getColumnIndex(USER_MONEY)));
			friend.setSex(cursor.getString(cursor.getColumnIndex(USER_SEX)));
			friends.add(friend);
			
			uinHashSet.add(friend.getUin());
		}
		cursor.close();
		//mDatabase.close();
		return uinHashSet;
	}
	
	/**
	 * 替换好友列表
	 */
	public void replaceFriendList(ArrayList<StockFriend> friends) {
		mDatabase = qiNiuDataBase.getWritableDatabase();
		mDatabase.delete(mTableFriendName,
				 null, null);
		//mDatabase.close();
		for (int i = 0; i < friends.size(); i++) {
			addFriend(friends.get(i));
		}
	}
	
//	//获取联系人ContentValues
//	private ContentValues getValuesFromContactsFriend(StockFriend friend) {
//		ContentValues values = new ContentValues();
//		
//		values.put(QiNiuDataBase.FRIEND_NAME, friend.getName());
//		values.put(QiNiuDataBase.FRIEND_PHONE, friend.getPhone());
//		values.put(QiNiuDataBase.FRIEND_RELATION, friend.getRelation());
//		return values;
//	}
//	
//	//保存联系人表
//	public void saveContacts(ArrayList<StockFriend> friends) {
//		
////		mDatabase.delete(QiNiuDataBase.TABLE_CONTACTS,
////				 null, null);
//		
//		for (int i = 0; i < friends.size(); i++) {
//			StockFriend friend = friends.get(i);
//			addContactsFriend(friend);
//		}
//		
//	}
//	/**
//	 * 修改联系人
//	 * @param friend
//	 * @return
//	 */
//	public Boolean updateContactsFriend(StockFriend friend) {
//		ContentValues values = getValuesFromContactsFriend(friend);
//		
//		mDatabase = qiNiuDataBase.getWritableDatabase();
//		mDatabase.update(QiNiuDataBase.TABLE_CONTACTS,
//				values, QiNiuDataBase.FRIEND_PHONE+"=?", new String[]{friend.getPhone()}); 
//		mDatabase.close();
//		return true;
//				
//	}
//	
//	//添加联系人
//	public void addContactsFriend(StockFriend friend) {
//		mDatabase = qiNiuDataBase.getWritableDatabase();
//		ContentValues values = getValuesFromContactsFriend(friend);
//		mDatabase.insert(QiNiuDataBase.TABLE_CONTACTS,null, values);
//		mDatabase.close();
//	}
//	
//	//取出联系人
//	public void getContactsFriends(ArrayList<StockFriend> friends) {
//		mDatabase = qiNiuDataBase.getWritableDatabase();
//		
//		Cursor cursor = mDatabase.query(QiNiuDataBase.TABLE_CONTACTS,
//					QiNiuDataBase.CONTACTS_ALL_PROJECTION, null, null, null, null, QiNiuDataBase.FRIEND_SORT_ORDER);
//		while (cursor.moveToNext()) {
//			StockFriend friend = new StockFriend();
//			friend.setName(cursor.getString(cursor.getColumnIndex(QiNiuDataBase.FRIEND_NAME))); 
//			friend.setRelation(cursor.getString(cursor.getColumnIndex(QiNiuDataBase.FRIEND_RELATION)));
//			friend.setPhone(cursor.getString(cursor.getColumnIndex(QiNiuDataBase.FRIEND_PHONE)));
//			
//			friends.add(friend);
//		}
//		cursor.close();
//		
//		mDatabase.close();
//	}
//	
//	
}


