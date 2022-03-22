package com.example.yelloclient;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    public static final int FC_NAVIGATE_LOGIN = 1;
    public static final int FC_NAVIGATE_SMS_CODE = 2;
    public static final int FC_NAVIGATE_MAINPAGE = 3;
    public static final int FC_NAVIGATE_INTRO = 4;
    public static final int FC_NAVIGET_TAXI_OPTIONS = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
    }

    protected void fragmentCallback(int code) {

    }
}
