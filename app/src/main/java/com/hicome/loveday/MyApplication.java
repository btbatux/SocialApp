package com.hicome.loveday;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MyApplication extends Application {
    private static MyApplication instance;
    private RequestQueue requestQueue;

    public static synchronized MyApplication getInstance() {
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
