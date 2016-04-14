package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppInfo;


public class ListLockItem extends RelativeLayout {
    private ImageView iconview, lockview;
    private TextView apptitle, applocknums;
    private AppInfo mInfo;

    public ListLockItem(Context context) {
        super(context);
    }

    public ListLockItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListLockItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        iconview = (ImageView)
                findViewById(R.id.lock_app_icon);
        lockview = (ImageView)
                findViewById(R.id.lock_app_check);
        apptitle = (TextView)
                findViewById(R.id.lock_app_title);
        applocknums = (TextView)
                findViewById(R.id.lock_app_nums);
    }

    public void setIcon(Drawable icon) {
        Context ctx = AppMasterApplication.getInstance();
        if (icon == null && mInfo != null) {
            // 加保护icon为空，先动态获取
            try {
                icon = ctx.getPackageManager().getApplicationIcon(mInfo.packageName);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        iconview.setImageDrawable(icon);
    }

    public void setLockView(boolean isLock) {
        if (isLock) {
            lockview.setImageResource(R.drawable.lock_list_icon);
        } else {
            lockview.setImageResource(R.drawable.unlock_icon);
        }
    }

    public void setTitle(String title) {
        apptitle.setText(title);
    }

    public void setDesc(AppInfo info, boolean setLock) {
        if (info != null && applocknums != null) {
            int mTop = info.topPos;
            Context context = getContext();
            if (mTop != -1) {
                String text;
                if (setLock) {
                    text = context.getString(R.string.have_added_lock);
                } else {
                    String string = makePosRight(info);
                    text = context.getString(R.string.lock_app_item_desc_cb_color, string);
                }
                if (text != null) {
                    applocknums.setText(text);
                }
            } else {
                if (setLock) {
                    applocknums.setText(context.getString(R.string.have_added_lock));
                } else {
//                    applocknums.setText(context.getString(R.string.advised_add_lock));
                    applocknums.setText("");
                }
            }
        }
    }

    public void setDescEx(AppInfo info, boolean setLock) {
        if (info != null && applocknums != null) {
            int mTop = info.topPos;
            Context context = getContext();
            if (mTop != -1) {
                String text;
                if (setLock) {
                    text = context.getString(R.string.have_added_lock);
                    applocknums.setText(text);
                } else {
                    String string = makePosRight(info);
                    text = context.getString(R.string.lock_app_item_desc_cb_color, string);
                    applocknums.setText(Html.fromHtml(text));
                }
            } else {
                if (setLock) {
                    applocknums.setText(context.getString(R.string.have_added_lock));
                } else {
                    if (info.isRecomment) {
                        applocknums.setText(context.getString(R.string.advised_add_lock));
                    } else {
                        applocknums.setText("");
                    }
                }
            }
        }
    }

    private String makePosRight(AppInfo info) {
        int topPos = fixPosEqules(info);
//        int topPos = info.topPos;
        String tops;
        if (topPos > 0 && topPos < 5000) {
            tops = "1000+";
        } else if (topPos >= 5000 && topPos < 10000) {
            tops = "5000+";
        } else if (topPos >= 10000 && topPos < 50000) {
            tops = "10000+";
        } else if (topPos >= 50000 && topPos < 100000) {
            tops = "50000+";
        } else if (topPos >= 100000 && topPos < 500000) {
            tops = "100000+";
        } else if (topPos >= 500000 && topPos < 1000000) {
            tops = "500000+";
        } else if (topPos >= 1000000 && topPos < 5000000) {
            tops = "1000000+";
        } else if (topPos >= 5000000 && topPos < 10000000) {
            tops = "5000000+";
        } else {
            tops = "10000000+";
        }
        return tops;
    }

    private int fixPosEqules(AppInfo info) {
        int topPosGet = info.topPos;
        String pckName = info.packageName;

        String[] strings = AppLoadEngine.sLocalLockArray;
        int k = 0;
        boolean isHavePckName = false;
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string.equals(pckName)) {
                k = i;
                isHavePckName = true;
                break;
            }
        }

        if (isHavePckName) {
            String[] nums = AppLoadEngine.sLocalLockNumArray;
            int num = Integer.parseInt(nums[k]);
            if (num > topPosGet) {
                return num;
            } else {
                return topPosGet;
            }
        } else {
            if (topPosGet != -1) {
                if (topPosGet == 0) {
                    return 1000;
                } else {
                    return topPosGet;
                }
            } else {
                return topPosGet;
            }
        }
    }

    public SpannableStringBuilder setSpecifiedTextsColor(String text, String specifiedTexts, int color) {
        List<Integer> sTextsStartList = new ArrayList<Integer>();

        int sTextLength = specifiedTexts.length();
        String temp = text;
        int lengthFront = 0;//记录被找出后前面的字段的长度
        int start = -1;
        do {
            start = temp.indexOf(specifiedTexts);

            if (start != -1) {
                start = start + lengthFront;
                sTextsStartList.add(start);
                lengthFront = start + sTextLength;
                temp = text.substring(lengthFront);
            }

        } while (start != -1);

        SpannableStringBuilder styledText = new SpannableStringBuilder(text);
        for (Integer i : sTextsStartList) {
            styledText.setSpan(
                    new ForegroundColorSpan(color),
                    i,
                    i + sTextLength,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return styledText;
    }


    public void setDefaultRecommendApp(boolean isLocked) {
        if (isLocked) {
            lockview.setImageResource(R.drawable.select2_icon);
        } else {
            lockview.setImageResource(R.drawable.unselect_icon);
        }
    }

    public void setInfo(AppInfo info) {
        this.mInfo = info;
    }

    public AppInfo getInfo() {
        return mInfo;
    }

    public void setText(CharSequence text) {
        if (text != null && applocknums != null) {
            applocknums.setText(text);
        }
    }
}
