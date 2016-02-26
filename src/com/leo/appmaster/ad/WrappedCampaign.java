package com.leo.appmaster.ad;

import com.mobvista.sdk.m.core.entity.Campaign;

/**
 * Created by stone on 16/2/26.
 */
public class WrappedCampaign {

    private String mTitle = "";
    private String mDescription = "";
    private String mIconUrl = "";
    private String mPreviewUrl = "";
    private String mCta = "";

    private WrappedCampaign () {}

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    public String getCTA() {
        return mCta;
    }

    private static WrappedCampaign createCampaign (String title, String desc,
                                           String iconUrl, String previewUrl, String cta) {
        WrappedCampaign campaign = new WrappedCampaign();
        campaign.mTitle = title;
        campaign.mDescription = desc;
        campaign.mIconUrl = iconUrl;
        campaign.mPreviewUrl = previewUrl;
        campaign.mCta = cta;
        return campaign;
    }

    public static WrappedCampaign fromMaxSDK (LEONativeAdData data) {
        return createCampaign(data.getAppName(), data.getAppDesc(),
                data.getIconUrl(), data.getImageUrl(), data.getAdCall());
    }

    public static WrappedCampaign fromMabVistaSDK (Campaign campaign) {
        return createCampaign(campaign.getAppName(), campaign.getAppDesc(),
                campaign.getIconUrl(), campaign.getImageUrl(), campaign.getAdCall());
    }

}
