package com.zlf.appmaster.db.stock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yushian on 15-4-15.
 */
public class UserDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "UserDBHelper";

    public static final String DB_NAME = "User.db";
    public static final int DB_VERSION = 4;

    private static UserDBHelper mInstance;

    public UserDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * 获取单例 防止泄露
     * @param context
     * @return
     */
    public static UserDBHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new UserDBHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion){
            if (oldVersion == 3) {
                db.execSQL("ALTER TABLE " + UserTable.TABLE_NAME+" ADD " + UserTable.SETTING_JOB_NAME + " TEXT");
                db.execSQL("ALTER TABLE " + UserTable.TABLE_NAME+" ADD " + UserTable.SETTING_JOB_CARD + " TEXT");
                db.execSQL("ALTER TABLE " + UserTable.TABLE_NAME+" ADD " + UserTable.SETTING_COMPANY_NAME + " TEXT");
            }
            else if (oldVersion < 3){
                db.execSQL("DROP TABLE " + UserTable.TABLE_NAME);
            }
        }

    }
}
