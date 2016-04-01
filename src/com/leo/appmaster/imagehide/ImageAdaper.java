package com.leo.appmaster.imagehide;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.ui.MaskImageView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 隐私等级适配器基类
 * Created by Jasper on 2015/10/15.
 */
public abstract class ImageAdaper<T> extends BaseAdapter {
    public interface SelectionChangeListener {
        public void onSelectionChange(boolean selectAll, int selectedCount);
    }

    private static final byte SELECTED = 0x01;
    private static final byte UNSELECTED = 0x00;

    private List<T> mDataList;
    //    private List<T> mSelectedList;
    private byte[] mSelectedArray;
    private int mSelectedCount;

    protected SelectionChangeListener mListener;

    protected LayoutInflater mInflater;
    protected Context mContext;

    protected HashMap<View, T> mItemsView;

    public ImageAdaper() {
        mDataList = new ArrayList<T>();
//        mSelectedList = new ArrayList<T>();

        mContext = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(mContext);
        mItemsView = new HashMap<View, T>();
    }

    public void setList(final List<T> dataList) {
        if (dataList == null) return;

        ThreadManager.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mDataList.clear();
                LeoLog.v("NewHideImageActivity","mDataList size " + mDataList.size());
                mDataList.addAll(dataList);
                LeoLog.v("NewHideImageActivity","mDataList size " + mDataList.size());
                mSelectedArray = new byte[dataList.size()];

                notifyDataSetChanged();
            }
        });
    }

    public void setOnSelectionChangeListener(SelectionChangeListener listener) {
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void toggle(int pos) {
        if (pos < 0) {
            return;
        }
        boolean isChecked = false;
        int selection = mSelectedArray[pos];
        if (selection == SELECTED) {
            mSelectedArray[pos] = UNSELECTED;
            mSelectedCount--;
        } else {
            mSelectedArray[pos] = SELECTED;
            mSelectedCount++;
            isChecked = true;
        }

        if (mSelectedCount == mDataList.size()) {
            if (mListener != null) {
                mListener.onSelectionChange(true, mDataList.size());
            }
        } else {
            if (mListener != null) {
                mListener.onSelectionChange(false, mSelectedCount);
            }
        }
        for (View view : mItemsView.keySet()) {
            T item = mItemsView.get(view);
            T data = (T) getItem(pos);
            if (item == data) {
                PrivacyNewHolder holder = (PrivacyNewHolder) view.getTag();
                if (holder == null) continue;

                holder.checkBox.setChecked(isChecked);
                if (holder.imageView instanceof MaskImageView) {
                    ((MaskImageView) holder.imageView).setChecked(isChecked);
                }
            }
        }
    }

    public void selectAll() {
        if (mSelectedCount != mDataList.size()) {
            mSelectedCount = mDataList.size();
            Arrays.fill(mSelectedArray, SELECTED);
            if (mListener != null) {
                mListener.onSelectionChange(true, mSelectedCount);
            }

            updateSelectItem(true);
        }
    }

    public void deselectAll() {
        if (mSelectedCount != 0) {
            mSelectedCount = 0;
            Arrays.fill(mSelectedArray, UNSELECTED);

            if (mListener != null) {
                mListener.onSelectionChange(false, 0);
            }

            updateSelectItem(false);
        }
    }

    private void updateSelectItem(boolean isChecked) {
        for (View view : mItemsView.keySet()) {
            PrivacyNewHolder holder = (PrivacyNewHolder) view.getTag();
            if (holder == null) continue;

            holder.checkBox.setChecked(isChecked);
            if (holder.imageView instanceof MaskImageView) {
                ((MaskImageView) holder.imageView).setChecked(isChecked);
            }
        }
    }

    public List<T> getSelectedList() {
        List result = new ArrayList(mSelectedCount);
        for (int i = 0; i < mSelectedArray.length; i++) {
            if (mSelectedArray[i] == SELECTED) {
                result.add(mDataList.get(i));
            }
        }
        return result;
    }

    public DisplayImageOptions getMediaOptions() {
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

    public static DisplayImageOptions getOptions() {
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

    public boolean isChecked(int pos) {
        if (pos < 0 || pos >= mSelectedArray.length) {
            return false;
        }
        return mSelectedArray[pos] == SELECTED;
    }

    public static class PrivacyNewHolder {
        public ImageView imageView;
        public TextView title;
        public TextView summary;
        public CheckBox checkBox;
    }

}
