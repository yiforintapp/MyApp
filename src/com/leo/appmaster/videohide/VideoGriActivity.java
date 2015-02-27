
package com.leo.appmaster.videohide;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.videohide.AsyncLoadImage.ImageCallback;

@SuppressLint("NewApi")
public class VideoGriActivity extends BaseActivity implements OnItemClickListener, OnClickListener {
    private Cursor mCursor;
    private GridView mHideVideo;
    private List<VideoItemBean> mVideoItems;
    private CommonTitleBar mCommonTtileBar;
    private int mActivityMode;
    private boolean mIsEditmode = false;
    private LinearLayout mBottomBar;
    private ImageView mSelectImage;
    private Button mHideButton;
    private HideVideoAdapter mHideVideoAdapter;
    private List<VideoItemBean> mClickList;
    private Button mSelectAll;
    private LEOAlarmDialog mDialog;
    private boolean mIsBackgoundRunning = false;
    private LEOCircleProgressDialog mProgressDialog;
    private List<Integer> mClickPosList;
    private ArrayList<String> mAllPath;
    private static final int REQUEST_CODE = 0;
    private boolean mShouldLockOnRestart = true;
    public static final int REQUEST_CODE_LOCK = 1000;
    public static final int REQUEST_CODE_OPTION = 1001;
    public List<VideoItemBean> mUnhide;
    private ArrayList<String> mUnhidePath;

    private void init() {
        mSelectAll = (Button) findViewById(R.id.select_all);
        mBottomBar = (LinearLayout) findViewById(R.id.bottom_bar);
        mHideButton = (Button) findViewById(R.id.hide_image);
        mCommonTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mHideVideo = (GridView) findViewById(R.id.Image_hide_folder);
        mCommonTtileBar.openBackView();
        Intent intent = getIntent();
        mActivityMode = intent.getIntExtra("mode", Constants.SELECT_HIDE_MODE);
        VideoBean video = (VideoBean) intent.getExtras().getSerializable("data");
        mVideoItems = video.getBitList();
        getVideoPath();
        if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
            String optionName = getString(R.string.app_hide_image_edit);
            mCommonTtileBar.setOptionTextVisibility(View.VISIBLE);
            mCommonTtileBar.setOptionListener(this);
            mCommonTtileBar.setOptionText(optionName);
            mBottomBar.setVisibility(View.GONE);
        } else if (mActivityMode == Constants.SELECT_HIDE_MODE) {

        }
        if (mVideoItems != null && mVideoItems.size() != 0) {
            mHideVideoAdapter = new HideVideoAdapter(this, mVideoItems);
            mHideVideo.setAdapter(mHideVideoAdapter);
        }
        mCommonTtileBar.setTitle(video.getName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_gridview);
        mClickList = new ArrayList<VideoItemBean>();
        mClickPosList = new ArrayList<Integer>();
        mAllPath = new ArrayList<String>();
        mUnhide = new ArrayList<VideoItemBean>();
        mUnhidePath = new ArrayList<String>();
        init();
        mHideVideo.setOnItemClickListener(this);
        mSelectAll.setOnClickListener(this);
        mHideButton.setOnClickListener(this);
        getResultValue();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    private void getVideoPath() {
        for (VideoItemBean videoItem : mVideoItems) {
            String path = videoItem.getPath();
            mAllPath.add(path);
        }
    }

    @Override
    public void onBackPressed() {

        if (mActivityMode == Constants.CANCLE_HIDE_MODE && mIsEditmode) {
            cancelEditMode();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * HideVideoAdapter
     */
    class HideVideoAdapter extends BaseAdapter {
        Context context;
        List<VideoItemBean> videos;
        LayoutInflater layoutInflater;
        AsyncLoadImage asyncLoadImage;

        public HideVideoAdapter(Context context, List<VideoItemBean> videos) {
            this.context = context;
            this.videos = videos;
            this.layoutInflater = LayoutInflater.from(context);
            this.asyncLoadImage = new AsyncLoadImage();
        }

        @Override
        public int getCount() {

            return videos != null ? videos.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return videos != null ? videos.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            ImageView imageView, selectImage;
            TextView text;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.activity_item_video_gridview, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.selectImage = (ImageView) convertView.findViewById(R.id.photo_select);
                mSelectImage = viewHolder.selectImage;
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
                viewHolder.text = (TextView) convertView.findViewById(R.id.txt_item_picture);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            // AM-806
            if (position < videos.size()) {
                VideoItemBean video = videos.get(position);
                final String path = video.getPath();
                if (mActivityMode == Constants.CANCLE_HIDE_MODE && !mIsEditmode) {
                    viewHolder.selectImage.setVisibility(View.GONE);
                } else {
                    viewHolder.selectImage.setVisibility(View.VISIBLE);
                    if (mClickList.contains(mVideoItems.get(position))) {
                        viewHolder.selectImage.setImageResource(R.drawable.pic_choose_active);
                    } else {
                        viewHolder.selectImage.setImageResource(R.drawable.pic_choose_normal);
                    }
                }
                String name = FileOperationUtil.getNoExtNameFromHideFilepath(path);
                viewHolder.text.setText(name);
                final ImageView imageView = viewHolder.imageView;
                imageView.setTag(path);
                viewHolder.imageView.setBackgroundDrawable(context.getResources()
                        .getDrawable(R.drawable.video_loading));
                Drawable drawableCache = asyncLoadImage.loadImage(imageView, path,
                        new ImageCallback() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void imageLoader(Drawable drawable) {
                                if (imageView != null
                                        && imageView.getTag().equals(path) && drawable != null) {
                                    imageView.setBackgroundDrawable(drawable);
                                }
                            }
                        });
                if (drawableCache != null) {
                    viewHolder.imageView.setBackgroundDrawable(drawableCache);
                }
            }
            return convertView;
        }

    }

    /**
     * getVideoInfo
     */
    public void getVideoInfo(String dirPath) {
        Uri uri = Files.getContentUri("external");
        String selection = Constants.VIDEO_FORMAT;
        try {
            mCursor = getContentResolver().query(uri, null, selection, null,
                    MediaColumns.DATE_MODIFIED + " desc");
        } catch (Exception e) {
        }

        if (mCursor != null) {
            List<VideoItemBean> countMap = new ArrayList<VideoItemBean>();
            while (mCursor.moveToNext()) {
                VideoItemBean video = new VideoItemBean();
                String path = mCursor.getString(mCursor
                        .getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                String dirName = FileOperationUtil.getDirNameFromFilepath(path);
                FileOperationUtil.getDirPathFromFilepath(path);
                video.setPath(path);
                video.setName(dirName);
                countMap.add(video);
            }
        }

    }

    /**
     * GrideView OnItemClick
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        VideoItemBean video = mVideoItems.get(position);
        if (mActivityMode == Constants.CANCLE_HIDE_MODE && !mIsEditmode && mVideoItems.size() > 0) {
            Intent intent = new Intent();
            intent.setClass(VideoGriActivity.this, VideoViewPager.class);
            intent.putExtra("path", video.getPath());
            intent.putStringArrayListExtra("mAllPath", mAllPath);
            intent.putExtra("position", position);
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            ImageView cView = (ImageView) view.findViewById(R.id.photo_select);
            if (!mClickList.contains(mVideoItems.get(position))) {
                cView.setImageResource(R.drawable.pic_choose_active);
                mClickList.add(mVideoItems.get(position));
                mClickPosList.add((Integer) position);
            } else {
                cView.setImageResource(R.drawable.pic_choose_normal);
                mClickList.remove(mVideoItems.get(position));
                mClickPosList.remove((Integer) position);
            }
            if (mClickList.size() < mVideoItems.size()) {
                mSelectAll.setText(R.string.app_select_all);
            } else {
                mSelectAll.setText(R.string.app_select_none);
            }
            updateRightButton();
        }

    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.select_all:
                if (mClickList.size() < mVideoItems.size()) {
                    mClickList.clear();
                    mClickList.addAll(mVideoItems);
                    mSelectAll.setText(R.string.app_select_none);
                } else {
                    mClickList.clear();
                    mSelectAll.setText(R.string.app_select_all);
                }
                updateRightButton();
                mHideVideoAdapter.notifyDataSetChanged();
                break;
            case R.id.hide_image:
                showAlarmDialog();
                break;
            case R.id.tv_option_text:
                if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                    mIsEditmode = !mIsEditmode;
                    if (!mIsEditmode) {
                        cancelEditMode();
                    } else {
                        mBottomBar.setVisibility(View.VISIBLE);
                        mHideButton.setText(getString(R.string.app_cancel_hide_image));
                        mCommonTtileBar.setOptionText(getString(R.string.cancel));
                    }
                    mHideVideoAdapter.notifyDataSetChanged();
                }

                break;
            default:
                break;
        }
    }

    private void cancelEditMode() {
        mIsEditmode = false;
        mClickList.clear();
        mHideVideoAdapter.notifyDataSetChanged();
        mBottomBar.setVisibility(View.GONE);
        mCommonTtileBar.setOptionText(getString(R.string.app_hide_image_edit));
        mSelectImage.setVisibility(View.GONE);
        mSelectAll.setText(R.string.app_select_all);
        updateRightButton();
    }

    private void updateRightButton() {
        if (mClickList.size() > 0) {
            if (mActivityMode == Constants.SELECT_HIDE_MODE) {
                mHideButton.setText(getString(R.string.app_hide_image) + "("
                        + mClickList.size() + ")");
            } else if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                mHideButton.setText(getString(R.string.app_cancel_hide_image) + "("
                        + mClickList.size() + ")");
            }
            mHideButton.setEnabled(true);
        } else {
            if (mActivityMode == Constants.SELECT_HIDE_MODE) {
                mHideButton.setText(getString(R.string.app_hide_image));
            } else if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                mHideButton.setText(getString(R.string.app_cancel_hide_image));
            }
            mHideButton.setEnabled(false);
        }
    }

    private void showAlarmDialog() {
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(this);
        }
        mDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    if (mClickList.size() > 0) {
                        if (mActivityMode == Constants.SELECT_HIDE_MODE) {

                            showProgressDialog(getString(R.string.tips),
                                    getString(R.string.app_hide_image) + "...", true, true);
                            BackgoundTask task = new BackgoundTask(VideoGriActivity.this);
                            task.execute(true);
                            mHideVideoAdapter.notifyDataSetChanged();
                            /* SDK:use hide video */
                            SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hide_Video",
                                    "used");
                        } else if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                            showProgressDialog(getString(R.string.tips),
                                    getString(R.string.app_cancel_hide_image) + "...", true, true);
                            BackgoundTask task = new BackgoundTask(VideoGriActivity.this);
                            task.execute(false);
                            mHideVideoAdapter.notifyDataSetChanged();
                        }
                    }

                }

            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        if (mActivityMode == Constants.SELECT_HIDE_MODE) {
            mDialog.setTitle(R.string.app_hide_image);
            mDialog.setContent(getString(R.string.app_hide_video_dialog_content));

        } else if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
            mDialog.setTitle(R.string.app_cancel_hide_image);
            mDialog.setContent(getString(R.string.app_unhide_video_dialog_content));
        }
        mDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClickList.clear();
        mUnhide.clear();
        mUnhidePath.clear();
        mVideoItems.clear();
        mClickPosList.clear();
        mAllPath.clear();
    }

    /**
     * hideVideo and unVideo
     * 
     * @author run
     */
    private class BackgoundTask extends AsyncTask<Boolean, Integer, Boolean> {
        private Context context;
        private Toast mHideFailToast = null;

        BackgoundTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            mIsBackgoundRunning = true;
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            String newFileName;
            Boolean isSuccess = true;
            boolean isHide = params[0];
            if (mClickList != null && mClickList.size() > 0) {
                if (isHide) {
                    for (VideoItemBean item : mClickList) {
                        if (!mIsBackgoundRunning)
                            break;
                        mUnhidePath.add(item.getPath());
                        newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                        newFileName = newFileName + ".leotmv";
                        if (FileOperationUtil.renameFile(item.getPath(), newFileName)) {
                            FileOperationUtil.saveFileMediaEntry(FileOperationUtil.makePath(
                                    FileOperationUtil.getDirPathFromFilepath(item.getPath()),
                                    newFileName), context);
                            FileOperationUtil.deleteVideoMediaEntry(item.getPath(), context);
                            mVideoItems.remove(item);
                        } else {
                            mUnhidePath.remove(item.getPath());
                            isSuccess = false;
                        }

                    }
                } else {
                    for (VideoItemBean item : mClickList) {
                        if (!mIsBackgoundRunning)
                            break;
                        newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                        newFileName = newFileName.substring(1, newFileName.indexOf(".leotmv"));
                        if (FileOperationUtil.renameFile(item.getPath(), newFileName)) {
                            FileOperationUtil.saveImageMediaEntry(FileOperationUtil.makePath(
                                    FileOperationUtil.getDirPathFromFilepath(item.getPath()),
                                    newFileName), context);
                            FileOperationUtil.deleteFileMediaEntry(item.getPath(), context);
                            mVideoItems.remove(item);
                        }
                    }
                }
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean isSuccess) {
            mClickList.clear();
            if (!isSuccess) {
                mSelectAll.setText(R.string.app_select_all);
                Toast.makeText(VideoGriActivity.this, getString(R.string.app_hide_video_fail),
                        Toast.LENGTH_SHORT).show();
            }
            dismissProgressDialog();
            if (mVideoItems.size() > 0) {
                animateReorder();
                updateRightButton();
            } else {
                finish();
            }
        }
    }

    private void showProgressDialog(String title, String message, boolean indeterminate,
            boolean cancelable) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEOCircleProgressDialog(this);
            mProgressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mIsBackgoundRunning = false;
                    mSelectAll.setText(R.string.app_select_all);
                    mHideVideoAdapter.notifyDataSetChanged();
                }
            });
        }
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setButtonVisiable(cancelable);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(indeterminate);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void animateReorder() {
        int length = mClickPosList.size();
        List<Animator> resultList = new LinkedList<Animator>();
        int fistVisblePos = mHideVideo.getFirstVisiblePosition();
        int lastVisblePos = mHideVideo.getLastVisiblePosition();
        int pos;
        final List<Integer> viewList = new LinkedList<Integer>();
        for (int i = 0; i < length; i++) {
            pos = mClickPosList.get(i);
            if (pos >= fistVisblePos && pos <= lastVisblePos) {
                View view = mHideVideo.getChildAt(pos - fistVisblePos);
                viewList.add((Integer) (pos - fistVisblePos));
                resultList.add(createZoomAnimations(view));
            }
        }

        AnimatorSet resultSet = new AnimatorSet();
        resultSet.playTogether(resultList);
        resultSet.setDuration(500);
        resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
        resultSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mHideVideoAdapter.notifyDataSetChanged();
                for (Integer view : viewList) {
                    mHideVideo.getChildAt(view).setAlpha(1);
                    mHideVideo.getChildAt(view).setScaleX(1);
                    mHideVideo.getChildAt(view).setScaleY(1);
                }
                mClickPosList.clear();
            }
        });
        resultSet.start();
    }

    private Animator createZoomAnimations(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f);
        ObjectAnimator zoomIn = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        AnimatorSet animZoom = new AnimatorSet();
        animZoom.playTogether(scaleX, scaleY, zoomIn);
        return animZoom;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && mVideoItems != null && data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                ArrayList<String> resultPath = (ArrayList<String>) bundle.get("path");
                if (resultPath != null && resultPath.size() > 0) {
                    for (int i = 0; i < mVideoItems.size(); i++) {
                        String path = mVideoItems.get(i).getPath();
                        if (resultPath.contains(path)) {
                            mVideoItems.remove(i);
                        }
                    }
                    mAllPath.clear();
                    getVideoPath();
                    if (mHideVideoAdapter != null) {
                        mHideVideoAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
        mShouldLockOnRestart = false;
    }

    @Override
    public void onActivityRestart() {
        super.onActivityRestart();
        if (mShouldLockOnRestart) {
            showLockPage();
        } else {
            mShouldLockOnRestart = true;
        }
    }

    private void showLockPage() {
        Intent intent = new Intent(this, LockScreenActivity.class);
        int lockType = AppMasterPreference.getInstance(this).getLockType();
        if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
            intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
                    LockFragment.LOCK_TYPE_PASSWD);
        } else {
            intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
                    LockFragment.LOCK_TYPE_GESTURE);
        }
        intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
                LockFragment.FROM_SELF);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        // | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(intent, 1000);
    }

    private void getResultValue() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("path", mUnhidePath);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }
}
