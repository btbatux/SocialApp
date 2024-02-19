package com.hicome.loveday;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender {

    String userFcmToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;


    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final
    String
            fcmServerKey =
            "AAAAaooSVv4:APA91bHT0Z6pU44dh99NBTnSmkB-zZEd1V4tDvnxu9stTSMjR4N2sUD8vhAp8nQiM-i-2YS6beI5WJqjeW0lf9_MlPa7cQcz0eC9KaB5OC-uO9mFTGZjPhf7ZzXdKzYiH_GR3YzDUIzW";

    public FcmNotificationsSender(String userFcmToken, String title, String body, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
        this.mActivity = mActivity;


    }

    public void SendNotifications() {
        // MyApplication sınıfından RequestQueue nesnesini al
        requestQueue = MyApplication.getInstance().getRequestQueue();

        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);
            notiObject.put("icon", "icon"); // drawable içindeki icon ismini girin

            mainObj.put("notification", notiObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Cevap alındığında çalışacak kod
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Hata alındığında çalışacak kod
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;
                }
            };
            // İsteği RequestQueue'ye ekle
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
