package com.example.yelloclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yelloclient.databinding.FragmentDriverWaitingYouBinding;
import com.google.gson.JsonObject;

public class FragmentDriverWaitingYou extends BaseFragment {

    private FragmentDriverWaitingYouBinding _b;
    private JsonObject mData;

    public FragmentDriverWaitingYou(JsonObject jo) {
        super();
        mData = jo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _b = FragmentDriverWaitingYouBinding.inflate(inflater, container, false);
        _b.txtMessage.setText(mData.get("message").getAsString());
        _b.txtCar.setText(String.format("%s %s %s",
                mData.getAsJsonObject("payload").getAsJsonObject("car").get("color").getAsString(),
                mData.getAsJsonObject("payload").getAsJsonObject("car").get("mark").getAsString(),
                mData.getAsJsonObject("payload").getAsJsonObject("car").get("state_license_plate").getAsString()));
        return _b.getRoot();
    }

    @Override
    public void onClick(View v) {

    }
}