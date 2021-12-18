package com.example.yelloclient;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yelloclient.databinding.FragmentPhoneSmsBinding;

public class FragmentPhoneSms extends BaseFragment {

    private FragmentPhoneSmsBinding _b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentPhoneSmsBinding.inflate(getLayoutInflater(), container, false);
        return _b.getRoot();
    }

    @Override
    public void onClick(View view) {

    }
}