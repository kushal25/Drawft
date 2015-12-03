package com.drawft.util;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

/**
 * Will help in obtaining telephone data like phone number, sim number, IMEI etc
 *
 * @author suneel
 */
public class TelephoneUtils {
    /**
     * provides telephone number of user.
     *
     * @param mContext
     * @return
     */
    public static String getPhoneNumber(Context mContext) {
        return ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
    }

    /**
     * Provides sim number of user
     *
     * @param mContext
     * @return
     */
    public static String getSimNumber(Context mContext) {
        return ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getSimSerialNumber();
    }

    /**
     * provides Unique id of the device
     *
     * @param mContext
     * @return
     */
    public static String getDeviceId(Context mContext) {
        return Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
    }

    public static String getIMEI(Context mContext) {
        // GET IMEI NUMBER
        return ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }
}
