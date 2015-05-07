
package com.leo.appmaster.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import com.leo.appmaster.utils.DipPixelUtil;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;

public class LeoPopMenu {

    public final static int DIRECTION_DOWN = 1;
    public final static float SMALLWidth = 160.0f;
    public final static float FITWidth = 230.0f;

    public final static float OVERPX = 220.0f;
    public static boolean isOverWidth = false;

    public static class LayoutStyles {
        public int width;
        public int height;
        public int animation;
        public int direction;
    }

    private PopupWindow mLeoPopMenu;

    private List<String> mItems;

    private OnItemClickListener mPopItemClickListener;

    private MenuListAdapter mAdapter;
    private LayoutStyles mStyles = new LayoutStyles();
    private boolean mIsItemHTMLFormatted = false;

    private int mAnimaStyle = -1;

    /**
     * @param aContext
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

        if (styles == null) {
            float popWidth = 0;
            if (!isOverWidth) {
                popWidth = SMALLWidth;
            } else {
                popWidth = FITWidth;
            }
            mStyles.width = DipPixelUtil.dip2px((Context) activity, popWidth);
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

        View convertView = buildTabListLayout();

        mLeoPopMenu = new PopupWindow(convertView, mStyles.width, mStyles.height, true);
        mLeoPopMenu.setFocusable(true);
        mLeoPopMenu.setOutsideTouchable(true);
        mLeoPopMenu.setOnDismissListener(dimissListener);
        mLeoPopMenu.setBackgroundDrawable(AppMasterApplication.getInstance()
                .getResources().getDrawable(R.drawable.popup_menu_bg));
        mLeoPopMenu.setAnimationStyle(mAnimaStyle);
        mLeoPopMenu.update();

        mLeoPopMenu.setAnimationStyle(mStyles.animation);
        // mLeoPopMenu.showAsDropDown(anchorView, 0, 0);
        if (mStyles != null && mStyles.direction == DIRECTION_DOWN) {
            mLeoPopMenu.showAtLocation(anchorView, Gravity.NO_GRAVITY, 0, 0);
        } else {
            mLeoPopMenu.showAsDropDown(anchorView, 50, 0);
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
            mLeoPopMenu.dismiss();
            mLeoPopMenu = null;
        }
    }

    private View buildTabListLayout() {
        LayoutInflater inflater = LayoutInflater.from(AppMasterApplication
                .getInstance());
        LinearLayout convertView = (LinearLayout) inflater.inflate(
                R.layout.popmenu_window_list_layout, null);
        ListView listView = (ListView) convertView.findViewById(R.id.menu_list);

        listView.setOnItemClickListener(mPopItemClickListener);

        mAdapter = new MenuListAdapter(mItems);
        listView.setAdapter(mAdapter);
        return convertView;
    }

    public void setPopMenuItems(Context mContext, List<String> items) {
        mItems = items;

        float mMaxLength = 0;
        TextView testTextView = new TextView(mContext);
        for (int i = 0; i < mItems.size(); i++) {
            testTextView.setText(mItems.get(i));
            float mOne = getTextViewLength(testTextView, mItems.get(i));
            if (mOne > mMaxLength) {
                mMaxLength = mOne;
            }
        }
        if (mMaxLength > OVERPX) {
            isOverWidth = true;
        } else {
            isOverWidth = false;
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
    }

    private class MenuListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<String> mListItems;
        private Holder mHolder;

        private MenuListAdapter(List<String> itemList) {
            mListItems = itemList;
            inflater = LayoutInflater.from(AppMasterApplication.getInstance());
        }

        @Override
        public int getCount() {
            if (mListItems != null) {
                return mListItems.size();
            } else {
                return 0;
            }

        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            if (convertView != null) {
                mHolder = (Holder) convertView.getTag();
            } else {
                mHolder = new Holder();
                convertView = inflater.inflate(
                        R.layout.popmenu_window_list_item, null);
                mHolder.mItemName = (TextView) convertView
                        .findViewById(R.id.menu_text);
                convertView.setTag(mHolder);
            }

            if (mIsItemHTMLFormatted) {
                Spanned itemText = Html.fromHtml(mListItems.get(position));
                mHolder.mItemName.setText(itemText);
            } else {
                mHolder.mItemName.setText(mListItems.get(position));
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

}
