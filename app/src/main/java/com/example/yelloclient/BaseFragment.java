package com.example.yelloclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseFragment extends Fragment implements View.OnClickListener {

    protected BaseActivity mActivity;
    public static final String mBaseFragmentFilter = "BASE_FRAGMENT_FILTER";
    public static final int SET_FRAGMENT_TAXIOTPIONS = 1;
    public static final int SET_MY_LOCATION = 2;
    public static final int SET_ADDRESS_FROM_STRING = 3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mFragmentReceiver, new IntentFilter(mBaseFragmentFilter));
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mFragmentReceiver);
        super.onDestroy();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    protected void messageHandler(int msg, Intent i) {

    }

    protected void replaceFragment(BaseFragment fr) {
        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.llMainContainer, fr, fr.tag());
        fragmentTransaction.commit();
    }

    protected String tag() {
        System.out.println(getClass().getName());
        return getClass().getName();
    }

    protected BroadcastReceiver mFragmentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            messageHandler(intent.getIntExtra("cmd", 0), intent);
        }
    };
}
