
package com.leo.appmaster.appsetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.home.GooglePlayGuideActivity;
import com.leo.appmaster.home.ProtocolActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.videohide.VideoGriActivity;
import com.leo.appmaster.videohide.VideoHideMainActivity;
import com.leoers.leoanalytics.LeoStat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;

public class AboutActivity extends BaseActivity implements OnClickListener {

    private CommonTitleBar mTtileBar;
    private TextView mAppVersion;
    private LeoPopMenu mLeoPopMenu;
    private Button mShowProtocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_about_layout);
        mTtileBar = (CommonTitleBar) findViewById(R.id.about_title_bar);
        mTtileBar.setTitle(R.string.app_setting_about);
        mTtileBar.setOptionImageVisibility(View.VISIBLE);
        mTtileBar.openBackView();
        mTtileBar.setOptionText("");
        mTtileBar.setOptionImage(R.drawable.setting_selector);
        mTtileBar.setOptionListener(this);
        mShowProtocol = (Button) findViewById(R.id.check_update_button);
        mShowProtocol.setOnClickListener(this);

        mAppVersion = (TextView) findViewById(R.id.app_version);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            mAppVersion.setText("V" + versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLeoPopMenu != null)
        {
            mLeoPopMenu.dismissSnapshotList();
            mLeoPopMenu = null;
        }
    }

    @Override
    protected void onResume() {

        if (mShowProtocol != null) {
            mShowProtocol.setText(R.string.protocolBar);
        }

        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.check_update_button:
                Intent intent = new Intent();
                intent.setClass(AboutActivity.this, ProtocolActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_option_image:

                if (mLeoPopMenu == null) {
                    mLeoPopMenu = new LeoPopMenu();
                    mLeoPopMenu.setPopMenuItems(getPopMenuItems());
                    mLeoPopMenu.setItemSpaned(true);
                    mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
                    mLeoPopMenu.setPopMenuItems(getPopMenuItems());
                    mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
                            if (position == 0) {
                                if (AppUtil.appInstalled(getApplicationContext(),
                                        "com.google.android.apps.plus")) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri
                                            .parse("https://plus.google.com/u/0/communities/112552044334117834440");
                                    intent.setData(uri);
                                    ComponentName cn = new ComponentName(
                                            "com.google.android.apps.plus",
                                            "com.google.android.libraries.social.gateway.GatewayActivity");
                                    intent.setComponent(cn);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    try {
                                        startActivity(intent);
                                    } catch (Exception e) {
                                    }
                                } else {
                                    Uri uri = Uri
                                            .parse("https://plus.google.com/u/0/communities/112552044334117834440");
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }
                            } else if (position == 1) {
                                if (AppUtil.appInstalled(getApplicationContext(),
                                        "com.facebook.katana")) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri
                                            .parse("fb://page/1709302419294051");
                                    intent.setData(uri);
                                    ComponentName cn = new ComponentName("com.facebook.katana",
                                            "com.facebook.katana.IntentUriHandler");
                                    intent.setComponent(cn);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    try {
                                        startActivity(intent);
                                    } catch (Exception e) {
                                    }
                                } else {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri
                                            .parse("https://www.facebook.com/pages/App-Master/1709302419294051");
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                            /**
                             * AM-494
                             */
                            else if(position == 2){     
                                if (AppMasterPreference.getInstance(AboutActivity.this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                                    enterHideVideo();
                                } else {
                                    startVideoLockSetting();
                                }
                            }
                            mLeoPopMenu.dismissSnapshotList();
                        }
                    });
                }
                mLeoPopMenu.showPopMenu(this, mTtileBar.findViewById(R.id.tv_option_image), null,
                        null);
                break;
            default:
                break;
        }

    }
    //*****************************AM-494*********************************
    private void enterHideVideo() {
        Intent intent = null;
        int lockType = AppMasterPreference.getInstance(this).getLockType();
        intent = new Intent(this, LockScreenActivity.class);
        intent.putExtra(LockScreenActivity.EXTRA_LOCK_TITLE,
                getString(R.string.app_image_hide));
        intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
                LockFragment.FROM_SELF_HOME);
        intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
                VideoHideMainActivity.class.getName());
        if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
            intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
                    LockFragment.LOCK_TYPE_PASSWD);
        } else {
            intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
                    LockFragment.LOCK_TYPE_GESTURE);
        }
        startActivity(intent);
    }

    private void startVideoLockSetting() {
        Intent intent = new Intent(this, LockSettingActivity.class);
        intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
                VideoHideMainActivity.class.getName());
        startActivity(intent);
    }
  //**************************************************************
    /**
     * getPopMenuItems
     * 
     * @return
     */
    private List<String> getPopMenuItems() {
        List<String> listItems = new ArrayList<String>();
        Resources resources = AppMasterApplication.getInstance().getResources();
        listItems.add(resources.getString(R.string.about_group));
        listItems.add(resources.getString(R.string.about_praise));
        /**
         * AM-494
         */
        listItems.add("视频隐藏");
        return listItems;
    }
}
