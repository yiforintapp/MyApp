
package com.leo.appmaster.lockertheme;

import java.util.List;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.model.ThemeItemInfo;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageScaleType;
import com.leo.imageloader.core.ImageSize;
import com.mobvista.sdk.m.core.entity.Campaign;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LockerThemeAdapter extends BaseAdapter {
    private List<ThemeItemInfo> themes;
    private LayoutInflater layoutInflater;
    private DisplayImageOptions commonOption;
    private DisplayImageOptions compatibleOption;

    private final static int NORMALVIEWCOUNT = 1;
    private final static int ADVIEWCOUNT = 2;
    private final static int NORMALTYEP = 0;
    private final static int ADTYPE = 1;
    private boolean isGetAd = false;
    private Context mContext;

    private Options options;

    public LockerThemeAdapter(Context context, List<ThemeItemInfo> themes) {
        mContext = context;
        this.themes = themes;
        this.layoutInflater = LayoutInflater.from(context);

        options = new Options();
        // 主题使用565配置
        options.inPreferredConfig = Config.RGB_565;
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
        return themes != null ? themes.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (themes.get(position).themeType == Constants.THEME_TYPE_ONLINE) {
            return NORMALTYEP;
        } else if (themes.get(position).themeType == Constants.THEME_TYPE_LOCAL) {
            return NORMALTYEP;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        if (isGetAd) {
            return ADVIEWCOUNT;
        } else {
            return NORMALVIEWCOUNT;
        }
    }

    @Override
    public Object getItem(int arg0) {

        return themes != null ? themes.get(arg0) : null;
    }

    @Override
    public long getItemId(int arg0) {

        return arg0;
    }

    class ViewHolder {
        ImageView image;
        ImageView selectedView;
        TextView themeName, tag;
    }

    class AdHolder {
        ImageView ad_icon, ad_background;
        TextView ad_title, ad_desc;
        Button ad_download;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        View viewItem1 = null;
        convertView = showNormalTypeItem(position, convertView, viewItem1);

        return convertView;
    }

    private View showNormalTypeItem(int position, View convertView, View viewItem1) {
        ViewHolder viewHolder;

        if (convertView != null && convertView.getTag() instanceof ViewHolder) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            viewHolder = new ViewHolder();
            viewItem1 = layoutInflater.inflate(R.layout.list_item_lockerthem, null);
            viewHolder.themeName = (TextView) viewItem1
                    .findViewById(R.id.lockerThemName);
            viewHolder.image = (ImageView) viewItem1
                    .findViewById(R.id.theme_preview);
            viewHolder.tag = (TextView) viewItem1.findViewById(R.id.flagTV);
            viewHolder.selectedView = (ImageView) viewItem1
                    .findViewById(R.id.visibilityIV);
            viewItem1.setTag(viewHolder);
            convertView = viewItem1;
        }

        ThemeItemInfo theme = themes.get(position);

        if (theme.themeName == null || theme.themeName.equals("")) {
            viewHolder.themeName.setText("");
        } else {
            viewHolder.themeName.setText(theme.themeName);
        }
        if (theme.themeType == Constants.THEME_TYPE_ONLINE) {
            if (theme.tag == Constants.THEME_TAG_NEW) {
                viewHolder.tag.setVisibility(View.VISIBLE);
                viewHolder.tag.setBackgroundResource(R.drawable.theme_tag_new);
                viewHolder.tag.setText(R.string.theme_tag_new);
            } else if (theme.tag == Constants.THEME_TAG_HOT) {
                viewHolder.tag.setVisibility(View.VISIBLE);
                viewHolder.tag.setBackgroundResource(R.drawable.theme_tag_hot);
                viewHolder.tag.setText(R.string.theme_tag_hot);
            } else {
                viewHolder.tag.setVisibility(View.INVISIBLE);
            }

            ImageLoader.getInstance().displayImage(theme.previewUrl,
                    viewHolder.image, commonOption);

        } else {
            viewHolder.tag.setVisibility(View.INVISIBLE);
            if (compatibleOption == null) {
                compatibleOption = new DisplayImageOptions.Builder()
                        .showImageOnLoading(theme.themeImage)
                        .showImageOnFail(theme.themeImage).cacheInMemory(true)
                        .displayer(new FadeInBitmapDisplayer(500))
                        .decodingOptions(options)
                        .cacheOnDisk(true).considerExifParams(true).build();
            }
//            if (Constants.THEME_PACKAGE_NIGHT.equals(theme.packageName)) {
//                ImageLoader.getInstance().displayImage(
//                        Constants.THEME_MOONNIGHT_URL, viewHolder.image,
//                        compatibleOption);
//            } else if (Constants.THEME_PACKAGE_CHRITMAS
//                    .equals(theme.packageName)) {
//                ImageLoader.getInstance().displayImage(
//                        Constants.THEME_CHRISTMAS_URL, viewHolder.image,
//                        compatibleOption);
//            } else if (Constants.THEME_PACKAGE_FRUIT.equals(theme.packageName)) {
//                ImageLoader.getInstance().displayImage(
//                        Constants.THEME_FRUIT_URL, viewHolder.image,
//                        compatibleOption);
//            } else if (Constants.THEME_PACKAGE_SPATIAL
//                    .equals(theme.packageName)) {
//                ImageLoader.getInstance().displayImage(
//                        Constants.THEME_SPATIAL_URL, viewHolder.image,
//                        compatibleOption);
//            } else {
            viewHolder.image.setImageDrawable(theme.themeImage);
//            }
        }

        if (theme.curUsedTheme) {
            viewHolder.selectedView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selectedView.setVisibility(View.GONE);
        }
        return convertView;
    }

}
