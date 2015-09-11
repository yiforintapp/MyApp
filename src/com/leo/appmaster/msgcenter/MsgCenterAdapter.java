package com.leo.appmaster.msgcenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.MsgCenterTable;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/9/10.
 */
public class MsgCenterAdapter extends BaseAdapter {
    private List<Message> mMessageList;
    private LayoutInflater mInflater;

    private BitmapFactory.Options options;
    private DisplayImageOptions commonOption;
    private DisplayImageOptions compatibleOption;


    public MsgCenterAdapter() {
        mMessageList = new ArrayList<Message>();

        AppMasterApplication ctx = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(ctx);

        options = new BitmapFactory.Options();
        // 主题使用565配置
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        commonOption = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .showImageOnLoading(R.drawable.online_theme_loading)
                .showImageOnFail(R.drawable.online_theme_loading_failed)
                .displayer(new FadeInBitmapDisplayer(500))
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .decodingOptions(options)
                .build();
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterTable table = new MsgCenterTable();
                List<Message> list = table.queryMsgList();
                onQueryResult(list);
            }
        });
    }

    private void onQueryResult(final List<Message> list) {
        if (list == null || list.isEmpty()) return;

        ThreadManager.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mMessageList.clear();
                mMessageList.addAll(list);

                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MsgCenterHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.mc_list_item, null);

            holder = new MsgCenterHolder();
            holder.description = (TextView) convertView.findViewById(R.id.mc_description_tv);
            holder.image = (ImageView) convertView.findViewById(R.id.mc_iv);
            holder.unread = (ImageView) convertView.findViewById(R.id.mc_unread_iv);
            holder.time = (TextView) convertView.findViewById(R.id.mc_time_tv);
            holder.title = (TextView) convertView.findViewById(R.id.mc_title_tv);

            convertView.setTag(holder);
        } else {
            holder = (MsgCenterHolder) convertView.getTag();
        }

        Message msg = (Message) getItem(position);
        holder.title.setText(msg.title);
        holder.time.setText(msg.time);
        holder.description.setText(msg.description);
        if (msg.unread) {
            holder.unread.setVisibility(View.VISIBLE);
        } else {
            holder.unread.setVisibility(View.GONE);
        }
        ImageLoader.getInstance().displayImage(msg.imageUrl, holder.image, commonOption);

        return convertView;
    }

    private static class MsgCenterHolder {
        ImageView unread;
        TextView title;
        TextView time;
        ImageView image;
        TextView description;
    }
}
