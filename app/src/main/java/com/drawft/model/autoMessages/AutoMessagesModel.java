package com.drawft.model.autoMessages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.drawft.db.DbHelper;

public class AutoMessagesModel {
    private SQLiteDatabase db;
    private DbHelper dbHelper;

    public static final String TABLE_AUTO_MESSAGES = "autoMessages";
    public static final String COL_AUTO_MESSAGES_UID = "uid";
    public static final String COL_GROUP_GID = "gid";
    public static final String COL_TYPE = "type";
    public static final String COL_CONTENT = "content";

    public static final String CREATE_AUTO_MESSAGES = "create table "
            + TABLE_AUTO_MESSAGES + "("
            + COL_AUTO_MESSAGES_UID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_GROUP_GID + " TEXT NOT NULL,"
            + COL_TYPE + " TEXT NOT NULL,"
            + COL_CONTENT + " TEXT NOT NULL);";

    private String[] allColumns = {
            COL_AUTO_MESSAGES_UID
            , COL_GROUP_GID
            , COL_TYPE
            , COL_CONTENT
    };

    public Context cx;

    public AutoMessagesModel(Context context) {
        dbHelper = new DbHelper(context);
        cx = context;
    }

    public void close() {
        db.close();
    }


    public static AutoMessagesModel getRW(Context cx) {
        AutoMessagesModel _db = new AutoMessagesModel(cx);
        try {
            _db.db = _db.dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
        }
        return _db;
    }

    public static AutoMessagesModel getR(Context cx) {
        AutoMessagesModel _db = new AutoMessagesModel(cx);
        try {
            _db.db = _db.dbHelper.getReadableDatabase();
        } catch (SQLException ex) {

        }
        return _db;
    }

    public static long iOt(Context cx, String table, String nullColumnHack, ContentValues values) throws SQLException {
        long affected = 0;
        AutoMessagesModel _db = getRW(cx);
        try {
            affected = _db.db.insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException e) {
        }
        closeDBConnection(_db);
        return affected;
    }

    public Cursor getAutoMessages(String grpId, int autoMsgId) {

        AutoMessagesModel _db = getR(cx);
        Cursor cursor = _db.db.query(TABLE_AUTO_MESSAGES, allColumns, COL_GROUP_GID + " = ? AND " + COL_AUTO_MESSAGES_UID + " = ?", new String[]{grpId, autoMsgId + ""}, null, null, null);
        cursor.getCount();
        return cursor;
    }

    public static int getLastInsertedId(Context cx, String grpId, String type) {
        AutoMessagesModel _db = getR(cx);
        String selectQuery = "SELECT max(" + COL_AUTO_MESSAGES_UID + ")," + COL_AUTO_MESSAGES_UID + "," + COL_GROUP_GID + " FROM " + TABLE_AUTO_MESSAGES + " WHERE " + COL_GROUP_GID + "=? AND " + COL_TYPE + "=? LIMIT 1";
        Cursor c = _db.db.rawQuery(selectQuery, new String[]{grpId, type});
        int autoMessageId = -1;
        if (c.moveToFirst()) {
            autoMessageId = c.getInt(c.getColumnIndexOrThrow(COL_AUTO_MESSAGES_UID));
        }
        c.close();
        closeDBConnection(_db);
        return autoMessageId;
    }

    public static void closeDBConnection(AutoMessagesModel _db) {
        try {
            if (_db != null && _db.db != null) {
                _db.db.close();
                _db.dbHelper.close();
                if (_db.cx != null) {
                    _db.cx = null;
                }
            }
        } catch (Exception e) {

        }
    }
}
