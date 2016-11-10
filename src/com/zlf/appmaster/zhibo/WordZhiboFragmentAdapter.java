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
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/3.
 */
public class WordZhiboFragmentAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private List<WordChatItem> mList;


    public WordZhiboFragmentAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<WordChatItem>();
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
        WordChatItem wordChatItem;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.word_answer_item, null);
            holder = new ViewHolder();
            holder.mAskLayout = (RelativeLayout) convertView.findViewById(R.id.ask_layout);
            holder.mCName = (TextView) convertView.findViewById(R.id.user_name);
            holder.mTName = (TextView) convertView.findViewById(R.id.admin_name);
            holder.mMsg = (TextView) convertView.findViewById(R.id.user_content);
            holder.mAnswer = (TextView) convertView.findViewById(R.id.admin_content);
            holder.mAskTime = (TextView) convertView.findViewById(R.id.user_time);
            holder.mAnswerTime = (TextView) convertView.findViewById(R.id.admin_time);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        wordChatItem = mList.get(position);

        holder.mCName.setText(wordChatItem.getCName());
        holder.mTName.setText(wordChatItem.getTName());
        holder.mMsg.setText(wordChatItem.getMsg());
        holder.mAnswer.setText(wordChatItem.getAnswer());
        if (!TextUtils.isEmpty(wordChatItem.getAskTime())) {
            holder.mAskTime.setText(TimeUtil.getSimpleTime(Long.parseLong(wordChatItem.getAskTime())));
        }
        if (!TextUtils.isEmpty(wordChatItem.getAnswerTime())) {
            holder.mAnswerTime.setText(TimeUtil.getSimpleTime(Long.parseLong(wordChatItem.getAnswerTime())));
        }

        if (TextUtils.isEmpty(holder.mCName.getText())) {
            holder.mAskLayout.setVisibility(View.GONE);
        } else {
            holder.mAskLayout.setVisibility(View.VISIBLE);
        }


        return convertView;
    }

    class ViewHolder {
        RelativeLayout mAskLayout;
        TextView mCName;
        TextView mTName;
        TextView mMsg;
        TextView mAnswer;
        TextView mAskTime;
        TextView mAnswerTime;
    }
}
