package com.leo.appmaster.msgcenter;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/9/10.
 */
public class MsgCenterAdapter extends BaseAdapter {
    private List<Message> mMessageList;
    private LayoutInflater mInflater;

    public MsgCenterAdapter() {
        mMessageList = new ArrayList<Message>();

        AppMasterApplication ctx = AppMasterApplication.getInstance();
        mInflater = LayoutInflater.from(ctx);
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                List<Message> list = MsgCenterTable.getInstance().queryMsgList();
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
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
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
