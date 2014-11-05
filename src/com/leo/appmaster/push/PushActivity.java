
package com.leo.appmaster.push;

import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leoers.leoanalytics.utils.Debug;

public class PushActivity extends Activity implements View.OnClickListener {

    private final static String TAG = PushActivity.class.getSimpleName();

    private EditText mPhoneNumber = null;

    private final static String GP_MARKET_PACKAGE_NAME = "com.android.vending";
    private final static String GP_WEB_URL = "https://play.google.com/store/apps/details?id=com.leo.appmaster";
    private final static int INDIAN_MOBILE_LENGTH = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI(getIntent());
    }

    private void initUI(Intent i) {
        setContentView(R.layout.dialog_single_input_alarm);

        mPhoneNumber = (EditText) findViewById(R.id.et_input);
        mPhoneNumber.setGravity(Gravity.CENTER_HORIZONTAL);
        mPhoneNumber.setRawInputType(InputType.TYPE_CLASS_PHONE);
        mPhoneNumber.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(INDIAN_MOBILE_LENGTH)
        });

        String title = i.getStringExtra(PushUIHelper.EXTRA_TITLE);
        String content = i.getStringExtra(PushUIHelper.EXTRA_CONTENT);
        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(title);
        TextView tvContent = (TextView) findViewById(R.id.dlg_content);
        tvContent.setText(content);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int dpi = dm.densityDpi;
        int height = 30 * dpi / 160;

        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel));
        tvCancel.setOnClickListener(this);
        tvCancel.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, height, 1.2f));

        TextView tvGO = (TextView) findViewById(R.id.dlg_right_btn);
        tvGO.setText(getString(R.string.goto_gp));
        tvGO.setOnClickListener(this);
        tvGO.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, height, 0.8f));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dlg_left_btn:
                /* user ignore this activity */
                PushUIHelper.getInstance(this).sendACK(false, "");
                finish();
                break;
            case R.id.dlg_right_btn:
                /* get money */
                handleCommit();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            PushUIHelper.getInstance(this).sendACK(false, "");
        }
        return super.onKeyDown(keyCode, event);
    }

    private void handleCommit() {
        String phone = mPhoneNumber.getText().toString().trim();
        String regexp = "([0-9]+)";
        Pattern p = Pattern.compile(regexp);

        if (phone.length() != INDIAN_MOBILE_LENGTH || !p.matcher(phone).matches()) {
            /* invalid indian phone number */
            Toast.makeText(this, getString(R.string.invalid_number), Toast.LENGTH_SHORT).show();
            return;
        }
        if (hasGPInstalled()) {
            /* go to GP client */
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="
                            + getPackageName()));
            i.setPackage(GP_MARKET_PACKAGE_NAME);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else {
            /* go to GP website */
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(GP_WEB_URL);
            intent.setData(content_url);
            startActivity(intent);
        }
        PushUIHelper.getInstance(this).sendACK(true, phone);
        finish();
    }

    private boolean hasGPInstalled() {
        PackageManager pm = this.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfos) {
            Debug.d(TAG, "name=" + info.activityInfo.packageName);
            if (info.activityInfo.packageName.equals(GP_MARKET_PACKAGE_NAME)) {
                return true;
            }
        }
        return false;
    }
}
