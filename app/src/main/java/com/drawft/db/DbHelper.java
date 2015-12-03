package com.drawft.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.drawft.model.autoMessages.AutoMessagesModel;
import com.drawft.model.contacts.ContactModel;
import com.drawft.model.drawfts.DrawftModel;
import com.drawft.model.groups.GroupModel;
import com.drawft.model.members.GroupMemberModel;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "groupdrawft";
    private static final int DATABASE_VERSION = 3;
    public SQLiteDatabase db;


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //db = this.getWritableDatabase();
    }

   /* public Cursor rawQry(String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }*/


    public Cursor rawQry(String sql, String[] selectionArgs) {
        return this.getReadableDatabase().rawQuery(sql, selectionArgs);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        try {
            database.execSQL(ContactModel.CREATE_CONTACTS);
            database.execSQL(DrawftModel.CREATE_DRAWFTS);
            database.execSQL(GroupModel.CREATE_GROUPS);
            database.execSQL(GroupMemberModel.CREATE_GROUP_MEMBERS);
            database.execSQL(AutoMessagesModel.CREATE_AUTO_MESSAGES);
        } catch (Exception e) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + DrawftModel.TABLE_DRAWFTS);
        db.execSQL("DROP TABLE IF EXISTS " + ContactModel.TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + GroupModel.TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + GroupMemberModel.TABLE_GROUP_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + AutoMessagesModel.TABLE_AUTO_MESSAGES);
        onCreate(db);
    }


}
