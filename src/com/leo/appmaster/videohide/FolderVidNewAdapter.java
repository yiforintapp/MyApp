package com.leo.appmaster.videohide;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.imagehide.FolderNewAdapter;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.core.ImageDownloader;

/**
 * Created by Jasper on 2015/10/22.
 */
public class FolderVidNewAdapter extends FolderNewAdapter<VideoItemBean> {

    public FolderVidNewAdapter() {
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
            String url = ImageDownloader.Scheme.VIDEOFILE.wrap(getPath(wrapper.items.get(0)));
            mImageLoader.displayImage(url, holder.imageView, FolderNewAdapter.getMediaOptions());
        }

        holder.checkBox.setClickable(false);
        holder.checkBox.setChecked(isGroupChecked(groupPosition));
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


        holder.clickRv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        PrivacyNewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pri_pro_new_video_item, null);

            holder = new PrivacyNewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.pp_video_iv);
            holder.title = (TextView) convertView.findViewById(R.id.pp_video_title_tv);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.pp_video_item_cb);

            convertView.setTag(holder);
        } else {
            holder = (PrivacyNewHolder) convertView.getTag();
        }

        final VideoItemBean info = (VideoItemBean) getChild(groupPosition, childPosition);

        String url = ImageDownloader.Scheme.VIDEOFILE.wrap(info.getPath());
        mImageLoader.displayImage(url, holder.imageView, getMediaOptions());
        holder.title.setText(info.getName());

        if (isChildChecked(groupPosition, childPosition)) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        holder.checkBox.setClickable(false);
        final CheckBox checkBox = holder.checkBox;

        RippleView view = (RippleView) convertView;
        view.setCheckBox(checkBox);

//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View rippleView) {
//                toggle(info);
//                checkBox.setChecked(isChildChecked(info));
//            }
//        });


        mItemViews.put(convertView, info);
        return convertView;
    }

    public void setCheck(View view, boolean childChecked) {
        LeoLog.d("testsetcheck", "son setcheckF");
        RippleView view2 = (RippleView) view;
        CheckBox checkBox = view2.getCheckBox();
        if (checkBox != null) {
            checkBox.setChecked(childChecked);
        }
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
