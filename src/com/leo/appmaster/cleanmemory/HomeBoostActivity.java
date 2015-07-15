
package com.leo.appmaster.cleanmemory;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class HomeBoostActivity extends Activity {
    private static final String TAG = "HomeBoostActivity";
    private Rect mBoundsRect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_boost_activity);
        handleIntent();
        initUI();
        LeoLog.e(TAG, mBoundsRect.toString());
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mBoundsRect = intent.getSourceBounds();
    }

    private void initUI() {

    }

    public void onClick(View view) {
        Toast.makeText(this, mBoundsRect.toString(), Toast.LENGTH_SHORT).show();
    }

}
