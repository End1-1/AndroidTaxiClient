package com.example.yelloclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yelloclient.classes.CarClass;
import com.example.yelloclient.classes.Driver;
import com.example.yelloclient.classes.GeocoderAnswer;
import com.example.yelloclient.classes.Messanger;
import com.example.yelloclient.classes.SocketThread;
import com.example.yelloclient.databinding.FragmentMainPageBinding;
import com.example.yelloclient.databinding.ItemCarsBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.runtime.image.ImageProvider;

import java.io.IOException;
import java.util.Calendar;

public class FragmentMainPage extends BaseFragment {

    private FragmentMainPageBinding _b;
    private boolean mCoordGeocoding = false;
    private boolean mMainFrameDown = false;
    private float cx = 0;
    private float cy = 0;

    private void changeState(int state, JsonObject jo) {
        switch (state) {
            case Config.StateNone:
                _b.llMainContainer.removeAllViews();
                replaceFragment(new FragmentBeforeOrder());
                mCoordGeocoding = true;
                WebRequest.create("", WebRequest.HttpMethod.GET, mCoordToAddress)
                        .setUrl(String.format("https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&kind=house&geocode=%f,%f&results=1&sco=latlong",
                                Config.yandexGeocodeKey(), Preference.getFloat("last_lat"), Preference.getFloat("last_lon")))
                        .request();
                break;
            case Config.StatePendingSearch:
                //replaceFragment(new FragmentSearchTaxi());
                _b.llMainContainer.removeAllViews();
                replaceFragment(new FragmentSearchTaxi());
                break;
            case Config.StateDriverAccept:
                _b.llMainContainer.removeAllViews();
                replaceFragment(new FragmentDriverAccept(jo));
                break;
            case Config.StateDriverOnWay:
                _b.llMainContainer.removeAllViews();
                replaceFragment(new FragmentDriverAccept(jo));
                break;
            case Config.StateDriverOnPlace:
                _b.llMainContainer.removeAllViews();
                replaceFragment(new FragmentDriverWaitingYou(jo));
                break;
            case Config.StateDriverOrderStarted:
                _b.llMainContainer.removeAllViews();
                replaceFragment(new FragmentOrderStarted());
                break;
            case Config.StateDriverOrderEnd:
                _b.llMainContainer.removeAllViews();
                replaceFragment(new FragmentVoteAfterOrder());
                break;
            default:
                Dlg.alertDialog(getContext(), R.string.Error, String.format("Unknown state code was received: %d", jo.get("status").getAsInt()));
                break;
        }
        Preference.setInt("last_state", state);
    }

    WebRequest.HttpResponse mBroadcastAuth = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            Messanger.create(Messanger.MSG_SOCKET_CONNECTION)
                    .putExtra("openconnection", true)
                    .broadcast();
        }
    };

    WebRequest.HttpResponse mLastState = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode == -1) {
                Dlg.alertDialog(getContext(), R.string.Error, R.string.InternetFail);
            } else if (httpReponseCode < 300) {
                WebRequest.create("/app/mobile/broadcasting/auth", WebRequest.HttpMethod.POST, mBroadcastAuth)
                        .setParameter("channel_name", Config.channelName())
                        .setParameter("socket_id", Config.socketId())
                        .request();
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                changeState(jo.get("status").getAsShort(), jo);
            } else {

            }
        }
    };

    WebRequest.HttpResponse mCoordToAddress = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            mCoordGeocoding = false;
            if (httpReponseCode == -1) {
                Dlg.alertDialog(getContext(), R.string.Error, R.string.InternetFail);
            } else if (httpReponseCode < 300) {
                if (Preference.getFloat("camera_lat") > 0.01) {
                    mCoordGeocoding = true;
                    WebRequest wr = WebRequest.create("", WebRequest.HttpMethod.GET, mCoordToAddress)
                            .setUrl(String.format("https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&kind=house&geocode=%f,%f&results=1&sco=latlong",
                                    Config.yandexGeocodeKey(), Preference.getFloat("camera_lat"), Preference.getFloat("camera_lon")));
                    Preference.setFloat("camera_lat", 0);
                    Preference.setFloat("camera_lon", 0);
                    wr.request();
                    return;
                }
                GeocoderAnswer ga = new GeocoderAnswer(data);
                if (ga.isValid) {
                    Preference.setString("from_display", ga.mAddressLine);
                    Preference.setString("from_title", ga.mStreet + (ga.mHouse.isEmpty() ? "" : ", " + ga.mHouse));
                    Preference.setString("from_subtitle", "");
                    Preference.setFloat("last_lat", (float) ga.mPoint.getLatitude());
                    Preference.setFloat("last_lon", (float) ga.mPoint.getLongitude());
                    Intent cAddressFrom = new Intent(BaseFragment.mBaseFragmentFilter);
                    cAddressFrom.putExtra("cmd", BaseFragment.SET_ADDRESS_FROM_STRING);
                    cAddressFrom.putExtra("address", ga.mStreet + (ga.mHouse.isEmpty() ? "" : ", " + ga.mHouse));
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(cAddressFrom);
                } else {

                }
            } else  {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
            }
        }
    };

    BroadcastReceiver mSocketReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String e = intent.getStringExtra("event");
            System.out.println(e);
            JsonObject jo = JsonParser.parseString(e).getAsJsonObject();
            if (jo.get("event").getAsString().equalsIgnoreCase("Src\\Broadcasting\\Broadcast\\Client\\DriverOnAcceptOrderEvent")) {
                changeState(Config.StateDriverAccept, JsonParser.parseString(jo.get("data").getAsString()).getAsJsonObject());
            } else if (jo.get("event").getAsString().equalsIgnoreCase("Src\\Broadcasting\\Broadcast\\Client\\DriverOnWayOrderEvent")) {
                changeState(Config.StateDriverOnWay, JsonParser.parseString(jo.get("data").getAsString()).getAsJsonObject());
            } else if (jo.get("event").getAsString().equalsIgnoreCase("Src\\Broadcasting\\Broadcast\\Client\\DriverInPlace")) {
                changeState(Config.StateDriverOnPlace, JsonParser.parseString(jo.get("data").getAsString()).getAsJsonObject());
            } else if (jo.get("event").getAsString().equalsIgnoreCase("Src\\Broadcasting\\Broadcast\\Client\\OrderStarted")) {
                changeState(Config.StateDriverOrderStarted, JsonParser.parseString(jo.get("data").getAsString()).getAsJsonObject());
            } else if (jo.get("event").getAsString().equalsIgnoreCase("Src\\Broadcasting\\Broadcast\\Client\\ClientOrderEndData")) {
                changeState(Config.StateDriverOrderEnd, JsonParser.parseString(jo.get("data").getAsString()).getAsJsonObject());
            } else if (jo.get("event").getAsString().equalsIgnoreCase("Src\\Broadcasting\\Broadcast\\Client\\ListenRadiusTaxiEvent")) {
                JsonObject joc = JsonParser.parseString(jo.get("data").getAsString()).getAsJsonObject();
                updateTaxiOnMap(joc.getAsJsonArray("taxis"));
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _b = FragmentMainPageBinding.inflate(getLayoutInflater(), container, false);
        MapKitFactory.initialize(getContext());
        ViewTreeObserver vto = _b.getRoot().getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                _b.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //_b.mapview.setTop(-80);
            }
        });

        _b.llMainContainer.setOnTouchListener(mTouchListener);
        //_b.btnTaxi.setOnTouchListener(mTouchListener);
        return _b.getRoot();
    }

    View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                _b.fr.animate().y(0).setDuration(0).start();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                cx = event.getX() - event.getRawX();
                cy = event.getY() - event.getRawY();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                _b.fr.animate().y(event.getRawY() + cy).setDuration(0).start();
                return true;
            }
            return false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        _b.mapview.onStart();
        _b.mapview.getMap().move(new CameraPosition(new Point(Preference.getFloat("last_lat"), Preference.getFloat("last_lon")), 16,0, 0));
        mPlaceMark = _b.mapview.getMap().getMapObjects().addPlacemark(new Point(Preference.getFloat("last_lat"), Preference.getFloat("last_lon")), mPlaceMarkImage);
        _b.mapview.getMap().addCameraListener(mCameraListener);
        WebRequest.create("/app/mobile/real_state", WebRequest.HttpMethod.GET, mLastState)
                .request();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mSocketReceiver, new IntentFilter(SocketThread.SOCKET_MESSAGE));
    }

    @Override
    public void onStop() {
        super.onStop();
        _b.mapview.getMap().removeCameraListener(mCameraListener);
        _b.mapview.getMap().getMapObjects().remove(mPlaceMark);
        _b.mapview.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mSocketReceiver);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void messageHandler(int msg, Intent i) {
        switch (msg) {
            case BaseFragment.SET_FRAGMENT_TAXIOTPIONS:
                _b.llMainContainer.removeAllViews();
                replaceFragment(new FragmentTaxiOptions());
                break;
            case BaseFragment.SET_MY_LOCATION:
                LocationService.getSingleLocation(mLocationListener);
                break;
        }
    }

    private void updateTaxiOnMap(JsonArray ja) {
        for (int i = 0; i < ja.size(); i++) {
            JsonObject jo = ja.get(i).getAsJsonObject();
            Gson g = new GsonBuilder().create();
            Driver d = g.fromJson(jo, Driver.class);
            _b.mapview.getMap().getMapObjects().addPlacemark(new Point(d.current_coordinate.lat, d.current_coordinate.lut));
        }
    }

    PlacemarkMapObject mPlaceMark;
    ImageProvider mPlaceMarkImage = new ImageProvider() {
        @Override
        public String getId() {
            return "1";
        }

        @Override
        public Bitmap getImage() {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.placemark);
            bm  = Bitmap.createScaledBitmap(bm, 100, 110, false);
            return bm;
        }
    };

    CameraListener mCameraListener = new CameraListener() {
        @Override
        public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition, @NonNull CameraUpdateReason cameraUpdateReason, boolean b) {
            mPlaceMark.setGeometry(cameraPosition.getTarget());

            if (mCoordGeocoding) {
                Preference.setFloat("camera_lat", (float) cameraPosition.getTarget().getLatitude());
                Preference.setFloat("camera_lon", (float) cameraPosition.getTarget().getLongitude());
            } else {
                mCoordGeocoding = true;
                WebRequest.create("", WebRequest.HttpMethod.GET, mCoordToAddress)
                        .setUrl(String.format("https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&kind=house&geocode=%f,%f&results=1&sco=latlong",
                                Config.yandexGeocodeKey(), cameraPosition.getTarget().getLatitude(), cameraPosition.getTarget().getLongitude()))
                        .request();
            }
        }
    };

    LocationService.LocationChangeListener mLocationListener = new LocationService.LocationChangeListener() {
        @Override
        public void location(Location l) {
            _b.mapview.getMap().move(new CameraPosition(new Point(l.getLatitude(), l.getLongitude()), 16, 0, 0), new Animation(Animation.Type.SMOOTH, 1), null);
        }
    };

    public void reset() {
        mActivity.fragmentCallback(BaseActivity.FC_NAVIGATE_MAINPAGE);
    }
}