package com.zlf.appmaster.msgcenter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.db.MsgCenterTable;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.imageloader.DisplayImageOptions;
import com.zlf.imageloader.ImageLoader;
import com.zlf.imageloader.core.FadeInBitmapDisplayer;
import com.zlf.imageloader.core.ImageScaleType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private int mDesUpdatePadding;
    private int mDesPadding;

    private ListView mListView;
    private View mEmptyView;

    public MsgCenterAdapter(ListView listView, View empty) {
        mListView = listView;
        mEmptyView = empty;

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
        mDesUpdatePadding = res.getDimensionPixelSize(R.dimen.mc_item_update_des_padding);
        mDesPadding = res.getDimensionPixelSize(R.dimen.mc_item_des_padding);
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterTable table = new MsgCenterTable();
                List<Message> list = table.queryMsgList(false);
                onQueryResult(list);
            }
        });
    }

    private void onQueryResult(final List<Message> list) {
        ThreadManager.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mListView.setEmptyView(mEmptyView);
            }
        });
        if (list == null || list.isEmpty()) return;
        LeoLog.d(TAG, "onQueryResult, list size: " + list.size());

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
            holder.description.setVisibility(View.INVISIBLE);
            holder.description.setPadding(0, mDesUpdatePadding, 0, mDesUpdatePadding);
//            holder.image.setPadding(0, 0, 0, mUpdateBottomPadding);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setPadding(0, mDesPadding, 0, mDesPadding);
//            holder.image.setPadding(0, 0, 0, 0);
        }
//        String url = "file://" + Environment.getExternalStorageDirectory() + "/banner.png";
//        ImageLoader.getInstance().displayImage(url, holder.image, commonOption);
        ImageLoader.getInstance().displayImage(msg.imageUrl, holder.image, commonOption);

        return convertView;
    }

    public static class MsgCenterHolder {
        ImageView unread;
        TextView title;
        TextView time;
        ImageView image;
        TextView description;
    }
}
