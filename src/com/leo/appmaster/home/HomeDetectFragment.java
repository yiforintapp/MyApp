package com.leo.appmaster.home;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.leo.appmaster.R;

public class HomeDetectFragment extends Fragment implements View.OnClickListener {
    private LinearLayout mResultAppLt;
    private LinearLayout mResultImgLt;
    private LinearLayout mResultVideoLt;
    private RelativeLayout mCenterTipRt;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_detect, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout rootView = (LinearLayout) view.findViewById(R.id.det_result_ly);
        mResultAppLt = (LinearLayout) rootView.findViewById(R.id.lt_det_result_app);
        mResultImgLt = (LinearLayout) rootView.findViewById(R.id.lt_det_result_img);
        mResultVideoLt = (LinearLayout) rootView.findViewById(R.id.lt_det_result_video);
        mCenterTipRt = (RelativeLayout) view.findViewById(R.id.lt_home_det_tip);
        mResultAppLt.setOnClickListener(this);
        mResultImgLt.setOnClickListener(this);
        mResultVideoLt.setOnClickListener(this);
        mCenterTipRt.setOnClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        Activity activity = getActivity();
        switch (v.getId()) {
            case R.id.lt_det_result_app:
                //应用扫描结果
                Toast.makeText(activity,"应用扫描结果",Toast.LENGTH_SHORT).show();
                break;
            case R.id.lt_det_result_img:
                //图片扫描结果
                Toast.makeText(activity,"图片扫描结果",Toast.LENGTH_SHORT).show();
                break;
            case R.id.lt_det_result_video:
                //视频扫描结果
                Toast.makeText(activity,"视频扫描结果",Toast.LENGTH_SHORT).show();
                break;
            case R.id.lt_home_det_tip:
                //中间banner
                Toast.makeText(activity,"中间banner",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
