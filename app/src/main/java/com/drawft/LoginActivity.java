package com.drawft;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.drawft.GroupDrawft.P;
import com.drawft.util.TelephoneUtils;

import java.util.ArrayList;


public class LoginActivity extends Activity {
    public TextView verifyTerms, phoneNumber, callNum1, callNum2, callNum3, callInfo, countryCodeIcon, phoneIcon, countryField, codeField;
    public LinearLayout callNum;
    public EditText phoneNumberEditText;
    public Button dialImageView;
    public String mobileNumber;
    public String countryCode;
    public PhoneNumberUtil phoneUtil;
    public static String regId = "";
    public static String contactNumber = "";
    public static boolean firstOnCreate = false;
    private ProgressBar main_wait = null;
    public boolean countryCodeShown = false;
    public Dialog mDialog = null;
    ProgressBar loadingTerms = null;
    TextView textTitleTerms = null;
    Button okBtnTerms = null;
    WebView webview = null;
    public AlertDialog dialog = null;
    public boolean countryClicked = false;
    public int callState = 0;
    public TelephonyManager telephonyManager = null;
    StateListener phoneStateListener = null;
    public boolean dialImageClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        P.read(this);

        if (P.USER_REGISTERED) {
            LoginActivity.this.finish();
            Intent callIntent = new Intent(LoginActivity.this, CommunicationListActivity.class);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(callIntent);
        } else {
            setContentView(R.layout.activity_login);
            initViews();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
            // L.fe(getApplicationContext(), Event.EXCEPTION, e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // check if already logged in,, if yes, take the user to contacts page && P.AUTH_CODE.equals("")
        if (GroupDrawft.isNetworkOK(LoginActivity.this)) {
            // callState = 0;
            // L.fi(LoginActivity.this, Event.VERBOSE, "onResume");
            if (!P.GCM_REGISTERED) {
                checkNotNull(GroupDrawft.SENDER_ID, "285101437973");
                // Make sure the device has the proper dependencies.
                GCMRegistrar.checkDevice(this);
                // Make sure the manifest was properly set - comment out this line
                // while developing the app, then uncomment it when it's ready.
                GCMRegistrar.checkManifest(this);
                //
                registerReceiver(mHandleMessageReceiver, new IntentFilter(GroupDrawft.DISPLAY_MESSAGE_ACTION));
                registerGCM();
            } else if (P.USER_REGISTERED) {
                LoginActivity.this.finish();
                Intent callIntent = new Intent(LoginActivity.this, CommunicationListActivity.class);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(callIntent);
            } else
            // if(!dialImageClicked)
            {
                if (!dialImageClicked) {
                    getPhoneNumber();
                } else {
                    dialImageClicked = false;
                    showProgressBar();
                    // sendLogstoLoggly(phoneNumber.getText().toString() + " On Resume From Missed call screen ");
                }
            }
            // else{
            // dialImageClicked = false;
            // showProgressBar();
            // }
        } else {
            openConnectionDialog();
        }
    }

    private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(getString(R.string.error_config, name));
        }
    }

    private void registerGCM() {
        try {
            if (!P.USER_REGISTERED) {
                GCMRegistrar.checkDevice(this);
                GCMRegistrar.checkManifest(this);
                if (GCMRegistrar.isRegistered(this)) {
                    Log.d("info", GCMRegistrar.getRegistrationId(this));
                }
                GCMRegistrar.register(this, GroupDrawft.SENDER_ID);
            } else {
                Log.d("info", "already registered as" + regId);
            }
        } catch (Exception e) {
            // L.fe(getApplicationContext(), GroupDrawft.Event.EXCEPTION, e);
        }
    }

    public void hideProgressBar() {
        main_wait.setVisibility(View.GONE);
        callInfo.setVisibility(View.GONE);
        dialImageView.setVisibility(View.VISIBLE);
        if (phoneNumber.getVisibility() == View.VISIBLE) phoneNumber.setVisibility(View.GONE);
        phoneNumberEditText.setVisibility(View.VISIBLE);
        countryField.setVisibility(View.VISIBLE);
        codeField.setVisibility(View.VISIBLE);
        countryCodeIcon.setVisibility(View.VISIBLE);
        phoneIcon.setVisibility(View.VISIBLE);
        verifyTerms.setVisibility(View.VISIBLE);
        callNum.setVisibility(View.VISIBLE);
    }

    public void getPhoneNumber() {
        hideProgressBar();
        if (mobileNumber != null && !mobileNumber.equals("")) {
            // Phone number standardization
            try {
                mobileNumber = phoneUtil.format(phoneUtil.parse(mobileNumber, countryCode.toUpperCase()), PhoneNumberUtil.PhoneNumberFormat.E164);
                phoneNumber.setText(mobileNumber);
            } catch (NumberParseException e) {
                //L.fe(getApplicationContext(), Event.EXCEPTION, e);
            }
        } else {
            // Prompt Number
            phoneNumber.setVisibility(View.GONE);
            phoneNumberEditText.setVisibility(View.VISIBLE);
            phoneNumberEditText.requestFocus();
        }
        // Dial action
        dialImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialImageClicked = true;
                phoneStateListener = new StateListener();
                telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                P.AUTH_CODE = "";
                P.write(LoginActivity.this);
                if (countryField.getText().toString().equalsIgnoreCase("Invalid Region")) {
                    Toast.makeText(LoginActivity.this, "Please enter your valid country code", Toast.LENGTH_SHORT).show();
                } else {
                    if (countryCode == null || countryCode.isEmpty()) {
                        if (!countryCodeShown) {
                            Toast.makeText(LoginActivity.this, "Please enter your valid country code", Toast.LENGTH_SHORT).show();
                            countryCodeShown = true;
                        } else {
                            mobileNumber = phoneNumberEditText.getText().toString();
                            phoneNumber.setText(mobileNumber);
                            showProgressBar();
                            callInfo.setVisibility(View.VISIBLE);
                            init(GCMRegistrar.getRegistrationId(LoginActivity.this), mobileNumber);
                        }
                    } else {
                        try {
                            if (phoneUtil.isValidNumber(phoneUtil.parse(phoneNumberEditText.getText().toString(), countryCode.toUpperCase()))) {
                                mobileNumber = phoneUtil.format(phoneUtil.parse(phoneNumberEditText.getText().toString(), countryCode.toUpperCase()), PhoneNumberUtil.PhoneNumberFormat.E164);
                                phoneNumber.setText(mobileNumber);
                                if (phoneNumber.getText().toString().equals("") || phoneNumber.getText().toString().equals("Enter your number") || phoneNumber.getText().toString() == null) {
                                    Toast.makeText(LoginActivity.this, "Please enter your valid phone number", Toast.LENGTH_SHORT).show();
                                } else {
                                    showProgressBar();
                                    callInfo.setVisibility(View.VISIBLE);
                                    init(GCMRegistrar.getRegistrationId(LoginActivity.this), phoneNumber.getText().toString());
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                                phoneNumberEditText.requestFocus();
                            }
                        } catch (NumberParseException e) {
                            Toast.makeText(LoginActivity.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                            // L.fe(getApplicationContext(), Event.EXCEPTION, e);
                        }
                    }
                }
                //sendLogstoLoggly(phoneNumber.getText().toString() + " Dial image button clicked ");
            }
        });
    }

    private void init(String push_notification_id, String mobile_no) {
        String device_id = TelephoneUtils.getDeviceId(LoginActivity.this);
        // String countryField = ;
        // new ServiceRequestHelper().initialize(initListener, InitializationActivity.this, device_id, push_notification_id, mobile_no, phoneNumberEditText.getText().toString(), countryField.getText().toString());
    }

    public final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(GroupDrawft.EXTRA_MESSAGE);
            if (P.GCM_REGISTERED) {
                // get the phone number
                getPhoneNumber();
            }
            // if (newMessage.equals(P.AUTH_CODE))
            // {
            // registerUser();
            // }
        }
    };

    private void initViews() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        countryCode = manager.getSimCountryIso().toUpperCase();
        phoneUtil = PhoneNumberUtil.getInstance();
        verifyTerms = (TextView) findViewById(R.id.terms);
        verifyTerms.setTypeface(GroupDrawft.fontHelsinki);
        phoneNumber = (TextView) findViewById(R.id.phoneNumber);
        phoneNumber.setTypeface(GroupDrawft.fontHelsinki);
        countryField = (TextView) findViewById(R.id.countryName);
        countryField.setTypeface(GroupDrawft.fontHelsinki);
        codeField = (TextView) findViewById(R.id.country_code);
        codeField.setTypeface(GroupDrawft.fontHelsinki);
        countryCodeIcon = (TextView) findViewById(R.id.country_icon);
        //countryCodeIcon.setTypeface(GroupDrawft.fontAwesome);
        countryCodeIcon.setShadowLayer(4.5f, -1, 1, Color.DKGRAY);
        phoneIcon = (TextView) findViewById(R.id.phone_icon);
        //phoneIcon.setTypeface(GroupDrawft.fontAwesome);
        phoneIcon.setShadowLayer(6.5f, -1, 1, Color.DKGRAY);
        callNum = (LinearLayout) findViewById(R.id.callNum);
        callNum1 = (TextView) findViewById(R.id.callNum1);
        callNum2 = (TextView) findViewById(R.id.callNum2);
        callNum3 = (TextView) findViewById(R.id.callNum3);
        callInfo = (TextView) findViewById(R.id.callInfo);
        callInfo.setTypeface(GroupDrawft.fontHelsinki);
        callNum1.setTypeface(GroupDrawft.fontHelsinki);
        callNum2.setTypeface(GroupDrawft.fontHelsinki);
        callNum3.setTypeface(GroupDrawft.fontHelsinki);
        // String textCall = "GIVE US A CALL\n- TOLL FREE -\nTO VERIFY YOUR NUMBER";
        // Spanned sp = Html.fromHtml( "<span>GIVE US A Call</span><br><font  color='red'><h2>TOLL FREE</h2></font><br><spanp>TO VERIFY YOUR NUMBER</span>" );
        // // callNum.setText(sp);
        phoneNumberEditText = (EditText) findViewById(R.id.contactNumEditText);
        phoneNumberEditText.setTypeface(GroupDrawft.fontHelsinki);
        phoneNumberEditText.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);
        try {
            mobileNumber = TelephoneUtils.getPhoneNumber(LoginActivity.this);
        } catch (Exception e) {

        }
        dialImageView = (Button) findViewById(R.id.dial);
        dialImageView.setShadowLayer(6.5f, -1, 1, Color.DKGRAY);
        main_wait = (ProgressBar) findViewById(R.id.main_wait);
        if (!countryCode.isEmpty()) {
            countryField.setText(GetCountryName(countryCode));
        }
        countryField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCountryDialog();
            }
        });
        codeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                if (!countryClicked)
                    checkCountryName(arg0.toString());
                else
                    countryClicked = false;

            }
        });
        countryCodeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCountryDialog();
            }
        });
        verifyTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GroupDrawft.isNetworkOK(LoginActivity.this)) {
                    openTermsOfUseDialog();
                } else {
                    openConnectionDialog();
                }
            }
        });
        showProgressBar();
    }

    public void openTermsOfUseDialog() {
        if (null != mDialog) mDialog.dismiss();
        //
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout dialogView = (RelativeLayout) inflater.inflate(R.layout.dialog_terms, null);
        //
        loadingTerms = (ProgressBar) dialogView.findViewById(R.id.main_wait_1);
        webview = (WebView) dialogView.findViewById(R.id.body);
        textTitleTerms = (TextView) dialogView.findViewById(R.id.terms_wrapper);
        textTitleTerms.setTypeface(GroupDrawft.fontHelsinki);
        textTitleTerms.setShadowLayer(4.5f, -1, 1, Color.BLACK);
        okBtnTerms = (Button) dialogView.findViewById(R.id.dialog_close);
        okBtnTerms.setTypeface(GroupDrawft.fontFeather);
        loadingTerms.setVisibility(View.VISIBLE);
        //new ServiceRequestHelper().fetchTermsOfUse(termsListener, InitializationActivity.this, P.MOBILE_NUMBER, P.AUTH_CODE);
        builder.setView(dialogView);
        //
        mDialog = builder.create();
        mDialog.setCancelable(false);
        okBtnTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        mDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    //
                }
                return true;
            }
        });
        mDialog.show();
    }

    public void openConnectionDialog() {
//        if (null != mDialog) mDialog.dismiss();
//        //
//        GroupDrawft.fireBaseConnected = false;
//        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//        LayoutInflater inflater = getLayoutInflater();
//        LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.dialog_no_internet, null);
//        //
//        TextView textTitle = (TextView) dialogView.findViewById(R.id.txtTitle);
//        textTitle.setTypeface(GroupDrawft.fontHelsinki);
//        textTitle.setShadowLayer(4.5f, -1, 1, Color.DKGRAY);
//        Button retryBtn = (Button) dialogView.findViewById(R.id.retryBtn);
//        retryBtn.setTypeface(GroupDrawft.fontHelsinki);
//        retryBtn.setShadowLayer(4.5f, -1, 1, Color.DKGRAY);
//        builder.setView(dialogView);
//        //
//        mDialog = builder.create();
//        mDialog.setCancelable(false);
//        retryBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (GroupDrawft.isNetworkOK(LoginActivity.this)) {
//                    mDialog.dismiss();
//                    onResume();
//                } else {
//                    openConnectionDialog();
//                }
//            }
//        });
//        mDialog.setOnKeyListener(new Dialog.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    dialog.dismiss();
//                    //
//                    LoginActivity.this.finish();
//                }
//                return true;
//            }
//        });
//        mDialog.show();
    }

    public void showProgressBar() {
        main_wait.setVisibility(View.VISIBLE);
        callInfo.setVisibility(View.GONE);
        dialImageView.setVisibility(View.GONE);
        phoneNumber.setVisibility(View.GONE);
        countryField.setVisibility(View.GONE);
        codeField.setVisibility(View.GONE);
        countryCodeIcon.setVisibility(View.GONE);
        phoneIcon.setVisibility(View.GONE);
        phoneNumberEditText.setVisibility(View.GONE);
        verifyTerms.setVisibility(View.GONE);
        callNum.setVisibility(View.GONE);
    }

    public void checkCountryName(String CountryID) {
        boolean countryExists = false;
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[0].trim().equalsIgnoreCase(CountryID.trim())) {
                countryCode = g[1];
                countryField.setText(g[2]);
                countryExists = true;
                P.COUNTRY_CODE = g[1];
                P.write(this);
                break;
            }
        }
        if (!countryExists) {
            countryField.setText("Invalid Region");
        }
    }

    public String GetCountryName(String CountryID) {
        String CountryName = "";
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equalsIgnoreCase(CountryID.trim())) {
                CountryName = g[2];
                codeField.setText(g[0]);
                P.COUNTRY_CODE = g[1];
                P.write(this);
                break;
            }
        }
        return CountryName;
    }

    public void openCountryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Select your region");
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout dialogView = (RelativeLayout) inflater.inflate(R.layout.country_view, null);
        ListView list = (ListView) dialogView.findViewById(R.id.myListView);
        ArrayList<String> stringList = InitListViewData();
        dataListAdapter adapter = new dataListAdapter(LoginActivity.this, stringList);
        list.setAdapter(adapter);
        builder.setView(dialogView);
        dialog = builder.create();
        dialog.show();
    }


    private ArrayList<String> InitListViewData() {
        ArrayList<String> stringList = new ArrayList<String>();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            stringList.add(g[2]);
        }
        return stringList;
    }


    class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // System.out.println("call Activity off hook");
                    callState += state;
                    // Toast.makeText(getApplicationContext(), "call Activity off hook " + incomingNumber +" callState " + callState, Toast.LENGTH_LONG).show();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    // callState = state;
                    // Toast.makeText(getApplicationContext(), "CALL_STATE_IDLE " + incomingNumber +" callState " + callState, Toast.LENGTH_LONG).show();
                    if (callState >= 2) {
                        // call api for auth code
                        showProgressBar();
                        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                        Handler launchCategory = new Handler();
                        launchCategory.postDelayed(new Runnable() {
                            public void run() {
                                Log.d(GroupDrawft.TAG, " \n\n\nClass: init, Method : CALL_STATE_IDLE -- " + phoneNumber.getText().toString() + "\n\n\n");
                                String device_id = TelephoneUtils.getDeviceId(LoginActivity.this);
                                //new ServiceRequestHelper().getAuthCode(initAuthListener, InitializationActivity.this, phoneNumber.getText().toString(), device_id);
                                callState = 0;
                            }
                        }, 1000);
                        //sendLogstoLoggly(phoneNumber.getText().toString() + " TelephonyManager CALL_STATE_MISS_CALLED ");
                    }
                    break;
            }
        }
    }

    ;

    class dataListAdapter extends BaseAdapter {
        private ArrayList<String> stringArray;
        private Context context;
        private LayoutInflater mInflater;

        public dataListAdapter(Context _context, ArrayList<String> arr) {
            stringArray = arr;
            context = _context;
            mInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return stringArray.size();
        }

        public Object getItem(int arg0) {
            return stringArray.get(arg0);
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder = null;
            if (view == null) {
                view = mInflater.inflate(R.layout.listview_row, parent, false);
                holder = new ViewHolder();
                holder.header = (TextView) view.findViewById(R.id.section);
                holder.countryLayout = (RelativeLayout) view.findViewById(R.id.countryLayout);
                holder.countryName = (TextView) view.findViewById(R.id.countrySelName);
                holder.countryRadio = (RadioButton) view.findViewById(R.id.countryRadio);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            String label = stringArray.get(position);
            char firstChar = label.toUpperCase().charAt(0);
            if (position == 0) {
                holder.header.setVisibility(View.VISIBLE);
                holder.countryLayout.setVisibility(View.GONE);
                holder.header.setText(label.substring(0, 1).toUpperCase());
            } else {
                String preLabel = stringArray.get(position - 1);
                char preFirstChar = preLabel.toUpperCase().charAt(0);
                if (firstChar != preFirstChar) {
                    holder.header.setVisibility(View.VISIBLE);
                    holder.countryLayout.setVisibility(View.GONE);
                    holder.header.setText(label.substring(0, 1).toUpperCase());
                } else {
                    holder.header.setVisibility(View.GONE);
                    holder.countryLayout.setVisibility(View.VISIBLE);
                    holder.countryName.setText(label);
                    if (label.equalsIgnoreCase(countryField.getText().toString())) {
                        holder.countryRadio.setChecked(true);
                    } else {
                        holder.countryRadio.setChecked(false);
                    }
                    holder.countryName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            countrySelectedInfo(stringArray.get(position));
                        }
                    });
                    holder.countryRadio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            countrySelectedInfo(stringArray.get(position));
                        }
                    });
                }
            }
            return view;
        }


        public class ViewHolder {
            RelativeLayout countryLayout;
            TextView header;
            TextView countryName;
            RadioButton countryRadio;
        }
    }

    public void countrySelectedInfo(String countrySel) {
        countryClicked = true;
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[2].trim().equals(countrySel.trim())) {
                countryCode = g[1];
                countryField.setText(g[2]);
                codeField.setText(g[0]);
                P.COUNTRY_CODE = g[1];
                P.write(this);
                break;
            }
        }
    }

}
