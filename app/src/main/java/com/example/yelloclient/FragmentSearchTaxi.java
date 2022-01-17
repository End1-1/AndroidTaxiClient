package com.example.yelloclient;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yelloclient.databinding.FragmentSearchTaxiBinding;

public class FragmentSearchTaxi extends BaseFragment {

    private FragmentSearchTaxiBinding _b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentSearchTaxiBinding.inflate(getLayoutInflater(), container, false);
        _b.btnCancelSearch.setOnClickListener(this);
        return _b.getRoot();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCancelSearch: {
                _b.btnCancelSearch.setEnabled(false);
                WebRequest.create("/app/mobile/cancel_order", WebRequest.HttpMethod.POST, mCancelSearch)
                        .request();
                break;
            }
        }
    }

    @Override
    protected String tag() {
        return "FragmentSearchTaxi";
    }

    WebRequest.HttpResponse mCancelSearch = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            _b.btnCancelSearch.setEnabled(true);
            if (httpReponseCode == -1) {
                Dlg.alertDialog(getContext(), R.string.Error, R.string.InternetFail);
            } else if (httpReponseCode < 300) {
                getParentFragmentManager().getFragments().get(0);
                FragmentMainPage fmp = (FragmentMainPage) getParentFragmentManager().getFragments().get(0);
                if (fmp != null) {
                    fmp.reset();
                }
            } else {

            }
        }
    };
}