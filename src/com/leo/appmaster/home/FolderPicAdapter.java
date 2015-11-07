package com.leo.appmaster.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import com.leo.appmaster.home.PrivacyNewAdaper.PrivacyNewHolder;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaskImageView;
import com.leo.appmaster.ui.RippleView;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

/**
 * Created by Jasper on 2015/10/30.
 */
public class FolderPicAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "FolderPicAdapter";

    private static final int GROUP_EXPANDED = 1;

    public static interface OnPicFolderClickListener {
        public void onGroupClick(int groupPosition, boolean isExpanded);

        public void onChildClick(int groupPosition, int childPosition);

        public void onListScroll(int groupPosition, int childPosition);

        public void onGroupCheckChanged(int groupPosition, boolean checked);

        public void onSelectionChange(boolean selectAll, int selectedCount);
    }

    private OnPicFolderClickListener mListener;

    private List<PhotoItem> mSrcList;
    private List<PhotoItemsWrapper> mDataList;

    private LayoutInflater mInflater;
    private Context mContext;
    private List<PhotoItem> mSelectData;

    private ImageLoader mImageLoader;
    private HashMap<View, PhotoItemsWrapper> mWrapperViews;
    private HashMap<View, PhotoItem> mItemViews;

    private boolean mExpanded;

    private HashMap<Integer, View> mGroupViews;
    public FolderPicAdapter() {
        mSrcList = new ArrayList<PhotoItem>();
        mDataList = new ArrayList<PhotoItemsWrapper>();
        mSelectData = new ArrayList<PhotoItem>();
        mGroupViews = new HashMap<Integer, View>();

        mContext = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(mContext);
        mImageLoader = ImageLoader.getInstance();

        mWrapperViews = new HashMap<View, PhotoItemsWrapper>();
        mItemViews = new HashMap<View, PhotoItem>();

    }

    public void setOnVideoClickListener(OnPicFolderClickListener l) {
        mListener = l;
    }

    public void setList(final List<PhotoItem> dataList) {
        if (dataList == null || dataList.isEmpty()) return;

        ThreadManager.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mSrcList.clear();
                mDataList.clear();

                mSrcList.addAll(dataList);
                mDataList.addAll(format(dataList));

                Iterator<PhotoItem> iterator = mSelectData.iterator();
                while (iterator.hasNext()) {
                    PhotoItem itemBean = iterator.next();
                    if (!mDataList.contains(itemBean)) {
                        iterator.remove();
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    public static List<PhotoItemsWrapper> format(List<PhotoItem> list) {
        if (list == null) return null;

        List<PhotoItemsWrapper> result = new ArrayList<PhotoItemsWrapper>();
        for (PhotoItem item : list) {
            String path = item.getPath();
            String parentPath = path.substring(0, path.lastIndexOf("/"));

            PhotoItemsWrapper itemsWrapper = null;
            for (PhotoItemsWrapper wrapper : result) {
                if (wrapper.parentPath.equals(parentPath)) {
                    itemsWrapper = wrapper;
                    break;
                }
            }
            if (itemsWrapper == null) {
                itemsWrapper = new PhotoItemsWrapper();
                itemsWrapper.parentPath = parentPath;
                itemsWrapper.parentName = parentPath.substring(parentPath.lastIndexOf("/") + 1);

                result.add(itemsWrapper);
            }

            if (!itemsWrapper.photoItems.contains(item)) {
                itemsWrapper.photoItems.add(item);
            }
        }

        return result;
    }

    public List<PhotoItem> getSelectData() {
        return mSelectData;
    }

    @Override
    public int getGroupCount() {
        return mDataList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        PhotoItemsWrapper wrapper = mDataList.get(groupPosition);
        return wrapper.photoItems.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mDataList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        PhotoItemsWrapper wrapper = mDataList.get(groupPosition);
        return wrapper.photoItems.get(childPosition);
    }

    public Object getFirstVisibleGroup(int firstVisibleItem) {
        int index = 0;
        for (int i = 0; i < getGroupCount(); i++) {
            int rowCount = getChildrenCount(i) / 3;
            rowCount += getChildrenCount(i) % 3 != 0 ? 1 : 0;
            index += rowCount + 1;
            if (firstVisibleItem <= index) {
                return getGroup(i);
            }
        }

        return null;
    }

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
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
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

        final PhotoItemsWrapper wrapper = (PhotoItemsWrapper) getGroup(groupPosition);
        if (isExpanded) {
            setLableContent(holder.title, wrapper.parentName, wrapper.photoItems.size());
        } else {
            holder.title.setText(wrapper.parentName);
            holder.count.setText(mContext.getString(R.string.pri_pro_folder_summary, wrapper.photoItems.size()));
            String url = "file://" + wrapper.photoItems.get(0).getPath();
            mImageLoader.displayImage(url, holder.imageView, PrivacyNewAdaper.getOptions());
        }

        holder.checkBox.setChecked(isChecked(wrapper));
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
        String url = "file://" + item.getPath();

        mImageLoader.displayImage(url, holder.imageView, getMediaOptions());
        holder.checkBox.setClickable(false);
        boolean isChecked = isChecked(item);
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
                boolean isChecked = mSelectData.contains(item);
                checkBox.setChecked(isChecked);
                if (imageView instanceof MaskImageView) {
                    ((MaskImageView) imageView).setChecked(isChecked);
                }
            }
        });
        mItemViews.put(convertView, item);
        return convertView;
    }

    private void toggle(PhotoItem data) {
        if (mSelectData.contains(data)) {
            mSelectData.remove(data);
        } else {
            mSelectData.add(data);
        }

        int group = getGroupPosition(data);
        PhotoItemsWrapper wrapper = (PhotoItemsWrapper) getGroup(group);
        boolean isGroupChecked = isChecked(wrapper);
        if (mListener != null) {
            mListener.onGroupCheckChanged(group, isGroupChecked);
        }
        for (View view : mWrapperViews.keySet()) {
            PhotoItemsWrapper itemsWrapper = mWrapperViews.get(view);
            if (itemsWrapper == wrapper) {
                FoldHolder holder = (FoldHolder) view.getTag(R.layout.pri_pro_list_sticky_header);
                if (holder != null) {
                    holder.checkBox.setChecked(isGroupChecked);
                }
            }
        }

        if (mListener != null) {
            mListener.onSelectionChange(mSelectData.size() == mSrcList.size(), mSelectData.size());
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public int getGroupTypeCount() {
        return GROUP_EXPANDED + 1;
    }

    @Override
    public int getGroupType(int groupPosition) {
        if (mExpanded) {
            return GROUP_EXPANDED;
        }
        return super.getGroupType(groupPosition);
    }

    public void setLableContent(TextView textView, String content, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(content)
                .append("<font color='#00a8ff'>(")
                .append(count)
                .append(")</font>");
        textView.setText(Html.fromHtml(stringBuilder.toString()));
    }

    public boolean isChecked(PhotoItem item) {
        return mSelectData.contains(item);
    }

    public boolean isChecked(PhotoItemsWrapper data) {
        for (PhotoItem photoItem : data.photoItems) {
            if (!isChecked(photoItem)) return false;
        }
        return true;
    }

    public String getGroupName(int group) {
        PhotoItemsWrapper wrapper = (PhotoItemsWrapper) getGroup(group);
        if (wrapper != null) {
            return wrapper.parentName;
        }

        return null;
    }

    public void selectAll(int groupPosition) {
        PhotoItemsWrapper wrapper = (PhotoItemsWrapper) getGroup(groupPosition);
        for (PhotoItem photoItem : wrapper.photoItems) {
            if (mSelectData.contains(photoItem)) continue;

            mSelectData.add(photoItem);
            for (View view : mItemViews.keySet()) {
                PhotoItem item = mItemViews.get(view);
                if (item == photoItem) {
                    PrivacyNewHolder holder = (PrivacyNewHolder) view.getTag(R.layout.pri_pro_new_pic_item);
                    if (holder != null) {
                        holder.checkBox.setChecked(true);
                        if (holder.imageView instanceof MaskImageView) {
                            ((MaskImageView) holder.imageView).setChecked(true);
                        }
                    }
                }
            }
        }
        if (mListener != null) {
            mListener.onSelectionChange(mSelectData.size() == mSrcList.size(), mSelectData.size());
        }
    }

    public void deselectAll(int groupPosition) {
        PhotoItemsWrapper wrapper = (PhotoItemsWrapper) getGroup(groupPosition);
        for (PhotoItem photoItem : wrapper.photoItems) {
            if (mSelectData.contains(photoItem)) {
                mSelectData.remove(photoItem);

                for (View view : mItemViews.keySet()) {
                    PhotoItem item = mItemViews.get(view);
                    if (item == photoItem) {
                        PrivacyNewHolder holder = (PrivacyNewHolder) view.getTag(R.layout.pri_pro_new_pic_item);
                        if (holder != null) {
                            holder.checkBox.setChecked(false);
                            if (holder.imageView instanceof MaskImageView) {
                                ((MaskImageView) holder.imageView).setChecked(false);
                            }
                        }
                    }
                }
            }
        }
        if (mListener != null) {
            mListener.onSelectionChange(mSelectData.size() == mSrcList.size(), mSelectData.size());
        }
    }

    public int getGroupPosition(PhotoItem data) {
        for (int i = 0; i <= getGroupCount(); i++) {
            PhotoItemsWrapper wrapper = (PhotoItemsWrapper) getGroup(i);
            for (PhotoItem photoItem : wrapper.photoItems) {
                if (data == photoItem) return i;
            }
        }

        return 0;
    }

    private DisplayImageOptions getMediaOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.pic_loading_shape)
                .showImageForEmptyUri(R.drawable.pic_loading_shape)
                .showImageOnFail(R.drawable.pic_loading_shape)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();

        return options;
    }

    public static class PhotoItemsWrapper {
        String parentPath;
        String parentName;
        List<PhotoItem> photoItems;

        public PhotoItemsWrapper() {
            parentName = "";
            parentPath = "";
            photoItems = new ArrayList<PhotoItem>();
        }
    }

    public static class FoldHolder extends PrivacyNewAdaper.PrivacyNewHolder {
        RippleView clickRv;
        ImageView arrow;
        TextView count;
    }
}
