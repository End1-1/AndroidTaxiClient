package com.example.yelloclient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yelloclient.classes.CarClass;
import com.example.yelloclient.databinding.FragmentBeforeOrderBinding;
import com.example.yelloclient.databinding.ItemCarsBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class FragmentBeforeOrder extends BaseFragment {

    private FragmentBeforeOrderBinding _b;
    private boolean mLoading = false;
    private ActivityResultLauncher mAddr;

    public FragmentBeforeOrder() {
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
                            initCoin(null);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentBeforeOrderBinding.inflate(inflater, container, false);
        _b.btnMinimize.setOnClickListener(this);
        _b.edtFrom.setOnClickListener(this);
        _b.edtTo.setOnClickListener(this);
        _b.btnOptions.setOnClickListener(this);
        _b.btnORDER.setOnClickListener(this);
        _b.btnPaymentType.setOnClickListener(this);
        _b.btnMyPos.setOnClickListener(this);
        _b.rvCars.setAdapter(new CarClassAdapter());
        _b.edtFrom.setText(Preference.getString("from_title"));
        _b.edtTo.setText(Preference.getString("to_title"));
        _b.btnMapFrom.setOnClickListener(this);
        _b.btnMapto.setOnClickListener(this);
        _b.btnTaxi.setOnClickListener(this);
        _b.btnRent.setOnClickListener(this);
        return _b.getRoot();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOptions:
                mActivity.fragmentCallback(BaseActivity.FC_NAVIGET_TAXI_OPTIONS);
                Intent cTaxiOptions = new Intent(BaseFragment.mBaseFragmentFilter);
                cTaxiOptions.putExtra("cmd", BaseFragment.SET_FRAGMENT_TAXIOTPIONS);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(cTaxiOptions);
                break;
            case R.id.btnMinimize:

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
            case R.id.btnORDER: {
                initCoin(() -> initOrder());
                break;
            }
            case R.id.btnTaxi:
                _b.btnTaxi.setBackground(getContext().getDrawable(R.drawable.btn_transparent1selected));
                _b.btnRent.setBackground(getContext().getDrawable(R.drawable.btn_transparent1));
                break;
            case R.id.btnRent:
                _b.btnRent.setBackground(getContext().getDrawable(R.drawable.btn_transparent1selected));
                _b.btnTaxi.setBackground(getContext().getDrawable(R.drawable.btn_transparent1));
                break;
            case R.id.btnMyPos:
                Intent cLocation = new Intent(BaseFragment.mBaseFragmentFilter);
                cLocation.putExtra("cmd", BaseFragment.SET_MY_LOCATION);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(cLocation);
                break;
            case R.id.btnMapFrom:
                break;
            case R.id.btnMapto:
                break;
        }
    }

    @Override
    protected void messageHandler(int msg, Intent i) {
        switch (msg) {
            case BaseFragment.SET_ADDRESS_FROM_STRING:
                _b.edtFrom.setText(i.getStringExtra("address"));
                setLoading(true);
                initCoin(null);
                break;
        }
    }

    private void setLoading(boolean v) {
        mLoading = v;
        _b.rvCars.getAdapter().notifyDataSetChanged();
        _b.rvCars.setEnabled(!v);
        _b.btnMinimize.setEnabled(!v);
        _b.btnTaxi.setEnabled(!v);
        _b.btnORDER.setEnabled(!v);
        _b.btnRent.setEnabled(!v);
        _b.btnOptions.setEnabled(!v);
        _b.btnPaymentType.setEnabled(!v);
    }

    public void initOrder() {
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
        jphone.addProperty("passenger", "");
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
        WebRequest.create("/app/mobile/init_order", WebRequest.HttpMethod.POST, mOrderNow)
                .setBody(jo.toString())
                .request();
    }

    private void initCoin(WebRequest.HttpPostLoad post) {
        setLoading(true);

        JsonObject jo = new JsonObject();

        ((MainActivity) mActivity).mCurrentCarClass  = ((MainActivity) mActivity).mCarClasses.getCurrent().class_id;
        JsonObject jcar = new JsonObject();
        jcar.addProperty("class", ((MainActivity) mActivity).mCurrentCarClass);
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
        jphone.addProperty("passenger", "");
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
                .setPostLoad(post)
                .setBody(jo.toString())
                .request();
    }

    private WebRequest.HttpResponse mInitCoin = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            setLoading(false);
            if (httpReponseCode == -1) {
                Dlg.alertDialog(getContext(), R.string.Error, R.string.InternetFail);
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

    WebRequest.HttpResponse mOrderNow = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            setLoading(false);
            if (httpReponseCode == -1) {

            } else if (httpReponseCode < 300) {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                getParentFragmentManager().getFragments().get(0);
                FragmentMainPage fmp = (FragmentMainPage) getParentFragmentManager().getFragments().get(0);
                if (fmp != null) {
                    fmp.reset();
                }
            } else  {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
            }
        }
    };

    class CarClassAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Drawable drawable;
        public CarClassAdapter() {
            ImageDecoder.Source source;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                source = ImageDecoder.createSource(getResources(), R.drawable.load1);
                try {
                    drawable = ImageDecoder.decodeDrawable(source);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                drawable = AppCompatResources.getDrawable(getContext(), R.drawable.load1);
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
                _b.txtPrice.setText(String.format("%.0f %s", cc.min_price < 0.1 ? cc.coin : cc.min_price, cc.currency));
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
                if (mLoading) {
                    return;
                }
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