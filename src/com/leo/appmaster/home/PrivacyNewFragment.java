package com.leo.appmaster.home;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Jasper on 2015/10/16.
 */
public abstract class PrivacyNewFragment extends Fragment implements AbsListView.OnScrollListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener, PrivacyNewAdaper.SelectionChangeListener {
    private Dictionary<Integer, Integer> listViewItemHeights = new Hashtable<Integer, Integer>();
    protected HomeActivity mActivity;
    protected PrivacyNewAdaper mAdaper;

    private TextView mProcessTv;
    private MaterialRippleLayout mProcessBtn;
    private View mProcessClick;
    private MaterialRippleLayout mIgnoreBtn;
    private View mIgnoreClick;

    public View mStickView;
    private CheckBox mStickyCb;
    protected TextView mNewLabelTv;
    protected TextView mNewLabelContent;

    private View mEmptyBg;

    private int mToolbarHeight;
    protected int mEmptyHeight;
    protected int mStickyHeight;

    private LEOAlarmDialog mIgnoreDlg;

    private List<? extends Object> mDataList;

    protected String mAppName;

    public void setData(List<? extends Object> list, String text) {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (HomeActivity) activity;

        mToolbarHeight = activity.getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        mEmptyHeight = activity.getResources().getDimensionPixelSize(R.dimen.pri_pro_header);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProcessBtn = (MaterialRippleLayout) view.findViewById(R.id.pp_process_rv);
        mProcessBtn.setRippleOverlay(true);
        mProcessClick = view.findViewById(R.id.pp_process_rv_click);

        mIgnoreBtn = (MaterialRippleLayout) view.findViewById(R.id.pp_process_ignore_rv);
        mIgnoreBtn.setRippleOverlay(true);
        mIgnoreClick = view.findViewById(R.id.pp_process_ignore_rv_click);
        mProcessTv = (TextView) view.findViewById(R.id.pp_process_tv);

        mProcessClick.setOnClickListener(this);
        mIgnoreClick.setOnClickListener(this);

        mStickView = view.findViewById(R.id.pri_pro_sticky_header);
        mStickyCb = (CheckBox) view.findViewById(R.id.pri_pro_cb);
        mNewLabelTv = (TextView) view.findViewById(R.id.pri_pro_new_label_tv);
        mNewLabelContent = (TextView) view.findViewById(R.id.app_name);
        mStickyCb.setOnClickListener(this);
        mAdaper.setOnSelectionChangeListener(this);

        mEmptyBg = view.findViewById(R.id.empty_bg);

        mProcessBtn.setEnabled(false);
        mProcessClick.setEnabled(false);
        if (mAdaper != null) {
            mAdaper.setOnSelectionChangeListener(this);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        View c = view.getChildAt(0); //this is the first visible row
        if (c == null) return;

        int scrollY = -c.getTop();
        listViewItemHeights.put(view.getFirstVisiblePosition(), c.getHeight());
        for (int i = 0; i < view.getFirstVisiblePosition(); ++i) {
            if (listViewItemHeights.get(i) != null) // (this is a sanity check)
                scrollY += listViewItemHeights.get(i); //add all heights of the views that are gone
        }

        int stickyMaxScrollHeight = mEmptyHeight - mToolbarHeight;
        if (scrollY > stickyMaxScrollHeight) {
            mStickView.setTranslationY(-stickyMaxScrollHeight);
            mEmptyBg.setTranslationY(-stickyMaxScrollHeight);
        } else {
            mStickView.setTranslationY(-scrollY);
            mEmptyBg.setTranslationY(-scrollY);
        }
        mActivity.onListScroll(scrollY);
    }

    protected void onProcessClick() {

    }

    protected void onIgnoreClick(boolean direct) {
        mActivity.onIgnoreClick(0, null);
    }

    protected void setProcessContent(int stringId) {
        mProcessTv.setText(stringId);
    }

    protected void setProcessContent(String string) {
        mProcessTv.setText(string);
    }

    protected View getEmptyHeader() {
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new AbsListView.LayoutParams(1, mEmptyHeight + mStickyHeight));
        textView.setBackgroundResource(R.color.transparent);
        textView.setClickable(false);
        textView.setEnabled(false);
        textView.setWidth(1);

        return textView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mAdaper.selectAll();
        } else {
            mAdaper.deselectAll();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pri_pro_cb:
                if (mStickyCb.isChecked()) {
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", getFolderFullDesc());
                    mAdaper.selectAll();
                } else {
                    mAdaper.deselectAll();
                }
                break;
            case R.id.pp_process_rv_click:
                onProcessClick();
                break;
            case R.id.pp_process_ignore_rv_click:
//                if (mIgnoreDlg == null) {
//                    initIgnoreDlg();
//                }
//                if (mActivity.shownIgnoreDlg()) {
//                    onIgnoreClick(true);
//                } else {
//                    mIgnoreDlg.show();
//                    mActivity.setShownIngoreDlg();
//                }
                onIgnoreClick(true);
                mIgnoreClick.setEnabled(false);
                mIgnoreClick.setClickable(false);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIgnoreDlg != null && mIgnoreDlg.isShowing()) {
            mIgnoreDlg.dismiss();
            mIgnoreDlg = null;
        }
    }

    @Override
    public void onSelectionChange(boolean selectAll, int selectedCount) {
        if (selectAll) {
            mStickyCb.setChecked(true);
        } else {
            mStickyCb.setChecked(false);
        }

        if (selectedCount > 0) {
            mProcessBtn.setEnabled(true);
            mProcessClick.setEnabled(true);
            mProcessBtn.setBackgroundResource(R.drawable.green_radius_btn_shape);
        } else {
            mProcessBtn.setEnabled(false);
            mProcessClick.setEnabled(false);
            mProcessBtn.setBackgroundResource(R.drawable.green_radius_shape_disable);
        }
    }

    private void initIgnoreDlg() {
        if (mIgnoreDlg != null) return;

        mIgnoreDlg = new LEOAlarmDialog(getActivity());
        String content = getString(R.string.pri_pro_ignore_dialog);
        mIgnoreDlg.setContent(content);
        mIgnoreDlg.setCanceledOnTouchOutside(false);
        mIgnoreDlg.setLeftBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", getSkipCancelDesc());
                mIgnoreDlg.dismiss();
            }
        });
        mIgnoreDlg.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", getSkipConfirmDesc());
                onIgnoreClick(false);
                mIgnoreDlg.dismiss();
            }
        });
    }

    protected abstract int getIgnoreStringId();

    protected abstract String getSkipConfirmDesc();

    protected abstract String getSkipCancelDesc();

    protected abstract String getFolderFullDesc();
}
