
package com.leo.appmaster.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 语言工具类
 * 
 */
/*
 * 从右到左显示的语言
 */
public class LanguageUtils {
    public static String[] mLanguageFliter = {
            "ar"
    };

    // 获取从右到左显示的语言集合
    public static final List<String> getRightToLeftLanguage() {
        if (mLanguageFliter.length > 0) {
            return Arrays.asList(mLanguageFliter);
        }
        return null;
    }

    // 判断当前系统语言或传入语言是否为从右到左显示的语言
    public static final boolean isRightToLeftLanguage(String language) {
/*        if(true){
        return true;
        }*/
        if (!Utilities.isEmpty(language)) {
            if (getRightToLeftLanguage() != null) {
                if (getRightToLeftLanguage().contains(language)) {
                    return true;
                }
            }
        } else {
            if (getRightToLeftLanguage() != null) {
                if (getRightToLeftLanguage().contains(Locale.getDefault().getLanguage())) {
                    return true;
                }
            }
        }
        return false;
    }
}
