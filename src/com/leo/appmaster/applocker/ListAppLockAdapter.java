package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.lockswitch.BlueToothLockSwitch;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.lockswitch.WifiLockSwitch;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.ui.MaterialRippleLayout;

/**
 * Created by qili on 15-10-10.
 */
public class ListAppLockAdapter extends BaseAdapter {
    private List<AppInfo> mList;
    private String mFlag;
    private Context mContext;
    private LayoutInflater layoutInflater;
    private LockManager mLockManager;

    public ListAppLockAdapter(Context mContext) {
        this.mContext = mContext;
        mList = new ArrayList<AppInfo>();
        layoutInflater = LayoutInflater.from(mContext);
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        AppInfo info = mList.get(i);
        if (info.label.equals(Constants.LABLE_LIST)) {
            view = layoutInflater.inflate(R.layout.home_more_label_item, null);
            TextView textView = (TextView) view.findViewById(R.id.more_label_tv);
            String text;
            if (i == 0) {
                text = mContext.getString(R.string.app_lock_list_switch_title_one);
            } else {
                text = mContext.getString(R.string.app_lock_list_switch_title_two);
            }
            textView.setText(text);
            return view;
        } else {
            view = layoutInflater.inflate(R.layout.item_lock_app, null);
            ListLockItem itemView = (ListLockItem) view.findViewById(R.id.content_item_all);
            if (mFlag.equals(AppLockListActivity.FROM_DEFAULT_RECOMMENT_ACTIVITY)) {
                itemView.setIcon(info.icon);
                itemView.setTitle(info.label);
                itemView.setDescEx(info, info.isLocked);
                itemView.setLockView(info.isLocked);
                itemView.setInfo(info);
                return view;
            } else {
                itemView.setIcon(info.icon);
                itemView.setTitle(info.label);
                if (info.topPos > -1) {
                    String infoTopPos = makePosRight(info);
                    String text = mContext.getString(R.string.lock_app_item_desc_cb_color, infoTopPos);
                    itemView.setText(Html.fromHtml(text));
                } else {
                    itemView.setText("");
                }
                itemView.setDefaultRecommendApp(info.isLocked);
                itemView.setInfo(info);
                return view;
            }
        }
    }

    public static String makePosRight(AppInfo info) {
        int topPos = info.topPos;
        String tops;
        if (topPos > 0 && topPos < 5000) {
            tops = "1000+";
        } else if (topPos >= 5000 && topPos < 10000) {
            tops = "5000+";
        } else if (topPos >= 10000 && topPos < 50000) {
            tops = "10000+";
        } else if (topPos >= 50000 && topPos < 100000) {
            tops = "50000+";
        } else if (topPos >= 100000 && topPos < 500000) {
            tops = "100000+";
        } else if (topPos >= 100000 && topPos < 500000) {
            tops = "100000+";
        } else if (topPos >= 500000 && topPos < 1000000) {
            tops = "500000+";
        } else if (topPos >= 1000000 && topPos < 5000000) {
            tops = "1000000+";
        } else if (topPos >= 5000000 && topPos < 10000000) {
            tops = "5000000+";
        } else {
            tops = "10000000+";
        }
        return tops;
    }

    public static int fixPosEqules(AppInfo info) {
        int topPosGet = info.topPos;
        String pckName = info.packageName;

        String[] strings = AppLoadEngine.sLocalLockArray;
        int k = 0;
        boolean isHavePckName = false;
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string.equals(pckName)) {
                k = i;
                isHavePckName = true;
                break;
            }
        }

        if (isHavePckName) {
            String[] nums = AppLoadEngine.sLocalLockNumArray;
            int num = Integer.parseInt(nums[k]);
            if (num > topPosGet) {
                return num;
            } else {
                return topPosGet;
            }
        } else {
            if (topPosGet != -1) {
                if (topPosGet == 0) {
                    return 1000;
                } else {
                    return topPosGet;
                }
            } else {
                return topPosGet;
            }
        }
    }


    private Boolean mIsNewMode;
    private LockMode mMode;

    public void setMode(LockMode mode, boolean isNewMode) {
        if (mode == null) {
            mMode = mLockManager.getCurLockMode();
        } else {
            mMode = mode;
        }
        mIsNewMode = isNewMode;
    }

    List<AppInfo> switchList;

    public void setData(ArrayList<AppInfo> resault) {
        mList.clear();
        switchList = getSwitchList(true);
        mList.addAll(switchList);
        mList.addAll(resault);
        notifyDataSetChanged();
    }

    public List<AppInfo> getSwitchs() {
        if (switchList != null) {
            return switchList;
        } else {
            return null;
        }
    }

    public void setFlag(String fromDefaultRecommentActivity) {
        mFlag = fromDefaultRecommentActivity;
    }

    public List<AppInfo> getSwitchList(boolean isNeedLabel) {
        List<AppInfo> switchList = new ArrayList<AppInfo>();
        WifiLockSwitch wifiSwitch = new WifiLockSwitch();
        BlueToothLockSwitch blueToothSwitch = new BlueToothLockSwitch();

        if (isNeedLabel) {
            AppInfo labelInfo = new AppInfo();
            labelInfo.label = Constants.LABLE_LIST;
            switchList.add(labelInfo);
        }

        AppInfo wifiInfo = new AppInfo();
        wifiInfo.label = mContext.getString(R.string.app_lock_list_switch_wifi);
        wifiInfo.packageName = SwitchGroup.WIFI_SWITCH;
        wifiInfo.icon = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_wifi);
        if (mIsNewMode) {
            wifiInfo.isLocked = false;
        } else {
            wifiInfo.isLocked = wifiSwitch.isLockNow(mMode);

        }
        wifiInfo.topPos = wifiSwitch.getLockNum();
        switchList.add(wifiInfo);

        AppInfo bluetoothInfo = new AppInfo();
        bluetoothInfo.label = mContext.getString(R.string.app_lock_list_switch_bluetooth);
        bluetoothInfo.packageName = SwitchGroup.BLUE_TOOTH_SWITCH;
        bluetoothInfo.icon = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_bluetooth);
        if (mIsNewMode) {
            bluetoothInfo.isLocked = false;
        } else {
            bluetoothInfo.isLocked = blueToothSwitch.isLockNow(mMode);
        }

        bluetoothInfo.topPos = blueToothSwitch.getLockNum();
        switchList.add(bluetoothInfo);

        if (isNeedLabel) {
            AppInfo labelInfo2 = new AppInfo();
            labelInfo2.label = Constants.LABLE_LIST;
            switchList.add(labelInfo2);
        }

        return switchList;
    }
}
