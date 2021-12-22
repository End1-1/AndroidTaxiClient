package com.example.yelloclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.yelloclient.databinding.ActivitySuggestAddressBinding;

public class ActivitySuggestAddress extends AppCompatActivity {

    private ActivitySuggestAddressBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivitySuggestAddressBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
    }
}