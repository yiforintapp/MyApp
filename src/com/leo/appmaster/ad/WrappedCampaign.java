package com.leo.appmaster.ad;

import com.mobvista.sdk.m.core.entity.Campaign;

/**
 * Created by stone on 16/2/26.
 */
public class WrappedCampaign {
	
	public static final int FB_TYPE = 1;
	public static final int MOB_MAX_TYPE = 2;

    private String mTitle = "";
    private String mDescription = "";
    private String mIconUrl = "";
    private String mPreviewUrl = "";
    private String mCta = "";
	
	private int type = 0;

    private WrappedCampaign () {}

    public String getAppName() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public String getImageUrl() {
        return mPreviewUrl;
    }

    public String getAdCall() {
        return mCta;
    }

	public int getType() {
		return type;
	}

	public void setType(int t) {
		this.type = t;
	}

	private static WrappedCampaign createCampaign (String title, String desc,
                                           String iconUrl, String previewUrl, String cta, int t) {
        WrappedCampaign campaign = new WrappedCampaign();
        campaign.mTitle = title;
        campaign.mDescription = desc;
        campaign.mIconUrl = iconUrl;
        campaign.mPreviewUrl = previewUrl;
        campaign.mCta = cta;
		campaign.type = t; 
        return campaign;
    }

    public static WrappedCampaign fromMaxSDK (LEONativeAdData data) {
        return createCampaign(data.getAppName(), data.getAppDesc(),
                data.getIconUrl(), data.getImageUrl(), data.getAdCall(), 0);
    }
	
    public static WrappedCampaign fromMabVistaSDK (Campaign campaign) {
        return createCampaign(campaign.getAppName(), campaign.getAppDesc(),
                campaign.getIconUrl(), campaign.getImageUrl(), campaign.getAdCall(), campaign.getType());
    }

}
