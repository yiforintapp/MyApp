
package com.leo.appmaster.videohide;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
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
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.browser.aidl.mInterface;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.GradeEvent;
import com.leo.appmaster.eventbus.event.MediaChangeEvent;
import com.leo.appmaster.fragment.GuideFragment;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageDownloader;
import com.leo.imageloader.core.ImageScaleType;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("NewApi")
public class VideoGriActivity extends BaseFragmentActivity implements OnItemClickListener, OnClickListener {

    private static final String TAG = VideoGriActivity.class.getSimpleName();

    public final static int START_CANCEL_OR_HIDE_VID = 26;
    public final static int CANCEL_OR_HIDE_FINISH = 27;
    private GridView mHideVideo;
    private List<VideoItemBean> mVideoItems;
    private CommonToolbar mCommonTtileBar;
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
    public List<VideoItemBean> mUnhide;
    private ArrayList<String> mUnhidePath;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;

    private mInterface mService;
    private ServiceConnection mConnection;
    private boolean isCbHere = false;
    private boolean isServiceDo = false;
    private boolean isBindServiceOK = false;
    private boolean isHaveServiceToBind = false;
    private String mOneName, mTwoName;
    private VideoBean mVideoBean;
    private GuideFragment mGuideFragment;
    private boolean mVideoEditGuide;
    private int mProcessNum = 0;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(final android.os.Message msg) {
            switch (msg.what) {
//                case INIT_UI_DONE:
//                    asyncLoad();
//                    break;
//                case LOAD_DATA_DONE:
//                    loadDone();
//                    break;
                case START_CANCEL_OR_HIDE_VID:
                    final boolean isHide = (Boolean) msg.obj;
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            startDoingBack(isHide);
                        }
                    });
                    break;
                case CANCEL_OR_HIDE_FINISH:

                    Bundle bundle = msg.getData();
                    boolean isSuccess = bundle.getBoolean("isSuccess");
                    boolean isHideb = bundle.getBoolean("isHide");

                    checkLostVid(isHideb);

                    onPostDo(isSuccess);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_gridview);
        initImageLoder();
        init();
        initUI();

        getResultValue();
        // coolbrowser aidl
        gotoBindService();
    }

    private void checkLostVid(final boolean isHide) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager mPDManager = (PrivacyDataManager) MgrContext
                        .getManager(MgrContext.MGR_PRIVACY_DATA);
                int savevidNum = LeoSettings.getInteger(Constants.HIDE_VIDS_NUM, -1);
                LeoLog.d("checkLostPic", "savevidNum : " + savevidNum);
                int vidnum = mPDManager.getHideVidsRealNum();
                LeoLog.d("checkLostPic", "hide vid num : " + vidnum);
                if (savevidNum != -1) {
                    if (isHide) {
                        LeoLog.d("checkLostPic", "isHide process num : " + mProcessNum);
                        int targetNum = savevidNum + mProcessNum;
                        if (vidnum >= targetNum) {
                            LeoLog.d("checkLostPic", "everything ok");
                            LeoSettings.setInteger(Constants.HIDE_VIDS_NUM, vidnum);
                        } else {
                            LeoLog.d("checkLostPic", "lost vid");
                            mPDManager.reportDisappearError(false, PrivacyDataManager.LABEL_DEL_BY_SELF);
                            LeoSettings.setInteger(Constants.HIDE_VIDS_NUM, vidnum);
                        }
                    } else {
                        LeoLog.d("checkLostPic", "cancelHide process num : " + mProcessNum);
                        int targetNum = savevidNum - mProcessNum;
                        if (vidnum >= targetNum) {
                            LeoLog.d("checkLostPic", "everything ok");
                            LeoSettings.setInteger(Constants.HIDE_VIDS_NUM, vidnum);
                        } else {
                            LeoLog.d("checkLostPic", "lost vid");
                            mPDManager.reportDisappearError(false, PrivacyDataManager.LABEL_DEL_BY_SELF);
                            LeoSettings.setInteger(Constants.HIDE_VIDS_NUM, vidnum);
                        }
                    }
                } else {
                    LeoSettings.setInteger(Constants.HIDE_VIDS_NUM, vidnum);
                }
            }
        });
    }

    private void initImageLoder() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.img_vid_loading)
                .showImageForEmptyUri(R.drawable.img_vid_loading)
                .showImageOnFail(R.drawable.img_vid_loading)
//                .showImageOnLoading(R.drawable.loading_icon)
//                .showImageForEmptyUri(R.drawable.loading_icon)
//                .showImageOnFail(R.drawable.loading_icon)
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

    private void init() {
        mClickList = new ArrayList<VideoItemBean>();
        mClickPosList = new ArrayList<Integer>();
        mAllPath = new ArrayList<String>();
        mUnhide = new ArrayList<VideoItemBean>();
        mUnhidePath = new ArrayList<String>();
    }

    private void initUI() {
        mSelectAll = (Button) findViewById(R.id.select_all);
        mBottomBar = (LinearLayout) findViewById(R.id.bottom_bar);
        mHideButton = (Button) findViewById(R.id.hide_image);
        mCommonTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mCommonTtileBar.setOptionMenuVisible(false);
        mHideVideo = (GridView) findViewById(R.id.Image_hide_folder);
        handleIntent();

        mHideVideo.setOnItemClickListener(this);
        mSelectAll.setOnClickListener(this);
        mHideButton.setOnClickListener(this);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mActivityMode = intent.getIntExtra("mode", Constants.SELECT_HIDE_MODE);
        mVideoBean = (VideoBean) intent.getExtras().getSerializable("data");

        mVideoItems = ((PrivacyDataManager) MgrContext.
                getManager(MgrContext.MGR_PRIVACY_DATA)).getHideVidFile(mVideoBean);
        getVideoPath();

        if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
            mCommonTtileBar.setOptionMenuVisible(true);
            mCommonTtileBar.setOptionClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                        mIsEditmode = !mIsEditmode;
                        if (!mIsEditmode) {
                            cancelEditMode();
                        } else {
                            mBottomBar.setVisibility(View.VISIBLE);
                            mHideButton.setText(R.string.app_cancel_hide_image);
//                            Drawable topDrawable = getResources().getDrawable(
//                                    R.drawable.unhide_picture_selector);
//                            topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(),
//                                    topDrawable.getMinimumHeight());
//                            mHideButton.setCompoundDrawables(null, topDrawable, null, null);
                            mCommonTtileBar.setOptionImageResource(R.drawable.mode_done);
                        }
                        mHideVideoAdapter.notifyDataSetChanged();
                        if (!mVideoEditGuide) {
                            cancelVideoGuide();
                        }
                    }
                }
            });
            mCommonTtileBar.setOptionImageResource(R.drawable.edit_mode_name);
            mBottomBar.setVisibility(View.GONE);
            LeoPreference pre = LeoPreference.getInstance();
            mVideoEditGuide = pre.getBoolean(PrefConst.KEY_VIDEO_EDIT_GUIDE, false);
            if (!mVideoEditGuide) {
                mGuideFragment = (GuideFragment) getSupportFragmentManager().findFragmentById(R.id.video_guide);
                mGuideFragment.setEnable(true, GuideFragment.GUIDE_TYPE.VIDEO_GUIDE);
                mCommonTtileBar.setNavigationClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelVideoGuide();
                        finish();
                    }
                });
            }

        }
        mCommonTtileBar.setToolbarTitle(mVideoBean.getName());
        if (mVideoItems != null && mVideoItems.size() != 0) {
            mHideVideoAdapter = new HideVideoAdapter(this, mVideoItems);
            mHideVideo.setAdapter(mHideVideoAdapter);
        }
    }

    private void cancelVideoGuide() {
        if (mGuideFragment != null) {
            mGuideFragment.setEnable(false, GuideFragment.GUIDE_TYPE.VIDEO_GUIDE);
            LeoPreference pre = LeoPreference.getInstance();
            pre.putBoolean(PrefConst.KEY_VIDEO_EDIT_GUIDE, true);

            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "hidvid_bub_cnts");
        }
    }

    private void gotoBindService() {
        mConnection = new AdditionServiceConnection();
        Intent intent = new Intent("com.appmater.aidl.service");
        LeoLog.d("testBindService", "bindService");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /*
     * This inner class is used to connect to the service
     */
    class AdditionServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            mService = mInterface.Stub.asInterface((IBinder) boundService);
            isHaveServiceToBind = true;
            isBindServiceOK = true;
            LeoLog.d("testBindService", "connect service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isHaveServiceToBind = true;
            isBindServiceOK = false;
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
        if (!mVideoEditGuide) {
            cancelVideoGuide();
        }
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
                viewHolder.selectImage = (ImageView) convertView.findViewById(R.id.video_select);
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
                        viewHolder.selectImage.setImageResource(R.drawable.select2_icon);
                    } else {
                        viewHolder.selectImage.setImageResource(R.drawable.ic_check_normal_n);
                    }
                }
                String name = FileOperationUtil.getNoExtNameFromHideFilepath(path);
                viewHolder.text.setText(name);
//                viewHolder.imageView.setBackgroundDrawable(context.getResources()
//                        .getDrawable(R.drawable.video_loading));
                String filePath = ImageDownloader.Scheme.VIDEOFILE.wrap(path);
                mImageLoader.displayImage(filePath, viewHolder.imageView, mOptions);
            }
            return convertView;
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
            ImageView cView = (ImageView) view.findViewById(R.id.video_select);
            if (!mClickList.contains(mVideoItems.get(position))) {
                cView.setImageResource(R.drawable.select2_icon);
                mClickList.add(mVideoItems.get(position));
                mClickPosList.add((Integer) position);
            } else {
                cView.setImageResource(R.drawable.ic_check_normal_n);
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
                                && mSecondName.equals(VideoHideMainActivity.SECOND_CATALOG)
                                && isCbHere
                                && isHaveServiceToBind && isBindServiceOK) {
                            isServiceDo = true;
                            LeoLog.d("testBindService", "isServiceDo = true");
                        } else {
                            LeoLog.d("testBindService", "isServiceDo = false");
                        }
                        showAlarmDialog();
                    }
                } else {
                    showAlarmDialog();
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
        mCommonTtileBar.setOptionImageResource(R.drawable.edit_mode_name);
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
                    int size = mClickList.size();
                    if (size > 0) {
                        VideoItemBean item = mClickList.get(0);
                        String mPath = item.getPath();
                        String mLastName = FileOperationUtil.getDirNameFromFilepath(mPath);
                        String mSecondName = FileOperationUtil
                                .getSecondDirNameFromFilepath(mPath);

                        if (mActivityMode == Constants.SELECT_HIDE_MODE) {
                            // new
                            if (mLastName.equals(VideoHideMainActivity.LAST_CATALOG)
                                    && mSecondName.equals(VideoHideMainActivity.SECOND_CATALOG)
                                    && isCbHere && isHaveServiceToBind && isBindServiceOK) {
                                isServiceDo = true;
                            }

                            showProgressDialog(getString(R.string.tips),
                                    getString(R.string.app_hide_image) + "...", true, true);
//                            BackgoundTask task = new BackgoundTask(VideoGriActivity.this);
//                            task.execute(true);
                            doingBackGround(true);
                            mHideVideoAdapter.notifyDataSetChanged();
                            /* SDK:use hide video */
                            if (VideoHideMainActivity.mFromHomeEnter) {
                                 SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hide_Video", "vid_home_hide");
                            } else {
                                 SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hide_pic", "vid_icon_hide");
                            }
                            SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hide_Video", "used");
                            SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hide_vid_operation", "vid_add_cnts");
                            SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hide_vid_operation", "vid_add_pics_$" + size);
                        } else if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                            showProgressDialog(getString(R.string.tips),
                                    getString(R.string.app_cancel_hide_image) + "...",
                                    true,
                                    true);
                            doingBackGround(false);
                            mHideVideoAdapter.notifyDataSetChanged();
                            SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1,
                                    "hide_vid_operation",
                                    "vid_ccl_pics_" + size);
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

    private void doingBackGround(boolean isHideVid) {
        onPreDo();
        readyDoingBack(isHideVid);
    }

    private void readyDoingBack(boolean isHideVid) {
        if (mHandler != null) {
            Message msg = new Message();
            msg.obj = isHideVid;
            msg.what = START_CANCEL_OR_HIDE_VID;
            mHandler.sendMessage(msg);
        }
    }

    private ArrayList<VideoItemBean> clickListAnimation = new ArrayList<VideoItemBean>();

    private void startDoingBack(boolean isHide) {
        LeoEventBus.getDefaultBus().post(new MediaChangeEvent(false));

        mProcessNum = 0;
        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        String newFileName;
        Boolean isSuccess = true;
        ArrayList<VideoItemBean> list = (ArrayList<VideoItemBean>) mClickList.clone();
        clickListAnimation = list;
        if (list != null && list.size() > 0) {

            VideoItemBean vItem = list.get(0);
            String mPath = vItem.getPath();
            mTwoName = FileOperationUtil.getDirNameFromFilepath(mPath);
            mOneName = FileOperationUtil
                    .getSecondDirNameFromFilepath(mPath);
            pdm.unregisterMediaListener();
            if (isHide) {
                for (VideoItemBean item : list) {
                    if (!mIsBackgoundRunning) { break; }

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
                        // if cb can not do this , pg do this
                        if (!isSuccess) {
                            newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                            newFileName = newFileName + ".leotmv";
                            boolean isHideSuccees = ((PrivacyDataManager) MgrContext.
                                    getManager(MgrContext.MGR_PRIVACY_DATA)).
                                    onHideVid(item.getPath(), "");
                            if (isHideSuccees) {
                                FileOperationUtil.saveFileMediaEntry(FileOperationUtil.makePath(
                                        FileOperationUtil.getDirPathFromFilepath(item.getPath()),
                                        newFileName), this);
                                FileOperationUtil.deleteVideoMediaEntry(item.getPath(), this);
                                mVideoItems.remove(item);
                                SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb", "hide_done");
                            } else {
                                mUnhidePath.remove(item.getPath());
                                isSuccess = false;
                            }
                        }
                    } else {
                        newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                        try {
                            newFileName = newFileName + ".leotmv";

                            boolean isHideSuccees = ((PrivacyDataManager) MgrContext.
                                    getManager(MgrContext.MGR_PRIVACY_DATA)).
                                    onHideVid(item.getPath(), "");

                            if (isHideSuccees) {
                                FileOperationUtil.saveFileMediaEntry(FileOperationUtil.makePath(
                                        FileOperationUtil.getDirPathFromFilepath(item.getPath()), newFileName), this);
                                FileOperationUtil.deleteVideoMediaEntry(item.getPath(), this);
                                mVideoItems.remove(item);
                                SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb", "hide_done");
                                mProcessNum++;
                            } else {
                                mUnhidePath.remove(item.getPath());
                                isSuccess = false;
                                SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hide_vid_operation", "vid_hid_fal");
                            }
                        } catch (Exception e) {
                            isSuccess = false;
                        }

                    }
                }
            } else {
                for (VideoItemBean item : list) {
                    if (!mIsBackgoundRunning)
                        break;
                    if (isServiceDo) {
                        LeoLog.d("testcancelHide", "isServiceDo");
                        int mProcessType = -1;
                        try {
                            mProcessType = mService.cancelHide(item.getPath());
                        } catch (RemoteException e) {
                            isSuccess = false;
                        }
                        if (mProcessType == 0) {
                            mVideoItems.remove(item);
                        } else if (mProcessType == -1) {
                            isSuccess = false;
                        }
                        // if cb can not do this , pg do this
                        if (!isSuccess) {
                            LeoLog.d("testcancelHide", "CB do not cancel , pg do");
                            try {
                                newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                                newFileName = newFileName.substring(0, newFileName.indexOf(".leotmv"));

                                boolean isUnHideSuccees = ((PrivacyDataManager) MgrContext.
                                        getManager(MgrContext.MGR_PRIVACY_DATA)).
                                        cancelHideVid(item.getPath());

                                if (isUnHideSuccees) {
                                    FileOperationUtil.saveVideoMediaEntry(FileOperationUtil.makePath(
                                            FileOperationUtil .getDirPathFromFilepath(item .getPath()), newFileName), this);
                                    FileOperationUtil.deleteFileMediaEntry(item.getPath(), this);
                                    mVideoItems.remove(item);
                                }
                            } catch (Exception e) {
                                isSuccess = false;
                            }
                        }
                    } else {
                        newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                        try {
                            newFileName = newFileName.substring(0, newFileName.indexOf(".leotmv"));
                            boolean isUnHideSuccees = ((PrivacyDataManager) MgrContext.
                                    getManager(MgrContext.MGR_PRIVACY_DATA)).
                                    cancelHideVid(item.getPath());
                            if (isUnHideSuccees) {
                                FileOperationUtil.saveVideoMediaEntry(FileOperationUtil.makePath(
                                        FileOperationUtil.getDirPathFromFilepath(item.getPath()), newFileName), this);
                                FileOperationUtil.deleteFileMediaEntry(item.getPath(), this);
                                LeoLog.d("testcancelHide", "cancel success");
                                mVideoItems.remove(item);
                                mProcessNum++;
                            } else {
                                isSuccess = false;
                                SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hide_vid_operation", "vid_ccl_fal");
                            }
                        } catch (Exception e) {
                            isSuccess = false;
                        }
                    }
                }

                // 取消隐藏不算新增
                pdm.haveCheckedVid();
            }
            //refresh by itself
            pdm.registerMediaListener();
            pdm.notifySecurityChange();

        }
        readyDoingDone(isSuccess, isHide);
    }

    private void readyDoingDone(Boolean isSuccess, Boolean isHide) {
        if (mHandler != null) {
            Message msg = new Message();

            Bundle bundle = new Bundle();
            bundle.putBoolean("isSuccess", isSuccess);
            bundle.putBoolean("isHide", isHide);
            msg.setData(bundle);//bundle传值，耗时，效率低

            msg.what = CANCEL_OR_HIDE_FINISH;

            mHandler.sendMessage(msg);
        }
    }

    private void onPostDo(boolean isSuccess) {
        mClickList.clear();
        if (!isSuccess) {
            LeoLog.d("testcancelHide", "onPostDo not success");
            mSelectAll.setText(R.string.app_select_all);
            if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                if (mTwoName.equals(VideoHideMainActivity.LAST_CATALOG)
                        && mOneName.equals(VideoHideMainActivity.SECOND_CATALOG)) {
                    if (isServiceDo) {
                        Toast.makeText(VideoGriActivity.this,
                                getString(R.string.video_cencel_hide_fail),
                                Toast.LENGTH_SHORT).show();
                        SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb ",
                                "fail_toast");
                    } else {
                        String mContentString;
                        if (!isCbHere) {// no cb
                            mContentString = getString(R.string.video_hide_need_cb);
                            showDownLoadNewCbDialog(mContentString);
                        } else if (!isHaveServiceToBind) {
                            mContentString = getString(R.string.video_hide_need_new_cb);
                            showDownLoadNewCbDialog(mContentString);
                        } else {
                            Toast.makeText(VideoGriActivity.this,
                                    getString(R.string.video_cencel_hide_fail),
                                    Toast.LENGTH_SHORT).show();
                            SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1,
                                    "hidevd_cb ",
                                    "fail_toast");
                        }
                    }
                } else {
                    Toast.makeText(VideoGriActivity.this,
                            getString(R.string.video_cencel_hide_fail),
                            Toast.LENGTH_SHORT).show();
                    SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb ",
                            "fail_toast");
                }
                SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb ",
                        "unhide_fail");
            } else {
                Toast.makeText(VideoGriActivity.this, getString(R.string.app_hide_video_fail),
                        Toast.LENGTH_SHORT).show();
                SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb",
                        "hide_fail");
            }
        } else {
            LeoLog.d("testcancelHide", "onPostDo success::" + mActivityMode);
            if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
                SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb ",
                        "unhide_done");
            } else if (mActivityMode == Constants.SELECT_HIDE_MODE) {
                if (!VideoHideMainActivity.mIsFromConfirm) {
                    LeoEventBus.getDefaultBus().postSticky(new GradeEvent(GradeEvent.FROM_VID, true));
                }
            }
        }
        dismissProgressDialog();
        LeoLog.d("testcancelHide", "mVideoItems size : " + mVideoItems.size());
        if (mVideoItems.size() > 0) {
            animateReorder();
            updateRightButton();
            if (mHideVideoAdapter != null) {
                mHideVideoAdapter.notifyDataSetChanged();
            }
        } else {
            finish();
        }
        isServiceDo = false;
    }

    private void onPreDo() {
        mIsBackgoundRunning = true;
    }

    protected void showDownLoadNewCbDialog(String mContent) {
        SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb",
                "cbdialogue");
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(this);
        }
        mDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    // getURL and go browser
                    requestUrl();
                    SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb",
                            "cbdialogue_y");
                } else {
                    SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb",
                            "cbdialogue_n");
                }
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setContent(mContent);
        mDialog.setSureButtonText(getString(R.string.button_install));
        mDialog.show();
    }

    private void requestUrl() {
        String CB_FINAL_URL = VideoHideMainActivity.URL_CB + "?id="
                + this.getString(R.string.channel_code);
        Uri uri = Uri.parse(CB_FINAL_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        this.startActivity(intent);
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
//    private class BackgoundTask extends AsyncTask<Boolean, Integer, Boolean> {
//        private Context context;
//
//        BackgoundTask(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            mIsBackgoundRunning = true;
//        }
//
//        @Override
//        protected Boolean doInBackground(Boolean... params) {
//            String newFileName;
//            Boolean isSuccess = true;
//            boolean isHide = params[0];
//            ArrayList<VideoItemBean> list = (ArrayList<VideoItemBean>) mClickList.clone();
//            if (list != null && list.size() > 0) {
//
//                VideoItemBean vItem = list.get(0);
//                String mPath = vItem.getPath();
//                mTwoName = FileOperationUtil.getDirNameFromFilepath(mPath);
//                mOneName = FileOperationUtil
//                        .getSecondDirNameFromFilepath(mPath);
//                ((PrivacyDataManager) MgrContext.getManager
//                        (MgrContext.MGR_PRIVACY_DATA)).unregisterMediaListener();
//                if (isHide) {
//                    for (VideoItemBean item : list) {
//                        if (!mIsBackgoundRunning)
//                            break;
//                        mUnhidePath.add(item.getPath());
//                        if (isServiceDo) {
//
//                            int mProcessType = -1;
//                            try {
//                                mProcessType = mService.hideVideo(item.getPath());
//                            } catch (RemoteException e) {
//                                isSuccess = false;
//                            }
//                            if (mProcessType == 0) {
//                                mVideoItems.remove(item);
//                            } else if (mProcessType == -1) {
//                                mUnhidePath.remove(item.getPath());
//                                isSuccess = false;
//                            }
//
//                            // if cb can not do this , pg do this
//                            if (!isSuccess) {
//                                newFileName =
//                                        FileOperationUtil.getNameFromFilepath(item.getPath());
//                                newFileName = newFileName + ".leotmv";
//
//                                boolean isHideSuccees = ((PrivacyDataManager) MgrContext.
//                                        getManager(MgrContext.MGR_PRIVACY_DATA)).
//                                        onHideVid(item.getPath(), "");
//
//                                if (isHideSuccees) {
//                                    FileOperationUtil.saveFileMediaEntry(FileOperationUtil
//                                            .makePath(
//                                                    FileOperationUtil.getDirPathFromFilepath(item
//                                                            .getPath()),
//                                                    newFileName), context);
//                                    FileOperationUtil.deleteVideoMediaEntry(item.getPath(),
//                                            context);
//
//                                    mVideoItems.remove(item);
//                                    SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1,
//                                            "hidevd_cb",
//                                            "hide_done");
//
//                                } else {
//                                    mUnhidePath.remove(item.getPath());
//                                    isSuccess = false;
//                                }
//                            }
//
//                        } else {
//                            newFileName =
//                                    FileOperationUtil.getNameFromFilepath(item.getPath());
//                            try {
//                                newFileName = newFileName + ".leotmv";
//
//                                boolean isHideSuccees = ((PrivacyDataManager) MgrContext.
//                                        getManager(MgrContext.MGR_PRIVACY_DATA)).
//                                        onHideVid(item.getPath(), "");
//
//                                if (isHideSuccees) {
//                                    LeoLog.d("testVedio", "newFileName: " + newFileName);
//                                    LeoLog.d("testVedio", "old path: " + item.getPath());
//                                    LeoLog.d("testVedio", "new path2: " + FileOperationUtil
//                                            .makePath(
//                                                    FileOperationUtil
//                                                            .getDirPathFromFilepath(item
//                                                                    .getPath()),
//                                                    newFileName));
//                                    FileOperationUtil.saveFileMediaEntry(FileOperationUtil
//                                            .makePath(
//                                                    FileOperationUtil
//                                                            .getDirPathFromFilepath(item
//                                                                    .getPath()),
//                                                    newFileName), context);
////                                    FileOperationUtil.saveVideoMediaEntry(FileOperationUtil
////                                            .makePath(
////                                                    FileOperationUtil
////                                                            .getDirPathFromFilepath(item
////                                                                    .getPath()), newFileName), context);
//
////                                    FileOperationUtil.updateVedioMediaEntry(item.getPath(), true, "text/plain", context);
//                                    FileOperationUtil.deleteVideoMediaEntry(item.getPath(),
//                                            context);
//
//                                    mVideoItems.remove(item);
//                                    SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1,
//                                            "hidevd_cb",
//                                            "hide_done");
//
//                                } else {
//                                    mUnhidePath.remove(item.getPath());
//                                    isSuccess = false;
//                                }
//                                // }
//
//                            } catch (Exception e) {
//                                return isSuccess = false;
//                            }
//
//                        }
//                    }
//                } else {
//                    for (VideoItemBean item : list) {
//                        if (!mIsBackgoundRunning)
//                            break;
//
//                        if (isServiceDo) {
//                            int mProcessType = -1;
//                            try {
//                                mProcessType =
//                                        mService.cancelHide(item.getPath());
//                            } catch (RemoteException e) {
//                                isSuccess = false;
//                            }
//                            if (mProcessType == 0) {
//                                mVideoItems.remove(item);
//                            } else if (mProcessType == -1) {
//                                isSuccess = false;
//                            }
//
//                            // if cb can not do this , pg do this
//                            if (!isSuccess) {
//                                try {
//                                    newFileName =
//                                            FileOperationUtil.getNameFromFilepath(item.getPath());
//                                    newFileName = newFileName.substring(0,
//                                            newFileName.indexOf(".leotmv"));
//
//                                    boolean isUnHideSuccees = ((PrivacyDataManager) MgrContext.
//                                            getManager(MgrContext.MGR_PRIVACY_DATA)).
//                                            cancelHideVid(item.getPath());
//
//                                    if (isUnHideSuccees) {
//                                        FileOperationUtil.saveVideoMediaEntry(FileOperationUtil
//                                                .makePath(
//                                                        FileOperationUtil
//                                                                .getDirPathFromFilepath(item
//                                                                        .getPath()), newFileName), context);
////                                        FileOperationUtil.saveImageMediaEntry(FileOperationUtil
////                                                .makePath(
////                                                        FileOperationUtil
////                                                                .getDirPathFromFilepath(item
////                                                                        .getPath()),
////                                                        newFileName), context);
//                                        FileOperationUtil.deleteFileMediaEntry(item.getPath(),
//                                                context);
//                                        mVideoItems.remove(item);
//                                    }
//                                } catch (Exception e) {
//                                    isSuccess = false;
//                                }
//                            }
//
//                        } else {
//                            newFileName =
//                                    FileOperationUtil.getNameFromFilepath(item.getPath());
//                            try {
//                                LeoLog.d("testVedio", "before newFileName: " + newFileName);
////                                newFileName = newFileName.substring(1,
////                                        newFileName.indexOf(".leotmv"));
//                                newFileName = newFileName.substring(0,
//                                        newFileName.indexOf(".leotmv"));
//                                LeoLog.d("testVedio", "after newFileName: " + newFileName);
//                                boolean isUnHideSuccees = ((PrivacyDataManager) MgrContext.
//                                        getManager(MgrContext.MGR_PRIVACY_DATA)).
//                                        cancelHideVid(item.getPath());
//
//                                if (isUnHideSuccees) {
//
////                                    FileOperationUtil.saveImageMediaEntry(FileOperationUtil
////                                            .makePath(
////                                                    FileOperationUtil
////                                                            .getDirPathFromFilepath(item
////                                                                    .getPath()),
////                                                    newFileName), context);
//                                    FileOperationUtil.saveVideoMediaEntry(FileOperationUtil
//                                            .makePath(
//                                                    FileOperationUtil
//                                                            .getDirPathFromFilepath(item
//                                                                    .getPath()), newFileName), context);
////                                    FileOperationUtil.updateVedioMediaEntry(FileOperationUtil
////                                            .makePath(
////                                                    FileOperationUtil
////                                                            .getDirPathFromFilepath(item
////                                                                    .getPath()),
////                                                    newFileName), false, "video/mp4", context);
//                                    FileOperationUtil.deleteFileMediaEntry(item.getPath(),
//                                            context);
//                                    mVideoItems.remove(item);
//                                } else {
//                                    return isSuccess = false;
//                                    // }
//                                }
//                            } catch (Exception e) {
//                                return isSuccess = false;
//                            }
//                        }
//                    }
//                }
//                //refresh by itself
//                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
//                pdm.notifySecurityChange();
//                ((PrivacyDataManager) MgrContext.getManager
//                        (MgrContext.MGR_PRIVACY_DATA)).registerMediaListener();
//                ((PrivacyDataManager) MgrContext.getManager
//                        (MgrContext.MGR_PRIVACY_DATA)).notifySecurityChange();
//            }
//            return isSuccess;
//        }
//
//
//        @Override
//        protected void onPostExecute(final Boolean isSuccess) {
//            mClickList.clear();
//            if (!isSuccess) {
//                mSelectAll.setText(R.string.app_select_all);
//                if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
//                    if (mTwoName.equals(VideoHideMainActivity.LAST_CATALOG)
//                            && mOneName.equals(VideoHideMainActivity.SECOND_CATALOG)) {
//                        if (isServiceDo) {
//                            Toast.makeText(VideoGriActivity.this,
//                                    getString(R.string.video_cencel_hide_fail),
//                                    Toast.LENGTH_SHORT).show();
//                            SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb ",
//                                    "fail_toast");
//                        } else {
//                            String mContentString;
//                            if (!isCbHere) {// no cb
//                                mContentString = getString(R.string.video_hide_need_cb);
//                                showDownLoadNewCbDialog(mContentString);
//                            } else if (!isHaveServiceToBind) {
//                                mContentString = getString(R.string.video_hide_need_new_cb);
//                                showDownLoadNewCbDialog(mContentString);
//                            } else {
//                                Toast.makeText(VideoGriActivity.this,
//                                        getString(R.string.video_cencel_hide_fail),
//                                        Toast.LENGTH_SHORT).show();
//                                SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1,
//                                        "hidevd_cb ",
//                                        "fail_toast");
//                            }
//                        }
//                    } else {
//                        Toast.makeText(VideoGriActivity.this,
//                                getString(R.string.video_cencel_hide_fail),
//                                Toast.LENGTH_SHORT).show();
//                        SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb ",
//                                "fail_toast");
//                    }
//                    SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb ",
//                            "unhide_fail");
//                } else {
//                    Toast.makeText(VideoGriActivity.this, getString(R.string.app_hide_video_fail),
//                            Toast.LENGTH_SHORT).show();
//                    SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb",
//                            "hide_fail");
//                }
//            } else {
//                if (mActivityMode == Constants.CANCLE_HIDE_MODE) {
//                    SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1, "hidevd_cb ",
//                            "unhide_done");
//                }
//            }
//            dismissProgressDialog();
//            if (mVideoItems.size() > 0) {
//                animateReorder();
//                updateRightButton();
//                if (mHideVideoAdapter != null) {
//                    mHideVideoAdapter.notifyDataSetChanged();
//                }
//            } else {
//                finish();
//            }
//            isServiceDo = false;
//            // video change, recompute privacy level
////            PrivacyHelper.getInstance(VideoGriActivity.this).computePrivacyLevel(
////                    PrivacyHelper.VARABLE_HIDE_VIDEO);
//        }
//    }
    private void showProgressDialog(String title, String message, boolean indeterminate,
                                    boolean cancelable) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEOCircleProgressDialog(this);
            mProgressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mIsBackgoundRunning = false;
                    mSelectAll.setText(R.string.app_select_all);
                    mSelectAll.setCompoundDrawablesWithIntrinsicBounds(null,
                            getResources().getDrawable(R.drawable.select_all_selector), null,
                            null);
                    SDKWrapper.addEvent(VideoGriActivity.this, SDKWrapper.P1,
                            "hide_vid_operation",
                            "vid_ccl_cnts");
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
        if (clickListAnimation != null && clickListAnimation.size() > 0) {
            if (length > clickListAnimation.size()) {
                length = clickListAnimation.size();
            }
        }

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
