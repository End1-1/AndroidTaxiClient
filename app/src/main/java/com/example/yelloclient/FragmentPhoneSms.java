package com.example.yelloclient;

import static com.example.yelloclient.BaseActivity.FC_NAVIGATE_INTRO;
import static com.example.yelloclient.BaseActivity.FC_NAVIGATE_MAINPAGE;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yelloclient.databinding.FragmentPhoneSmsBinding;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

public class FragmentPhoneSms extends BaseFragment {

    public static final String tag = "FragmentPhoneSms";

    private FragmentPhoneSmsBinding _b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentPhoneSmsBinding.inflate(getLayoutInflater(), container, false);
        _b.n1.addTextChangedListener(w);
        _b.n2.addTextChangedListener(w);
        _b.n3.addTextChangedListener(w);
        _b.n4.addTextChangedListener(w);
        _b.n5.addTextChangedListener(w);
        _b.n6.addTextChangedListener(w);
        _b.btnNext.setOnClickListener(this);
        _b.txtResendSMS.setOnClickListener(this);
        _b.txtMessage.setText(Preference.getString("sms_message"));
        _b.n1.setOnKeyListener(keyListener);
        _b.n2.setOnKeyListener(keyListener);
        _b.n3.setOnKeyListener(keyListener);
        _b.n4.setOnKeyListener(keyListener);
        _b.n5.setOnKeyListener(keyListener);
        _b.n6.setOnKeyListener(keyListener);
        return _b.getRoot();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtResendSMS:
                mActivity.fragmentCallback(BaseActivity.FC_NAVIGATE_LOGIN);
                break;
            case R.id.btnNext:
                setControlsEnabled(false);
                WebRequest.create("/app/mobile/auth", WebRequest.HttpMethod.POST, smsResponse)
                        .setParameter("phone", Preference.getString("clear_phone"))
                        .setParameter("accept_code", getAcceptCode())
                        .request();
                break;
        }
    }

    private void setControlsEnabled(boolean v) {
        _b.pbNextMainView.setVisibility(v ? View.GONE : View.VISIBLE);
        _b.btnNext.setEnabled(v);
        _b.n1.setEnabled(v);
        _b.n2.setEnabled(v);
        _b.n3.setEnabled(v);
        _b.n4.setEnabled(v);
        _b.n5.setEnabled(v);
        _b.n6.setEnabled(v);
    }

    private String getAcceptCode() {
        return _b.n1.getText().toString()
                + _b.n2.getText().toString()
                + _b.n3.getText().toString()
                + _b.n4.getText().toString()
                + _b.n5.getText().toString()
                + _b.n6.getText().toString();
    }

    private TextWatcher w = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.toString().length() > 0) {
                if (editable == _b.n1.getText()) {
                    _b.n2.requestFocus();
                } else if (editable == _b.n2.getText()) {
                    _b.n3.requestFocus();
                } else if (editable == _b.n3.getText()) {
                    _b.n4.requestFocus();
                } else if (editable == _b.n4.getText()) {
                    _b.n5.requestFocus();
                } else if (editable == _b.n5.getText()) {
                    _b.n6.requestFocus();
                } else if (editable == _b.n6.getText()) {
                    onClick(_b.btnNext.findViewById(R.id.btnNext));
                }
            }
        }
    };

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                switch (view.getId()) {
                    case R.id.n1:
                        break;
                    case R.id.n2:
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                            if (_b.n2.getText().toString().isEmpty()) {
                                _b.n1.requestFocus();
                            }
                        }
                        break;
                    case R.id.n3:
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                            if (_b.n3.getText().toString().isEmpty()) {
                                _b.n2.requestFocus();
                            }
                        }
                        break;
                    case R.id.n4:
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                            if (_b.n4.getText().toString().isEmpty()) {
                                _b.n3.requestFocus();
                            }
                        }
                        break;
                    case R.id.n5:
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                            if (_b.n5.getText().toString().isEmpty()) {
                                _b.n4.requestFocus();
                            }
                        }
                        break;
                    case R.id.n6:
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                            if (_b.n6.getText().toString().isEmpty()) {
                                _b.n5.requestFocus();
                            }
                        }
                        break;
                }
            }
            return false;
        }
    };

    public WebRequest.HttpResponse smsResponse = new WebRequest.HttpResponse() {

        @Override
        public void httpRespone(int httpReponseCode, String data) {
            setControlsEnabled(true);
            if (httpReponseCode == -1) {
                _b.txtMessage.setText(R.string.InternetFail);
            } else if (httpReponseCode < 300) {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject().get("data").getAsJsonObject();
                Preference.setInt("client_id", jo.get("client_id").getAsInt());
                Config.setBearerKey(jo.get("token").getAsString());
                mActivity.fragmentCallback(FC_NAVIGATE_INTRO);
            } else  {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                _b.txtMessage.setText(jo.get("message").getAsString());
            }
        }
    };
}