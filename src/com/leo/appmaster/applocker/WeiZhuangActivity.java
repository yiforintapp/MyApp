
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.model.WeiZhuangInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;

public class WeiZhuangActivity extends BaseActivity implements OnItemClickListener, OnClickListener {
    //这个是没有伪装的选项对应的position
    private final static int noMode = 1;
    private Drawable[] mIcon = new Drawable[5];
    private String[] mName;
    private List<WeiZhuangInfo> mList;
    private WeiZhuangAdapt mAdapt;
    private CommonTitleBar mTtileBar;
    private GridView mGridView;
    private Resources mThemeRes;
    private AppMasterPreference sp_weizhuang;
    private int selected = 1;
    private ImageView weizhuang_ask;
    private View trffic_setting_iv;
    private LinearLayout mWeizhuangHelp;
    private TextView mKnowBt;
    private Animation mGuidAnimation;
    private boolean mIsOpenHelp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weizhuang_girdview);
        init();
        fillData();
    }

    private void init() {
        mTtileBar = (CommonTitleBar) findViewById(R.id.weizhuang_title_bar);
        mTtileBar.setTitle(R.string.title_bar_weizhuang);
        mTtileBar.openBackView();

        weizhuang_ask = (ImageView) findViewById(R.id.weizhuang_ask_iv);
        weizhuang_ask.setVisibility(View.VISIBLE);
        trffic_setting_iv = (View) findViewById(R.id.trffic_setting_iv);
        trffic_setting_iv.setVisibility(View.VISIBLE);
        trffic_setting_iv.setOnClickListener(this);

        mGridView = (GridView) findViewById(R.id.gv_weizhuang);

        sp_weizhuang = AppMasterPreference.getInstance(this);
        mThemeRes = this.getResources();
        selected = sp_weizhuang.getPretendLock();

        mWeizhuangHelp = (LinearLayout) findViewById(R.id.activity_weizhuang_firstin);
        mKnowBt = (TextView) mWeizhuangHelp.findViewById(R.id.bt_go);
        mKnowBt.setOnClickListener(this);
        if (sp_weizhuang.getWeiZhuang()) {
            mWeizhuangHelp.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            mIsOpenHelp = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapt != null) {
            mAdapt.notifyDataSetChanged();
        }
    }

    private void fillData() {
        mName = getResources().getStringArray(R.array.weizhuang_type_num);
        mIcon[0]= mThemeRes.getDrawable(R.drawable.disguise_icon_no);
        mIcon[1] = mThemeRes.getDrawable(R.drawable.disguise_icon_no);
        mIcon[2] = mThemeRes.getDrawable(R.drawable.disguise_iocn_error);
        mIcon[3] = mThemeRes.getDrawable(R.drawable.disguise_icon_call);
        mIcon[4] = mThemeRes.getDrawable(R.drawable.disguise_iocn_finger);

        // list
        mList = new ArrayList<WeiZhuangInfo>();
        int size = mName.length;
        for (int i = 0; i < size; i++) {
            WeiZhuangInfo info = new WeiZhuangInfo();
            info.setAppName(mName[i]);
            info.setIcon(mIcon[i]);
            mList.add(info);
        }

        // set adapter
        mAdapt = new WeiZhuangAdapt(this, mList);
        mGridView.setAdapter(mAdapt);
        mGridView.setOnItemClickListener(this);
    }

    /**
     * adapter
     * 
     * @author hqili
     */
    class WeiZhuangAdapt extends BaseAdapter {
        Context context;
        List<WeiZhuangInfo> list;

        public WeiZhuangAdapt(Context context, List<WeiZhuangInfo> mList) {
            this.context = context;
            this.list = mList;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WeizhuangHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.item_weizhuang_gridview, null);
                viewHolder = new WeizhuangHolder();
                viewHolder.iv_icon = (ImageView) convertView
                        .findViewById(R.id.item_icon);
                viewHolder.iv_selected = (ImageView) convertView
                        .findViewById(R.id.item_selected);
                viewHolder.tv_name = (TextView) convertView
                        .findViewById(R.id.item_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (WeizhuangHolder) convertView.getTag();
            }

            selected = sp_weizhuang.getPretendLock();
            // who selected
            if (position == selected) {
                viewHolder.iv_selected.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_selected.setVisibility(View.GONE);
            }

            WeiZhuangInfo mInfo = list.get(position);
            viewHolder.iv_icon.setImageDrawable(mInfo.getIcon());
            viewHolder.tv_name.setText(mInfo.getAppName());

            return convertView;
        }

    }

    private static class WeizhuangHolder {
        private ImageView iv_icon, iv_selected;
        private TextView tv_name;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                if (selected != 0) {
                    // 美女
                    Intent mIntent = new Intent(this, BeautyWeiZhuang.class);
                    this.startActivity(mIntent);
                }
                break;
            case 1:
                if (selected != 1) {
                    // 无
                    sp_weizhuang.setPretendLock(noMode);
                    mAdapt.notifyDataSetChanged();
                }
                break;
            case 2:
                if (selected != 2) {
                    // 应用错误
                    Intent mIntent = new Intent(this, ErrorWeiZhuang.class);
                    this.startActivity(mIntent);
                }
                break;
            case 3:
                if (selected != 3) {
                    // 未知来电
                    // Intent intent = new
                    // Intent(this,UnknowCallActivity.class);
                    // this.startActivity(intent);
                    Intent intent = new Intent(this, UnKnowCallActivity5.class);
                    this.startActivity(intent);
                }
                break;
            case 4:
                if (selected != 4) {
                    // 指纹解锁
                    Intent zhiWenIntent = new Intent(this, ZhiWenActivity.class);
                    this.startActivity(zhiWenIntent);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trffic_setting_iv:
                /* SDK Event Mark */
                SDKWrapper.addEvent(WeiZhuangActivity.this, SDKWrapper.P1, "help", "cover");
                mGuidAnimation = AnimationUtils.loadAnimation(WeiZhuangActivity.this,
                        R.anim.lock_mode_guide_in);
                if (mIsOpenHelp) {
                    mWeizhuangHelp.setVisibility(View.GONE);
                    mGridView.setVisibility(View.VISIBLE);
                    mWeizhuangHelp.startAnimation(AnimationUtils
                            .loadAnimation(WeiZhuangActivity.this, R.anim.lock_mode_guide_out));
                    mIsOpenHelp = false;
                } else {
                    mGridView.setVisibility(View.GONE);
                    mWeizhuangHelp.setVisibility(View.VISIBLE);
                    mWeizhuangHelp.startAnimation(mGuidAnimation);
                    mIsOpenHelp = true;
                }
                if (sp_weizhuang.getWeiZhuang()) {
                    sp_weizhuang.setWeiZhuang(false);
                }
                break;
            case R.id.bt_go:
                mWeizhuangHelp.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
                mGuidAnimation = AnimationUtils
                        .loadAnimation(WeiZhuangActivity.this, R.anim.lock_mode_guide_out);
                mWeizhuangHelp.startAnimation(mGuidAnimation);
                Animation animation =
                        AnimationUtils.loadAnimation(WeiZhuangActivity.this,
                                R.anim.help_tip_show);
                weizhuang_ask.startAnimation(animation);
                if (sp_weizhuang.getWeiZhuang()) {
                    sp_weizhuang.setWeiZhuang(false);
                }
                mIsOpenHelp = false;
                break;
            default:
                break;
        }
    }
}
