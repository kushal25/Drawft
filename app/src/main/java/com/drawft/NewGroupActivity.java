package com.drawft;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drawft.GroupDrawft.P;
import com.drawft.data.ContactTile;
import com.drawft.model.autoMessages.AutoMessagesModel;
import com.drawft.model.contacts.ContactModel;
import com.drawft.model.drawfts.DrawftBean;
import com.drawft.model.drawfts.DrawftModel;
import com.drawft.model.groups.GroupModel;
import com.drawft.model.groups.GroupSkeleton;
import com.drawft.model.members.GroupMemberModel;
import com.drawft.service.HttpClientUtil;
import com.drawft.util.CommunicationListAdapter;
import com.drawft.util.FileUtil;
import com.drawft.util.FirebaseUtil;
import com.drawft.util.MemberListAdapter;
import com.drawft.util.PrepareDrawft;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.Firebase;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class NewGroupActivity extends Activity {
    protected TextView leftBtn = null, saveBtn = null, addedMembersBtn = null, addMembersBtn = null, deleteGroupBtn = null;
    protected TextView groupNameInput, memberCount, editGroupNameIcon, blocked, clearDrawfts = null;
    private static final String TAG = NewGroupActivity.class.getSimpleName();
    private ArrayList<String> memberList = new ArrayList<String>();
    protected FileUtil __fileUtil = null;
    private GroupModel gm = new GroupModel(NewGroupActivity.this);
    private String groupId = "", groupName = "", coverPic = "default";
    private Boolean isGroup;
    private int isAdmin = 0;
    private int fav = 0;
    private int isNotification = 0;
    private int isappusing = 1;
    private int isBlocked = 0;
    protected CommunicationListAdapter listAdapter = null;
    public boolean isNewGroup = true;
    private AutoMessagesModel amm = new AutoMessagesModel(NewGroupActivity.this);
    protected FirebaseUtil __firebaseUtil = null;
    private String old_name, new_name;
    private Uri picUri;
    protected ListView addedListView = null, addListView = null;
    protected MemberListAdapter addedListAdapter = null, addListAdapter = null;
    private Bitmap coverBitmap;
    private SimpleDraweeView groupPhoto1, groupPhoto2, groupPhoto3;
    DrawftModel mDrawft = new DrawftModel(NewGroupActivity.this);
    HttpClientUtil httpClientUtil = new HttpClientUtil();
    ContactTile tile_name = new ContactTile();
    JSONObject cons = new JSONObject();
    ProgressBar loadingSave = null;
    View newgroup1, newgroup2, newgroup3, newGroupContainer;
    Date time = new Date();
    View sep1, sep2;
    private List<String> myColorList = GroupDrawft.getColorList();
    public int color1, color2, color3;
    public Dialog mDialog = null;
    public String curMembers = "";
    LinearLayout saveBtnWrapper = null;


    JsonHttpResponseHandler editGroupNameListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("success")) {
                    groupNameInput.setText(new_name);
                    String groupName = groupNameInput.getText().toString();
                    gm.updateGroupName(groupName, groupId);
                    ContentValues cv = new ContentValues();
                    cv.put(AutoMessagesModel.COL_GROUP_GID, groupId);
                    cv.put(AutoMessagesModel.COL_TYPE, "editGroupName");
                    JSONObject object = new JSONObject();
                    try {
                        object.put("group_name", groupName);
                        cv.put(AutoMessagesModel.COL_CONTENT, object.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AutoMessagesModel.iOt(getApplicationContext(), AutoMessagesModel.TABLE_AUTO_MESSAGES, "", cv);

                    int autoMessageId = AutoMessagesModel.getLastInsertedId(NewGroupActivity.this, groupId, "editGroupName");
                    mDrawft.addNewRecord(groupId, P.MOBILE_NUMBER, null, System.currentTimeMillis() + "", null, autoMessageId, 1);
                    hideProgressLoader();
                } else {
                    Toast.makeText(getApplicationContext(), "Error while editing.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {

            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
            int i = 0;
        }
    };
    JsonHttpResponseHandler editGroupMembersListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("success")) {
                    ArrayList<String> previous_grp_members = new ArrayList<>();
                    ArrayList<String> current_grp_members = new ArrayList<>();
                    GroupMemberModel groupMembersDb = GroupMemberModel.getR(getApplicationContext());
                    Cursor cr = groupMembersDb.getGroupMembersWithDetails(groupId);
                    if (cr != null && cr.getCount() > 0 && cr.moveToFirst()) {
                        do {
                            if (!P.MOBILE_NUMBER.equals(cr.getString(cr.getColumnIndexOrThrow(GroupMemberModel.COL_MEMBER_ID)))) {
                                previous_grp_members.add(cr.getString(cr.getColumnIndexOrThrow(GroupMemberModel.COL_MEMBER_ID)));
                            }
                        } while (cr.moveToNext());
                        cr.close();
                        GroupMemberModel.closeDBConnection(groupMembersDb);
                    }
                    for (int i = 0; i < addedListAdapter.getCount(); i++) {
                        ContactTile tile = addedListAdapter.getItem(i);
                        if (!P.MOBILE_NUMBER.equals(tile.getMobileNumber())) {
                            current_grp_members.add(tile.getMobileNumber());
                        }
                    }
                    StringBuilder sb1 = new StringBuilder();
                    for (int j = 0; j < previous_grp_members.size(); j++) {
                        if (!current_grp_members.contains(previous_grp_members.get(j))) {
                            GroupMemberModel.delGroupMember(getApplicationContext(), groupId, previous_grp_members.get(j));
                            sb1.append(previous_grp_members.get(j));
                            sb1.append(",");
                        }
                    }
                    if (sb1.toString().length() != 0) {
                        JSONObject object = new JSONObject();
                        ContentValues cv = new ContentValues();
                        cv.put(AutoMessagesModel.COL_GROUP_GID, groupId);
                        cv.put(AutoMessagesModel.COL_TYPE, "groupMemberRemoved");
                        try {
                            object.put("removed_members", sb1.toString());
                            cv.put(AutoMessagesModel.COL_CONTENT, object.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AutoMessagesModel.iOt(getApplicationContext(), AutoMessagesModel.TABLE_AUTO_MESSAGES, "", cv);
                        int autoMessageId = AutoMessagesModel.getLastInsertedId(NewGroupActivity.this, groupId, "groupMemberRemoved");
                        mDrawft.addNewRecord(groupId, P.MOBILE_NUMBER, null, System.currentTimeMillis() + "", null, autoMessageId, 1);
                    }

                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < current_grp_members.size(); j++) {
                        if (!previous_grp_members.contains(current_grp_members.get(j))) {
                            GroupMemberModel.addMember(getApplicationContext(), groupId, current_grp_members.get(j), 0, System.currentTimeMillis() + "");
                            sb.append(current_grp_members.get(j));
                            sb.append(",");
                        }
                    }
                    if (sb.toString().length() != 0) {
                        JSONObject object = new JSONObject();
                        ContentValues cv = new ContentValues();
                        cv.put(AutoMessagesModel.COL_GROUP_GID, groupId);
                        cv.put(AutoMessagesModel.COL_TYPE, "groupMemberAdded");
                        try {
                            object.put("added_members", sb.toString());
                            cv.put(AutoMessagesModel.COL_CONTENT, object.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AutoMessagesModel.iOt(getApplicationContext(), AutoMessagesModel.TABLE_AUTO_MESSAGES, "", cv);
                        int autoMessageId = AutoMessagesModel.getLastInsertedId(NewGroupActivity.this, groupId, "groupMemberAdded");
                        mDrawft.addNewRecord(groupId, P.MOBILE_NUMBER, null, time.getTime() + "", null, autoMessageId, 1);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error while editing.", Toast.LENGTH_SHORT).show();
                }
                goToHomeActivity();

            } catch (JSONException e) {

            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
            int i = 0;
        }
    };
    JsonHttpResponseHandler deleteGroupListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("success")) {
                    PrepareDrawft.onDeleteGroup(NewGroupActivity.this, groupId);
                    Intent callIntent = new Intent(getApplicationContext(), CommunicationListActivity.class);
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(callIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Error while deleting.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {

            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {

        }
    };

    JsonHttpResponseHandler saveGroupListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("success")) {
                    JSONObject groupInfoObj = response.getJSONObject("group");
                    JSONObject groupMembersObj = groupInfoObj.getJSONObject("members");

                    Map<String, Integer> mems = new HashMap<String, Integer>();
                    Iterator<String> iter = groupMembersObj.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            int value = (Integer) groupMembersObj.get(key);
                            mems.put(key, value);
                        } catch (JSONException e) {
                        }
                    }

                    GroupSkeleton groupInfo = new GroupSkeleton(
                            groupInfoObj.getString("groupName")
                            , coverPic
                            , P.MOBILE_NUMBER
                            , groupInfoObj.getString("groupId")
                            , fav
                            , isNotification
                            , isappusing
                            , isBlocked
                            , mems
                            , groupInfoObj.getString("createdAt")
                    );
                    groupInfo.setNotificationTime(System.currentTimeMillis());
                    GroupModel gm = new GroupModel(getApplicationContext());
                    boolean inserted = gm.addNewGroup(groupInfo);
                    if (inserted) {

                        try {
                            JSONObject object = new JSONObject();
                            ContentValues automessages = new ContentValues();
                            Map.Entry pairs = null;
                            StringBuffer sb = new StringBuffer();
                            automessages.put(AutoMessagesModel.COL_GROUP_GID, groupInfo.getGroupId());
                            automessages.put(AutoMessagesModel.COL_TYPE, "groupCreate");
                            Toast.makeText(getApplicationContext(), "\"" + groupInfo.getGroupName() + "\" group saved.", Toast.LENGTH_SHORT).show();
                            Map<String, Integer> gMembersInfo = groupInfo.getMembers();
                            Iterator it = gMembersInfo.entrySet().iterator();
                            while (it.hasNext()) {
                                pairs = (Map.Entry) it.next();
                                if (!pairs.getKey().toString().equals(P.MOBILE_NUMBER)) {
                                    sb.append(pairs.getKey().toString());
                                    sb.append(",");
                                }
                                GroupMemberModel.addMember(getApplicationContext(), groupInfo.getGroupId(), (String) pairs.getKey(), (Integer) pairs.getValue(), groupInfo.getCreatedAt());
                            }
                            object.put("group_name", groupInfo.getGroupName());
                            object.put("added_members", sb.toString());
                            object.put("removed_members", "");

                            automessages.put(AutoMessagesModel.COL_CONTENT, object.toString());
                            AutoMessagesModel.iOt(getApplicationContext(), AutoMessagesModel.TABLE_AUTO_MESSAGES, "", automessages);
                            int autoMessageId = AutoMessagesModel.getLastInsertedId(NewGroupActivity.this, groupInfo.getGroupId(), "groupCreate");
                            mDrawft.addNewRecord(groupInfo.getGroupId(), P.MOBILE_NUMBER, null, System.currentTimeMillis() + "", null, autoMessageId, 1);
                           /* Collections.sort(addedListAdapter.list, new Comparator<ContactTile>() {
                                @Override
                                public int compare(ContactTile lhs, ContactTile rhs) {
                                    Log.d("check", "saveaddedList " + lhs.getGroupName() + " " + rhs.getGroupName() + " " + lhs.getGroupName().compareTo(rhs.getGroupName()));
                                    return lhs.getGroupName().compareTo(rhs.getGroupName());
                                }
                            });

                            Collections.sort(addListAdapter.list, new Comparator<ContactTile>() {
                                @Override
                                public int compare(ContactTile lhs, ContactTile rhs) {
                                    Log.d("check", "saveaddList " + lhs.getGroupName() + " " + rhs.getGroupName() + " " + lhs.getGroupName().compareTo(rhs.getGroupName()));
                                    return lhs.getGroupName().compareTo(rhs.getGroupName());
                                }
                            });*/

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Re-try", Toast.LENGTH_SHORT).show();
                    }
                    goToHomeActivity();
                }
            } catch (JSONException e) {

            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
            int j = 0;
        }
    };


    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        public void onClick(View paramAnonymousView) {
            paramAnonymousView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
            switch (paramAnonymousView.getId()) {
                case R.id.new_group_up:
                    goToHomeActivity();
                    break;
                case R.id.groupSaveBtn:
                    if (isNewGroup) {
                            saveBtn.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
                            saveGroup();
                    } else {
                            editGroup();
                    }
                    break;
                case R.id.editGroupNameIcon:
                        editGroupName();
                    break;
                case R.id.deleteGroup:
                        deleteGroup(groupId);
                    break;
            }

        }
    };


    public void deleteGroup(final String groupId) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewGroupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.alert_dialog, null);
        TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setText("Delete Group");
        TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
        TextView ok = (TextView) dialogView.findViewById(R.id.ok);
        title.setTypeface(GroupDrawft.robotoBold);
        cancel.setTypeface(GroupDrawft.robotoBold);
        ok.setTypeface(GroupDrawft.robotoBold);
        RelativeLayout header_wrapper = (RelativeLayout) dialogView.findViewById(R.id.header_wrapper);
        RelativeLayout middle_wrapper = (RelativeLayout) dialogView.findViewById(R.id.middle_wrapper);
        View middle_seperator = dialogView.findViewById(R.id.middle_seperator);
        middle_seperator.setVisibility(View.GONE);
        middle_wrapper.setVisibility(View.GONE);
        settingColor();
        header_wrapper.setBackgroundColor(color1);
        cancel.setBackgroundColor(color2);
        ok.setBackgroundColor(color3);
        alertDialog.setView(dialogView);
        final Dialog mDialog = alertDialog.create();
        mDialog.setCancelable(false);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GroupDrawft.isNetworkOK(NewGroupActivity.this)) {
                    showProgressLoader();
                    mDialog.dismiss();
                    httpClientUtil.deleteGroup(deleteGroupListener, P.MOBILE_NUMBER, P.AUTH_CODE, groupId);
                }else {
                    mDialog.dismiss();
                    openConnectionDialog();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();

//        final AlertDialog.Builder deleteGroupDialog = new AlertDialog.Builder(this);
//        deleteGroupDialog.setTitle("Delete Group");
//        deleteGroupDialog.setMessage("Do you want to delete the Group?");
//        deleteGroupDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                showProgressLoader();
//                httpClientUtil.deleteGroup(deleteGroupListener, P.MOBILE_NUMBER, P.AUTH_CODE, groupId);
//
//            }
//        });
//        deleteGroupDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//
//            }
//        });
//        deleteGroupDialog.show();
    }

    public void editGroupName() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewGroupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.alert_dialog, null);

        TextView title = (TextView) dialogView.findViewById(R.id.title);
        final EditText name = (EditText) dialogView.findViewById(R.id.updateGroupName);
        TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
        TextView ok = (TextView) dialogView.findViewById(R.id.ok);
        title.setTypeface(GroupDrawft.robotoBold);
        cancel.setTypeface(GroupDrawft.robotoBold);
        ok.setTypeface(GroupDrawft.robotoBold);
        RelativeLayout header_wrapper = (RelativeLayout) dialogView.findViewById(R.id.header_wrapper);
        settingColor();
        header_wrapper.setBackgroundColor(color1);
        cancel.setBackgroundColor(color2);
        ok.setBackgroundColor(color3);
        alertDialog.setView(dialogView);
        final Dialog mDialog = alertDialog.create();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        String grpName = gm.getGroupName(groupId);
        groupNameInput.setText(grpName);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                TextView tv = (TextView) dialogView.findViewById(R.id.textCount);
                tv.setText(String.valueOf(20 - name.length()));
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }
        });
        name.append(groupNameInput.getText().toString());
        mDialog.setCancelable(false);


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GroupDrawft.isNetworkOK(NewGroupActivity.this)) {
                    if (NewGroupActivity.this.isNewGroup) {
                        if (name.getText().toString().trim().length() != 0) {
                            groupNameInput.setText(name.getText().toString().trim());
                            mDialog.dismiss();
                        } else {
                            editGroupName();
                        }

                    } else {
                        old_name = groupNameInput.getText().toString().trim();
                        new_name = name.getText().toString().trim();
                        if (!old_name.equals(new_name)) {
                            showProgressLoader();
                            httpClientUtil.editGroup(editGroupNameListener, P.MOBILE_NUMBER, P.AUTH_CODE, groupId, "edit_name", new_name);
                            groupNameInput.setText(new_name);
                            groupName = new_name;
                        }
                        mDialog.dismiss();
                    }
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
                else
                {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    mDialog.dismiss();
                    openConnectionDialog();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NewGroupActivity.this.isNewGroup) {
                    goToHomeActivity();
                } else {
                    mDialog.cancel();
                    saveBtnWrapper.setVisibility(View.GONE);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });
        mDialog.show();
//        alertDialog.setTitle("Enter Group Name");
//        LayoutInflater li = LayoutInflater.from(this);
//        final View promptsView = li.inflate(R.layout.edit_group_name, null);
//        alertDialog.setView(promptsView);
//
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//        final EditText name = (EditText) promptsView.findViewById(R.id.updateGroupName);
//        String grpName = gm.getGroupName(groupId);
//        groupNameInput.setText(grpName);
//        name.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {
//                TextView tv = (TextView) promptsView.findViewById(R.id.textCount);
//                tv.setText(String.valueOf(20 - name.length()));
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int st, int b, int c) {
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
//            }
//        });
//        name.append(groupNameInput.getText().toString());
//        alertDialog.setCancelable(false);
//
//        alertDialog.setPositiveButton("Ok",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        if (NewGroupActivity.this.isNewGroup) {
//                            if (name.getText().toString().trim().length() != 0) {
//                                groupNameInput.setText(name.getText().toString().trim());
//                                dialog.dismiss();
//                            } else {
//                                editGroupName();
//                            }
//
//                        } else {
//                            old_name = groupNameInput.getText().toString().trim();
//                            new_name = name.getText().toString().trim();
//                            if (!old_name.equals(new_name)) {
//                                //editGroupListener
//                                showProgressLoader();
//                                httpClientUtil.editGroup(editGroupNameListener, P.MOBILE_NUMBER, P.AUTH_CODE, groupId, "edit_name", new_name);
//                                groupNameInput.setText(new_name);
//                                groupName = new_name;
//                             /*   String groupName = groupNameInput.getText().toString();
//                                gm.updateGroupName(groupName, groupId);
//                                ContentValues cv = new ContentValues();
//                                cv.put(amm.COL_GROUP_GID, groupId);
//                                cv.put(amm.COL_TYPE, "editGroupName");
//                                JSONObject object = new JSONObject();
//                                try {
//                                    object.put("group_name", groupName);
//                                    cv.put(amm.COL_CONTENT, object.toString());
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                amm.iOt(getApplicationContext(), amm.TABLE_AUTO_MESSAGES, "", cv);
//                                int autoMessageId = checkGroupChange(groupId);
//                                mDrawft.addNewRecord(groupId, null, null, null, null, null, autoMessageId);*/
//
//                            }
//                            dialog.dismiss();
//                        }
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//                    }
//                });
//
//        alertDialog.setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (NewGroupActivity.this.isNewGroup) {
//                            goToHomeActivity();
//                        } else {
//                            dialog.cancel();
//                            saveBtn.setVisibility(View.GONE);
//                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//                        }
//                    }
//                });
//        alertDialog.show();
    }

    public void settingColor() {
        do {
            color1 = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
            color2 = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
            color3 = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
        }
        while (color1 == color2 || color2 == color3 || color3 == color1);

    }

    public void openConnectionDialog() {
        if (null != mDialog) mDialog.dismiss();

        GroupDrawft.fireBaseConnected = false;
        settingColor();
        AlertDialog.Builder builder = new AlertDialog.Builder(NewGroupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout dialogView = (RelativeLayout) inflater.inflate(R.layout.dialog_no_internet, null);
        TextView textTitle = (TextView) dialogView.findViewById(R.id.title);
        textTitle.setTypeface(GroupDrawft.robotoBold);
        textTitle.setBackgroundColor(color1);
        TextView retryBtn = (TextView) dialogView.findViewById(R.id.retry);
        retryBtn.setTypeface(GroupDrawft.robotoBold);
        retryBtn.setBackgroundColor(color2);
        builder.setView(dialogView);

        mDialog = builder.create();
        mDialog.setCancelable(false);
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GroupDrawft.isNetworkOK(NewGroupActivity.this)) {
                    mDialog.dismiss();
                    onResume();
                } else {
                    openConnectionDialog();
                }
            }
        });
        mDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                }
                return true;
            }
        });
        mDialog.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.push_down_out, R.anim.push_down_in);
        onNewIntent(getIntent());
        setContentView(R.layout.activity_new_group);
        Firebase.setAndroidContext(this);
        //ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        initViews();
        getCoverPhoto();
        initListViews();
        if (this.isNewGroup) {
            isAdmin = 1;
            this.addedListAdapter.setAdmin(isAdmin);
            deleteGroupBtn.setVisibility(View.INVISIBLE);
            blocked.setVisibility(View.INVISIBLE);
            clearDrawfts.setVisibility(View.INVISIBLE);
            saveBtnWrapper.setVisibility(View.VISIBLE);
            editGroupName();
            loadContacts();
            getYourInfo();
        } else {
            getYourInfo();
            int val = getGroupMembers();
            if (val == 0) {
                this.addListView.setVisibility(View.GONE);
                addMembersBtn.setVisibility(View.GONE);
                saveBtnWrapper.setVisibility(View.GONE);
                sep1.setVisibility(View.INVISIBLE);
                sep2.setVisibility(View.INVISIBLE);
            } else {
                loadContacts();
                saveBtnWrapper.setVisibility(View.GONE);
            }
        }


    }

    protected void onResume() {
        super.onResume();
        sortLists();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDialog != null)
            mDialog.dismiss();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mDialog != null)
                mDialog.dismiss();
            this.__firebaseUtil.destroy();
            this.__firebaseUtil = null;
            this.listAdapter.destroy();
            this.listAdapter = null;
            this.__fileUtil = null;
            this.addedListAdapter = null;
            this.addListAdapter = null;
        } catch (Exception e) {
        }

    }

    private void sortLists() {
        Collections.sort(addedListAdapter.list, new Comparator<ContactTile>() {
            @Override
            public int compare(ContactTile lhs, ContactTile rhs) {
                if (rhs.getGroupName().toUpperCase().equals("YOU")) {
                    return 1;
                } else if (rhs.getGroupName().toUpperCase().equals("YOU (ADMIN)")) {
                    return 1;
                } else {
                    return lhs.getGroupName().toUpperCase().compareTo(rhs.getGroupName().toUpperCase());
                }
            }
        });

        Collections.sort(addListAdapter.list, new Comparator<ContactTile>() {
            @Override
            public int compare(ContactTile lhs, ContactTile rhs) {
                return lhs.getGroupName().toUpperCase().compareTo(rhs.getGroupName().toUpperCase());
            }
        });
    }

    private void getCoverPhoto() {
        if (!isNewGroup) {
            DrawftModel dm = DrawftModel.getR(getApplicationContext());
            ArrayList<DrawftBean> covers = dm.getGroupDrawfts(groupId);
            ArrayList<String> arr = new ArrayList<String>();
            ArrayList<String> dimens = new ArrayList<>();
            DrawftBean dBean;
            for (int i = 0; i < covers.size(); i++) {
                dBean = covers.get(i);
                if (dBean.getFileName() != null) {
                    // arr.add(__fileUtil.getFilePath(dBean.getFileName()));
                    arr.add(__fileUtil.getFilePath1(groupId + "/" + dBean.getFileName()));
                    dimens.add(dBean.getDimensions());
                }
            }

            ContactTile tile = new ContactTile();
            tile.setCoverPic1(arr);
            tile.setDimensions(dimens);
            tile.setGroupId(groupId);
            listAdapter.setCoverDrawfts(groupPhoto1, groupPhoto2, groupPhoto3, tile);
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {

            if (extras.getBoolean("isNewGroup")) {
                this.isNewGroup = true;
            } else {
                this.isNewGroup = false;
                groupId = extras.getString("groupId");
                groupName = extras.getString("groupName");
                isGroup = extras.getBoolean("isGroup");
            }
            getIntent().removeExtra("isNewGroup");
        }
    }


    private void initViews() {
        final GroupModel gm = new GroupModel(NewGroupActivity.this);
        leftBtn = (TextView) findViewById(R.id.new_group_up);
        saveBtn = (TextView) findViewById(R.id.groupSaveBtn);
        saveBtnWrapper = (LinearLayout) findViewById(R.id.groupSaveBtnWrap);
        addedMembersBtn = (TextView) findViewById(R.id.addedMembers);
        addMembersBtn = (TextView) findViewById(R.id.addMembers);
        groupNameInput = (TextView) findViewById(R.id.groupName);
        editGroupNameIcon = (TextView) findViewById(R.id.editGroupNameIcon);
        deleteGroupBtn = (TextView) findViewById(R.id.deleteGroup);
        memberCount = (TextView) findViewById(R.id.memberCount);
        this.listAdapter = new CommunicationListAdapter(this);
        groupPhoto1 = (SimpleDraweeView) findViewById(R.id.groupCoverPhoto1);
        groupPhoto2 = (SimpleDraweeView) findViewById(R.id.groupCoverPhoto2);
        groupPhoto3 = (SimpleDraweeView) findViewById(R.id.groupCoverPhoto3);
        blocked = (TextView) findViewById(R.id.blockGroup);
        clearDrawfts = (TextView) findViewById(R.id.clearDrawfts);
        loadingSave = (ProgressBar) findViewById(R.id.loadingSave);
        sep1 = findViewById(R.id.seperator1);
        sep2 = findViewById(R.id.seperator2);

        newgroup1 = findViewById(R.id.newgroup1);
        newgroup2 = findViewById(R.id.newgroup2);
        newgroup3 = findViewById(R.id.newgroup3);
        newGroupContainer = findViewById(R.id.newGroupMain);
        int color = Color.parseColor(myColorList.get(new Random().nextInt(34)));
        newgroup1.setBackgroundColor(color);
        newgroup3.setBackgroundColor(color);
        /*addedMembersBtn.setTextColor(color);
        addMembersBtn.setTextColor(color);*/

        if (!isNewGroup) {
            if (gm.getBlocked(groupId) == 0) {
                blocked.setTextColor(getResources().getColor(R.color.white));
            } else {
                blocked.setTextColor(getResources().getColor(R.color.blocked));
            }
        }


        leftBtn.setTypeface(GroupDrawft.fontFeather);
        saveBtn.setTypeface(GroupDrawft.fontFeather);
        deleteGroupBtn.setTypeface(GroupDrawft.fontFeather);
        blocked.setTypeface(GroupDrawft.fontFeather);
        editGroupNameIcon.setTypeface(GroupDrawft.fontFeather);


        addedMembersBtn.setTypeface(GroupDrawft.robotoBold);
        addMembersBtn.setTypeface(GroupDrawft.robotoBold);

        clearDrawfts.setTypeface(GroupDrawft.fontFeather);
        groupNameInput.setTypeface(GroupDrawft.robotoLight);

        if (!isNewGroup) {
            blocked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewGroupActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.alert_dialog, null);
                    TextView title = (TextView) dialogView.findViewById(R.id.title);
                    if (gm.getBlocked(groupId) == 0) {
                        title.setText("Block Chat");
                    } else {
                        title.setText("Unblock Chat");
                    }
                    TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
                    TextView ok = (TextView) dialogView.findViewById(R.id.ok);
                    title.setTypeface(GroupDrawft.robotoBold);
                    cancel.setTypeface(GroupDrawft.robotoBold);
                    ok.setTypeface(GroupDrawft.robotoBold);
                    settingColor();
                    RelativeLayout header_wrapper = (RelativeLayout) dialogView.findViewById(R.id.header_wrapper);
                    RelativeLayout middle_wrapper = (RelativeLayout) dialogView.findViewById(R.id.middle_wrapper);
                    View middle_seperator = dialogView.findViewById(R.id.middle_seperator);
                    middle_seperator.setVisibility(View.GONE);
                    middle_wrapper.setVisibility(View.GONE);
                    header_wrapper.setBackgroundColor(color1);
                    cancel.setBackgroundColor(color2);
                    ok.setBackgroundColor(color3);
                    alertDialog.setView(dialogView);
                    final Dialog mDialog = alertDialog.create();
                    mDialog.setCancelable(false);

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(GroupDrawft.isNetworkOK(NewGroupActivity.this)) {
                                NewGroupActivity.this.__firebaseUtil = new FirebaseUtil(getApplicationContext(), P.FB_TOKEN);
                                if (gm.getBlocked(groupId) == 0) {
                                    gm.setBlocked(1, groupId);
                                    blocked.setTextColor(getResources().getColor(R.color.blocked));
                                    NewGroupActivity.this.__firebaseUtil.addToBlockList(P.MOBILE_NUMBER, groupId, "group");
                                } else {
                                    gm.setBlocked(0, groupId);
                                    blocked.setTextColor(getResources().getColor(R.color.white));
                                    NewGroupActivity.this.__firebaseUtil.removeFromBlockList(P.MOBILE_NUMBER, groupId);
                                }
                                mDialog.dismiss();
                            }
                            else
                            {
                                mDialog.dismiss();
                                openConnectionDialog();
                            }
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });
                    mDialog.show();

//                    v.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
//                    final AlertDialog.Builder blockedDialog = new AlertDialog.Builder(NewGroupActivity.this);
//                    if(gm.getBlocked(groupId)==0)
//                    {
//                        blockedDialog.setTitle("Block Conversation");
//                        blockedDialog.setMessage("Do you want to Block this Conversation?");
//                    }
//                    else
//                    {
//                        blockedDialog.setTitle("Unblock Conversion");
//                        blockedDialog.setMessage("Do you want to Unblock the Conversation");
//                    }
//                    blockedDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            if (gm.getBlocked(groupId) == 0) {
//                                gm.setBlocked(1, groupId);
//                                blocked.setTextColor(getResources().getColor(R.color.blocked));
//                                NewGroupActivity.this.__firebaseUtil.addToBlockList(P.MOBILE_NUMBER, groupId, "group");
//                            } else {
//                                gm.setBlocked(0, groupId);
//                                blocked.setTextColor(getResources().getColor(R.color.icon_color));
//                                NewGroupActivity.this.__firebaseUtil.removeFromBlockList(P.MOBILE_NUMBER, groupId);
//                            }
//
//                        }
//                    });
//                    blockedDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//
//                        }
//                    });
//                    blockedDialog.show();
                }
            });
        }

        clearDrawfts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewGroupActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.alert_dialog, null);
                TextView title = (TextView) dialogView.findViewById(R.id.title);
                title.setText("Clear All Drawfts");
                TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
                TextView ok = (TextView) dialogView.findViewById(R.id.ok);
                title.setTypeface(GroupDrawft.robotoBold);
                cancel.setTypeface(GroupDrawft.robotoBold);
                ok.setTypeface(GroupDrawft.robotoBold);
                settingColor();
                RelativeLayout header_wrapper = (RelativeLayout) dialogView.findViewById(R.id.header_wrapper);
                RelativeLayout middle_wrapper = (RelativeLayout) dialogView.findViewById(R.id.middle_wrapper);
                View middle_seperator = dialogView.findViewById(R.id.middle_seperator);
                middle_seperator.setVisibility(View.GONE);
                middle_wrapper.setVisibility(View.GONE);
                header_wrapper.setBackgroundColor(color1);
                cancel.setBackgroundColor(color2);
                ok.setBackgroundColor(color3);
                alertDialog.setView(dialogView);
                final Dialog mDialog = alertDialog.create();
                mDialog.setCancelable(false);

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showProgressLoader();
                        DrawftModel dm = new DrawftModel(NewGroupActivity.this);
                        dm.delDrawfts(groupId);
                        dm.addNewRecord(groupId, P.MOBILE_NUMBER, "", System.currentTimeMillis() + "", "", -1, 1);
                        DrawftModel.closeDBConnection(dm);

                        __fileUtil.DeleteRecursive(groupId);

                        /*ImageLoader.getInstance().clearDiskCache();
                        ImageLoader.getInstance().clearMemoryCache();*/

                        goToHomeActivity();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
                mDialog.show();

//                final AlertDialog.Builder clearDrawftsDialog = new AlertDialog.Builder(NewGroupActivity.this);
//                clearDrawftsDialog.setTitle("Clear All Drawfts");
//                clearDrawftsDialog.setMessage("Do you want to Clear the Drawfts?");
//                clearDrawftsDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        showProgressLoader();
//                        DrawftModel dm = new DrawftModel(NewGroupActivity.this);
//                        dm.delDrawfts(groupId);
//                        dm.addNewRecord(groupId, P.MOBILE_NUMBER, "", System.currentTimeMillis() + "", "", -1);
//                        dm.close();
//
//                        __fileUtil.DeleteRecursive(groupId);
//
//                        ImageLoader.getInstance().clearDiskCache();
//                        ImageLoader.getInstance().clearMemoryCache();
//
//                        goToHomeActivity();
//
//                    }
//                });
//                clearDrawftsDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//
//                    }
//                });
//                clearDrawftsDialog.show();
            }
        });


        leftBtn.setOnClickListener(btnClickListener);
        saveBtn.setOnClickListener(btnClickListener);
        deleteGroupBtn.setOnClickListener(btnClickListener);
        editGroupNameIcon.setOnClickListener(btnClickListener);
        this.__fileUtil = new FileUtil(this);
    }

    private void goToHomeActivity() {
        if (this.isNewGroup) {
            Intent callIntent = new Intent(getApplicationContext(), CommunicationListActivity.class);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(callIntent);
            this.finish();
        } else {
            Intent callIntent = new Intent(getApplicationContext(), DrawingPadActivity.class);
            Bundle extras = new Bundle();
            extras.putString("groupId", groupId);
            extras.putString("groupName", groupName);
            extras.putBoolean("isGroup", isGroup);
            extras.putString("caller", "profile");
            callIntent.putExtras(extras);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(callIntent);
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToHomeActivity();
    }

    public void saveGroup() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewGroupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.alert_dialog, null);
        TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setText("Create Group");
        TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
        TextView ok = (TextView) dialogView.findViewById(R.id.ok);
        title.setTypeface(GroupDrawft.robotoBold);
        cancel.setTypeface(GroupDrawft.robotoBold);
        ok.setTypeface(GroupDrawft.robotoBold);
        RelativeLayout header_wrapper = (RelativeLayout) dialogView.findViewById(R.id.header_wrapper);
        RelativeLayout middle_wrapper = (RelativeLayout) dialogView.findViewById(R.id.middle_wrapper);
        View middle_seperator = dialogView.findViewById(R.id.middle_seperator);
        middle_seperator.setVisibility(View.GONE);
        middle_wrapper.setVisibility(View.GONE);
        settingColor();
        header_wrapper.setBackgroundColor(color1);
        cancel.setBackgroundColor(color2);
        ok.setBackgroundColor(color3);
        alertDialog.setView(dialogView);
        final Dialog mDialog = alertDialog.create();
        mDialog.setCancelable(false);
        final String groupName = this.groupNameInput.getText().toString();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupName == null && groupName.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Group Name cannot be Empty", Toast.LENGTH_SHORT).show();
                } else {
                    mDialog.dismiss();
                    if(GroupDrawft.isNetworkOK(NewGroupActivity.this)) {
                        if(groupName != null && groupName.length() !=0) {
                            showProgressLoader();
                            saveGroupToServer(groupName);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Group Name cannot be Empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        openConnectionDialog();
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    public void showProgressLoader() {
        loadingSave.setVisibility(View.VISIBLE);
        newGroupContainer.setVisibility(View.VISIBLE);
        // newGroupContainer.setBackgroundColor(getResources().getColor(R.color.transparent_color));
        /*newgroup1.setBackgroundColor(getResources().getColor(R.color.transparent_color));
        newgroup2.setBackgroundColor(getResources().getColor(R.color.transparent_color));
        newgroup3.setBackgroundColor(getResources().getColor(R.color.transparent_color));*/

    }

    public void hideProgressLoader() {
        loadingSave.setVisibility(View.GONE);
        newGroupContainer.setVisibility(View.GONE);
        //newGroupContainer.setBackgroundColor(getResources().getColor(R.color.tile_background));
        /*newgroup1.setBackgroundColor(getResources().getColor(R.color.tile_background));
        newgroup2.setBackgroundColor(getResources().getColor(R.color.home_screen_background));
        newgroup3.setBackgroundColor(getResources().getColor(R.color.light_grey));*/
    }

    public void saveGroupToServer(String groupName) {
        String members = "";
        for (int i = 0; i < this.addedListAdapter.getCount(); i++) {
            ContactTile tile = this.addedListAdapter.getItem(i);
            if (!P.MOBILE_NUMBER.equals(tile.getMobileNumber())) {
                if (members.isEmpty()) {
                    members += tile.getMobileNumber();
                } else {
                    members = members + "||" + tile.getMobileNumber();
                }
            }
        }
        if(GroupDrawft.isNetworkOK(NewGroupActivity.this)) {
            httpClientUtil.addNewGroup(saveGroupListener, P.MOBILE_NUMBER, P.AUTH_CODE, groupName, members);
        }else{
            openConnectionDialog();
        }
    }

    public void editGroup() {

        for (int i = 0; i < this.addedListAdapter.getCount(); i++) {
            ContactTile tile = this.addedListAdapter.getItem(i);
            if (!P.MOBILE_NUMBER.equals(tile.getMobileNumber())) {
                if (curMembers.isEmpty()) {
                    curMembers += tile.getMobileNumber();
                } else {
                    curMembers = curMembers + "||" + tile.getMobileNumber();
                }
            }
        }
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewGroupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.alert_dialog, null);
        TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setText("Accept Changes");
        TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
        TextView ok = (TextView) dialogView.findViewById(R.id.ok);
        title.setTypeface(GroupDrawft.robotoBold);
        cancel.setTypeface(GroupDrawft.robotoBold);
        ok.setTypeface(GroupDrawft.robotoBold);
        RelativeLayout header_wrapper = (RelativeLayout) dialogView.findViewById(R.id.header_wrapper);
        RelativeLayout middle_wrapper = (RelativeLayout) dialogView.findViewById(R.id.middle_wrapper);
        View middle_seperator = dialogView.findViewById(R.id.middle_seperator);
        middle_seperator.setVisibility(View.GONE);
        middle_wrapper.setVisibility(View.GONE);
        settingColor();
        header_wrapper.setBackgroundColor(color1);
        cancel.setBackgroundColor(color2);
        ok.setBackgroundColor(color3);
        alertDialog.setView(dialogView);
        final Dialog mDialog = alertDialog.create();
        mDialog.setCancelable(false);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GroupDrawft.isNetworkOK(NewGroupActivity.this)) {
                    showProgressLoader();
                    httpClientUtil.editGroup(editGroupMembersListener, P.MOBILE_NUMBER, P.AUTH_CODE, groupId, "edit_members", curMembers);
                    mDialog.dismiss();
                } else {
                    openConnectionDialog();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    public boolean editProgress() {
        if (!this.isNewGroup) {
            Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.right_to_left);
            saveBtnWrapper.setVisibility(View.VISIBLE);
            saveBtnWrapper.setAnimation(slideDown);
            return false;
        }
        return true;
    }

    public void initListViews() {
        this.addedListView = (ListView) findViewById(R.id.addedMembersList);
        this.addListView = (ListView) findViewById(R.id.addMembersList);
        this.addedListAdapter = new MemberListAdapter(this, "remove");
        this.addListAdapter = new MemberListAdapter(this, "add");

        this.addedListView.setAdapter(this.addedListAdapter);
        this.addListView.setAdapter(this.addListAdapter);
        this.addedListView.setSmoothScrollbarEnabled(true);
        this.addListView.setSmoothScrollbarEnabled(true);
        this.addListAdapter.setAddListener(new MyOnMemberAddListener());
        this.addedListAdapter.setRemoveListener(new MyOnMemberRemoveListener());

    }

    public void loadContacts() {
        final NewGroupActivity targetActivity = this;
        GroupDrawft.runOnBackground(targetActivity, new Runnable() {
            @Override
            public void run() {

                final ArrayList<ContactTile> tiles = new ArrayList<ContactTile>();
                ContactModel _db = ContactModel.getR(targetActivity);
                Cursor cr = _db.getAppUsingContacts();

                if (cr != null && cr.getCount() > 0 && cr.moveToFirst()) {
                    do {

                        ContactTile tile = new ContactTile();
                        tile.setMobileNumber(cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NUMBER)));
                        tile.setGroupName(cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NAME)));
                        tile.setGroupId(cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NAME)));
                        tile.setIsGroup(false);

                        tile_name.setMobileNumber(cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NUMBER)));
                        tile_name.setGroupName(cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NAME)));
                        if (tile.getGroupName() == null || tile.getGroupName().isEmpty()) {
                            tile.setGroupName(tile.getMobileNumber());
                        }
                        if (!tile.getMobileNumber().equals(P.MOBILE_NUMBER)) {
                            try {
                                cons.put(tile.getMobileNumber(), tile.getGroupName());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (!memberList.contains(tile.getMobileNumber())) {
                                ContactModel cm = new ContactModel(getApplicationContext());
                                if (cm.getAppUsing(tile.getMobileNumber()) == 1) {
                                    tiles.add(tile);
                                }
                            }
                        }
                    } while (cr.moveToNext());
                    cr.close();
                    ContactModel.closeDBConnection(_db);
                    //addListView.setEmptyView("");

                    targetActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            targetActivity.addListAdapter.concatList(tiles);
                        }
                    });
                }
            }
        });

    }

    public int getGroupMembers() {
        groupNameInput.setText(groupName);
        GroupMemberModel groupMembersDb = GroupMemberModel.getR(this);
        Cursor cr = groupMembersDb.getGroupMembersWithDetails(this.groupId);
        if (cr != null && cr.getCount() > 0 && cr.moveToFirst()) {
            do {
                ContactTile tile = new ContactTile();
                tile.setMobileNumber(cr.getString(cr.getColumnIndexOrThrow(GroupMemberModel.COL_MEMBER_ID)));

                int admin = cr.getInt(cr.getColumnIndexOrThrow(GroupMemberModel.COL_IS_ADMIN));

                if (tile.getMobileNumber().equals(P.MOBILE_NUMBER) && admin == 1) {
                    isAdmin = 1;
                    this.addedListAdapter.setAdmin(isAdmin);
                } else {
                    memberList.add(tile.getMobileNumber());
                }

                if (!tile.getMobileNumber().equals(P.MOBILE_NUMBER)) {
                    this.addedListAdapter.addItem(tile);
                    String uName = cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NAME));
                    if (uName == null || uName.isEmpty()) {
                        tile.setGroupName(tile.getMobileNumber());
                        tile.setGroupId(tile.getMobileNumber());
                    } else {
                        tile.setGroupName(cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NAME)));
                        tile.setGroupId(tile.getMobileNumber());
                    }
                    if (admin == 1) {
                        tile.setGroupName(tile.getGroupName() + " (Admin)");
                    }
                    tile.setIsGroup(false);
                } else {
                    if (admin == 1) {
                        ContactTile yourTile = this.addedListAdapter.getItem(0);
                        yourTile.setGroupName(yourTile.getGroupName() + " (Admin)");
                    }
                }

            } while (cr.moveToNext());
            cr.close();
            GroupMemberModel.closeDBConnection(groupMembersDb);
        }
        memberCount.setText(this.addedListAdapter.getCount() + " members");
        return isAdmin;
    }

    private void getYourInfo() {
        ContactTile tile = new ContactTile();
        tile.setMobileNumber(GroupDrawft.P.MOBILE_NUMBER);
        tile.setGroupName("You");
        tile.setGroupId(GroupDrawft.P.MOBILE_NUMBER);
        tile.setIsGroup(false);
        this.addedListAdapter.addItem(tile);
    }

    public void addMember(int pos) {
        editProgress();
        ContactTile tile = this.addListAdapter.getItem(pos);
        addedListAdapter.addItem(tile);
        Toast.makeText(getApplicationContext(), tile.getGroupName() + " added.", Toast.LENGTH_SHORT).show();
        this.addListAdapter.removeItem(pos);
    }

    public void removeMember(int pos) {
        editProgress();
        ContactTile tile = this.addedListAdapter.getItem(pos);
        /*ContactModel cm = new ContactModel(getApplicationContext());
        if (cm.getAppUsing(tile.getMobileNumber()) == 1) {

        }*/
        addListAdapter.addItem(tile);
        Toast.makeText(getApplicationContext(), tile.getGroupName() + " removed.", Toast.LENGTH_SHORT).show();
        this.addedListAdapter.removeItem(pos);

    }


    class MyOnMemberAddListener implements MemberListAdapter.OnAddListener {
        MyOnMemberAddListener() {
        }

        public void onAddMember(int pos) {
            addMember(pos);
        }
    }

    class MyOnMemberRemoveListener implements MemberListAdapter.OnRemoveListener {
        MyOnMemberRemoveListener() {
        }

        public void onRemoveMember(int pos) {
            removeMember(pos);
        }
    }
}
