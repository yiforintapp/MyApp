package com.leo.appmaster.imagehide;

import android.content.Context;
import android.graphics.Bitmap;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageSize;

/**
 * Created by Jasper on 2016/3/18.
 */
public class ImagePreviewUtil {

    public static DisplayImageOptions getPreviewOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.photo_bg_loding)
                .showImageForEmptyUri(R.drawable.photo_bg_loding)
                .showImageOnFail(R.drawable.photo_bg_loding)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .displayer(new FadeInBitmapDisplayer(500))
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        return options;
    }

    public static ImageSize getPreviewSize() {
        Context ctx = AppMasterApplication.getInstance();
        int size = ctx.getResources().getDimensionPixelSize(R.dimen.hide_album_image_frame_width);

        return new ImageSize(size, size);
    }
}
