package com.leo.appmaster.battery;

import com.leo.appmaster.R;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BatteryBoostResultFragment extends Fragment {
    private Activity mActivity;
    @Override
    public void onViewCreated(View view, @Nullable
    Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_app_beauty, container, false);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }
}
