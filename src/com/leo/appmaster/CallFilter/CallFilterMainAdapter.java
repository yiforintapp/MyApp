package com.leo.appmaster.CallFilter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.ListLockItem;
import com.leo.appmaster.ui.MaterialRippleLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qili on 15-10-10.
 */
public class CallFilterMainAdapter extends BaseAdapter {
    private List<CallFilterInfo> mList;
    private String mFlag;
    private Context mContext;
    private LayoutInflater layoutInflater;

    public CallFilterMainAdapter(Context mContext) {
        this.mContext = mContext;
        mList = new ArrayList<CallFilterInfo>();
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
            headView = (MaterialRippleLayout)
                    layoutInflater.inflate(R.layout.call_filter_list_item, null);
        } else {
            headView = (MaterialRippleLayout) view;
        }
        ListLockItem itemView = (ListLockItem) headView.findViewById(R.id.content_item_all);
        if (mFlag.equals("applocklist_activity")) {
            CallFilterInfo info = mList.get(i);
//            itemView.setIcon(info.icon);
//            itemView.setTitle(info.label);
//            itemView.setDescEx(info, info.isLocked);
//            itemView.setLockView(info.isLocked);
//            itemView.setInfo(info);
            return headView;
        } else {
            CallFilterInfo info = mList.get(i);
//            itemView.setIcon(info.icon);
//            itemView.setTitle(info.label);
//            itemView.setDefaultRecommendApp(info.isLocked);
//            itemView.setInfo(info);
            return headView;
        }
    }

    public void setData(ArrayList<CallFilterInfo> infoList) {
        mList = infoList;
        notifyDataSetChanged();
    }

    public void setFlag(String fromWhere) {
        mFlag = fromWhere;
    }

}
