
package com.leo.appmaster.battery;


import java.util.List;

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
import android.widget.RelativeLayout;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.fragment.PretendAppBeautyFragment;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonToolbar;

public class BatterMainActivity extends BaseFragmentActivity implements OnClickListener {
    private CommonToolbar mCtbMain;
    private RelativeLayout mRlContent;
    private Fragment mFrgmResult;
    private GridView mGvApps;
    private List mListInfos;
    
    private final int APPS_COLUMNS = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_manage);
        initUI();
    }

    private void initUI() {
        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_battery);
        mCtbMain.setToolbarTitle(R.string.app_elec_aca);
        mCtbMain.setToolbarColorResource(R.color.cb);
        mCtbMain.setOptionClickListener(this);
        mCtbMain.setNavigationClickListener(this);
        mCtbMain.setOptionImageResource(R.drawable.setup_icon);
        mCtbMain.setOptionMenuVisible(true);
        mGvApps = (GridView) findViewById(R.id.gv_apps);
        mGvApps.setNumColumns(APPS_COLUMNS);
        mGvApps.setAdapter(new AppsAdapter());
        mRlContent = (RelativeLayout) findViewById(R.id.rl_content);
//        ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
//            @Override
//            public void run() {
//                FragmentManager fm = getSupportFragmentManager();
//                FragmentTransaction transaction = fm.beginTransaction();  
//                transaction.setCustomAnimations(R.anim.anim_down_to_up_long, R.anim.anim_up_to_down_long);
//                mFrgmResult = new PretendAppBeautyFragment();  
//                transaction.replace(mContent.getId(), mFrgmResult);
//                transaction.commit();  
//            }
//        }, 3000);
    }

    @Override
    public void onBackPressed() {
        if (mFrgmResult != null && mFrgmResult.isVisible()) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();  
            transaction.setCustomAnimations(R.anim.anim_down_to_up_long, R.anim.anim_up_to_down_long);
            transaction.remove(mFrgmResult);
            transaction.commit();
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
    }

    @Override
    public void onClick(View v) {
         
    }

    class AppsAdapter extends BaseAdapter {
        View mView;
        LayoutInflater mInflater;
        public AppsAdapter() {
            mInflater = LayoutInflater.from(BatterMainActivity.this);
        }
        
        @Override
        public int getCount() {
            return 12;
        }

        @Override
        public Object getItem(int position) {
            return mView = mInflater.inflate(R.layout.item_battery_app, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mView = mInflater.inflate(R.layout.item_battery_app, null);
        }
    }

}
