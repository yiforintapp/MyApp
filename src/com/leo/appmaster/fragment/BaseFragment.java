
package com.leo.appmaster.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * @author lipeil
 * if we extends this class,we must:
 * first provider layout id in method   layoutResourceId()
 * secend we init our UI in onInitUI.
 */
public abstract class BaseFragment extends Fragment {

    protected FragmentActivity mActivity;
    protected View mRootView;

    final String TAG = getClass().getSimpleName();
    
    @Override
    public void onAttach(Activity activity) {
        mActivity = (FragmentActivity) activity;
        getArguments();
//        Log.e("xxxx", "onAttach");
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mRootView == null){
            mRootView = LayoutInflater.from(mActivity).inflate(layoutResourceId(), null);
            onInitUI();
        }else{
            detachRootView();
        }
//        Log.e("xxxx", "onCreateView");
        return mRootView;
    }
    
    protected void detachRootView(){
        if(mRootView == null){
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
    
    protected View findViewById(int id){   
        if(mRootView == null){
            try {
//                throw new Exception(getString(R.string.exception_not_root));
            } catch (Exception e) {             
//                LTLog.e(TAG, e.getMessage());
            }
        }
        return mRootView.findViewById(id);
    }   

    @Override
    public void onDetach() {
//        Log.e("xxxx", "onDetach");
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
//    	Log.e("xxxx", "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
//    	Log.e("xxxx", "onDestroy");
        super.onDestroy();
    }
    
    public boolean onBackPressed() {
        return false;
    }

    public boolean onMenuPressed() {
        return false;
    }

}
