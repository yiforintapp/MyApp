
package com.leo.appmaster.videohide;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.MediaColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.browser.aidl.mInterface;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

@SuppressLint("NewApi")
public class VideoGriActivity extends BaseActivity implements OnItemClickListener, OnClickListener {
    private static final String TAG = "VideoGriActivity";
    private static final int FROM_VIDEOHIDEMAIN_ACTIVITY = 1;
    private static final int FROM_VIDEOHIDEGALLER_ACTIVITY = 2;
    private GridView mHideVideo;
    private List<VideoItemBean> mVideoItems;
    private CommonTitleBar mCommonTtileBar;
    private int mActivityMode;
    private boolean mIsEditmode = false;
    private LinearLayout mBottomBar;
    private ImageView mSelectImage;
    private Button mHideButton;
    private HideVideoAdapter mHideVideoAdapter;
    private ArrayList<VideoItemBean> mClickList;
    private Button mSelectAll;
    private LEOAlarmDialog mDialog;
    private boolean mIsBackgoundRunning = false;
    private LEOCircleProgressDialog mProgressDialog;
    private List<Integer> mClickPosList;
    private ArrayList<String> mAllPath;
    private static final int REQUEST_CODE = 0;
    public static final int REQUEST_CODE_LOCK = 1000;
    public static final int REQUEST_CODE_OPTION = 1001;
    public List<VideoItemBean> mUnhide;
    private ArrayList<String> mUnhidePath;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;

    private mInterface mService;
    private ServiceConnection mConnection;
    private int mCbVersionCode = -1;
    private boolean isCbHere = false;
    private int mFromWhere = 0;
    private boolean isServiceDo = false;

    private void init() {
        mSelectAll = (Button) findViewById(R.id.select_all);
        mBottomBar = (LinearLayout) findViewById(R.id.bottom_bar);
        mHideButton = (Button) findViewById(R.id.hide_image);
        mCommonTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mHideVideo = (GridView) findViewById(R.id.Image_hide_folder);
        mCommonTtileBar.openBackView();
        Intent intent = getIntent();
        mActivityMode = intent.getIntExtra("mode", Constants.SELECT_HIDE_MODE);
        mFromWhere = intent.getIntExtra("fromwhere", 0);
        VideoBean video = (VideoBean) intent.getExtras().getSerializable("data");
        mVideoItems = video.getBitList();
        getVideoPath();
        if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
            mCommonTtileBar.setOptionImageVisibility(View.VISIBLE);
            mCommonTtileBar.setOptionListener(this);
            mCommonTtileBar.setOptionImage(R.drawable.edit_mode_name);
            mBottomBar.setVisibility(View.GONE);
        } else if (mActivityMode == Constants.SELECT_HIDE_MODE) {

        }
        if (mVideoItems != null && mVideoItems.size() != 0) {
            mHideVideoAdapter = new HideVideoAdapter(this, mVideoItems);
            mHideVideo.setAdapter(mHideVideoAdapter);
        }
        mCommonTtileBar.setTitle(video.getName());
    }

    private void initImageLoder() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.video_loading)
                .showImageForEmptyUri(R.drawable.video_loading)
                .showImageOnFail(R.drawable.video_loading)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(ImageLoaderConfiguration.createDefault(this));
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
        initImageLoder();
        // coolbrowser aidl
        gotoBindService();
    }

    private void gotoBindService() {
        mConnection = new AdditionServiceConnection();
        // Bundle args = new Bundle();
        Intent intent = new Intent("com.appmater.aidl.service");
        // intent.putExtras(args);
        LeoLog.d("testBindService", "bindService");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /*
     * This inner class is used to connect to the service
     */
    class AdditionServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            mService = mInterface.Stub.asInterface((IBinder) boundService);
            LeoLog.d("testBindService", "connect service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            LeoLog.d("testBindService", "disconnect service");
        }
    }

    @Override
    protected void onResume() {
        checkCbAndVersion();
        super.onResume();
    }

    private void checkCbAndVersion() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> list = packageManager
                .getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo packageInfo : list) {
            String packNameString = packageInfo.packageName;
            if (packNameString.equals(VideoHideMainActivity.CB_PACKAGENAME)) {
                isCbHere = true;
                mCbVersionCode = packageInfo.versionCode;
            }
        }
    }

    private void getVideoPath() {
        for (VideoItemBean videoItem : mVideoItems) {
            String path = videoItem.getPath();
            String name = videoItem.getName();
            LeoLog.d("testVio", "name is : " + name);
            LeoLog.d("testVio", "mPath is : " + path);
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

        public HideVideoAdapter(Context context, List<VideoItemBean> videos) {
            this.context = context;
            this.videos = videos;
            this.layoutInflater = LayoutInflater.from(context);
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
            if (position < videos.size()) {
                VideoItemBean video = videos.get(position);
                String path = video.getPath();
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
                // final ImageView imageView = viewHolder.imageView;
                // imageView.setTag(path);
                viewHolder.imageView.setBackgroundDrawable(context.getResources()
                        .getDrawable(R.drawable.video_loading));
                // Drawable drawableCache = asyncLoadImage.loadImage(imageView,
                // path,
                // new ImageCallback() {
                // @SuppressWarnings("deprecation")
                // @Override
                // public void imageLoader(Drawable drawable) {
                // if (imageView != null
                // && imageView.getTag().equals(path) && drawable != null) {
                // imageView.setBackgroundDrawable(drawable);
                // }
                // }
                // });
                // if (drawableCache != null) {
                // viewHolder.imageView.setBackgroundDrawable(drawableCache);
                // }
                String filePath = "voidefile://" + path;
                mImageLoader.displayImage(filePath, viewHolder.imageView, mOptions);
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
        Cursor mCursor = null;
        try {
            mCursor = getContentResolver().query(uri, null, selection, null,
                    MediaColumns.DATE_MODIFIED + " desc");

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
        } catch (Exception e) {
        } finally {
            if (mCursor != null) {
                mCursor.close();
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
                mSelectAll.setCompoundDrawablesWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.select_all_selector), null,
                        null);
                mSelectAll.setText(R.string.app_select_all);

            } else {
                mSelectAll.setCompoundDrawablesWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.no_select_all_selector), null,
                        null);
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
                    mSelectAll.setCompoundDrawablesWithIntrinsicBounds(null,
                            getResources().getDrawable(R.drawable.no_select_all_selector), null,
                            null);
                } else {
                    mClickList.clear();
                    mSelectAll.setText(R.string.app_select_all);
                    mSelectAll.setCompoundDrawablesWithIntrinsicBounds(null,
                            getResources().getDrawable(R.drawable.select_all_selector), null,
                            null);
                }
                updateRightButton();
                mHideVideoAdapter.notifyDataSetChanged();
                break;
            case R.id.hide_image:

                if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                    if (mClickList.size() > 0) {
                        VideoItemBean item = mClickList.get(0);
                        String mPath = item.getPath();
                        String mLastName = FileOperationUtil.getDirNameFromFilepath(mPath);
                        String mSecondName = FileOperationUtil
                                .getSecondDirNameFromFilepath(mPath);
                        if (mLastName.equals(VideoHideMainActivity.LAST_CATALOG)
                                && mSecondName.equals(VideoHideMainActivity.SECOND_CATALOG)) {
                            if (isCbHere
                                    && mCbVersionCode >= VideoHideMainActivity.TARGET_VERSION) {
                                // bindservice to do
                                isServiceDo = true;
                                showAlarmDialog();
                            } else {
                                mSelectAll.setText(R.string.app_select_all);
                                mClickList.clear();
                                updateRightButton();
                                mHideVideoAdapter.notifyDataSetChanged();
                                showDownLoadNewCbDialog();
                            }
                        } else {
                            showAlarmDialog();
                        }
                    }
                } else {
                    showAlarmDialog();
                }

                break;
            case R.id.tv_option_image:
                if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                    mIsEditmode = !mIsEditmode;
                    if (!mIsEditmode) {
                        cancelEditMode();
                    } else {
                        mBottomBar.setVisibility(View.VISIBLE);
                        mHideButton.setText(getString(R.string.app_cancel_hide_image));
                        mCommonTtileBar.setOptionImage(R.drawable.mode_done);
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
        mCommonTtileBar.setOptionImage(R.drawable.edit_mode_name);
        mSelectImage.setVisibility(View.GONE);
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
                        VideoItemBean item = mClickList.get(0);
                        String mPath = item.getPath();
                        String mLastName = FileOperationUtil.getDirNameFromFilepath(mPath);
                        String mSecondName = FileOperationUtil
                                .getSecondDirNameFromFilepath(mPath);

                        if (mActivityMode == Constants.SELECT_HIDE_MODE) {

                            if (mLastName.equals(VideoHideMainActivity.LAST_CATALOG)
                                    && mSecondName.equals(VideoHideMainActivity.SECOND_CATALOG)) {
                                if (isCbHere
                                        && mCbVersionCode >= VideoHideMainActivity.TARGET_VERSION) {
                                    // bindservice to do
                                    isServiceDo = true;
                                    showProgressDialog(getString(R.string.tips),
                                            getString(R.string.app_hide_image) + "...", true, true);
                                    BackgoundTask task = new BackgoundTask(VideoGriActivity.this);
                                    task.execute(true);
                                    mHideVideoAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(VideoGriActivity.this,
                                            getString(R.string.app_hide_video_fail),
                                            Toast.LENGTH_SHORT).show();
                                    mSelectAll.setText(R.string.app_select_all);
                                    mClickList.clear();
                                    updateRightButton();
                                    mHideVideoAdapter.notifyDataSetChanged();
                                }
                            } else {
                                showProgressDialog(getString(R.string.tips),
                                        getString(R.string.app_hide_image) + "...", true, true);
                                BackgoundTask task = new BackgoundTask(VideoGriActivity.this);
                                task.execute(true);
                                mHideVideoAdapter.notifyDataSetChanged();
                                /* SDK:use hide video */
                                SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1,
                                        "hide_Video",
                                        "used");
                            }
                        } else if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                            showProgressDialog(getString(R.string.tips),
                                    getString(R.string.app_cancel_hide_image) + "...",
                                    true,
                                    true);
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

    protected void showDownLoadNewCbDialog() {
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(this);
        }
        mDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    // getURL and go browser

                }
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setContent(getString(R.string.video_hide_need_new_cb));
        mDialog.setSureButtonText(getString(R.string.button_install));
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
        if (mImageLoader != null) {
            mImageLoader.clearMemoryCache();
        }
        if (mService != null) {
            unbindService(mConnection);
        }
    }

    /**
     * hideVideo and unVideo
     * 
     * @author run
     */
    private class BackgoundTask extends AsyncTask<Boolean, Integer, Boolean> {
        private Context context;

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
            ArrayList<VideoItemBean> list = (ArrayList<VideoItemBean>) mClickList.clone();
            if (list != null && list.size() > 0) {
                if (isHide) {
                    for (VideoItemBean item : list) {
                        if (!mIsBackgoundRunning)
                            break;
                        mUnhidePath.add(item.getPath());
                        if (isServiceDo) {
                            int mProcessType = -1;
                            try {
                                mProcessType = mService.hideVideo(item.getPath());
                            } catch (RemoteException e) {
                                isSuccess = false;
                            }
                            if (mProcessType == 0) {
                                mVideoItems.remove(item);
                            } else if (mProcessType == -1) {
                                mUnhidePath.remove(item.getPath());
                                isSuccess = false;
                            }
                        } else {
                            newFileName =
                                    FileOperationUtil.getNameFromFilepath(item.getPath());
                            newFileName = newFileName + ".leotmv";
                            if (FileOperationUtil.renameFile(item.getPath(),
                                    newFileName)) {
                                FileOperationUtil.saveFileMediaEntry(FileOperationUtil.makePath(
                                        FileOperationUtil.getDirPathFromFilepath(item.getPath()),
                                        newFileName), context);
                                FileOperationUtil.deleteVideoMediaEntry(item.getPath(),
                                        context);

                                mVideoItems.remove(item);
                            } else {
                                mUnhidePath.remove(item.getPath());
                                isSuccess = false;
                            }
                        }
                    }
                    isServiceDo = false;
                } else {
                    for (VideoItemBean item : list) {
                        if (!mIsBackgoundRunning)
                            break;

                        if (isServiceDo) {
                            int mProcessType = -1;
                            try {
                                mProcessType =
                                        mService.cancelHide(item.getPath());
                            } catch (RemoteException e) {
                                isSuccess = false;
                            }
                            if (mProcessType == 0) {
                                mVideoItems.remove(item);
                            } else if (mProcessType == -1) {
                                isSuccess = false;
                            }
                        } else {
                            try {
                                newFileName =
                                        FileOperationUtil.getNameFromFilepath(item.getPath());
                                newFileName = newFileName.substring(1,
                                        newFileName.indexOf(".leotmv"));
                                if (FileOperationUtil.renameFile(item.getPath(),
                                        newFileName)) {
                                    FileOperationUtil.saveImageMediaEntry(FileOperationUtil
                                            .makePath(
                                                    FileOperationUtil.getDirPathFromFilepath(item
                                                            .getPath()),
                                                    newFileName), context);
                                    FileOperationUtil.deleteFileMediaEntry(item.getPath(),
                                            context);
                                    mVideoItems.remove(item);
                                }
                            } catch (Exception e) {
                                isSuccess = false;
                            }
                        }
                    }
                    isServiceDo = false;
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
                if (mHideVideoAdapter != null) {
                    mHideVideoAdapter.notifyDataSetChanged();
                }
            } else {
                finish();
            }
            // video change, recompute privacy level
            PrivacyHelper.getInstance(VideoGriActivity.this).computePrivacyLevel(
                    PrivacyHelper.VARABLE_HIDE_VIDEO);
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
                    View child = mHideVideo.getChildAt(view);
                    if (child != null) {
                        child.setAlpha(1);
                        child.setScaleX(1);
                        child.setScaleY(1);
                    }
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
    }

    private void getResultValue() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("path", mUnhidePath);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }
}
