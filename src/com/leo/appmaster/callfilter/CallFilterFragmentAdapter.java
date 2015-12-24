package com.leo.appmaster.callfilter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOWithSingleCheckboxDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qili on 15-10-10.
 */
public class CallFilterFragmentAdapter extends BaseAdapter {
    private List<CallFilterInfo> mList;
    private String mFlag;
    private Context mContext;
    private LayoutInflater layoutInflater;

    public CallFilterFragmentAdapter(Context mContext) {
        this.mContext = mContext;
        mList = new ArrayList<CallFilterInfo>();
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
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
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        BlackListHolder holder;
        if (convertView == null) {

            convertView = layoutInflater.inflate(R.layout.fragment_call_filter_item, null);

            holder = new BlackListHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.desc = (TextView) convertView.findViewById(R.id.tv_desc);

            convertView.setTag(holder);
        } else {
            holder = (BlackListHolder) convertView.getTag();
        }

        RippleView view = (RippleView) convertView;
        view.setNeedLongClick(true);

        CallFilterInfo info = mList.get(i);
        String numberName = info.numberName;
        String number = info.number;
        int filterType = info.filterType;

        if (Utilities.isEmpty(numberName) || numberName.equals(number)) {
            if (filterType == 0) {
                holder.title.setText(number);
                holder.desc.setText(number);
            } else {
                String string;
                if (filterType == 1) {
                    string = mContext.getString(R.string.filter_number_type_saorao);
                } else if (filterType == 2) {
                    string = mContext.getString(R.string.filter_number_type_ad);
                } else {
                    string = mContext.getString(R.string.filter_number_type_zhapian);
                }
                holder.title.setText(string);
                holder.desc.setText(number);
            }
        } else {
            holder.title.setText(numberName);
            holder.desc.setText(number);
        }

        return convertView;
    }


    public static class BlackListHolder {
        ImageView imageView;
        TextView title;
        TextView desc;
    }

    public void setData(List<CallFilterInfo> infoList) {
        mList = infoList;
        if (mList.size() < 1) {
            CallFilterMainActivity callFilterMainActivity =
                    (CallFilterMainActivity) mContext;
            callFilterMainActivity.callFilterShowEmpty();
        }
        notifyDataSetChanged();
    }

    public void setFlag(String fromWhere) {
        mFlag = fromWhere;
    }


}
