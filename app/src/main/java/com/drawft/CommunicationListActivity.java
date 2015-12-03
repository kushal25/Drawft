package com.drawft;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drawft.GroupDrawft.P;
import com.drawft.data.ContactTile;
import com.drawft.model.contacts.ContactBean;
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
import com.drawft.util.PrepareDrawft;
import com.drawft.util.TelephoneUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.Firebase;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.google.android.gms.gcm.GoogleCloudMessaging;

public class CommunicationListActivity extends Activity implements Filterable {

    private static final String TAG = CommunicationListActivity.class.getSimpleName();
    public ListView contactListView = null;
    protected CommunicationListAdapter listAdapter = null;
    public static CommunicationListHandler __listHandler = null;
    protected FirebaseUtil __firebaseUtil = null;
    protected FileUtil __fileUtil = null;
    protected LinearLayout mainWaitLayout = null;
    TextView sync, syncIcon;
    private ArrayList<ContactTile> tiles = new ArrayList<ContactTile>();
    static ArrayList<ContactTile> allTiles = new ArrayList<>();
    private JSONArray contactsListNumbers = new JSONArray();
    //public FloatingActionButton refresh, search, createGroup = null;
    public TextView refresh, search, createGroup, terms, version, versionText, appName = null;
    public ImageView appImage;
    public TextView cancel;
    SimpleDraweeView slider;
    private ArrayList<ContactTile> filtered = new ArrayList<>();
    EditText searchBox = null;
    public static String regId = "";
    HttpClientUtil httpClientUtil = new HttpClientUtil();
    protected PrepareDrawft __prepareDrawft = null;

    private RelativeLayout mainLayout = null;
    private List<String> myColorList = GroupDrawft.getColorList();
    public AlertDialog dialog = null;
    public boolean countryClicked = false;
    public String countryCode;
    public Dialog mDialog = null;
    ProgressBar loadingTerms = null;
    WebView webview = null;
    TextView textTitleTerms = null, main_weight;
    Button okBtnTerms = null;
    static Parcelable listViewState;
    private int color1,color2,color3;
    RelativeLayout dialogView = null;
    AlertDialog.Builder alertDialog = null;
    FrameLayout bottomLayout;
    Animation rotate;


    JsonHttpResponseHandler initListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                Log.d(GroupDrawft.TAG, " \n\n\nClass: init, Method : initListener -- " + response.getBoolean("success") + "\n\n\n");
                if (response.getBoolean("success")) {
                    //tring phNumberToCall = response.getString("zd_no");
                    httpClientUtil.verifyMissedCall(missedCallListener, "9959833920", P.MOBILE_NUMBER);
                   /* Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:+" + phNumberToCall)); // 918067730033
                    startActivity(callIntent);*/
                    P.COUNTRY_CODE_NAME = "IN";
                    P.write(getApplicationContext());
                    showToast("Registration Success.");
                } else {
                    // hideProgressBar();
                    P.GCM_REGISTERED = false;
                    P.write(getApplicationContext());
                    showToast(getString(R.string.error_gcm_registered));
                    //onResume();
                }
                P.write(getApplicationContext());
            } catch (JSONException e) {
                // hideProgressBar();
                P.GCM_REGISTERED = false;
                P.write(getApplicationContext());
                Toast.makeText(getApplicationContext(), getString(R.string.error_gcm_registered), Toast.LENGTH_SHORT).show();
                // onResume();
                //sendLogstoLoggly(phoneNumber.getText().toString() + " User init - JSONException : " + e.toString());
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
        }
    };
    JsonHttpResponseHandler registerListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("success")) {
                    P.USER_REGISTERED = true;
                    P.REGISTERED_TIME = System.currentTimeMillis();
                    P.FB_TOKEN = response.getString("fb_token");
                    P.FB_URL = response.getString("fb_url");
                    P.SHARING_MSG = response.getString("share_msg");
                    P.write(getApplicationContext());
                    __listHandler.sendEmptyMessage(Do.SAVE_CONTACTS);
                }
            } catch (JSONException e) {

            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
        }
    };
    JsonHttpResponseHandler missedCallListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            httpClientUtil.getAuthCode(getAuthCodeListener, P.MOBILE_NUMBER, P.DEVICE_ID);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
        }
    };
    JsonHttpResponseHandler getAuthCodeListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("success")) {
                    P.AUTH_CODE = response.getString("auth_code");
                    P.write(getApplicationContext());
                    httpClientUtil.registerUser(registerListener, P.DEVICE_ID, P.REGISTRATION_ID, P.MOBILE_NUMBER, P.AUTH_CODE, P.RESOLUTION);
                } else {

                }
            } catch (JSONException e) {

            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
        }
    };
    JsonHttpResponseHandler getFBAuthCodeListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("success")) {
                    P.FB_TOKEN = response.getString("fb_token");
                    P.FB_URL = response.getString("fb_url");
                    P.write(getApplicationContext());
                    if (__firebaseUtil != null)
                    __firebaseUtil.authenticateToken(P.FB_TOKEN);
                }
            } catch (JSONException e) {

            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
            int j = 0;
        }
    };

    JsonHttpResponseHandler getUserGroupsListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("success")) {
                    try{
                        Log.d(TAG, "Saved Contacts::" + ContactModel.getTotalSavedContacts(getApplicationContext()));
                        CommunicationListActivity.this.__firebaseUtil.getBlockList(P.MOBILE_NUMBER);
                        JSONArray resultNumbers = response.getJSONArray("app_using_contacts");
                        if (resultNumbers.length() != 0) {
                            for (int i = 0; i < resultNumbers.length(); i++) {
                                String appUsingNumber = resultNumbers.getJSONObject(i).getString("mno");
                                ContentValues cvCategory = new ContentValues();
                                cvCategory.put(ContactModel.COL_APPUSING, 1);
                                ContactModel.uOt(getApplicationContext(), ContactModel.TABLE_CONTACTS, cvCategory, ContactModel.COL_CONTACT_NUMBER + "='" + appUsingNumber + "'", null);
                            }
                        }
                        JSONArray groups = response.getJSONArray("joinedGroups");
                        ArrayList<String> storedGroups = GroupModel.getCurrentGroups(getApplicationContext());
                        ArrayList<String> currentGroups = new ArrayList<>();
                        for (int i = 0; i < groups.length(); i++) {
                            JSONObject groupInfoObj = (JSONObject) groups.get(i);
                            currentGroups.add(groupInfoObj.getString("groupId"));
                            JSONObject groupMembersObj = groupInfoObj.getJSONObject("members");
                            if (!storedGroups.contains(groupInfoObj.getString("groupId"))) {
                                //ADD group
                                GroupSkeleton groupInfo = new GroupSkeleton(
                                        groupInfoObj.getString("groupName")
                                        , ""
                                        , groupInfoObj.getString("createdBy")
                                        , groupInfoObj.getString("groupId")
                                        , 0
                                        , 0
                                        , 1
                                        , 0
                                        , new HashMap<String, Integer>()
                                        , groupInfoObj.getString("createdAt")
                                );
                                GroupModel gm = new GroupModel(getApplicationContext());
                                boolean inserted = gm.addNewGroup(groupInfo);
                                if (inserted) {
                                    Iterator<String> iterator = groupMembersObj.keys();
                                    while (iterator.hasNext()) {
                                        String key = iterator.next();
                                        int isAdmin = 0;
                                        if (groupMembersObj.getInt(key) == 1) {
                                            isAdmin = 1;
                                        }
                                        GroupMemberModel.addMember(getApplicationContext(), groupInfo.getGroupId(), key, isAdmin, response.getString("currentTime"));
                                    }
                                }
                            } else {

                            }
                        }
                        for (int i = 0; i < storedGroups.size(); i++) {
                            if (!currentGroups.contains(storedGroups.get(i))) {
                                PrepareDrawft.onDeleteGroup(getApplicationContext(), storedGroups.get(i));
                            }
                        }
                        __listHandler.sendEmptyMessage(Do.GET_CONTACTS);
                    }catch (Exception e){

                    }


                } else {
                    Toast.makeText(getApplicationContext(), "Try Again", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Catch Try Again", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
            Toast.makeText(getApplicationContext(), "Try Again", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
            Toast.makeText(getApplicationContext(), "Try Again", Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        public void onClick(View paramAnonymousView) {
            try {
                paramAnonymousView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
                switch (paramAnonymousView.getId()) {

                    case R.id.searchIcon:
                    case R.id.search:
                        onSearchClick();
                        break;
                    case R.id.refreshIcon:
                    case R.id.refresh:
                        if(GroupDrawft.isNetworkOK(CommunicationListActivity.this)) {
                            showProgressLoader();
                            slider.setVisibility(View.INVISIBLE);
                    /*int color = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
                    mainWaitLayout.setBackgroundColor(color);*/
                            __listHandler.sendEmptyMessage(Do.SAVE_CONTACTS);
                            listAdapter.notifyDataSetChanged();
                            bottomLayout.setVisibility(View.INVISIBLE);
                        }else{
                            openConnectionDialog();
                        }
                        break;
                    case R.id.createGroupIcon:
                    case R.id.createGroup:
                        Intent callIntent = new Intent(getApplicationContext(), NewGroupActivity.class);
                        Bundle extras = new Bundle();
                        extras.putBoolean("isNewGroup", true);
                        callIntent.putExtras(extras);
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(callIntent);
                        break;
                    case R.id.termsIcon:
                    case R.id.terms:
                        openTermsOfUseDialog();
                        break;

                }
            } catch (Exception e) {

            }
        }
    };

    /*JsonHttpResponseHandler sendContactListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {

            } catch (JSONException e) {

            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
            int j = 0;
        }
    };*/
    /*public static void refreshThings(String type) {
        switch (type) {
            case "on_new_drawft":
                int i = 0;
                break;
        }
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_out);
        setContentView(R.layout.activity_communication_list);
        //mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
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
        initViews();
        initHandler();
//        if (savedInstanceState != null) {
//            listViewState = savedInstanceState.getParcelable("data");
//        }
        initListView();
        //loadDefaultList();

        if (!P.USER_REGISTERED) {
            //httpClientUtil.getAuthCode(getAuthCodeListener, P.MOBILE_NUMBER, P.DEVICE_ID);
            //P.MOBILE_NUMBER = GroupDrawft.owner;
            String device_id = TelephoneUtils.getDeviceId(CommunicationListActivity.this);

            P.DEVICE_ID = device_id;
            httpClientUtil.initUser(initListener, device_id, P.REGISTRATION_ID, P.MOBILE_NUMBER, P.MOBILE_NUMBER, P.COUNTRY_CODE.toUpperCase());
        }
        try {
            if (P.CONTACTS_SAVED) {
                __listHandler.sendEmptyMessage(Do.GET_CONTACTS);
            } else {
                onSaveContacts();
            }
        } catch (Exception e) {
            CommunicationListActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(CommunicationListActivity.this, "Error in Getting Contacts! Please Try Again", Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    @Override
    public void onPause() {
        //listViewState = this.contactListView.onSaveInstanceState();
        super.onPause();
       /* new Thread() {
            @Override
            public void run() {
                try {
                    FirebaseUtil.goOffline();
                } catch (Exception e) {

                }
            }
        }.start();*/
        FirebaseUtil.goOffline();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (!extras.getBoolean("restoreSession")) {
                listViewState = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            CommunicationListActivity.__listHandler = null;
            this.__prepareDrawft.onDestroy();
            this.__prepareDrawft = null;
            this.listAdapter.destroy();
            this.__fileUtil = null;
            this.__firebaseUtil.destroy();
            this.__firebaseUtil = null;
            /*allTiles.clear();*/
            this.tiles.clear();
            //this.allTiles = null;
            // CommunicationListActivity.tiles = null;
            //trimCache(this);
            // this.appImage.setImageDrawable(null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    protected void onResume() {
        super.onResume();
        listAdapter.getColors();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (P.NEW_VERSION_STATUS)
        {
            updateNewVersion();
        }
        FirebaseUtil.goOnline();
        /*String sha = getSHA(getApplicationContext());
        int i = 0;*/
    }


    /*public void getRegId() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(GroupDrawft.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    P.GCM_REGISTERED = true;
                    P.REGISTRATION_ID = regid;
                    P.AUTH_CODE = "";
                    P.write(getApplicationContext());

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //  etRegId.setText(msg + "\n");
            }
        }.execute(null, null, null);
    }*/
    public void onSaveContacts() {
        showProgressLoader();
        slider.setVisibility(View.INVISIBLE);
        __listHandler.sendEmptyMessage(Do.SAVE_CONTACTS);
        listAdapter.notifyDataSetChanged();
        //fam.collapse();
        //bottomLayout.setVisibility(View.INVISIBLE);
        //cross.setVisibility(View.INVISIBLE);
    }

    public void initViews() {
        try {
            Firebase.setAndroidContext(this);
           /* final ActionBar actionBar = getActionBar();
            actionBar.hide();*/
            mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
            final Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            mainWaitLayout = (LinearLayout) findViewById(R.id.main_wait_layout);
            main_weight = (TextView) findViewById(R.id.main_wait);
            slider = (SimpleDraweeView) findViewById(R.id.slider);
            //slider.setTypeface(GroupDrawft.fontFeather);
            // final View shad = findViewById(R.id.shadowView);
            bottomLayout = (FrameLayout) findViewById(R.id.bottomFrame);
            LayoutInflater inflater = getLayoutInflater();
            dialogView = (RelativeLayout) inflater.inflate(R.layout.dialog_verification, null);
            alertDialog = new AlertDialog.Builder(CommunicationListActivity.this);
            alertDialog.setView(dialogView);
            mDialog = alertDialog.create();


            refresh = (TextView) findViewById(R.id.refresh);
            search = (TextView) findViewById(R.id.search);
            createGroup = (TextView) findViewById(R.id.createGroup);
            terms = (TextView) findViewById(R.id.terms);
            final TextView refreshIcon = (TextView) findViewById(R.id.refreshIcon);
            final TextView searchIcon = (TextView) findViewById(R.id.searchIcon);
            final TextView createGroupIcon = (TextView) findViewById(R.id.createGroupIcon);
            final TextView termsIcon = (TextView) findViewById(R.id.termsIcon);
            refreshIcon.setTypeface(GroupDrawft.fontFeather);
            searchIcon.setTypeface(GroupDrawft.fontFeather);
            createGroupIcon.setTypeface(GroupDrawft.fontFeather);
            termsIcon.setTypeface(GroupDrawft.fontFeather);
            main_weight.setTypeface(GroupDrawft.fontFeather);

            refresh.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            search.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            createGroup.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            terms.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            refreshIcon.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            createGroupIcon.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            searchIcon.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            termsIcon.setShadowLayer(4.5f, -1, 1, Color.BLACK);

            cancel = (TextView) findViewById(R.id.cancel);
            version = (TextView) findViewById(R.id.version);
            versionText = (TextView) findViewById(R.id.versionText);
            appName = (TextView) findViewById(R.id.appName);
            appImage = (ImageView) findViewById(R.id.appImage);
            refresh.setTypeface(GroupDrawft.robotoBold);
            search.setTypeface(GroupDrawft.robotoBold);
            createGroup.setTypeface(GroupDrawft.robotoBold);
            cancel.setTypeface(GroupDrawft.fontFeather);
            terms.setTypeface(GroupDrawft.robotoBold);
            version.setTypeface(GroupDrawft.robotoBold);
            versionText.setTypeface(GroupDrawft.robotoBold);
            appName.setTypeface(GroupDrawft.robotoBold);
            appName.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            version.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            versionText.setShadowLayer(4.5f, -1, 1, Color.BLACK);
            cancel.setShadowLayer(4.5f, -1, 1, Color.BLACK);

            slideUp.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation arg0) {

                }

                @Override
                public void onAnimationRepeat(Animation arg0) {

                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        String curVersion = pInfo.versionName;
                       // version.setVisibility(View.VISIBLE);
                        if (!curVersion.equals(P.APP_VERSION)) {

                            /*version.setPadding(10, 10, 10, 10);*/
                            cancel.setPadding(10, 100, 20, 10);
                            version.setText(R.string.new_version_icon);
                            version.setTypeface(GroupDrawft.fontFeather);
                            version.setTextSize(40);
                            versionText.setVisibility(View.VISIBLE);
                            //version.setTextColor(Color.parseColor(myColorList.get(new Random().nextInt(34))));
                            //version.setBackgroundColor(Color.parseColor(myColorList.get(new Random().nextInt(34))));
                            version.setClickable(true);
                            version.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToStore();
                                }
                            });
                            versionText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToStore();
                                }
                            });
                            //version.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.right_to_left));
                            version.setClickable(true);
                        } else {
                            version.setClickable(false);
                        }

                    } catch (PackageManager.NameNotFoundException e) {

                    }

                }
            });
            version.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToStore();
                }
            });
            slider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (bottomLayout.getVisibility() == View.INVISIBLE) {
                        int color1 = Color.parseColor(myColorList.get(new Random().nextInt(34)));
                        int color2 = Color.parseColor(myColorList.get(new Random().nextInt(34)));
                        int color3 = Color.parseColor(myColorList.get(new Random().nextInt(34)));
                        int color4 = Color.parseColor(myColorList.get(new Random().nextInt(34)));
                        int color5 = Color.parseColor(myColorList.get(new Random().nextInt(34)));

                        //paramAnonymousView.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.imageanim));
                        bottomLayout.setVisibility(View.VISIBLE);
                        version.setVisibility(View.VISIBLE);

                        /*createGroup.setTextColor(color1);
                        refresh.setTextColor(color2);
                        search.setTextColor(color3);
                        terms.setTextColor(color4);

                        createGroupIcon.setTextColor(color1);
                        refreshIcon.setTextColor(color2);
                        searchIcon.setTextColor(color3);
                        termsIcon.setTextColor(color4);*/
                        appName.setTextColor(color1);
                        //  cancel.setTextColor(color5);
                        bottomLayout.startAnimation(slideUp);
                        slider.setVisibility(View.INVISIBLE);
                        version.setText("Version: " + P.APP_VERSION);

                    }
                }
            });
            //cancel.setBackgroundColor(getResources().getColor(R.color.number_color));
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomLayout.setVisibility(View.INVISIBLE);
                    bottomLayout.startAnimation(slideDown);
                    slider.setVisibility(View.VISIBLE);
                }
            });

            //fam = (FloatingActionsMenu) findViewById(R.id.floatingActionsMenu);
            //refresh = (FloatingActionButton) findViewById(R.id.refresh);
            //search = (FloatingActionButton) findViewById(R.id.search);
            //createGroup = (FloatingActionButton) findViewById(R.id.createGroup);
            /*
            fam.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
                @Override
                public void onMenuExpanded() {
                    shad.getLayoutParams();
                    shad.setVisibility(View.VISIBLE);
                    shad.setOnClickListener(null);
                }

                @Override
                public void onMenuCollapsed() {
                    shad.setVisibility(View.GONE);
                }
            });*/
            refresh.setOnClickListener(btnClickListener);
            refreshIcon.setOnClickListener(btnClickListener);
            search.setOnClickListener(btnClickListener);
            searchIcon.setOnClickListener(btnClickListener);
            terms.setOnClickListener(btnClickListener);
            termsIcon.setOnClickListener(btnClickListener);
            createGroup.setOnClickListener(btnClickListener);
            createGroupIcon.setOnClickListener(btnClickListener);
        } catch (Exception e) {
            Log.d(TAG, "Error");
        }
    }

    public void goToStore() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
    public void onSearchClick() {
        bottomLayout.setVisibility(View.INVISIBLE);
        slider.setVisibility(View.INVISIBLE);
        LayoutInflater inflater = LayoutInflater
                .from(getApplicationContext());
        View view = inflater.inflate(R.layout.communication_list_action_bar, null);
        RelativeLayout.LayoutParams rLParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
        mainLayout.addView(view, rLParams);
        final LinearLayout inputLinear = (LinearLayout) view.findViewById(R.id.inputSearchLi);
        //final LinearLayout inputLinear = (LinearLayout) actionBar.getCustomView().findViewById(R.id.inputSearchLi);
        searchBox = (EditText) inputLinear.findViewById(R.id.inputSearch);
        searchBox.setTextColor(Color.parseColor("#FFFFFF"));
        final Button search_action_icon = (Button) inputLinear.findViewById(R.id.search_close);
        search_action_icon.setTypeface(GroupDrawft.fontFeather);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                CommunicationListActivity.this.getFilter().filter(cs);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        search_action_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommunicationListActivity.this.getFilter().filter("");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(inputLinear.getApplicationWindowToken(), 0);
                inputLinear.setVisibility(View.GONE);
                slider.setVisibility(View.VISIBLE);
            }
        });
    }
    JsonHttpResponseHandler fetchTermsListener = new JsonHttpResponseHandler() {
        final String termsString = "<span style=\"color:#1a2a3a;\"><div style='text-align:center;padding-top:10px;font-weight:bold'>We are Drawft.</div><br/>  <div style='text-align:center;font-style:italic;'>You can use Drawft only if you agree to all these terms.</div><br/>  <div style=\"padding-right:24px;text-align:justify;\"><ol><li>We collect your private data, mainly your phonebook. We log certain data like when you login and logout and from where. We never share this data with anyone except as required to provide you our service or when we receive a legal order.</li><br/>  <li>When you delete something it is deleted immediately from our servers.</li><br/>  <li>We are not responsible for any good or bad you may or may not incur by your direct or indirect use of Drawft.</li><br/>   <li>You cannot use Drawft in any way that interferes others from using Drawft.</li><br/>  <li>We do not guaratee that our servers will be up and running all the time.</li><br/>    <li>We may discontinue Drawft any time without prior notice to you.</li><br/>  <li>We may modify these terms anytime by making an announcement on our Twitter page  <a target='_blank' href='https://twitter.com/sayitwithoutwords' style=\"text-decoration:none;color:#1ca5ec;\" > @sayitwithoutwords.</a></li></ol></div> <div style='text-align:center;font-style:italic;'>End.</div><br/></span>";


        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            LayoutInflater inflater = getLayoutInflater();
            //final RelativeLayout dialogView = (RelativeLayout) inflater.inflate(R.layout.dialog_verification, null);
            final WebView webview1 = (WebView) dialogView.findViewById(R.id.body);
            try {
                final JSONObject result = new JSONObject(response.toString());
                if (result.getBoolean("success")) {
                    //
                    dialogView.findViewById(R.id.load_terms).setVisibility(View.GONE);
                    //loadingTerms.setVisibility(View.GONE);
                    String html = "<html><body>" + result.optString("result", termsString) + "</body></html>";
                    String mime = "text/html";
                    String encoding = "utf-8";
                    webview1.getSettings().setJavaScriptEnabled(true);
                    webview1.loadDataWithBaseURL(null, html, mime, encoding, null);
                    // textTitleTerms.setText(result.optString("result",getString(R.string.terms)));
                } else {
                    //loadingTerms.setVisibility(View.GONE);
                    dialogView.findViewById(R.id.load_terms).setVisibility(View.GONE);
                    String html = "<html><body>" + termsString + "</body></html>";
                    String mime = "text/html";
                    String encoding = "utf-8";
                    webview1.getSettings().setJavaScriptEnabled(true);
                    webview1.loadDataWithBaseURL(null, html, mime, encoding, null);
                }
            } catch (JSONException e) {
                //loadingTerms.setVisibility(View.GONE);
                dialogView.findViewById(R.id.load_terms).setVisibility(View.GONE);
                String html = "<html><body>" + termsString + "</body></html>";
                String mime = "text/html";
                String encoding = "utf-8";
                webview1.getSettings().setJavaScriptEnabled(true);
                webview1.loadDataWithBaseURL(null, html, mime, encoding, null);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
            int j = 0;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
            int j = 0;
        }
    };

    public void settingColor() {
        do {
            color1 = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
            color2 = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
            color3 = Color.parseColor(myColorList.get(new Random().nextInt(myColorList.size())));
        }
        while (color1 == color2 || color2 == color3 || color3 == color1);

    }
    public void openTermsOfUseDialog() {
        if (null != mDialog) mDialog.dismiss();
        //
        cancel.performClick();
        AlertDialog.Builder builder = new AlertDialog.Builder(CommunicationListActivity.this);

        final TextView title = (TextView) dialogView.findViewById(R.id.title);
        final TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
        final TextView ok = (TextView) dialogView.findViewById(R.id.ok);
        final TextView noteVerify = (TextView) dialogView.findViewById(R.id.noteVerify);
        final TextView termsText = (TextView) dialogView.findViewById(R.id.termsText);
        final RelativeLayout header_wrapper = (RelativeLayout) dialogView.findViewById(R.id.header_wrapper);
        final RelativeLayout webView_wrapper = (RelativeLayout) dialogView.findViewById(R.id.webViewWrapper);
        final View sepRight = dialogView.findViewById(R.id.sepRight);

        title.setTypeface(GroupDrawft.robotoBold);
        cancel.setTypeface(GroupDrawft.robotoBold);
        ok.setTypeface(GroupDrawft.robotoBold);
        noteVerify.setTypeface(GroupDrawft.robotoBold);
        termsText.setTypeface(GroupDrawft.robotoLight);

        settingColor();
        header_wrapper.setBackgroundColor(color1);
        cancel.setBackgroundColor(color2);

        title.setText("Terms");
        cancel.setText("OK");
        ok.setVisibility(View.GONE);
        sepRight.setVisibility(View.GONE);
        noteVerify.setVisibility(View.GONE);
        termsText.setVisibility(View.GONE);
        webView_wrapper.setVisibility(View.VISIBLE);
        mDialog.setCancelable(false);
        if (GroupDrawft.isNetworkOK(CommunicationListActivity.this)) {
            httpClientUtil.fetchTermsOfUse(fetchTermsListener);
        } else {
            openConnectionDialog();
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
        //
//        loadingTerms = (ProgressBar) dialogView.findViewById(R.id.main_wait_1);
//        webview = (WebView) dialogView.findViewById(R.id.body);
//        textTitleTerms = (TextView) dialogView.findViewById(R.id.terms_wrapper);
//        textTitleTerms.setTypeface(GroupDrawft.robotoBold);
//        okBtnTerms = (Button) dialogView.findViewById(R.id.dialog_close);
//        okBtnTerms.setTypeface(GroupDrawft.fontFeather);
//        loadingTerms.setVisibility(View.VISIBLE);
//        //new ServiceRequestHelper().fetchTermsOfUse(termsListener, InitializationActivity.this, P.MOBILE_NUMBER, P.AUTH_CODE);
//        builder.setView(dialogView);
//        //
//        mDialog = builder.create();
//        mDialog.setCancelable(false);
//        okBtnTerms.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mDialog.dismiss();
//            }
//        });
//        mDialog.setOnKeyListener(new Dialog.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    dialog.dismiss();
//                    //
//                }
//                return true;
//            }
//        });
//        mDialog.show();
    }




    public void initListView() {
        this.contactListView = (ListView) findViewById(R.id.contactListView);
        this.listAdapter = new CommunicationListAdapter(this, allTiles);
        this.contactListView.setAdapter(this.listAdapter);

        if (listViewState != null) {
            this.contactListView.onRestoreInstanceState(listViewState);
        }
        this.contactListView.setSmoothScrollbarEnabled(true);
        this.contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {

                if (allTiles.get(pos).getAppUsing() == 1 || allTiles.get(pos).getIsGroup() == true) {
                    /*int firstVisible = contactListView.getFirstVisiblePosition()
                            - contactListView.getHeaderViewsCount();
                    int lastVisible = contactListView.getLastVisiblePosition()
                            - contactListView.getHeaderViewsCount();

                    View child = contactListView.getChildAt(lastVisible
                            - firstVisible);
                    listPosition = child.getTop() + child.getMeasuredHeight()
                            - contactListView.getMeasuredHeight();*/
                    /*listIndex = contactListView.getFirstVisiblePosition();
                    View c = contactListView.getChildAt(0);
                    listPosition = (c == null) ? 0 : (c.getTop() - contactListView.getPaddingTop());*/
                    listViewState = contactListView.onSaveInstanceState();
                    CommunicationListActivity.this.openNewActivity(pos);
                } else {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, P.SHARING_MSG);
                    sendIntent.setType("text/plain");

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(arg0.getWindowToken(), 0);

                    String shareTitle = CommunicationListActivity.this.getResources().getString(R.string.share_title) + " " + allTiles.get(pos).getGroupName();
                    CommunicationListActivity.this.startActivity(Intent.createChooser(sendIntent, shareTitle));
                }
                //throw new RuntimeException("This is an intentional crash");
            }
        });

        this.__firebaseUtil = new FirebaseUtil(this, P.FB_TOKEN);
        this.__fileUtil = new FileUtil(this);
        this.__firebaseUtil.setGroupDataDispatcher(new GroupDataFromFB());
        this.__firebaseUtil.setDispatcher(new MyDrawableDataFromFB());
        GCMIntentService.setGcmDispatcher(new MyGcmDispatcher()); // to listen from gcm notifications
        this.__prepareDrawft = new PrepareDrawft(this);
        // this.__firebaseUtil.getUserGroups(GroupDrawft.owner);
    }

    public void showToast(String str) {
        Toast.makeText(CommunicationListActivity.this.getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
//        if (fam.isExpanded()) {
//            fam.collapse();
//        } else {
        super.onBackPressed();
//        }
    }

    public void showProgressLoader() {
        mainWaitLayout.setVisibility(View.VISIBLE);
        contactListView.setVisibility(View.GONE);
        RotateAnimation rotateAnimation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1600);
        rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        main_weight.setAnimation(rotateAnimation);
        rotateAnimation.start();
    }

    public void hideProgressLoader() {
        mainWaitLayout.setVisibility(View.GONE);
        contactListView.setVisibility(View.VISIBLE);
        main_weight.clearAnimation();
    }


    public void openNewActivity(int pos) {
        ContactTile tile = this.listAdapter.getItem(pos);
        Intent callIntent = new Intent(getApplicationContext(), DrawingPadActivity.class);
        Bundle extras = new Bundle();
        extras.putString("groupId", tile.getGroupId());
       /* extras.putInt("listPosition", listPosition);
        extras.putInt("listIndex", listIndex);*/
        extras.putString("groupName", tile.getGroupName());
        extras.putBoolean("isGroup", tile.getIsGroup());
        extras.putString("caller", "contacts");
        callIntent.putExtras(extras);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(callIntent);
        this.finish();
    }

    public void updateNewVersion()
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CommunicationListActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.alert_dialog, null);
        TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setText("Update Version");
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
                if(GroupDrawft.isNetworkOK(CommunicationListActivity.this))
                {
                    goToStore();
                }
                else
                {
                    openConnectionDialog();
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                P.NEW_VERSION_STATUS = false;
                P.write(getApplicationContext());
            }
        });
        mDialog.show();
    }

    public void initHandler() {

        __listHandler = new CommunicationListHandler(CommunicationListActivity.this);
        // __listHandler.sendEmptyMessage(Do.GET_CONTACTS);
        /*CharSequence text;
        text = "Loading Contacts";
        Toast.makeText(this.getApplicationContext(), text, Toast.LENGTH_SHORT).show();*/
    }

    public void openConnectionDialog() {
        if (null != mDialog) mDialog.dismiss();

        GroupDrawft.fireBaseConnected = false;
        settingColor();
        AlertDialog.Builder builder = new AlertDialog.Builder(CommunicationListActivity.this);
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
                if (GroupDrawft.isNetworkOK(CommunicationListActivity.this)) {
                    mDialog.dismiss();
                    //onResume();
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


    public void loadUserGroupList(Map group) {
        if (group == null) {
            TextView t = (TextView) findViewById(R.id.emptyListText);
            t.setTypeface(GroupDrawft.fontFeather);
            this.contactListView.setEmptyView(t);
            return;
        }
        ContactTile tile = new ContactTile();
        tile.setMobileNumber((String) group.get("createdBy"));
        tile.setGroupName((String) group.get("groupName"));
        tile.setGroupId((String) group.get("groupId"));
        this.listAdapter.addItem(tile);
    }

    public void addContactItem(HashMap group) {
        if (group == null) {
            TextView t = (TextView) findViewById(R.id.emptyListText);
            t.setTypeface(GroupDrawft.fontFeather);
            this.contactListView.setEmptyView(t);
            return;
        }
        ContactTile tile = new ContactTile();
        tile.setMobileNumber((String) group.get("createdBy"));
        tile.setGroupName((String) group.get("groupName"));
        tile.setGroupId((String) group.get("groupId"));
        this.listAdapter.addItem(tile);
    }

    public static class Do {
        public static final int GET_CONTACTS = 1;
        public static final int SAVE_CONTACTS = 2;
        public static final int GET_NOTIFICATIONS = 3;
        public static final int SEND_CONTACTS = 4;
    }

    public class CommunicationListHandler extends Handler {

        private final WeakReference<CommunicationListActivity> parent;

        CommunicationListHandler(CommunicationListActivity parent) {
            this.parent = new WeakReference<CommunicationListActivity>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            final CommunicationListActivity targetActivity = parent.get();
            try {
                switch (msg.what) {
                    case Do.GET_CONTACTS: {
                        GroupDrawft.runOnBackground(targetActivity, new Runnable() {
                            @Override
                            public void run() {
                                final ArrayList<ContactTile> notifications = new ArrayList<ContactTile>();
                                final ArrayList<ContactTile> appUsing = new ArrayList<ContactTile>();
                                final ArrayList<ContactTile> blocked = new ArrayList<ContactTile>();

                                // allTiles.clear();
                                tiles.clear();
                                notifications.clear();
                                appUsing.clear();
                                blocked.clear();

                                Cursor cr;
                                GroupModel groupsDb = GroupModel.getR(targetActivity);
                                try {
                                    cr = groupsDb.getGroups();

                                } catch (Exception e) {
                                    cr = null;
                                }

                                if (cr != null && cr.getCount() > 0 && cr.moveToFirst()) {
                                    do {

                                        final ContactBean contacts = new ContactBean();
                                        contacts.setName(cr.getString(cr.getColumnIndexOrThrow(GroupModel.COl_GROUP_NAME)));
                                        contacts.setMemberCount(cr.getInt(cr.getColumnIndexOrThrow("mem")));
                                        contacts.setNumber(cr.getString(cr.getColumnIndexOrThrow(GroupModel.COl_GROUP_GID)));
                                        contacts.setIsAppUsing(cr.getInt(cr.getColumnIndexOrThrow(GroupModel.COL_GROUP_APPUSING)));
                                        contacts.setIsNotification(cr.getInt(cr.getColumnIndexOrThrow(GroupModel.COL_GROUP_NOTIFICATION)));
                                        contacts.setIsBlocked(cr.getInt(cr.getColumnIndexOrThrow(GroupModel.COL_GROUP_BLOCKED)));
                                        contacts.setNotificationTime(Long.parseLong(cr.getString(cr.getColumnIndexOrThrow(GroupModel.COL_GROUP_NOTIFICATION_TIME))));
                                        contacts.setType(1);

                                        if (!contacts.getNumber().equals(P.MOBILE_NUMBER)) {
                                            //Log.d(TAG, contacts.getName() + "GET GROUPS" + contacts.getNumber());

                                            DrawftModel dm = DrawftModel.getR(getApplicationContext());
                                            ArrayList<DrawftBean> covers = dm.getGroupDrawfts(contacts.getNumber());
                                            DrawftModel.closeDBConnection(dm);
                                            ArrayList<String> dimens = new ArrayList<String>();
                                            ArrayList<String> arr = new ArrayList<String>();

                                            for (int i = 0; i < covers.size(); i++) {
                                                DrawftBean dBean = covers.get(i);
                                                if (dBean.getFileName() != null) {
                                                    //arr.add(__fileUtil.getFilePath(dBean.getFileName()));
                                                    arr.add(__fileUtil.getFilePath1(contacts.getNumber() + "/" + dBean.getFileName()));
                                                    dimens.add(dBean.getDimensions());
                                                }
                                            }
                                            ContactTile tile = new ContactTile();
                                            tile.setMobileNumber(contacts.getNumber());
                                            tile.setGroupName(contacts.getName());
                                            tile.setGroupId(contacts.getNumber());
                                            tile.setIsGroup(true);
                                            tile.setCoverPic1(arr);
                                            tile.setDimensions(dimens);
                                            tile.setNotifications(contacts.getIsNotification());
                                            tile.setAppUsing(1);
                                            tile.setNotificationTime(contacts.getNotificationTime());
                                            tile.setMemberCount(contacts.getMemberCount());
                                            tile.setBlocked(contacts.getIsBlocked());

                                            if (tile.getNotifications() > 0) {
                                                notifications.add(tile);
                                            } else if (tile.getAppUsing() == 1) {
                                                appUsing.add(tile);
                                            } else if (tile.getBlocked() == 1) {
                                                blocked.add(tile);
                                            } else {
                                                tiles.add(tile);
                                            }
                                        }

                                    } while (cr.moveToNext());
                                    cr.close();
                                }
                                GroupModel.closeDBConnection(groupsDb);

                                ContactModel _db = ContactModel.getR(targetActivity);
                                cr = _db.getContacts();
                                DrawftModel dm = DrawftModel.getR(getApplicationContext());

                                if (cr != null && cr.getCount() > 0 && cr.moveToFirst()) {
                                    do {
                                        ContactBean contacts = new ContactBean();
                                        contacts.setName(cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NAME)));
                                        contacts.setNumber(cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_CONTACT_NUMBER)));
                                        contacts.setIsAppUsing(cr.getInt(cr.getColumnIndexOrThrow(ContactModel.COL_APPUSING)));
                                        contacts.setIsNotification(cr.getInt(cr.getColumnIndexOrThrow(ContactModel.COL_NOTIFICATION)));
                                        contacts.setIsBlocked(cr.getInt(cr.getColumnIndexOrThrow(ContactModel.COL_BLOCKED)));
                                        contacts.setNotificationTime(Long.parseLong(cr.getString(cr.getColumnIndexOrThrow(ContactModel.COL_NOTIFICATION_TIME))));
                                        //Log.d(TAG, contacts.getName() + "GET Contacts" + contacts.getNumber());

                                        if (!contacts.getNumber().equals(P.MOBILE_NUMBER)) {


                                            ArrayList<DrawftBean> covers = dm.getGroupDrawfts(GroupDrawft.getSortedNumber(P.MOBILE_NUMBER, contacts.getNumber()));
                                            ArrayList<String> arr = new ArrayList<String>();
                                            ArrayList<String> dimens = new ArrayList<String>();
                                            for (int i = 0; i < covers.size(); i++) {
                                                DrawftBean dBean = covers.get(i);
                                                // arr.add(__fileUtil.getFilePath1(dBean.getFileName()));
                                                arr.add(__fileUtil.getFilePath1(GroupDrawft.getSortedNumber(P.MOBILE_NUMBER, contacts.getNumber()) + "/" + dBean.getFileName()));
                                                dimens.add(dBean.getDimensions());
                                            }//for cover pic

                                            ContactTile tile = new ContactTile();
                                            tile.setMobileNumber(contacts.getNumber());
                                            if (contacts.getName().isEmpty()) {
                                                tile.setGroupName(contacts.getNumber());
                                            } else {
                                                tile.setGroupName(contacts.getName());
                                            }
                                            tile.setGroupId(contacts.getNumber());
                                            tile.setIsGroup(false);

                                            if (contacts.getIsAppUsing() == 1) {
                                                tile.setAppUsing(1);
                                            } else {
                                                tile.setAppUsing(0);
                                            }

                                            tile.setCoverPic1(arr);
                                            tile.setDimensions(dimens);
                                            tile.setNotifications(contacts.getIsNotification());
                                            tile.setBlocked(contacts.getIsBlocked());
                                            tile.setNotificationTime(contacts.getNotificationTime());

                                            if (tile.getNotifications() > 0) {
                                                notifications.add(tile);
                                            } else if (tile.getAppUsing() == 1) {
                                                if (tile.getBlocked() == 0) {
                                                    appUsing.add(tile);
                                                } else {
                                                    blocked.add(tile);
                                                }
                                            } else {
                                                tiles.add(tile);
                                            }
                                            contactsListNumbers.put(tile.getMobileNumber());
                                        }

                                    } while (cr.moveToNext());
                                    cr.close();
                                }
                                ContactModel.closeDBConnection(_db);
                                DrawftModel.closeDBConnection(dm);
                                targetActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        allTiles.clear();
                                        allTiles.addAll(notifications);
                                        allTiles.addAll(appUsing);
                                        allTiles.addAll(blocked);
                                        Collections.sort(allTiles);
                                        // Log.d(TAG, "Sorting List = " + allTiles.size());
                                        allTiles.addAll(tiles);
                                        targetActivity.listAdapter.concatList(allTiles);
                                        // Log.d(TAG, "Count = " + targetActivity.listAdapter.getCount());
                                        hideProgressLoader();
                                        slider.setVisibility(View.VISIBLE);

                                        //targetActivity.contactListView.setSelectionFromTop(listIndex, listPosition);
                                        hideProgressLoader();

                                    }
                                });

                            }
                        });
                    }
                    break;
                    case Do.SAVE_CONTACTS: {
                        GroupDrawft.runOnBackground(targetActivity, new Runnable() {
                            @Override
                            public void run() {
                                allTiles.clear();
                                PhoneNumberUtil phoneUtil;

                               /* //Groups
                                GroupModel groupsDb = GroupModel.getR(targetActivity);
                                Cursor cr = groupsDb.getGroups();

                                DrawftModel dm = DrawftModel.getR(getApplicationContext());*/


                                //Fetch Contacts
                                ContactModel.clearContacts(targetActivity);
                                Cursor managedCursor = targetActivity.getContentResolver().query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{
                                                ContactsContract.CommonDataKinds.Phone._ID
                                                , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                                                , ContactsContract.CommonDataKinds.Phone.NUMBER
                                        }, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE");
                                int nameFieldColumnIndex = 1;
                                int numberFieldColumnIndex = 2;
                                contactsListNumbers = new JSONArray();
                                final ExecutorService executor = Executors.newFixedThreadPool(5);
                                while (managedCursor.moveToNext()) {
                                    String contactName = managedCursor.getString(nameFieldColumnIndex);
                                    String number = managedCursor.getString(numberFieldColumnIndex);
                                    String mobileNumber = null;
                                    // Phone util initialization
                                    phoneUtil = PhoneNumberUtil.getInstance();

                                    try {
                                        if (phoneUtil.isValidNumber(phoneUtil.parse(number, P.COUNTRY_CODE_NAME))) {
                                            mobileNumber = phoneUtil.format(phoneUtil.parse(number, P.COUNTRY_CODE_NAME), PhoneNumberUtil.PhoneNumberFormat.E164);

                                            try {
                                                mobileNumber = mobileNumber.substring(1);
                                                //Log.d(TAG, contactName + "==" + mobileNumber);
                                                JSONObject contact = new JSONObject();
                                                try {
                                                    if ((!contactsListNumbers.toString().contains(mobileNumber)) && !mobileNumber.equals(P.MOBILE_NUMBER)) {
                                                        contact.put("name", contactName);
                                                        contact.put("number", mobileNumber);
                                                        contact.put("type", 0);
                                                        executor.execute(new ContactBean().new ContactBeanWorker(targetActivity, contact, managedCursor.getPosition() + 1));

                                                        contactsListNumbers.put(mobileNumber);
                                                        //send request to the server for notifications. favorites, Blocked
                                                        //Log.d("TAG", "Fetching Contact Details from Server");
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }


                                            } catch (Exception e) {
                                                targetActivity.runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(targetActivity, "Error in Getting Contacts! Please Try Again", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        } else {
                                            Log.d(TAG, contactName + "== Not valid");
                                        }
                                    } catch (NumberParseException e) {

                                        Log.d(TAG, contactName + "== Not valid Ca");
                                    }
                                }
                                managedCursor.close();
                                executor.shutdown();
                                while (!executor.isTerminated()) {
                                }
                                // dm.close();

                                P.CONTACTS_SAVED = true;
                                P.write(targetActivity);
                                targetActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                       /* allTiles.addAll(appUsing);
                                        allTiles.addAll(tiles);
                                        targetActivity.listAdapter.concatList(allTiles);*/
                                        httpClientUtil.getUserGroups(getUserGroupsListener, P.MOBILE_NUMBER, P.AUTH_CODE, contactsListNumbers);

                                        /*hideProgressLoader();
                                        slider.setVisibility(View.VISIBLE);*/
                                        Log.d(TAG, "Count Before SAVE= " + contactsListNumbers.length());
                                    }
                                });
                            }
                        });
                    }
                    break;

                    case Do.GET_NOTIFICATIONS: {
                        __listHandler.sendEmptyMessage(Do.GET_CONTACTS);
                        break;
                    }
                   /* case Do.SEND_CONTACTS: {

                        new ServiceRequestHelper().refreshContacts(contactsListener, target, P.DEVICE_ID, P.MOBILE_NUMBER, contactsListNumbers, P.AUTH_CODE);
                        refreshBtnClicked = false;
                        break;
                    }*/

                }

            } catch (Exception e) {
                // Log.d(TAG, "Error");
            }
        }

    }


    class GroupDataFromFB
            implements FirebaseUtil.GroupDataFromFB {
        GroupDataFromFB() {

        }

        public void onDataReceive(Map<String, String> blockList) {
            try {
                for (Map.Entry<String, String> entry : blockList.entrySet()) {

                    int position;
                    Date date = new Date();
                    if (entry.getValue().equals("group")) {
                        ContentValues cvCategory = new ContentValues();
                        cvCategory.put(GroupModel.COL_GROUP_BLOCKED, 1);
                        cvCategory.put(GroupModel.COL_GROUP_BLOCKED_AT, date.getTime() + "");
                        GroupModel.uOt(getApplicationContext(), GroupModel.TABLE_GROUPS, cvCategory, GroupModel.COl_GROUP_GID + "='" + entry.getKey() + "'", null);
                        position = CommunicationListActivity.this.listAdapter.getTilePositionById(entry.getKey());
                    } else {

                        ContentValues cvCategory = new ContentValues();
                        cvCategory.put(ContactModel.COL_BLOCKED, 1);
                    /*cvCategory.put(ContactModel.C, date.getTime() + "");*/
                        ContactModel.uOt(getApplicationContext(), ContactModel.TABLE_CONTACTS, cvCategory, ContactModel.COL_CONTACT_NUMBER + "='" + entry.getKey() + "'", null);
                        position = CommunicationListActivity.this.listAdapter.getTilePositionById(entry.getKey());
                    }
                    if (position > -1) {
                        ContactTile tile = CommunicationListActivity.this.listAdapter.getItem(position);
                        tile.setBlocked(1);
                    }


                }
                __listHandler.sendEmptyMessage(Do.GET_CONTACTS);
            } catch (Exception e) {

            }

            //CommunicationListActivity.this.loadUserGroupList(groupInfo);

        }
    }

    class MyGcmDispatcher
            implements GCMIntentService.GcmDispatcher {
        MyGcmDispatcher() {

        }


        public void onNewGcmNotification(String type, boolean isGroup, String groupId, String otherNumber) {
            switch (type) {
                case "on_new_drawft":
                    DrawftModel dm = DrawftModel.getR(getApplicationContext());
                    String lastSynced = dm.getLastSyncedDrawft(groupId);
                    DrawftModel.closeDBConnection(dm);
                    if (lastSynced.isEmpty()) {
                        lastSynced = "0";
                    }
                    String blockedAt = "0";
                    try {
                        if (isGroup) {
                            GroupModel gm = new GroupModel(CommunicationListActivity.this);
                            blockedAt = gm.getBlockedAt(groupId);
                            GroupModel.closeDBConnection(gm);
                        } else {
                            ContactModel cm = new ContactModel(CommunicationListActivity.this);
                            blockedAt = cm.getBlockedAt(otherNumber);
                            ContactModel.closeDBConnection(cm);
                        }
                    } catch (Exception e) {
                        int i = 0;
                    }

                    if (Long.parseLong(blockedAt) > Long.parseLong(lastSynced)) {
                        lastSynced = blockedAt;
                    }
                    if (P.REGISTERED_TIME > Long.parseLong(lastSynced)) {
                        lastSynced = P.REGISTERED_TIME + "";
                    }
                    CommunicationListActivity.this.__firebaseUtil.getOldDrawftsFrom(isGroup, groupId, otherNumber, lastSynced, false);
                    break;
                case "on_new_group":
                    CommunicationListActivity.__listHandler.sendEmptyMessage(CommunicationListActivity.Do.GET_CONTACTS);
                    break;
                case "on_edit_group":
                    CommunicationListActivity.__listHandler.sendEmptyMessage(CommunicationListActivity.Do.GET_CONTACTS);
                    break;
                case "on_modify_group_members":
                    break;
                case "on_delete_group":
                    int position = CommunicationListActivity.this.listAdapter.getTilePositionById(groupId);
                    if (position > -1) {
                        CommunicationListActivity.this.listAdapter.removeItem(position);
                        /*CommunicationListActivity.this.listAdapter.notifyDataSetChanged();*/
                    }

                    break;
            }

        }
    }

    class MyDrawableDataFromFB
            implements FirebaseUtil.DrawableDataFromFB {
        MyDrawableDataFromFB() {

        }

        public void onSaveDrawft(String groupId, String drawftId) {

        }

        public void onReceiveTime(String groupId, String drawftId, String time) {

        }

        public void onDataReceive(List list, String fName, String by, String time, String res) {

        }

        public void onOldDataReceive(boolean isGroup, String groupId, List list, String fName, String by, String time, String res) {
            if (!fName.isEmpty()) {
                String dim = CommunicationListActivity.this.__prepareDrawft.onReceive(groupId, list, fName, by, time, res);
                if (!dim.isEmpty()) {
                    try {
                        CommunicationListActivity.this.__firebaseUtil.removeDrawft(isGroup, fName, groupId, by);
                        //CommunicationListActivity.__listHandler.sendEmptyMessage(Do.GET_CONTACTS);
                        int position;
                        if (isGroup) {
                            position = CommunicationListActivity.this.listAdapter.getTilePositionById(groupId);
                        } else {
                            position = CommunicationListActivity.this.listAdapter.getTilePositionById(by);
                        }

                        ContactTile tile = CommunicationListActivity.this.listAdapter.getItem(position);
                        DrawftModel dm = DrawftModel.getR(getApplicationContext());

                        if (isGroup) {
                            ArrayList<DrawftBean> covers = dm.getGroupDrawfts(groupId);
                            ArrayList<String> dimens = new ArrayList<String>();
                            ArrayList<String> arr = new ArrayList<String>();

                            for (int i = 0; i < covers.size(); i++) {
                                DrawftBean dBean = covers.get(i);
                                if (dBean.getFileName() != null) {
                                    //arr.add(__fileUtil.getFilePath(dBean.getFileName()));
                                    arr.add(__fileUtil.getFilePath1(groupId + "/" + dBean.getFileName()));
                                    dimens.add(dBean.getDimensions());
                                }
                            }

                            tile.setDimensions(dimens);
                            tile.setCoverPic1(arr);
                            ContentValues cvCategory = new ContentValues();
                            cvCategory.put(GroupModel.COL_GROUP_NOTIFICATION_TIME, time);
                            GroupModel.uOt(getApplicationContext(), GroupModel.TABLE_GROUPS, cvCategory, GroupModel.COl_GROUP_GID + "='" + groupId + "'", null);
                        } else {
                            ArrayList<DrawftBean> covers = dm.getGroupDrawfts(GroupDrawft.getSortedNumber(P.MOBILE_NUMBER, by));
                            ArrayList<String> arr = new ArrayList<String>();
                            ArrayList<String> dimens = new ArrayList<String>();
                            for (int i = 0; i < covers.size(); i++) {
                                DrawftBean dBean = covers.get(i);
                                // arr.add(__fileUtil.getFilePath1(dBean.getFileName()));
                                arr.add(__fileUtil.getFilePath1(GroupDrawft.getSortedNumber(P.MOBILE_NUMBER, by) + "/" + dBean.getFileName()));
                                dimens.add(dBean.getDimensions());
                            }

                            tile.setDimensions(dimens);
                            tile.setCoverPic1(arr);
                            ContentValues cvCategory = new ContentValues();
                            cvCategory.put(ContactModel.COL_NOTIFICATION_TIME, time);
                            ContactModel.uOt(getApplicationContext(), ContactModel.TABLE_CONTACTS, cvCategory, ContactModel.COL_CONTACT_NUMBER + "='" + by + "'", null);

                        }
                        DrawftModel.closeDBConnection(dm);
                        tile.setNotifications(1);
                        CommunicationListActivity.this.listAdapter.removeItem(position);
                        CommunicationListActivity.this.listAdapter.addItemAtFirst(tile);
                    } catch (Exception e) {

                    }

                }
            }
        }

        public void onConnection() {

        }

        public void onAuthError() {
            httpClientUtil.getFBAuthCode(getFBAuthCodeListener, P.MOBILE_NUMBER, P.AUTH_CODE);
        }

        public void onLastSeenReceive(Long time) {

        }
    }


    public android.widget.Filter getFilter() {
        return new android.widget.Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered = (ArrayList<ContactTile>) results.values;
                if (results.count > 0) {
                    listAdapter.concatList(filtered);

                    contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                            if (filtered.get(pos).getAppUsing() == 1 || filtered.get(pos).getIsGroup() == true) {
                                CommunicationListActivity.this.openNewActivity(pos);
                            } else {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "DRAWFT lets us communicate with drawings, like Early Man did.\n Download Now . <..Link..>");
                                sendIntent.setType("text/plain");

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(arg0.getWindowToken(), 0);

                                String shareTitle = CommunicationListActivity.this.getResources().getString(R.string.share_title) + " " + filtered.get(pos).getGroupName();
                                CommunicationListActivity.this.startActivity(Intent.createChooser(sendIntent, shareTitle));

                            }
                        }
                    });

                } else {
                    listAdapter.concatList(filtered);
                    listAdapter.notifyDataSetInvalidated();
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString().toLowerCase();
                FilterResults Result = new FilterResults();
                ArrayList<ContactTile> tiles1 = new ArrayList<>();
                ArrayList<ContactTile> con = new ArrayList<>();
                synchronized (this) {
                    tiles1.addAll(allTiles);
                }
                if (constraint == null && constraint.length() == 0) {
                    ArrayList<ContactTile> list = new ArrayList<>();
                    synchronized (this) {
                        list.addAll(tiles1);
                    }
                    Result.values = list;
                    Result.count = list.size();
                    return Result;
                }
                for (int i = 0; i < tiles1.size(); i++) {
                    ContactTile ct = tiles1.get(i);

                    if (ct.getGroupName().toLowerCase().contains(constraint)) {
                        con.add(ct);
                    }
                }
                Result.count = con.size();
                Result.values = con;
                return Result;
            }
        };
    }


}
