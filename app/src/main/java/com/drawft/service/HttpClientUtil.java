package com.drawft.service;


import android.content.Context;

import com.drawft.GroupDrawft;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;

public class HttpClientUtil {
    private final String baseUrl = "https://drawing-slate.herokuapp.com/";
    //final String baseUrl = "http://192.168.1.150:3000/";
    final String initUrl = baseUrl + "api/user_init";
    final String missedCallUrl = baseUrl + "api/missed-call-number-verified";
    String registerUrl = "";
    final String getAuthCodeUrl = baseUrl + "api/check_pending_number";
    final String getFBTokenUrl = baseUrl + "api/get_fb_token";
    final String updateContactsToServerUrl = baseUrl + "api/contacts_info";
    final String refreshContactsToServerUrl = baseUrl + "api/refresh_contacts_info";
    final String notifyUpdatesUrl = baseUrl + "api/notify_user";
    final String updateAliveUrl = baseUrl + "api/is_alive";
    final String getFirebaseTokenUrl = baseUrl + "api/tercom for a";
    final String getUsersNotificationsUrl = baseUrl + "api/get_user_notifications";
    final String addNewUserInteractionUrl = baseUrl + "api/add_user_interaction";
    final String fetchTermsOfUseUrl = baseUrl + "api/fetch_terms";
    final String createGroupUrl = baseUrl + "api/create_group";
    final String notifyGroupUrl = baseUrl + "api/notify_group";
    final String editGroupUrl = baseUrl + "api/edit_group";
    final String deleteGroupUrl = baseUrl + "api/delete_group";
    final String getGroupsUrl = baseUrl + "api/get_groups";
    private final String baseUrlLogs = "http://logs-01.loggly.com/inputs/c683f4c4-c087-4204-b514-caa55dd54f55/tag/http/";
    AsyncHttpClient httpClient = new AsyncHttpClient();

    public HttpClientUtil() {
        if (GroupDrawft.allowEmulator) {
            registerUrl = baseUrl + "api/old_user_registration";
        } else {
            registerUrl = baseUrl + "api/user_registration";
        }
    }
    public void initUser(JsonHttpResponseHandler listener, String dId, String pid, String mno, String imno, String cCode) {
        RequestParams params = new RequestParams();
        params.put("device_id", dId);
        params.put("push_notification_id", pid);
        params.put("mobile_no", mno);
        params.put("input_mobile_no", imno);
        params.put("country_name", cCode);
        httpClient.post(initUrl, params, listener);
    }

    public void verifyMissedCall(JsonHttpResponseHandler listener, String zipdialnumber, String mno) {
        RequestParams params = new RequestParams();
        params.put("timeofcall", "656755675");
        params.put("zipdialnumber", "917828176791");
        params.put("usernumber", mno);
        params.put("zdtok", "XYZQHqXGVOkeaxdSI");
        httpClient.post(missedCallUrl, params, listener);
    }

    public void registerUser(JsonHttpResponseHandler listener, String device_id, String push_notification_id, String mobile_no, String device_auth_code, String resolution) {
        RequestParams params = new RequestParams();
        params.put("device_id", device_id);
        params.put("device_auth_code", device_auth_code);
        params.put("push_notification_id", push_notification_id);
        params.put("mobile_no", mobile_no);
        params.put("resolution", resolution);
        params.put("sim_serial_no", "");
        params.put("imei", "");
        params.put("network_id", "");
        params.put("network_name", "");
        params.put("device_name", "");
        params.put("os_version", "");

        httpClient.post(registerUrl, params, listener);
    }

    public void getAuthCode(JsonHttpResponseHandler listener, String mobile_no, String device_id) {
        try {
            RequestParams params = new RequestParams();
            params.put("device_id", device_id);
            if (mobile_no.contains("+")) mobile_no = mobile_no.replace("+", "");
            params.put("mobile_no", mobile_no);

            // Execute HTTP Post Request
            httpClient.post(getAuthCodeUrl, params, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchTermsOfUse(JsonHttpResponseHandler listener) {
        try {
            RequestParams params = new RequestParams();
            httpClient.post(fetchTermsOfUseUrl, params, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addNewGroup(JsonHttpResponseHandler listener, String mobile_no, String device_auth_code, String g_name, String members) {
        try {
            RequestParams params = new RequestParams();
            params.put("device_auth_code", device_auth_code);
            params.put("groupName", g_name);
            params.put("mobile_no", mobile_no);
            params.put("members", members);
            httpClient.post(createGroupUrl, params, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyGroup(JsonHttpResponseHandler listener, String mobile_no, String device_auth_code, String g_id, String drawft_id, boolean is_group) {
        try {
            RequestParams params = new RequestParams();
            params.put("device_auth_code", device_auth_code);
            params.put("group_id", g_id);
            params.put("drawft_id", drawft_id);
            params.put("mobile_no", mobile_no);
            params.put("isGroup", is_group);
            httpClient.post(notifyGroupUrl, params, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void editGroup(JsonHttpResponseHandler listener, String mobile_no, String device_auth_code, String g_id, String type, String updateContent) {
        try {
            RequestParams params = new RequestParams();
            params.put("device_auth_code", device_auth_code);
            params.put("mobile_no", mobile_no);
            params.put("group_id", g_id);
            params.put("type", type);
            params.put("content", updateContent);
            httpClient.post(editGroupUrl, params, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteGroup(JsonHttpResponseHandler listener, String mobile_no, String device_auth_code, String g_id) {
        try {
            RequestParams params = new RequestParams();
            params.put("device_auth_code", device_auth_code);
            params.put("mobile_no", mobile_no);
            params.put("group_id", g_id);
            httpClient.post(deleteGroupUrl, params, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUserGroups(JsonHttpResponseHandler listener, String mobile_no, String device_auth_code, JSONArray contactsList) {
        try {
            RequestParams params = new RequestParams();
            params.put("device_auth_code", device_auth_code);
            params.put("mobile_no", mobile_no);
            params.put("contacts_list", contactsList.toString());
            httpClient.post(getGroupsUrl, params, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getFBAuthCode(JsonHttpResponseHandler listener, String mobile_no, String device_auth_code) {
        try {
            RequestParams params = new RequestParams();
            params.put("device_auth_code", device_auth_code);
            params.put("mobile_no", mobile_no);
            httpClient.post(getFBTokenUrl, params, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public interface ServiceStatusListener {
        public void onSuccess(Object object, Context context);

        public void onFailure(Exception exception, Context context);
    }
}
