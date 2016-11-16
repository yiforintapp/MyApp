package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.model.WordChatItem;
import com.zlf.appmaster.utils.TimeUtil;
import com.zlf.imageloader.DisplayImageOptions;
import com.zlf.imageloader.ImageLoader;
import com.zlf.imageloader.core.FadeInBitmapDisplayer;
import com.zlf.imageloader.core.ImageScaleType;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Administrator on 2016/11/15.
 */
public class WordPointFragmentAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<WordChatItem> mList;
    private DisplayImageOptions commonOption;
    private BitmapFactory.Options options;


    public WordPointFragmentAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<WordChatItem>();

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
    public View getView(final int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;
        final WordChatItem wordChatItem;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.word_point_item, null);
            holder = new ViewHolder();
            holder.mMsg = (TextView) convertView.findViewById(R.id.content);
            holder.mTime = (TextView) convertView.findViewById(R.id.time);
            holder.mIv = (PhotoView) convertView.findViewById(R.id.img);
            holder.mIvLayout = (RelativeLayout) convertView.findViewById(R.id.img_layout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        wordChatItem = mList.get(position);

        holder.mMsg.setText(wordChatItem.getAnswer());
        if (!TextUtils.isEmpty(wordChatItem.getAnswerTime())) {
            holder.mTime.setText(TimeUtil.getSimpleTime(Long.parseLong(wordChatItem.getAnswerTime())));
        }
        if (TextUtils.isEmpty(wordChatItem.getAnswerImg())) {
            holder.mIvLayout.setVisibility(View.GONE);
        } else {
            ImageLoader.getInstance().displayImage(Constants.ZHIBO_IMG_DOMAIN.concat(wordChatItem.getAnswerImg()),
                    holder.mIv, commonOption);
            holder.mIvLayout.setVisibility(View.VISIBLE);
        }

        holder.mIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WordLiveActivity)mContext).showImageScaleDialog(
                        Constants.ZHIBO_IMG_DOMAIN.concat(wordChatItem.getAnswerImg()));
            }
        });

        return convertView;
    }

    class ViewHolder {
        TextView mName;
        TextView mMsg;
        TextView mTime;
        RelativeLayout mIvLayout;
        PhotoView mIv;
    }

}
