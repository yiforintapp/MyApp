
package com.leo.appmaster.appmanage;

import java.util.List;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.engine.BatteryInfoProvider;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.LeoLog;

public class EleActivity extends BaseFragmentActivity {
    private CommonTitleBar mTtileBar;
    private ListView listview_ele;
    private customAdapter adapter;
    private BatteryInfoProvider info;
    private List<BatteryComsuption> mList;
    private ProgressBar pb_loading_ele;
    private static final String Tag = "testCase";
    private ActivityManager am;
    private static final String SCHEME = "package";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_manager_elec);
        initUI();
    }

    private void initUI() {
        mTtileBar = (CommonTitleBar) findViewById(R.id.ele_title_bar);
        mTtileBar.setTitle(R.string.app_elec_aca);
        mTtileBar.openBackView();

        pb_loading_ele = (ProgressBar) findViewById(R.id.pb_loading_ele);
        listview_ele = (ListView) findViewById(R.id.listview_ele);
        adapter = new customAdapter();
        listview_ele.setAdapter(adapter);

        info = new BatteryInfoProvider(EleActivity.this);
        info.setMinPercentOfTotal(0.01);

        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        getBatteryStats();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    private void getBatteryStats() {
        new Thread() {
            public void run() {
                mList = info.getBatteryStats();
                mHandler.sendEmptyMessage(1);
            }
        }.start();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    if (EleActivity.this.isFinishing())
                        return;
                    pb_loading_ele.setVisibility(View.GONE);
                    listview_ele.setVisibility(View.VISIBLE);

                    // for(BatteryComsuption bif : mList){
                    // LeoLog.d("testfuckele", bif.getDefaultPackageName());
                    // }

                    adapter.setData(mList);
                    break;
            }
        }
    };

    class customAdapter extends BaseAdapter implements OnClickListener {
        private List<BatteryComsuption> list;
        private LayoutInflater inflater;

        public customAdapter() {
            inflater = LayoutInflater.from(EleActivity.this);
        }

        public void setData(List<BatteryComsuption> list) {
            this.list = list;

            for (int i = list.size() - 1; i >= 0; i--) {

                String packageName = list.get(i).getDefaultPackageName();
                if (null != packageName) {
                    if (packageName.equals(EleActivity.this.getPackageName())) {
                        list.remove(i);
                        continue;
                    }
                }

                final BatteryComsuption sipper = list.get(i);
                String name = sipper.getName();
                if (name == null) {
                    LeoLog.d(Tag, "name == null! name is :" + name);
                    Drawable icon = sipper.getIcon();
                    switch (sipper.getDrainType()) {
                        case CELL:
                            name = getString(R.string.power_cell);
                            icon =
                                    getResources().getDrawable(R.drawable.ic_settings_cell_standby);
                            break;
                        case IDLE:
                            name = getString(R.string.power_idle);
                            icon = getResources().getDrawable(R.drawable.ic_settings_phone_idle);
                            break;
                        case BLUETOOTH:
                            name = getString(R.string.power_bluetooth);
                            icon = getResources().getDrawable(R.drawable.ic_settings_bluetooth);
                            break;
                        case WIFI:
                            name = getString(R.string.power_wifi);
                            icon = getResources().getDrawable(R.drawable.ic_settings_wifi);
                            break;
                        case SCREEN:
                            name = getString(R.string.power_screen);
                            icon = getResources().getDrawable(R.drawable.ic_settings_display);
                            break;
                        case PHONE:
                            name = getString(R.string.power_phone);
                            icon =
                                    getResources().getDrawable(R.drawable.ic_settings_voice_calls);
                            break;
                        case KERNEL:
                            name = getString(R.string.process_kernel_label);
                            icon = getResources().getDrawable(R.drawable.ic_power_system);
                            break;
                        case MEDIASERVER:
                            name = getString(R.string.process_mediaserver_label);
                            icon = getResources().getDrawable(R.drawable.ic_power_system);
                            break;
                        default:
                            break;
                    }

                    if (name != null) {
                        sipper.setName(name);
                        if (icon == null) {
                            PackageManager pm = EleActivity.this.getPackageManager();
                            icon = pm.getDefaultActivityIcon();
                        }
                        sipper.setIcon(icon);
                    } else {
                        list.remove(i);
                    }
                }
            }
            // notifyDataSetInvalidated();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public BatteryComsuption getItem(int position) {
            return list == null ? null : list.get(position);
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
                convertView = inflater.inflate(R.layout.manager_elec_list_item, null);
                holder.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
                holder.appName = (TextView) convertView.findViewById(R.id.appName);
                holder.txtProgress = (TextView) convertView.findViewById(R.id.txtProgress);
                holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                holder.stopView = (ImageView) convertView.findViewById(R.id.ele_stop_app);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            BatteryComsuption sipper = getItem(position);
            holder.appName.setText(sipper.getName());
            holder.appIcon.setImageDrawable(sipper.getIcon());

            double percentOfTotal = sipper.getPercentOfTotal();
            if (percentOfTotal <= 1) {
                holder.txtProgress.setText(format(1));
                holder.progress.setProgress((int) 1);
            } else {
                holder.txtProgress.setText(format(percentOfTotal));
                holder.progress.setProgress((int) percentOfTotal);
            }

            holder.stopView.setTag("" + position);
            holder.stopView.setOnClickListener(this);

            return convertView;
        }

        @Override
        public void onClick(View v) {
            int index = Integer.parseInt(v.getTag().toString());
            BatteryComsuption abc = mList.get(index);
            String packageName = abc.getDefaultPackageName();
            LeoLog.d("Eleactivity", "OnClick咯！！");
            LockManager.getInstatnce().timeFilterSelf();
            showInstalledAppDetails(packageName);
        }
    }

    class Holder {
        ImageView appIcon;
        TextView appName;
        TextView txtProgress;
        ProgressBar progress;
        ImageView stopView;
    }

    private String format(double size) {
        return String.format("%1$.2f%%", size);
    }

    protected void showInstalledAppDetails(String packageName) {
        try {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "batterypage", "batterystop");
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
            // LeoLog.d("Eleactivity", "TRY里面，准备STARTACTIVITY");
            startActivity(intent);
            // LeoLog.d("Eleactivity", "发射！！");
        } catch (Exception e) {
            // LeoLog.d("Eleactivity", "报错咯！！");
            Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            ResolveInfo resolveInfo = getPackageManager().resolveActivity(powerUsageIntent, 0);
            // check that the Battery app exists on this device
            if (resolveInfo != null) {
                startActivity(powerUsageIntent);
            } else {
                Toast.makeText(this, R.string.battery_cannot_do, 0).show();
            }
        }

    }

    protected void kill(int position) {
        BatteryComsuption abc = mList.get(position);
        String packageName = abc.getDefaultPackageName();
        am.killBackgroundProcesses(packageName);
        mList.remove(position);
        adapter.setData(mList);
    }

}
