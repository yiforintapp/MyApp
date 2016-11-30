package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.model.WordMyAskItem;
import com.zlf.appmaster.utils.TimeUtil;
import com.zlf.imageloader.DisplayImageOptions;
import com.zlf.imageloader.ImageLoader;
import com.zlf.imageloader.core.FadeInBitmapDisplayer;
import com.zlf.imageloader.core.ImageScaleType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/3.
 */
public class WordZhiboFragmentAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private List<WordMyAskItem> mList;
    private DisplayImageOptions commonOption;
    private BitmapFactory.Options options;


    public WordZhiboFragmentAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<WordMyAskItem>();

        options = new BitmapFactory.Options();
        // 主题使用565配置
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (commonOption == null) {
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
        }
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
        WordMyAskItem wordChatItem;
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
            holder.mAdminIv = (ImageView) convertView.findViewById(R.id.admin_iv);

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
            holder.mAskTime.setText(mContext.getResources().getString(
                    R.string.word_ask_time, TimeUtil.getSimpleTime(Long.parseLong(wordChatItem.getAskTime()))));
        }
        if (!TextUtils.isEmpty(wordChatItem.getAnswerTime())) {
            holder.mAnswerTime.setText(mContext.getResources().getString(
                    R.string.word_answer_time, TimeUtil.getSimpleTime(Long.parseLong(wordChatItem.getAnswerTime()))));
        }

        if (!TextUtils.isEmpty(wordChatItem.getAnswerHeadImg())) {
            ImageLoader.getInstance().displayImage(Constants.ZHIBO_ADMIN_IMG_DOMAIN.concat(wordChatItem.getAnswerHeadImg()),
                    holder.mAdminIv, commonOption);
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
        ImageView mAdminIv;
    }
}
