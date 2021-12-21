package com.example.yelloclient;

import android.app.Application;
import android.content.Context;

public class TaxiApp extends Application {

    private static TaxiApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static Context getContext(){
        return mInstance.getApplicationContext();
    }
}
