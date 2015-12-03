package com.drawft;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drawft.GroupDrawft.P;
import com.drawft.data.ContactTile;
import com.drawft.data.DrawftInfo;
import com.drawft.data.DrawingData;
import com.drawft.data.SingleDrawft;
import com.drawft.model.autoMessages.AutoMessagesBean;
import com.drawft.model.autoMessages.AutoMessagesModel;
import com.drawft.model.contacts.ContactModel;
import com.drawft.model.drawfts.DrawftBean;
import com.drawft.model.drawfts.DrawftModel;
import com.drawft.model.groups.GroupModel;
import com.drawft.model.members.GroupMemberModel;
import com.drawft.service.HttpClientUtil;
import com.drawft.util.AmbilWarnaDialog;
import com.drawft.util.CustomAdapter;
import com.drawft.util.FileUtil;
import com.drawft.util.FirebaseUtil;
import com.drawft.util.PrepareDrawft;
import com.drawft.view.DrawingPad;
import com.firebase.client.Firebase;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class DrawingPadActivity extends Activity {

    protected LinearLayout __drawingPadLayout = null;
    protected RelativeLayout __relativeLayoutList = null;
    protected LinearLayout __linearLayoutPad = null;
    protected DrawingPad __drawingPadView = null;
    protected DrawingData __serializedData = null;
    protected DrawftInfo __drawftInfo = null;
    protected FileUtil __fileUtil = null;
    protected FirebaseUtil __firebaseUtil = null;
    protected PrepareDrawft __prepareDrawft = null;
    protected CustomAdapter adapter = null;
    protected ListView listView = null;
    int autoMessageId = 0;
    Button colorButton1 = null;
    Button colorButton2 = null;
    Button colorButton3 = null;
    Button colorButton4 = null;
    Button sendBtn = null;
    Button undoBtn = null;
    Button lockBtn = null;
    Button unLockBtn = null;
    TextView profileIcon;
    private long unlockedAt = 0;
    private boolean isGroup = false, isBlocked = false;
    private String joinedAt = "";
    private String groupId = null, groupName = null, otherNumber = null, contactNum = null;
    private static final String TAG = DrawingPadActivity.class.getSimpleName();
    int offset_inc;
    int clickCount = 0;
    public ArrayList<SingleDrawft> arr = new ArrayList<>();
    JSONObject obj = new JSONObject();
    ViewGroup headerview;
    boolean loadedNew = false, restoreSession = true;
    ArrayList<String> offlineDrawfts = new ArrayList<String>();
    TextView lastSeen;
    private List<String> myColorList = GroupDrawft.getColorList();
    public int color1, color2, color3;
    public Dialog mDialog = null;
    public int drawingPadFlag = 0;

    private static final int minBitmapW = 200, minBitmapH = 200;
    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        public void onClick(View paramAnonymousView) {
            paramAnonymousView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
            switch (paramAnonymousView.getId()) {

                case R.id.icon_undo_drawft:
                    DrawingPadActivity.this.undoCurrentDrawft();
                    break;
                case R.id.icon_lock_pad:
                    if (DrawingPadActivity.this.__drawingPadView != null) {
                        hideDrawingPad();
                    }
                    break;
                case R.id.icon_unlock_pad:

                    if (DrawingPadActivity.this.__drawingPadView != null) {
                        showDrawingPad();
                    }
                    break;
                case R.id.icon_send_drawft:
                    DrawingPadActivity.this.sendDrawft();
                    break;
                case R.id.padTopBarProfileIcon:
                    if (isGroup)
                        startGroupActivity();
                    else
                        startUserActivity();
                    break;
            }

        }
    };

    HttpClientUtil httpClientUtil = new HttpClientUtil();
    JsonHttpResponseHandler notifyGroupListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            int i = 0;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.getString("caller") != null && extras.getString("caller").equals("profile")) {
                //overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
            } else {
                overridePendingTransition(R.anim.push_down_out, R.anim.push_down_in);
            }
        }
        setContentView(R.layout.drawing_pad_activity);
        onNewIntent(getIntent());


        /*ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .denyCacheImageMultipleSizesInMemory()
                .writeDebugLogs() // Remove for release app
                .build();
        ImageLoader.getInstance().init(config);*/
        Firebase.setAndroidContext(this);
        initViews();
        loadInitialList();
        //Log.d(TAG, "Activity on Create");

    }

    protected void onPause() {
        super.onPause();
        try {
            GCMIntentService.currentDrawingPad = null;
            this.__firebaseUtil.close(this.isGroup, groupId, this.otherNumber);
            FirebaseUtil.goOffline();

        } catch (Exception e) {

        }

        //Log.d(TAG, "Activity on pause");
    }

    protected void onResume() {
        super.onResume();
        getOfflineDrawfts();
        GCMIntentService.currentDrawingPad = groupId;
        FirebaseUtil.goOnline();
        try {
            if (this.isBlocked) {
                return;
            }
            DrawftModel dm = new DrawftModel(getApplicationContext());
            String lastSynced = dm.getLastSyncedDrawft(this.groupId);
            if (lastSynced.isEmpty() && !this.joinedAt.isEmpty()) {
                lastSynced = this.joinedAt;
            } else if (lastSynced.isEmpty()) {
                lastSynced = "0";
            }
            String blockedAt;
            if (this.isGroup) {
                GroupModel gm = new GroupModel(DrawingPadActivity.this);
                blockedAt = gm.getBlockedAt(this.groupId);
                GroupModel.closeDBConnection(gm);
            } else {
                ContactModel cm = new ContactModel(DrawingPadActivity.this);
                blockedAt = cm.getBlockedAt(this.otherNumber);
                ContactModel.closeDBConnection(cm);
            }
            unlockedAt = Long.parseLong(blockedAt);
            if (Long.parseLong(blockedAt) > Long.parseLong(lastSynced)) {
                lastSynced = blockedAt;
            }
            if (P.REGISTERED_TIME > Long.parseLong(lastSynced)) {
                lastSynced = P.REGISTERED_TIME + "";
            }
            this.__firebaseUtil.getOldDrawftsFrom(this.isGroup, this.groupId, this.otherNumber, lastSynced, true);
            clearNotifications();
            if (!isGroup) {
                lastSeen.setVisibility(View.VISIBLE);
                this.__firebaseUtil.getLastSeen(this.otherNumber);
            }
        } catch (Exception e) {
            int i = 0;
        }

        //this.__firebaseUtil.getServerOffset();
        //Log.d(TAG, "Activity on Resume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.__prepareDrawft.onDestroy();
            this.__prepareDrawft = null;
            this.__firebaseUtil.destroy();
            this.__firebaseUtil = null;
            this.__drawingPadView.onDestroy();
            this.__drawingPadView = null;
            this.adapter.destroy();
            this.adapter = null;
            GCMIntentService.currentDrawingPad = null;
            //Log.d(TAG, "Activity on Destroyed");
        } catch (Exception e) {

        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.getBoolean("refreshActivity")) {
                finish();
                getIntent().removeExtra("refreshActivity");
                startActivity(intent);
            }
            if (extras.getBoolean("isGroup")) {
                groupId = extras.getString("groupId");
                this.groupName = extras.getString("groupName");
                this.isGroup = true;
            } else {
                contactNum = extras.getString("groupId");
                this.groupId = GroupDrawft.getSortedNumber(P.MOBILE_NUMBER, extras.getString("groupId"));
                this.groupName = extras.getString("groupName");
                this.otherNumber = extras.getString("groupId");
                this.isGroup = false;
            }

            //Log.d(TAG, "Other Name - " + groupId);
            getIntent().removeExtra("groupName");
            getIntent().removeExtra("groupId");


        }
    }

    private void getOfflineDrawfts() {
        //Toast.makeText(getApplicationContext(), "Firebase connection " + FirebaseUtil.connectedToFB, Toast.LENGTH_SHORT).show();
        try {
            if (FirebaseUtil.connectedToFB && GroupDrawft.isNetworkOK(getApplicationContext())) {
                DrawftModel dm = new DrawftModel(getApplicationContext());
                this.offlineDrawfts = dm.getOfflineDrawfts(this.groupId);
                if (this.offlineDrawfts.size() > 0) {
                    for (int i = 0; i < this.offlineDrawfts.size(); i++) {
                        ArrayList<HashMap> dataFromFile = this.__fileUtil.getCurrentDrawftFromFile(this.offlineDrawfts.get(i) + ".txt");
                        String[] fullDrawftId = this.offlineDrawfts.get(i).split("\\.");
                        String drawftId = fullDrawftId[0];
                        if (!drawftId.isEmpty()) {
                            this.__firebaseUtil.syncOfflineDrawft(this.isGroup, dataFromFile, this.groupId, P.MOBILE_NUMBER, drawftId);
                            int pos = DrawingPadActivity.this.adapter.getTilePositionById(this.groupId + "/" + this.offlineDrawfts.get(i));
                            if (pos > -1) {
                                SingleDrawft sd = DrawingPadActivity.this.adapter.getItem(pos);
                                sd.setIsSent(2);
                                DrawingPadActivity.this.adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    Toast.makeText(getApplicationContext(), "All offline Drawfts synced", Toast.LENGTH_SHORT).show();

                    this.offlineDrawfts.clear();
                }
            }
        } catch (Exception e) {

        }
    }

    public void settingColor() {
        do {
            color1 = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
            color2 = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
            color3 = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
        }
        while (color1 == color2 || color2 == color3 || color3 == color1);

    }

    private void initViews() {

        TextView text = (TextView) findViewById(R.id.groupName);
        lastSeen = (TextView) findViewById(R.id.lastSeen);
        text.setText(this.groupName);
        text.setTypeface(GroupDrawft.robotoLight);
        lastSeen.setTypeface(GroupDrawft.robotoLight);
        TextView upBtn = (TextView) findViewById(R.id.drawingPadUp);
        upBtn.setTypeface(GroupDrawft.fontFeather);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
                goToCommunication();
            }
        });
        listView = (ListView) findViewById(R.id.listview);
        sendBtn = (Button) findViewById(R.id.icon_send_drawft);
        undoBtn = (Button) findViewById(R.id.icon_undo_drawft);
        lockBtn = (Button) findViewById(R.id.icon_lock_pad);
        unLockBtn = (Button) findViewById(R.id.icon_unlock_pad);
        LayoutInflater inflater = DrawingPadActivity.this.getLayoutInflater();
        headerview = (ViewGroup) inflater.inflate(
                R.layout.list_single_other, listView, false);
        TextView loadDrawfts = (TextView) headerview.findViewById(R.id.loadDrawfts);
        loadDrawfts.setTypeface(GroupDrawft.fontFeather);
        listView.addHeaderView(headerview, null, false);

        profileIcon = (TextView) findViewById(R.id.padTopBarProfileIcon);

        __drawingPadLayout = (LinearLayout) findViewById(R.id.drawing_pad);
        __relativeLayoutList = (RelativeLayout) findViewById(R.id.bottom_bar_list);
        __linearLayoutPad = (LinearLayout) findViewById(R.id.bottom_bar);
        LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(-1, -1);
        sendBtn.setTypeface(GroupDrawft.fontFeather);
        undoBtn.setTypeface(GroupDrawft.fontFeather);
        lockBtn.setTypeface(GroupDrawft.fontFeather);
        unLockBtn.setTypeface(GroupDrawft.fontFeather);
        profileIcon.setTypeface(GroupDrawft.fontFeather);
        sendBtn.setOnClickListener(this.btnClickListener);
        undoBtn.setOnClickListener(this.btnClickListener);
        lockBtn.setOnClickListener(this.btnClickListener);
        unLockBtn.setOnClickListener(this.btnClickListener);
        profileIcon.setOnClickListener(this.btnClickListener);

        sendBtn.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));
        /*upBtn.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));
        undoBtn.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));
        lockBtn.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));
        unLockBtn.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));
        profileIcon.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));*/


        this.adapter = new CustomAdapter(this, P.MOBILE_NUMBER, groupId);


        final TextView blockedText = (TextView) findViewById(R.id.blockedText);
        blockedText.setTypeface(GroupDrawft.robotoBold);
        final TextView blockIcon = (TextView) findViewById(R.id.block);
        blockIcon.setTypeface(GroupDrawft.fontFeather);

        final ContactModel cm = new ContactModel(DrawingPadActivity.this);
        final GroupModel gm = new GroupModel(DrawingPadActivity.this);

        if (cm.getBlocked(contactNum) == 1 || gm.getBlocked(groupId) == 1) {
            isBlocked = true;
            hideDrawingPad();
            drawingPadFlag = 1;
            blockIcon.setVisibility(View.VISIBLE);
            blockedText.setVisibility(View.VISIBLE);
            blockedText.setText(R.string.blockedText);
            unLockBtn.setClickable(false);
        } else {
            blockIcon.setVisibility(View.GONE);
            blockedText.setVisibility(View.GONE);
            unLockBtn.setClickable(true);
            listView.setAdapter(adapter);
            listView.setSmoothScrollbarEnabled(true);
            drawingPadFlag = 0;
        }

        blockIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrawingPadActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.alert_dialog, null);
                TextView title = (TextView) dialogView.findViewById(R.id.title);
                title.setText("Unblock Chat");
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
                        if (GroupDrawft.isNetworkOK(DrawingPadActivity.this)) {
                            DrawingPadActivity.this.__firebaseUtil = new FirebaseUtil(getApplicationContext(), P.FB_TOKEN);
                            if (cm.getBlocked(contactNum) == 1 || gm.getBlocked(groupId) == 1) {
                                blockIcon.setVisibility(View.GONE);
                                blockedText.setVisibility(View.GONE);
                                unLockBtn.setClickable(true);
                                listView.setAdapter(adapter);
                                listView.setSmoothScrollbarEnabled(true);
                                drawingPadFlag = 0;
                                if (isGroup) {
                                    gm.setBlocked(0, groupId);
                                    DrawingPadActivity.this.__firebaseUtil.removeFromBlockList(P.MOBILE_NUMBER, groupId);
                                } else {
                                    cm.setBlocked(0, contactNum);
                                    DrawingPadActivity.this.__firebaseUtil.removeFromBlockList(P.MOBILE_NUMBER, contactNum);
                                }
                            }
                            mDialog.dismiss();
                        } else {
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

            }
        });

        ContactModel.closeDBConnection(cm);
        GroupModel.closeDBConnection(gm);
        Integer[] dimensions = getDimensions();
        this.__drawingPadView = new DrawingPad(this, dimensions[0], dimensions[1]);
        this.__serializedData = new DrawingData();
        this.__drawftInfo = new DrawftInfo();
        this.__fileUtil = new FileUtil(this);
        this.__prepareDrawft = new PrepareDrawft(this);
        this.__firebaseUtil = new FirebaseUtil(this, P.FB_TOKEN);
        this.__drawingPadView.setOnTouchEventListener(new MyOnTouchEventListener());
        this.__firebaseUtil.setDispatcher(new MyDrawableDataFromFB());


        __drawingPadLayout.addView(this.__drawingPadView, localLayoutParams);
        this.__drawingPadView.initialize(__drawingPadLayout.getWidth(), __drawingPadLayout.getHeight());
        hideDrawingPad();
        colorPicker();
        //listViewScrolling();
        loadingDrawfts(0);

    }

    public void openConnectionDialog() {
        if (null != mDialog) mDialog.dismiss();

        GroupDrawft.fireBaseConnected = false;
        settingColor();
        AlertDialog.Builder builder = new AlertDialog.Builder(DrawingPadActivity.this);
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
                if (GroupDrawft.isNetworkOK(DrawingPadActivity.this)) {
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


    public void listViewScrolling() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount > 0) {
                    if (firstVisibleItem == 0) {
                        loadingDrawfts(7);
                    } else {
                        hideLoader();
                    }
                }
            }

        });

    }

    public void startGroupActivity() {
        Intent callIntent = new Intent(getApplicationContext(), NewGroupActivity.class);
        Bundle extras = new Bundle();
        extras.putBoolean("isNewGroup", !isGroup);
        extras.putBoolean("isGroup", this.isGroup);
        extras.putString("groupId", groupId);
        extras.putString("groupName", groupName);
        extras.putString("contactNum", contactNum);
        callIntent.putExtras(extras);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(callIntent);
        this.finish();
    }

    public void startUserActivity() {
        Intent callIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
        Bundle extras = new Bundle();
        extras.putString("groupId", groupId);
        extras.putString("groupName", groupName);
        extras.putString("contactNum", contactNum);
        callIntent.putExtras(extras);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(callIntent);
        this.finish();
    }


    private void unselectedButtons() {
        colorButton1.setText(R.string.icon_fa_circle_o);
        colorButton2.setText(R.string.icon_fa_circle_o);
        colorButton3.setText(R.string.icon_fa_circle_o);
        colorButton4.setText(R.string.icon_fa_circle_o);
        colorButton1.setTextSize(20);
        colorButton2.setTextSize(20);
        colorButton3.setTextSize(20);
        colorButton4.setTextSize(20);

    }

    private void saveColors(int pos, int color) {
        //Log.d(TAG, "Color = " + color);
        /*String savedColors = P.SAVED_COLORS;
        String[] savedColorsArray = savedColors.split(",");
        if (savedColorsArray.length > pos) {
            savedColorsArray[pos] = color + "";
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < savedColorsArray.length; i++) {
                result.append(savedColorsArray[i] + ",");
            }
            P.SAVED_COLORS = result.toString();
            P.write(getApplicationContext());
        } else {
            P.SAVED_COLORS = colorButton1.getTextColors().getDefaultColor() + "," + colorButton2.getTextColors().getDefaultColor() + "," + colorButton3.getTextColors().getDefaultColor() + "," + colorButton4.getTextColors().getDefaultColor() + ",";
            P.write(getApplicationContext());
        }*/
        P.SAVED_COLORS = colorButton1.getTextColors().getDefaultColor() + "," + colorButton2.getTextColors().getDefaultColor() + "," + colorButton3.getTextColors().getDefaultColor() + "," + colorButton4.getTextColors().getDefaultColor() + ",";
        P.write(getApplicationContext());
    }

    private void colorPicker() {
        colorButton1 = (Button) findViewById(R.id.colorButton1);//Green
        colorButton2 = (Button) findViewById(R.id.colorButton2);//black
        colorButton3 = (Button) findViewById(R.id.colorButton3);//Blue
        colorButton4 = (Button) findViewById(R.id.colorButton4);//Red
        String savedColors = P.SAVED_COLORS;
        String[] savedColorsArray = savedColors.split(",");
        if (savedColorsArray.length > 3) {
            colorButton1.setTextColor(Integer.parseInt(savedColorsArray[0]));
            colorButton2.setTextColor(Integer.parseInt(savedColorsArray[1]));
            colorButton3.setTextColor(Integer.parseInt(savedColorsArray[2]));
            colorButton4.setTextColor(Integer.parseInt(savedColorsArray[3]));
            this.__drawingPadView.setPaintColor(Integer.parseInt(savedColorsArray[0]));
        } else {
            colorButton1.setTextColor(getResources().getColor(R.color.paint_color_red));
            colorButton2.setTextColor(getResources().getColor(R.color.paint_color_blue));
            colorButton3.setTextColor(getResources().getColor(R.color.paint_color_green));
            colorButton4.setTextColor(getResources().getColor(R.color.paint_color_black));
            this.__drawingPadView.setPaintColor(getResources().getColor(R.color.paint_color_red));
        }
        /*colorButton1.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));
        colorButton2.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));
        colorButton3.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));
        colorButton4.setShadowLayer(0.25f, -1, 1, getResources().getColor(R.color.add_or_remove_list));*/

        unselectedButtons();
        colorButton1.setText(R.string.icon_fa_circle);
        colorButton1.setTextSize(25);


        colorButton1.setTypeface(GroupDrawft.fontFeather);
        colorButton2.setTypeface(GroupDrawft.fontFeather);
        colorButton3.setTypeface(GroupDrawft.fontFeather);
        colorButton4.setTypeface(GroupDrawft.fontFeather);

        View.OnClickListener paintEffectsListener = new View.OnClickListener() {
            public void onClick(View paramAnonymousView) {
                Log.d(TAG, "Entered");
                unselectedButtons();
                switch (paramAnonymousView.getId()) {
                    case R.id.colorButton1:
                        colorButton1.setText(R.string.icon_fa_circle);
                        colorButton1.setTextSize(25);
                        DrawingPadActivity.this.__drawingPadView.setPaintColor(colorButton1.getTextColors().getDefaultColor());
                        break;
                    case R.id.colorButton2:
                        colorButton2.setText(R.string.icon_fa_circle);
                        colorButton2.setTextSize(25);
                        DrawingPadActivity.this.__drawingPadView.setPaintColor(colorButton2.getTextColors().getDefaultColor());
                        break;
                    case R.id.colorButton3:
                        colorButton3.setText(R.string.icon_fa_circle);
                        colorButton3.setTextSize(25);
                        DrawingPadActivity.this.__drawingPadView.setPaintColor(colorButton3.getTextColors().getDefaultColor());
                        break;
                    case R.id.colorButton4:
                        colorButton4.setText(R.string.icon_fa_circle);
                        colorButton4.setTextSize(25);
                        DrawingPadActivity.this.__drawingPadView.setPaintColor(colorButton4.getTextColors().getDefaultColor());
                        break;
                }
            }
        };

        View.OnLongClickListener paintEffectsLongListener = new View.OnLongClickListener() {
            public boolean onLongClick(View paramAnonymousView) {

                switch (paramAnonymousView.getId()) {
                    case R.id.colorButton1:
                        DrawingPadActivity.this.__drawingPadView.setPaintColor(colorButton1.getTextColors().getDefaultColor());
                        colorpickerDialog(colorButton1, 0);
                        break;
                    case R.id.colorButton2:
                        DrawingPadActivity.this.__drawingPadView.setPaintColor(colorButton2.getTextColors().getDefaultColor());
                        colorpickerDialog(colorButton2, 1);
                        break;
                    case R.id.colorButton3:
                        DrawingPadActivity.this.__drawingPadView.setPaintColor(colorButton3.getTextColors().getDefaultColor());
                        colorpickerDialog(colorButton3, 2);
                        break;
                    case R.id.colorButton4:
                        DrawingPadActivity.this.__drawingPadView.setPaintColor(colorButton4.getTextColors().getDefaultColor());
                        colorpickerDialog(colorButton4, 3);
                        break;
                }
                return true;
            }
        };

        colorButton1.setOnClickListener(paintEffectsListener);
        colorButton2.setOnClickListener(paintEffectsListener);
        colorButton3.setOnClickListener(paintEffectsListener);
        colorButton4.setOnClickListener(paintEffectsListener);

        colorButton1.setOnLongClickListener(paintEffectsLongListener);
        colorButton2.setOnLongClickListener(paintEffectsLongListener);
        colorButton3.setOnLongClickListener(paintEffectsLongListener);
        colorButton4.setOnLongClickListener(paintEffectsLongListener);

    }


    /*private SingleDrawft prepareSingleDrawft(String fName, String dim, String by) {

        SingleDrawft singleDrawft = new SingleDrawft();
        if (fName == null && dim == null) {
            singleDrawft.setDrawftText(by);
        } else {
            // Bitmap bmp = this.__fileUtil.getBitmapFile(fName);
            singleDrawft.setDrawftImage(null);
            singleDrawft.setDrawftText(by);
            singleDrawft.setBitmapUrl(this.__fileUtil.getFilePath(fName));
            try {
                String[] size = dim.split("-");
                float w = Float.parseFloat(size[0]);
                float h = Float.parseFloat(size[1]);
                singleDrawft.setW(Math.round(w));
                singleDrawft.setH(Math.round(h));

            } catch (Exception e) {
                singleDrawft.setW(0);
                singleDrawft.setH(0);
            }
        }
        return singleDrawft;
    }*/

    public void colorpickerDialog(final Button b, final int pos) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, __drawingPadView.getPaintColor(), 0xff0000ff, new AmbilWarnaDialog.OnAmbilWarnaListener() {

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {

                b.setTextColor(color);
                unselectedButtons();
                b.setText(R.string.icon_fa_circle);
                b.setTextSize(25);
                DrawingPadActivity.this.__drawingPadView.setPaintColor(color);
                DrawingPadActivity.this.saveColors(pos, color);

            }
        });
        dialog.show();
    }


   /* private int[] getBitmapWidthAndHeight(String dim) {
        String[] size = dim.split("-");
        int w = Math.round(Float.parseFloat(size[0]));
        int h = Math.round(Float.parseFloat(size[1]));
        int[] dims = {w, h};
        return dims;
    }*/

    public void loadInitialList() {

        try {
            GroupMemberModel gmm = new GroupMemberModel(DrawingPadActivity.this);
            Cursor cr = gmm.getGroupMembersWithDetails(groupId);
            if (cr != null && cr.getCount() > 0 && cr.moveToFirst()) {
                do {
                    ContactTile tile = new ContactTile();
                    tile.setMobileNumber(cr.getString(cr.getColumnIndexOrThrow(GroupMemberModel.COL_MEMBER_ID)));
                    String uName = cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NAME));
                    if (uName == null || uName.isEmpty()) {
                        tile.setGroupName(tile.getMobileNumber());
                    } else {
                        tile.setGroupName(uName);
                    }
                    obj.put(tile.getMobileNumber(), tile.getGroupName());
                    if (tile.getMobileNumber().equals(P.MOBILE_NUMBER)) {
                        this.joinedAt = cr.getString(cr.getColumnIndexOrThrow(GroupMemberModel.COL_JOINED_AT));
                    }


                } while (cr.moveToNext());
            }
            if (cr != null)
                cr.close();
            GroupMemberModel.closeDBConnection(gmm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DrawftModel dm = new DrawftModel(getApplicationContext());
        ArrayList<DrawftBean> list = dm.getUserRecords(groupId, 7, 0);
        DrawftModel.closeDBConnection(dm);
        loadDrawfts(list, 0);
    }

    public void loadDrawfts(ArrayList<DrawftBean> list, int flag) {
        String con_name;
        try {

            for (int i = 0; i < list.size(); i++) {
                DrawftBean dBean = list.get(i);
                /*if (dBean.getSentBy().equals(GroupDrawft.owner)) {
                    sender = "You";
                } else {
                    sender = null;
                }*/
                String sender;
                if (dBean.getAutoMessageId() != 0) {
                    AutoMessagesModel amm = new AutoMessagesModel(DrawingPadActivity.this);
                    Cursor cr = amm.getAutoMessages(groupId, dBean.getAutoMessageId());
                    final AutoMessagesBean amb = new AutoMessagesBean();
                    if (cr != null && cr.getCount() > 0 && cr.moveToLast()) {
                        amb.setGroupId(cr.getString(cr.getColumnIndexOrThrow(AutoMessagesModel.COL_GROUP_GID)));
                        amb.setUid(cr.getInt(cr.getColumnIndexOrThrow(AutoMessagesModel.COL_AUTO_MESSAGES_UID)));
                        amb.setType(cr.getString(cr.getColumnIndexOrThrow(AutoMessagesModel.COL_TYPE)));
                        amb.setContent(cr.getString(cr.getColumnIndexOrThrow(AutoMessagesModel.COL_CONTENT)));

                        String option = amb.getType();
                        String objectString = amb.getContent();
                        JSONObject obj_json = new JSONObject(objectString);
                        switch (option) {
                            case "groupCreate": {
                                sender = getSenderName(dBean.getSentBy());
                                if (flag == 0) {
                                    this.adapter.addItem(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " created group " + obj_json.get("group_name"), dBean.getSentAt()));
                                } else {
                                    arr.add(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " created group " + obj_json.get("group_name"), dBean.getSentAt()));
                                }
                                String[] added_members = obj_json.get("added_members").toString().split(",");
                                for (int j = 0; j < added_members.length; j++) {
                                    if (added_members[j] != null) {
                                        con_name = getMemberName(added_members[j]);
                                        if (flag == 0) {
                                            this.adapter.addItem(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " added " + con_name, dBean.getSentAt()));
                                        } else {
                                            arr.add(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " added " + con_name, dBean.getSentAt()));
                                        }
                                    }
                                }
                            }
                            break;

                            case "editGroupName": {
                                sender = getSenderName(dBean.getSentBy());
                                if (flag == 0) {
                                    this.adapter.addItem(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " changed group name to " + obj_json.get("group_name"), dBean.getSentAt()));
                                } else {
                                    arr.add(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " changed group name to " + obj_json.get("group_name"), dBean.getSentAt()));
                                }
                            }
                            break;

                            case "groupMemberAdded": {
                                sender = getSenderName(dBean.getSentBy());
                                String[] added_members = obj_json.get("added_members").toString().split(",");
                                for (int j = 0; j < added_members.length; j++) {
                                    if (added_members[j] != null) {
                                        con_name = getMemberName(added_members[j]);
                                        if (flag == 0) {
                                            this.adapter.addItem(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " added " + con_name, dBean.getSentAt()));
                                        } else {
                                            arr.add(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " added " + con_name, dBean.getSentAt()));
                                        }
                                    }
                                }
                            }
                            break;

                            case "groupMemberRemoved": {
                                sender = getSenderName(dBean.getSentBy());
                                String[] removed_members = obj_json.get("removed_members").toString().split(",");
                                for (int j = 0; j < removed_members.length; j++) {
                                    if (removed_members[j] != null) {
                                        con_name = getMemberName(removed_members[j]);
                                        if (flag == 0) {
                                            this.adapter.addItem(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " removed " + con_name, dBean.getSentAt()));
                                        } else {
                                            arr.add(this.__prepareDrawft.prepareSingleDrawft(1, null, null, sender + " removed " + con_name, dBean.getSentAt()));
                                        }
                                    }
                                }
                            }
                            break;

                        }
                        cr.close();
                    }
                    AutoMessagesModel.closeDBConnection(amm);
                } else {
                    try {
                        String name = dBean.getSentBy();
                        if (!isGroup) {
                            if (!name.equals(P.MOBILE_NUMBER)) {
                                name = DrawingPadActivity.this.groupName;
                            }
                        } else {
                            if (obj.has(name) && !name.equals(P.MOBILE_NUMBER)) {
                                name = obj.get(name).toString();
                            } else {
                                ContactModel cm = new ContactModel(DrawingPadActivity.this);
                                String conName = cm.getContactName(name);
                                ContactModel.closeDBConnection(cm);
                                if (conName != null) {
                                    obj.put(name, conName);
                                    name = conName;
                                }
                            }
                        }
                        //Log.d(TAG, "Initial url" + dBean.getFileName());
                        if (flag == 0) {
                            this.adapter.addItem(this.__prepareDrawft.prepareSingleDrawft(dBean.getIsSent(), groupId + "/" + dBean.getFileName(), dBean.getDimensions(), name, dBean.getSentAt()));
                        } else {
                            arr.add(this.__prepareDrawft.prepareSingleDrawft(dBean.getIsSent(), groupId + "/" + dBean.getFileName(), dBean.getDimensions(), name, dBean.getSentAt()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (list.size() < 7) {
                TextView loadDrawfts = (TextView) headerview.findViewById(R.id.loadDrawfts);
                loadDrawfts.setText(R.string.icon_cancel);
                if (this.adapter.getCount() < 7) {
                    hideLoader();

                }
                if (this.adapter.getCount() == 0 && drawingPadFlag == 0) {
                    showDrawingPad();
                }

            }
            if (flag == 0) {
                this.listView.smoothScrollToPosition(this.adapter.getCount());
            } else {
                this.adapter.addItemAtFirst(arr);
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        int index = listView.getFirstVisiblePosition();
                        View vi = listView.getChildAt(0);
                        int top = (vi == null) ? 0 : vi.getTop();
                        listView.setSelectionFromTop(index + arr.size(), top);
                        arr.clear();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSenderName(String by) {
        String sender;
        try {
            if (by.equals(P.MOBILE_NUMBER)) {
                sender = "You";
            } else if (obj.get(by).toString() == null) {
                sender = by;
            } else {
                sender = obj.get(by).toString();
            }
        } catch (JSONException e) {
            sender = by;
        }
        return sender;
    }

    private String getMemberName(String by) {
        String memberName = "";
        try {
            if (by.equals(P.MOBILE_NUMBER)) {
                memberName = "you";
            } else {
                if (obj.has(by)) {
                    memberName = obj.get(by).toString();
                } else {
                    ContactModel cm = new ContactModel(DrawingPadActivity.this);
                    String conName = cm.getContactName(by);
                    if (conName != null && !conName.isEmpty()) {
                        memberName = conName;
                        obj.put(by, memberName);
                    } else {
                        memberName = by;
                    }
                    ContactModel.closeDBConnection(cm);
                }
            }

        } catch (JSONException e) {

        }

        return memberName;
    }

    private void hideDrawingPad() {
        DrawingPadActivity.this.__relativeLayoutList.setVisibility(View.VISIBLE);
        DrawingPadActivity.this.__drawingPadLayout.setVisibility(View.GONE);
        DrawingPadActivity.this.__linearLayoutPad.setVisibility(View.GONE);
        sendBtn.setVisibility(View.GONE);
    }

    private void showDrawingPad() {
        sendBtn.setVisibility(View.VISIBLE);
        this.__drawingPadLayout.setVisibility(View.VISIBLE);
        this.__linearLayoutPad.setVisibility(View.VISIBLE);
        this.__relativeLayoutList.setVisibility(View.GONE);
        this.checkForData();
    }

    private Integer[] getDimensions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Integer[] dim = {size.x, size.y};
        return dim;
    }

    /*private void prepareBitmap(String by, String fName, String time) {

        String fileName = fName + DrawingPadActivity.imgType;
        String dim = this.__drawingPaintView.getDimensionsString();
        int[] val = getBitmapWidthAndHeight(dim);
        int[] offset = this.__drawingPaintView.getIntersectionPoint();
        int w = this.__drawingPaintView.mBitmap.getWidth();
        int h = this.__drawingPaintView.mBitmap.getHeight();
        Bitmap b;
        b = this.formBitmap(offset, val, w, h, this.__drawingPaintView.mBitmap);
        if (b != null) {
            this.__fileUtil.saveDataToBitmapFIle(fileName, b);
            if (this.__drawftModel.addNewRecord(groupId, by, fileName, "Ashok-VJ", time, dim, autoMessageId)) {
                this.adapter.addItem(this.prepareSingleDrawft(fileName, dim, by));
            }
            this.listView.smoothScrollToPosition(this.adapter.getCount());
        }
        this.__drawingPaintView.empty();

    }*/

    /*protected Bitmap formBitmap(int[] offset, int[] val, int w, int h, Bitmap originalBitmap) {
        if ((offset[0] - 10) < 0) {
            offset[0] = 0;
        } else {
            offset[0] = offset[0] - 10;
        }
        if ((offset[1] - 10) < 0) {
            offset[1] = 0;
        } else {
            offset[1] = offset[1] - 10;
        }
        int imgW = (val[0] < DrawingPadActivity.minBitmapW) ? val[0] + 25 : (val[0] + 25 > w) ? w : val[0] + 25;
        int imgH = (val[1] < DrawingPadActivity.minBitmapH) ? val[1] + 25 : (val[1] + 25 > h) ? h : val[1] + 25;

        if ((offset[0] + imgW) > w) {
            imgW = (w - offset[0]);
        }
        if ((offset[1] + imgH) > h) {
            imgH = (h - offset[1]);
        }
        Bitmap b = null;
        try {
            b = Bitmap.createBitmap(originalBitmap, offset[0], offset[1], imgW, imgH);
        } catch (Exception e) {

        }
        return b;
    }*/

    public void saveCurrentDrawft() {
        if (this.__drawingPadView != null) {
            ArrayList<Float> currentPathsData = this.__drawingPadView.getCurPathData();
            int i = currentPathsData.size();
            if ((i > 1)) {
                ArrayList<Float> arrayOfFloat1 = new ArrayList<Float>();
                for (float path : currentPathsData) {
                    arrayOfFloat1.add(path);
                }
                this.__drawftInfo.addNewItem(arrayOfFloat1, this.__drawingPadView.curColor);
                this.__fileUtil.saveCurrentDrawftToFIle(this.__drawftInfo, "current_drawft.txt");
                this.__drawingPadView.clear();
                this.checkForData();
            }

        }
    }

    private void undoCurrentDrawft() {
        ArrayList<HashMap> dataFromFile = this.__fileUtil.getCurrentDrawftFromFile("current_drawft.txt");
        if (!dataFromFile.isEmpty()) {
            this.__drawingPadView.empty();
            this.__drawingPadView.clear();
            this.__drawingPadView.invalidate();
            dataFromFile.remove(dataFromFile.size() - 1);
            int i = dataFromFile.size();
            if (i > 0) {
                for (int j = 0; j < i; j++) {
                    HashMap localPath = dataFromFile.get(j);
                    this.__drawingPadView.drawUndoPath(localPath);
                    this.__drawingPadView.invalidate();
                    // saveUndoDrawft((ArrayList) localPath.get("path"), (Integer) localPath.get("path_info"));
                    //this.__drawingPadView.clear();
                }
            }
            this.__drawftInfo.clear();
            this.__drawftInfo.setCurrentDraft(dataFromFile);
            this.__fileUtil.saveCurrentDrawftToFIle(this.__drawftInfo, "current_drawft.txt");
        } else {
            this.__drawingPadView.empty();
            this.__drawingPadView.invalidate();
            this.__drawftInfo.clear();
        }
        this.checkForData();
    }


    private void sendDrawft() {
        try {
            if (this.__drawingPadView != null && this.__drawingPadView.hasData()) {
                getOfflineDrawfts();
                final DrawftInfo currentDraft = this.__drawftInfo;
                this.__serializedData.getMyPaintData().add(currentDraft);
                int isSent = 1;
                //Toast.makeText(getApplicationContext(), "Firebase connection before send " + FirebaseUtil.connectedToFB, Toast.LENGTH_SHORT).show();
                if (!FirebaseUtil.connectedToFB || !GroupDrawft.isNetworkOK(getApplicationContext())) {
                    isSent = 0;
                }
                String newDrawftId = this.__firebaseUtil.setNewDrawft(this.isGroup, currentDraft.getCurrentDrawft(), this.groupId, P.MOBILE_NUMBER, isSent) + PrepareDrawft.imgType;
                if (isSent == 0) {
                    Toast.makeText(this, "⌘ Drawfts will be synced when you come online.", Toast.LENGTH_SHORT).show();
                    this.__fileUtil.saveCurrentDrawftToFIle(this.__drawftInfo, newDrawftId + ".txt");
                }
                String dim = this.__drawingPadView.getDimensionsString();
                int[] val = this.__prepareDrawft.getBitmapWidthAndHeight(dim);
                int[] offset = this.__drawingPadView.getIntersectionPoint();
                int w = this.__drawingPadView.mBitmap.getWidth();
                int h = this.__drawingPadView.mBitmap.getHeight();
                Bitmap b;
                b = this.__prepareDrawft.formBitmap(offset, val, w, h, this.__drawingPadView.mBitmap);
                if (b != null) {
                    this.__fileUtil.saveDataToBitmapFIle(groupId + "/" + newDrawftId, b);
                    DrawftModel dm = new DrawftModel(getApplicationContext());
                    if (dm.addNewRecord(groupId, P.MOBILE_NUMBER, newDrawftId, "now", dim, autoMessageId, 0)) {

                        this.adapter.addItem(this.__prepareDrawft.prepareSingleDrawft((isSent == 0) ? 0 : 2, groupId + "/" + newDrawftId, dim, P.MOBILE_NUMBER, "now"));
                        this.listView.smoothScrollToPosition(this.adapter.getCount());
                        Animation zoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin);
                        zoomIn.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation arg0) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void onAnimationRepeat(Animation arg0) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void onAnimationEnd(Animation arg0) {
                                __drawingPadView.clearAnimation();
                                currentDraft.clear();
                                __drawingPadView.empty();
                                checkForData();
                            }
                        });
                        //FadeInBitmapDisplayer.animate(imageView, 2000);
                        this.__drawingPadView.setAnimation(zoomIn);
                    }
                    DrawftModel.closeDBConnection(dm);
                } else {
                    currentDraft.clear();
                    __drawingPadView.empty();
                    checkForData();
                }
                // notifyGroup();



            }
        } catch (Exception e) {
            if (this.__drawingPadView != null) {
                this.__drawingPadView.empty();
            }
        }
        this.checkForData();
    }

    public void checkForData() {
        if (this.__drawingPadView.hasData()) {
            DrawingPadActivity.this.undoBtn.setTextColor(getResources().getColor(R.color.hint_color));
            DrawingPadActivity.this.undoBtn.setClickable(true);
            this.__drawingPadLayout.setBackgroundColor(getResources().getColor(R.color.light_transparent));
        } else {
            DrawingPadActivity.this.undoBtn.setTextColor(getResources().getColor(R.color.icon_color));
            DrawingPadActivity.this.undoBtn.setClickable(false);
            this.__drawingPadLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }

    public void notifyGroup(String drawftId) {
        httpClientUtil.notifyGroup(notifyGroupListener, P.MOBILE_NUMBER, P.AUTH_CODE, groupId, drawftId, isGroup);
    }

    public void clearNotifications() {
        ContentValues cvCategory = new ContentValues();
        if (this.isGroup) {
            cvCategory.put(GroupModel.COL_GROUP_NOTIFICATION, 0);
            GroupModel.uOt(getApplicationContext(), GroupModel.TABLE_GROUPS, cvCategory, GroupModel.COl_GROUP_GID + "='" + groupId + "'", null);
        } else {
            cvCategory.put(ContactModel.COL_NOTIFICATION, 0);
            ContactModel.uOt(getApplicationContext(), ContactModel.TABLE_CONTACTS, cvCategory, ContactModel.COL_CONTACT_NUMBER + "='" + this.otherNumber + "'", null);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToCommunication();
    }

    public void goToCommunication() {
        // super.onBackPressed();
        Intent callIntent = new Intent(getApplicationContext(), CommunicationListActivity.class);
        Bundle extras = new Bundle();
        extras.putBoolean("restoreSession", restoreSession);
        callIntent.putExtras(extras);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(callIntent);
        this.finish();
    }


    public void hideLoader() {
        headerview.setVisibility(View.INVISIBLE);

    }

    public void loadingDrawfts(int offset) {
        offset_inc = offset;

        //headerview.setVisibility(View.VISIBLE);

        headerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++;
                offset_inc = clickCount * 7;
                DrawftModel dm = new DrawftModel(getApplicationContext());
                ArrayList<DrawftBean> lists = dm.getUserRecords(groupId, 7, offset_inc);

                if (lists.size() > 0) {
                    loadDrawfts(lists, 1);
                }
                DrawftModel.closeDBConnection(dm);
                //headerview.setVisibility(View.INVISIBLE);


            }
        });

    }

    public void updateNotificationTime(String groupId, String sentAt) {
        try {
            ContentValues cvCategory = new ContentValues();
            if (isGroup) {
                cvCategory.put(GroupModel.COL_GROUP_NOTIFICATION_TIME, sentAt);
                GroupModel.uOt(getApplicationContext(), GroupModel.TABLE_GROUPS, cvCategory, GroupModel.COl_GROUP_GID + "='" + groupId + "'", null);
            } else {
                cvCategory.put(ContactModel.COL_NOTIFICATION_TIME, sentAt);
                ContactModel.uOt(getApplicationContext(), ContactModel.TABLE_CONTACTS, cvCategory, ContactModel.COL_CONTACT_NUMBER + "='" + otherNumber + "'", null);
            }
            restoreSession = false;
            //Toast.makeText(this, "Time Updated = " + sentAt, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

        }

    }

    class MyOnTouchEventListener
            implements DrawingPad.OnTouchEventListener {
        MyOnTouchEventListener() {
        }

        public void onActionUp(MotionEvent paramMotionEvent) {
            DrawingPadActivity.this.saveCurrentDrawft();
        }

        public void onActionDown(MotionEvent paramMotionEvent) {
            DrawingPadActivity.this.checkForData();
        }
    }

    class MyDrawableDataFromFB
            implements FirebaseUtil.DrawableDataFromFB {
        MyDrawableDataFromFB() {

        }

        public void onSaveDrawft(String groupId, String drawftId) {
            DrawingPadActivity.this.notifyGroup(drawftId);
        }

        public void onReceiveTime(String groupId, String drawftId, String time) {
            try {
                ContentValues cvCategory = new ContentValues();
                cvCategory.put(DrawftModel.COLUMN_DRAWFT_SENT_AT, time);
                cvCategory.put(DrawftModel.COL_IS_SENT, 1);
                String fileName = drawftId + PrepareDrawft.imgType;
                DrawftModel.uOt(getApplicationContext(), DrawftModel.TABLE_DRAWFTS, cvCategory, DrawftModel.COLUMN_GROUP_ID + "='" + groupId + "' AND " + DrawftModel.COLUMN_DRAWFT_FILE_NAME + "='" + fileName + "'", null);
                DrawingPadActivity.this.updateNotificationTime(groupId, time);
                int pos = DrawingPadActivity.this.adapter.getTilePositionById(groupId + "/" + drawftId + PrepareDrawft.imgType);
                if (pos > -1) {
                    SingleDrawft sd = DrawingPadActivity.this.adapter.getItem(pos);
                    sd.setIsSent(1);
                    DrawingPadActivity.this.adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {

            }
        }

        public void onDataReceive(List list, String fName, String by, String time, String res) {
            try {
                if (unlockedAt > Long.parseLong(time)) {
                    return;
                }
                // Toast.makeText(getApplicationContext(), "Received", Toast.LENGTH_SHORT).show();
                if (DrawingPadActivity.this.adapter.exists(DrawingPadActivity.this.__fileUtil.getFilePath1(groupId + "/" + fName + PrepareDrawft.imgType))) {
                    if (!P.MOBILE_NUMBER.equals(by)) {
                        String dim = DrawingPadActivity.this.__prepareDrawft.onReceive(DrawingPadActivity.this.groupId, list, fName, by, time, res);
                        if (!dim.isEmpty()) {
                            DrawingPadActivity.this.__firebaseUtil.removeDrawft(DrawingPadActivity.this.isGroup, fName, DrawingPadActivity.this.groupId, DrawingPadActivity.this.otherNumber);
                            String fileName = fName + PrepareDrawft.imgType;

                            try {
                                if (!isGroup) {
                                    by = DrawingPadActivity.this.groupName;
                                } else {
                                    if (obj.has(by)) {
                                        by = obj.get(by).toString();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            DrawingPadActivity.this.adapter.addItem(DrawingPadActivity.this.__prepareDrawft.prepareSingleDrawft(1, groupId + "/" + fileName, dim, by, time));
                            DrawingPadActivity.this.listView.smoothScrollToPosition(DrawingPadActivity.this.adapter.getCount());
                            DrawingPadActivity.this.__drawingPadView.clear();
                            DrawingPadActivity.this.updateNotificationTime(groupId, time);

                        }
                    }
                }
            } catch (Exception e) {

            }

        }

        public void onOldDataReceive(boolean isGroup, String groupId, List list, String fName, String by, String time, String res) {
            try {
                if (!fName.isEmpty()) {
                    String dim = DrawingPadActivity.this.__prepareDrawft.onReceive(DrawingPadActivity.this.groupId, list, fName, by, time, res);
                    if (!dim.isEmpty()) {
                        DrawingPadActivity.this.__firebaseUtil.removeDrawft(DrawingPadActivity.this.isGroup, fName, DrawingPadActivity.this.groupId, DrawingPadActivity.this.otherNumber);
                        String fileName = fName + PrepareDrawft.imgType;
                        try {
                            if (!isGroup) {
                                by = DrawingPadActivity.this.groupName;
                            } else {
                                if (obj.has(by)) {
                                    by = obj.get(by).toString();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!loadedNew) {
                            DrawingPadActivity.this.adapter.addItem(DrawingPadActivity.this.__prepareDrawft.prepareSingleDrawft(1, null, null, "New", "new"));
                            loadedNew = true;
                        }
                        DrawingPadActivity.this.adapter.addItem(DrawingPadActivity.this.__prepareDrawft.prepareSingleDrawft(1, groupId + "/" + fileName, dim, by, time));
                        DrawingPadActivity.this.listView.smoothScrollToPosition(DrawingPadActivity.this.adapter.getCount());
                        DrawingPadActivity.this.updateNotificationTime(groupId, time);
                    }
                }
            } catch (Exception e) {
            }

        }

        public void onConnection() {
            getOfflineDrawfts();
        }

        public void onAuthError() {

        }

        public void onLastSeenReceive(Long time) {
            try {
                Long nowDate = System.currentTimeMillis();
                Long dateFromFireBase = time;
                // Long dateSt = 1410417832000L;
                SimpleDateFormat sdfNow = new SimpleDateFormat("dd-MMM-yyyy"); // the format of your date
                String formattedNowDate = sdfNow.format(nowDate);
                Date date = new Date(dateFromFireBase);    //(dateFromFireBase); // *1000 is to convert minutes to milliseconds
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy"); // the format of your date
                String formattedDate = sdf.format(date);
                SimpleDateFormat sdfTimeOther = new SimpleDateFormat("h:mm a"); // the format of your date
                String formattedTimeOther = sdfTimeOther.format(date);
                int diffInDays = (int) ((nowDate - date.getTime()) / (1000 * 60 * 60 * 24));
                String[] dateSplit = formattedDate.split("-");
                String[] nowDateSplit = formattedNowDate.split("-");
                if (dateSplit[0].equals(nowDateSplit[0])) {
                    SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a"); // the format of your date
                    String formattedTime = sdfTime.format(date);

                    lastSeen.setText("Today at " + formattedTime);
                } else if (diffInDays == 0 && (!dateSplit[0].equals(nowDateSplit[0]))) {
                    SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a"); // the format of your date
                    String formattedTime = sdfTime.format(date);
                    lastSeen.setText("Yesterday at " + formattedTime);
                } else if (diffInDays > 0 && diffInDays <= 7) { // this is till week
                    if (diffInDays == 1)
                        lastSeen.setText(diffInDays + " day ago");
                    else
                        lastSeen.setText(diffInDays + " days ago");
                } else
                    lastSeen.setText(formattedTimeOther + " on " + formattedDate);
            } catch (Exception e) {
                lastSeen.setVisibility(View.GONE);
            }
        }
    }

}
