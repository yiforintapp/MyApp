
package com.leo.appmaster.appsetting;

import java.util.ArrayList;
import java.util.List;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.home.ProtocolActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.utils.AppUtil;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

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
        return listItems;
    }
}
