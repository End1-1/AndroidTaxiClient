package com.example.yelloclient;

import static com.example.yelloclient.BaseActivity.FC_NAVIGATE_INTRO;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yelloclient.classes.CarClass;
import com.example.yelloclient.databinding.FragmentMainPageBinding;
import com.example.yelloclient.databinding.ItemCarsBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.Date;

public class FragmentMainPage extends BaseFragment {

    public static final String tag = "FragmentMainPage";

    private FragmentMainPageBinding _b;
    private boolean mLoading = false;

    public FragmentMainPage() {
        mAddr = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data.getStringExtra("from_display") != null) {
                                Preference.setString("from_display", data.getStringExtra("from_display"));
                                Preference.setString("from_title", data.getStringExtra("from_title"));
                                Preference.setString("from_subtitle", data.getStringExtra("from_subtitle"));
                                _b.edtFrom.setText(Preference.getString("from_title"));
                            } else {
                                Preference.setString("from_display", "");
                                _b.edtFrom.setText("");
                            }
                            if (data.getStringExtra("to_display") != null) {
                                Preference.setString("to_display", data.getStringExtra("to_display"));
                                Preference.setString("to_title", data.getStringExtra("to_title"));
                                Preference.setString("to_subtitle", data.getStringExtra("to_subtitle"));
                                _b.edtTo.setText(Preference.getString("to_title"));
                            } else {
                                Preference.setString("to_display", "");
                                _b.edtTo.setText("");
                            }
                            initCoin();
                        }
                    }
                });
    }

    WebRequest.HttpResponse mInitCoin = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            setLoading(false);
            if (httpReponseCode == -1) {

            } else if (httpReponseCode < 300) {
                JsonObject jo = new JsonObject();
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().get("data").getAsJsonArray();
                jo.add("car_classes", ja);
                ((MainActivity) mActivity).setCarClasses(jo);
                _b.rvCars.getAdapter().notifyDataSetChanged();
            } else  {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _b = FragmentMainPageBinding.inflate(getLayoutInflater(), container, false);
        MapKitFactory.initialize(getContext());
        _b.btnMinimize.setOnClickListener(this);
        _b.edtFrom.setOnClickListener(this);
        _b.edtTo.setOnClickListener(this);
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
            case R.id.edtFrom: {
                Intent intent = new Intent(getContext(), ActivitySuggestAddress.class);
                intent.putExtra("from", true);
                mAddr.launch(intent);
                break;
            }
            case R.id.edtTo: {
                Intent intent = new Intent(getContext(), ActivitySuggestAddress.class);
                intent.putExtra("from", false);
                mAddr.launch(intent);
                break;
            }
        }
    }

    private void setLoading(boolean v) {
        mLoading = v;
        _b.rvCars.getAdapter().notifyDataSetChanged();
        _b.btnMinimize.setEnabled(!v);
        _b.btnTaxi.setEnabled(!v);
        _b.btnORDER.setEnabled(!v);
        _b.btnRent.setEnabled(!v);
        _b.btnOptions.setEnabled(!v);
    }

    private void initCoin() {
        setLoading(true);

        JsonObject jo = new JsonObject();

        JsonObject jcar = new JsonObject();
        jcar.addProperty("class", ((MainActivity) mActivity).mCarClasses.getCurrent().class_id);
        JsonArray jcarOptions = new JsonArray();
        for (int o: ((MainActivity)  mActivity).mCarOptions) {
            jcarOptions.add(o);
        }
        jcar.add("options", jcarOptions);
        jcar.addProperty("comments", Preference.getString("driver_comment"));
        jo.add("car", jcar);

        JsonObject jpayment = new JsonObject();
        jpayment.addProperty("type", ((MainActivity) mActivity).mPaymentTypes.getCurrent().id);
        jpayment.addProperty("company", ((MainActivity) mActivity).mPaymentCompany);
        jo.add("payment", jpayment);

        JsonObject jroute = new JsonObject();
        JsonArray jfromCoord = new JsonArray();
        jfromCoord.add(Preference.getFloat("last_lat"));
        jfromCoord.add(Preference.getFloat("last_lon"));
        jroute.add("from", jfromCoord);
        jroute.addProperty("from_address", Preference.getString("from_display"));

        JsonArray jtoCoord = new JsonArray();
        if (Preference.getFloat("to_lat") > 0.01) {
            jtoCoord.add(Preference.getFloat("to_lat"));
            jtoCoord.add(Preference.getFloat("to_lon"));
        }
        jroute.add("to", jtoCoord);
        jroute.addProperty("to_address", Preference.getString("to_display"));
        jo.add("route", jroute);

        JsonObject jtime = new JsonObject();
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        jtime.addProperty("zone", "Asia/Yerevan");
        jtime.addProperty("create_time", df.format("yyyy-MM-dd HH:mm", Calendar.getInstance().getTime()).toString());
        jtime.addProperty("time", df.format("yyyy-MM-dd HH:mm", Calendar.getInstance().getTime()).toString());
        jo.add("time", jtime);

        JsonObject jphone = new JsonObject();
        jphone.addProperty("client", Preference.getString("phone"));
        jphone.addProperty("passanger", "");
        jo.add("phone", jphone);

        JsonObject jmeet = new JsonObject();
        jmeet.addProperty("is_meet", false);
        jmeet.addProperty("place_id", "");
        jmeet.addProperty("place_type", "");
        jmeet.addProperty("number", "");
        jmeet.addProperty("text", "");
        jo.add("meet", jmeet);

        jo.addProperty("is_rent", ((MainActivity) mActivity).mIsRent);
        jo.addProperty("rent_time", ((MainActivity) mActivity).mRentTime);

        WebRequest.create("/app/mobile/init_coin", WebRequest.HttpMethod.POST, mInitCoin)
                .setBody(jo.toString())
                .request();
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
                _b.gif.setVisibility(mLoading ? View.VISIBLE : View.INVISIBLE);
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

    ActivityResultLauncher<Intent> mAddr;
}