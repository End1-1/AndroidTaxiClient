package com.example.yelloclient;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public abstract class BaseFragment extends Fragment implements View.OnClickListener {

    protected BaseActivity mActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
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
}
