
package com.leo.appmaster.ui;

import java.util.List;

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
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;

public class LeoPopMenu {

    public final static int DIRECTION_DOWN = 1;
    //public final static float SMALLWidth = 160.0f;
    public final static float SMALLWidth = 160.0f;
    public final static float LongWidth = 230.0f;

    public final static float OVERPX = 220.0f;
    public static boolean isOverWidth = false;

    private static float newSmallWidth;
    private static float newLongWidth;
    private boolean isShowIcon = false;
    
    public static class LayoutStyles {
        public int width;
        public int height;
        public int animation;
        public int direction;
    }

    private PopupWindow mLeoPopMenu;

    private List<String> mItems;
    private List<Integer> mIcons;

    private OnItemClickListener mPopItemClickListener;

    private ListView listView;
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
                if(isShowIcon){
                    newSmallWidth +=DipPixelUtil.dip2px(activity, 12f);
                }
                popWidth = newSmallWidth;
            } else {
                if(isShowIcon){
                    newLongWidth +=DipPixelUtil.dip2px(activity, 12f);
                }
                popWidth = newLongWidth;
            }
                Log.i("tag","popWidth="+popWidth);
            
            LeoLog.d("LeoPopMenu", "popWidth is : " + popWidth);
            mStyles.width = DipPixelUtil.dip2px((Context) activity, popWidth);
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
        listView = (ListView) convertView.findViewById(R.id.menu_list);

        listView.setOnItemClickListener(mPopItemClickListener);

        mAdapter = new MenuListAdapter(mItems);
        listView.setAdapter(mAdapter);
        return convertView;
    }
    
    public void setPopMenuItems(Context mContext, List<String> items,List<Integer> icons,boolean isShowicon){
        this.isShowIcon = isShowicon;
        mIcons = icons;
        setPopMenuItems(mContext,items);
    }

    public void setPopMenuItems(Context mContext, List<String> items) {
        mItems = items;
        
        Display mDisplay = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        int W = mDisplay.getWidth();
        int H = mDisplay.getHeight();
        Log.i("Main", "Width = " + W);
        Log.i("Main", "Height = " + H);
        LeoLog.d("LeoPopMenu", "Width = " + W);
        LeoLog.d("LeoPopMenu", "Height = " + H);
        
        
        float mMaxLength = 0;
        TextView testTextView = new TextView(mContext);
        for (int i = 0; i < mItems.size(); i++) {
            testTextView.setText(mItems.get(i));
            float mOne = getTextViewLength(testTextView, mItems.get(i));
            LeoLog.d("LeoPopMenu", "字符：" + mItems.get(i) + "...长度：" + mOne);
            if (mOne > mMaxLength) {
                mMaxLength = mOne;
            }
        }

        if (W >=1080) {
            if (mMaxLength > OVERPX) {
                isOverWidth = true;
                if(mMaxLength > 260){
                    newLongWidth = mMaxLength - 120;
                }else {
                    newLongWidth = mMaxLength - 100;
                }
                if(newLongWidth > 210){
                    newLongWidth = 210;
                }
            } else {
                isOverWidth = false;
                if (mMaxLength < SMALLWidth) {
                    newSmallWidth = mMaxLength - 20;
                } else if(mMaxLength < 180){
                    newSmallWidth = mMaxLength - 40;
                }else {
                    newSmallWidth = mMaxLength - 60;
                }
            }
        } else if(W >= 720){
            if (mMaxLength > OVERPX) {
                isOverWidth = true;
                newLongWidth = LongWidth - 20;
                if(newLongWidth > 210){
                    newLongWidth = 210;
                }
            } else {
                isOverWidth = false;
                if (mMaxLength < SMALLWidth) {
                    newSmallWidth = mMaxLength;
                } else {
                    newSmallWidth = mMaxLength - 20;
                }
            }
        } else if(W >= 480){
            if (mMaxLength > OVERPX) {
                isOverWidth = true;
                newLongWidth = LongWidth + 30;
                if(newLongWidth > 210){
                    newLongWidth = 210;
                }
            } else {
                isOverWidth = false;
                newSmallWidth = mMaxLength + 40;
            }
        }else{
            if (mMaxLength > OVERPX) {
                isOverWidth = true;
                newLongWidth = LongWidth + 50;
                if(newLongWidth > 210){
                    newLongWidth = 210;
                }
            } else {
                isOverWidth = false;
                newSmallWidth = mMaxLength + 60;
            }
        }
        //特殊处理一下只有一个item的情况
        if(items.size()==1)
        {
            newSmallWidth-=W/720*20;
            newLongWidth-=W/720*20;
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
                if(isShowIcon){
                    convertView = inflater.inflate(R.layout.popmenu_window_home_list_item, null);
                    mHolder.mItemIcon = (ImageView) convertView.findViewById(R.id.menu_icon);
                }else{
                    convertView = inflater.inflate(R.layout.popmenu_window_list_item, null);
                }
                mHolder.mItemName = (TextView) convertView .findViewById(R.id.menu_text);
                convertView.setTag(mHolder);
            }

            if (mIsItemHTMLFormatted) {
                Spanned itemText = Html.fromHtml(mListItems.get(position));
                mHolder.mItemName.setText(itemText);
            } else {
                mHolder.mItemName.setText(mListItems.get(position));
                if(isShowIcon){
                    mHolder.mItemIcon.setImageResource(mIcons.get(position));
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

    public void setListViewDivider(Drawable divider){
        if(null != listView){
            listView.setDivider(divider);
        }
    }
}
