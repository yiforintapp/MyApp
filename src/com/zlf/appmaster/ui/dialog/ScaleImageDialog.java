package com.zlf.appmaster.ui.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.zlf.appmaster.R;
import com.zlf.imageloader.DisplayImageOptions;
import com.zlf.imageloader.ImageLoader;
import com.zlf.imageloader.core.FadeInBitmapDisplayer;
import com.zlf.imageloader.core.ImageScaleType;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Administrator on 2016/11/16.
 */
public class ScaleImageDialog extends LEOBaseDialog {

    private ImageView mImage;
    private DisplayImageOptions commonOption;
    private BitmapFactory.Options options;

    private String mImgUrl;

    public ScaleImageDialog(Context context) {
        super(context, R.style.LoginProgressDialog);
        View dlgView = LayoutInflater.from(context.getApplicationContext()).inflate(
                R.layout.dialog_image_scale, null);
        setContentView(dlgView);
        mImage = (PhotoView) findViewById(R.id.image);
        mImage.setImageDrawable(context.getResources().getDrawable(R.drawable.new_page_one));
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
//        ViewGroup.LayoutParams layoutParams = mImage.getLayoutParams();
//        layoutParams.width = Utilities.getScreenSize(context)[0]; //设置图片的宽度
//        mImage.setLayoutParams(layoutParams);
    }

    public void setImgUrl(String url) {
        this.mImgUrl = url;
        ImageLoader.getInstance().displayImage(mImgUrl, mImage, commonOption);
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

}
