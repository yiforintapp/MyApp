package com.leo.appmaster.callFilter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qili on 15-10-10.
 */
public class BlackListAdapter extends BaseAdapter implements View.OnClickListener {
    private List<CallFilterInfo> mList;
    private String mFlag;
    private Context mContext;
    private LayoutInflater layoutInflater;

    public BlackListAdapter(Context mContext) {
        this.mContext = mContext;
        mList = new ArrayList<CallFilterInfo>();
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        BlackListHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.black_list_item, null);

            holder = new BlackListHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.desc = (TextView) convertView.findViewById(R.id.tv_desc);
            holder.clickView = (ImageView) convertView.findViewById(R.id.bg_delete);

            convertView.setTag(holder);
        } else {
            holder = (BlackListHolder) convertView.getTag();
        }


        if (mFlag.equals(CallFilterConstants.ADAPTER_FLAG_BLACK_LIST)) {
            CallFilterInfo info = mList.get(i);
            String numberName = info.numberName;
            String number = info.number;

            if (!Utilities.isEmpty(numberName)) {
                holder.title.setText(numberName);
                holder.desc.setText(number);
            } else {
                holder.title.setText(number);
                holder.desc.setVisibility(View.GONE);
            }

            holder.clickView.setOnClickListener(BlackListAdapter.this);

        }
        return convertView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bg_delete:
                Toast.makeText(mContext, "!!!!!!!!!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static class BlackListHolder {
        ImageView imageView;
        TextView title;
        TextView desc;
        ImageView clickView;
    }

    public void setData(ArrayList<CallFilterInfo> infoList) {
        mList = infoList;
        notifyDataSetChanged();
    }

    public void setFlag(String fromWhere) {
        mFlag = fromWhere;
    }


}
