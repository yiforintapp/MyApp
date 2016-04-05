package com.leo.appmaster.home;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.model.ProcessDetectorUsageStats;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.ui.BaseSelfDurationToast;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.Utilities;

import java.util.List;

/**
 * Created by RunLee on 2016/3/31.
 */
public class HomeDetectPresenter {
    private HomeDetectFragment mHomeDetect;
    private BaseSelfDurationToast mPermissionGuideToast;

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
        startOpenLockPerm();
    }

    private String getfilterTarget(Intent intent) {
        if (intent == null) {
            return null;
        }

        List<ResolveInfo> resolveInfos = mHomeDetect.getActivity().getPackageManager().queryIntentActivities(intent, 0);
        String filterTarget = null;
        if (resolveInfos != null && resolveInfos.size() == 1) {
            for (ResolveInfo resolveInfo : resolveInfos) {
                String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
                if (!TextUtils.isEmpty(pkgName)) {
                    filterTarget = pkgName;
                }
            }
        }

        return filterTarget;
    }

    private void startOpenLockPerm() {
        LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        ProcessDetectorUsageStats usageStats = new ProcessDetectorUsageStats();
        if (!usageStats.checkAvailable()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            String filterTarget = getfilterTarget(intent);
            try {
                mHomeDetect.getActivity().startActivity(intent);
                lockManager.filterSelfOneMinites();
                if (!TextUtils.isEmpty(filterTarget)) {
                    lockManager.filterPackage(filterTarget, Constants.TIME_FILTER_TARGET);
                }
            } catch (Exception e) {
            }
        }
        try {
            ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mPermissionGuideToast == null) {
                        mPermissionGuideToast = new BaseSelfDurationToast(mHomeDetect.getActivity());
                    }
                    mPermissionGuideToast.setDuration(1000 * 5);
                    if (Utilities.hasNavigationBar(mHomeDetect.getActivity())) {
                        mPermissionGuideToast.setWindowAnimations(R.style.toast_guide_permission_navigationbar);
                    } else {
                        mPermissionGuideToast.setWindowAnimations(R.style.toast_guide_permission);
                    }
                    mPermissionGuideToast.setMatchParent();
                    mPermissionGuideToast.setGravity(Gravity.BOTTOM, 0, DipPixelUtil.dip2px(mHomeDetect.getActivity(), 14));
                    View v = LayoutInflater.from(mHomeDetect.getActivity()).inflate(R.layout.toast_permission_guide, null);
                    ImageView ivClose = (ImageView) v.findViewById(R.id.iv_permission_guide_close);
                    ivClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mPermissionGuideToast != null) {
                                mPermissionGuideToast.hide();
                            }
                        }
                    });
                    mPermissionGuideToast.setView(v);
                    mPermissionGuideToast.show();
                }
            }, 200);
        } catch (Exception e) {
        }
    }

}
