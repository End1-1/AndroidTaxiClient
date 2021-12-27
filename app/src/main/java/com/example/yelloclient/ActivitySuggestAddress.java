package com.example.yelloclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yelloclient.databinding.ActivitySuggestAddressBinding;
import com.example.yelloclient.databinding.ItemSuggestBinding;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.search.KeyValuePair;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.mapkit.search.SuggestOptions;
import com.yandex.mapkit.search.SuggestSession;
import com.yandex.mapkit.search.SuggestType;
import com.yandex.runtime.Error;

import java.io.Serializable;
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
        _b.edtFrom.addTextChangedListener(mTextWatcher);
        _b.edtTo.addTextChangedListener(mTextWatcher);
        _b.edtFrom.setOnClickListener(this);
        _b.edtTo.setOnClickListener(this);
        _b.btnClearFrom.setOnClickListener(this);
        _b.btnClearTo.setOnClickListener(this);
        _b.imgBack.setOnClickListener(this);
        _b.txtBack.setOnClickListener(this);
        _b.txtReady.setOnClickListener(this);

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
            case R.id.txtReady:
                Intent data = new Intent();
                if (mItemFrom != null) {
                    data.putExtra("from_display", mItemFrom.getDisplayText());
                    data.putExtra("from_title", mItemFrom.getTitle().getText());
                    data.putExtra("from_subtitle", mItemFrom.getSubtitle().getText());
                }
                if (mItemTo != null) {
                    data.putExtra("to_display", mItemTo.getDisplayText());
                    data.putExtra("to_title", mItemTo.getTitle().getText());
                    data.putExtra("to_subtitle", mItemTo.getSubtitle().getText());
                }
                setResult(RESULT_OK, data);
                finish();
        }
    }
}