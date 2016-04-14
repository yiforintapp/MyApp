package com.leo.appmaster.imagehide;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
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
import com.leo.appmaster.ui.dialog.OneButtonDialog;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageDownloader;
import com.leo.imageloader.core.ImageScaleType;
import com.leo.imageloader.core.ImageSize;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ImageGridActivity extends BaseFragmentActivity implements OnClickListener, OnItemClickListener {

    public static final String TAG = "ImageGridActivity";

    public final static int INIT_UI_DONE = 24;
    public final static int LOAD_DATA_DONE = 25;
    public final static int START_CANCEL_OR_HIDE_PIC = 26;
    public final static int HIDE_FINISH = 27;

    public final static int CANCEL_HIDE_MODE = 0;
    public final static int SELECT_HIDE_MODE = 1;
    private int mActicityMode = SELECT_HIDE_MODE;

    private CommonToolbar mTtileBar;
    private PhotoAibum mPhotoAibum;
    private int mPhotoAibumPos;
    private GridView mGridView;
    private ImageAdapter mImageAdapter;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private LinearLayout mBottomBar;
    private Button mSelectAll;
    private Button mHidePicture;

    private ArrayList<PhotoItem> mClickList = new ArrayList<PhotoItem>();
    private LinkedList<Integer> mClickPosList = new LinkedList<Integer>();
    private List<PhotoItem> mPicturesList = new ArrayList<PhotoItem>();
    private ArrayList<String> mAllListPath = new ArrayList<String>();

    private boolean mIsEditmode = false;
    private LEOCircleProgressDialog mProgressDialog;
    private LEOAlarmDialog mDialog;
    private OneButtonDialog memeryDialog;
    private boolean mIsBackgoundRunning = false;
    private ProgressBar mLoadingBar;
    private Boolean mIsFromIntruderMore = false;
    private Boolean isLoadDone = false;

    private GuideFragment mGuideFragment;
    private boolean mPicGuide;
    private boolean isFristIn = true;
    //通过拷贝方式隐藏图片的方式，删除源文件是否成功
    private boolean mIsDeletSucFromDatebase = true;
    private int mProcessNum = 0;

    private ImageSize mImageSize;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(final android.os.Message msg) {
            switch (msg.what) {
                case INIT_UI_DONE:
                    asyncLoad();
                    break;
                case LOAD_DATA_DONE:
                    loadDone();
                    break;
                case START_CANCEL_OR_HIDE_PIC:
                    final boolean isHide = (Boolean) msg.obj;
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            startDoingBack(isHide);
                        }
                    });
                    break;
                case HIDE_FINISH:
                    Bundle bundle = msg.getData();
                    String isSuccess = bundle.getString("isSuccess");
                    boolean isHideb = bundle.getBoolean("isHide");

                    checkLostPic(isHideb);

                    onPostDo(isSuccess);
                    break;
            }
        }
    };

    private void checkLostPic(final boolean isHide) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager mPDManager = (PrivacyDataManager) MgrContext
                        .getManager(MgrContext.MGR_PRIVACY_DATA);
                int saveNum = LeoSettings.getInteger(Constants.HIDE_PICS_NUM, -1);
                LeoLog.d("checkLostPic", "savenum : " + saveNum);
                int num = mPDManager.getHidePicsRealNum();
                LeoLog.d("checkLostPic", "hide pic num : " + num);
                if (saveNum != -1) {
                    if (isHide) {
                        LeoLog.d("checkLostPic", "isHide process num : " + mProcessNum);
                        int targetNum = saveNum + mProcessNum;
                        if (num >= targetNum) {
                            LeoLog.d("checkLostPic", "everything ok");
                            LeoSettings.setInteger(Constants.HIDE_PICS_NUM, num);
                        } else {
                            LeoLog.d("checkLostPic", "lost pic");
                            mPDManager.reportDisappearError(true, PrivacyDataManager.LABEL_DEL_BY_SELF);
                            LeoSettings.setInteger(Constants.HIDE_PICS_NUM, num);
                        }
                    } else {
                        LeoLog.d("checkLostPic", "cancelHide process num : " + mProcessNum);
                        int targetNum = saveNum - mProcessNum;
                        if (num >= targetNum) {
                            LeoLog.d("checkLostPic", "everything ok");
                            LeoSettings.setInteger(Constants.HIDE_PICS_NUM, num);
                        } else {
                            LeoLog.d("checkLostPic", "lost pic");
                            mPDManager.reportDisappearError(true, PrivacyDataManager.LABEL_DEL_BY_SELF);
                            LeoSettings.setInteger(Constants.HIDE_PICS_NUM, num);
                        }
                    }
                } else {
                    LeoSettings.setInteger(Constants.HIDE_PICS_NUM, num);
                }
            }
        });
    }


    private void loadDone() {
        isLoadDone = true;
        mLoadingBar.setVisibility(View.GONE);
        mGridView.setVisibility(View.VISIBLE);
        if (mPhotoAibum != null) {
            mPicturesList = ((PrivacyDataManager) MgrContext.
                    getManager(MgrContext.MGR_PRIVACY_DATA)).getHidePicFile(mPhotoAibum);
            mTtileBar.setToolbarTitle(mPhotoAibum.getName());
            for (PhotoItem item : mPicturesList) {
                mAllListPath.add(item.getPath());
            }
        }

        mImageAdapter = new ImageAdapter();
        mGridView.setAdapter(mImageAdapter);
        mSelectAll.setEnabled(true);
    }

    private void asyncLoad() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                if (mActicityMode == SELECT_HIDE_MODE) {
                    mPhotoAibum = ((PrivacyDataManager) MgrContext.
                            getManager(MgrContext.MGR_PRIVACY_DATA)).getAllPicFile("").get(
                            mPhotoAibumPos);
                } else {
                    mPhotoAibum = ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).
                            getHidePicAlbum("").get(mPhotoAibumPos);
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(LOAD_DATA_DONE);
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_grid);
        mGridView = (GridView) findViewById(R.id.image_gallery_image);
        mBottomBar = (LinearLayout) findViewById(R.id.bottom_bar);
        mSelectAll = (Button) findViewById(R.id.select_all);
        mSelectAll.setEnabled(false);
        mHidePicture = (Button) findViewById(R.id.hide_image);
        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTtileBar.setToolbarTitle("");

        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_pic);
        onInit();
        initImageLoder();
        loadImageList();
        mGridView.setOnItemClickListener(this);
        mSelectAll.setOnClickListener(this);
        mHidePicture.setOnClickListener(this);
    }

    private void updateRightButton() {
        if (mClickList.size() > 0) {
            if (mActicityMode == SELECT_HIDE_MODE) {
                mHidePicture.setText(getString(R.string.app_hide_image) + "("
                        + mClickList.size() + ")");
            } else if (mActicityMode == CANCEL_HIDE_MODE) {
                mHidePicture.setText(getString(R.string.app_cancel_hide_image)
                        + "(" + mClickList.size() + ")");
            }
            mHidePicture.setEnabled(true);
        } else {
            if (mActicityMode == SELECT_HIDE_MODE) {
                mHidePicture.setText(getString(R.string.app_hide_image));
            } else if (mActicityMode == CANCEL_HIDE_MODE) {
                mHidePicture.setText(getString(R.string.app_cancel_hide_image));
            }
            mHidePicture.setEnabled(false);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        if (!mPicGuide) {
            cancelGuide();
        }
        if (mActicityMode == CANCEL_HIDE_MODE && mIsEditmode) {
            cancelEditMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LeoLog.d("teststartResult", "onActivityResult");
        if (resultCode == RESULT_OK) {
            mAllListPath = data.getStringArrayListExtra("list");
            LeoLog.d("teststartResult", "mAllListPath size : " + mAllListPath.size());

            boolean needRefresh = false;
            for (Iterator iterator = mPicturesList.iterator(); iterator
                    .hasNext(); ) {
                PhotoItem item = (PhotoItem) iterator.next();
                if (!mAllListPath.contains(item.getPath())) {
                    iterator.remove();
                    needRefresh = true;
                }
            }
            if (mImageAdapter != null && needRefresh) {
                mImageAdapter.notifyDataSetChanged();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initImageLoder() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.img_vid_loading)
                .showImageForEmptyUri(R.drawable.img_vid_loading)
                .showImageOnFail(R.drawable.img_vid_loading)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();
        mImageSize = ImagePreviewUtil.getPreviewSize();

        mImageLoader = ImageLoader.getInstance();
    }

    private void onInit() {
        Intent intent = getIntent();
        mActicityMode = intent.getIntExtra("mode", SELECT_HIDE_MODE);
        mIsFromIntruderMore = intent.getBooleanExtra("fromIntruderMore", false);
        mPhotoAibum = (PhotoAibum) intent.getExtras().get("data");
        if (mPhotoAibum == null) {
            mPhotoAibumPos = (int) intent.getIntExtra("pos", 0);
        }

        if (mPhotoAibum != null) {
            isLoadDone = true;
            mPicturesList = ((PrivacyDataManager) MgrContext.
                    getManager(MgrContext.MGR_PRIVACY_DATA)).getHidePicFile(mPhotoAibum);
            mTtileBar.setToolbarTitle(mPhotoAibum.getName());
            mTtileBar.setOptionMenuVisible(false);
            for (PhotoItem item : mPicturesList) {
                mAllListPath.add(item.getPath());
            }
        }
        if (mActicityMode == SELECT_HIDE_MODE) {
            mHidePicture.setText(R.string.app_hide_image);
        } else if (mActicityMode == CANCEL_HIDE_MODE) {
            mHidePicture.setText(R.string.app_cancel_hide_image);
//            Drawable topDrawable = getResources().getDrawable(
//                    R.drawable.unhide_picture_selector);
//            topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(),
//                    topDrawable.getMinimumHeight());
//            mHidePicture.setCompoundDrawables(null, topDrawable, null, null);
            mTtileBar.setOptionMenuVisible(true);
            mTtileBar.setOptionImageResource(R.drawable.edit_mode_name);
            mTtileBar.setOptionClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mActicityMode == CANCEL_HIDE_MODE && isLoadDone) {
                        mIsEditmode = !mIsEditmode;
                        if (!mIsEditmode) {
                            cancelEditMode();
                        } else {
                            mBottomBar.setVisibility(View.VISIBLE);
                            mTtileBar.setOptionImageResource(R.drawable.mode_done);
                        }
                        mSelectAll.setText(R.string.app_select_all);
                        mSelectAll.setCompoundDrawablesWithIntrinsicBounds(null,
                                getResources().getDrawable(R.drawable.select_all_selector), null,
                                null);
                        mImageAdapter.notifyDataSetChanged();
                    }
                    if (!mPicGuide) {
                        cancelGuide();
                    }
                }
            });
            mBottomBar.setVisibility(View.GONE);
            LeoPreference pre = LeoPreference.getInstance();
            mPicGuide = pre.getBoolean(PrefConst.KEY_PIC_EDIT_GUIDE, false);
            if (!mPicGuide) {
                mGuideFragment = (GuideFragment) getSupportFragmentManager().findFragmentById(R.id.pic_guide);
                mGuideFragment.setEnable(true, GuideFragment.GUIDE_TYPE.PIC_GUIDE);
                mTtileBar.setNavigationClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelGuide();
                        finish();
                    }
                });
            }
        }

    }

    private void cancelGuide() {
        if (mGuideFragment != null) {
            mGuideFragment.setEnable(false, GuideFragment.GUIDE_TYPE.PIC_GUIDE);
            LeoPreference pre = LeoPreference.getInstance();
            pre.putBoolean(PrefConst.KEY_PIC_EDIT_GUIDE, true);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "hidpic_bub_cnts");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (!isFristIn) {
//            mLoadingBar.setVisibility(View.VISIBLE);
//            mGridView.setVisibility(View.GONE);
//            mHandler.sendEmptyMessage(INIT_UI_DONE);
//        } else {
//            isFristIn = false;
//        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mImageLoader != null) {
            mImageLoader.stop();
            mImageLoader.clearMemoryCache();
        }

    }

    @Override
    public void finish() {
        super.finish();
        if (mImageLoader != null) {
            mImageLoader.stop();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View conView, int position, long l) {
        if (mActicityMode == CANCEL_HIDE_MODE && !mIsEditmode
                && mAllListPath.size() > 0) {
            Intent intent = new Intent(ImageGridActivity.this,
                    PictureViewPager.class);
            if (mIsFromIntruderMore) {
                intent.putExtra("fromIntruderMore", true);
            }
            intent.putStringArrayListExtra("list", mAllListPath);
            LeoLog.d("teststartResult", "ready start Activity , list size : " + mAllListPath.size());
            intent.putExtra("pos", position);
            startActivityForResult(intent, 0);
            SDKWrapper.addEvent(ImageGridActivity.this, SDKWrapper.P1,
                    "hide_pic_operation",
                    "pic_viw_cnts");
        } else {
            ImageView cView = (ImageView) conView
                    .findViewById(R.id.photo_select);
            if (!mClickList.contains(mPicturesList.get(position))) {
                cView.setImageResource(R.drawable.ic_check_checked);
                mClickList.add(mPicturesList.get(position));
                mClickPosList.add((Integer) position);
            } else {
                cView.setImageResource(R.drawable.ic_media_check_normal);//pic_choose_normal
                mClickList.remove(mPicturesList.get(position));
                mClickPosList.remove((Integer) position);
            }
            if (mClickList.size() < mPicturesList.size()) {
                mSelectAll.setCompoundDrawablesWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.select_all_selector), null,
                        null);
                mSelectAll.setText(R.string.app_select_all);
            } else {
                mSelectAll.setCompoundDrawablesWithIntrinsicBounds(null,
                        getResources().getDrawable(R.drawable.no_select_all_selector),
                        null,
                        null);
                mSelectAll.setText(R.string.app_select_none);
            }
            updateRightButton();
        }
    }

    public class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (mPhotoAibum == null)
                return 0;
            return mPhotoAibum.getBitList().size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                holder.clickView = (ImageView) view.findViewById(R.id.photo_select);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (position < mPicturesList.size()) {
                PhotoItem item = mPicturesList.get(position);
                String path = item.getPath();
                if (mActicityMode == CANCEL_HIDE_MODE && !mIsEditmode) {
                    holder.clickView.setVisibility(View.GONE);
                } else {
                    holder.clickView.setVisibility(View.VISIBLE);
                    if (mClickList.contains(item)) {
                        holder.clickView.setImageResource(R.drawable.ic_check_checked);
                    } else {
                        holder.clickView.setImageResource(R.drawable.ic_media_check_normal);
                    }
                }
                String uri = null;
                if (path != null && path.endsWith(Constants.CRYPTO_SUFFIX)) {
                    uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
                } else {
                    uri = ImageDownloader.Scheme.FILE.wrap(path);
                }
                mImageLoader.displayImage(uri, holder.imageView, mOptions, mImageSize);
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LeoLog.i("w_h", "holder.imageView : " + holder.imageView.getWidth() + "  " + holder.imageView.getHeight());
                    }
                }, 2000);
            }
            return view;
        }

        class ViewHolder {
            ImageView imageView;
            ImageView clickView;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_all:
                if (mClickList.size() < mPicturesList.size()) {
                    mClickList.clear();
                    mClickList.addAll(mPicturesList);
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
                mImageAdapter.notifyDataSetChanged();
                break;
            case R.id.hide_image:
                showAlarmDialog();
                break;
            default:
                break;
        }
    }

    private void cancelEditMode() {
        mIsEditmode = false;
        mClickList.clear();
        mImageAdapter.notifyDataSetChanged();
        mBottomBar.setVisibility(View.GONE);
        mTtileBar.setOptionImageResource(R.drawable.edit_mode_name);
        updateRightButton();
    }

//    private class BackgoundTask extends AsyncTask<Boolean, Integer, Integer> {
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
//        protected Integer doInBackground(Boolean... params) {
//            long a = System.currentTimeMillis();
//            LeoLog.d("testPicLoadTime", "doInBackground");
//
//            String newFileName;
//            int isSuccess = 3;
//            boolean isHide = params[0];
//            try {
//                if (mClickList != null && mClickList.size() > 0) {
//                    ArrayList<PhotoItem> list = (ArrayList<PhotoItem>) mClickList.clone();
//                    Iterator<PhotoItem> iterator = list.iterator();
//                    PhotoItem item;
//                    ArrayList<PhotoItem> deleteList = new ArrayList<PhotoItem>();
//                    ((PrivacyDataManager) MgrContext.getManager
//                            (MgrContext.MGR_PRIVACY_DATA)).unregisterMediaListener();
//                    if (isHide) {
//                        while (iterator.hasNext()) {
//                            item = iterator.next();
//                            if (!mIsBackgoundRunning)
//                                break;
//
//                            String newPath = ((PrivacyDataManager) MgrContext.
//                                    getManager(MgrContext.MGR_PRIVACY_DATA)).
//                                    onHidePic(item.getPath(), "");
//
////                            newFileName = FileOperationUtil
////                                    .getNameFromFilepath(item.getPath());
////                            newFileName = newFileName + ".leotmi";
////                            String path = item.getPath();
////                            String newPath = FileOperationUtil.hideImageFile(context,
////                                    path, newFileName, mTotalSize);
//
//                            if (newPath != null) {
//                                if ("-2".equals(newPath)) {
//                                    isSuccess = -2;
////                                    Log.d("com.leo.appmaster.imagehide.ImageGridActivity",
////                                            "Hide rename image fail!");
//                                } else if ("0".equals(newPath)) {
//                                    isSuccess = 0;
//                                    // mPicturesList.remove(item);
//                                    // mAllListPath.remove(item.getPath());
//                                    deleteList.add(item);
//                                } else if ("-1".equals(newPath)) {
//                                    isSuccess = -1;
////                                    Log.d("com.leo.appmaster.imagehide.ImageGridActivity",
////                                            "Copy image fail!");
//                                } else if ("4".equals(newPath)) {
//                                    isSuccess = 4;
//                                    break;
//                                } else {
//                                    isSuccess = 3;
//                                    FileOperationUtil.saveFileMediaEntry(newPath,
//                                            context);
//                                    File file = new File(item.getPath());
//                                    if (!file.exists()) {
//                                        FileOperationUtil.deleteImageMediaEntry(item.getPath(), context);
//                                    }
//                                    // mPicturesList.remove(item);
//                                    // mAllListPath.remove(item.getPath());
//                                    deleteList.add(item);
//                                }
//                            } else {
//                                isSuccess = 2;
//                            }
//                        }
//                        if (deleteList.size() > 0) {
//                            mPicturesList.removeAll(deleteList);
//                            for (PhotoItem photoItem : deleteList) {
//                                mAllListPath.remove(photoItem.getPath());
//                            }
//                        }
//                    } else {
//                        long b = System.currentTimeMillis();
//                        LeoLog.d("testPicLoadTime", "ready cancel hide : " + (b - a));
//                        while (iterator.hasNext()) {
//                            long a1 = System.currentTimeMillis();
//                            LeoLog.d("testPicLoadTime", "ready operation");
//                            item = iterator.next();
//                            if (!mIsBackgoundRunning)
//                                break;
//
//                            String filepath = item.getPath();
//                            String newPaht = ((PrivacyDataManager) MgrContext.getManager
//                                    (MgrContext.MGR_PRIVACY_DATA)).cancelHidePic(filepath);
//
//                            long a2 = System.currentTimeMillis();
//                            LeoLog.d("testPicLoadTime", "operation1:" + (a2 - a1));
//
//                            if (newPaht == null) {
//                                isSuccess = 2;
//                            } else if ("-1".equals(newPaht) || "-2".equals(newPaht)) {
//                                //Copy Hide image fail!
//                                isSuccess = 2;
//                            } else if ("0".equals(newPaht)) {
//                                isSuccess = 3;
//                                ContentValues values = new ContentValues();
//                                values.put("image_path", filepath);
//                                getContentResolver().insert(Constants.IMAGE_HIDE_URI, values);
//                                // mPicturesList.remove(item);
//                                // mAllListPath.remove(item.getPath());
//                                deleteList.add(item);
//                            } else if ("4".equals(newPaht)) {
//                                isSuccess = 4;
//                                break;
//                            } else {
//                                isSuccess = 3;
//                                FileOperationUtil.saveImageMediaEntry(newPaht, context);
//                                FileOperationUtil.deleteFileMediaEntry(filepath, context);
//                                // mPicturesList.remove(item);
//                                // mAllListPath.remove(item.getPath());
//                                deleteList.add(item);
//                            }
//                            long a3 = System.currentTimeMillis();
//                            LeoLog.d("testPicLoadTime", "operation2:" + (a3 - a2));
//                        }
//                        long c = System.currentTimeMillis();
//                        LeoLog.d("testPicLoadTime", "finish operation : " + (c - b));
//                        if (deleteList.size() > 0) {
//                            mPicturesList.removeAll(deleteList);
//                            for (PhotoItem photoItem : deleteList) {
//                                mAllListPath.remove(photoItem.getPath());
//                            }
//
//                        }
//                    }
//                    ((PrivacyDataManager) MgrContext.getManager
//                            (MgrContext.MGR_PRIVACY_DATA)).registerMediaListener();
//                    //refresh by itself
//                    PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
//                    pdm.notifySecurityChange();
//                }
//                long d = System.currentTimeMillis();
//                LeoLog.d("testPicLoadTime", "total cost : " + (d - a));
//            } catch (Exception e) {
//            }
//            return isSuccess;
//        }
//
//        @Override
//        protected void onPostExecute(final Integer isSuccess) {
//            mClickList.clear();
//            if (isSuccess == 0) {
//                String title = getString(R.string.no_image_hide_dialog_title);
//                String content = getString(R.string.no_image_hide_dialog_content);
//                String rightBtn = getString(R.string.no_image_hide_dialog_button);
//                float width = getResources().getDimension(R.dimen.memery_dialog_button_width);
//                float height = getResources().getDimension(R.dimen.memery_dialog_button_height);
//                showMemeryAlarmDialog(title, content, null, rightBtn, false, true, width, height);
//            } else if (isSuccess == -1 || isSuccess == -2) {
////                Log.d("com.leo.appmaster.imagehide.ImageGridActivity", "Copy Hide  image fail!");
//            } else if (isSuccess == 2) {
////                Log.d("com.leo.appmaster.imagehide.ImageGridActivity", "Hide  image fail!");
//            } else if (isSuccess == 4) {
//                if (mActicityMode == SELECT_HIDE_MODE) {
//                    String title = getString(R.string.image_hide_memery_insuficient_dialog_title);
//                    String content = getString(R.string.image_hide_memery_insuficient_dialog_content);
//                    String rightBtn = getString(R.string.image_hide_memery_insuficient_dialog_button);
//                    float width = getResources().getDimension(
//                            R.dimen.memery_dialog_button_width);
//                    float height = getResources().getDimension(
//                            R.dimen.memery_dialog_button_height);
//                    showMemeryAlarmDialog(title, content, null, rightBtn, false, true,
//                            width, height);
//                } else if (mActicityMode == CANCEL_HIDE_MODE) {
//                    String title = getString(R.string.image_hide_memery_insuficient_dialog_title);
//                    String content = getString(R.string.image_unhide_memery_insuficient_dialog_content);
//                    String rightBtn = getString(R.string.image_hide_memery_insuficient_dialog_button);
//                    float width = getResources().getDimension(
//                            R.dimen.memery_dialog_button_width);
//                    float height = getResources().getDimension(
//                            R.dimen.memery_dialog_button_height);
//                    showMemeryAlarmDialog(title, content, null, rightBtn, false, true,
//                            width, height);
//                }
//            }
//            dismissProgressDialog();
//            if (mPicturesList.size() > 0) {
//                animateReorder();
//                updateRightButton();
//            } else {
//                if (isSuccess == 0) {
//                    if (mImageAdapter != null) {
//                        mImageAdapter.notifyDataSetChanged();
//                    }
//                } else {
//                    finish();
//                }
//            }
//        }
//    }

    private void animateReorder() {
        int length = mClickPosList.size();
        List<Animator> resultList = new LinkedList<Animator>();
        int fistVisblePos = mGridView.getFirstVisiblePosition();
        int lastVisblePos = mGridView.getLastVisiblePosition();
        int pos;
        final List<Integer> viewList = new LinkedList<Integer>();
        for (int i = 0; i < length; i++) {
            pos = mClickPosList.get(i);
            if (pos >= fistVisblePos && pos <= lastVisblePos) {
                View view = mGridView.getChildAt(pos - fistVisblePos);
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
                mImageAdapter.notifyDataSetChanged();
                for (Integer view : viewList) {
                    View child = mGridView.getChildAt(view);
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
        ObjectAnimator scaleX = ObjectAnimator
                .ofFloat(view, "scaleX", 1f, 0.5f);
        ObjectAnimator scaleY = ObjectAnimator
                .ofFloat(view, "scaleY", 1f, 0.5f);
        ObjectAnimator zoomIn = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        AnimatorSet animZoom = new AnimatorSet();
        animZoom.playTogether(scaleX, scaleY, zoomIn);
        return animZoom;
    }

    private AnimatorSet createTranslationAnimations(View view, float startX,
                                                    float endX, float startY, float endY) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
                startX, endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
                startY, endY);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        return animSetXY;
    }

    private void showProgressDialog(String title, String message,
                                    boolean indeterminate, boolean cancelable) {
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
                    SDKWrapper.addEvent(ImageGridActivity.this,
                            SDKWrapper.P1, "hide_pic_operation", "pic_ccl_cnts");
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
        // AM-737
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        } catch (Exception e) {

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
                        if (mActicityMode == SELECT_HIDE_MODE) {
                            showProgressDialog(getString(R.string.tips),
                                    getString(R.string.app_hide_image) + "...",
                                    true, true);
//                            BackgoundTask task = new BackgoundTask(
//                                    ImageGridActivity.this);
//                            task.execute(true);
                            doingBackGround(true);
                            if (ImageHideMainActivity.mFromHomeEnter) {
                                SDKWrapper.addEvent(ImageGridActivity.this, SDKWrapper.P1, "hide_pic", "pic_home_hide");
                            } else {
                                SDKWrapper.addEvent(ImageGridActivity.this, SDKWrapper.P1, "hide_pic", "pic_icon_hide");
                            }
                            SDKWrapper.addEvent(ImageGridActivity.this, SDKWrapper.P1, "hide_pic", "used");
                            SDKWrapper.addEvent(ImageGridActivity.this, SDKWrapper.P1, "hide_pic_operation", "pic_add_cnts");
                            SDKWrapper.addEvent(ImageGridActivity.this, SDKWrapper.P1, "hide_pic_operation", "pic_add_pics_$" + size);
                        } else if (mActicityMode == CANCEL_HIDE_MODE) {
                            showProgressDialog(getString(R.string.tips),
                                    getString(R.string.app_cancel_hide_image)
                                            + "...", true, true);
//                            BackgoundTask task = new BackgoundTask(
//                                    ImageGridActivity.this);
//                            task.execute(false);
                            doingBackGround(false);
                            SDKWrapper.addEvent(ImageGridActivity.this, SDKWrapper.P1, "hide_pic_operation", "pic_ccl_pics_$" + size);
                        }
                    }
                }

            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        if (mActicityMode == SELECT_HIDE_MODE) {
            mDialog.setTitle(R.string.app_hide_image);
            mDialog.setContent(getString(R.string.app_hide_pictures_dialog_content));
        } else if (mActicityMode == CANCEL_HIDE_MODE) {
            mDialog.setTitle(R.string.app_cancel_hide_image);
            mDialog.setContent(getString(R.string.app_unhide_pictures_dialog_content));
        }
        mDialog.show();
    }

    private void startDoingBack(boolean isHide) {
        LeoLog.d("testnewLoad", "isHide:" + isHide);
        LeoEventBus.getDefaultBus().post(new MediaChangeEvent(true));
        mProcessNum = 0;
        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        String isSuccess = FileOperationUtil.HIDE_PIC_SUCESS;
        try {

            //恢复隐藏方式默认值
            FileOperationUtil.setHideTpye(FileOperationUtil.DEF_HIDE);
            FileOperationUtil.setIsSdcCypTip(false);
            if (mClickList != null && mClickList.size() > 0) {
                ArrayList<PhotoItem> list = (ArrayList<PhotoItem>) mClickList.clone();
                Iterator<PhotoItem> iterator = list.iterator();
                PhotoItem item;
                ArrayList<PhotoItem> deleteList = new ArrayList<PhotoItem>();
                pdm.unregisterMediaListener();
                if (isHide) {
                    while (iterator.hasNext()) {
                        item = iterator.next();
                        if (!mIsBackgoundRunning)
                            break;
                        long cu1 = SystemClock.elapsedRealtime();
                        if (!TextUtils.isEmpty(item.getPath())) {
                            LeoLog.d("testHidePic", "size:" + new File(item.getPath()).length() + ",path : " + item.getPath());
                        }
                        String newPath = ((PrivacyDataManager) MgrContext.
                                getManager(MgrContext.MGR_PRIVACY_DATA)).
                                onHidePic(item.getPath(), "");
                        LeoLog.d("testHidePic", "result : " + newPath);
                        LeoLog.d("testHidePic", "---------------------------------------");
                        if (newPath != null) {
                            if (FileOperationUtil.HIDE_PIC_COPY_RENAME_FAIL.equals(newPath)) {
                                isSuccess = FileOperationUtil.HIDE_PIC_COPY_RENAME_FAIL;
                            } else if (FileOperationUtil.HIDE_PIC_COPY_SUCESS.equals(newPath)) {

                                isSuccess = FileOperationUtil.HIDE_PIC_COPY_SUCESS;
                                deleteList.add(item);
                                mProcessNum++;
                            } else if (FileOperationUtil.HIDE_PIC_COPY_FAIL.equals(newPath)) {
                                isSuccess = FileOperationUtil.HIDE_PIC_COPY_FAIL;
                            } else if (FileOperationUtil.HIDE_PIC_NO_MEMERY.equals(newPath)) {
                                isSuccess = FileOperationUtil.HIDE_PIC_NO_MEMERY;
                                break;
                            } else {
                                isSuccess = FileOperationUtil.HIDE_PIC_SUCESS;
                                FileOperationUtil.saveFileMediaEntry(newPath,
                                        this);
                                File file = new File(item.getPath());
                                if (!file.exists()) {
                                    FileOperationUtil.deleteImageMediaEntry(item.getPath(), this);
                                }
                                deleteList.add(item);
                                mProcessNum++;
                            }
                        } else {
                            SDKWrapper.addEvent(this, SDKWrapper.P1,
                                    "hide_pic_operation",
                                    "pic_hid_fal");
                            isSuccess = FileOperationUtil.HIDE_PIC_PATH_EMPTY;
                        }

                        //从外置卡隐藏图片toast提示
                        boolean isSdCpy = FileOperationUtil.isIsSdcCypTip();
                        if (!isSdCpy) {
                            int hideType = FileOperationUtil.getHideTpye();
                            boolean isCopyType = (hideType == FileOperationUtil.COPY_HIDE);
                            LeoLog.d("testHidePic", "isCopyType:" + isCopyType);
                            if (isCopyType) {
                                ThreadManager.executeOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sdcHideImgToast();
                                    }
                                });
                            }
                            FileOperationUtil.setIsSdcCypTip(true);
                        }
                    }

                    if (deleteList.size() > 0) {
                        mPicturesList.removeAll(deleteList);
                        for (PhotoItem photoItem : deleteList) {
                            mAllListPath.remove(photoItem.getPath());
                        }
                    }

                    //pg3.5：对删除数据库触发删除文件的不能成功的系统，弹框处理
                    final String flag = isSuccess;
                    final PhotoItem photoItem = list.get(0);
                    int hideType = FileOperationUtil.getHideTpye();
                    final ArrayList<PhotoItem> deleteListClone = deleteList;
                    final boolean isCopyType = (hideType == FileOperationUtil.COPY_HIDE);
                    ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                        @Override
                        public void run() {
                            boolean isHideImg = (deleteListClone != null && deleteListClone.size() > 0);
                            if (isHideImg && isCopyType) {
                                String path = photoItem.getPath();
                                File file = new File(path);
                                boolean isExsit = file.exists();
                                if (isExsit) {
                                    LeoLog.e("testHidePic", "file exsit.");
                                    ThreadManager.executeOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            onPostDo(FileOperationUtil.HIDE_PIC_COPY_SUCESS);
                                        }
                                    });
                                } else {
                                    LeoLog.e("testHidePic", "file is empy");
                                }
                            }
                        }
                    }, 1000);

                } else {
                    while (iterator.hasNext()) {
                        item = iterator.next();
                        if (!mIsBackgoundRunning)
                            break;

                        String filepath = item.getPath();
                        LeoLog.d("testHidePic", "filepath : " + filepath);
                        String newPaht = ((PrivacyDataManager) MgrContext.getManager
                                (MgrContext.MGR_PRIVACY_DATA)).cancelHidePic(filepath);
                        LeoLog.d("testHidePic", "result : " + newPaht);
                        LeoLog.d("testHidePic", "---------------------------------------");
                        if (newPaht == null) {
                            SDKWrapper.addEvent(this, SDKWrapper.P1,
                                    "hide_pic_operation",
                                    "pic_ccl_fal");
                            isSuccess = FileOperationUtil.HIDE_PIC_PATH_EMPTY;
                        } else if (FileOperationUtil.HIDE_PIC_COPY_FAIL.equals(newPaht)
                                || FileOperationUtil.HIDE_PIC_PATH_EMPTY.equals(newPaht)) {
                            isSuccess = FileOperationUtil.HIDE_PIC_PATH_EMPTY;
                        } else if (FileOperationUtil.HIDE_PIC_COPY_SUCESS.equals(newPaht)) {
                            isSuccess = FileOperationUtil.HIDE_PIC_SUCESS;
                            ContentValues values = new ContentValues();
                            values.put("image_path", filepath);
                            getContentResolver().insert(Constants.IMAGE_HIDE_URI, values);
                            deleteList.add(item);
                            mProcessNum++;
                        } else if (FileOperationUtil.HIDE_PIC_NO_MEMERY.equals(newPaht)) {
                            isSuccess = FileOperationUtil.HIDE_PIC_NO_MEMERY;
                            break;
                        } else {
                            isSuccess = FileOperationUtil.HIDE_PIC_SUCESS;
                            FileOperationUtil.saveImageMediaEntry(newPaht, this);
                            FileOperationUtil.deleteFileMediaEntry(filepath, this);
                            deleteList.add(item);
                            mProcessNum++;
                        }
                    }
                    if (deleteList.size() > 0) {
                        mPicturesList.removeAll(deleteList);
                        for (PhotoItem photoItem : deleteList) {
                            mAllListPath.remove(photoItem.getPath());
                        }

                    }
                    // 取消隐藏不算新增，add in v3.6
                    pdm.haveCheckedPic();
                }
                pdm.registerMediaListener();
                pdm.notifySecurityChange();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        readyDoingDone(isSuccess, isHide);
    }

    private void readyDoingDone(String isSuccess, boolean isHide) {
        if (mHandler != null) {
            Message msg = new Message();

            Bundle bundle = new Bundle();
            bundle.putString("isSuccess", isSuccess);
            bundle.putBoolean("isHide", isHide);
            msg.setData(bundle);//bundle传值，耗时，效率低

//            msg.obj = isSuccess;
            msg.what = HIDE_FINISH;
            mHandler.sendMessage(msg);
        }
    }

    private void onPostDo(String isSuccess) {
        LeoLog.d("testnewLoad", "onPostDo isSuccess:" + isSuccess + mActicityMode);
        mClickList.clear();
        if (FileOperationUtil.HIDE_PIC_COPY_SUCESS.equals(isSuccess)) {
            String title = getString(R.string.no_image_hide_dialog_title);
            String content = getString(R.string.no_image_hide_dialog_content);
            String rightBtn = getString(R.string.no_image_hide_dialog_button);
            float width = getResources().getDimension(R.dimen.memery_dialog_button_width);
            float height = getResources().getDimension(R.dimen.memery_dialog_button_height);
            LeoLog.d(TAG, "");
            showMemeryAlarmDialog(title, content, null, rightBtn, false, true, width, height);
        } else if (FileOperationUtil.HIDE_PIC_COPY_FAIL.equals(isSuccess)
                || FileOperationUtil.HIDE_PIC_COPY_RENAME_FAIL.equals(isSuccess)) {

        } else if (FileOperationUtil.HIDE_PIC_PATH_EMPTY.equals(isSuccess)) {

        } else if (FileOperationUtil.HIDE_PIC_NO_MEMERY.equals(isSuccess)) {
            if (mActicityMode == SELECT_HIDE_MODE) {
                String title = getString(R.string.image_hide_memery_insuficient_dialog_title);
                String content = getString(R.string.image_hide_memery_insuficient_dialog_content);
                String rightBtn = getString(R.string.image_hide_memery_insuficient_dialog_button);
                float width = getResources().getDimension(
                        R.dimen.memery_dialog_button_width);
                float height = getResources().getDimension(
                        R.dimen.memery_dialog_button_height);
                showMemeryAlarmDialog(title, content, null, rightBtn, false, true,
                        width, height);
            } else if (mActicityMode == CANCEL_HIDE_MODE) {
                String title = getString(R.string.image_hide_memery_insuficient_dialog_title);
                String content = getString(R.string.image_unhide_memery_insuficient_dialog_content);
                String rightBtn = getString(R.string.image_hide_memery_insuficient_dialog_button);
                float width = getResources().getDimension(
                        R.dimen.memery_dialog_button_width);
                float height = getResources().getDimension(
                        R.dimen.memery_dialog_button_height);
                showMemeryAlarmDialog(title, content, null, rightBtn, false, true,
                        width, height);
            }
        } else if (FileOperationUtil.HIDE_PIC_SUCESS.equals(isSuccess) && mActicityMode == SELECT_HIDE_MODE) {
            if (!ImageHideMainActivity.mIsFromConfirm) {
                LeoEventBus.getDefaultBus().postSticky(new GradeEvent(GradeEvent.FROM_PIC, true));
            }
        }
        dismissProgressDialog();
        if (mPicturesList.size() > 0) {
            animateReorder();
            updateRightButton();
        } else {
            if (FileOperationUtil.HIDE_PIC_COPY_SUCESS.equals(isSuccess)) {
                if (mImageAdapter != null) {
                    mImageAdapter.notifyDataSetChanged();
                }
            } else {
                int hideType = FileOperationUtil.getHideTpye();
                if (hideType != FileOperationUtil.COPY_HIDE) {
                    finish();
                }
            }
        }
        mIsDeletSucFromDatebase = true;

    }

    private void doingBackGround(boolean isHidePic) {
        onPreDo();
        readyDoingBack(isHidePic);
    }

    private void readyDoingBack(boolean isHidePic) {
        if (mHandler != null) {
            Message msg = new Message();
            msg.obj = isHidePic;
            msg.what = START_CANCEL_OR_HIDE_PIC;
            mHandler.sendMessage(msg);
        }
    }

    private void onPreDo() {
        mIsBackgoundRunning = true;
    }

    private void loadImageList() {
        if (mPhotoAibum == null) {
            mLoadingBar.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            mHandler.sendEmptyMessage(INIT_UI_DONE);
        } else {
            mLoadingBar.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            mImageAdapter = new ImageAdapter();
            mGridView.setAdapter(mImageAdapter);
            mSelectAll.setEnabled(true);
        }
    }

    private void showMemeryAlarmDialog(String title, String content, String leftBtn,
                                       String rightBtn, boolean isLeft, boolean isRight, float width, float height) {
        if (memeryDialog == null) {
            memeryDialog = new OneButtonDialog(this);
        }
        memeryDialog.setRightBtnListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 1) {

                    mIsBackgoundRunning = false;
                    mSelectAll.setText(R.string.app_select_all);
                    mSelectAll.setCompoundDrawablesWithIntrinsicBounds(null,
                            getResources().getDrawable(R.drawable.select_all_selector), null,
                            null);

//                    mSelectAll.setText(R.string.app_select_all);
                    if (mPicturesList.size() <= 0) {
                        finish();
                    }
                }
                memeryDialog.dismiss();
            }
        });

        memeryDialog.setCanceledOnTouchOutside(false);
        memeryDialog.setTitle(title);
        memeryDialog.setText(content);
        memeryDialog.setBtnText(rightBtn);
        if (!this.isFinishing()) {
            memeryDialog.show();
        }
    }

    private void sdcHideImgToast() {
        String toast = this.getResources().getString(R.string.img_hide_cpy_toast);
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

}
