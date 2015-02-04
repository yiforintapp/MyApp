
package com.leo.appmaster.imagehide;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.db.AppMasterDBHelper;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPictureViewPager;
import com.leo.appmaster.ui.LeoPictureViewPager.OnPageChangeListener;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageScaleType;

public class PictureViewPager extends BaseActivity implements OnClickListener {
    private CommonTitleBar mTtileBar;

    private LinearLayout mBottomButtonBar;
    private Button mUnhidePicture;
    private Button mCancelPicture;

    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private int mListPos = 0;

    private Intent mIntent;
    private ArrayList<String> mPicturesList = new ArrayList<String>();
    private LeoPictureViewPager mPager;
    private VPagerAdapter mPagerAdapter;
    private LEOAlarmDialog mDialog, memeryDialog;

    private boolean mDontLock = false;
    private boolean mIsFullScreen = false;

    private Animation mAnimationRotate;

    private final int UNHIDE_DIALOG_TYPE = 0;
    private final int DELETE_DIALOG_TYPE = 1;

    private boolean mShouldLockOnRestart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_picture);
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle("");
        mTtileBar.openBackView();
        mTtileBar.setBackViewListener(this);
        initImageLoader();
        initAnimationRotate();
        mBottomButtonBar = (LinearLayout) findViewById(R.id.bottom_button_bar);
        mUnhidePicture = (Button) findViewById(R.id.unhide_picture);
        mUnhidePicture.setOnClickListener(this);
        mCancelPicture = (Button) findViewById(R.id.delete_image);
        mCancelPicture.setOnClickListener(this);

        mIntent = getIntent();
        if (null != mIntent) {
            mPicturesList = mIntent.getStringArrayListExtra("list");
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
        // TODO Auto-generated method stub
        super.onRestart();
        // if (mDontLock) {
        // mDontLock = false;
        // return;
        // }
        //
        // Intent intent = new Intent(this, LockScreenActivity.class);
        // int lockType = AppLockerPreference.getInstance(this).getLockType();
        // if (lockType == AppLockerPreference.LOCK_TYPE_PASSWD) {
        // intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
        // LockFragment.LOCK_TYPE_PASSWD);
        // } else {
        // intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
        // LockFragment.LOCK_TYPE_GESTURE);
        // }
        // intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
        // LockFragment.FROM_SELF);
        // intent.putExtra(LockScreenActivity.EXTRA_FROM_ACTIVITY,
        // PictureViewPager.class.getName());
        // startActivity(intent);
        // finish();
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        intent.putStringArrayListExtra("list", mPicturesList);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void initImageLoader() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .resetViewBeforeLoading(true).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(30)).build();

        mImageLoader = ImageLoader.getInstance();
    }

    class VPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mPicturesList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // // TODO Auto-generated method stub
            View view = getLayoutInflater().inflate(R.layout.item_pager_img,
                    null);
            PhotoView zoomImageView = (PhotoView) view
                    .findViewById(R.id.zoom_image_view);
            final ImageView loadingImage = (ImageView) view
                    .findViewById(R.id.image_loading);
            ImageLoader.getInstance().displayImage(
                    "file://" + mPicturesList.get(position), zoomImageView,
                    mOptions, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            /**
                             * 显示动画
                             */
                            loadingImage.startAnimation(mAnimationRotate);
                            loadingImage.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                FailReason failReason) {

                            loadingImage.clearAnimation();
                            loadingImage.setVisibility(View.GONE);
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
                    });

            zoomImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mIsFullScreen = !mIsFullScreen;
                    if (mIsFullScreen) {
                        mTtileBar.setVisibility(View.GONE);
                        mBottomButtonBar.setVisibility(View.GONE);
                    } else {
                        mTtileBar.setVisibility(View.VISIBLE);
                        mBottomButtonBar.setVisibility(View.VISIBLE);
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

    private static class ViewHolder {
        ImageView img;
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
                } else {
                    mTtileBar.setVisibility(View.VISIBLE);
                    mBottomButtonBar.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.layout_title_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void unhidePicture() {
        BackgoundTask task = new BackgoundTask(this);
        task.execute();
    }

    private class BackgoundTask extends AsyncTask<Boolean, Integer, Integer> {
        private Context context;

        BackgoundTask(Context context) {
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Boolean... params) {
            String filepath = mPicturesList.get(mListPos);
            long totalSize = new File(filepath).length();
            int isSuccess = 3;
            String newPaht = FileOperationUtil.unhideImageFile(
                    PictureViewPager.this, filepath, totalSize);
            if (newPaht == null) {
                isSuccess = 2;
            } else if ("-1".equals(newPaht) || "-2".equals(newPaht)) {
                isSuccess = 2;
                Log.d("com.leo.appmaster.imagehide.ImageGridActivity",
                        "Copy Hide  image fail!");
            } else if ("0".equals(newPaht)) {
                isSuccess = 3;
                AppMasterDBHelper db = new AppMasterDBHelper(context);
                ContentValues values = new ContentValues();
                String dirPath=FileOperationUtil.getDirPathFromFilepath(filepath);
                values.put("image_dir",dirPath);
                values.put("image_path", filepath);
                long flagId = db.insert("hide_image_leo", null, values);
                mPicturesList.remove(mListPos);
            } else if ("4".equals(newPaht)) {
                isSuccess = 4;
            } else {
                isSuccess = 3;
                mPicturesList.remove(mListPos);
                FileOperationUtil.saveImageMediaEntry(newPaht, context);
                FileOperationUtil.deleteFileMediaEntry(filepath, context);
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Integer success) {
            if (success == 4) {
                String title = getString(R.string.image_hide_memery_insuficient_dialog_title);
                String content = getString(R.string.image_unhide_memery_insuficient_dialog_content);
                String rightBtn = getString(R.string.image_hide_memery_insuficient_dialog_button);
                float width = getResources().getDimension(
                        R.dimen.memery_dialog_button_width);
                float height = getResources().getDimension(
                        R.dimen.memery_dialog_button_height);
                showMemeryAlarmDialog(title, content, null, rightBtn, false, true,
                        width, height);
            } else if (success == -1 || success == -2) {
                Log.d("com.leo.appmaster.imagehide.ImageGridActivity", "Copy Hide  image fail!");
            } else if (success == 2) {
                Log.d("com.leo.appmaster.imagehide.ImageGridActivity", "Hide  image fail!");
            }
            if (mPicturesList.size() == 0) {
                Intent intent = new Intent(PictureViewPager.this,
                        ImageHideMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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
    }

    private void deletePicture() {
        String filepath = mPicturesList.get(mListPos);
        if (!FileOperationUtil.deleteFile(filepath))
            return;
        mPicturesList.remove(mListPos);
        FileOperationUtil.deleteFileMediaEntry(filepath, this);

        if (mPicturesList.size() == 0) {
            Intent intent = new Intent(PictureViewPager.this,
                    ImageHideMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            if (mListPos == mPicturesList.size()) {
                mListPos = 0;
            }
            mTtileBar.setTitle(FileOperationUtil
                    .getNoExtNameFromHideFilepath(mPicturesList.get(mListPos)));
            mPagerAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(mListPos);
        }
    }

    private void sharePhoto(String photoUri, final Activity activity) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File file = new File(photoUri);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.setType("image/jpeg");
        activity.startActivity(Intent.createChooser(shareIntent,
                getString(R.string.app_share_hide_image)));
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

    @Override
    public void onActivityCreate() {
        // showLockPage();
    }

    @Override
    public void onActivityRestart() {
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
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivityForResult(intent, 1000);
	}

    @Override
    public void onActivityResault(int requestCode, int resultCode) {
        mShouldLockOnRestart = false;
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
