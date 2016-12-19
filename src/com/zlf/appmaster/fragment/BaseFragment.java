package com.zlf.appmaster.fragment;

import android.app.Activity;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.zlf.appmaster.utils.LeoLog;

/**
 * @author zwy if we extends this class,we must: first provider layout id in
 *         method layoutResourceId() secend we init our UI in onInitUI.
 */
public abstract class BaseFragment extends Fragment {

    protected FragmentActivity mActivity;
    protected View mRootView;

    protected BatteryManager mBatteryManager;
    final String TAG = getClass().getSimpleName();

    @Override
    public void onAttach(Activity activity) {
        mActivity = (FragmentActivity) activity;
        getArguments();
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = LayoutInflater.from(mActivity).inflate(
                    layoutResourceId(), null);
            onInitUI();
        } else {
            detachRootView();
        }
        return mRootView;
    }

    protected void detachRootView() {
        if (mRootView == null) {
            return;
        }

        ViewParent parent = mRootView.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mRootView);
        }
    }

    /**
     * @return provider layout id
     */
    protected abstract int layoutResourceId();

    /**
     * init UI only one time
     */
    protected abstract void onInitUI();

    protected View findViewById(int id) {
        View result = null;
        if (mRootView != null) {
            result = mRootView.findViewById(id);
        }
        if (result == null && mActivity != null) {
            result = mActivity.findViewById(id);
        }

        return result;
//		if (mRootView == null) {
//			try {
//				// throw new Exception(getString(R.string.exception_not_root));
//			} catch (Exception e) {
//				// LTLog.e(TAG, e.getMessage());
//			}
//		}
//		return mRootView.findViewById(id);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean onBackPressed() {
        return false;
    }

    public boolean onMenuPressed() {
        return false;
    }

    public void onBackgroundChanged(int color) {

    }

    public void toRequestDate(){
    }

}
