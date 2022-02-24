package com.example.yelloclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yelloclient.databinding.FragmentOrderStartedBinding;

public class FragmentOrderStarted extends BaseFragment {

    private FragmentOrderStartedBinding _b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _b = FragmentOrderStartedBinding.inflate(inflater, container, false);
        return _b.getRoot();
    }

    @Override
    public void onClick(View v) {

    }
}