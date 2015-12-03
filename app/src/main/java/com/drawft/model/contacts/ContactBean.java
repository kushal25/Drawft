package com.drawft.model.contacts;


import android.content.ContentValues;
import android.content.Context;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class ContactBean {
    String uid;
    String name;
    String number;
    int isappusing;
    int isNotification;
    long notificationTime;
    int isBlocked;
    int type;
    int memberCount = 2;

    public ContactBean() {

    }

    public static ContactBean fromJSON(JSONObject vDrawft2, Integer position) {
        ContactBean vDrawft = new ContactBean();
        try {
            vDrawft.setName(vDrawft2.getString("name"));
            vDrawft.setNumber(vDrawft2.getString("number"));
            vDrawft.setIsNotification(vDrawft2.getInt("Notification"));
            vDrawft.setIsBlocked(vDrawft2.getInt("Blocked"));
            vDrawft.setIsAppUsing(vDrawft2.getInt("AppUsing"));
        } catch (Exception e) {

        }
        return vDrawft;
    }

    public class ContactBeanWorker implements Runnable {
        private final WeakReference<Context> parent;
        private final JSONObject contactBean;
        private final Integer position;

        public ContactBeanWorker(Context parent, JSONObject hashMap, Integer position) {
            this.parent = new WeakReference<Context>(parent);
            this.contactBean = hashMap;
            this.position = position;
        }

        @Override
        public void run() {
            final Context target = (Context) parent.get();
            try {
                ContactBean store = ContactBean.fromJSON(contactBean, position);
                //
                //ContactsDb _db = ContactsDb.getR(target.getApplicationContext());
                //
                ContentValues cv = new ContentValues();
                cv.put(ContactModel.COL_CONTACT_NAME, store.getName());
                cv.put(ContactModel.COL_CONTACT_NUMBER, store.getNumber());
                cv.put(ContactModel.COL_NOTIFICATION, store.getIsNotification());
                cv.put(ContactModel.COL_APPUSING, store.getIsAppUsing());
                cv.put(ContactModel.COL_BLOCKED, store.getIsBlocked());


                /*StringBuilder sb = new StringBuilder();
                sb.append("SELECT * FROM ");
                sb.append(DbHelper.TABLE_CONTACTS).append(" WHERE ");
                sb.append(DbHelper.COL_CONTACT_NUMBER).append("='" + store.getNumber() + "'");*/
                //
                // Cursor cr = _db.rawQry(sb.toString(), null);
                ContactModel.iOt(target.getApplicationContext(), ContactModel.TABLE_CONTACTS, "", cv);
                // db
            } catch (Exception e) {

            }
        }

        @Override
        public String toString() {
            return contactBean.toString();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setNumber(String num) {
        this.number = num;
    }

    public String getNumber() {
        return this.number;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public int getIsAppUsing() {
        return this.isappusing;
    }

    public void setIsAppUsing(int isappusing) {
        this.isappusing = isappusing;
    }

    public int getIsNotification() {
        return this.isNotification;
    }

    public void setIsNotification(int isNotification) {
        this.isNotification = isNotification;
    }

    public int getIsBlocked() {
        return this.isBlocked;
    }

    public void setIsBlocked(int isBlocked) {
        this.isBlocked = isBlocked;
    }

    public void setNotificationTime(long time) {
        this.notificationTime = time;
    }

    public long getNotificationTime() {
        return this.notificationTime;
    }

    public void setMemberCount(int c) {
        this.memberCount = c;
    }

    public int getMemberCount() {
        return this.memberCount;
    }
}
