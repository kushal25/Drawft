package com.drawft;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drawft.data.ContactTile;
import com.drawft.model.contacts.ContactModel;
import com.drawft.model.drawfts.DrawftBean;
import com.drawft.model.drawfts.DrawftModel;
import com.drawft.util.CommunicationListAdapter;
import com.drawft.util.FileUtil;
import com.drawft.util.FirebaseUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class UserProfileActivity extends Activity {
    private String userId = "", userName = "", contactNum = "";
    private SimpleDraweeView userPhoto1, userPhoto2, userPhoto3;
    protected TextView leftBtn = null, username, tileNumber;
    protected TextView blocked, clearDrawfts;
    protected CommunicationListAdapter listAdapter = null;
    protected FirebaseUtil __firebaseUtil = null;
    protected FileUtil __fileUtil = null;
    ProgressBar loader = null;
    private List<String> myColorList = GroupDrawft.getColorList();
    public int color1, color2, color3;
    public Dialog mDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.push_down_out, R.anim.push_down_in);
        onNewIntent(getIntent());
        setContentView(R.layout.activity_user_profile);
        Firebase.setAndroidContext(this);
        // ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        initViews();
        getCoverPhoto();
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
        } catch (Exception e) {
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            userId = extras.getString("groupId");
            userName = extras.getString("groupName");
            contactNum = extras.getString("contactNum");
        }
    }

    private void initViews() {
        leftBtn = (TextView) findViewById(R.id.user_profile_up);

        this.listAdapter = new CommunicationListAdapter(this);
        final ContactModel cm = new ContactModel(UserProfileActivity.this);

        userPhoto1 = (SimpleDraweeView) findViewById(R.id.userCoverPhoto1);
        userPhoto2 = (SimpleDraweeView) findViewById(R.id.userCoverPhoto2);
        userPhoto3 = (SimpleDraweeView) findViewById(R.id.userCoverPhoto3);
        username = (TextView) findViewById(R.id.userName);
        tileNumber = (TextView) findViewById(R.id.tileNumber);
        tileNumber.setText(contactNum);
        blocked = (TextView) findViewById(R.id.blockUser);
        clearDrawfts = (TextView) findViewById(R.id.clearDrawfts);
        loader = (ProgressBar) findViewById(R.id.loader);
        if (cm.getBlocked(contactNum) == 0) {
            blocked.setTextColor(getResources().getColor(R.color.white));
        } else {
            blocked.setTextColor(getResources().getColor(R.color.blocked));
        }
        username.setText(userName);
        username.setTypeface(GroupDrawft.robotoLight);
        leftBtn.setTypeface(GroupDrawft.fontFeather);
        blocked.setTypeface(GroupDrawft.fontFeather);
        clearDrawfts.setTypeface(GroupDrawft.fontFeather);
        this.__firebaseUtil = new FirebaseUtil(this, GroupDrawft.P.FB_TOKEN);

        clearDrawfts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserProfileActivity.this);
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
                        DrawftModel dm = new DrawftModel(UserProfileActivity.this);
                        dm.delDrawfts(userId);
                        dm.addNewRecord(userId, GroupDrawft.P.MOBILE_NUMBER, "", System.currentTimeMillis() + "", "", -1, 1);
                        DrawftModel.closeDBConnection(dm);

                        __fileUtil.DeleteRecursive(userId);

                       /* ImageLoader.getInstance().clearDiskCache();
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

//                final AlertDialog.Builder clearDrawftsDialog = new AlertDialog.Builder(UserProfileActivity.this);
//                clearDrawftsDialog.setTitle("Clear All Drawfts");
//                clearDrawftsDialog.setMessage("Do you want to Clear the Drawfts?");
//                clearDrawftsDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        showProgressLoader();
//                        DrawftModel dm = new DrawftModel(UserProfileActivity.this);
//                        dm.delDrawfts(userId);
//                        dm.addNewRecord(userId, GroupDrawft.P.MOBILE_NUMBER, "", System.currentTimeMillis() + "", "", -1);
//                        dm.close();
//
//                        __fileUtil.DeleteRecursive(userId);
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

        blocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserProfileActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.alert_dialog, null);
                TextView title = (TextView) dialogView.findViewById(R.id.title);
                if (cm.getBlocked(contactNum) == 0) {
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
                        if(GroupDrawft.isNetworkOK(UserProfileActivity.this)) {
                            if (cm.getBlocked(contactNum) == 0) {
                                cm.setBlocked(1, contactNum);
                                blocked.setTextColor(getResources().getColor(R.color.blocked));
                                UserProfileActivity.this.__firebaseUtil.addToBlockList(GroupDrawft.P.MOBILE_NUMBER, contactNum, "user");
                            } else {
                                cm.setBlocked(0, contactNum);
                                blocked.setTextColor(getResources().getColor(R.color.white));
                                UserProfileActivity.this.__firebaseUtil.removeFromBlockList(GroupDrawft.P.MOBILE_NUMBER, contactNum);
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

//                final AlertDialog.Builder blockedDialog = new AlertDialog.Builder(UserProfileActivity.this);
//                if (cm.getBlocked(contactNum) == 0) {
//                    blockedDialog.setTitle("Block Conversation");
//                    blockedDialog.setMessage("Do you want to Block the Conversation?");
//                }
//                else {
//                    blockedDialog.setTitle("Unblock Conversation");
//                    blockedDialog.setMessage("Do you want to Unblock the Conversation?");
//                }
//                blockedDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if (cm.getBlocked(contactNum) == 0) {
//                            cm.setBlocked(1, contactNum);
//                            blocked.setTextColor(getResources().getColor(R.color.blocked));
//                            UserProfileActivity.this.__firebaseUtil.addToBlockList(GroupDrawft.P.MOBILE_NUMBER, contactNum, "user");
//
//                        } else {
//                            cm.setBlocked(0, contactNum);
//                            blocked.setTextColor(getResources().getColor(R.color.icon_color));
//                            UserProfileActivity.this.__firebaseUtil.removeFromBlockList(GroupDrawft.P.MOBILE_NUMBER, contactNum);
//                        }
//                    }
//                });
//                blockedDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//
//
//                    }
//                });
//                blockedDialog.show();
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHomeActivity();
            }
        });
        this.__fileUtil = new FileUtil(this);
        int color = Color.parseColor(myColorList.get(new Random().nextInt(34)));
        View view1 = findViewById(R.id.coverImages);
        View view3 = findViewById(R.id.relativeLayout);
        view1.setBackgroundColor(color);
        view3.setBackgroundColor(color);
    }


    private void goToHomeActivity() {
        Intent callIntent = new Intent(getApplicationContext(), DrawingPadActivity.class);
        Bundle extras = new Bundle();
        extras.putString("groupId", contactNum);
        extras.putString("groupName", userName);
        extras.putString("caller", "profile");
        //extras.putString("contactNum",contactNum);
        callIntent.putExtras(extras);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(callIntent);
        this.finish();
    }

    private void showProgressLoader() {
        loader.setVisibility(View.VISIBLE);
        View view1 = findViewById(R.id.coverImages);
        View view2 = findViewById(R.id.useractivity1);
        View view3 = findViewById(R.id.relativeLayout);
        view1.setBackgroundColor(getResources().getColor(R.color.transparent_color));
        view2.setBackgroundColor(getResources().getColor(R.color.transparent_color));
        view3.setBackgroundColor(getResources().getColor(R.color.transparent_color));

    }

    private void getCoverPhoto() {
        DrawftModel dm = DrawftModel.getR(getApplicationContext());
        ArrayList<DrawftBean> covers = dm.getGroupDrawfts(userId);
        ArrayList<String> arr = new ArrayList<String>();
        ArrayList<String> dimens = new ArrayList<>();
        DrawftBean dBean;
        for (int i = 0; i < covers.size(); i++) {
            dBean = covers.get(i);
            // arr.add(__fileUtil.getFilePath(dBean.getFileName()));
            arr.add(__fileUtil.getFilePath1(userId + "/" + dBean.getFileName()));
            dimens.add(dBean.getDimensions());
        }
        ContactTile tile = new ContactTile();
        tile.setCoverPic1(arr);
        tile.setDimensions(dimens);
        tile.setGroupId(userId);

        listAdapter.setCoverDrawfts(userPhoto1, userPhoto2, userPhoto3, tile);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToHomeActivity();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
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
                if (GroupDrawft.isNetworkOK(UserProfileActivity.this)) {
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
}
