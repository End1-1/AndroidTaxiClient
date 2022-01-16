package com.example.yelloclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yelloclient.classes.GeocoderAnswer;
import com.example.yelloclient.databinding.ActivitySuggestAddressBinding;
import com.example.yelloclient.databinding.ItemSuggestBinding;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.mapkit.search.SuggestOptions;
import com.yandex.mapkit.search.SuggestSession;
import com.yandex.mapkit.search.SuggestType;
import com.yandex.runtime.Error;

import java.util.ArrayList;
import java.util.List;

public class ActivitySuggestAddress extends BaseActivity implements View.OnClickListener {

    private ActivitySuggestAddressBinding _b;
    private boolean mFrom = true;
    private SearchManager mSearchManager;
    private SuggestSession mSuggestSession;
    private List<SuggestItem> mSuggestItemList = new ArrayList<>();
    private SuggestAdapter mResultAdapter;
    private List<String> mSuggestResultList = new ArrayList<>();
    private SuggestItem mItemFrom;
    private SuggestItem mItemTo;
    boolean mTextChangedByItemClick = false;
    private boolean mRequestFromPoint = false;
    private boolean mRequestToPoint = false;
    Intent mData = new Intent();


    private final Point CENTER = new Point(Preference.getFloat("last_lat"), Preference.getFloat("last_lon"));
    private final double BOX_SIZE = 0.2;
    private final BoundingBox BOUNDING_BOX = new BoundingBox(
            new Point(CENTER.getLatitude() - BOX_SIZE, CENTER.getLongitude() - BOX_SIZE),
            new Point(CENTER.getLatitude() + BOX_SIZE, CENTER.getLongitude() + BOX_SIZE));
    private final SuggestOptions SEARCH_OPTIONS =  new SuggestOptions().setSuggestTypes(
            SuggestType.GEO.value |
                    SuggestType.BIZ.value |
                    SuggestType.TRANSIT.value);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchFactory.initialize(this);
        mSearchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        mSuggestSession = mSearchManager.createSuggestSession();

        _b = ActivitySuggestAddressBinding.inflate(getLayoutInflater());
        _b.edtFrom.setOnClickListener(this);
        _b.edtTo.setOnClickListener(this);
        _b.btnClearFrom.setOnClickListener(this);
        _b.btnClearTo.setOnClickListener(this);
        _b.imgBack.setOnClickListener(this);
        _b.txtBack.setOnClickListener(this);
        _b.txtReady.setOnClickListener(this);
        _b.edtFrom.setText(Preference.getString("from_title"));
        _b.edtTo.setText(Preference.getString("to_title"));
        _b.edtFrom.addTextChangedListener(mTextWatcher);
        _b.edtTo.addTextChangedListener(mTextWatcher);

        mResultAdapter = new SuggestAdapter(this);
        _b.list.setAdapter(mResultAdapter);
        _b.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mTextChangedByItemClick = true;
                if (mFrom) {
                    mItemFrom = mSuggestItemList.get(i);
                    _b.edtFrom.setText(mItemFrom.getTitle().getText());
                } else {
                    mItemTo = mSuggestItemList.get(i);
                    _b.edtTo.setText(mItemTo.getTitle().getText());
                }
                _b.list.setVisibility(View.INVISIBLE);
            }
        });

        if (getIntent().getExtras().getBoolean("from", true)) {
            _b.edtFrom.requestFocus();
        } else {
            _b.edtTo.requestFocus();
        }

        _b.edtFrom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mFrom = true;
                }
                return false;
            }
        });
        _b.edtTo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mFrom = false;
                }
                return false;
            }
        });

        setContentView(_b.getRoot());
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (mTextChangedByItemClick) {
                mTextChangedByItemClick = false;
                return;
            }
            if (editable.toString().length() > 2) {
                _b.list.setVisibility(View.INVISIBLE);
                mSuggestSession.suggest(editable.toString(), BOUNDING_BOX, SEARCH_OPTIONS, mSuggestListener);
            }
        }
    };

    private SuggestSession.SuggestListener mSuggestListener = new SuggestSession.SuggestListener() {
        @Override
        public void onResponse(@NonNull List<SuggestItem> list) {
            mSuggestItemList = list;
            mSuggestResultList.clear();
            for (int i = 0; i < list.size(); i++) {
                mSuggestResultList.add(list.get(i).getDisplayText());
            }
            mResultAdapter.notifyDataSetChanged();
            _b.list.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError(@NonNull Error error) {

        }
    };

    private class SuggestAdapter extends ArrayAdapter {

        private ItemSuggestBinding _view;

        public SuggestAdapter(@NonNull Context context) {
            super(context, R.layout.item_suggest);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                _view = ItemSuggestBinding.inflate(getLayoutInflater(), parent, false);
            } else {
                _view = ItemSuggestBinding.bind(convertView);
            }
            _view.txtTitle.setText(mSuggestItemList.get(position).getTitle().getText());
            String st = mSuggestItemList.get(position).getSubtitle() == null ? "" : mSuggestItemList.get(position).getSubtitle().getText();
            _view.txtSubtitle.setText(st);
            return _view.getRoot();
        }

        @Override
        public int getCount() {
            return mSuggestItemList.size();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edtFrom:
                mFrom = true;
                break;
            case R.id.edtTo:
                mFrom = false;
                break;
            case R.id.btnClearFrom:
                mItemFrom = null;
                _b.edtFrom.setText("");
                _b.edtFrom.requestFocus();
                mFrom = true;
                break;
            case R.id.btnClearTo:
                mItemTo = null;
                _b.edtTo.setText("");
                _b.edtTo.requestFocus();
                mFrom = false;
                break;
            case R.id.txtBack:
                finish();
                break;
            case R.id.imgBack:
                finish();
                break;
            case R.id.txtReady:

                if (mItemFrom != null) {
                    mData.putExtra("from_display", mItemFrom.getDisplayText());
                    mData.putExtra("from_title", mItemFrom.getTitle().getText());
                    mData.putExtra("from_subtitle", mItemFrom.getSubtitle().getText());
                    mRequestFromPoint = true;
                } else {
                    mData.putExtra("from_display", "");
                    mData.putExtra("from_title", "");
                    mData.putExtra("from_subtitle", "");
                }
                if (mItemTo != null) {
                    mData.putExtra("to_display", mItemTo.getDisplayText());
                    mData.putExtra("to_title", mItemTo.getTitle().getText());
                    mData.putExtra("to_subtitle", mItemTo.getSubtitle().getText());
                    mRequestToPoint = true;
                } else {
                    mData.putExtra("to_display", "");
                    mData.putExtra("to_title", "");
                    mData.putExtra("to_subtitle", "");
                }
                if (mRequestFromPoint) {
                    WebRequest.create("", WebRequest.HttpMethod.GET, mFromPoint)
                            .setUrl(String.format("https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&kind=house&geocode=%s&results=1&sco=latlong",
                                    Config.yandexGeocodeKey(), mData.getStringExtra("from_display")))
                            .request();
                    return;
                }
                if (!mRequestFromPoint && mRequestToPoint) {
                    WebRequest.create("", WebRequest.HttpMethod.GET, mToPoint)
                            .setUrl(String.format("https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&kind=house&geocode=%s&results=1&sco=latlong",
                                    Config.yandexGeocodeKey(), mData.getStringExtra("to_display")))
                            .request();
                    return;
                }
                setResult(RESULT_OK, mData);
                finish();
        }
    }

    WebRequest.HttpResponse mFromPoint = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            GeocoderAnswer ga = new GeocoderAnswer(data);
            if (ga.isValid) {
                Preference.setFloat("last_lat", (float) ga.mPoint.getLatitude());
                Preference.setFloat("last_lon", (float) ga.mPoint.getLongitude());
                if (mRequestToPoint) {
                    WebRequest.create("", WebRequest.HttpMethod.GET, mToPoint)
                            .setUrl(String.format("https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&kind=house&geocode=%s&results=1&sco=latlong",
                                    Config.yandexGeocodeKey(), Preference.getString("to_display")))
                            .request();
                } else {
                    setResult(RESULT_OK, mData);
                    finish();
                }
            }
        }
    };

    WebRequest.HttpResponse mToPoint = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            GeocoderAnswer ga = new GeocoderAnswer(data);
            if (ga.isValid) {
                Preference.setFloat("to_lat", (float) ga.mPoint.getLatitude());
                Preference.setFloat("to_lon", (float) ga.mPoint.getLongitude());
                setResult(RESULT_OK, mData);
                finish();
            }
        }
    };
}