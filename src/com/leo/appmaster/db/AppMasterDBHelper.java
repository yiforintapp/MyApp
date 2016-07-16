
package com.leo.appmaster.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.leo.appmaster.Constants;

public class AppMasterDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "appmaster.db";
    // 3.2 -> 9
    // 3.3 -> 10
    public static final int DB_VERSION = 10;

    private static final String CREATE_DOWNLOAD_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Constants.TABLE_DOWNLOAD
            + " ( "
            + Constants.ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Constants.COLUMN_DOWNLOAD_FILE_NAME
            + " TEXT,"
            + Constants.COLUMN_DOWNLOAD_DESTINATION
            + " TEXT,"
            + Constants.COLUMN_DOWNLOAD_URL
            + " TEXT,"
            + Constants.COLUMN_DOWNLOAD_MIME_TYPE
            + " TEXT,"
            + Constants.COLUMN_DOWNLOAD_TOTAL_SIZE
            + " INTEGER NOT NULL DEFAULT 0,"
            + Constants.COLUMN_DOWNLOAD_CURRENT_SIZE
            + " INTEGER,"
            + Constants.COLUMN_DOWNLOAD_STATUS
            + " INTEGER,"
            + Constants.COLUMN_DOWNLOAD_DATE
            + " INTEGER,"
            + Constants.COLUMN_DOWNLOAD_TITLE
            + " TEXT, "
            + Constants.COLUMN_DOWNLOAD_DESCRIPTION
            + " TEXT, "
            + Constants.COLUMN_DOWNLOAD_WIFIONLY
            + " INTEGER NOT NULL DEFAULT -1" + ");";
    private static final String CREATE_MESSAGE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Constants.TABLE_MESSAGE
            + " ( "
            + Constants.COLUMN_MESSAGE_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Constants.COLUMN_MESSAGE_PHONE_NUMBER
            + " TEXT,"
            + Constants.COLUMN_MESSAGE_CONTACT_NAME
            + " TEXT,"
            + Constants.COLUMN_MESSAGE_BODY
            + " TEXT,"
            + Constants.COLUMN_MESSAGE_DATE
            + " TEXT,"
            + Constants.COLUMN_MESSAGE_THREAD_ID
            + " INTEGER,"
            + Constants.COLUMN_MESSAGE_PROTCOL
            + " TEXT,"
            + Constants.COLUMN_MESSAGE_IS_READ
            + " INTEGER,"
            + Constants.COLUMN_MESSAGE_TYPE
            + " INTEGER " + ");";
    private static final String CREATE_CONTACT_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Constants.TABLE_CONTACT
            + " ( "
            + Constants.COLUMN_CONTACT_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Constants.COLUMN_CONTACT_NAME
            + " TEXT,"
            + Constants.COLUMN_PHONE_NUMBER
            + " TEXT,"
            + Constants.COLUMN_PHONE_ANSWER_TYPE
            + " INTEGER,"
            + Constants.COLUMN_ICON
            + " BLOB"
            + ");";
    private static final String CREATE_CALL_LOG_TABLE = "CREATE TABLE IF NOT EXISTS "
            + Constants.TABLE_CALLLOG
            + " ( "
            + Constants.COLUMN_CALL_LOG_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Constants.COLUMN_CALL_LOG_PHONE_NUMBER
            + " TEXT,"
            + Constants.COLUMN_CALL_LOG_CONTACT_NAME
            + " TEXT,"
            + Constants.COLUMN_CALL_LOG_DATE
            + " TEXT,"
            + Constants.COLUMN_CALL_LOG_TYPE
            + " INTEGER,"
            + Constants.COLUMN_CALL_LOG_DURATION
            + " INTEGER,"
            + Constants.COLUMN_CALL_LOG_IS_READ
            + " INTEGER"
            + ");";

    private static final String CREATE_LOCK_MODE = "CREATE TABLE IF NOT EXISTS "
            + Constants.TABLE_LOCK_MODE
            + " ( "
            + Constants.COLUMN_LOCK_MODE_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Constants.COLUMN_LOCK_MODE_NAME
            + " TEXT,"
            + Constants.COLUMN_LOCKED_LIST
            + " TEXT,"
            + Constants.COLUMN_MODE_ICON
            + " BLOB,"
            + Constants.COLUMN_DEFAULT_MODE_FLAG
            + " INTEGER,"
            + Constants.COLUMN_CURRENT_USED
            + " INTEGER,"
            + Constants.COLUMN_OPENED
            + " INTEGER"
            + ");";

    private static final String CREATE_TIME_LOCK = "CREATE TABLE IF NOT EXISTS "
            + Constants.TABLE_TIME_LOCK
            + " ( "
            + Constants.COLUMN_TIME_LOCK_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Constants.COLUMN_TIME_LOCK_NAME
            + " TEXT,"
            + Constants.COLUMN_LOCK_MODE
            + " INTEGER,"
            + Constants.COLUMN_LOCK_MODE_NAME
            + " TEXT,"
            + Constants.COLUMN_LOCK_TIME
            + " TEXT,"
            + Constants.COLUMN_REPREAT_MODE
            + " INTEGER,"
            + Constants.COLUMN_TIME_LOCK_USING
            + " INTEGER"
            + ");";

    private static final String CREATE_LOCATION_LOCK = "CREATE TABLE IF NOT EXISTS "
            + Constants.TABLE_LOCATION_LOCK
            + " ( "
            + Constants.COLUMN_LOCATION_LOCK_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Constants.COLUMN_LOCATION_LOCK_NAME
            + " TEXT,"
            + Constants.COLUMN_WIFF_NAME
            + " TEXT,"
            + Constants.COLUMN_ENTRANCE_MODE
            + " INTEGER,"
            + Constants.COLUMN_ENTRANCE_MODE_NAME
            + " TEXT,"
            + Constants.COLUMN_QUITE_MODE
            + " INTEGER,"
            + Constants.COLUMN_QUITE_MODE_NAME
            + " TEXT,"
            + Constants.COLUMN_LOCATION_LOCK_USING
            + " INTEGER"
            + ");";

    private static final String[] TABLES = {
            "com.leo.appmaster.db.MsgCenterTable",
            "com.leo.appmaster.db.PreferenceTable",
            "com.leo.appmaster.db.InstalledAppTable",
            "com.leo.appmaster.db.LockRecommentTable",
            "com.leo.appmaster.db.BlacklistTab"
    };

    private static AppMasterDBHelper sInstance;

    public static synchronized AppMasterDBHelper getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new AppMasterDBHelper(ctx);
        }

        return sInstance;
    }

    private AppMasterDBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS feedback ("
                + "_id INTEGER PRIMARY KEY," + "email TEXT," + "content TEXT,"
                + "category TEXT," + "submit_date TEXT" + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS applist_business ("
                + "_id INTEGER PRIMARY KEY," + "lebal TEXT,"
                + "package_name TEXT," + "icon_url TEXT,"
                + "download_url TEXT," + "icon BLOB," + "container_id INTEGER,"
                + "rating TEXT," + "download_count TEXT," + "desc TEXT,"
                + "gp_priority INTEGER," + "gp_url TEXT," + "app_size INTEGER,"
                + "icon_status INTEGER" + ");");

        db.execSQL(CREATE_DOWNLOAD_TABLE);

        // day flow db
        db.execSQL("CREATE TABLE IF NOT EXISTS countflow ("
                + "_id INTEGER PRIMARY KEY," + "daytime varchar," + "daymemory float,"
                + "monthmemory float," + "year integer,"
                + "month integer," + "day integer" + ");");
        // app flow db
        db.execSQL("CREATE TABLE IF NOT EXISTS countappflow ("
                + "_id INTEGER PRIMARY KEY," + "daytime varchar," + "year integer,"
                + "month integer," + "uid integer," + "monthsend float," + "monthrev float,"
                + "monthall float"
                + ");");
        db.execSQL(CREATE_DOWNLOAD_TABLE);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Constants.TABLE_IMAGE_HIDE + " ("
                + "_id INTEGER PRIMARY KEY," + "image_dir TEXT," + "image_path TEXT" + ");");

        /*
         * PrivacyContact
         */
        db.execSQL(CREATE_CONTACT_TABLE);
        db.execSQL(CREATE_CALL_LOG_TABLE);
        db.execSQL(CREATE_MESSAGE_TABLE);

        /*
         * Lock Mode
         */
        db.execSQL(CREATE_LOCK_MODE);
        db.execSQL(CREATE_TIME_LOCK);
        db.execSQL(CREATE_LOCATION_LOCK);

        for (String table : TABLES) {
            try {
                Class<?> clazz = Class.forName(table);
                Object object = clazz.newInstance();

                if (object instanceof BaseTable) {
                    final BaseTable t = (BaseTable) object;
                    t.createTable(db);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        execSQLForFilter(db);

    }

    /*骚扰拦截*/
    private void execSQLForFilter(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS applist_business ("
                    + "_id INTEGER PRIMARY KEY," + "lebal TEXT,"
                    + "package_name TEXT," + "icon_url TEXT,"
                    + "download_url TEXT," + "icon BLOB,"
                    + "container_id INTEGER," + "gp_priority INTEGER,"
                    + "gp_url TEXT," + "app_size INTEGER,"
                    + "icon_status INTEGER" + ");");

            db.execSQL(CREATE_DOWNLOAD_TABLE);
        } else if (newVersion == 3) {
            db.execSQL("DROP TABLE IF EXISTS " + "applist_business");
            db.execSQL("CREATE TABLE IF NOT EXISTS applist_business ("
                    + "_id INTEGER PRIMARY KEY," + "lebal TEXT,"
                    + "package_name TEXT," + "icon_url TEXT,"
                    + "download_url TEXT," + "icon BLOB,"
                    + "container_id INTEGER," + "rating TEXT,"
                    + "download_count TEXT," + "desc TEXT,"
                    + "gp_priority INTEGER," + "gp_url TEXT,"
                    + "app_size INTEGER," + "icon_status INTEGER" + ");");

        } else if (newVersion == 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + Constants.TABLE_IMAGE_HIDE + " ("
                    + "_id INTEGER PRIMARY KEY," + "image_dir TEXT," + "image_path TEXT" + ");");

        } else if (newVersion == 5) {
            // flow db
            db.execSQL("CREATE TABLE IF NOT EXISTS countflow ("
                    + "_id INTEGER PRIMARY KEY," + "daytime varchar," + "daymemory float,"
                    + "monthmemory float," + "year integer,"
                    + "month integer," + "day integer" + ");");
            // app flow db
            db.execSQL("CREATE TABLE IF NOT EXISTS countappflow ("
                    + "_id INTEGER PRIMARY KEY," + "daytime varchar," + "year integer,"
                    + "month integer," + "uid integer," + "monthsend float," + "monthrev float,"
                    + "monthall float"
                    + ");");
            /*
             * PrivacyContact
             */
            db.execSQL(CREATE_CONTACT_TABLE);
            db.execSQL(CREATE_CALL_LOG_TABLE);
            db.execSQL(CREATE_MESSAGE_TABLE);

            /*
             * Lock Mode
             */
            db.execSQL(CREATE_LOCK_MODE);
            db.execSQL(CREATE_TIME_LOCK);
            db.execSQL(CREATE_LOCATION_LOCK);
        } else if (newVersion == 6) {
            db.execSQL("DROP TABLE IF EXISTS " + "applist_business");
            db.execSQL("CREATE TABLE IF NOT EXISTS applist_business ("
                    + "_id INTEGER PRIMARY KEY," + "lebal TEXT,"
                    + "package_name TEXT," + "icon_url TEXT,"
                    + "download_url TEXT," + "icon BLOB,"
                    + "container_id INTEGER," + "rating TEXT,"
                    + "download_count TEXT," + "desc TEXT,"
                    + "gp_priority INTEGER," + "gp_url TEXT,"
                    + "app_size INTEGER," + "icon_status INTEGER" + ");");
        } else if (newVersion > 8) {
            execSQLForFilter(db);
        }

        for (String table : TABLES) {
            try {
                Class<?> clazz = Class.forName(table);
                Object object = clazz.newInstance();

                if (object instanceof BaseTable) {
                    final BaseTable t = (BaseTable) object;
                    t.upgradeTable(db, oldVersion, newVersion);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        try {
            return super.getReadableDatabase();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        try {
            return super.getWritableDatabase();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }
}
