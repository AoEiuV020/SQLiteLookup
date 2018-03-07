package com.xdroid.sqlitelookup.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.xdroid.sqlitelookup.model.DbModel;

import com.xdroid.utils.sqlite.DaoFactory;
import com.xdroid.utils.sqlite.DbSqlite;
import com.xdroid.utils.sqlite.IBaseDao;

public class AppContext extends Application {

    public static final String DB_NAME = "sqlitelookup.db";
    private static final int DB_VERSION = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        initDb();
    }

    private void initDb() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        int dbVersion = db.getVersion();
        if (dbVersion == 0) {
            db.setVersion(DB_VERSION);
            onCreateTables(db);
        } else if (dbVersion < DB_VERSION) {
            db.setVersion(DB_VERSION);
            onUpdateTables(db);
        }
    }

    private void onCreateTables(SQLiteDatabase db) {
        DbSqlite dbSqlite = new DbSqlite(this, db);
        IBaseDao<DbModel> dbDao = DaoFactory.createGenericDao(dbSqlite, DbModel.class);
        dbDao.createTable();
        dbSqlite.closeDB();
    }

    private void onUpdateTables(SQLiteDatabase db) {
        DbSqlite dbSqlite = new DbSqlite(this, db);
        IBaseDao<DbModel> dbDao = DaoFactory.createGenericDao(dbSqlite, DbModel.class);
        dbDao.updateTable();
        dbSqlite.closeDB();
    }
}
