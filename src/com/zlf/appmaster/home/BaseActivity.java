
package com.zlf.appmaster.home;

/**
 * Author: stonelam@leoers.com
 * Brief: Base activity to be tracked by application so that we can finish them when completely exit is required
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;


public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        try {
            super.onStop();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        try {
            super.onPause();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Throwable e) {

        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }
}
