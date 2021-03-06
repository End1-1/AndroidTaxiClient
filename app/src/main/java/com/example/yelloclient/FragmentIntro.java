package com.example.yelloclient;

import static com.example.yelloclient.BaseActivity.FC_NAVIGATE_LOGIN;
import static com.example.yelloclient.BaseActivity.FC_NAVIGATE_MAINPAGE;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yelloclient.classes.Companies;
import com.example.yelloclient.classes.PaymentTypes;
import com.example.yelloclient.databinding.FragmentIntroBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yandex.mapkit.MapKitFactory;

import java.util.Locale;

public class FragmentIntro extends BaseFragment {

    private FragmentIntroBinding _b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentIntroBinding.inflate(getLayoutInflater(), container, false);
        WebRequest.create(String.format("/app/mobile/get_api_keys/%d/%s", 1, Config.mapkitKey()), WebRequest.HttpMethod.GET, getMapkitKey).request();
        return _b.getRoot();
    }

    @Override
    public void onClick(View view) {

    }

    LocationService.LocationChangeListener locationListener = new LocationService.LocationChangeListener() {
        @Override
        public void location(Location l) {
            Preference.setFloat("last_lat", (float) l.getLatitude());
            Preference.setFloat("last_lon", (float) l.getLongitude());
            WebRequest.create("/app/mobile/init_open", WebRequest.HttpMethod.POST, getInitOpen)
                    .setParameter("lat", String.format(Locale.ROOT, "%f", l.getLatitude()))
                    .setParameter("lut", String.format(Locale.ROOT, "%f", l.getLongitude()))
                    .request();
        }
    };

    WebRequest.HttpResponse getMapkitKey = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode  < 0) {
                _b.txtStatus.setText(R.string.InternetFail);
            } else if (httpReponseCode < 300) {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                Config.setYandexGeocodeKey(jo.get("_payload").getAsJsonObject().get("key").getAsString());
                MapKitFactory.setApiKey(Config.mapkitKey());
                LocationService.getSingleLocation(locationListener);
            } else if (httpReponseCode == 401){
                mActivity.fragmentCallback(FC_NAVIGATE_LOGIN);
            } else {
                _b.txtStatus.setText(data);
            }
        }
    };

    WebRequest.HttpResponse getInitOpen = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode  < 0) {
                _b.txtStatus.setText(R.string.InternetFail);
            } else if (httpReponseCode < 300) {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                GsonBuilder gb = new GsonBuilder();
                Gson g = gb.create();
                ((MainActivity) mActivity).setCarClasses(jo.get("data").getAsJsonObject());
                ((MainActivity) mActivity).mPaymentTypes = g.fromJson(jo.get("data").getAsJsonObject(), PaymentTypes.class);
                if (((MainActivity) mActivity).mPaymentTypes.payment_types.size() > 0) {
                    ((MainActivity) mActivity).mPaymentTypes.payment_types.get(0).selected = true;
                }
                ((MainActivity) mActivity).mCompanies = g.fromJson(jo.get("data").getAsJsonObject(), Companies.class);
                mActivity.fragmentCallback(FC_NAVIGATE_MAINPAGE);
            } else if (httpReponseCode == 401) {
                mActivity.fragmentCallback(FC_NAVIGATE_LOGIN);
            } else {
                _b.txtStatus.setText(data);
            }
        }
    };
}