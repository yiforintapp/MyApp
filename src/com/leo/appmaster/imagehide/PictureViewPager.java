
package com.leo.appmaster.imagehide;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.intruderprotection.IntruderCatchedActivity;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPictureViewPager;
import com.leo.appmaster.ui.LeoPictureViewPager.OnPageChangeListener;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageDownloader;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageScaleType;

public class PictureViewPager extends BaseActivity implements OnClickListener {
    private CommonTitleBar mTtileBar;

    private View mContainer;
    private LinearLayout mBottomButtonBar;
    private Button mUnhidePicture;
    private Button mCancelPicture;

    private DisplayImageOptions mOptions;
    private int mListPos = 0;
    private Boolean mIsFromIntruderMore = false;
    private Intent mIntent;
    private ArrayList<String> mPicturesList = new ArrayList<String>();
    private LeoPictureViewPager mPager;
    private VPagerAdapter mPagerAdapter;
    private LEOAlarmDialog mDialog, memeryDialog;

    private boolean mIsFullScreen = false;

    private Animation mAnimationRotate;

    private final int UNHIDE_DIALOG_TYPE = 0;
    private final int DELETE_DIALOG_TYPE = 1;
    public final static int CANCEL_HIDE = 26;
    public final static int CANCEL_HIDE_FINISH = 27;
    private int mProcessNum = 0;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(final android.os.Message msg) {
            switch (msg.what) {
                case CANCEL_HIDE:
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            startDoingBack();
                        }
                    });
                    break;
                case CANCEL_HIDE_FINISH:
                    String isSuccess = (String) msg.obj;
                    checkLostPic();
                    onPostDo(isSuccess);
                    break;
            }
        }
    };

    private void checkLostPic() {
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
                } else {
                    LeoSettings.setInteger(Constants.HIDE_PICS_NUM, num);
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_picture);
        mContainer = findViewById(R.id.container);
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle("");

        mTtileBar.setSelfBackPressListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        initImageLoader();
        initAnimationRotate();
        mBottomButtonBar = (LinearLayout) findViewById(R.id.bottom_button_bar);
        mUnhidePicture = (Button) findViewById(R.id.unhide_picture);
        mUnhidePicture.setOnClickListener(this);
        mCancelPicture = (Button) findViewById(R.id.delete_image);
        mCancelPicture.setOnClickListener(this);

        mIntent = getIntent();
        if (null != mIntent) {
            mIsFromIntruderMore = mIntent.getBooleanExtra("fromIntruderMore", false);
            mPicturesList = mIntent.getStringArrayListExtra("list");
            LeoLog.d("teststartResult", "come to ViewPager , list size : " + mPicturesList.size());
            int maxSize = mPicturesList.size() - 1;
            mListPos = mIntent.getIntExtra("pos", 0);

            // AM-533, add protect
            if (mListPos > maxSize) {
                mListPos = maxSize;
            }
        }

        mPager = (LeoPictureViewPager) findViewById(R.id.picture_view_pager);
        // mPager.setOnClickListener(this);
        mPagerAdapter = new VPagerAdapter();
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mListPos);
        mTtileBar.setTitle(FileOperationUtil
                .getNoExtNameFromHideFilepath(mPicturesList.get(mListPos)));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mListPos = position;
                mTtileBar.setTitle(FileOperationUtil
                        .getNoExtNameFromHideFilepath(mPicturesList
                                .get(mListPos)));

            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageLoader.getInstance().clearMemoryCache();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("list", mPicturesList);
        LeoLog.d("teststartResult", "ViewPager BackPressed, list size : " + mPicturesList.size());
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void initImageLoader() {
        mOptions = new DisplayImageOptions.Builder()
//                .showImageForEmptyUri(R.drawable.photo_bg_loding)
//                .showImageOnFail(R.drawable.photo_bg_loding)
                .showImageOnLoading(new ColorDrawable(0xd7d7dd))
                .showImageForEmptyUri(new ColorDrawable(0xd7d7dd))
                .showImageOnFail(new ColorDrawable(0xd7d7dd))
                .resetViewBeforeLoading(true).cacheOnDisk(false).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(500)).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    class VPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPicturesList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.item_pager_img,
                    null);
            PhotoView zoomImageView = (PhotoView) view.findViewById(R.id.zoom_image_view);
            final ImageView loadingImage = (ImageView) view.findViewById(R.id.image_loading);

            String path = mPicturesList.get(position);
            String uri = null;
            if (path != null && path.endsWith(Constants.CRYPTO_SUFFIX)) {
                uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
            } else {
                uri = ImageDownloader.Scheme.FILE.wrap(path);
            }
            ImageLoader.getInstance().displayImage(uri, zoomImageView, mOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    /**
                     * 显示动画
                     */
                    loadingImage.startAnimation(mAnimationRotate);
                    loadingImage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingComplete(String imageUri,
                                              View view, Bitmap loadedImage) {

                    loadingImage.clearAnimation();
                    loadingImage.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri,
                                               View view) {
                    loadingImage.clearAnimation();
                    loadingImage.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {
                    // TODO Auto-generated method stub
                    loadingImage.clearAnimation();
                    loadingImage.setVisibility(View.GONE);
                }
            });

            zoomImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mIsFullScreen = !mIsFullScreen;
                    if (mIsFullScreen) {
                        mTtileBar.setVisibility(View.GONE);
                        mBottomButtonBar.setVisibility(View.GONE);
                        mContainer.setBackgroundColor(getResources().getColor(R.color.gallery_bg));
                    } else {
                        mTtileBar.setVisibility(View.VISIBLE);
                        mBottomButtonBar.setVisibility(View.VISIBLE);
                        mContainer.setBackgroundColor(Color.WHITE);
                    }

                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private void initAnimationRotate() {
        mAnimationRotate = AnimationUtils.loadAnimation(this,
                R.anim.clockwise_rotate_animation);
        LinearInterpolator lir = new LinearInterpolator();
        mAnimationRotate.setInterpolator(lir);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.unhide_picture:
                showAlarmDialog(getString(R.string.app_cancel_hide_image),
                        getString(R.string.app_unhide_dialog_content),
                        UNHIDE_DIALOG_TYPE);
                break;
            case R.id.delete_image:
                showAlarmDialog(getString(R.string.delete),
                        getString(R.string.app_delete_dialog_content),
                        DELETE_DIALOG_TYPE);
                break;
            case R.id.zoom_image_view:
                mIsFullScreen = !mIsFullScreen;
                if (mIsFullScreen) {
                    mTtileBar.setVisibility(View.GONE);
                    mBottomButtonBar.setVisibility(View.GONE);
                    mContainer.setBackgroundColor(getResources().getColor(R.color.gallery_bg));
                } else {
                    mTtileBar.setVisibility(View.VISIBLE);
                    mBottomButtonBar.setVisibility(View.VISIBLE);
                    mContainer.setBackgroundColor(Color.WHITE);
                }
                break;
//            case R.id.layout_title_back:
//                onBackPressed();
//                break;
            default:
                break;
        }
    }

    private void unhidePicture() {
//        BackgoundTask task = new BackgoundTask(this);
//        task.execute();
        doingBackGround();
    }

    private void startDoingBack() {
        mProcessNum = 0;
        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        pdm.unregisterMediaListener();
        String filepath = mPicturesList.get(mListPos);
        long totalSize = new File(filepath).length();
        String isSuccess = FileOperationUtil.HIDE_PIC_SUCESS;
        String newPaht = ((PrivacyDataManager) MgrContext.getManager
                (MgrContext.MGR_PRIVACY_DATA)).cancelHidePic(filepath);
        if (newPaht == null) {
            SDKWrapper.addEvent(this, SDKWrapper.P1,
                    "hide_pic_operation",
                    "pic_ccl_fal");
            isSuccess = FileOperationUtil.HIDE_PIC_PATH_EMPTY;
        } else if (FileOperationUtil.HIDE_PIC_COPY_FAIL.equals(newPaht)
                || FileOperationUtil.HIDE_PIC_COPY_RENAME_FAIL.equals(newPaht)) {
            isSuccess = FileOperationUtil.HIDE_PIC_PATH_EMPTY;
        } else if (FileOperationUtil.HIDE_PIC_COPY_SUCESS.equals(newPaht)) {
            isSuccess = FileOperationUtil.HIDE_PIC_SUCESS;
            ContentValues values = new ContentValues();
            String dirPath = FileOperationUtil.getDirPathFromFilepath(filepath);
            values.put("image_dir", dirPath);
            values.put("image_path", filepath);
            try {
                getContentResolver().insert(Constants.IMAGE_HIDE_URI, values);
            } catch (Exception e) {
            }
            mProcessNum++;
            mPicturesList.remove(mListPos);
        } else if (FileOperationUtil.HIDE_PIC_NO_MEMERY.equals(newPaht)) {
            isSuccess = FileOperationUtil.HIDE_PIC_NO_MEMERY;
        } else {
            isSuccess = FileOperationUtil.HIDE_PIC_SUCESS;
            mPicturesList.remove(mListPos);
            FileOperationUtil.saveImageMediaEntry(newPaht, this);
            FileOperationUtil.deleteFileMediaEntry(filepath, this);
            mProcessNum++;
        }
        pdm.registerMediaListener();
        pdm.notifySecurityChange();
        readyDoingDone(isSuccess);

        // 取消隐藏不算新增 add in v3.6
        pdm.haveCheckedPic();
    }

    private void onPostDo(String isSuccess) {
        if (FileOperationUtil.HIDE_PIC_NO_MEMERY.equals(isSuccess)) {
            String title = getString(R.string.image_hide_memery_insuficient_dialog_title);
            String content = getString(R.string.image_unhide_memery_insuficient_dialog_content);
            String rightBtn = getString(R.string.image_hide_memery_insuficient_dialog_button);
            float width = getResources().getDimension(
                    R.dimen.memery_dialog_button_width);
            float height = getResources().getDimension(
                    R.dimen.memery_dialog_button_height);
            showMemeryAlarmDialog(title, content, null, rightBtn, false, true,
                    width, height);
        } else if (FileOperationUtil.HIDE_PIC_COPY_FAIL.equals(isSuccess)
                || FileOperationUtil.HIDE_PIC_COPY_RENAME_FAIL.equals(isSuccess)) {
        } else if (FileOperationUtil.HIDE_PIC_PATH_EMPTY.equals(isSuccess)) {
        }
        if (mPicturesList.size() == 0) {
            if (mIsFromIntruderMore) {
                Intent intent = new Intent(PictureViewPager.this,
                        IntruderCatchedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("isClear", true);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(PictureViewPager.this,
                        ImageHideMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } else {
            if (mListPos == mPicturesList.size()) {
                mListPos = 0;
            }
            mTtileBar.setTitle(FileOperationUtil
                    .getNoExtNameFromHideFilepath(mPicturesList
                            .get(mListPos)));
            mPagerAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(mListPos);
        }
    }

    private void readyDoingDone(String isSuccess) {
        if (mHandler != null) {
            Message msg = new Message();
            msg.obj = isSuccess;
            msg.what = CANCEL_HIDE_FINISH;
            mHandler.sendMessage(msg);
        }
    }

    private void doingBackGround() {
        onPreDo();
        readyDoingBack();
    }

    private void readyDoingBack() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(CANCEL_HIDE);
        }
    }

    private void onPreDo() {

    }

    private void deletePicture() {
        mProcessNum = 0;
        String filepath = mPicturesList.get(mListPos);

        boolean isSuccees = ((PrivacyDataManager) MgrContext.
                getManager(MgrContext.MGR_PRIVACY_DATA)).deleteHidePic(filepath);

        if (!isSuccees)
            return;
        mPicturesList.remove(mListPos);
        FileOperationUtil.deleteFileMediaEntry(filepath, this);
        mProcessNum++;

        if (mPicturesList.size() == 0) {
            if (mIsFromIntruderMore) {
                Intent intent = new Intent(PictureViewPager.this,
                        IntruderCatchedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("isClear", true);
                startActivity(intent);
                finish();
            } else {

                Intent intent = new Intent(PictureViewPager.this,
                        ImageHideMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } else {
            if (mListPos == mPicturesList.size()) {
                mListPos = 0;
            }
            mTtileBar.setTitle(FileOperationUtil
                    .getNoExtNameFromHideFilepath(mPicturesList.get(mListPos)));
            mPagerAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(mListPos);
        }

        checkLostPic();
    }

    private void showAlarmDialog(String title, String content,
                                 final int dialogType) {
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(this);
        }
        mDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    if (dialogType == UNHIDE_DIALOG_TYPE) {
                        unhidePicture();
                    } else if (dialogType == DELETE_DIALOG_TYPE) {
                        deletePicture();
                    }
                }

            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setTitle(title);
        mDialog.setContent(content);
        mDialog.show();
    }

    private void showMemeryAlarmDialog(String title, String content, String leftBtn,
                                       String rightBtn, boolean isLeft, boolean isRight, float width, float height) {
        if (memeryDialog == null) {
            memeryDialog = new LEOAlarmDialog(this);
        }
        memeryDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {

                }

            }
        });
        memeryDialog.setCanceledOnTouchOutside(false);
        memeryDialog.setTitle(title);
        memeryDialog.setContent(content);
        memeryDialog.setLeftBtnVisibility(false);
        memeryDialog.setRightBtnStr(rightBtn);
        memeryDialog.setRightBtnParam(width, height);
        memeryDialog.show();
    }
}
