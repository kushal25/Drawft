/*
 * Copyright 2012 Google Inc. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.drawft;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.drawft.GroupDrawft.P;
import com.drawft.model.autoMessages.AutoMessagesModel;
import com.drawft.model.contacts.ContactModel;
import com.drawft.model.drawfts.DrawftModel;
import com.drawft.model.groups.GroupModel;
import com.drawft.model.groups.GroupSkeleton;
import com.drawft.model.members.GroupMemberModel;
import com.drawft.util.PrepareDrawft;
import com.google.android.gcm.GCMBaseIntentService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*import com.drawft.Drawft.Event;
import com.drawft.db.DrawftDatabase;
import com.drawft.framework.L;
import com.drawft.ui.ContactListActivity;
import com.drawft.ui.ContactListActivity.Do;
import com.drawft.ui.DrawingContainerActivity;*/


/**
 * {@link IntentService} responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";
    static GcmDispatcher gcmDispatcher = null;
    static String currentDrawingPad = null;

    public GCMIntentService() {
        super(GroupDrawft.SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        P.GCM_REGISTERED = true;
        P.REGISTRATION_ID = registrationId;
        P.AUTH_CODE = "";
        P.write(context);
        GroupDrawft.displayMessageGCM(context, getString(R.string.gcm_registered, registrationId));
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
    }

    @Override
    protected void onMessage(final Context context, Intent intent) {
        Log.i(TAG, "Received message. Extras: " + intent.getExtras());
        P.read(context);
        String message = getString(R.string.gcm_message) + intent.getExtras().toString();
//		String authCode = intent.getStringExtra("auth_code");
//		if (authCode != null)
//		{
//			P.AUTH_CODE = authCode;
//			P.write(context);
//			Drawft.displayMessageGCM(context, authCode);
//		}
        try {
            String type = intent.getStringExtra("type");
            switch (type) {
                case "add_group":
                    addGroup(context, intent.getStringExtra("group_id"), intent.getStringExtra("groupInfo"), intent.getStringExtra("message_to"));
                    break;
                case "delete_group":
                    deleteGroup(context, intent.getStringExtra("group_id"), intent.getStringExtra("message_to"));
                    break;
                case "delete_member":
                    deleteMember(context, intent.getStringExtra("group_id"), intent.getStringExtra("message_from"), intent.getStringExtra("message_to"));
                    break;
                case "edit_group_name":
                    editGroupName(context, intent.getStringExtra("group_id"), intent.getStringExtra("new_name"), intent.getStringExtra("message_from"), intent.getStringExtra("message_to"));
                    break;
                case "edit_members":
                    editGroupMembers(context, intent.getStringExtra("group_id"), intent.getStringExtra("groupInfo"), intent.getStringExtra("newly_added_members"), intent.getStringExtra("removed_members"), intent.getStringExtra("message_from"), intent.getStringExtra("message_to"));
                    break;
                case "new_drawft":
                    notifyUser(context, Boolean.valueOf(intent.getStringExtra("is_group")), intent.getStringExtra("group_id"), intent.getStringExtra("drawft_id"), intent.getStringExtra("sent_at"), intent.getStringExtra("message_from"), intent.getStringExtra("message_to"), intent.getStringExtra("current_version"));
                    break;
                case "contact_installed":
                    notifyNewInstall(context, intent.getStringExtra("message_from"), intent.getStringExtra("message_to"));
                    break;
            }
            /*final String msgFrom = intent.getStringExtra("message_from");
            final String fromResolution = intent.getStringExtra("res");
            final String msgTo = intent.getStringExtra("message_to");
            if (msgTo != null && msgTo.equals(P.MOBILE_NUMBER)) {
                if (msgFrom != null) {
                    if (P.DESTINATION_USER == null || !P.DESTINATION_USER.equals(msgFrom)) {
                        GroupDrawft.runOnBackground(context, new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    JSONObject contactInfo = new JSONObject();
                                    contactInfo.put("contactPhone", msgFrom);
                                    generateNotification(context, "Ashok" + getResources().getString(R.string.gcm_message), contactInfo);
                                } catch (Exception e) {
                                    // L.fe(context, Drawft.Event.EXCEPTION, e);
                                }
                            *//*try {
                                String contactName = null;
                                String contactRes = null;
                                boolean contactBlock = false;
                                JSONObject contactInfo = new JSONObject();
                                DrawftDatabase.getRW(context);
                                DrawftDatabase _db = DrawftDatabase.getR(context);
                                //
                                StringBuilder sbNotify = new StringBuilder();
                                sbNotify.append("SELECT ").append(DrawftDatabase.col_U_CONTACT_NAME).append(",").append(DrawftDatabase.col_U_RESOLUTION).append(",").append(DrawftDatabase.col_U_IS_BLOCKED).append(",").append(DrawftDatabase.col_U_IS_APPUSING).append("  FROM ");
                                sbNotify.append(DrawftDatabase.table_Drawft_CONTACTS);
                                sbNotify.append(" WHERE ");
                                sbNotify.append(DrawftDatabase.col_U_CONTACT_NUMBER).append(" = '").append(msgFrom).append("'");
                                Cursor cr = _db.rawQry(sbNotify.toString(), null);
                                if (cr != null && cr.getCount() > 0 && cr.moveToFirst()) {
                                    contactName = cr.getString(cr.getColumnIndexOrThrow(DrawftDatabase.col_U_CONTACT_NAME));
                                    contactRes = cr.getString(cr.getColumnIndexOrThrow(DrawftDatabase.col_U_RESOLUTION));
                                    contactBlock = cr.getInt(cr.getColumnIndexOrThrow(DrawftDatabase.col_U_IS_BLOCKED)) > 0 ? true : false;
                                    try {
                                        contactInfo.put("contactName", contactName);
                                        contactInfo.put("contactPhone", msgFrom);
                                        contactInfo.put("contactRes", contactRes);
                                        contactInfo.put("contactBlock", contactBlock);
                                    } catch (JSONException e) {
                                        L.fe(getApplicationContext(), Event.EXCEPTION, e);
                                    }
                                    if (cr.getInt(cr.getColumnIndexOrThrow(DrawftDatabase.col_U_IS_APPUSING)) == 0) {
                                        try {
                                            contactInfo.put("contactRes", fromResolution);
                                            ContentValues cvCategory = new ContentValues();
                                            cvCategory.put(DrawftDatabase.col_U_IS_APPUSING, true);
                                            cvCategory.put(DrawftDatabase.col_U_RESOLUTION, fromResolution);
                                            DrawftDatabase.uOt(context, DrawftDatabase.table_Drawft_CONTACTS, cvCategory, DrawftDatabase.col_U_CONTACT_NUMBER + "='" + contactInfo.getString("contactPhone") + "'", null);
                                        } catch (Exception e) {
                                            L.fe(context, Drawft.Event.EXCEPTION, e);
                                        }
                                    }
                                }
                                if (cr.getCount() > 0 && !contactBlock)
                                    generateNotification(context, contactName + " " + getResources().getString(R.string.gcm_message), contactInfo);
                                //
                                if (cr != null && cr.getCount() == 0) {
                                    try {
                                        try {
                                            contactInfo.put("contactName", msgFrom);
                                            contactInfo.put("contactPhone", msgFrom);
                                            contactInfo.put("contactRes", fromResolution);
                                            contactInfo.put("contactBlock", false);
                                        } catch (JSONException e) {
                                            L.fe(getApplicationContext(), Event.EXCEPTION, e);
                                        }
                                        ContentValues cv = new ContentValues();
                                        cv.put(DrawftDatabase.col_U_UID, UUID.randomUUID().toString());
                                        cv.put(DrawftDatabase.col_U_CONTACT_NAME, msgFrom);
                                        cv.put(DrawftDatabase.col_U_CONTACT_NUMBER, msgFrom);
                                        cv.put(DrawftDatabase.col_U_IS_APPUSING, true);
                                        cv.put(DrawftDatabase.col_U_IS_FAVORITED, false);
                                        cv.put(DrawftDatabase.col_U_IS_NOTIFICATION_RECEIVED, true);
                                        cv.put(DrawftDatabase.col_U_IS_BLOCKED, false);
                                        cv.put(DrawftDatabase.col_U_IS_NOTIFICATION_TIME, System.currentTimeMillis());
                                        cv.put(DrawftDatabase.col_U_RESOLUTION, fromResolution);
                                        cv.put(DrawftDatabase.col_U_CONTACT_ORDER, P.TOTAL_CONTACTS + 1);
                                        cv.put(DrawftDatabase.col_U_ACCESS, System.currentTimeMillis());
                                        DrawftDatabase.iOt(context.getApplicationContext(), DrawftDatabase.table_Drawft_CONTACTS, "", cv);
                                        generateNotification(context, msgFrom + " " + getResources().getString(R.string.gcm_message), contactInfo);
                                    } catch (Exception e) {
                                        L.fe(context, Drawft.Event.EXCEPTION, e);
                                    }
                                }
                                if (null != cr && !cr.isClosed()) cr.close();
                            } catch (Exception e) {
                                L.fe(context, Drawft.Event.EXCEPTION, e);
                            }*//*
                            }
                        });
                    }
                }
            }*/

        } catch (Exception e) {

        }

    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    public void notifyUser(Context ctx, boolean isGroup, String groupId, String drawftId, String sentAt, String msgFrom, String msgTo, String version) {
        if (!version.equals(P.APP_VERSION)) {
            P.NEW_VERSION_STATUS = true;
            P.APP_VERSION = version;
            P.write(ctx);
        }
        if (currentDrawingPad != null && ((isGroup && currentDrawingPad.equals(groupId)) || (!isGroup && currentDrawingPad.equals(groupId)))) {
            return;
        }
        if (P.MOBILE_NUMBER.equals(msgTo)) {
            try {

                DrawftModel _db = DrawftModel.getR(ctx);
                if (_db.checkDrawftExistence(groupId, drawftId + PrepareDrawft.imgType)) {
                    DrawftModel.closeDBConnection(_db);
                    return;
                }
                JSONObject contactInfo = new JSONObject();
                contactInfo.put("isGroup", isGroup);

                contactInfo.put("isDrawingPad", true);
                try {
                    //ContentValues cvCategory = new ContentValues();
                    if (isGroup) {
                        //cvCategory.put(GroupModel.COL_GROUP_NOTIFICATION, 1);
                        // GroupModel.uOt(ctx, GroupModel.TABLE_GROUPS, cvCategory, GroupModel.COl_GROUP_GID + "='" + groupId + "'", null);
                        GroupModel gm = new GroupModel(ctx);
                        if (gm.getBlocked(groupId) != 1) {
                            gm.incrementNotificationCount(GroupModel.COl_GROUP_GID + "='" + groupId + "'", sentAt + "");
                        } else {
                            return;
                        }
                        contactInfo.put("contactPhone", groupId);
                    } else {
                        // cvCategory.put(ContactModel.COL_NOTIFICATION, 1);
                        ContactModel cm = new ContactModel(ctx);
                        if (cm.getBlocked(msgFrom) != 1) {
                            cm.incrementNotificationCount(ContactModel.COL_CONTACT_NUMBER + "='" + msgFrom + "'", sentAt + "");
                        } else {
                            return;
                        }

                        String k = cm.searchNumber(msgFrom);
                        /*cm.close();*/
                        if (k.isEmpty()) {
                            ContentValues cv = new ContentValues();
                            cv.put(ContactModel.COL_CONTACT_NAME, "");
                            cv.put(ContactModel.COL_CONTACT_NUMBER, msgFrom);
                            cv.put(ContactModel.COL_NOTIFICATION, 1);
                            cv.put(ContactModel.COL_APPUSING, 1);
                            cv.put(ContactModel.COL_BLOCKED, 0);
                            cv.put(ContactModel.COL_NOTIFICATION, 1);
                            cv.put(ContactModel.COL_NOTIFICATION_TIME, System.currentTimeMillis());
                            ContactModel.iOt(ctx, ContactModel.TABLE_CONTACTS, "", cv);
                        }
                        contactInfo.put("contactPhone", msgFrom);
                        //ContactModel.uOt(ctx, ContactModel.TABLE_CONTACTS, cvCategory, ContactModel.COL_CONTACT_NUMBER + "='" + contactInfo.getString("contactPhone") + "'", null);
                    }
                    ContactModel cm = new ContactModel(ctx);
                    String msgFromName = cm.getContactName(msgFrom);
                    if (msgFromName == null || msgFromName.isEmpty()) {
                        msgFromName = msgFrom;
                    }
                    if (isGroup) {
                        GroupModel gm = new GroupModel(ctx);
                        String gName = gm.getGroupName(groupId);
                        contactInfo.put("groupName", gName);
                        contactInfo.put("groupId", groupId);
                        generateNotification(ctx, msgFromName + " sent you a drawft in \"" + gName + "\" group", contactInfo);
                    } else {
                        contactInfo.put("groupName", msgFromName);
                        contactInfo.put("groupId", msgFrom);
                        generateNotification(ctx, msgFromName + " sent you a drawft", contactInfo);
                    }
                    ContactModel.closeDBConnection(cm);

                } catch (Exception e) {
                    int j = 0;
                }

                GCMIntentService.gcmDispatcher.onNewGcmNotification("on_new_drawft", isGroup, groupId, msgFrom);
            } catch (Exception e) {

            }
        }
    }

    public void addGroup(Context ctx, String groupId, String gInfo, String msgTo) {
        if (P.MOBILE_NUMBER.equals(msgTo)) {
            try {
                JSONObject contactInfo = new JSONObject();
                JSONObject groupInfoObj = new JSONObject(gInfo);
                JSONObject groupMembersObj = groupInfoObj.getJSONObject("members");

                Map<String, Integer> mems = new HashMap<String, Integer>();

                Iterator<String> iter = groupMembersObj.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    mems.put(key, 1);
                    try {
                        int value = (Integer) groupMembersObj.get(key);
                        mems.put(key, value);
                    } catch (JSONException e) {
                    }
                }

                GroupSkeleton groupInfo = new GroupSkeleton(
                        groupInfoObj.getString("groupName")
                        , ""
                        , groupInfoObj.getString("createdBy")
                        , groupInfoObj.getString("groupId")
                        , 0
                        , 0
                        , 1
                        , 0
                        , mems
                        , groupInfoObj.getString("createdAt")
                );
                groupInfo.setNotificationTime(System.currentTimeMillis());
                GroupModel gm = new GroupModel(getApplicationContext());
                boolean inserted = gm.addNewGroup(groupInfo);
                if (inserted) {
                    Map<String, Integer> gMembersInfo = groupInfo.getMembers();
                    Iterator it = gMembersInfo.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry) it.next();
                        GroupMemberModel.addMember(getApplicationContext(), groupInfo.getGroupId(), (String) pairs.getKey(), (Integer) pairs.getValue(), System.currentTimeMillis() + "");

                        if (P.MOBILE_NUMBER.equals(pairs.getKey()) && !groupInfo.getCreatedBy().equals(pairs.getKey())) {
                            ContentValues cv = new ContentValues();
                            JSONObject object = new JSONObject();
                            cv.put(AutoMessagesModel.COL_GROUP_GID, groupId);
                            cv.put(AutoMessagesModel.COL_TYPE, "groupMemberAdded");
                            try {
                                object.put("added_members", pairs.getKey());
                                cv.put(AutoMessagesModel.COL_CONTENT, object.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            AutoMessagesModel.iOt(getApplicationContext(), AutoMessagesModel.TABLE_AUTO_MESSAGES, "", cv);
                            int autoMessageId = AutoMessagesModel.getLastInsertedId(ctx, groupId, "groupMemberAdded");
                            DrawftModel mDrawft = DrawftModel.getRW(ctx);
                            mDrawft.addNewRecord(groupId, groupInfo.getCreatedBy(), null, (System.currentTimeMillis()) + "", null, autoMessageId, 1);
                            DrawftModel.closeDBConnection(mDrawft);
                        }

                    }
                    contactInfo.put("contactPhone", groupInfoObj.getString("createdBy"));
                    ContactModel cm = new ContactModel(ctx);
                    String msgFromName = cm.getContactName(groupInfoObj.getString("createdBy"));
                    if (msgFromName == null || msgFromName.isEmpty()) {
                        msgFromName = groupInfoObj.getString("createdBy");
                    }
                    contactInfo.put("isGroup", true);
                    contactInfo.put("groupId", groupId);
                    contactInfo.put("isDrawingPad", true);
                    contactInfo.put("groupName", groupInfoObj.getString("groupName"));
                    generateNotification(ctx, msgFromName + " added you to the group \"" + groupInfoObj.getString("groupName") + "\"", contactInfo);
                    GCMIntentService.gcmDispatcher.onNewGcmNotification("on_new_group", true, groupId, "");
                }

            } catch (Exception e) {
                int i = 0;
            }
        }
    }

    public void deleteGroup(Context ctx, String groupId, String msgTo) {
        if (P.MOBILE_NUMBER.equals(msgTo)) {
            PrepareDrawft.onDeleteGroup(ctx, groupId);
            GCMIntentService.gcmDispatcher.onNewGcmNotification("on_delete_group", true, groupId, "");
        }
    }

    public void deleteMember(Context ctx, String groupId, String msgFrom, String msgTo) {
        //if (P.MOBILE_NUMBER.equals(msgTo)) {
        if (P.MOBILE_NUMBER.equals(msgFrom)) {
            PrepareDrawft.onDeleteGroup(ctx, groupId);
            GCMIntentService.gcmDispatcher.onNewGcmNotification("on_delete_group", true, groupId, "");
        } else {
            //TO-DO on member delete
            GroupMemberModel.delGroupMember(ctx, groupId, msgFrom);
            JSONObject object = new JSONObject();
            ContentValues cv = new ContentValues();
            cv.put(AutoMessagesModel.COL_GROUP_GID, groupId);
            cv.put(AutoMessagesModel.COL_TYPE, "groupMemberRemoved");
            try {
                object.put("removed_members", msgFrom);
                cv.put(AutoMessagesModel.COL_CONTENT, object.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            AutoMessagesModel.iOt(getApplicationContext(), AutoMessagesModel.TABLE_AUTO_MESSAGES, "", cv);
            int autoMessageId = AutoMessagesModel.getLastInsertedId(ctx, groupId, "groupMemberRemoved");
            DrawftModel mDrawft = new DrawftModel(ctx);
            mDrawft.addNewRecord(groupId, msgTo, null, (System.currentTimeMillis()) + "", null, autoMessageId, 1);
            DrawftModel.closeDBConnection(mDrawft);
        }
        //}
    }

    public void editGroupName(Context ctx, String groupId, String newName, String msgFrom, String msgTo) {
        if (P.MOBILE_NUMBER.equals(msgTo)) {
            ContentValues cvCategory = new ContentValues();
            cvCategory.put(GroupModel.COl_GROUP_NAME, newName);
            GroupModel.uOt(ctx, GroupModel.TABLE_GROUPS, cvCategory, GroupModel.COl_GROUP_GID + "='" + groupId + "'", null);
            ContentValues cv = new ContentValues();
            cv.put(AutoMessagesModel.COL_GROUP_GID, groupId);
            cv.put(AutoMessagesModel.COL_TYPE, "editGroupName");
            JSONObject object = new JSONObject();
            try {
                object.put("group_name", newName);
                cv.put(AutoMessagesModel.COL_CONTENT, object.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            AutoMessagesModel.iOt(getApplicationContext(), AutoMessagesModel.TABLE_AUTO_MESSAGES, "", cv);
            int autoMessageId = AutoMessagesModel.getLastInsertedId(ctx, groupId, "editGroupName");
            DrawftModel mDrawft = new DrawftModel(ctx);
            mDrawft.addNewRecord(groupId, msgFrom, null, (System.currentTimeMillis()) + "", null, autoMessageId, 1);
            GCMIntentService.gcmDispatcher.onNewGcmNotification("on_edit_group", true, groupId, "");
            DrawftModel.closeDBConnection(mDrawft);

            ContactModel cm = new ContactModel(ctx);
            String msgFromName = cm.getContactName(msgFrom);
            if (msgFromName == null || msgFromName.isEmpty()) {
                msgFromName = msgFrom;
            }
            JSONObject contactInfo = new JSONObject();
            try {
                contactInfo.put("contactPhone", msgFrom);
                contactInfo.put("isGroup", true);
                contactInfo.put("groupId", groupId);
                contactInfo.put("isDrawingPad", true);
                contactInfo.put("groupName", newName);
                generateNotification(ctx, msgFromName + " edited group name to \"" + newName + "\"", contactInfo);
            } catch (JSONException e) {

            }
            /*ContentValues cv = new ContentValues();
            cv.put(amm.COL_GROUP_GID, groupId);
            cv.put(amm.COL_TYPE, "editGroupName");
            JSONObject object = new JSONObject();
            try {
                object.put("group_name", groupName);
                cv.put(amm.COL_CONTENT, object.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            amm.iOt(getApplicationContext(), amm.TABLE_AUTO_MESSAGES, "", cv);*/
        }
    }

    public void editGroupMembers(Context ctx, String groupId, String groupInfo, String addedMembers, String removedMembers, String msgFrom, String msgTo) {
        if (P.MOBILE_NUMBER.equals(msgTo)) {
            try {
                JSONObject addedMembersObj = new JSONObject(addedMembers);
                JSONObject removedMembersObj = new JSONObject(removedMembers);
                Iterator<String> newlyAddedList = addedMembersObj.keys();
                Iterator<String> removedList = removedMembersObj.keys();
                while (newlyAddedList.hasNext()) {
                    String member = newlyAddedList.next();
                    if (member.equals(P.MOBILE_NUMBER)) {
                        addGroup(ctx, groupId, groupInfo, msgTo);
                    } else {
                        //add to group members
                        GroupMemberModel.addMember(ctx, groupId, member, 0, System.currentTimeMillis() + "");
                        JSONObject object = new JSONObject();
                        ContentValues cv = new ContentValues();
                        cv.put(AutoMessagesModel.COL_GROUP_GID, groupId);
                        cv.put(AutoMessagesModel.COL_TYPE, "groupMemberAdded");
                        try {
                            object.put("added_members", member);
                            cv.put(AutoMessagesModel.COL_CONTENT, object.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AutoMessagesModel.iOt(getApplicationContext(), AutoMessagesModel.TABLE_AUTO_MESSAGES, "", cv);
                        int autoMessageId = AutoMessagesModel.getLastInsertedId(ctx, groupId, "groupMemberAdded");
                        DrawftModel mDrawft = new DrawftModel(ctx);
                        mDrawft.addNewRecord(groupId, msgFrom, null, (System.currentTimeMillis()) + "", null, autoMessageId, 1);
                    }
                }
                while (removedList.hasNext()) {
                    String member = removedList.next();
                    deleteMember(ctx, groupId, member, msgFrom);
                }
            } catch (JSONException e) {

            }

        }
    }

    public void notifyNewInstall(Context ctx, String msgFrom, String msgTo) {
        if (P.MOBILE_NUMBER.equals(msgTo)) {
            JSONObject contactInfo = new JSONObject();
            try {
                ContactModel cm = new ContactModel(ctx);
                int isUsing = cm.getAppUsing(msgFrom);
                if (isUsing != -1 && isUsing != 1) {
                    ContentValues cvCategory = new ContentValues();
                    cvCategory.put(ContactModel.COL_APPUSING, 1);
                    ContactModel.uOt(ctx, ContactModel.TABLE_CONTACTS, cvCategory, ContactModel.COL_CONTACT_NUMBER + "='" + msgFrom + "'", null);
                    String msgFromName = cm.getContactName(msgFrom);
                    if (msgFromName == null || msgFromName.isEmpty()) {
                        msgFromName = msgFrom;
                    }
                    contactInfo.put("contactPhone", msgFrom);
                    contactInfo.put("isDrawingPad", false);
                    generateNotification(ctx, msgFromName + " has installed Drawft.", contactInfo);
                }

            } catch (JSONException e) {
            }
        }
    }

    public static void setGcmDispatcher(GcmDispatcher dis) {
        GCMIntentService.gcmDispatcher = dis;
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message, JSONObject contactInfo) {

        try {
            int icon = R.drawable.ic_launcher;
            long when = System.currentTimeMillis();
            String title = context.getString(R.string.app_name);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(icon, message, when);

            // parameters
            /*Notification notification = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setSmallIcon(icon)
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(message))
                    .build();*/
            Intent notificationIntent = null;
            Bundle extras = new Bundle();
            if (contactInfo.getBoolean("isDrawingPad")) {
                notificationIntent = new Intent(context, DrawingPadActivity.class);
                extras.putString("groupId", contactInfo.getString("groupId"));
                extras.putBoolean("isGroup", contactInfo.getBoolean("isGroup"));
                extras.putString("groupName", contactInfo.getString("groupName"));
                extras.putBoolean("refreshActivity", true);

            } else {
                notificationIntent = new Intent(context, CommunicationListActivity.class);
            }


            /*Bundle extras = new Bundle();
            extras.putString("groupId", tile.getGroupId());
            extras.putString("groupName", tile.getGroupName());
            extras.putBoolean("isGroup", tile.getIsGroup());*/
            // set intent so it does not start a new activity

            notificationIntent.putExtras(extras);
            //
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setLatestEventInfo(context, title, message, intent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;

            //
            notificationManager.notify(contactInfo.getString("contactPhone").hashCode(), notification);

        } catch (NumberFormatException e) {
            //L.fe(context, Event.EXCEPTION, e);
        } catch (JSONException e) {
            //  L.fe(context, Event.EXCEPTION, e);
        }

        /*try {
            int icon = R.drawable.ic_launcher;
            long when = System.currentTimeMillis();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(icon, message, when);
            // parameters


            String title = context.getString(R.string.app_name);
            Intent notificationIntent = new Intent(context, DrawingContainerActivity.class);
            // set intent so it does not start a new activity
            Bundle extras = new Bundle();
            extras.putString("PhoneNumber", contactInfo.getString("contactPhone"));
            extras.putString("PhoneResolution", contactInfo.getString("contactRes"));
            extras.putString("OtherName", contactInfo.getString("contactName"));
            extras.putBoolean("IsBlocked", contactInfo.getBoolean("contactBlock"));
            notificationIntent.putExtras(extras);
            //
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setLatestEventInfo(context, title, message, intent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
            //
            notificationManager.notify(contactInfo.getString("contactPhone").hashCode(), notification);
            try {
                ContentValues cvCategory = new ContentValues();
                cvCategory.put(DrawftDatabase.col_U_IS_NOTIFICATION_RECEIVED, true);
                DrawftDatabase.uOt(context, DrawftDatabase.table_Drawft_CONTACTS, cvCategory, DrawftDatabase.col_U_CONTACT_NUMBER + "='" + contactInfo.getString("contactPhone") + "'", null);
                ContactListActivity.HANDLER.sendEmptyMessage(Do.GETCONTACTS);
            } catch (Exception e) {
                L.fe(context, Drawft.Event.EXCEPTION, e);
            }
        } catch (NumberFormatException e) {
            L.fe(context, Event.EXCEPTION, e);
        } catch (JSONException e) {
            L.fe(context, Event.EXCEPTION, e);
        }*/
    }

    public static abstract interface GcmDispatcher {
        public abstract void onNewGcmNotification(String type, boolean isGroup, String groupId, String otherNumber);
    }
}
