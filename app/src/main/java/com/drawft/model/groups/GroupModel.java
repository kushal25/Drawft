package com.drawft.model.groups;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.drawft.db.DbHelper;
import com.drawft.model.members.GroupMemberModel;

import java.util.ArrayList;


public class GroupModel {
    private SQLiteDatabase db;
    private DbHelper dbHelper;

    public static final String TABLE_GROUPS = "groups";
    public static final String COl_GROUP_GID = "gid";
    public static final String COl_GROUP_NAME = "name";
    public static final String COl_COVER_PIC = "cover_pic";
    public static final String COl_GROUP_CREATED_BY = "created_by";
    public static final String COl_GROUP_CREATED_AT = "created_at";
    public static final String COl_GROUP_VIEWED_COUNT = "viewed_count";
    public static final String COl_GROUP_MESSAGES_COUNT = "msg_count";
    public static final String COL_GROUP_BLOCKED = "blocked";
    public static final String COL_GROUP_BLOCKED_AT = "blocked_at";
    public static final String COL_GROUP_NOTIFICATION = "notification";
    public static final String COL_GROUP_NOTIFICATION_TIME = "notification_time";
    public static final String COL_GROUP_APPUSING = "appUsing";
    public static final String COL_GROUP_LASTSYNCED = "last_synced";

    public static final String CREATE_GROUPS = "create table "
            + TABLE_GROUPS + "("
            + COl_GROUP_GID + " TEXT  PRIMARY KEY,"
            + COl_GROUP_NAME + " text not null,"
            + COl_GROUP_CREATED_BY + " text not null,"
            + COl_COVER_PIC + " text not null,"
            + COL_GROUP_NOTIFICATION + " INTEGER DEFAULT 0,"
            + COL_GROUP_NOTIFICATION_TIME + " text DEFAULT 0,"
            + COL_GROUP_APPUSING + " INTEGER DEFAULT 1,"
            + COL_GROUP_BLOCKED + " INTEGER DEFAULT 0,"
            + COL_GROUP_BLOCKED_AT + " text DEFAULT 0,"
            + COl_GROUP_VIEWED_COUNT + " INTEGER not null,"
            + COl_GROUP_MESSAGES_COUNT + " INTEGER not null,"
            + COl_GROUP_CREATED_AT + " text not null,"
            + COL_GROUP_LASTSYNCED + " text null);";

    private String[] allColumns = {
            COl_GROUP_GID
            , COl_GROUP_NAME
            , COl_GROUP_CREATED_BY
            , COl_GROUP_CREATED_AT
            , COL_GROUP_NOTIFICATION
            , COL_GROUP_NOTIFICATION_TIME
            , COL_GROUP_APPUSING
            , COL_GROUP_BLOCKED
            , COL_GROUP_BLOCKED_AT
            , COl_GROUP_VIEWED_COUNT
            , COl_GROUP_MESSAGES_COUNT
            , COl_COVER_PIC
    };

    public Context cx;

    public static final String CLEAR_CONTACTS = "DELETE FROM " + TABLE_GROUPS + " ;";

    public GroupModel(Context context) {
        dbHelper = new DbHelper(context);
        cx = context;
    }


    public Boolean addNewGroup(GroupSkeleton gInfo) {

        ContentValues values = new ContentValues();
        values.put(COl_GROUP_GID, gInfo.getGroupId());
        values.put(COl_GROUP_NAME, gInfo.getGroupName());
        values.put(COl_GROUP_MESSAGES_COUNT, gInfo.getMsgCount());
        values.put(COl_GROUP_VIEWED_COUNT, gInfo.getViewedCount());
        values.put(COl_GROUP_CREATED_AT, gInfo.getCreatedAt());
        values.put(COL_GROUP_NOTIFICATION, gInfo.getIsNotification());
        values.put(COL_GROUP_APPUSING, gInfo.getIsAppUsing());
        values.put(COL_GROUP_BLOCKED, gInfo.getIsBlocked());
        values.put(COl_GROUP_CREATED_BY, gInfo.getCreatedBy());
        values.put(COl_COVER_PIC, gInfo.getCoverPic());
        values.put(COL_GROUP_LASTSYNCED, gInfo.getLastSynced());
        values.put(COL_GROUP_NOTIFICATION_TIME, gInfo.getNotificationTime());

        GroupModel _db = GroupModel.getRW(cx);

        long id = _db.db.insert(TABLE_GROUPS, null,
                values);
        closeDBConnection(_db);
        return (id != -1);

    }

    public Boolean editGroup(GroupSkeleton gInfo) {

        ContentValues values = new ContentValues();
        values.put(COl_GROUP_NAME, gInfo.getGroupName());

        GroupModel _db = GroupModel.getRW(cx);

        long id = _db.db.update(TABLE_GROUPS, values, "gid = '" + gInfo.getGroupId() + "'", null);
        return (id != -1);

    }

    public static GroupModel getRW(Context cx) {
        GroupModel _db = new GroupModel(cx);
        try {
            _db.db = _db.dbHelper.getWritableDatabase();
        } catch (SQLException ex) {

        }
        return _db;
    }

    public static GroupModel getR(Context cx) {
        GroupModel _db = new GroupModel(cx);
        try {
            _db.db = _db.dbHelper.getReadableDatabase();
        } catch (SQLException ex) {

        }
        return _db;
    }

    /*public void close() {
        try {
            dbHelper.close();
        } catch (SQLException ex) {

        }
    }*/

    public Cursor getGroups() {
        String query = "SELECT g.*,count(gm._id) as mem FROM " + TABLE_GROUPS + " as g LEFT JOIN " + GroupMemberModel.TABLE_GROUP_MEMBERS + " as gm ON g.gid = gm.gid GROUP BY gm.gid";
        return dbHelper.rawQry(query, null);
       // return dbHelper.rawQry("SELECT * FROM " + TABLE_GROUPS + " ORDER BY " + COl_GROUP_CREATED_AT + " DESC", null);
    }

    public void delGroup(String itemId) {
        db.delete(GroupModel.TABLE_GROUPS, GroupModel.COl_GROUP_GID + "='" + itemId + "'", null);
    }

//    public void setFav(int fav, String id) {
//        ContentValues values = new ContentValues();
//        GroupModel _db = getRW(cx);
//        values.put(COL_GROUP_FAV, fav);
//        _db.db.update(TABLE_GROUPS, values, COl_GROUP_GID + "='" + id + "'", null);
//        _db.close();
//    }

    public void setBlocked(int blocked, String id) {
        ContentValues values = new ContentValues();
        GroupModel _db = getRW(cx);
        values.put(COL_GROUP_BLOCKED, blocked);
        values.put(COL_GROUP_BLOCKED_AT, System.currentTimeMillis() + "");
        _db.db.update(TABLE_GROUPS, values, COl_GROUP_GID + "='" + id + "'", null);
        closeDBConnection(_db);

    }

    public int getBlocked(String id) {
        int k = 123;
        String query = "SELECT blocked FROM groups WHERE gid = ?";
        Cursor c = dbHelper.rawQry(query, new String[]{id});
        if (c != null && c.moveToFirst()) {
            k = c.getInt(0);
            c.close();
        }
        return k;
    }

    public String getBlockedAt(String id) {
        String blockedAt = "0";
        String query = "SELECT " + COL_GROUP_BLOCKED_AT + " FROM groups WHERE gid = ?";
        Cursor c = dbHelper.rawQry(query, new String[]{id});
        if (c != null && c.moveToFirst()) {
            blockedAt = c.getString(0);
            c.close();
        }
        return blockedAt;
    }

    public void updateGroupName(String grpName, String id) {
        ContentValues values = new ContentValues();
        GroupModel _db = getRW(cx);
        values.put(COl_GROUP_NAME, grpName);
        _db.db.update(TABLE_GROUPS, values, COl_GROUP_GID + "='" + id + "'", null);
        closeDBConnection(_db);
    }

    public String getGroupName(String id) {
        String grpName = "";
        String query = "SELECT name FROM groups WHERE gid = ?";
        Cursor c = dbHelper.rawQry(query, new String[]{id});
        if (c != null && c.moveToFirst()) {
            grpName = c.getString(0);
            c.close();
        }
        return grpName;

    }

    public static int uOt(Context cx, String table, ContentValues values, String whereClause, String[] whereArgs) {
        int affected = 0;
        GroupModel _db = getRW(cx);
        try {
            affected = _db.db.update(table, values, whereClause, whereArgs);
        } catch (SQLException e) {
        }
        closeDBConnection(_db);
        return affected;
    }

    public void incrementNotificationCount(String whereClause, String time) {
        String query = "UPDATE " + GroupModel.TABLE_GROUPS + " SET " + GroupModel.COL_GROUP_NOTIFICATION + " = " + GroupModel.COL_GROUP_NOTIFICATION + "+1," + GroupModel.COL_GROUP_NOTIFICATION_TIME + "=" + time + " WHERE " + whereClause + " ";
        try {
            Cursor c = dbHelper.rawQry(query, null);
            if (c != null && c.moveToFirst()) {
                c.close();
            }
        } catch (SQLException e) {

            int k = 0;
        }
    }

    public static ArrayList<String> getCurrentGroups(Context cx) {
        ArrayList<String> groups = new ArrayList<>();
        GroupModel _db = getRW(cx);
        String query = "SELECT * FROM " + TABLE_GROUPS;
        Cursor c = _db.dbHelper.rawQry(query, null);
        if (c != null && c.getCount() > 0 && c.moveToFirst()) {
            do {
                groups.add(c.getString(c.getColumnIndexOrThrow(COl_GROUP_GID)));

            } while (c.moveToNext());
            c.close();
        }

        closeDBConnection(_db);
        return groups;
    }

    public static void closeDBConnection(GroupModel _db) {
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
