package com.leo.appmaster.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * 隐私等级适配器基类
 * Created by Jasper on 2015/10/15.
 */
public abstract class PrivacyNewAdaper<T> extends BaseAdapter {
    public interface SelectionChangeListener {
        public void onSelectionChange(boolean selectAll, int selectedCount);
    }

    private List<T> mDataList;
    private List<T> mSelectedList;

    protected SelectionChangeListener mListener;

    protected LayoutInflater mInflater;
    protected Context mContext;

    public PrivacyNewAdaper() {
        mDataList = new ArrayList<T>();
        mSelectedList = new ArrayList<T>();

        mContext = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(mContext);
    }

    public void setList(final List<T> dataList) {
        if (dataList == null) return;

        ThreadManager.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mDataList.clear();
                mDataList.addAll(dataList);

                Iterator<T> iterator = mSelectedList.iterator();
                while (iterator.hasNext()) {
                    T data = iterator.next();
                    if (!mDataList.contains(data)) {
                        iterator.remove();
                    }
                }
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

    public void toggle(T data) {
        if (mSelectedList.contains(data)) {
            mSelectedList.remove(data);
        } else {
            mSelectedList.add(data);
        }

        if (mSelectedList.size() == mDataList.size()) {
            if (mListener != null) {
                mListener.onSelectionChange(true, mDataList.size());
            }
        } else {
            if (mListener != null) {
                mListener.onSelectionChange(false, mSelectedList.size());
            }
        }
        notifyDataSetChanged();
    }

    public void selectAll() {
        if (mSelectedList.size() != mDataList.size()) {
            mSelectedList.clear();
            mSelectedList.addAll(mDataList);

            if (mListener != null) {
                mListener.onSelectionChange(true, mSelectedList.size());
            }
            notifyDataSetChanged();
        }
    }

    public void deselectAll() {
        mSelectedList.clear();
        if (mListener != null) {
            mListener.onSelectionChange(false, mSelectedList.size());
        }
        notifyDataSetChanged();
    }

    public List<T> getSelectedList() {
        return mSelectedList;
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

    public boolean isChecked(T data) {
        return mSelectedList.contains(data);
    }

    public static class PrivacyNewHolder {
        ImageView imageView;
        TextView title;
        TextView summary;
        CheckBox checkBox;
    }

}
