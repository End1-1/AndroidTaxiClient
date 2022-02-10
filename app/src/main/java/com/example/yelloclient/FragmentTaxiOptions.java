package com.example.yelloclient;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yelloclient.classes.CarClass;
import com.example.yelloclient.classes.CarOption;
import com.example.yelloclient.databinding.FragmentTaxiOptionsBinding;
import com.example.yelloclient.databinding.ItemTaxiOptionsBinding;

import java.security.cert.PKIXRevocationChecker;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentTaxiOptions#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTaxiOptions extends BaseFragment {

    private FragmentTaxiOptionsBinding _b;
    private MainActivity _m;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentTaxiOptionsBinding.inflate(inflater, container, false);
        _b.rvOptions.setAdapter(new OptionsAdapter());
        _b.rvOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        _b.btnOptionsReady.setOnClickListener(this);
        return _b.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        _m = (MainActivity) mActivity;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOptionsReady:
                getParentFragmentManager().getFragments().get(0);
                FragmentMainPage fmp = (FragmentMainPage) getParentFragmentManager().getFragments().get(0);
                if (fmp != null) {
                    fmp.reset();
                }
                break;
        }
    }

    private class OptionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            ItemTaxiOptionsBinding o = ItemTaxiOptionsBinding.inflate(getLayoutInflater(), viewGroup, false);
            return new OptionItem(o);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((OptionItem) viewHolder).onBind(i);
        }

        @Override
        public int getItemCount() {
            return _m.mCarClasses.getCurrent().car_options.size();
        }

        private class OptionItem extends RecyclerView.ViewHolder {

            private ItemTaxiOptionsBinding _o;

            public OptionItem(@NonNull ItemTaxiOptionsBinding o) {
                super(o.getRoot());
                _o = o;
                _o.ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int i = getAdapterPosition();
                        CarClass cc = _m.mCarClasses.getCurrent();
                        CarOption co = cc.car_options.get(i);
                        if (isChecked) {
                            if (!_m.mCarOptions.contains(co.id)) {
                                _m.mCarOptions.add(co.id);
                            }
                        } else {
                            if (_m.mCarOptions.contains(co.id)) {
                                _m.mCarOptions.remove(Integer.valueOf(co.id));
                            }
                        }
                    }
                });
            }

            public void onBind(int position) {
                CarClass cc = _m.mCarClasses.getCurrent();
                CarOption co = cc.car_options.get(position);
                _o.txtOptionPrice.setText(String.valueOf(co.price));
                _o.txtName.setText(co.name);
                _o.ch.setChecked(_m.mCarOptions.contains(co.id));
            }
        }
    };
}