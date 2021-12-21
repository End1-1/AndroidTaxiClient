package com.example.yelloclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.yelloclient.databinding.FragmentMainPageBinding;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.runtime.image.ImageProvider;

public class FragmentMainPage extends BaseFragment {

    public static final String tag = "FragmentMainPage";

    private FragmentMainPageBinding _b;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentMainPageBinding.inflate(getLayoutInflater(), container, false);
        MapKitFactory.initialize(getContext());
        return _b.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        _b.mapview.onStart();
        _b.mapview.getMap().move(new CameraPosition(new Point(Preference.getFloat("last_lat"), Preference.getFloat("last_lon")), 16,0, 0));
        mPlaceMark = _b.mapview.getMap().getMapObjects().addPlacemark(new Point(Preference.getFloat("last_lat"), Preference.getFloat("last_lon")), mPlaceMarkImage);
        _b.mapview.getMap().addCameraListener(mCameraListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        _b.mapview.getMap().removeCameraListener(mCameraListener);
        _b.mapview.getMap().getMapObjects().remove(mPlaceMark);
        _b.mapview.onStop();
    }

    @Override
    public void onClick(View view) {

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
        }
    };
}