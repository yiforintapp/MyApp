package com.leo.appmaster.msgcenter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Program;
import android.text.TextUtils;
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
import com.leo.appmaster.schedule.MsgCenterFetchJob;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jasper on 2015/9/10.
 */
public class MsgCenterAdapter extends BaseAdapter {
    private static final String TAG = "MsgCenterAdapter";
    private List<Message> mMessageList;
    private LayoutInflater mInflater;

    private BitmapFactory.Options options;
    private DisplayImageOptions commonOption;

    private Context mContext;

    private int mTitleLeftPadding;
    private int mUpdateBottomPadding;

    public MsgCenterAdapter() {
        mMessageList = new ArrayList<Message>();

        mContext = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(mContext);

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

        Resources res = mContext.getResources();
        mTitleLeftPadding = res.getDimensionPixelSize(R.dimen.mc_item_title_left_padding);
        mUpdateBottomPadding = res.getDimensionPixelSize(R.dimen.mc_item_update_bottom_padding);
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterTable table = new MsgCenterTable();
                List<Message> list = table.queryMsgList(false);
                onQueryResult(list);
            }
        });
    }

    private void onQueryResult(final List<Message> list) {
        if (list == null || list.isEmpty()) return;

//        Iterator<Message> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            Message msg = iterator.next();
//            if (!msg.isCategoryUpdate()) continue;
//
//            // 更新日志，还没有缓存，则不显示
//            if (!MsgCenterFetchJob.hasCacheFile(msg.jumpUrl)) {
//                iterator.remove();
//                LeoLog.i(TAG, "remove from list, becase there is no cache.");
//            }
//        }
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

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
        String time = null;
        try {
            Date date = format.parse(msg.time);
            time = simpleFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.time.setText(TextUtils.isEmpty(time) ? msg.time : time);
        holder.description.setText(msg.description);
        if (msg.unread) {
            holder.title.setPadding(mTitleLeftPadding, 0, 0, 0);
            holder.unread.setVisibility(View.VISIBLE);
        } else {
            holder.unread.setVisibility(View.GONE);
            holder.title.setPadding(0, 0, 0, 0);
        }
        if (msg.isCategoryUpdate()) {
            // 更新日志只显示标题，不显示描述
            holder.description.setVisibility(View.GONE);
            holder.image.setPadding(0, 0, 0, mUpdateBottomPadding);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.image.setPadding(0, 0, 0, 0);
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
