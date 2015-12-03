package com.drawft.model.drawfts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.drawft.db.DbHelper;
import com.drawft.model.autoMessages.AutoMessagesModel;

import java.util.ArrayList;


public class DrawftModel {
    private SQLiteDatabase db;
    private DbHelper dbHelper;
    private Context cx;

    public static final String TABLE_DRAWFTS = "drawfts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DRAWFT_SENT_BY = "drawft_sent_by";
    public static final String COLUMN_DRAWFT_FILE_NAME = "drawft_file_name";
    public static final String COLUMN_DRAWFT_SENT_AT = "drawft_sent_at";
    public static final String COLUMN_DRAWFT_DIMENSIONS = "drawft_dimensions";
    public static final String COLUMN_GROUP_ID = "drawft_group_id";
    public static final String COL_AUTO_MESSAGES_UID = "uid";
    public static final String COL_IS_SENT = "is_sent";


    public static final String CREATE_DRAWFTS = "create table "
            + TABLE_DRAWFTS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DRAWFT_SENT_BY + " text null,"
            + COLUMN_DRAWFT_FILE_NAME + " text null,"
            + COLUMN_DRAWFT_SENT_AT + " text null,"
            + COLUMN_GROUP_ID + " text not null,"
            + COLUMN_DRAWFT_DIMENSIONS + " text null,"
            + COL_AUTO_MESSAGES_UID + " INTEGER null,"
            + COL_IS_SENT + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + COL_AUTO_MESSAGES_UID + ")REFERENCES " + AutoMessagesModel.TABLE_AUTO_MESSAGES + "(uid));";

    private String[] allColumns = {
            COLUMN_ID
            , COLUMN_DRAWFT_SENT_BY
            , COLUMN_DRAWFT_FILE_NAME
            , COLUMN_DRAWFT_SENT_AT
            , COLUMN_GROUP_ID
            , COLUMN_DRAWFT_DIMENSIONS
            , COL_AUTO_MESSAGES_UID
            , COL_IS_SENT
    };

    public DrawftModel(Context context) {
        dbHelper = new DbHelper(context);
        cx = context;
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    /*public void close() {
        try {
            dbHelper.close();
        } catch (Exception e) {

        }

    }*/

    public static DrawftModel getR(Context cx) {
        DrawftModel _db = new DrawftModel(cx);
        try {
            _db.db = _db.dbHelper.getReadableDatabase();
        } catch (SQLiteException ex) {

        }
        return _db;
    }

    public Boolean addNewRecord(String gid, String sentBy, String fileName, String sentAt, String dim, int autoMsgId, int isSent) {

        ContentValues values = new ContentValues();
        values.put(COLUMN_DRAWFT_SENT_BY, sentBy);
        values.put(COLUMN_GROUP_ID, gid);
        values.put(COLUMN_DRAWFT_FILE_NAME, fileName);
        values.put(COLUMN_DRAWFT_SENT_AT, sentAt);
        values.put(COLUMN_DRAWFT_DIMENSIONS, dim);
        values.put(COL_AUTO_MESSAGES_UID, autoMsgId);
        values.put(COL_IS_SENT, isSent);
        DrawftModel _db = getRW(cx);

        long id = _db.db.insert(TABLE_DRAWFTS, null,
                values);
        closeDBConnection(_db);
        return (id != -1);

    }

    public ArrayList<String> getOfflineDrawfts(String gid) {
        ArrayList<String> offlineList = new ArrayList<String>();
        try {
            DrawftModel _db = getR(cx);
            String selectQuery = "SELECT " + COLUMN_DRAWFT_FILE_NAME + " FROM " + TABLE_DRAWFTS + " WHERE " + COLUMN_GROUP_ID + "=? AND " + COL_IS_SENT + "=0";
            Cursor cursor = _db.db.rawQuery(selectQuery, new String[]{gid});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                offlineList.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DRAWFT_FILE_NAME)));
                cursor.moveToNext();
            }
            cursor.close();
            closeDBConnection(_db);

        } catch (Exception e) {

        }

        return offlineList;
    }

    public static DrawftModel getRW(Context cx) {
        DrawftModel _db = new DrawftModel(cx);
        try {
            _db.db = _db.dbHelper.getWritableDatabase();
        } catch (SQLException ex) {

        }
        return _db;
    }

    public ArrayList<DrawftBean> getGroupDrawfts(String id) throws SQLException {
        ArrayList<DrawftBean> drawfts = new ArrayList<>();
        Cursor cursor = db.query(TABLE_DRAWFTS, allColumns, COLUMN_GROUP_ID + " = ? AND " + COL_AUTO_MESSAGES_UID + " = ?", new String[]{id, 0 + ""}, null, null, COLUMN_ID + " DESC", String.valueOf(3));
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DrawftBean d = cursorToDrawftBean(cursor);
            drawfts.add(d);
            cursor.moveToNext();
        }
        cursor.close();
        return drawfts;

    }

    public ArrayList<DrawftBean> getUserRecords(String groupId, int limit, int offset) throws SQLException {
        ArrayList<DrawftBean> records = new ArrayList<DrawftBean>();
        Cursor cursor = null;
        try {
            DrawftModel _db = getR(cx);
            cursor = _db.db.query(TABLE_DRAWFTS,
                    allColumns, COLUMN_GROUP_ID + " = '" + groupId + "'", null, null, null, COLUMN_ID + " DESC", String.valueOf(offset) + "," + String.valueOf(limit));
            if (offset == 0) {
                cursor.moveToLast();
                while (!cursor.isBeforeFirst()) {
                    DrawftBean comment = cursorToDrawftBean(cursor);
                    records.add(comment);
                    cursor.moveToPrevious();
                }
            } else {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    DrawftBean comment = cursorToDrawftBean(cursor);
                    records.add(comment);
                    cursor.moveToNext();
                }
            }
            cursor.close();
            closeDBConnection(_db);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return records;
    }

    public boolean checkDrawftExistence(String groupId, String drawftId) throws SQLException {
        String selectQuery = "SELECT " + COLUMN_ID + " FROM " + TABLE_DRAWFTS + " WHERE " + COLUMN_GROUP_ID + "=? AND " + COLUMN_DRAWFT_FILE_NAME + "=?";
        Cursor c = db.rawQuery(selectQuery, new String[]{groupId, drawftId});
        boolean exists = false;
        if (c.moveToFirst()) {
            exists = true;
        }
        c.close();
        return exists;
    }

    public String getLastSyncedDrawft(String groupId) throws SQLException {
        DrawftModel _db = getR(cx);
        String selectQuery = "SELECT max(" + COLUMN_ID + ")," + COLUMN_ID + "," + COLUMN_DRAWFT_SENT_AT + " FROM " + TABLE_DRAWFTS + " WHERE " + COLUMN_GROUP_ID + "=? AND " + COLUMN_DRAWFT_SENT_AT + "!=? LIMIT 1";
        Cursor c = _db.db.rawQuery(selectQuery, new String[]{groupId, "now"});
        String lastSynced = "";
        if (c.moveToFirst()) {
            lastSynced = c.getString(c.getColumnIndexOrThrow(COLUMN_DRAWFT_SENT_AT));
            if (lastSynced == null) {
                lastSynced = "";
            }
        }
        c.close();
        closeDBConnection(_db);
        return lastSynced;
    }

    private DrawftBean cursorToDrawftBean(Cursor cursor) {
        DrawftBean drawftBean = new DrawftBean();
        //drawftBean.setId(cursor.getLong(0));
        drawftBean.setId(cursor.getColumnIndexOrThrow(COLUMN_ID));
        drawftBean.setSentBy(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DRAWFT_SENT_BY)));
        drawftBean.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DRAWFT_FILE_NAME)));
        drawftBean.setSentAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DRAWFT_SENT_AT)));
        drawftBean.setDimensions(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DRAWFT_DIMENSIONS)));
        drawftBean.setAutoMessageId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_AUTO_MESSAGES_UID)));
        drawftBean.setIsSent(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SENT)));
        return drawftBean;
    }


    public void delDrawfts(String itemId) {
        getRW(cx).db.delete(DrawftModel.TABLE_DRAWFTS, DrawftModel.COLUMN_GROUP_ID + "='" + itemId + "'", null);
    }

    public static int uOt(Context cx, String table, ContentValues values, String whereClause, String[] whereArgs) {
        int affected = 0;
        DrawftModel _db = getRW(cx);
        try {
            affected = _db.db.update(table, values, whereClause, whereArgs);
        } catch (SQLException e) {
        }
        closeDBConnection(_db);
        return affected;
    }

    public static void closeDBConnection(DrawftModel _db) {
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
