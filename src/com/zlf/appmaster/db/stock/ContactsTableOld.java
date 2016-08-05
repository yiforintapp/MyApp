package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * 联系人数据表--与服务器一致
 * Created by yushian on 15-4-2.
 */
public class ContactsTableOld extends SyncBaseTable{

    public static class ContactType {

        /**
         * 用户联系人类型：关注用户
         */
        public static int CONTACT_TYPE_UIN = 1;

        /**
         * 用户联系人类型：关注组合
         */
        public static int CONTACT_TYPE_PORTFOLIO = 2;

        /**
         * 用户联系人类型：创建组合
         */
        public static int CONTACT_TYPE_CPORTFOLIO = 3;

        /**
         * 用户联系人类型：创建群组
         */
        public static int CONTACT_TYPE_CCHATROOM = 4;

        /**
         * 用户联系人类型：加入群组
         */
        public static int CONTACT_TYPE_CHATROOM = 5;
    }

    public static final String TABLE_NAME = "contacts_old";


    public static final String COLNAME_IID = "iid";//(用户id、组合id、群组id)
    public static final String COLNAME_TYPE = "type";//类型（用户、组合、群组）

    /**
     * 数据库操作SQL
     */
    public static final String OTHER_STRUCTURE =
                    ","
                    + COLNAME_IID +     "	INT,"
                    + COLNAME_TYPE +    "   INT";

    public static final String WHERE_ROW = COLNAME_IID + " = ? AND " + COLNAME_TYPE + " = ?";



    public ContactsTableOld(Context context) {
        super(context, TABLE_NAME, OTHER_STRUCTURE);
    }


    public boolean isExist(long iid,long type){
        Cursor c = db.query(TABLE_NAME, null, WHERE_ROW,
                new String[] { String.valueOf(iid), String.valueOf(type) }, null, null, null);
        boolean ret = c.moveToFirst();
        c.close();
        return ret;
    }

    /**
     * 插入关注
     * @param id
     */
    public void followCombination(long id, boolean isFollow){
        synchronized (m_dbHelper){
            db = m_dbHelper.getWritableDatabase();
            long currentTime = System.currentTimeMillis();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLNAME_IID,id);
            contentValues.put(COLNAME_TYPE, ContactType.CONTACT_TYPE_PORTFOLIO);
            contentValues.put(COLNAME_LOCAL_SYNC_FLAG,BaseDB.SYNC_FLAG_NO);

            if (isFollow){
                contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_NORMAL);
            }else {
                contentValues.put(COLNAME_DELFLAG, DELETE_FLAG_DELETE);
            }

            if (isExist(id, ContactType.CONTACT_TYPE_PORTFOLIO)){
                contentValues.put(COLNAME_UPDATE_TIME, currentTime);
                //更新
                db.update(TABLE_NAME,contentValues, WHERE_ROW,
                        new String[]{String.valueOf(id), String.valueOf(ContactType.CONTACT_TYPE_PORTFOLIO)});
            }else {
                contentValues.put(COLNAME_CREATE_TIME, currentTime);
                //插入
                db.insert(TABLE_NAME, null, contentValues);
            }

        }
    }


    /**
     * 根据类型与删除标记 获取待提交的信息
     * @param contactType
     * @param isDelete
     * @return Cursor
     */
    public Cursor getCommitCursor(int contactType, boolean isDelete){
        String where_row = WHERE_COMMIT_ADD_ROW
                + " AND " + COLNAME_TYPE + " = " + contactType
                + " AND " + COLNAME_DELFLAG ;
        if (isDelete){
            where_row += " = "+DELETE_FLAG_DELETE;
        }else {
            where_row += " = "+DELETE_FLAG_NORMAL;
        }

        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME,
                    new String[]{COLNAME_IID, COLNAME_TYPE, COLNAME_DELFLAG},
                    where_row, null, null, null, null);
            return c;
        }
    }

    /**
     * 根据类型与删除标记 获取待提交的信息
     * @param contactType
     * @param isDelete
     * @return JSONArray
     */
    public JSONArray getCommitJsonArray(int contactType, boolean isDelete){
        Cursor cursor = getCommitCursor(contactType,isDelete);
        JSONArray jsonNewArray = new JSONArray();
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("IId", cursor.getInt(0));
                    jsonNewArray.put(jsonObject);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return jsonNewArray;
    }


    /**
     * 是否关注组合
     * 自己创建的默认是跟随的
     */
    public boolean isFollowed(long id,long adminUin,long uin){
        if (uin != 0 && adminUin == uin){
            return true;
        }

        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME, null, WHERE_ROW,
                    new String[]{String.valueOf(id), String.valueOf(ContactType.CONTACT_TYPE_PORTFOLIO)},
                    null, null, null);
            if (c.moveToFirst()) {
                if (c.getInt(c.getColumnIndex(COLNAME_DELFLAG)) != DELETE_FLAG_DELETE) {
                    c.close();
                    return true;
                }
            }
            c.close();
            return false;
        }
    }

    /**
     * 根据类型 获取联系人的信息 获取全部
     * @param contactType
     * @return Cursor
     */
//    public Cursor getContactCursor(int contactType){
//
//        String where_row =
//                COLNAME_TYPE + " = " + contactType
//                        + " AND " + COLNAME_DELFLAG + " = " + DELETE_FLAG_NORMAL;
//
//        synchronized (m_dbHelper) {
//            db = m_dbHelper.getReadableDatabase();
//            Cursor c = db.query(TABLE_NAME,
//                    new String[]{COLNAME_IID},
//                    where_row, null, null, null, ORDER_BY_DESC_TIME);
//            return c;
//        }
//    }


    public String getIdsFromType(int contactType){
        String where_row =
                COLNAME_TYPE + " = " + contactType
                        + " AND " + COLNAME_DELFLAG + " = " + DELETE_FLAG_NORMAL;

        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_NAME,
                    new String[]{COLNAME_IID},
                    where_row, null, null, null, ORDER_BY_DESC_TIME);
            if (c != null) {
                StringBuffer ids = new StringBuffer();
                while (c.moveToNext()) {
                    ids.append(c.getLong(0)+",");
                }
                return ids.toString();
            }
        }
        return "";
    }

    /**
     * 判断是否有创建的组合
     * @return
     */
    public boolean hasCreateCombination(){
        Cursor c = getContactCursor(ContactsTableOld.ContactType.CONTACT_TYPE_CPORTFOLIO,
                0,10);
        return c.moveToNext();
    }

    /**
     * 根据类型 获取联系人的信息 指定数量
     * @param contactType
     * @param startIndex
     * @param num
     * @return
     */
    public Cursor getContactCursor(int contactType, int startIndex, int num){
        String limitString = startIndex + "," + num;//前面是偏移，后面是数量
        String where_row =
                COLNAME_TYPE + " = " + contactType
                + " AND " + COLNAME_DELFLAG + " = " + DELETE_FLAG_NORMAL;

        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(false,TABLE_NAME,
                    new String[]{COLNAME_IID},
                    where_row, null, null, null, ORDER_BY_DESC_TIME,limitString);
            return c;
        }
    }

    /**
     * 获取我的组合数据(ID组)
     */
    public HashSet<Long> getMyPortfolioDataFromSql(int startIndex, int num){
        String limitString = startIndex + "," + num;//前面是偏移，后面是数量
        String where_row =
                COLNAME_TYPE + " = " + ContactsTableOld.ContactType.CONTACT_TYPE_CPORTFOLIO
                        + " AND " + COLNAME_DELFLAG + " = " + DELETE_FLAG_NORMAL;

        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor c = db.query(false,TABLE_NAME,
                    new String[]{COLNAME_IID},
                    where_row, null, null, null, ORDER_BY_DESC_TIME,limitString);

            HashSet<Long> ids = new HashSet<Long>();
            while (c.moveToNext()) {
                ids.add(c.getLong(0));
            }
            c.close();
            db.close();

            return ids;
        }

    }


    public void saveItem(ContentValues contentValues){
        long id = contentValues.getAsLong(COLNAME_IID);
        int deleteFlag = contentValues.getAsInteger(COLNAME_DELFLAG);

        if (isExist(id, ContactType.CONTACT_TYPE_PORTFOLIO)){
            if (deleteFlag == DELETE_FLAG_DELETE){
                //删除数据
                db.delete(TABLE_NAME,WHERE_ROW,
                        new String[]{String.valueOf(id), String.valueOf(ContactType.CONTACT_TYPE_PORTFOLIO)});
            }else {
                //更新
                db.update(TABLE_NAME, contentValues, WHERE_ROW,
                        new String[]{String.valueOf(id), String.valueOf(ContactType.CONTACT_TYPE_PORTFOLIO)});
            }
        }else {
            //插入
            db.insert(TABLE_NAME,null,contentValues);
        }
    }

    //保存同步数据
    public void syncData(JSONArray syncData){
        try {
            if (syncData != null) {
                synchronized (m_dbHelper) {
                    db = m_dbHelper.getWritableDatabase();
                    db.beginTransaction();
                    for (int i = 0; i < syncData.length(); i++) {
                        JSONObject itemObject = syncData.getJSONObject(i);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(COLNAME_IID, itemObject.optInt("IID"));
                        contentValues.put(COLNAME_SEQ, itemObject.optInt("Seq"));
                        contentValues.put(COLNAME_UPDATE_TIME, itemObject.optInt("Utime"));
                        contentValues.put(COLNAME_CREATE_TIME, itemObject.optInt("Ctime"));
                        contentValues.put(COLNAME_DELFLAG, itemObject.optInt("DelFlag"));
                        contentValues.put(COLNAME_TYPE, itemObject.optInt("Type"));
                        contentValues.put(COLNAME_LOCAL_SYNC_FLAG, String.valueOf(BaseDB.SYNC_FLAG_YES));
                        saveItem(contentValues);
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.close();
                }

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
