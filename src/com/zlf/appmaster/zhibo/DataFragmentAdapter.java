package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.AdviceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/28.
 */
public class DataFragmentAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<AdviceItem> mList;


    public DataFragmentAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<AdviceItem>();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setList(List list) {
        mList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_data_adapter, null);
            holder = new ViewHolder();
            holder.mCode = (TextView) convertView.findViewById(R.id.code_content);
            holder.mTime = (TextView) convertView.findViewById(R.id.time_content);
            holder.mStyle = (TextView) convertView.findViewById(R.id.style_content);
            holder.mCangwei = (TextView) convertView.findViewById(R.id.cangwei_content);
            holder.mShangPin = (TextView) convertView.findViewById(R.id.shangpin_content);
            holder.mKaiCangJia = (TextView) convertView.findViewById(R.id.kaicangjia_content);
            holder.mZhiSunJia = (TextView) convertView.findViewById(R.id.zhisunjia_content);
            holder.mZhiYingJia = (TextView) convertView.findViewById(R.id.zhiyingjia_content);
            holder.mFenXiShi = (TextView) convertView.findViewById(R.id.fenxishi_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AdviceItem item = mList.get(position);

        holder.mCode.setText(item.getId());
        holder.mTime.setText(item.getTime());
        holder.mStyle.setText(item.getType());
        holder.mCangwei.setText(item.getPosition());
        holder.mShangPin.setText(item.getKind());
        holder.mKaiCangJia.setText(item.getOpen());
        holder.mZhiSunJia.setText(item.getStop());
        holder.mZhiYingJia.setText(item.getOnly());
        holder.mFenXiShi.setText(item.getName());

        return convertView;
    }

    class ViewHolder {
        TextView mCode;
        TextView mTime;
        TextView mStyle;
        TextView mCangwei;
        TextView mShangPin;
        TextView mKaiCangJia;
        TextView mZhiSunJia;
        TextView mZhiYingJia;
        TextView mFenXiShi;
    }
}

