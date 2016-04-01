package com.leo.appmaster.imagehide;

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
import com.leo.appmaster.home.PrivacyNewAdaper.PrivacyNewHolder;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jasper on 2015/11/13.
 */
public abstract class FolderNewAdapter<T> extends BaseExpandableListAdapter {

    private static final String TAG = FolderNewAdapter.class.getSimpleName();

    private static final int GROUP_EXPANDED = 1;
    private static final byte SELECTED = 0x01;
    private static final byte UNSELECTED = 0x00;

    public interface OnFolderClickListener {
        public void onGroupClick(int groupPosition, boolean isExpanded);

        public void onGroupCheckChanged(int groupPosition, boolean checked);

        public void onSelectionChange(boolean selectAll, int selectedCount);

    }

    protected OnFolderClickListener mListener;

    private List<T> mSrcList;
    protected List<ItemsWrapper> mDataList;
//    private List<T> mSelectData;
    private List<SelectionInfo> mSelectionInfo;

    protected LayoutInflater mInflater;
    protected Context mContext;

    protected ImageLoader mImageLoader;
    protected HashMap<View, ItemsWrapper> mWrapperViews;
    protected HashMap<View, T> mItemViews;

    // group -- > allIndex，分组号对应当前分组在所有item中的位置
    protected SparseIntArray mGroupIndexArray;

    protected boolean mExpanded;

    public FolderNewAdapter() {
        mSrcList = new ArrayList<T>();
        mDataList = new ArrayList<ItemsWrapper>();

        mContext = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(mContext);
        mImageLoader = ImageLoader.getInstance();

        mWrapperViews = new HashMap<View, ItemsWrapper>();
        mItemViews = new HashMap<View, T>();

        mGroupIndexArray = new SparseIntArray();
    }

    public List<ItemsWrapper> getDataList(){
        return mDataList;
    }

    public void setOnFolderClickListener(OnFolderClickListener l) {
        mListener = l;
    }

    public void setList(final List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) return;

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                List<ItemsWrapper> wrapperList = format(dataList);
                mSrcList.clear();
                mSrcList.addAll(dataList);
                setListInner(wrapperList);
            }
        });
    }

    private void setListInner(final List<ItemsWrapper> dataList) {
        ThreadManager.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mDataList.clear();
                mDataList.addAll(dataList);
                mSelectionInfo = new ArrayList<SelectionInfo>(mDataList.size());
                for (ItemsWrapper wrapper : mDataList) {
                    SelectionInfo info = new SelectionInfo();
                    info.selectedArray = new byte[wrapper.items.size()];
                    mSelectionInfo.add(info);
                }
                initGroupIndexArray();
                notifyDataSetChanged();
            }
        });
    }

    protected abstract void initGroupIndexArray();

    public List<T> getSelectData() {
        List<T> result = new ArrayList<T>();
        for (int i = 0; i < mSelectionInfo.size(); i++) {
            SelectionInfo info = mSelectionInfo.get(i);
            ItemsWrapper<T> wrapper = mDataList.get(i);
            for (int j = 0; j < info.selectedArray.length; j++) {
                if (info.selectedArray[j] == SELECTED) {
                    result.add(wrapper.items.get(j));
                }
            }
        }
        return result;
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

    public void setCheck(View view, boolean childChecked) {
        LeoLog.d("testsetcheck", "father setcheckF");
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

    public void toggle(int group, int child) {
        SelectionInfo info = mSelectionInfo.get(group);
        byte selection = info.selectedArray[child];
        if (selection == SELECTED) {
            info.selectedArray[child] = UNSELECTED;
            info.selectedCount--;
        } else {
            info.selectedArray[child] = SELECTED;
            info.selectedCount++;
        }

        ItemsWrapper<T> wrapper = (ItemsWrapper) getGroup(group);
        boolean isGroupChecked = isGroupChecked(group);
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
            int selectedCount = 0;
            for (SelectionInfo selectionInfo : mSelectionInfo) {
                selectedCount += selectionInfo.selectedCount;
            }
            mListener.onSelectionChange(selectedCount == mSrcList.size(), selectedCount);
        }
    }

    public boolean isChildChecked(int group, int child) {
        SelectionInfo info = mSelectionInfo.get(group);
        byte selection = info.selectedArray[child];

        return selection == SELECTED;
    }

    public boolean isGroupChecked(int group) {
        ItemsWrapper<T> wrapper = mDataList.get(group);
        SelectionInfo info = mSelectionInfo.get(group);

        return info.selectedCount == wrapper.items.size();
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

    public void selectAllGroup(){
        for (int i = 0; i < mDataList.size(); i++) {
            ItemsWrapper<T> wrapper = (ItemsWrapper) getGroup(i);
            SelectionInfo info = mSelectionInfo.get(i);
            if (info.selectedCount != wrapper.items.size()) {
                Arrays.fill(info.selectedArray, SELECTED);
                info.selectedCount = wrapper.items.size();
            }
        }
        notifyDataSetChanged();
        int totalCount = 0;
        for (SelectionInfo selectionInfo : mSelectionInfo) {
            totalCount += selectionInfo.selectedCount;
        }
        if (mListener != null) {
            mListener.onSelectionChange(totalCount == mSrcList.size(), totalCount);
        }
    }

    public void selectAll(int groupPosition) {
        ItemsWrapper<T> wrapper = (ItemsWrapper) getGroup(groupPosition);
        SelectionInfo info = mSelectionInfo.get(groupPosition);
        if (info.selectedCount != wrapper.items.size()) {
            Arrays.fill(info.selectedArray, SELECTED);
            info.selectedCount = wrapper.items.size();

            notifyDataSetChanged();

            int totalCount = 0;
            for (SelectionInfo selectionInfo : mSelectionInfo) {
                totalCount += selectionInfo.selectedCount;
            }
            if (mListener != null) {
                mListener.onSelectionChange(totalCount == mSrcList.size(), totalCount);
            }
        }
    }

    public void deselectAllGroup(){
        for (int i = 0; i < mDataList.size(); i++) {
            ItemsWrapper<T> wrapper = (ItemsWrapper) getGroup(i);
            SelectionInfo info = mSelectionInfo.get(i);
            if (info.selectedCount == wrapper.items.size()) {
                Arrays.fill(info.selectedArray, UNSELECTED);
                info.selectedCount = 0;
            }
        }
        notifyDataSetChanged();
        int totalCount = 0;
        for (SelectionInfo selectionInfo : mSelectionInfo) {
            totalCount += selectionInfo.selectedCount;
        }
        if (mListener != null) {
            mListener.onSelectionChange(totalCount == mSrcList.size(), totalCount);
        }
    }

    public void deselectAll(int groupPosition) {
        ItemsWrapper<T> wrapper = (ItemsWrapper) getGroup(groupPosition);
        SelectionInfo info = mSelectionInfo.get(groupPosition);
        if (info.selectedCount == wrapper.items.size()) {
            Arrays.fill(info.selectedArray, UNSELECTED);
            info.selectedCount = 0;

            notifyDataSetChanged();

            int totalCount = 0;
            for (SelectionInfo selectionInfo : mSelectionInfo) {
                totalCount += selectionInfo.selectedCount;
            }
            if (mListener != null) {
                mListener.onSelectionChange(totalCount == mSrcList.size(), totalCount);
            }
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
        int index = -1;
        for (T item : list) {
            String path = getPath(item);
            index = path.lastIndexOf("/");
            if(index < 1) {
                continue;
            }
            String parentPath = path.substring(0, index);

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
        public String parentPath;
        public String parentName;
        public List<T> items;

        public ItemsWrapper() {
            parentName = "";
            parentPath = "";
            items = new ArrayList<T>();
        }
    }

    public static class FoldHolder extends PrivacyNewHolder {
        public RippleView clickRv;
        public ImageView arrow;
        public TextView count;
    }

    private static class SelectionInfo {
        public int selectedCount;
        public byte[] selectedArray;
    }
}
