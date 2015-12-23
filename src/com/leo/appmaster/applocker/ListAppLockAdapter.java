package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
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

    public ListAppLockAdapter(Context mContext) {
        this.mContext = mContext;
        mList = new ArrayList<AppInfo>();
        layoutInflater = LayoutInflater.from(mContext);
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
        MaterialRippleLayout headView = (MaterialRippleLayout) view;
        if (headView == null) {
            headView = (MaterialRippleLayout) layoutInflater.inflate(R.layout.item_lock_app, null);
        } else {
            headView = (MaterialRippleLayout) view;
        }
        ListLockItem itemView = (ListLockItem) headView.findViewById(R.id.content_item_all);
        if (mFlag.equals("applocklist_activity")) {
            AppInfo info = mList.get(i);
            itemView.setIcon(info.icon);
            itemView.setTitle(info.label);
            itemView.setDescEx(info, info.isLocked);
            itemView.setLockView(info.isLocked);
            itemView.setInfo(info);
            return headView;
        } else {
            AppInfo info = mList.get(i);
            itemView.setIcon(info.icon);
            itemView.setTitle(info.label);
            if (info.topPos > -1) {
                String infoTopPos = makePosRight(info);
                String text = mContext.getString(R.string.lock_app_item_desc_cb, infoTopPos);
                itemView.setText(Html.fromHtml(text));
            } else {
                itemView.setText("");
            }
            itemView.setDefaultRecommendApp(info.isLocked);
            itemView.setInfo(info);
            return headView;
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


    public void setData(ArrayList<AppInfo> resault) {
        mList = resault;
        notifyDataSetChanged();
    }

    public void setFlag(String fromDefaultRecommentActivity) {
        mFlag = fromDefaultRecommentActivity;
    }

}
