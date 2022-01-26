package com.example.yelloclient;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.yelloclient.classes.CarClasses;
import com.example.yelloclient.classes.Companies;
import com.example.yelloclient.classes.PaymentTypes;
import com.example.yelloclient.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding _b;
    static final private int REQUEST_LOCATION = 1;
    public CarClasses mCarClasses;
    public List<Integer> mCarOptions = new ArrayList<>();
    public PaymentTypes mPaymentTypes;
    public Integer mPaymentCompany = 0;
    public Companies mCompanies;
    public boolean mIsRent = false;
    public Integer mRentTime = 0;
    public Integer mCurrentCarClass = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());

        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        boolean granted = true;
        for (String p: permissions) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        if (granted) {
            replaceFragment(new FragmentIntro());
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Dlg.alertDialog(this, R.string.background_location_permission_title, getString(R.string.background_location_permission_message), new DialogInterface() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void dismiss() {
                        for (String p: permissions) {
                            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                                shouldShowRequestPermissionRationale(p);
                            }
                        }
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION);
            }
        }

        if (!Preference.isMyServiceRunning(TaxiService.class)) {
            Intent srvIntent = new Intent(this, TaxiService.class);
            startForegroundService(srvIntent);
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_LOCATION);
        return super.shouldShowRequestPermissionRationale(permission);
    }

    @Override
    protected void fragmentCallback(int code) {
        super.fragmentCallback(code);
        switch (code) {
            case FC_NAVIGATE_LOGIN:
                replaceFragment(new FragmentPhoneNumber());
                break;
            case FC_NAVIGATE_SMS_CODE:
                replaceFragment(new FragmentPhoneSms());
                break;
            case FC_NAVIGATE_INTRO:
                replaceFragment(new FragmentIntro());
                break;
            case FC_NAVIGATE_MAINPAGE:
                replaceFragment(new FragmentMainPage());
                break;
            default:
                break;
        }
    }

    void replaceFragment(BaseFragment fr) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fr, fr, fr.tag());
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) {
            return;
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Dlg.alertDialog(this, R.string.background_location_permission_title, getString(R.string.background_location_permission_message), new DialogInterface() {
                @Override
                public void cancel() {
                    finish();
                }

                @Override
                public void dismiss() {
                    finish();
                }
            });

        } else {
            replaceFragment(new FragmentIntro());
        }
    }

    public void setCarClasses(JsonObject jo) {
        GsonBuilder gb = new GsonBuilder();
        Gson g = gb.create();
        mCarClasses = g.fromJson(jo, CarClasses.class);
        for (int i = 0; i < mCarClasses.car_classes.size(); i++) {
            if (mCurrentCarClass == 0) {
                mCarClasses.car_classes.get(i).selected = i == 0 ? 1 : 0;
            } else if (mCurrentCarClass == mCarClasses.car_classes.get(i).class_id) {
                mCarClasses.car_classes.get(i).selected = 1;
            } else {
                mCarClasses.car_classes.get(i).selected = 0;
            }
            byte[] decodedString = Base64.decode(mCarClasses.car_classes.get(i).image, Base64.DEFAULT);
            mCarClasses.car_classes.get(i)._image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        if (mCarClasses.getCurrent() == null) {
            if (mCarClasses.car_classes.size() > 0) {
                mCarClasses.car_classes.get(0).selected = 1;
            }
        }
    }
}