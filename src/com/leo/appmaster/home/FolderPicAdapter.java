package com.leo.appmaster.home;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaskImageView;
import com.leo.appmaster.ui.RippleView;

/**
 * Created by Jasper on 2015/10/30.
 */
public class FolderPicAdapter extends FolderAdapter<PhotoItem> {
    private static final String TAG = "FolderPicAdapter";

    public FolderPicAdapter() {
        super();
    }

    @Override
    protected void initGroupIndexArray() {
        mGroupIndexArray.clear();
        int index = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            ItemsWrapper wrapper = mDataList.get(i);
            mGroupIndexArray.put(i, index);

            int remind = wrapper.items.size() % 3;
            index += wrapper.items.size() / 3 + (remind > 0 ? 1 : 0) + 1;
        }
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
        mExpanded = isExpanded;
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
                holder = new FoldHolder();
                convertView = mInflater.inflate(R.layout.pri_pro_list_sticky_header, null);

                holder.title = (TextView) convertView.findViewById(R.id.pri_pro_new_label_tv);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.pri_pro_cb);
                holder.clickRv = (RippleView) convertView.findViewById(R.id.pri_pro_click_rv);
                holder.arrow = (ImageView) convertView.findViewById(R.id.pri_pro_label_arrow_iv);

                convertView.setTag(R.layout.pri_pro_list_sticky_header, holder);
            } else {
                holder = (FoldHolder) convertView.getTag(R.layout.pri_pro_list_sticky_header);
            }

        }

        final ItemsWrapper<PhotoItem> wrapper = (ItemsWrapper) getGroup(groupPosition);
        if (isExpanded) {
            setLableContent(holder.title, wrapper.parentName, wrapper.items.size());
        } else {
            holder.title.setText(wrapper.parentName);
            holder.count.setText(mContext.getString(R.string.pri_pro_folder_summary, wrapper.items.size()));
            String url = "file://" + getPath(wrapper.items.get(0));
            mImageLoader.displayImage(url, holder.imageView, PrivacyNewAdaper.getOptions());
        }

        holder.checkBox.setChecked(isGroupChecked(wrapper));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                if (checkBox.isChecked()) {
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "process", "pic_folder_full_" + wrapper.parentName);
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
            convertView = mInflater.inflate(R.layout.pri_pro_new_pic_item, null);

            holder = new PrivacyNewAdaper.PrivacyNewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.pp_pic_iv);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.pp_pic_item_cb);

            convertView.setTag(R.layout.pri_pro_new_pic_item, holder);
        } else {
            holder = (PrivacyNewAdaper.PrivacyNewHolder) convertView.getTag(R.layout.pri_pro_new_pic_item);
        }

        final PhotoItem item = (PhotoItem) getChild(groupPosition, childPosition);
        String url = "file://" + getPath(item);

        mImageLoader.displayImage(url, holder.imageView, getMediaOptions());
        holder.checkBox.setClickable(false);
        boolean isChecked = isChildChecked(item);
        holder.checkBox.setChecked(isChecked);
        if (holder.imageView instanceof MaskImageView) {
            ((MaskImageView) holder.imageView).setChecked(isChecked);
        }

        final CheckBox checkBox = holder.checkBox;
        final ImageView imageView = holder.imageView;
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(item);
                boolean isChecked = isChildChecked(item);
                checkBox.setChecked(isChecked);
                if (imageView instanceof MaskImageView) {
                    ((MaskImageView) imageView).setChecked(isChecked);
                }
            }
        });
        mItemViews.put(convertView, item);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    protected String getPath(PhotoItem data) {
        if (data == null) {
            return null;
        }

        return data.getPath();
    }

    @Override
    protected int getChildTagId() {
        return R.layout.pri_pro_new_pic_item;
    }

    @Override
    protected int getGroupTagId() {
        return R.layout.pri_pro_list_sticky_header;
    }

    @Override
    public int getFirstVisibleGroupPosition(int firstVisibleItem) {
        int index = 0;
        for (int i = 0; i < getGroupCount(); i++) {
            int rowCount = getChildrenCount(i) / 3;
            rowCount += getChildrenCount(i) % 3 != 0 ? 1 : 0;
            index += rowCount + 1;
            if (firstVisibleItem <= index) {
                return i;
            }
        }

        return 0;
    }

    @Override
    public Object getFirstVisibleGroup(int firstVisibleItem) {
        int group = getFirstVisibleGroupPosition(firstVisibleItem);
        if (group >= 0 && group < getGroupCount()) {
            return getGroup(group);
        }
        return null;
    }
}
