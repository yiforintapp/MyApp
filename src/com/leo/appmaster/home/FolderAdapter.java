package com.leo.appmaster.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.ui.MaskImageView;
import com.leo.appmaster.ui.RippleView;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.appmaster.home.PrivacyNewAdaper.PrivacyNewHolder;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Jasper on 2015/11/13.
 */
public abstract class FolderAdapter<T> extends BaseExpandableListAdapter {

    private static final int GROUP_EXPANDED = 1;

    public interface OnFolderClickListener {
        public void onGroupClick(int groupPosition, boolean isExpanded);
        public void onGroupCheckChanged(int groupPosition, boolean checked);
        public void onSelectionChange(boolean selectAll, int selectedCount);
    }

    protected OnFolderClickListener mListener;

    private List<T> mSrcList;
    protected List<ItemsWrapper> mDataList;
    private List<T> mSelectData;

    protected LayoutInflater mInflater;
    protected Context mContext;

    protected ImageLoader mImageLoader;
    protected HashMap<View, ItemsWrapper> mWrapperViews;
    protected HashMap<View, T> mItemViews;

    // group -- > allIndex，分组号对应当前分组在所有item中的位置
    protected SparseIntArray mGroupIndexArray;

    protected boolean mExpanded;

    public FolderAdapter() {
        mSrcList = new ArrayList<T>();
        mDataList = new ArrayList<ItemsWrapper>();
        mSelectData = new ArrayList<T>();

        mContext = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(mContext);
        mImageLoader = ImageLoader.getInstance();

        mWrapperViews = new HashMap<View, ItemsWrapper>();
        mItemViews = new HashMap<View, T>();

        mGroupIndexArray = new SparseIntArray();
    }

    public void setOnFolderClickListener(OnFolderClickListener l) {
        mListener = l;
    }

    public void setList(final List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) return;

        ThreadManager.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mSrcList.clear();
                mDataList.clear();

                mSrcList.addAll(dataList);
                mDataList.addAll(format(dataList));

                Iterator<T> iterator = mSelectData.iterator();
                while (iterator.hasNext()) {
                    T itemBean = iterator.next();
                    if (!mDataList.contains(itemBean)) {
                        iterator.remove();
                    }
                }
                notifyDataSetChanged();
                initGroupIndexArray();
            }
        });
    }

    protected void initGroupIndexArray() {
        mGroupIndexArray.clear();
        int index = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            ItemsWrapper wrapper = mDataList.get(i);
            mGroupIndexArray.put(i, index);
            index += wrapper.items.size() + 1;
        }
    }

    public List<T> getSelectData() {
        return mSelectData;
    }

    @Override
    public int getGroupCount() {
        return mDataList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ItemsWrapper<T> wrapper = mDataList.get(groupPosition);
        return wrapper.items.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mDataList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ItemsWrapper<T> wrapper = mDataList.get(groupPosition);
        return wrapper.items.get(childPosition);
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

    public abstract Object getFirstVisibleGroup(int firstVisibleItem);
    public abstract int getFirstVisibleGroupPosition(int firstVisibleItem);

    public int getGroupPosition(T data) {
        for (int i = 0; i <= getGroupCount(); i++) {
            ItemsWrapper<T> wrapper = (ItemsWrapper) getGroup(i);
            for (T photoItem : wrapper.items) {
                if (data == photoItem) return i;
            }
        }

        return 0;
    }

    protected void toggle(T data) {
        if (mSelectData.contains(data)) {
            mSelectData.remove(data);
        } else {
            mSelectData.add(data);
        }

        int group = getGroupPosition(data);
        ItemsWrapper<T> wrapper = (ItemsWrapper) getGroup(group);
        boolean isGroupChecked = isGroupChecked(wrapper);
        if (mListener != null) {
            mListener.onGroupCheckChanged(group, isGroupChecked);
        }
        for (View view : mWrapperViews.keySet()) {
            ItemsWrapper<T> itemsWrapper = mWrapperViews.get(view);
            if (itemsWrapper == wrapper) {
                FoldHolder holder = (FoldHolder) view.getTag(getGroupTagId());
                if (holder != null) {
                    holder.checkBox.setChecked(isGroupChecked);
                }
            }
        }

        if (mListener != null) {
            mListener.onSelectionChange(mSelectData.size() == mSrcList.size(), mSelectData.size());
        }
    }

    protected boolean isChildChecked(T data) {
        return mSelectData.contains(data);
    }

    protected boolean isGroupChecked(ItemsWrapper<T> wrapper) {
        for (T photoItem : wrapper.items) {
            if (!isChildChecked(photoItem)) return false;
        }
        return true;
    }

    public void setLableContent(TextView textView, String content, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(content)
                .append("<font color='#00a8ff'>(")
                .append(count)
                .append(")</font>");
        textView.setText(Html.fromHtml(stringBuilder.toString()));
    }

    public String getGroupName(int group) {
        ItemsWrapper wrapper = (ItemsWrapper) getGroup(group);
        if (wrapper != null) {
            return wrapper.parentName;
        }

        return null;
    }

    public void selectAll(int groupPosition) {
        ItemsWrapper<T> wrapper = (ItemsWrapper) getGroup(groupPosition);
        for (T photoItem : wrapper.items) {
            if (mSelectData.contains(photoItem)) continue;

            mSelectData.add(photoItem);
            for (View view : mItemViews.keySet()) {
                T item = mItemViews.get(view);
                if (item == photoItem) {
                    PrivacyNewHolder holder = (PrivacyNewHolder) view.getTag(getChildTagId());
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
        ItemsWrapper<T> wrapper = (ItemsWrapper) getGroup(groupPosition);
        for (T photoItem : wrapper.items) {
            if (mSelectData.contains(photoItem)) {
                mSelectData.remove(photoItem);

                for (View view : mItemViews.keySet()) {
                    T item = mItemViews.get(view);
                    if (item == photoItem) {
                        PrivacyNewHolder holder = (PrivacyNewHolder) view.getTag(getChildTagId());
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

    protected DisplayImageOptions getMediaOptions() {
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

    protected abstract String getPath(T data);
    protected abstract int getChildTagId();
    protected abstract int getGroupTagId();

    public List<ItemsWrapper> format(List<T> list) {
        if (list == null) return null;

        List<ItemsWrapper> result = new ArrayList<ItemsWrapper>();
        for (T item : list) {
            String path = getPath(item);
            String parentPath = path.substring(0, path.lastIndexOf("/"));

            ItemsWrapper itemsWrapper = null;
            for (ItemsWrapper wrapper : result) {
                if (wrapper.parentPath.equals(parentPath)) {
                    itemsWrapper = wrapper;
                    break;
                }
            }
            if (itemsWrapper == null) {
                itemsWrapper = new ItemsWrapper<T>();
                itemsWrapper.parentPath = parentPath;
                itemsWrapper.parentName = parentPath.substring(parentPath.lastIndexOf("/") + 1);

                result.add(itemsWrapper);
            }

            if (!itemsWrapper.items.contains(item)) {
                itemsWrapper.items.add(item);
            }
        }

        return result;
    }

    public int getNextPositionOfAllDatas(int groupPostion) {
        groupPostion++;
        try {
            return mGroupIndexArray.get(groupPostion);
        } catch (Exception e) {
        }

        return -1;
    }

    public static class ItemsWrapper<T> {
        String parentPath;
        String parentName;
        List<T> items;

        public ItemsWrapper() {
            parentName = "";
            parentPath = "";
            items = new ArrayList<T>();
        }
    }

    public static class FoldHolder extends PrivacyNewAdaper.PrivacyNewHolder {
        RippleView clickRv;
        ImageView arrow;
        TextView count;
    }
}
