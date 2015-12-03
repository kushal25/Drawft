package com.drawft.model.members;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.drawft.db.DbHelper;
import com.drawft.model.contacts.ContactModel;

import java.util.Iterator;
import java.util.Map;


public class GroupMemberModel {

    private SQLiteDatabase db;
    private DbHelper dbHelper;


    public static final String TABLE_GROUP_MEMBERS = "group_members";
    public static final String COLUMN_ID = "_id";
    public static final String COL_G_ID = "gid";
    public static final String COL_MEMBER_ID = "member_id";
    public static final String COL_IS_ADMIN = "is_admin";
    public static final String COL_JOINED_AT = "joined_at";

    private String[] allColumns = {
            COLUMN_ID
            , COL_G_ID
            , COL_MEMBER_ID
            , COL_IS_ADMIN
            , COL_JOINED_AT
    };

    public static final String CREATE_GROUP_MEMBERS = "create table "
            + TABLE_GROUP_MEMBERS + "("
            + COLUMN_ID + " text primary key, "
            + COL_G_ID + " TEXT not null,"
            + COL_MEMBER_ID + " text not null,"
            + COL_IS_ADMIN + " INTEGER not null,"
            + COL_JOINED_AT + " text not null);";

    public Context cx;

    public static final String CLEAR_GROUP_MEMBERS = "DELETE FROM " + CREATE_GROUP_MEMBERS + " ;";

    public GroupMemberModel(Context context) {
        dbHelper = new DbHelper(context);
        cx = context;
    }

    public static GroupMemberModel getRW(Context cx) {
        GroupMemberModel _db = new GroupMemberModel(cx);
        try {
            _db.db = _db.dbHelper.getWritableDatabase();
        } catch (SQLException ex) {

        }
        return _db;
    }

    public static GroupMemberModel getR(Context cx) {
        GroupMemberModel _db = new GroupMemberModel(cx);
        try {
            _db.db = _db.dbHelper.getReadableDatabase();
        } catch (SQLException ex) {

        }
        return _db;
    }

    public static Boolean addMember(Context context, String gid, String memId, int isAdmin, String joinedAt) {

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, gid + "||" + memId);
        values.put(COL_G_ID, gid);
        values.put(COL_MEMBER_ID, memId);
        values.put(COL_IS_ADMIN, isAdmin);
        values.put(COL_JOINED_AT, joinedAt);

        GroupMemberModel _db = getRW(context);
        long id = _db.db.insertWithOnConflict(TABLE_GROUP_MEMBERS, "", values, SQLiteDatabase.CONFLICT_REPLACE);

        /*long id = _db.db.insert(TABLE_GROUP_MEMBERS, null,
                values);*/
        closeDBConnection(_db);
        return (id != -1);

    }

    public void close() {
        try {
            db.close();
        } catch (Exception e) {

        }
    }

    public Cursor getGroupMembers(String gid) {
        Cursor cursor = db.query(TABLE_GROUP_MEMBERS,
                allColumns, COL_G_ID + " = '" + gid + "'", null, null, null, null);
        return cursor;
    }

    public void addMultipleRows(Context context, String gid, String memId, int isAdmin, String joinedAt, Map<String, Integer> mems) {
        /*insert into myTable (col1,col2)
        select aValue as col1,anotherValue as col2
        union select moreValue,evenMoreValue*/
        GroupMemberModel _db = getRW(context);
        StringBuilder cols = new StringBuilder();
        for (int i = 0; i < allColumns.length; i++) {
            if (i != 0)
                cols.append(",");
            cols.append(allColumns[i]);
        }
        String allCols = cols.toString();


        StringBuilder query = new StringBuilder();
        query.append("insert into ");
        query.append(TABLE_GROUP_MEMBERS);
        query.append(" (").append(allCols).append(")");

        query.append(" select ");
        query.append(gid).append(" as ").append(COL_G_ID).append(",");
        query.append(memId).append(" as ").append(COL_MEMBER_ID).append(",");
        query.append(isAdmin).append(" as ").append(COL_IS_ADMIN).append(",");
        query.append(joinedAt).append(" as ").append(COL_JOINED_AT);

        Iterator it = mems.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();

            query.append(" union ");
            query.append(" select ");
            query.append(gid).append(",");
            query.append(pairs.getKey()).append(",");
            query.append(pairs.getValue()).append(",");
            query.append(joinedAt);
        }


        //_db.db.execSQL("insert into " + TABLE_GROUP_MEMBERS + " (" + allCols + ") select "++"")
    }

    public Cursor getGroupMembersWithDetails(String gid) {
        //SELECT * FROM table_a a INNER JOIN table_b b ON a.id=b.other_id WHERE b.property_id=?
        return dbHelper.rawQry("SELECT * FROM " + TABLE_GROUP_MEMBERS + " LEFT JOIN " + ContactModel.TABLE_CONTACTS + " ON " + COL_MEMBER_ID + "=" + ContactModel.COL_CONTACT_NUMBER + " WHERE " + COL_G_ID + "=?", new String[]{String.valueOf(gid)});
        //return dbHelper.rawQry("SELECT * FROM " + TABLE_GROUP_MEMBERS, null);
    }

    public void delGroupMembers(String itemId) {
        db.delete(TABLE_GROUP_MEMBERS, COL_G_ID + "='" + itemId + "'", null);
    }

    public static void delGroupMember(Context context, String itemId, String memberId) {
        GroupMemberModel _db = getRW(context);
        _db.db.delete(TABLE_GROUP_MEMBERS, COL_G_ID + "='" + itemId + "' AND " + COL_MEMBER_ID + " = '" + memberId + "'", null);
    }

    public static void closeDBConnection(GroupMemberModel _db) {
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
