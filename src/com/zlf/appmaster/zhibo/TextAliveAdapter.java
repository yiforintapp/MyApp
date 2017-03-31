package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.TextAliveInfo;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.ui.dialog.AdviceDialog;
import com.zlf.appmaster.ui.dialog.TextAliveAdviceDialog;
import com.zlf.appmaster.ui.stock.StockTextView;

import java.util.List;

/**
 * Created by Huang on 2015/3/6.
 */
public class TextAliveAdapter extends BaseAdapter implements View.OnClickListener {
    private static final String DEAL_BUY = "多单";

    private LayoutInflater mInflater;
    private Context mContext;
    private List<TextAliveInfo> mList;

    public interface ClickCallBack {
        void click(View v);
    }

    private ClickCallBack mCallBack;

    private static class ViewType {
        private final static int TYPE_1 = 0;
        private final static int TYPE_2 = 1;

        public static int getCount() {
            return 2;
        }
    }

    public TextAliveAdapter(Context context, List<TextAliveInfo> list,ClickCallBack callback) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = list;
        mCallBack = callback;
    }

    @Override
    public int getCount() {
        return mList.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {

        int p = position;
        if (p == 0) {
            return ViewType.TYPE_1;
        } else {
            return ViewType.TYPE_2;
        }
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.getCount();
    }

    @Override
    public boolean isEnabled(int position) {

        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        int viewType = getItemViewType(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            switch (viewType) {
                case ViewType.TYPE_1: {
                    convertView = mInflater.inflate(R.layout.list_text_alive_title, null);
                }
                break;
                case ViewType.TYPE_2: {
                    convertView = mInflater.inflate(R.layout.list_text_alive_item, null);
                    viewHolder = new ViewHolder();
                    viewHolder.plan = (TextView) convertView.findViewById(R.id.tv_plan);
                    viewHolder.time = (TextView) convertView.findViewById(R.id.tv_time);
                    viewHolder.planType = (TextView) convertView.findViewById(R.id.tv_plantype);
                    viewHolder.sendMan = (TextView) convertView.findViewById(R.id.tv_sendman);
                    viewHolder.click = convertView.findViewById(R.id.desc_click_area);
                    convertView.setTag(viewHolder);
                }
                break;
            }
        } else {
            switch (viewType) {
                case ViewType.TYPE_2:
                    viewHolder = (ViewHolder) convertView.getTag();
                    break;
            }
        }

        switch (viewType) {
            case ViewType.TYPE_2:

                viewHolder.click.setOnClickListener(this);
                viewHolder.click.setTag(position-1);

                Resources res = mContext.getResources();

                TextAliveInfo info = mList.get(position-1);
                viewHolder.plan.setText(info.getmPlan());
                viewHolder.time.setText(info.getmSendTime());
                viewHolder.planType.setText(info.getmPlanType());
                viewHolder.sendMan.setText(info.getmSendMan());

                if (position % 2 == 0) {
                    convertView.setBackgroundColor(res.getColor(R.color.home_tab_pressed));
                } else {
                    convertView.setBackgroundColor(res.getColor(R.color.white));
                }

                if(info.getmPlan().equals(DEAL_BUY)){
                    viewHolder.plan.setTextColor(res.getColor(R.color.main_red));
                }else{
                    viewHolder.plan.setTextColor(res.getColor(R.color.main_color));
                }

                break;
        }
        return convertView;
    }

    class ViewHolder {
        TextView plan;
        TextView time;
        TextView planType;
        TextView sendMan;
        View click;
    }

    @Override
    public void onClick(View v) {
        mCallBack.click(v);
    }
}
