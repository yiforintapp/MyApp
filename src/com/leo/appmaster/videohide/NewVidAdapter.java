package com.leo.appmaster.videohide;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.imagehide.NewAdaper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.ImageDownloader;

/**
 * Created by Jasper on 2015/10/16.
 */
public class NewVidAdapter extends NewAdaper<VideoItemBean> {
    private ImageLoader mImageLoader;

    public NewVidAdapter() {
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        PrivacyNewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.new_vid_hide_grid_item, null);

            holder = new PrivacyNewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image);
            holder.title = (TextView) convertView.findViewById(R.id.txt_item_picture);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.video_select);

            convertView.setTag(holder);
        } else {
            holder = (PrivacyNewHolder) convertView.getTag();
        }

        final VideoItemBean info = (VideoItemBean) getItem(position);

        String url = ImageDownloader.Scheme.VIDEOFILE.wrap(info.getPath());
        mImageLoader.displayImage(url, holder.imageView, getMediaOptions());
        holder.title.setText(info.getName());

        if (isChecked(position)) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setClickable(false);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(position);
            }
        });
        mItemsView.put(convertView, info);
        return convertView;
    }

}
