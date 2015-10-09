
package com.leo.appmaster.sdk;



/**
 * Author: stonelam@leoers.com Brief: activity for users to do setting ,etc. ,
 * block them when force update required
 */

public class MainViewActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.checkForceUpdate();
    }

}
