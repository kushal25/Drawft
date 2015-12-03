package com.drawft;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Random;

public class GroupDrawft {

    public static final String TAG = "GroupDrawft";
    public static Typeface fontHelsinki = null, fontFeather = null;
    public static Typeface robotoBold = null, robotoThin = null, robotoLight = null, robotoRegular = null;
    public static boolean fireBaseConnected = false;

    public static final ArrayList<String> appUsing = new ArrayList<String>();

    static final String SENDER_ID = "564500282855";
    public static final String DISPLAY_MESSAGE_ACTION = "com.drawft.DISPLAY_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";
    public static boolean allowEmulator = false;
    public static int randomNumber = 0;


    public static void appInit(Context cx) {
        /*appUsing.add("919959833920");//ashok
        appUsing.add("911234567891");//Emulator Nexus
        appUsing.add("19876543210");//Emulator Nexus
        appUsing.add("919948287511");//anil
        appUsing.add("919885004800");//venkat
        appUsing.add("917416719938");//vj
        appUsing.add("911234567891");//emulator nexus
        appUsing.add("919876543219");//emulator
        appUsing.add("919411561298");//Venkat Tab
        appUsing.add("918106077157");//Kushal
        appUsing.add("919848080789");//Ravi
        appUsing.add("919000357111");// Venkat 9toppiks*/
        P.read(cx);

    }

    public static void displayMessageGCM(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    public static void runOnBackground(final Context cx, final Runnable r) {
        new Thread() {
            @Override
            public void run() {
                try {
                    r.run();
                } catch (Exception e) {
                    //L.fe(cx, Event.EXCEPTION, e);
                    Log.d("Error", e.toString());
                }
            }
        }.start();
    }

    public static String getSortedNumber(String mno, String mno2) {
        String result = mno + '_' + mno2;
        try {
            if (Long.parseLong(mno) > Long.parseLong(mno2)) {
                result = mno2 + '_' + mno;
            }
        } catch (Exception e) {

        }
        return result;
    }


    public static ArrayList<String> getColorList() {
        ArrayList<String> myColorList = new ArrayList<String>();
        ArrayList<String> arr = new ArrayList<>();

        try {
            //String arrayColors = "[\"2FD9FB\",\"EF663C\",\"B5F743\",\"F99DE8\",\"6F9867\",\"FBD15B\",\"FE5586\",\"7FFFA0\",\"978593\",\"CAE2E4\",\"8789D3\",\"838725\",\"0BC299\",\"E89A5A\",\"E7FB82\",\"4A91A3\",\"59B94F\",\"72FBE9\",\"E9BFC6\",\"9DBE15\",\"3BB6F1\",\"8D8457\",\"C8B8F0\",\"C2FA9E\",\"22A467\",\"88F6B6\",\"BE96EF\",\"ECF25A\",\"948775\",\"9D8511\",\"7BEDFA\",\"E55448\",\"D96F35\",\"C0D918\",\"21B067\"]";
            String arrayColors = "[\"1abc9c\", \"2ecc71\", \"3498db\", \"9b59b6\", \"34495e\", \"f39c12\", \"e67e22\", \"e74c3c\", \"95a5a6\", \"2c3e50\", \"E91E63\", \"00BCD4\", \"009587\", \"CCDB38\", \"FEEA3A\", \"785447\",\"5E35B1\",\"e53935\", \"D81B60\", \"8E24AA\",  \"3949AB\", \"1E88E5\", \"039BE5\", \"00ACC1\", \"00897B\", \"43A047\", \"7CB342\", \"C0CA33\", \"FDD835\", \"FFB300\", \"FB8C00\", \"F4511E\", \"6D4C41\", \"757575\", \"546E7A\"]";
            //String arrayColors = "[\"3498db\", \"3949AB\", \"95a5a6\", \"FFB300\", \"2c3e50\", \"785447\", \"00BCD4\", \"7CB342\", \"E91E63\", \"CCDB38\", \"e53935\", \"00897B\", \"757575\", \"1E88E5\", \"D81B60\", \"C0CA33\", \"d35400\", \"F4511E\", \"5E35B1\", \"6D4C41\", \"00ACC1\", \"2ecc71\", \"e74c3c\", \"009587\", \"34495e\", \"9b59b6\", \"1abc9c\", \"FB8C00\", \"FEEA3A\", \"43A047\", \"f39c12\", \"8E24AA\", \"546E7A\", \"e67e22\", \"039BE5\", \"FDD835\"]";


           /* String input = "abcdef";
            int inputLength = input.length();
            boolean[ ] used = new boolean[ inputLength ];
            StringBuffer outputString = new StringBuffer();
            char[ ] in = input.toCharArray();
            doPermute(in, outputString, used, inputLength, 0, arr);
            Random rand = new Random();
            int k;
            for(int j=0;j<arr.size();j++) {
                k = rand.nextInt(arr.size());
                myColorList.add(j, arr.get(k));
            }*/
            JSONArray arrayClrs = new JSONArray(arrayColors);
            if (randomNumber == 0) {
                randomNumber = new Random().nextInt(arrayClrs.length());
            }

            for (int i = randomNumber; i < arrayClrs.length(); i++) {
                myColorList.add("#" + arrayClrs.get(i));
            }
            for (int i = randomNumber - 1; i >= 1; i--) {
                myColorList.add("#" + arrayClrs.get(i));
            }
        } catch (Exception e) {
        }
        return myColorList;
    }

    public static void doPermute ( char[ ] in, StringBuffer outputString,
                            boolean[ ] used, int inputLength, int level, ArrayList<String> arr1)
    {
        int ct = 0;
        if( level == inputLength) {
            arr1.add(ct, "#88" + outputString.toString());
            ct++;
        }

        for( int i = 0; i < inputLength; ++i )
        {
            if( used[i] ) continue;
            outputString.append( in[i] );
            used[i] = true;
            doPermute( in,   outputString, used, inputLength, level + 1 , arr1);
            used[i] = false;
            outputString.setLength(outputString.length() - 1 );
        }

    }

    public static boolean isNetworkOK(Context cx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) cx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null == cm) {
                return false;
            }
            NetworkInfo ni;
            ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (null != ni && ni.isConnectedOrConnecting()) return true;
            ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (null != ni && ni.isConnectedOrConnecting()) return true;
            ni = cm.getActiveNetworkInfo();
            if (null != ni && ni.isConnectedOrConnecting()) return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static final class Event {
        public static final String VERBOSE = "Verbose";
        public static final String WARNING = "Warning";
        public static final String EXCEPTION = "Exception";
    }

    public static final class P {
       // public static ArrayList<String> appUsing = GroupDrawft.appUsing;
       public static String APP_VERSION = "1.6.4";
        public static boolean NEW_VERSION_STATUS = false;
        public static boolean GCM_REGISTERED = false;
        public static String AUTH_CODE = "";
        public static String RESOLUTION = "";
        public static String MOBILE_NUMBER = "";
        public static String COUNTRY_CODE = "";
        public static String COUNTRY_CODE_NAME = "";
        public static String DEVICE_ID = "";
        public static boolean CONTACTS_SAVED = false;
        public static boolean CONTACTS_SENT = false;
        public static String REGISTRATION_ID = null;
        public static boolean USER_REGISTERED = false;
        public static String FB_URL = "https://groupdrawft.firebaseio.com/";
        public static String FB_TOKEN = null;
        public static String DESTINATION_USER = null;
        public static Long OFFSET_TIME = 0L;
        public static Long CONTACTS_SAVED_TIME = System.currentTimeMillis();
        public static int TOTAL_CONTACTS = 0;
        public static Long REGISTERED_TIME = 0L;
        public static String SAVED_COLORS = "";
        public static String SHARING_MSG = "DRAWFT lets us communicate with drawings. Download for Android: http://play.google.com/store/apps/details?id=com.drawft";

        //
        public static void read(Context cx) {
            try {
                if (null == fontHelsinki)
                    fontHelsinki = Typeface.createFromAsset(cx.getAssets(), "helsinki.ttf");

                if (null == robotoBold)
                    robotoBold = Typeface.createFromAsset(cx.getAssets(), "Roboto-Bold.ttf");

                if (null == robotoLight)
                    robotoLight = Typeface.createFromAsset(cx.getAssets(), "Roboto-Light.ttf");

                if (null == robotoRegular)
                    robotoRegular = Typeface.createFromAsset(cx.getAssets(), "Roboto-Regular.ttf");

                if (null == robotoThin)
                    robotoThin = Typeface.createFromAsset(cx.getAssets(), "Roboto-Thin.ttf");

                if (null == fontFeather)
                    fontFeather = Typeface.createFromAsset(cx.getAssets(), "feather-webfont.ttf");

                SharedPreferences pref = cx.getSharedPreferences(TAG, Activity.MODE_PRIVATE);
                //
                NEW_VERSION_STATUS = pref.getBoolean("NEW_VERSION_STATUS", NEW_VERSION_STATUS);
                GCM_REGISTERED = pref.getBoolean("GCM_REGISTERED", GCM_REGISTERED);
                AUTH_CODE = pref.getString("AUTH_CODE", AUTH_CODE);
                REGISTRATION_ID = pref.getString("REGISTRATION_ID", REGISTRATION_ID);
                USER_REGISTERED = pref.getBoolean("USER_REGISTERED", USER_REGISTERED);
                FB_URL = pref.getString("FB_URL", FB_URL);
                FB_TOKEN = pref.getString("FB_TOKEN", FB_TOKEN);
                RESOLUTION = pref.getString("RESOLUTION", RESOLUTION);
                MOBILE_NUMBER = pref.getString("MOBILE_NUMBER", MOBILE_NUMBER);
                COUNTRY_CODE = pref.getString("COUNTRY_CODE", COUNTRY_CODE);
                COUNTRY_CODE_NAME = pref.getString("COUNTRY_CODE_NAME", COUNTRY_CODE_NAME);
                DEVICE_ID = pref.getString("DEVICE_ID", DEVICE_ID);
                CONTACTS_SAVED = pref.getBoolean("CONTACTS_SAVED", CONTACTS_SAVED);
                CONTACTS_SENT = pref.getBoolean("CONTACTS_SENT", CONTACTS_SENT);
                DESTINATION_USER = pref.getString("DESTINATION_USER", DESTINATION_USER);
                OFFSET_TIME = pref.getLong("OFFSET_TIME", OFFSET_TIME);
                CONTACTS_SAVED_TIME = pref.getLong("CONTACTS_SAVED_TIME", CONTACTS_SAVED_TIME);
                REGISTERED_TIME = pref.getLong("REGISTERED_TIME", REGISTERED_TIME);
                TOTAL_CONTACTS = pref.getInt("TOTAL_CONTACTS", TOTAL_CONTACTS);
                SHARING_MSG = pref.getString("SHARING_MSG", SHARING_MSG);
                SAVED_COLORS = pref.getString("SAVED_COLORS", SAVED_COLORS);
                APP_VERSION = pref.getString("APP_VERSION", APP_VERSION);

                //
            } catch (Exception e) {
                //L.fwtf(cx, e);
                int i = 0;
            }
        }

        public static void write(Context cx) {
            try {
                SharedPreferences.Editor prefEditor = cx.getSharedPreferences(TAG, Activity.MODE_PRIVATE).edit();
                //
                prefEditor.putBoolean("NEW_VERSION_STATUS", NEW_VERSION_STATUS);
                prefEditor.putBoolean("GCM_REGISTERED", GCM_REGISTERED);
                prefEditor.putString("AUTH_CODE", AUTH_CODE);
                prefEditor.putString("REGISTRATION_ID", REGISTRATION_ID);
                prefEditor.putBoolean("USER_REGISTERED", USER_REGISTERED);
                prefEditor.putString("FB_URL", FB_URL);
                prefEditor.putString("FB_TOKEN", FB_TOKEN);
                prefEditor.putString("RESOLUTION", RESOLUTION);
                prefEditor.putString("MOBILE_NUMBER", MOBILE_NUMBER);
                prefEditor.putString("COUNTRY_CODE", COUNTRY_CODE);
                prefEditor.putString("COUNTRY_CODE_NAME", COUNTRY_CODE_NAME);
                prefEditor.putString("DEVICE_ID", DEVICE_ID);
                prefEditor.putBoolean("CONTACTS_SAVED", CONTACTS_SAVED);
                prefEditor.putBoolean("CONTACTS_SENT", CONTACTS_SENT);
                prefEditor.putString("DESTINATION_USER", DESTINATION_USER);
                prefEditor.putLong("OFFSET_TIME", OFFSET_TIME);
                prefEditor.putLong("CONTACTS_SAVED_TIME", CONTACTS_SAVED_TIME);
                prefEditor.putLong("REGISTERED_TIME", REGISTERED_TIME);
                prefEditor.putInt("TOTAL_CONTACTS", TOTAL_CONTACTS);
                prefEditor.putString("SHARING_MSG", SHARING_MSG);
                prefEditor.putString("SAVED_COLORS", SAVED_COLORS);
                prefEditor.putString("APP_VERSION", APP_VERSION);
                //
                //
                prefEditor.commit();
            } catch (Exception e) {
                // L.fwtf(cx, e);
            }
        }

    }

}
