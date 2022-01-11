package com.example.yelloclient;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.yelloclient.classes.CarClasses;
import com.example.yelloclient.classes.Companies;
import com.example.yelloclient.classes.PaymentTypes;
import com.example.yelloclient.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding _b;
    static final private int REQUEST_LOCATION = 1;
    public CarClasses mCarClasses;
    public PaymentTypes mPaymentTypes;
    public Companies mCompanies;

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
            replaceFragment(new FragmentIntro(), FragmentIntro.tag);
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
                replaceFragment(new FragmentPhoneNumber(), FragmentPhoneNumber.tag);
                break;
            case FC_NAVIGATE_SMS_CODE:
                replaceFragment(new FragmentPhoneSms(), FragmentPhoneSms.tag);
                break;
            case FC_NAVIGATE_INTRO:
                replaceFragment(new FragmentIntro(), FragmentIntro.tag);
                break;
            case FC_NAVIGATE_MAINPAGE:
                replaceFragment(new FragmentMainPage(), FragmentMainPage.tag);
                break;
            default:
                break;
        }
    }

    void replaceFragment(Fragment fr, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fr, fr, tag);
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
            replaceFragment(new FragmentIntro(), FragmentIntro.tag);
        }
    }
}