package com.leo.appmaster.privacy;

import com.leo.appmaster.privacy.PrivacyHelper.Level;

public class PrivacyVariable {
    
    public int privacyCount;
    
    public int totalCount;
    
    public int levelOnePercent;
    public int levelTwoPercent;
    public int levelThreePercent;
    public int levelFourPercent;
    public int levelFivePercent;
    
    public boolean isProtected() {
        return privacyCount > 0;
    }
    
    public boolean matchLevel(Level level) {
        int privacyPercent = 0;
        if(totalCount > 0) {
            privacyPercent = (privacyCount * 100) / totalCount;
        }
        switch (level) {
            case LEVEL_ONE:                
                return privacyPercent >= levelOnePercent;
            case LEVEL_TWO:
                return privacyPercent >= levelTwoPercent;
            case LEVEL_THREE:
                return privacyPercent >= levelThreePercent;
            case LEVEL_FOUR:
                return privacyPercent >= levelFourPercent;
            case LEVEL_FIVE:
                return privacyPercent >= levelFivePercent;
        }
        return false;
    }

}
