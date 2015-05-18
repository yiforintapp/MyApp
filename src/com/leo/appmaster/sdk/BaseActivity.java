
package com.leo.appmaster.sdk;

/**
 * Author: stonelam@leoers.com
 * Brief: Base activity to be tracked by application so that we can finish them when completely exit is required
 * */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.leo.appmaster.AppMasterApplication;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppMasterApplication.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        AppMasterApplication.getInstance().removeActivity(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.onResume(this);
    }

    @Override
    protected void onPause() {
        SDKWrapper.onPause(this);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            
        }
    }
    
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
//        try {
//            super.onRestoreInstanceState(savedInstanceState, persistentState);
//        } catch (Exception e) {
//            
//        }
//    }

}
