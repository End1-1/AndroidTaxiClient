package com.example.yelloclient;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yelloclient.databinding.FragmentPhoneNumberBinding;

public class FragmentPhoneNumber extends Fragment {

    private FragmentPhoneNumberBinding _b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentPhoneNumberBinding.inflate(getLayoutInflater(), container, false);
        _b.edtPhone.addTextChangedListener(numWatcher);
        return _b.getRoot();
    }

    TextWatcher numWatcher = new TextWatcher() {
        String oldValue = "";
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (oldValue.length() < editable.toString().length()) {
                switch (editable.toString().length()) {
                    case 1:
                        editable.insert(0, "+");
                        break;
                    case 3:
                        editable.insert(2, "(");
                        break;
                    case 6:
                        editable.insert(6, ")");
                        break;
                    case 10:
                        editable.insert(10, "-");
                        break;
                    case 13:
                        editable.insert(13, "-");
                        break;
                }
            } else {
                switch (editable.toString().length()) {
                    case 14:
                        editable.delete(13, 14);
                        break;
                    case 11:
                        editable.delete(10, 11);
                        break;
                    case 7:
                        editable.delete(6, 7);
                        break;
                    case 3:
                        editable.delete(2, 3);
                        break;
                }
            }
            oldValue = editable.toString();
        }
    };
}