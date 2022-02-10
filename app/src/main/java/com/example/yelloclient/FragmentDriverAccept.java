package com.example.yelloclient;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yelloclient.databinding.FragmentDriverAcceptBinding;
import com.google.gson.JsonObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentDriverAccept#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentDriverAccept extends BaseFragment {

    private FragmentDriverAcceptBinding _b;
    private JsonObject mData;

    public FragmentDriverAccept(JsonObject o) {
        super();
        mData = o;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentDriverAcceptBinding.inflate(inflater, container, false);
        _b.btnCallDriver.setOnClickListener(this);
        _b.btnCancelOrder.setOnClickListener(this);
        _b.btnChat.setOnClickListener(this);
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