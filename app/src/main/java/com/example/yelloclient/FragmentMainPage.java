package com.example.yelloclient;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yelloclient.classes.CarClass;
import com.example.yelloclient.databinding.FragmentMainPageBinding;
import com.example.yelloclient.databinding.ItemCarsBinding;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.runtime.image.ImageProvider;

import java.io.IOException;

public class FragmentMainPage extends BaseFragment {

    public static final String tag = "FragmentMainPage";

    private FragmentMainPageBinding _b;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentMainPageBinding.inflate(getLayoutInflater(), container, false);
        MapKitFactory.initialize(getContext());
        _b.btnMinimize.setOnClickListener(this);
        _b.rvCars.setAdapter(new CarClassAdapter());
        ViewTreeObserver vto = _b.getRoot().getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                _b.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                _b.mapview.setTop(_b.mapview.getTop() - 100);
            }
        });
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
        switch (view.getId()) {
            case R.id.btnMinimize:
                showHideFragment();
                break;
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
        }
    };

    public void showHideFragment() {
        ObjectAnimator animation = ObjectAnimator.ofFloat(_b.fr, "translationY", 100f);
        animation.setDuration(2000);
        animation.start();

//        ValueAnimator anim = ValueAnimator.ofInt(_b.fr.getMeasuredHeight(), -500);
//        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                int val = (Integer) valueAnimator.getAnimatedValue();
//                ViewGroup.LayoutParams layoutParams = _b.fr.getLayoutParams();
//                layoutParams.height = val;
//                _b.fr.setLayoutParams(layoutParams);
//            }
//        });
//        anim.setDuration(1500);
//        anim.start();
    }

    class CarClassAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Drawable drawable;
        public CarClassAdapter() {
            ImageDecoder.Source source = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                source = ImageDecoder.createSource(getResources(), R.drawable.load1);
                try {
                    drawable = ImageDecoder.decodeDrawable(source);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                drawable = getContext().getDrawable(R.drawable.load1);
            }
        }

        private class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            public ItemCarsBinding _b;

            public VH(ItemCarsBinding b) {
                super(b.getRoot());
                _b = b;
                _b.getRoot().setOnClickListener(this);
            }

            public void onBind(int position) {
                CarClass cc = ((MainActivity) mActivity).mCarClasses.car_classes.get(position);
                _b.txtCarClass.setText(cc.name);
                _b.txtPrice.setText(String.format("%.0f", cc.min_price));
                _b.img.setImageBitmap(cc._image);
                _b.img.setAlpha(cc.selected == 1 ? 1f : 0.2f);
                _b.gif.setImageDrawable(drawable);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ((AnimatedImageDrawable) drawable).start();
                }
            }

            @Override
            public void onClick(View view) {
                for (int i = 0; i < ((MainActivity) mActivity).mCarClasses.car_classes.size(); i++) {
                    ((MainActivity) mActivity).mCarClasses.car_classes.get(i).selected = 0;
                }
                ((MainActivity) mActivity).mCarClasses.car_classes.get(getAdapterPosition()).selected = 1;
                notifyDataSetChanged();
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCarsBinding b = ItemCarsBinding.inflate(getLayoutInflater(), parent, false);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((VH) holder).onBind(position);
        }

        @Override
        public int getItemCount() {
            return ((MainActivity) mActivity).mCarClasses.car_classes.size();
        }
    }
}