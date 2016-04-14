package com.leo.appmaster.imagehide;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by yangyuefeng on 2015/11/13.
 */
public abstract class FolderNewFragment<T> extends Fragment implements AbsListView.OnScrollListener,
        ExpandableListView.OnGroupClickListener, FolderNewAdapter.OnFolderClickListener,
        View.OnClickListener {
    private static final String TAG = FolderNewFragment.class.getSimpleName();
    private Dictionary<Integer, Integer> mItemHeights = new Hashtable<Integer, Integer>();

    protected FolderNewAdapter<T> mAdapter;
    protected Activity mActivity;
    protected List<T> mDataList;
    protected ExpandableListView mListView;


    protected TextView mProcessTv;

    protected int mEmptyHeight;

    protected int mToolbarHeight;

    protected int mTopViewHeightPadding;

    protected int mTopViewHeight;

    private View mFloatingView;
    private TextView mFloatingTv;
    protected CheckBox mFloatingCb;
    private RippleView mFloatingRv;

    private Button mHideBtn;
    protected Button mSelectBtn;
    private View mProcessClick;
    private MaterialRippleLayout mIgnoreBtn;
    private View mIgnoreClick;

    protected LEOAlarmDialog mIgnoreDlg;

    private View mOffsetBg;
    protected int mCurrentGroup;
    private int mLastGroup = -1;
    protected boolean mFinishNotified;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        mEmptyHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.pri_pro_header);
        mTopViewHeightPadding = getActivity().getResources().getDimensionPixelSize(
                R.dimen.pri_pro_header_padding);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (ExpandableListView) view.findViewById(getListViewId());
        mListView.addHeaderView(getEmptyHeader());
        mListView.setAdapter(mAdapter);
        mAdapter.setList(mDataList);
        mAdapter.setOnFolderClickListener(this);

        mListView.setOnScrollListener(this);
        mListView.setOnGroupClickListener(this);

        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            mListView.collapseGroup(i);
        }

        mFloatingView = view.findViewById(R.id.floating_lable);
        mFloatingView.setVisibility(View.INVISIBLE);

        mFloatingTv = (TextView) mFloatingView.findViewById(R.id.pri_pro_new_label_tv);
        mFloatingCb = (CheckBox) mFloatingView.findViewById(R.id.pri_pro_cb);
        mFloatingRv = (RippleView) mFloatingView.findViewById(R.id.pri_pro_click_rv);
        mFloatingRv.setOnClickListener(this);
        mFloatingCb.setOnClickListener(this);

        mHideBtn = (Button) view.findViewById(R.id.hide_image);
        mSelectBtn = (Button) view.findViewById(R.id.select_all);
//        mProcessBtn.setRippleOverlay(true);
        mProcessClick = view.findViewById(R.id.pp_process_rv_click);
        mIgnoreBtn = (MaterialRippleLayout) view.findViewById(R.id.pp_process_ignore_rv);
        mIgnoreBtn.setRippleOverlay(true);
        mIgnoreClick = view.findViewById(R.id.pp_process_ignore_rv_click);
        mProcessTv = (TextView) view.findViewById(R.id.pp_process_tv);

        mHideBtn.setOnClickListener(this);
        mSelectBtn.setOnClickListener(this);
        mProcessClick.setOnClickListener(this);
        mIgnoreClick.setOnClickListener(this);

        mHideBtn.setEnabled(false);
        mProcessClick.setEnabled(false);

        mOffsetBg = view.findViewById(R.id.pri_offset_bg);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        View c = view.getChildAt(0); //this is the first visible row
        if (c == null) return;

        int scrollY = -c.getTop();
        mItemHeights.put(view.getFirstVisiblePosition(), c.getHeight());
        for (int i = 0; i < view.getFirstVisiblePosition(); ++i) {
            if (mItemHeights.get(i) != null) // (this is a sanity check)
                scrollY += mItemHeights.get(i); //add all heights of the views that are gone
        }

//        mActivity.onListScroll(scrollY);
        int maxScrollHeight = /*mEmptyHeight - */mTopViewHeight/* + mTopViewHeightPadding*/;
        LeoLog.e("HomePrivacyFragment", "onScroll:" + mTopViewHeight);
        if (scrollY > maxScrollHeight) {
            mOffsetBg.setTranslationY(-maxScrollHeight);
        } else {
            mOffsetBg.setTranslationY(-scrollY);
        }
        if (scrollY > maxScrollHeight && mListView.isGroupExpanded(0)) {
            mFloatingView.setVisibility(View.VISIBLE);

//            FolderAdapter.ItemsWrapper<T> wrapper = (FolderAdapter.ItemsWrapper<T>)
//                    mAdapter.getFirstVisibleGroup(firstVisibleItem);
            int group = mAdapter.getFirstVisibleGroupPosition(firstVisibleItem);
            FolderNewAdapter.ItemsWrapper<T> wrapper = (FolderNewAdapter.ItemsWrapper<T>) mAdapter.getGroup(group);
            if (wrapper == null) return;
            mCurrentGroup = mAdapter.getFirstVisibleGroupPosition(firstVisibleItem);
            if (mCurrentGroup != mLastGroup) {
                mAdapter.setLableContent(mFloatingTv, wrapper.parentName, wrapper.items.size());
                mFloatingCb.setChecked(mAdapter.isGroupChecked(group));
                mFloatingView.setTranslationY(0);
            } else if (mLastGroup != -1) {
                int nextPosition = mAdapter.getNextPositionOfAllDatas(mCurrentGroup);
                nextPosition += mListView.getHeaderViewsCount();
                if (nextPosition != -1 && nextPosition > firstVisibleItem
                        && nextPosition < (firstVisibleItem + visibleItemCount)) {
                    View nextChild = view.getChildAt(nextPosition - firstVisibleItem);
                    int nextTop = nextChild.getTop();
                    int floatingH = mFloatingView.getHeight();
                    if (nextTop < floatingH) {
                        mFloatingView.setTranslationY(-(floatingH - nextTop));
                    } else {
                        mFloatingView.setTranslationY(0);
                    }
                }
            }
            mLastGroup = mCurrentGroup;
        } else {
            mLastGroup = -1;
            mFloatingView.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        parent.expandGroup(groupPosition);
        return true;
    }

    @Override
    public void onGroupClick(final int groupPosition, boolean isExpanded) {
        if (isExpanded) {
            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                mListView.collapseGroup(i);
            }
        } else {
            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                mListView.expandGroup(i);
            }
        }
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mListView.setSelectedGroup(groupPosition);
            }
        }, 100);
    }

    @Override
    public void onGroupCheckChanged(int groupPosition, boolean checked) {
        int group = mCurrentGroup;
        if (group == groupPosition) {
            mFloatingCb.setChecked(checked);
        }
    }

    protected void hideDone(){
        mHideBtn.setText(getString(R.string.app_hide_image));
        mHideBtn.setEnabled(false);
    }

    @Override
    public void onSelectionChange(boolean selectAll, int selectedCount) {
        LeoLog.v(TAG, "onSelectionChange");
        if (selectedCount > 0) {
            mHideBtn.setEnabled(true);
            mProcessClick.setEnabled(true);
            mHideBtn.setText(getString(R.string.new_hide_num, mAdapter.getSelectData() == null ? 0 : mAdapter.getSelectData().size()));
        } else {
            mHideBtn.setEnabled(false);
            mProcessClick.setEnabled(false);
            mHideBtn.setText(getString(R.string.app_hide_image));
        }
        if (mAdapter.getSelectData() != null && mAdapter.getSelectData().size() < mDataList.size()) {
            mSelectBtn.setText(R.string.app_select_all);
            mSelectBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.select_all_selector), null,
                    null);
        } else {
            mSelectBtn.setText(R.string.app_select_none);
            mSelectBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.no_select_all_selector), null,
                    null);
        }
    }

    public List<T> getSelectData() {
        return mAdapter.getSelectData();
    }

    @Override
    public void onClick(View v) {
        if (v == mFloatingCb) {
            onFloatingCheckClick();
        }  else if (v.getId() == R.id.pri_pro_click_rv) {
            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                mListView.collapseGroup(i);
                final int group = mCurrentGroup;
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setSelectedGroup(group);
                    }
                }, 100);
            }
        } else if (v.getId() == R.id.hide_image) {
            List<T> list = mAdapter.getSelectData();
            LeoLog.v(TAG, "隐藏数量：" + (list != null ? list.size() : 0));
            onProcessClick();
        } else if (v.getId() == R.id.select_all) {
            if (mAdapter.getSelectData() != null && mAdapter.getSelectData().size() < mDataList.size()) {
                LeoLog.v(TAG, "selectAllGroup");
                mAdapter.selectAllGroup();
                mSelectBtn.setText(R.string.app_select_none);
                mSelectBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.no_select_all_selector), null,
                        null);
            } else {
                LeoLog.v(TAG, "cancelSelectAllGroup");
                mAdapter.deselectAllGroup();
                mSelectBtn.setText(R.string.app_select_all);
                mSelectBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.select_all_selector), null,
                        null);
                onSelectAllClick();
            }
        }

    }

    public void setData(List<T> list) {
        if (list == null) return;

        mDataList = new ArrayList<T>();
        for (T data : list) {
            mDataList.add(data);
        }

        if (mAdapter != null) {
            mAdapter.setList(mDataList);
        }
    }

    protected View getEmptyHeader() {

        return null;
    }

    protected abstract void onSelectAllClick();

    protected abstract int getListViewId();

    protected abstract void onIgnoreClick();

    protected abstract void onProcessClick();

    protected abstract void onFloatingCheckClick();

}
