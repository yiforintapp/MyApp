package com.leo.appmaster.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.videohide.VideoItemBean;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Jasper on 2015/10/22.
 */
public class FolderVidAdapter extends FolderAdapter<VideoItemBean> {

    public FolderVidAdapter() {
        super();
    }

    @Override
    protected void initGroupIndexArray() {
        mGroupIndexArray.clear();
        int index = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            ItemsWrapper wrapper = mDataList.get(i);
            mGroupIndexArray.put(i, index);
            index += wrapper.items.size() + 1;
        }
    }

    @Override
    protected String getPath(VideoItemBean data) {
        if (data == null) return null;

        return data.getPath();
    }

    @Override
    protected int getChildTagId() {
        return R.layout.pri_pro_new_video_item;
    }

    @Override
    protected int getGroupTagId() {
        return R.layout.pri_pro_list_sticky_header;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
        FoldHolder holder = null;
        if (!isExpanded) {
            if (convertView == null || convertView.getTag(R.layout.pri_new_folder_item) == null) {
                convertView = mInflater.inflate(R.layout.pri_new_folder_item, null);

                holder = new FoldHolder();
                holder.count = (TextView) convertView.findViewById(R.id.pp_folder_num);
                holder.imageView = (ImageView) convertView.findViewById(R.id.pp_folder_img);

                holder.title = (TextView) convertView.findViewById(R.id.pri_pro_new_label_tv);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.pri_pro_cb);
                holder.clickRv = (RippleView) convertView.findViewById(R.id.pri_pro_click_rv);
                holder.arrow = (ImageView) convertView.findViewById(R.id.pri_pro_label_arrow_iv);
                convertView.setTag(R.layout.pri_new_folder_item, holder);
            } else {
                holder = (FoldHolder) convertView.getTag(R.layout.pri_new_folder_item);
            }
        } else {
            if (convertView == null || convertView.getTag(R.layout.pri_pro_list_sticky_header) == null) {
                convertView = mInflater.inflate(R.layout.pri_pro_list_sticky_header, null);

                holder = new FoldHolder();
                holder.title = (TextView) convertView.findViewById(R.id.pri_pro_new_label_tv);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.pri_pro_cb);
                holder.clickRv = (RippleView) convertView.findViewById(R.id.pri_pro_click_rv);
                holder.arrow = (ImageView) convertView.findViewById(R.id.pri_pro_label_arrow_iv);

                convertView.setTag(R.layout.pri_pro_list_sticky_header, holder);
            } else {
                holder = (FoldHolder) convertView.getTag(R.layout.pri_pro_list_sticky_header);
            }
        }

        final ItemsWrapper<VideoItemBean> wrapper = (ItemsWrapper) getGroup(groupPosition);
        if (isExpanded) {
            setLableContent(holder.title, wrapper.parentName, wrapper.items.size());
        } else {
            holder.title.setText(wrapper.parentName);
            holder.count.setText(mContext.getString(R.string.pri_pro_folder_summary, wrapper.items.size()));
            String url = "voidefile://" + getPath(wrapper.items.get(0));
            mImageLoader.displayImage(url, holder.imageView, PrivacyNewAdaper.getOptions());
        }

        holder.checkBox.setClickable(false);
        holder.checkBox.setChecked(isGroupChecked(wrapper));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                if (checkBox.isChecked()) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "process", "vid_folder_full_" + wrapper.parentName);
                    selectAll(groupPosition);
                } else {
                    deselectAll(groupPosition);
                }
            }
        });
        holder.clickRv.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onRippleComplete(RippleView rippleView) {
                if (mListener != null) {
                    mListener.onGroupClick(groupPosition, isExpanded);
                }
            }
        });
        if (isExpanded) {
            mWrapperViews.put(convertView, wrapper);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        PrivacyNewAdaper.PrivacyNewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pri_pro_new_video_item, null);

            holder = new PrivacyNewAdaper.PrivacyNewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.pp_video_iv);
            holder.title = (TextView) convertView.findViewById(R.id.pp_video_title_tv);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.pp_video_item_cb);

            convertView.setTag(holder);
        } else {
            holder = (PrivacyNewAdaper.PrivacyNewHolder) convertView.getTag();
        }

        final VideoItemBean info = (VideoItemBean) getChild(groupPosition, childPosition);

        String url = "voidefile://" + info.getPath();
        mImageLoader.displayImage(url, holder.imageView, getMediaOptions());
        holder.title.setText(info.getName());

        if (isChildChecked(info)) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        holder.checkBox.setClickable(false);
        final CheckBox checkBox = holder.checkBox;
        RippleView view = (RippleView) convertView;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View rippleView) {
                toggle(info);
                checkBox.setChecked(isChildChecked(info));
            }
        });
        mItemViews.put(convertView, info);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public Object getFirstVisibleGroup(int firstVisibleItem) {
        int group = getFirstVisibleGroupPosition(firstVisibleItem);
        if (group >= 0 && group < getGroupCount()) {
            return getGroup(group);
        }
        return null;
    }

    @Override
    public int getFirstVisibleGroupPosition(int firstVisibleItem) {
        int index = 0;
        for (int i = 0; i < getGroupCount(); i++) {
            int rowCount = getChildrenCount(i);
            index += rowCount + 1;
            if (firstVisibleItem <= index) {
                return i;
            }
        }
        return 0;
    }
}
