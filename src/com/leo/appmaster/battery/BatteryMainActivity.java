
package com.leo.appmaster.battery;


import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.fragment.PretendAppBeautyFragment;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;

public class BatteryMainActivity extends BaseFragmentActivity implements OnClickListener {
    private final String TAG = "BatterMainActivity";
    private boolean DBG = true;
    private CommonToolbar mCtbMain;
    private RelativeLayout mRlContent;
    private Fragment mFrgmResult;
    private GridView mGvApps;
    private ArrayList<BatteryComsuption> mListBatteryComsuptions;
    private RippleView mRvBoost;
    private final int APPS_COLUMNS = 5;
    private AppsAdapter mAdapter;
    private ProgressBar mPbLoading;
    private RelativeLayout mRlEmpty;
    private RelativeLayout mRlLoadingOrEmpty;
    private BatteryManager mBtrManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_manage);
        mBtrManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
        initUI();
    }

    
    private void initUI() {
        mRlLoadingOrEmpty = (RelativeLayout) findViewById(R.id.rl_empty_or_loading);
        mPbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        mRlEmpty = (RelativeLayout) findViewById(R.id.rl_empty);
        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_battery);
        mCtbMain.setToolbarTitle(R.string.app_elec_aca);
        mCtbMain.setToolbarColorResource(R.color.cb);
        mCtbMain.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BatteryMainActivity.this, BatterySettingActivity.class);
                startActivity(intent);
            }
        });
        mCtbMain.setOptionImageResource(R.drawable.setup_icon);
        mCtbMain.setOptionMenuVisible(true);
        mGvApps = (GridView) findViewById(R.id.gv_apps);
        mGvApps.setNumColumns(APPS_COLUMNS);
        mAdapter = new AppsAdapter();
        mRlContent = (RelativeLayout) findViewById(R.id.rl_content);
        mRvBoost = (RippleView) findViewById(R.id.rv_accelerate);
        mRvBoost.setOnClickListener(this);
        mGvApps.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mFrgmResult != null && mFrgmResult.isVisible()) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();  
            transaction.setCustomAnimations(R.anim.anim_down_to_up_long, R.anim.anim_up_to_down_long);
            transaction.remove(mFrgmResult);
            transaction.commit();
            mRlLoadingOrEmpty.setVisibility(View.VISIBLE);
            mPbLoading.setVisibility(View.VISIBLE);
            mRlEmpty.setVisibility(View.GONE);
            loadData();
        } else {
            super.onBackPressed();
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mBtrManager.updateBatteryPageState(true);
        LeoLog.i(TAG, "onResume");
        if (mFrgmResult != null && mFrgmResult.isVisible()) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();  
            transaction.setCustomAnimations(R.anim.anim_down_to_up_long, R.anim.anim_up_to_down_long);
            transaction.remove(mFrgmResult);
            transaction.commit();
        }
        mRlLoadingOrEmpty.setVisibility(View.VISIBLE);
        mPbLoading.setVisibility(View.VISIBLE);
        mRlEmpty.setVisibility(View.GONE);
        loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBtrManager.updateBatteryPageState(false);
    }

    private void loadData() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mListBatteryComsuptions = (ArrayList<BatteryComsuption>) mBtrManager.getBatteryDrainApps();
                runOnUiThread(new Runnable() {
                    public void run() {
                        onDataLoaded();
                    }
                });
            }
        });
    }

    private void onDataLoaded() {
        mAdapter.fillData(mListBatteryComsuptions);
        if (mListBatteryComsuptions == null || mListBatteryComsuptions.size() == 0) {
            mPbLoading.setVisibility(View.GONE);
            mRlLoadingOrEmpty.setVisibility(View.VISIBLE);
        } else {
            mRlLoadingOrEmpty.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }
    
    
    @Override
    public void onClick(View v) {
         switch (v.getId()) {
            case R.id.rv_accelerate:
                startBoost();
                break;
            default:
                break;
        }
    }

    private void startBoost() {
        mBtrManager.killBatteryDrainApps();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.anim.anim_down_to_up_long, R.anim.anim_up_to_down_long);
        mFrgmResult = new PretendAppBeautyFragment();
        transaction.replace(mRlContent.getId(), mFrgmResult);
        transaction.commit();
    }

    class AppsAdapter extends BaseAdapter {
        private List<BatteryComsuption> mList;
        LayoutInflater mInflater;
        
        public void fillData(ArrayList<BatteryComsuption> list) {
            mList = list;
        }

        public AppsAdapter() {
            mInflater = LayoutInflater.from(BatteryMainActivity.this);
        }
        
        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Object getItem(int position) {
            return getView(position, null, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                holder = new Holder();
                convertView = mInflater.inflate(R.layout.item_battery_app, null);
                holder.iv_appicon = (ImageView) convertView.findViewById(R.id.iv_app);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            if (mList != null && mList.size() != 0) {
                holder.iv_appicon.setImageDrawable(mList.get(position).getIcon());
            }
            return convertView;
        }
    }

    class Holder {
        ImageView iv_appicon;
    }
}
