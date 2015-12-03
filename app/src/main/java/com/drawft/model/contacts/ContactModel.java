package com.drawft.model.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.drawft.db.DbHelper;


public class ContactModel {
    private SQLiteDatabase db;
    private DbHelper dbHelper;

    public static final String TABLE_CONTACTS = "contacts";
    public static final String COL_CONTACT_NAME = "name";
    public static final String COL_CONTACT_NUMBER = "number";
    public static final String COL_BLOCKED = "blocked";
    public static final String COL_BLOCKED_AT = "blocked_at";
    public static final String COL_NOTIFICATION = "notification";
    public static final String COL_NOTIFICATION_TIME = "notification_time";
    public static final String COL_APPUSING = "appUsing";


    public static final String CREATE_CONTACTS = "create table "
            + TABLE_CONTACTS + "("
            + COL_CONTACT_NUMBER + " TEXT  PRIMARY KEY,"
            + COL_CONTACT_NAME + " text null,"
            + COL_NOTIFICATION + " INTEGER DEFAULT 0,"
            + COL_NOTIFICATION_TIME + " text DEFAULT 0,"
            + COL_APPUSING + " INTEGER DEFAULT 0,"
            + COL_BLOCKED_AT + " text DEFAULT 0,"
            + COL_BLOCKED + " INTEGER DEFAULT 0);";

    private String[] allColumns = {
            COL_CONTACT_NAME
            , COL_CONTACT_NUMBER
            , COL_NOTIFICATION
            , COL_NOTIFICATION_TIME
            , COL_APPUSING
            , COL_BLOCKED
            , COL_BLOCKED_AT
    };

    public Context cx;

    public ContactModel(Context context) {
        dbHelper = new DbHelper(context);
        cx = context;
    }

    /*public void close() {
        try {
            dbHelper.close();
        } catch (Exception e) {

        }
    }*/


    public static ContactModel getRW(Context cx) {
        ContactModel _db = new ContactModel(cx);
        try {
            _db.db = _db.dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
        }
        return _db;
    }

    public static ContactModel getR(Context cx) {
        ContactModel _db = new ContactModel(cx);
        try {
            _db.db = _db.dbHelper.getReadableDatabase();
        } catch (SQLException ex) {

        }
        return _db;
    }

    public static long iOt(Context cx, String table, String nullColumnHack, ContentValues values) throws SQLException {
        long affected = 0;
        ContactModel _db = getRW(cx);
        try {
            affected = _db.db.insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException e) {
        }
        closeDBConnection(_db);
        return affected;
    }



    public static int uOt(Context cx, String table, ContentValues values, String whereClause, String[] whereArgs) {
        int affected = 0;
        ContactModel _db = getRW(cx);
        try {
            affected = _db.db.update(table, values, whereClause, whereArgs);
        } catch (SQLException e) {
        }
        closeDBConnection(_db);
        return affected;
    }

    public static void clearContacts(Context cx) {
        getRW(cx).db.delete(TABLE_CONTACTS, null, null);
    }

    public Cursor getContacts() {
        Cursor cursor = db.query(TABLE_CONTACTS, allColumns, null, null, null, null, COL_CONTACT_NAME + " COLLATE NOCASE;", null);
        return cursor;
    }

    public Cursor getAppUsingContacts() {
        try {
            ContactModel _db = getR(cx);
            String selectQuery = "SELECT " + COL_CONTACT_NUMBER + "," + COL_CONTACT_NAME + " FROM " + TABLE_CONTACTS + " WHERE " + COL_APPUSING + "=1";
            return _db.db.rawQuery(selectQuery, new String[]{});
        } catch (Exception e) {
            return null;
        }

    }

    public static int getTotalSavedContacts(Context ctx) {
        int cnt = 0;
        try {

            ContactModel _db = getR(ctx);
            String selectQuery = "SELECT COUNT(*) as total_contacts FROM " + TABLE_CONTACTS;
            Cursor c = _db.db.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                cnt = c.getInt(c.getColumnIndexOrThrow("total_contacts"));
            }
            return cnt;
        } catch (Exception e) {
            return cnt;
        }

    }


    public String getContactName(String contactNumber) {
        ContactModel _db = getR(cx);
        String selectQuery = "SELECT " + COL_CONTACT_NAME + " FROM " + TABLE_CONTACTS + " WHERE " + COL_CONTACT_NUMBER + "=? LIMIT 1";
        Cursor c = _db.db.rawQuery(selectQuery, new String[]{contactNumber});
        String conName = null;
        if (c.moveToFirst()) {
            conName = c.getString(c.getColumnIndexOrThrow(COL_CONTACT_NAME));
        }
        c.close();
        closeDBConnection(_db);
        return conName;
    }

    public int getAppUsing(String contactNumber) {
        ContactModel _db = getR(cx);
        String selectQuery = "SELECT " + COL_APPUSING + " FROM " + TABLE_CONTACTS + " WHERE " + COL_CONTACT_NUMBER + "=?";
        Cursor c = _db.db.rawQuery(selectQuery, new String[]{contactNumber});
        int appUsing = -1;
        if (c.moveToFirst()) {
            appUsing = c.getInt(c.getColumnIndexOrThrow(COL_APPUSING));
        }
        c.close();
        closeDBConnection(_db);
        return appUsing;
    }

    public String searchNumber(String contactNumber) {
        ContactModel _db = getR(cx);
        String selectQuery = "SELECT " + COL_CONTACT_NUMBER + " FROM " + TABLE_CONTACTS + " WHERE " + COL_CONTACT_NUMBER + "=? LIMIT 1";
        Cursor c = _db.db.rawQuery(selectQuery, new String[]{contactNumber});
        String conNumber = "";
        if (c.moveToFirst()) {
            conNumber = c.getString(c.getColumnIndexOrThrow(COL_CONTACT_NUMBER));
        }
        c.close();
        closeDBConnection(_db);
        return conNumber;

    }

//    public void setFav(int fav, String id) {
//        ContentValues values = new ContentValues();
//        ContactModel _db = getRW(cx);
//        values.put(COL_FAV, fav);
//        int s = _db.db.update(TABLE_CONTACTS, values, COL_CONTACT_NUMBER + "='" + id + "'", null);
//        Log.d("update", id + " " + values.get(COL_FAV) + " " + s);
//        _db.close();
//    }

    public void setBlocked(int blocked, String id) {
        ContentValues values = new ContentValues();
        ContactModel _db = getRW(cx);
        values.put(COL_BLOCKED, blocked);
        values.put(COL_BLOCKED_AT, System.currentTimeMillis() + "");
        int s = _db.db.update(TABLE_CONTACTS, values, COL_CONTACT_NUMBER + "='" + id + "'", null);
        Log.d("update", id + " " + values.get(COL_BLOCKED) + " " + s);
        closeDBConnection(_db);
    }

    public int getBlocked(String id) {
        int k = 123;
        String query = "SELECT blocked  FROM contacts  WHERE number = " + id;
        Cursor c = dbHelper.rawQry(query, null);
        if (c != null && c.moveToFirst()) {
            k = c.getInt(0);
            c.close();
        }
        return k;
    }

    public String getBlockedAt(String id) {
        String blockedAt = "0";
        String query = "SELECT " + COL_BLOCKED_AT + " FROM contacts WHERE number = ?";
        Cursor c = dbHelper.rawQry(query, new String[]{id});
        if (c != null && c.moveToFirst()) {
            blockedAt = c.getString(0);
            c.close();
        }
        return blockedAt;
    }


    public Cursor rawQry(String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }

    public void incrementNotificationCount(String whereClause, String time) {
        String query = "UPDATE " + ContactModel.TABLE_CONTACTS + " SET " + ContactModel.COL_NOTIFICATION + " = " + ContactModel.COL_NOTIFICATION + "+1," + ContactModel.COL_NOTIFICATION_TIME + "=" + time + " WHERE " + whereClause + " ";
        try {
            Cursor c = dbHelper.rawQry(query, null);
            if (c != null && c.moveToFirst()) {
                c.close();
            }
        } catch (SQLException e) {
        }
    }
    public static void closeDBConnection(ContactModel _db) {
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
