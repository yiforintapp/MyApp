
package com.leo.appmaster.lockertheme;

import java.util.List;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private int mLocalOrOnline = 1;
    private Campaign mCampaign;
    private Context mContext;

    private Options options;

    public LockerThemeAdapter(Context context, List<ThemeItemInfo> themes) {
        mContext = context;
        this.themes = themes;
        this.layoutInflater = LayoutInflater.from(context);

        options = new Options();
        // 主题使用565配置
        options.inPreferredConfig = Config.RGB_565;
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

    @Override
    public int getCount() {
        return themes != null ? themes.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (themes.get(position).themeType == Constants.THEME_TYPE_ONLINE) {
            if (themes.get(position).packageName.equals(LockerTheme.ONLINE_AD_NAME) && isGetAd) {
                return ADTYPE;
            } else {
                return NORMALTYEP;
            }
        } else if (themes.get(position).themeType == Constants.THEME_TYPE_LOCAL) {
            if (themes.get(position).packageName.equals(LockerTheme.LOCAL_AD_NAME) && isGetAd) {
                return ADTYPE;
            } else {
                return NORMALTYEP;
            }
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
        TextView ad_title, ad_desc, ad_download;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        View viewItem1 = null;
        View viewItem2 = null;
        int type = getItemViewType(position);

        if (mCampaign == null) {
            convertView = showNormalTypeItem(position, convertView, viewItem1);
        } else {
            switch (type) {
                case NORMALTYEP:
                    convertView = showNormalTypeItem(position, convertView, viewItem1);
                    break;
                case ADTYPE:
                    convertView = showAdType(position, convertView, viewItem2);
                    break;
            }
        }

        return convertView;
    }

    private View showAdType(int position, View convertView, View viewItem2) {
        AdHolder adHolder;

        if (convertView != null && convertView.getTag() instanceof AdHolder) {
            LeoLog.d("testGetView", "convertView != null");
            adHolder = (AdHolder) convertView.getTag();
        } else {
            LeoLog.d("testGetView", "convertView == null");
            adHolder = new AdHolder();
            viewItem2 = layoutInflater.inflate(R.layout.ad_list_item_lockerthem, null);

            adHolder.ad_icon = (ImageView) viewItem2
                    .findViewById(R.id.ad_theme_icon);
            adHolder.ad_background = (ImageView) viewItem2
                    .findViewById(R.id.theme_preview);
            adHolder.ad_title = (TextView) viewItem2
                    .findViewById(R.id.ad_title_theme);
            adHolder.ad_desc = (TextView) viewItem2
                    .findViewById(R.id.ad_desc_theme);
            adHolder.ad_download = (TextView) viewItem2
                    .findViewById(R.id.ad_download_theme);

            viewItem2.setTag(adHolder);
            convertView = viewItem2;
        }

        // icon
        loadADPic(
                mCampaign.getIconUrl(),
                new ImageSize(DipPixelUtil.dip2px(mContext, 44),
                        DipPixelUtil.dip2px(mContext, 44)), adHolder.ad_icon);
        // bg
        loadADPic(mCampaign.getImageUrl(),
                new ImageSize(300,
                        DipPixelUtil.dip2px(mContext, 170)), adHolder.ad_background);
        // title
        adHolder.ad_title.setText(mCampaign.getAppName());
        // desc
        adHolder.ad_desc.setText(mCampaign.getAppDesc());
        // download
        adHolder.ad_download.setText(mCampaign.getAdCall());

        adHolder.ad_background.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // LeoLog.d("testclickview", "点击广告，timeFilterSelf !!");
                // LockManager.getInstatnce().timeFilterSelf();
            }
        });

        LockerTheme activity = (LockerTheme) mContext;
        activity.regisClickView(adHolder.ad_background);

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
            compatibleOption = new DisplayImageOptions.Builder()
                    .showImageOnLoading(theme.themeImage)
                    .showImageOnFail(theme.themeImage).cacheInMemory(true)
                    .displayer(new FadeInBitmapDisplayer(500))
                    .decodingOptions(options)
                    .cacheOnDisk(true).considerExifParams(true).build();
            if (Constants.THEME_PACKAGE_NIGHT.equals(theme.packageName)) {
                ImageLoader.getInstance().displayImage(
                        Constants.THEME_MOONNIGHT_URL, viewHolder.image,
                        compatibleOption);
            } else if (Constants.THEME_PACKAGE_CHRITMAS
                    .equals(theme.packageName)) {
                ImageLoader.getInstance().displayImage(
                        Constants.THEME_CHRISTMAS_URL, viewHolder.image,
                        compatibleOption);
            } else if (Constants.THEME_PACKAGE_FRUIT.equals(theme.packageName)) {
                ImageLoader.getInstance().displayImage(
                        Constants.THEME_FRUIT_URL, viewHolder.image,
                        compatibleOption);
            } else if (Constants.THEME_PACKAGE_SPATIAL
                    .equals(theme.packageName)) {
                ImageLoader.getInstance().displayImage(
                        Constants.THEME_SPATIAL_URL, viewHolder.image,
                        compatibleOption);
            } else {
                viewHolder.image.setImageDrawable(theme.themeImage);
            }
        }

        if (theme.curUsedTheme) {
            viewHolder.selectedView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selectedView.setVisibility(View.GONE);
        }
        return convertView;
    }

    private void loadADPic(String url, ImageSize size, final ImageView v) {
        ImageLoader.getInstance().loadImage(
                url, size, new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (loadedImage != null)
                        {
                            v.setImageBitmap(loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
    }

    public void setCampaign(Campaign campaign, boolean isGetAd) {
        if (campaign != null) {
            LeoLog.d("testAdapter", "setCampaign");
            this.isGetAd = isGetAd;
            mCampaign = campaign;
        }
    }
}
