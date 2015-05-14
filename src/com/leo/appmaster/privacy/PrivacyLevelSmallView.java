package com.leo.appmaster.privacy;

import com.leo.appmaster.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

public class PrivacyLevelSmallView extends PrivacyLevelView {
    
    public PrivacyLevelSmallView(Context context) {
        this(context, null);
    }

    public PrivacyLevelSmallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrivacyLevelSmallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    protected int getDrawPadding(Resources res) {
        return res.getDimensionPixelSize(R.dimen.privacy_proposal_padding);
    }
    
    protected int getSepratorPadding(Resources res) {
        return res.getDimensionPixelSize(R.dimen.privacy_proposal_seprator_padding);
    }
    
    protected int getSmallTextSize(Resources res) {
        return res.getDimensionPixelSize(R.dimen.privacy_proposal_small_text_size);
    }
    
    protected int getBigTextSize(Resources res) {
        return res.getDimensionPixelSize(R.dimen.privacy_proposal_big_text_size);
    }
    
    protected boolean hasNoAnmation() {
        return true;
    }
    
}
