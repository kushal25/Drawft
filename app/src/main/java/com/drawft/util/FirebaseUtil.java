package com.drawft.util;

import android.content.Context;
import android.widget.Toast;

import com.drawft.GroupDrawft;
import com.drawft.model.groups.GroupSkeleton;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FirebaseUtil {

    public static boolean connectedToFB = false;
    private String rootUrl = GroupDrawft.P.FB_URL;
    private Context context = null;
    private Firebase fbRef = new Firebase(rootUrl);
    CharSequence text;
    private ChildEventListener groupDrawftListener = null;

    private String groupDrawftUrl = "groups_data";
    private String groupsUrl = "groups";
    private String userGroupsUrl = "user_groups";
    private String blockListUrl = "block_list";
    private String onlineUsersUrl = "online_users";
    private String lastSeenUrl = "last_seen";

    DrawableDataFromFB dispatcher = null;
    GroupDataFromFB groupDataDispatcher = null;


    public FirebaseUtil(final Context ctx, String token) {
        this.context = ctx;
        authenticateToken(token);
    }

    public static void goOffline() {
        Firebase.goOffline();
    }

    public static void goOnline() {
        Firebase.goOnline();
    }

    public void authenticateToken(String token) {

        if (!GroupDrawft.isNetworkOK(context)) {
            return;
        }

        Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Authenticated successfully with payload authData
                try {
                    checkFBConnection();
                    //Toast.makeText(context, "Authenticated with token.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {

                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // Authenticated failed with error firebaseError
                try {
                    if (context != null) {
                        FirebaseUtil.this.dispatcher.onAuthError();
                        //Toast.makeText(context, "Authentication failed with token.", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {

                }

            }
        };
        if (token != null) {
            fbRef.authWithCustomToken(token, authResultHandler);
        }
    }

    public String setNewDrawft(final boolean isGroup, ArrayList<HashMap> drawft, final String groupId, final String by, int isOnline) {
        HashMap val = new HashMap();
        val.put("info", by);
        val.put("data", drawft);
        val.put("time", ServerValue.TIMESTAMP);
        val.put("res", GroupDrawft.P.RESOLUTION);
        Firebase newRef = null;
        if (isGroup) {
            newRef = this.fbRef.child(groupDrawftUrl).child(groupId).push();
        } else {
            newRef = this.fbRef.child(groupDrawftUrl).child(groupId).child(by).push();
        }
        if (isOnline == 1) {
            newRef.setValue(val, ServerValue.TIMESTAMP, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        text = "Data could not be saved.";
                        Toast.makeText(FirebaseUtil.this.context, text, Toast.LENGTH_SHORT).show();
                    } else {
                        if (FirebaseUtil.this.dispatcher != null) {
                            FirebaseUtil.this.dispatcher.onSaveDrawft(groupId, firebase.getKey());
                            getPriorityOfDrawft(isGroup, groupId, by, firebase.getKey());
                            /*text = "Data saved successfully.";
                            Toast.makeText(FirebaseUtil.this.context, text, Toast.LENGTH_SHORT).show();*/
                        }

                    }
                }
            });
        }

        return newRef.getKey();
    }

    public void syncOfflineDrawft(final boolean isGroup, ArrayList<HashMap> drawft, final String groupId, final String by, final String drawftId) {
        HashMap val = new HashMap();
        val.put("info", by);
        val.put("data", drawft);
        val.put("time", ServerValue.TIMESTAMP);
        val.put("res", GroupDrawft.P.RESOLUTION);
        Firebase newRef = null;
        if (isGroup) {
            newRef = this.fbRef.child(groupDrawftUrl).child(groupId).child(drawftId);
        } else {
            newRef = this.fbRef.child(groupDrawftUrl).child(groupId).child(by).child(drawftId);
        }
        newRef.setValue(val, ServerValue.TIMESTAMP, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    text = "Data could not be saved.";
                    Toast.makeText(FirebaseUtil.this.context, text, Toast.LENGTH_SHORT).show();
                } else {
                    if (FirebaseUtil.this.dispatcher != null) {
                        FirebaseUtil.this.dispatcher.onSaveDrawft(groupId, firebase.getKey());
                        getPriorityOfDrawft(isGroup, groupId, by, firebase.getKey());
                        /*text = "Offline Drawft saved successfully.";
                        Toast.makeText(FirebaseUtil.this.context, text, Toast.LENGTH_SHORT).show();*/
                    }
                }
            }
        });
    }

    public void addToBlockList(String userId, String blockItem, String type) {
        this.fbRef.child(blockListUrl).child(userId).child(blockItem).setValue(type, ServerValue.TIMESTAMP);
    }

    public void removeFromBlockList(String userId, String blockItem) {
        this.fbRef.child(blockListUrl).child(userId).child(blockItem).removeValue();
    }

    public GroupSkeleton createGroup(String gName, String cover, String owner, int fav, int isNotification, int isappusing, int isBlocked, Map<String, Integer> members, final String createdAt) {

        Firebase newRef = this.fbRef.child(groupsUrl).push();

        GroupSkeleton groupInfo = new GroupSkeleton(gName, cover, owner, newRef.getKey(), fav, isNotification, isappusing, isBlocked, members, createdAt);
        newRef.setValue(groupInfo);
        this.fbRef.child(userGroupsUrl).child(owner).child(newRef.getKey()).setValue(1);
        return groupInfo;
    }

    public GroupSkeleton editGroup(final String gid, final String gName, final String cover, final String owner, final int fav, final int isNotification, final int isappusing, final int isBlocked, final Map<String, Integer> members, final String createdAt) {

       /* Firebase newRef = this.fbRef.child(groupsUrl).child(gid);


        newRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                MutableData newData = new MutableData()
                if (currentData.getValue() == null) {
                    Map<String, Object> oldData = (Map<String, Object>) currentData.getValue();
                    GroupSkeleton groupInfo = new GroupSkeleton(gName, owner, gid, members);
                    groupInfo.setMsgCount((Integer) oldData.get("msgCount"));
                    currentData.
                } else {

                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {

            }
        });
        GroupSkeleton groupInfo = new GroupSkeleton(gName, owner, newRef.getKey(), members);
        newRef.setValue(groupInfo);
        this.fbRef.child(userGroupsUrl).child(owner).child(newRef.getKey()).setValue(1);
        return groupInfo;*/
        return new GroupSkeleton(gName, cover, owner, gid, fav, isNotification, isappusing, isBlocked, members, createdAt);
    }

    /*public void addCommunication(String gName, String owner) {

        String gid=
        Firebase newRef = this.fbRef.child(groupsUrl).push();
        GroupSkeleton groupInfo = new GroupSkeleton(gName, owner, newRef.getKey());
        newRef.setValue(groupInfo);
        this.fbRef.child(userGroupsUrl).child(owner).child(newRef.getKey()).setValue(1);
    }*/

    public void addAdmins(String no, String other) {
        // GroupSkeleton groupInfo = new GroupSkeleton(name, name, no);
        // this.fbRef.child(groupsUrl).child(no).setValue(groupInfo);
        this.fbRef.child(userGroupsUrl).child(no).child(other).setValue(1);
    }

    /*public void getUserGroups(String user) {
        final ArrayList<String> groupsList = new ArrayList<String>();
        this.fbRef.child(userGroupsUrl).child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Object data = dataSnapshot.getValue();
                    Map<String, Object> list = (Map<String, Object>) data;
                    for (String key : list.keySet()) {
                        groupsList.add(key);
                        loadGroupInfo(key);
                    }
                } else {
                    FirebaseUtil.this.groupDataDispatcher.onDataReceive(null);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }*/

    public void removeDrawft(boolean isGroup, String key, String groupId, String otherNumber) {
        Firebase queryRef;
        if (isGroup) {
            return;
            // queryRef = this.fbRef.child(groupDrawftUrl).child(groupId).child(key);
        } else {
            queryRef = this.fbRef.child(groupDrawftUrl).child(groupId).child(otherNumber).child(key);
        }
        queryRef.removeValue();
    }

    public void getBlockList(String userId) {
        Query queryRef = this.fbRef.child(blockListUrl).child(userId);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Map<String, String> blockList = (Map<String, String>) dataSnapshot.getValue();
                    FirebaseUtil.this.groupDataDispatcher.onDataReceive(blockList);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getOldDrawftsFrom(final boolean isGroup, final String groupId, final String otherNumber, String lastSynced, final boolean listenNew) {
        Query queryRef;
        if (isGroup) {
            queryRef = this.fbRef.child(groupDrawftUrl).child(groupId).orderByPriority().startAt(Long.parseLong(lastSynced) + 1);
        } else {
            queryRef = this.fbRef.child(groupDrawftUrl).child(groupId).child(otherNumber).startAt(Long.parseLong(lastSynced) + 1);
        }
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {

                    if (listenNew)
                        FirebaseUtil.this.listenGroupDataChanges(isGroup, groupId, otherNumber);

                    if (snapshot.getValue() != null) {
                        /*text = snapshot.getChildrenCount() + " Syncing drawfts ";
                        Toast.makeText(FirebaseUtil.this.context, text, Toast.LENGTH_SHORT).show();*/
                        for (DataSnapshot child : snapshot.getChildren()) {
                            HashMap childData = (HashMap) child.getValue();
                            List list = (List) childData.get("data");
                            if (FirebaseUtil.this.dispatcher != null) {
                                FirebaseUtil.this.dispatcher.onOldDataReceive(isGroup, groupId, list, child.getKey(), (String) childData.get("info"), childData.get("time") + "", (String) childData.get("res"));
                            }
                        }

                    /*List list = (List) data.get("data");
                    int i = list.size();
                    text = "Available drawfts = " + i;*/
                    } else {
                        if (FirebaseUtil.this.dispatcher != null) {
                            FirebaseUtil.this.dispatcher.onOldDataReceive(isGroup, groupId, new ArrayList(), "", "no data", "", "");
                        }
                    }

                } catch (Exception e) {

                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getPriorityOfDrawft(boolean isGroup, final String groupId, String by, final String drawftId) {
        Query listenerRef;
        if (isGroup) {
            listenerRef = this.fbRef.child(groupDrawftUrl).child(groupId).child(drawftId).child("time");
        } else {
            listenerRef = this.fbRef.child(groupDrawftUrl).child(groupId).child(by).child(drawftId).child("time");
        }

        listenerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String savedTime = dataSnapshot.getValue() + "";
                    if (FirebaseUtil.this.dispatcher != null) {
                        FirebaseUtil.this.dispatcher.onReceiveTime(groupId, drawftId, savedTime);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void checkFBConnection() {
        this.fbRef.child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    try {
                        fbRef.child(onlineUsersUrl).child(GroupDrawft.P.MOBILE_NUMBER).setValue(Boolean.TRUE, ServerValue.TIMESTAMP);
                        // when this device disconnects, remove it
                        fbRef.child(onlineUsersUrl).child(GroupDrawft.P.MOBILE_NUMBER).onDisconnect().removeValue(new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError error, Firebase firebase) {
                            /*if (error != null) {
                                Toast.makeText(context, "could not establish onDisconnect event", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Established onDisconnect ", Toast.LENGTH_SHORT).show();
                            }*/
                            }
                        });
                        fbRef.child(lastSeenUrl).child(GroupDrawft.P.MOBILE_NUMBER).setValue(ServerValue.TIMESTAMP);
                        FirebaseUtil.connectedToFB = true;
                        // when I disconnect, update the last time I was seen online
                        if (FirebaseUtil.this.dispatcher != null) {
                            FirebaseUtil.this.dispatcher.onConnection();
                            // Toast.makeText(context, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {

                    }
                    // add this device to my connections list
                    // this value could contain info about the device or a timestamp too


                } else {
                    FirebaseUtil.connectedToFB = false;
                    //Log.d(FirebaseUtil.class.getSimpleName(), "Not Connected to firebase " + FirebaseUtil.connectedToFB);
                    // Toast.makeText(context, "Not Connected to firebase " + FirebaseUtil.connectedToFB, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
                FirebaseUtil.connectedToFB = false;
                //Toast.makeText(context, "Listener was cancelled at .info/connected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getServerOffset() {
        Firebase ref = this.fbRef.child(".info/serverTimeOffset");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double estimatedServerTimeMs = System.currentTimeMillis() + offset;
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    public void getLastSeen(String userId) {
        this.fbRef.child(lastSeenUrl).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Long lastSeenTime = (Long) dataSnapshot.getValue();
                    if (FirebaseUtil.this.dispatcher != null) {
                        FirebaseUtil.this.dispatcher.onLastSeenReceive(lastSeenTime);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void setDispatcher(DrawableDataFromFB dis) {
        this.dispatcher = dis;
    }

    public void setGroupDataDispatcher(GroupDataFromFB dis) {
        this.groupDataDispatcher = dis;
    }

    public void listenGroupDataChanges(boolean isGroup, String groupId, String otherNumber) {

        Query listenerRef;
        if (isGroup) {
            listenerRef = this.fbRef.child(groupDrawftUrl).child(groupId).limitToLast(1);
        } else {
            listenerRef = this.fbRef.child(groupDrawftUrl).child(groupId).child(otherNumber).limitToLast(1);
        }
        this.groupDrawftListener = listenerRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    HashMap data = (HashMap) dataSnapshot.getValue();
                    List list = (List) data.get("data");
                    if (list != null && list.size() > 0) {
                        int i = list.size();
                        text = "Available drawfts = " + i;
                        //Toast.makeText(FirebaseUtil.context, text, Toast.LENGTH_SHORT).show();
                        if (FirebaseUtil.this.dispatcher != null) {
                            FirebaseUtil.this.dispatcher.onDataReceive(list, dataSnapshot.getKey(), (String) data.get("info"), data.get("time") + "", (String) data.get("res"));
                        }
                    }

                } else {
                    FirebaseUtil.this.dispatcher.onDataReceive(new ArrayList(), "", "no data", "", "");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                int i = 0;
            }
        });
    }

    public void close(boolean isGroup, String gid, String otherNumber) {
        if (isGroup) {
            this.fbRef.child(groupDrawftUrl).child(gid).removeEventListener(this.groupDrawftListener);
        } else {
            this.fbRef.child(groupDrawftUrl).child(gid).child(otherNumber).removeEventListener(this.groupDrawftListener);
        }
    }

    public void destroy() {
        try {
            this.context = null;
            this.dispatcher = null;
        } catch (Exception e) {

        }
    }
    public static abstract interface DrawableDataFromFB {
        public abstract void onDataReceive(List list, String fName, String by, String time, String res);

        public abstract void onSaveDrawft(String groupId, String drawftId);

        public abstract void onReceiveTime(String groupId, String drawftId, String time);

        public abstract void onOldDataReceive(boolean isGroup, String groupId, List list, String fName, String by, String time, String res);

        public abstract void onConnection();

        public abstract void onAuthError();

        public abstract void onLastSeenReceive(Long time);
    }

    public static abstract interface GroupDataFromFB {
        public abstract void onDataReceive(Map<String, String> data);
    }
}
