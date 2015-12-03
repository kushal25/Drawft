package com.drawft;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drawft.GroupDrawft.P;
import com.drawft.service.HttpClientUtil;
import com.drawft.util.TelephoneUtils;
import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.matesnetwork.callverification.Cognalys;
import com.matesnetwork.interfaces.VerificationListner;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SignUpActivity extends Activity {
    TextView logo = null, next = null, countryField, countryCodeField = null, verifyTerms;
    public EditText phoneNumberEditText;
    public AlertDialog dialog = null;
    public boolean countryClicked = false;
    public String countryCode;
    public Dialog mDialog = null;
    ProgressBar loadingTerms = null;
    WebView webview = null;
    TextView textTitleTerms = null;
    Button okBtnTerms = null;
    HttpClientUtil httpClientUtil = new HttpClientUtil();
    RelativeLayout dialogView = null;
    AlertDialog.Builder alertDialog = null;

    private List<String> myColorList = GroupDrawft.getColorList();
    public int color1, color2, color3;

    boolean verifyBtnClicked = false;
    int registrationTryCount = 1;
    String cognalysAppId = null, cognalysAppToken = null;
    TextView title,cancel,ok,noteVerify,termsText;
    View sepRight;
    CountDownTimer cdt;
    int timerFlag = 0;



    JsonHttpResponseHandler initListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                Log.d(GroupDrawft.TAG, " \n\n\nClass: init, Method : initListener -- " + response.getBoolean("success") + "\n\n\n");
                if (response.getBoolean("success")) {
                    cognalysAppId = response.getString("cog_app_id");
                    cognalysAppToken = response.getString("cog_app_token");
                    verifyUserNumber();
                } else {
                    // hideProgressBar();
                    GroupDrawft.P.GCM_REGISTERED = false;
                    GroupDrawft.P.write(getApplicationContext());
                    Toast.makeText(getApplicationContext(), getString(R.string.error_gcm_registered), Toast.LENGTH_SHORT).show();
                    onResume();
                }
                GroupDrawft.P.write(getApplicationContext());
            } catch (JSONException e) {
                // hideProgressBar();
                GroupDrawft.P.GCM_REGISTERED = false;
                GroupDrawft.P.write(getApplicationContext());
                Toast.makeText(getApplicationContext(), getString(R.string.error_gcm_registered), Toast.LENGTH_SHORT).show();
                //onResume();
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

    JsonHttpResponseHandler registerListener = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getBoolean("success")) {
                    P.AUTH_CODE = response.getString("auth_code");
                    P.USER_REGISTERED = true;
                    P.REGISTERED_TIME = System.currentTimeMillis();
                    P.FB_TOKEN = response.getString("fb_token");
                    P.FB_URL = response.getString("fb_url");
                    P.SHARING_MSG = response.getString("share_msg");
                    P.write(getApplicationContext());
                    Toast.makeText(getApplicationContext(), "Registration Success.", Toast.LENGTH_SHORT).show();
                    goToCommunication();
                } else {
                    if (registrationTryCount <= 3) {
                        registrationTryCount++;
                        doRegister();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_gcm_registered), Toast.LENGTH_SHORT).show();
                    }

                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Error in cache", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String err, Throwable e) {
            // int j = 0;
        }
    };
    JsonHttpResponseHandler fetchTermsListener = new JsonHttpResponseHandler() {
        final String termsString = "<span style=\"color:#1a2a3a;\"><div style='text-align:center;padding-top:10px;font-weight:bold'>We are Drawft.</div><br/>  <div style='text-align:center;font-style:italic;'>You can use Drawft only if you agree to all these terms.</div><br/>  <div style=\"padding-right:24px;text-align:justify;\"><ol><li>We collect your private data, mainly your phonebook. We log certain data like when you login and logout and from where. We never share this data with anyone except as required to provide you our service or when we receive a legal order.</li><br/>  <li>When you delete something it is deleted immediately from our servers.</li><br/>  <li>We are not responsible for any good or bad you may or may not incur by your direct or indirect use of Drawft.</li><br/>   <li>You cannot use Drawft in any way that interferes others from using Drawft.</li><br/>  <li>We do not guaratee that our servers will be up and running all the time.</li><br/>    <li>We may discontinue Drawft any time without prior notice to you.</li><br/>  <li>We may modify these terms anytime by making an announcement on our Twitter page  <a target='_blank' href='https://twitter.com/sayitwithoutwords' style=\"text-decoration:none;color:#1ca5ec;\" > @sayitwithoutwords.</a></li></ol></div> <div style='text-align:center;font-style:italic;'>End.</div><br/></span>";


        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            LayoutInflater inflater = getLayoutInflater();
            //final RelativeLayout dialogView = (RelativeLayout) inflater.inflate(R.layout.dialog_verification, null);
            final WebView webview1 = (WebView) dialogView.findViewById(R.id.body);
            webview1.setBackgroundColor(getResources().getColor(R.color.light_grey));
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

    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        public void onClick(View paramAnonymousView) {

            switch (paramAnonymousView.getId()) {
                case R.id.next:

                    if (GroupDrawft.isNetworkOK(SignUpActivity.this)) {
                        phoneNumberEditText.clearFocus();
                        countryCodeField.clearFocus();

                        if (GroupDrawft.allowEmulator) {
                            verifyBtnClicked = true;
                            verifyUser();
                            goToCommunication();
                            return;
                        }

                        if (!countryCodeField.getText().toString().isEmpty() && !phoneNumberEditText.getText().toString().isEmpty()) {

                            LayoutInflater inflater = getLayoutInflater();
                            alertDialog = new AlertDialog.Builder(SignUpActivity.this);
                            dialogView = (RelativeLayout) inflater.inflate(R.layout.dialog_verification, null);

                            title = (TextView) dialogView.findViewById(R.id.title);
                            cancel = (TextView) dialogView.findViewById(R.id.cancel);
                            ok = (TextView) dialogView.findViewById(R.id.ok);
                            noteVerify = (TextView) dialogView.findViewById(R.id.noteVerify);
                            termsText = (TextView) dialogView.findViewById(R.id.termsText);
                            final RelativeLayout header_wrapper = (RelativeLayout) dialogView.findViewById(R.id.header_wrapper);
                            final RelativeLayout webView_wrapper = (RelativeLayout) dialogView.findViewById(R.id.webViewWrapper);
                            sepRight = dialogView.findViewById(R.id.sepRight);
                            final Animation fadeIn = AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.fade_in);
                            final Animation fadeOut = AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.fade_out);


                            title.setTypeface(GroupDrawft.robotoBold);
                            cancel.setTypeface(GroupDrawft.robotoBold);
                            ok.setTypeface(GroupDrawft.robotoBold);
                            noteVerify.setTypeface(GroupDrawft.robotoBold);
                            termsText.setTypeface(GroupDrawft.robotoLight);
                            noteVerify.setText(getString(R.string.noteVerify1) + " " + countryCodeField.getText() + "-" + phoneNumberEditText.getText());
                            SpannableString ss = new SpannableString(Html.fromHtml("By using Drawft you agree to these <font color='#EE0000'>terms</font>"));
                            ClickableSpan clickableSpan = new ClickableSpan() {
                                @Override
                                public void onClick(View textView) {
                                    webView_wrapper.setVisibility(View.VISIBLE);
                                    title.setText("Terms");
                                    ok.setText("Agree");
                                    cancel.setText("Disagree");
                                    noteVerify.setVisibility(View.GONE);
                                    termsText.setVisibility(View.GONE);

                                    if (GroupDrawft.isNetworkOK(SignUpActivity.this)) {
                                        httpClientUtil.fetchTermsOfUse(fetchTermsListener);
                                    } else {
                                        openConnectionDialog();
                                    }

                                }
                            };
                            ss.setSpan(clickableSpan, 35, 40, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            termsText.setText(ss);
                            termsText.setMovementMethod(LinkMovementMethod.getInstance());
                            settingColor();
                            header_wrapper.setBackgroundColor(color1);
                            cancel.setBackgroundColor(color2);
                            ok.setBackgroundColor(color3);
                            alertDialog.setView(dialogView);
                            mDialog = alertDialog.create();
                            mDialog.setCancelable(false);
//                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                            InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                            ok.setOnClickListener(new View.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
                                @Override
                                public void onClick(View v) {

                                    if (GroupDrawft.isNetworkOK(SignUpActivity.this)) {
                                        termsText.setVisibility(View.INVISIBLE);
                                        title.setText("Verifying");
                                        cancel.setText("cancel");
                                        ok.setVisibility(View.GONE);
                                        sepRight.setVisibility(View.GONE);
                                        webView_wrapper.setVisibility(View.GONE);
                                        noteVerify.setVisibility(View.VISIBLE);
                                        RelativeLayout.LayoutParams layoutParams =
                                                (RelativeLayout.LayoutParams) noteVerify.getLayoutParams();
                                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                                        noteVerify.setLayoutParams(layoutParams);
                                        noteVerify.setText("");
                                        noteVerify.setTextSize(50);
                                        noteVerify.setTextColor(getResources().getColor(R.color.number_color));
                                        cdt = new CountDownTimer(30000, 1000) {

                                            public void onTick(long millisUntilFinished) {
                                                timerFlag =1;
                                                noteVerify.setText("" + millisUntilFinished / 1000);

                                                if (millisUntilFinished / 1000 == 10) {
                                                    termsText.setVisibility(View.VISIBLE);
                                                    termsText.setText("Stay cool. We'll get you in.");
                                                    termsText.setAnimation(fadeIn);
                                                } else if (millisUntilFinished / 1000 == 1) {
                                                    RelativeLayout.LayoutParams layoutParams =
                                                            (RelativeLayout.LayoutParams) noteVerify.getLayoutParams();
                                                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
                                                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                                                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                                                    noteVerify.setLayoutParams(layoutParams);
                                                    title.setText("Oops !");
                                                    noteVerify.setText("Could not Verify Number" + " " + countryCodeField.getText() + "-" + phoneNumberEditText.getText());
                                                    noteVerify.setTextSize(25);
                                                    noteVerify.setTextColor(getResources().getColor(R.color.dark_color));
                                                    ok.setVisibility(View.VISIBLE);
                                                    sepRight.setVisibility(View.VISIBLE);
                                                    termsText.setAnimation(fadeOut);
                                                    termsText.setVisibility(View.INVISIBLE);
                                                    ok.setText("Retry");
                                                    cancel.setText("Edit Number");
                                                }
                                            }

                                            public void onFinish() {
                                            }
                                        }.start();

                                        verifyBtnClicked = true;
                                        verifyUser();
                                    } else {
                                        openConnectionDialog();
                                    }
                                }
                            });

                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(timerFlag==1) {
                                        cdt.cancel();
                                    }
                                    mDialog.dismiss();
                                }
                            });
                            mDialog.show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Fill all fields.", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        openConnectionDialog();
                    }

                    break;
//                case R.id.countryName:
//                    openCountryDialog();
//                    break;
//                case R.id.terms:
//                    onClickTerms();
//                    break;
            }
            // throw new RuntimeException("This is a crash");


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

    public void goToCommunication() {
        SignUpActivity.this.finish();
        Intent callIntent = new Intent(SignUpActivity.this, CommunicationListActivity.class);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(callIntent);
        onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        if (!GroupDrawft.P.USER_REGISTERED) {
            initViews();
            if (!GroupDrawft.P.GCM_REGISTERED)
                registerGCM();
            String code = Cognalys.getCountryCode(getApplicationContext());
            countryCodeField.setText(code);
            P.COUNTRY_CODE = code;
            checkCountryName(code.replace("+", ""));
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            String number = tm.getLine1Number();
            phoneNumberEditText.setText(number);
            if (number != null) {
                P.MOBILE_NUMBER = code.replace("+", "") + number;
            }
            int mWidth, mHeight;
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            mWidth = displaymetrics.widthPixels;
            mHeight = displaymetrics.heightPixels;
            P.RESOLUTION = mHeight + "X" + mWidth;
            P.write(getApplicationContext());
            //Toast.makeText(getApplicationContext(), "Code = " + code, Toast.LENGTH_SHORT).show();
        } else {
            goToCommunication();
        }
    }

    protected void onResume() {
        super.onResume();
    }

    private void verifyUserNumber() {
        if (P.GCM_REGISTERED && verifyBtnClicked) {

            String number = phoneNumberEditText.getText().toString();
            Toast.makeText(getApplicationContext(), "verifying", Toast.LENGTH_SHORT).show();
            Cognalys.verifyMobileNumber(getApplicationContext(), cognalysAppToken, cognalysAppId, number, new VerificationListner() {

                public void onVerificationStarted() {
                    Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
                }

                public void onVerificationSuccess() {
                    Toast.makeText(getApplicationContext(), "Verified", Toast.LENGTH_SHORT).show();
                    doRegister();
                }

                public void onVerificationFailed(ArrayList<String> errorList) {
                    Toast.makeText(getApplicationContext(), "Failed :: " + errorList.get(0), Toast.LENGTH_SHORT).show();
                    cdt.cancel();
                    RelativeLayout.LayoutParams layoutParams =
                            (RelativeLayout.LayoutParams) noteVerify.getLayoutParams();
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                    noteVerify.setLayoutParams(layoutParams);
                    title.setText("Oops !");
                    noteVerify.setText("Could not Verify Number" + " " + countryCodeField.getText() + "-" + phoneNumberEditText.getText());
                    noteVerify.setTextSize(25);
                    noteVerify.setTextColor(getResources().getColor(R.color.dark_color));
                    ok.setVisibility(View.VISIBLE);
                    sepRight.setVisibility(View.VISIBLE);
                    termsText.setVisibility(View.INVISIBLE);
                    ok.setText("Retry");
                    cancel.setText("Edit Number");
                }
            });
        }

    }

    private void doRegister() {
        httpClientUtil.registerUser(registerListener, P.DEVICE_ID, P.REGISTRATION_ID, P.MOBILE_NUMBER, P.AUTH_CODE, P.RESOLUTION);
    }

    private void verifyUser() {
        if (P.GCM_REGISTERED && verifyBtnClicked) {
            P.MOBILE_NUMBER = P.COUNTRY_CODE.replace("+", "") + phoneNumberEditText.getText();
            P.write(getApplicationContext());
            String device_id = TelephoneUtils.getDeviceId(SignUpActivity.this);
            P.DEVICE_ID = device_id;
            P.write(getApplicationContext());
            if (!GroupDrawft.allowEmulator) {
                httpClientUtil.initUser(initListener, device_id, P.REGISTRATION_ID, P.MOBILE_NUMBER, P.MOBILE_NUMBER, P.COUNTRY_CODE);
            }

        }
    }


    private void initViews() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        countryCode = manager.getSimCountryIso().toUpperCase();

        next = (TextView) findViewById(R.id.next);
        next.setTypeface(GroupDrawft.robotoBold);
        countryCodeField = (TextView) findViewById(R.id.country_code);
        countryCodeField.setTypeface(GroupDrawft.robotoBold);
        //countryField = (TextView) findViewById(R.id.countryName);
        phoneNumberEditText = (EditText) findViewById(R.id.contactNumEditText);
        phoneNumberEditText.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);
        //verifyTerms = (TextView) findViewById(R.id.terms);
        phoneNumberEditText.setTypeface(GroupDrawft.robotoBold);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = (RelativeLayout) inflater.inflate(R.layout.dialog_verification, null);
        //  alertDialog = new AlertDialog.Builder(SignUpActivity.this);
       final LinearLayout details = (LinearLayout) findViewById(R.id.details);

        settingColor();
        /*countryCodeField.setBackgroundColor(color1);
        phoneNumberEditText.setBackgroundColor(color2);
        next.setBackgroundColor(color3);*/
        //verifyTerms.setTypeface(GroupDrawft.fontHelsinki);

        next.setOnClickListener(btnClickListener);
        //verifyTerms.setOnClickListener(btnClickListener);
        //countryField.setOnClickListener(btnClickListener);

//        countryCodeField.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
//                // When user changed the Text
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void afterTextChanged(Editable arg0) {
//                // TODO Auto-generated method stub
//                if (!countryClicked)
//                    checkCountryName(arg0.toString());
//                else
//                    countryClicked = false;
//
//            }
//        });
    }

    private void registerGCM() {
        try {
            if (!GroupDrawft.P.USER_REGISTERED) {
                GCMRegistrar.checkDevice(this);
                GCMRegistrar.checkManifest(this);
                if (GCMRegistrar.isRegistered(this)) {
                    Log.d("info", GCMRegistrar.getRegistrationId(this));
                } else {
                    GCMRegistrar.register(this, GroupDrawft.SENDER_ID);
                    registerReceiver(mHandleMessageReceiver, new IntentFilter(GroupDrawft.DISPLAY_MESSAGE_ACTION));
                }
            }/* else {
                Log.d("info", "already registered as" + regId);
            }*/
        } catch (Exception e) {
            int i = 0;
            // L.fe(getApplicationContext(), Event.EXCEPTION, e);HANDLER
        }
    }


    //    public void openTermsOfUseDialog() {
//        if (null != mDialog) mDialog.dismiss();
//        //
//        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
//        LayoutInflater inflater = getLayoutInflater();
//        RelativeLayout dialogView = (RelativeLayout) inflater.inflate(R.layout.dialog_terms, null);
//        //
//        loadingTerms = (ProgressBar) dialogView.findViewById(R.id.main_wait_1);
//        webview = (WebView) dialogView.findViewById(R.id.body);
//        textTitleTerms = (TextView) dialogView.findViewById(R.id.terms_wrapper);
//        textTitleTerms.setTypeface(GroupDrawft.fontHelsinki);
//        textTitleTerms.setShadowLayer(4.5f, -1, 1, Color.BLACK);
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
//    }
    public void checkCountryName(String CountryID) {
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[0].trim().equalsIgnoreCase(CountryID.trim())) {
                P.COUNTRY_CODE_NAME = g[1];
                P.write(this);
                break;
            }
        }
    }
//
//    public void openCountryDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
//        builder.setTitle("Select your region");
//        LayoutInflater inflater = getLayoutInflater();
//        RelativeLayout dialogView = (RelativeLayout) inflater.inflate(R.layout.country_view, null);
//        ListView list = (ListView) dialogView.findViewById(R.id.myListView);
//        ArrayList<String> stringList = InitListViewData();
//        dataListAdapter adapter = new dataListAdapter(SignUpActivity.this, stringList);
//        list.setAdapter(adapter);
//        builder.setView(dialogView);
//        dialog = builder.create();
//        dialog.show();
//    }

//    public void onClickTerms() {
//        if (GroupDrawft.isNetworkOK(SignUpActivity.this)) {
//            openTermsOfUseDialog();
//        } else {
//            openConnectionDialog();
//        }
//    }

//    public void openTermsOfUseDialog() {
//        if (null != mDialog) mDialog.dismiss();
//        //
//        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
//        LayoutInflater inflater = getLayoutInflater();
//        RelativeLayout dialogView = (RelativeLayout) inflater.inflate(R.layout.dialog_terms, null);
//        //
//        loadingTerms = (ProgressBar) dialogView.findViewById(R.id.main_wait_1);
//        webview = (WebView) dialogView.findViewById(R.id.body);
//        textTitleTerms = (TextView) dialogView.findViewById(R.id.terms_wrapper);
//        textTitleTerms.setTypeface(GroupDrawft.fontHelsinki);
//        textTitleTerms.setShadowLayer(4.5f, -1, 1, Color.BLACK);
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
//    }

    public void openConnectionDialog() {
        if (null != mDialog) mDialog.dismiss();

        GroupDrawft.fireBaseConnected = false;
        settingColor();
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
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
                if (GroupDrawft.isNetworkOK(SignUpActivity.this)) {
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

    private ArrayList<String> InitListViewData() {
        ArrayList<String> stringList = new ArrayList<String>();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            stringList.add(g[2]);
        }
        return stringList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
        }
    }

    public final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            verifyUser();
        }
    };

   /* class dataListAdapter extends BaseAdapter {
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
    }*/

    public void countrySelectedInfo(String countrySel) {
        countryClicked = true;
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[2].trim().equals(countrySel.trim())) {
                countryCode = g[1];
                countryField.setText(g[2].toUpperCase());
                countryCodeField.setText(g[0]);
                GroupDrawft.P.COUNTRY_CODE = g[1];
                GroupDrawft.P.write(this);
                break;
            }
        }
    }


}
