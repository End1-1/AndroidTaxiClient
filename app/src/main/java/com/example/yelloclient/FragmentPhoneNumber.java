package com.example.yelloclient;

import static com.example.yelloclient.BaseActivity.FC_NAVIGATE_SMS_CODE;

import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yelloclient.databinding.FragmentPhoneNumberBinding;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FragmentPhoneNumber extends BaseFragment {

    private FragmentPhoneNumberBinding _b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentPhoneNumberBinding.inflate(getLayoutInflater(), container, false);
        _b.edtPhone.addTextChangedListener(numWatcher);
        _b.btnNext.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNext:
                setControlsEnabled(false);
                Preference.setString("phone", _b.edtPhone.getText().toString());
                Preference.setString("clear_phone", _b.edtPhone.getText().toString().replace("+", "").replace("(", "").replace(")", "").replace("-", ""));
                WebRequest.create("/app/mobile/register", WebRequest.HttpMethod.POST, requestRegister)
                        .setParameter("phone", Preference.getString("clear_phone"))
                        .request();
                break;
        }
    }

    private void setControlsEnabled(boolean v) {
        _b.pbNextSMS.setVisibility(v ? View.GONE : View.VISIBLE);
        _b.btnNext.setEnabled(v);
        _b.edtPhone.setEnabled(v);
    }

    WebRequest.HttpResponse requestRegister = (httpReponseCode, data) -> {
        setControlsEnabled(true);
        if (httpReponseCode == -1) {
            _b.txtMessage.setText(R.string.InternetFail);
        } else if (httpReponseCode < 300) {
            JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
            Preference.setString("sms_message", jo.get("message").getAsString());
            mActivity.fragmentCallback(FC_NAVIGATE_SMS_CODE);
        } else {
            JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
            _b.txtMessage.setText(jo.get("message").getAsString());
        }
    };
}