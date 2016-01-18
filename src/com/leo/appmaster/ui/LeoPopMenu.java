
package com.leo.appmaster.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

public class LeoPopMenu {

    public final static int DIRECTION_DOWN = 1;
    // public final static float SMALLWidth = 160.0f;
    public final static float SMALLWidth = 160.0f;
    public final static float LongWidth = 230.0f;

    public final static float OVERPX = 220.0f;
    public static boolean isOverWidth = false;

    protected static float finalWidth;
    protected static boolean mIsNewLine = true;

    protected static float newSmallWidth;
    protected static float newLongWidth;
    protected int mIconOffest = 0;
    protected Context mContext;

    public static class LayoutStyles {
        public int width;
        public int height;
        public int animation;
        public int direction;
    }

    protected PopupWindow mLeoPopMenu;

    protected List<String> mItems;
    protected List<Integer> mIcons;
    protected List<Integer> mItemIds;

    protected OnItemClickListener mPopItemClickListener;

    protected ListView mListView;
    protected BaseAdapter mAdapter;
    protected LayoutStyles mStyles = new LayoutStyles();
    protected boolean mIsItemHTMLFormatted = false;

    protected int mAnimaStyle = -1;

    /**
     * @param anchorView 显示tab列表
     */
    public void showPopMenu(Activity activity, View anchorView, LayoutStyles styles,
                            OnDismissListener dimissListener) {
        if (mLeoPopMenu != null) {
            if (mLeoPopMenu.isShowing()) {
                return;
            }
            mLeoPopMenu = null;
        }

        setWindowStyle(styles);

        View convertView = buildTabListLayout();

        mLeoPopMenu = new PopupWindow(convertView, mStyles.width, mStyles.height, true);
        // mLeoPopMenu = new PopupWindow(mContext);
        // mLeoPopMenu.setContentView(convertView);
        // mLeoPopMenu.setHeight(mStyles.height);

        // mLeoPopMenu.setWidth((int) (finalWidth+DipPixelUtil.dip2px(mContext,
        // 68)));
        // mLeoPopMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.e("hehe", finalWidth + "final///" + mLeoPopMenu.getWidth() + " true w");

        // mLeoPopMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // mLeoPopMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

        mLeoPopMenu.setFocusable(true);
        mLeoPopMenu.setOutsideTouchable(true);
        mLeoPopMenu.setBackgroundDrawable(AppMasterApplication.getInstance()
                .getResources().getDrawable(R.drawable.transparent));
        mLeoPopMenu.setOnDismissListener(dimissListener);

        mLeoPopMenu.setAnimationStyle(mAnimaStyle);
        mLeoPopMenu.update();

        mLeoPopMenu.setAnimationStyle(mStyles.animation);
        // mLeoPopMenu.showAsDropDown(anchorView, 0, 0);
        try {
            if (mStyles != null && mStyles.direction == DIRECTION_DOWN) {
                mLeoPopMenu.showAtLocation(anchorView, Gravity.NO_GRAVITY, 0, 0);
            } else {
                mLeoPopMenu.showAsDropDown(anchorView, 50, 0);
            }
        } catch (Exception e) {
        }

    }

    public void dimissPop() {
        if (mLeoPopMenu != null) {
            if (mLeoPopMenu.isShowing()) {
                mLeoPopMenu.dismiss();
            }
            mLeoPopMenu = null;
        }
    }

    private void setWindowStyle(LayoutStyles styles) {
        if (styles == null) {
            float popWidth = 0;
            if (!isOverWidth) {
                LeoLog.d("LeoPopMenu", "newSmallWidth is : " + newSmallWidth);
                newSmallWidth += DipPixelUtil.dip2px(mContext, mIconOffest);
                popWidth = newSmallWidth;
            } else {
                LeoLog.d("LeoPopMenu", "newLongWidth is : " + newLongWidth);
                newLongWidth += DipPixelUtil.dip2px(mContext, mIconOffest);
                popWidth = newLongWidth;
            }
            LeoLog.d("LeoPopMenu", "popWidth is : " + popWidth + " mIconOffest = " + mIconOffest);

            // mStyles.width = DipPixelUtil.dip2px((Context) mContext,
            // popWidth);

            mStyles.width = (int) (finalWidth + DipPixelUtil.dip2px(mContext, 68));

            // LeoLog.d("LeoPopMenu", "dip2px popWidth is : " + mStyles.width);
            mStyles.height = LayoutParams.WRAP_CONTENT;
            if (mAnimaStyle != -1) {
                mStyles.animation = mAnimaStyle;
            } else {
                mStyles.animation = R.style.PopupListAnimUpDown;
            }
        } else {
            mStyles.width = styles.width;
            mStyles.height = styles.height;
            mStyles.animation = styles.animation;
            mStyles.direction = styles.direction;
        }
    }

    public void setAnimation(int animaStyle) {
        mAnimaStyle = animaStyle;
    }

    /**
     * call this to set mIsSpanedItem true when your item is HTML style format
     * string
     */
    public void setItemSpaned(boolean flag) {
        mIsItemHTMLFormatted = flag;
    }

    public void setOnDismiss(OnDismissListener l) {
        if (mLeoPopMenu != null) {
            mLeoPopMenu.setOnDismissListener(l);
        }
    }

    public void dismissSnapshotList() {
        if (mLeoPopMenu != null) {
            try {
                mLeoPopMenu.dismiss();
            } catch (Exception e) {
            }
            mLeoPopMenu = null;
        }
    }

    private View buildTabListLayout() {
        LayoutInflater inflater = LayoutInflater.from(AppMasterApplication
                .getInstance());
        LinearLayout convertView = (LinearLayout) inflater.inflate(
                R.layout.popmenu_window_list_layout, null);
        mListView = (ListView) convertView.findViewById(R.id.menu_list);

        mListView.setOnItemClickListener(mPopItemClickListener);
        if (null == mAdapter) {
            mAdapter = new MenuListAdapter();
        }
        mListView.setAdapter(mAdapter);

        return convertView;
    }

    public void setPopMenuItems(Context context, Map<Integer, String> map) {
        if (null == map)
            return;
        List<Integer> itemIdList = new ArrayList<Integer>();
        List<String> itemList = new ArrayList<String>();
        for (Entry<Integer, String> entry : map.entrySet()) {
            itemIdList.add(entry.getKey());
            itemList.add(entry.getValue());
        }
        this.mItemIds = itemIdList;
        setPopMenuItems(context, itemList);
    }

    public List<Integer> getPopMenuItemIds() {
        return mItemIds;
    }

    public void setPopMenuItems(Context context, List<String> items) {
        mItems = items;
        mContext = context;

        Display mDisplay = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        int W = mDisplay.getWidth();
        int H = mDisplay.getHeight();
        LeoLog.d("LeoPopMenu", "Width = " + W);
        LeoLog.d("LeoPopMenu", "Height = " + H);

        float mMaxLength = 0;
        int MaxIndex = 0;
        TextView testTextView = (TextView) View.inflate(mContext,
                R.layout.popmenu_window_home_list_item, null).findViewById(R.id.menu_text);
        // TextView testTextView = new TextView(mContext);
        for (int i = 0; i < mItems.size(); i++) {
            testTextView.setText(mItems.get(i));
            float mOne = getTextViewLength(testTextView, mItems.get(i));
            LeoLog.d("LeoPopMenu", "字符：" + mItems.get(i) + "...长度：" + mOne);
            if (mOne > mMaxLength) {
                mMaxLength = mOne;
                MaxIndex = i;
            }
        }

        Log.e("hehe", "最长字符占的宽度px=" + mMaxLength);

        newSmallWidth = W / 4;
        newLongWidth = W / 2;
        finalWidth = mMaxLength + DipPixelUtil.dip2px(mContext, 7);

        if (mMaxLength > newLongWidth) {
            mIsNewLine = true;

            finalWidth = newLongWidth;

            // 判断第二长的item，然后再取出最长的单词长度，取两者的更大的那个值，最后还是加上7dp

            mMaxLength = 0;
            // 取出第二长
            for (int i = 0; i < mItems.size(); i++) {
                if (i == MaxIndex) {
                    continue;
                }
                testTextView.setText(mItems.get(i));
                float mOne = getTextViewLength(testTextView, mItems.get(i));
                if (mOne > mMaxLength) {
                    mMaxLength = mOne;
                }
            }
            finalWidth = mMaxLength + DipPixelUtil.dip2px(mContext, 7);

        }
        if (mMaxLength < newSmallWidth) {
            finalWidth = newSmallWidth;
        }


    }

    public List<String> getPopMenuItems() {
        return mItems;
    }

    public void setOnDismissListener() {

    }

    public void setPopItemClickListener(OnItemClickListener listener) {
        mPopItemClickListener = listener;
    }

    static class Holder {
        public TextView mItemName;
        public ImageView mItemIcon;
    }

    private class MenuListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private Holder mHolder;

        private MenuListAdapter() {
            inflater = LayoutInflater.from(AppMasterApplication.getInstance());
        }

        @Override
        public int getCount() {
            if (mItems != null) {
                return mItems.size();
            } else {
                return 0;
            }

        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (convertView != null) {
                mHolder = (Holder) convertView.getTag();
            } else {
                mHolder = new Holder();
                convertView = inflater.inflate(R.layout.popmenu_window_home_list_item, null);
                mHolder.mItemName = (TextView) convertView.findViewById(R.id.menu_text);
                mHolder.mItemIcon = (ImageView) convertView.findViewById(R.id.menu_icon);
                mHolder.mItemName.setWidth((int) finalWidth + 1);
                if (!mIsNewLine) {
                    mHolder.mItemName.setMaxLines(1);
                }
                convertView.setTag(mHolder);
            }

            if (mIsItemHTMLFormatted) {
                Spanned itemText = Html.fromHtml(mItems.get(position));
                mHolder.mItemName.setText(itemText);
            } else {
                mHolder.mItemName.setText(mItems.get(position));
                if (mIcons == null || mIcons.size() <= 0) {
                    mHolder.mItemIcon.setVisibility(View.GONE);
                }
            }
            return convertView;
        }
    }

    public static float getTextViewLength(TextView textView, String text) {
        TextPaint paint = textView.getPaint();
        // 得到使用该paint写上text的时候,像素为多少
        float textLength = paint.measureText(text);
        return textLength;
    }

    public void setListViewDivider(Drawable divider) {
        if (null != mListView) {
            mListView.setDivider(divider);
        }
    }

}
