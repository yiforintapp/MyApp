package com.leo.appmaster.home;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.ListAppLockAdapter;
import com.leo.appmaster.model.AppItemInfo;

import java.util.List;

/**
 * 新增应用适配器
 * Created by Jasper on 2015/10/15.
 */
public class PrivacyNewAppAdapter extends PrivacyNewAdaper<AppItemInfo> {
    @Override
    public void setList(final List<AppItemInfo> dataList) {
        super.setList(dataList);

        if (dataList == null) {
            return;
        }
        ThreadManager.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < dataList.size(); i++) {
                    AppItemInfo itemInfo = dataList.get(i);
                    if (itemInfo.topPos != -1) {
                        toggle(i);
                    }
                }
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PrivacyNewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pri_pro_new_app_item, null);

            holder = new PrivacyNewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.new_item_img);
            holder.title = (TextView) convertView.findViewById(R.id.new_item_title);
            holder.summary = (TextView) convertView.findViewById(R.id.new_item_summary);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.new_item_cb);

            convertView.setTag(holder);
        } else {
            holder = (PrivacyNewHolder) convertView.getTag();
        }

        final AppItemInfo info = (AppItemInfo) getItem(position);

        if (info.topPos > -1) {
            holder.summary.setVisibility(View.VISIBLE);
            String ceilNum = ListAppLockAdapter.makePosRight(info);
            String text = mContext.getString(R.string.lock_app_item_desc_cb, ceilNum);
            holder.summary.setText(Html.fromHtml(text));
            holder.title.setTextColor(mContext.getResources().getColor(R.color.cb2));
        } else {
            holder.summary.setVisibility(View.GONE);
            holder.title.setTextColor(mContext.getResources().getColor(R.color.c2));
        }
        holder.imageView.setImageDrawable(info.icon);
        holder.title.setText(info.label);

        if (isChecked(position)) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setClickable(false);

//        RippleView view = (RippleView) convertView;
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View rippleView) {
//                toggle(info);
//            }
//        });

        mItemsView.put(convertView, info);
        return convertView;
    }

}
