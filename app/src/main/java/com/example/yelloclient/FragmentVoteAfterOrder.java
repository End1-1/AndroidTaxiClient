package com.example.yelloclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.example.yelloclient.databinding.FragmentVoteAfterOrderBinding;

import java.util.ArrayList;
import java.util.List;

public class FragmentVoteAfterOrder extends BaseFragment {

    private FragmentVoteAfterOrderBinding _b;
    private List<Integer> mRbList = new ArrayList();
    private int mRbResult = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _b = FragmentVoteAfterOrderBinding.inflate(inflater, container, false);
        _b.rb1.setOnClickListener(this);
        _b.rb2.setOnClickListener(this);
        _b.rb3.setOnClickListener(this);
        _b.rb4.setOnClickListener(this);
        _b.rb5.setOnClickListener(this);
        mRbList.add(_b.rb1.getId());
        mRbList.add(_b.rb2.getId());
        mRbList.add(_b.rb3.getId());
        mRbList.add(_b.rb4.getId());
        mRbList.add(_b.rb5.getId());
        return _b.getRoot();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb1:
            case R.id.rb2:
            case R.id.rb3:
            case R.id.rb4:
            case R.id.rb5:
                paintStart(v.getId());
                break;
        }
    }

    private void paintStart(int id) {
        for (int rb: mRbList) {
            ((RadioButton) getView().findViewById(rb)).setBackgroundResource(R.drawable.redstari);
        }
        mRbResult = 0;
        for (int rb: mRbList) {
            mRbResult++;
            ((RadioButton) getView().findViewById(rb)).setBackgroundResource(R.drawable.redstar);
            if (rb == id) {
                break;
            }
        }
    }
}