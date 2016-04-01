package com.leo.appmaster.videohide;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.imagehide.NewAdaper;
import com.leo.imageloader.ImageLoader;

/**
 * Created by Jasper on 2015/10/16.
 */
public class NewVidAdapter extends NewAdaper<VideoItemBean> {
    private ImageLoader mImageLoader;

    public NewVidAdapter() {
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PrivacyNewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pri_pro_new_video_item, null);

            holder = new PrivacyNewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.pp_video_iv);
            holder.title = (TextView) convertView.findViewById(R.id.pp_video_title_tv);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.pp_video_item_cb);

            convertView.setTag(holder);
        } else {
            holder = (PrivacyNewHolder) convertView.getTag();
        }

        final VideoItemBean info = (VideoItemBean) getItem(position);

        String url = "voidefile://" + info.getPath();
        mImageLoader.displayImage(url, holder.imageView, getMediaOptions());
        holder.title.setText(info.getName());

        if (isChecked(position)) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setClickable(false);

//        RippleView1 view = (RippleView1) convertView;
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View rippleView) {
//                toggle(info);
//            }
//        });
//
//        if (view.getParent() instanceof AdapterView) {
//            final AdapterView adapterView = (AdapterView) view.getParent();
//            final long id = adapterView.getItemIdAtPosition(position);
//            if (adapterView.getOnItemClickListener() != null)
//                adapterView.getOnItemClickListener().onItemClick(adapterView, view, position, id);
//        }

        mItemsView.put(convertView, info);
        return convertView;
    }

}
