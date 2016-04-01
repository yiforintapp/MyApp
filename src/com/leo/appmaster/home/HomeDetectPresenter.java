package com.leo.appmaster.home;

import android.widget.Toast;

/**
 * Created by RunLee on 2016/3/31.
 */
public class HomeDetectPresenter {
    private HomeDetectFragment mHomeDetect;

    public HomeDetectPresenter() {

    }

    public void attachView(HomeDetectFragment fragment) {
        this.mHomeDetect = fragment;
    }

    public void detachView() {
        this.mHomeDetect = null;
    }

    public void appSaftHandler() {
        //应用检测结果处理
        Toast.makeText(mHomeDetect.getActivity(), "应用扫描结果", Toast.LENGTH_SHORT).show();
    }

    public void imageSaftHandler() {
        //图片检测结果处理
        Toast.makeText(mHomeDetect.getActivity(), "图片扫描结果", Toast.LENGTH_SHORT).show();
    }

    public void videoSaftHandler() {
        //视频检测结果处理
        Toast.makeText(mHomeDetect.getActivity(), "视频扫描结果", Toast.LENGTH_SHORT).show();
    }


    public void appDangerHandler() {
        //应用检测结果处理
        Toast.makeText(mHomeDetect.getActivity(), "应用扫描危险结果", Toast.LENGTH_SHORT).show();
    }

    public void imageDangerHandler() {
        //图片检测结果处理
        Toast.makeText(mHomeDetect.getActivity(), "图片扫描危险结果", Toast.LENGTH_SHORT).show();
    }

    public void videoDangerHandler() {
        //视频检测结果处理
        Toast.makeText(mHomeDetect.getActivity(), "视频扫描危险结果", Toast.LENGTH_SHORT).show();
    }


    public void centerBannerHandler() {
        //中间banner处理
        Toast.makeText(mHomeDetect.getActivity(), "中间banner", Toast.LENGTH_SHORT).show();
    }
}
