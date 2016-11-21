package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.WordChatItem;
import com.zlf.appmaster.model.WordMyAskItem;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.TimeUtil;
import com.zlf.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/3.
 */
public class WordMyAskFragmentAdapter extends BaseAdapter {

    public static final int TYPE_ASK = 0;
    public static final int TYPE_ANSWER = 1;
    public static final String C_TYPE_O = "oil";
    public static final String C_TYPE_S = "silver";
    public static final String C_TYPE_C = "copper";

    private LayoutInflater mInflater;
    private Context mContext;
    private List<WordMyAskItem> mList;


    public WordMyAskFragmentAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<WordMyAskItem>();
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
    public int getItemViewType(int position) {

        WordMyAskItem item = mList.get(position);
        String status = item.getmStatus();
        if(status.equals("0")){
            return TYPE_ASK;
        }else{
            return TYPE_ANSWER;
        }

    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        int viewType = getItemViewType(position);
        WordMyAskItem item;
        switch (viewType) {
            case TYPE_ASK:
                ViewAskHolder askHolder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.word_myask_ask_item, null);
                    askHolder = new ViewAskHolder();
                    askHolder.name = (TextView) convertView.findViewById(R.id.chat_name);
                    askHolder.text = (TextView) convertView.findViewById(R.id.chat_text);
                    askHolder.time = (TextView) convertView.findViewById(R.id.chat_time);
                    askHolder.cate = (TextView) convertView.findViewById(R.id.string_type_cate);
                    askHolder.status = (TextView) convertView.findViewById(R.id.string_status_cate);
                    convertView.setTag(askHolder);
                } else {
                    askHolder = (ViewAskHolder) convertView.getTag();
                }
                item = mList.get(position);
                askHolder.name.setText(item.getCName());
                askHolder.text.setText(item.getMsg());
                if (!TextUtils.isEmpty(item.getAskTime())) {
                    askHolder.time.setText(mContext.getResources().getString(
                            R.string.word_ask_time, TimeUtil.getSimpleTime(Long.parseLong(item.getAskTime()))));
                }
                String cate_ask = getCate(item.getmCate());
                askHolder.cate.setText(cate_ask);
                askHolder.status.setText(mContext.getString(R.string.text_zhibo_mysak_type_ask_status_stay));
                break;
            case TYPE_ANSWER:
                ViewHolder holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.word_myask_answer_item, null);
                    holder = new ViewHolder();
                    holder.mAskLayout = (RelativeLayout) convertView.findViewById(R.id.ask_layout);
                    holder.mCName = (TextView) convertView.findViewById(R.id.user_name);
                    holder.mTName = (TextView) convertView.findViewById(R.id.admin_name);
                    holder.mMsg = (TextView) convertView.findViewById(R.id.user_content);
                    holder.mAnswer = (TextView) convertView.findViewById(R.id.admin_content);
                    holder.mAskTime = (TextView) convertView.findViewById(R.id.user_time);
                    holder.mAnswerTime = (TextView) convertView.findViewById(R.id.admin_time);
                    holder.mCate = (TextView) convertView.findViewById(R.id.string_type_cate);
                    holder.mStatus = (TextView) convertView.findViewById(R.id.string_status_cate);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                item = mList.get(position);
                holder.mCName.setText(item.getCName());
                holder.mTName.setText(item.getTName());
                holder.mMsg.setText(item.getMsg());
                holder.mAnswer.setText(item.getAnswer());
                if (!TextUtils.isEmpty(item.getAskTime())) {
                    holder.mAskTime.setText(mContext.getResources().getString(
                            R.string.word_ask_time, TimeUtil.getSimpleTime(Long.parseLong(item.getAskTime()))));
                }
                if (!TextUtils.isEmpty(item.getAnswerTime())) {
                    holder.mAnswerTime.setText(mContext.getResources().getString(
                            R.string.word_answer_time, TimeUtil.getSimpleTime(Long.parseLong(item.getAnswerTime()))));
                }

                if (TextUtils.isEmpty(holder.mCName.getText())) {
                    holder.mAskLayout.setVisibility(View.GONE);
                } else {
                    holder.mAskLayout.setVisibility(View.VISIBLE);
                }

                String cate = getCate(item.getmCate());
                holder.mCate.setText(cate);
                holder.mStatus.setText(mContext.getString(R.string.text_zhibo_mysak_type_ask_status_rep));
                break;
        }
        return convertView;
    }

    private String getCate(String string) {
        if(Utilities.isEmpty(string)){
            string = mContext.getString(R.string.text_zhibo_mysak_type_all);
        }else{
            if(string.equals(C_TYPE_O)){
                string = mContext.getString(R.string.word_fragment_tab_oil);
            }else if(string.equals(C_TYPE_S)){
                string = mContext.getString(R.string.word_fragment_tab_silver);
            }else if(string.equals(C_TYPE_C)){
                string = mContext.getString(R.string.word_fragment_tab_copper);
            }else{
                string = mContext.getString(R.string.text_zhibo_mysak_type_all);
            }
        }
        return string;
    }

    class ViewHolder {
        RelativeLayout mAskLayout;
        TextView mCName;
        TextView mTName;
        TextView mMsg;
        TextView mAnswer;
        TextView mAskTime;
        TextView mAnswerTime;
        TextView mCate;
        TextView mStatus;
    }

    class ViewAskHolder {
        TextView name, text, time,cate,status;
    }
}
