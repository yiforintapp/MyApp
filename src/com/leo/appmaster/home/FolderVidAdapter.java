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
public class FolderVidAdapter extends BaseExpandableListAdapter {
    public static interface OnVideoClickListener {
        public void onGroupClick(int groupPosition, boolean isExpanded);

        public void onChildClick(int groupPosition, int childPosition);

        public void onListScroll(int groupPosition, int childPosition);

        public void onGroupCheckChanged(int groupPosition, boolean checked);

        public void onSelectionChange(boolean selectAll, int selectedCount);
    }

    private List<VideoItemBean> mSrcList;
    private List<VideoItemsWrapper> mDataList;

    private LayoutInflater mInflater;
    private Context mContext;
    private List<VideoItemBean> mSelectData;

    private ImageLoader mImageLoader;
    private OnVideoClickListener mListener;

    private HashMap<View, VideoItemsWrapper> mWrapperViews;
    private HashMap<View, VideoItemBean> mItemViews;

    public FolderVidAdapter() {
        mSrcList = new ArrayList<VideoItemBean>();
        mDataList = new ArrayList<VideoItemsWrapper>();
        mSelectData = new ArrayList<VideoItemBean>();

        mContext = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(mContext);
        mImageLoader = ImageLoader.getInstance();

        mWrapperViews = new HashMap<View, VideoItemsWrapper>();
        mItemViews = new HashMap<View, VideoItemBean>();
    }

    public void setList(final List<VideoItemBean> dataList) {
        if (dataList == null || dataList.isEmpty()) return;

        ThreadManager.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mSrcList.clear();
                mDataList.clear();

                mSrcList.addAll(dataList);
                mDataList.addAll(format(dataList));

                Iterator<VideoItemBean> iterator = mSelectData.iterator();
                while (iterator.hasNext()) {
                    VideoItemBean itemBean = iterator.next();
                    if (!mDataList.contains(itemBean)) {
                        iterator.remove();
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    public void setOnVideoClickListener(OnVideoClickListener l) {
        mListener = l;
    }

    public static List<VideoItemsWrapper> format(List<VideoItemBean> list) {
        if (list == null) return null;

        List<VideoItemsWrapper> result = new ArrayList<VideoItemsWrapper>();
        for (VideoItemBean item : list) {
            String path = item.getPath();
            String parentPath = path.substring(0, path.lastIndexOf("/"));

            VideoItemsWrapper itemsWrapper = null;
            for (VideoItemsWrapper wrapper : result) {
                if (wrapper.parentPath.equals(parentPath)) {
                    itemsWrapper = wrapper;
                    break;
                }
            }
            if (itemsWrapper == null) {
                itemsWrapper = new VideoItemsWrapper();
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

    public List<VideoItemBean> getSelectData() {
        return mSelectData;
    }

    @Override
    public int getGroupCount() {
        return mDataList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        VideoItemsWrapper wrapper = mDataList.get(groupPosition);
        return wrapper.photoItems.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mDataList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        VideoItemsWrapper wrapper = mDataList.get(groupPosition);
        return wrapper.photoItems.get(childPosition);
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

        final VideoItemsWrapper wrapper = (VideoItemsWrapper) getGroup(groupPosition);
        if (isExpanded) {
            setLableContent(holder.title, wrapper.parentName, wrapper.photoItems.size());
        } else {
            holder.title.setText(wrapper.parentName);
            holder.count.setText(mContext.getString(R.string.pri_pro_folder_summary, wrapper.photoItems.size()));
            String url = "voidefile://" + wrapper.photoItems.get(0).getPath();
            mImageLoader.displayImage(url, holder.imageView, PrivacyNewAdaper.getOptions());
        }

        holder.checkBox.setClickable(false);
        holder.checkBox.setChecked(isChecked(wrapper));
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

        if (isChecked(info)) {
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
                checkBox.setChecked(mSelectData.contains(info));
            }
        });
        if (mListener != null) {
            mListener.onListScroll(groupPosition, childPosition);
        }
        mItemViews.put(convertView, info);
        return convertView;
    }

    public String getGroupName(int group) {
        VideoItemsWrapper wrapper = (VideoItemsWrapper) getGroup(group);
        if (wrapper != null) {
            return wrapper.parentName;
        }

        return null;
    }

    private void toggle(VideoItemBean data) {
        if (mSelectData.contains(data)) {
            mSelectData.remove(data);
        } else {
            mSelectData.add(data);
        }

        int group = getGroupPosition(data);
        VideoItemsWrapper wrapper = (VideoItemsWrapper) getGroup(group);
        boolean isGroupChecked = isChecked(wrapper);
        if (mListener != null) {
            mListener.onGroupCheckChanged(group, isChecked(wrapper));
        }

        for (View view : mWrapperViews.keySet()) {
            VideoItemsWrapper itemsWrapper = mWrapperViews.get(view);
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

    public void selectAll(int groupPosition) {
        VideoItemsWrapper wrapper = (VideoItemsWrapper) getGroup(groupPosition);
        for (VideoItemBean photoItem : wrapper.photoItems) {
            if (mSelectData.contains(photoItem)) continue;

            mSelectData.add(photoItem);
            for (View view : mItemViews.keySet()) {
                VideoItemBean item = mItemViews.get(view);
                if (item == photoItem) {
                    PrivacyNewAdaper.PrivacyNewHolder holder = (PrivacyNewAdaper.PrivacyNewHolder) view.getTag();
                    if (holder != null) {
                        holder.checkBox.setChecked(true);
                    }
                }
            }
        }
        for (View view : mWrapperViews.keySet()) {
            VideoItemsWrapper itemsWrapper = mWrapperViews.get(view);
            if (itemsWrapper == wrapper) {
                FoldHolder holder = (FoldHolder) view.getTag(R.layout.pri_pro_list_sticky_header);
                if (holder != null) {
                    holder.checkBox.setChecked(true);
                }
            }
        }
        if (mListener != null) {
            mListener.onSelectionChange(mSelectData.size() == mSrcList.size(), mSelectData.size());
        }
    }

    public void deselectAll(int groupPosition) {
        VideoItemsWrapper wrapper = (VideoItemsWrapper) getGroup(groupPosition);
        for (VideoItemBean photoItem : wrapper.photoItems) {
            if (mSelectData.contains(photoItem)) {
                mSelectData.remove(photoItem);

                for (View view : mItemViews.keySet()) {
                    VideoItemBean item = mItemViews.get(view);
                    if (item == photoItem) {
                        PrivacyNewAdaper.PrivacyNewHolder holder = (PrivacyNewAdaper.PrivacyNewHolder) view.getTag();
                        if (holder != null) {
                            holder.checkBox.setChecked(false);
                        }
                    }
                }
            }
        }
        for (View view : mWrapperViews.keySet()) {
            VideoItemsWrapper itemsWrapper = mWrapperViews.get(view);
            if (itemsWrapper == wrapper) {
                FoldHolder holder = (FoldHolder) view.getTag(R.layout.pri_pro_list_sticky_header);
                if (holder != null) {
                    holder.checkBox.setChecked(false);
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

    public void setLableContent(TextView textView, String content, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(content)
                .append("<font color='#00a8ff'>(")
                .append(count)
                .append(")</font>");
        textView.setText(Html.fromHtml(stringBuilder.toString()));
    }

    public boolean isChecked(VideoItemBean item) {
        return mSelectData.contains(item);
    }

    public boolean isChecked(VideoItemsWrapper data) {
        for (VideoItemBean photoItem : data.photoItems) {
            if (!isChecked(photoItem)) return false;
        }
        return true;
    }

    public int getGroupPosition(VideoItemBean data) {
        for (int i = 0; i <= getGroupCount(); i++) {
            VideoItemsWrapper wrapper = (VideoItemsWrapper) getGroup(i);
            for (VideoItemBean photoItem : wrapper.photoItems) {
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

    public static class VideoItemsWrapper {
        String parentPath;
        String parentName;
        List<VideoItemBean> photoItems;

        public VideoItemsWrapper() {
            parentName = "";
            parentPath = "";
            photoItems = new ArrayList<VideoItemBean>();
        }
    }

    public static class FoldHolder extends PrivacyNewAdaper.PrivacyNewHolder {
        RippleView clickRv;
        ImageView arrow;
        TextView count;
    }
}
