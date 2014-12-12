
package com.leo.appmaster.videohide;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPictureViewPager;
import com.leo.appmaster.ui.LeoPictureViewPager.OnPageChangeListener;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.videohide.AsyncLoadImage.ImageCallback;

public class VideoViewPager extends BaseActivity implements OnClickListener {
    private CommonTitleBar mTtileBar;
    private Button mUnhideVideo;
    private Button mCancelVideo;
    private LinearLayout mBottomButtonBar;
    private String mPath;
    private ArrayList<String> mAllPath;
    private LeoPictureViewPager viewPager;
    private List<ImageView> mImageView;
    private int mPosition = 0;
    private LEOAlarmDialog mDialog;
    private static final int DIALOG_CANCLE_VIDEO = 0;
    private static final int DIALOG_DELECTE_VIDEO = 1;
    private static final String VIDEO_PLUS_PACKAGE_NAME = "com.leo.xplayer";
    private static final String VIDEO_PLAYER_ACTIVITY = "com.leo.xplayer.VodPlayActivity";
    private VideoPagerAdapter mPagerAdapter;
    private ArrayList<String> mResultPath;
    private RelativeLayout mVideoRT;
    private boolean mShouldLockOnRestart = true;
    public static final int REQUEST_CODE_LOCK = 1000;
    public static final int REQUEST_CODE_OPTION = 1001;
    public static final int JUMP_GP = 0;
    public static final int JUMP_URL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);
        mAllPath = new ArrayList<String>();
        mImageView = new ArrayList<ImageView>();
        mResultPath = new ArrayList<String>();
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle("");
        mTtileBar.openBackView();
        mBottomButtonBar = (LinearLayout) findViewById(R.id.bottom_button_bar);
        mUnhideVideo = (Button) findViewById(R.id.unhide_video);
        mCancelVideo = (Button) findViewById(R.id.delete_video);
        mCancelVideo.setOnClickListener(this);
        mUnhideVideo.setOnClickListener(this);
        /* get Path */
        getIntentPath();
//        getVideo();
        viewPager = (LeoPictureViewPager) findViewById(R.id.picture_view_pager);
        mPagerAdapter = new VideoPagerAdapter(this);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                mTtileBar.setTitle(FileOperationUtil.getNoExtNameFromHideFilepath(mAllPath
                        .get(mPosition)));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (mPath != null && !mPath.equals("")) {
            String videoName = FileOperationUtil.getNoExtNameFromHideFilepath(mPath);
            mTtileBar.setTitle(videoName);
            viewPager.setCurrentItem(mPosition, true);
        }
        getResultValue();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getResultValue() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("path", mResultPath);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }

    /**
     * getIntent
     */
    private void getIntentPath() {
        Intent intent = getIntent();
        mPath = intent.getStringExtra("path");
        mAllPath = intent.getStringArrayListExtra("mAllPath");
        mPosition = intent.getIntExtra("position", 0);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.unhide_video:
                String thumbnailPath = (String) mImageView.get(mPosition).getTag();
                String cancleHideVideoText = getString(R.string.app_unhide_dialog_content_video);
                showAlarmDialog(cancleHideVideoText, DIALOG_CANCLE_VIDEO);
                break;
            case R.id.delete_video:
                String deleteHideVideoText = getString(R.string.app_delete_dialog_content_video);
                showAlarmDialog(deleteHideVideoText, DIALOG_DELECTE_VIDEO);
                break;
            default:
                break;
        }
    }

    /**
     * There are this App
     */
    private boolean isVideo(String packageName) {
        boolean flag = false;
        ArrayList<AppDetailInfo> appInfo = AppLoadEngine.getInstance(this).getAllPkgInfo();
        for (AppDetailInfo appDetailInfo : appInfo) {
            String pn = appDetailInfo.getPkg();
            if (pn.equals(packageName)) {
                return flag = true;
            }
        }
        return flag;
    }

    /**
     * getVideo
     */
/*    public void getVideo() {
        for (String path : mAllPath) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_pager_video, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.zoom_image_view);
            imageView.setTag(path);
            final ImageView imageViewT = imageView;
            final String pathT = path;
            imageViewT.setTag(path);
            imageView.setImageDrawable(VideoViewPager.this.getResources()
                    .getDrawable(R.drawable.video_loading));
            AsyncLoadImage asyncLoadImage = new AsyncLoadImage();
            Drawable drawableCache = asyncLoadImage.loadImage(imageView, pathT,
                    new ImageCallback() {
                        @Override
                        public void imageLoader(Drawable drawable) {
                            if (imageViewT != null && imageViewT.getTag().equals(pathT)
                                    && drawable != null) {
                                imageViewT.setImageDrawable(drawable);
                            }
                        }
                    });
            if (drawableCache != null) {
                imageView.setImageDrawable(drawableCache);
            }
            mImageView.add(view);
        }
    }*/

    /**
     * ViewPagerAdapter PagerAdapter
     */
    private class VideoPagerAdapter extends PagerAdapter {
        private Context context;

        public VideoPagerAdapter(Context context) {
            this.context = context;
        }
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return mAllPath.size();
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            View view = (View) object;
            ((ViewGroup) container).removeView(view);
        }

        @Override
        public Object instantiateItem(View container, int position) {

            String path = mAllPath.get(position);
            View view = LayoutInflater.from(VideoViewPager.this).inflate(R.layout.item_pager_video,
                    null);
            ImageView imageView = (ImageView) view.findViewById(R.id.zoom_image_view);
            imageView.setTag(path);
            final ImageView imageViewT = imageView;
            final String pathT = path;
            imageViewT.setTag(path);
            imageView.setImageDrawable(VideoViewPager.this.getResources()
                    .getDrawable(R.drawable.video_loading));
            AsyncLoadImage asyncLoadImage = new AsyncLoadImage();
            Drawable drawableCache = asyncLoadImage.loadImage(imageView, pathT,
                    new ImageCallback() {
                        @Override
                        public void imageLoader(Drawable drawable) {
                            if (imageViewT != null && imageViewT.getTag().equals(pathT)
                                    && drawable != null) {
                                imageViewT.setImageDrawable(drawable);
                            }
                        }
                    });
            if (drawableCache != null) {
                imageView.setImageDrawable(drawableCache);
            }
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    boolean isVideoFlag = isVideo(VIDEO_PLUS_PACKAGE_NAME);
                    if (isVideoFlag) {
                        String path = mAllPath.get(mPosition);
                        ComponentName componentName = new ComponentName(VIDEO_PLUS_PACKAGE_NAME,
                                VIDEO_PLAYER_ACTIVITY);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + path), "video/*");
                        intent.setComponent(componentName);
                        startActivity(intent);
                    } else {
                        showAlarmDialogPlayer();
                    }
                }

            });

            ((LeoPictureViewPager) container).addView(view);
            return view;
        }
    };

    private void showAlarmDialog(final String string, final int flag) {
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(this);
        }
        mDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    if (flag == DIALOG_CANCLE_VIDEO) {
                        BackgoundTask backgoundTask = new BackgoundTask(VideoViewPager.this);
                        String path = (String) mImageView.get(mPosition).getTag();
                        backgoundTask.execute(true);
                    } else if (flag == DIALOG_DELECTE_VIDEO) {
                        deleteVideo();
                    }
                }
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setTitle(R.string.app_cancel_hide_image);
        mDialog.setContent(string);
        mDialog.show();
    }

    /**
     * delete Video
     */
    private void deleteVideo() {
        String filePath = mAllPath.get(mPosition);
        if (!FileOperationUtil.DeleteFile(filePath)) {
            return;
        }
        mResultPath.add(filePath);
        // mImageView.remove(mPosition);
        FileOperationUtil.deleteFileMediaEntry(filePath, this);
        int number = mImageView.size();
        if (number == 0) {
            Intent intent = new Intent();
            intent.setClass(VideoViewPager.this, VideoHideMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            if (number == mPosition) {
                mPosition = 0;
            }
            mPagerAdapter.notifyDataSetChanged();
            viewPager.setCurrentItem(mPosition, true);
        }
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

        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            String newFileName = null;
            boolean isSuccess = true;
            Boolean flag = params[0];
            if (flag) {
                String path = mAllPath.get(mPosition);
                newFileName = FileOperationUtil.getNameFromFilepath(path);
                newFileName = newFileName.substring(1, newFileName.indexOf(".leotmv"));
                if (!FileOperationUtil.RenameFile(path, newFileName)) {
                    return isSuccess = false;
                } else {
                    mResultPath.add(path);
                    FileOperationUtil.saveFileMediaEntry(
                            FileOperationUtil.makePath(
                                    FileOperationUtil.getDirPathFromFilepath(path), newFileName),
                            context);
                    FileOperationUtil.deleteFileMediaEntry(path, context);
                    mAllPath.remove(mPosition);
                }
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean isSuccess) {
            if (isSuccess) {
                int number = mAllPath.size();
                if (mPosition == 0) {
                    mTtileBar.setTitle(FileOperationUtil.getNoExtNameFromHideFilepath(mAllPath
                            .get(mPosition)));
                }
                if (number == 0) {
                    Intent intent = new Intent();
                    intent.setClass(VideoViewPager.this, VideoHideMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    if (mPosition == number) {
                        mPosition = 0;
                        mTtileBar.setTitle(FileOperationUtil.getNoExtNameFromHideFilepath(mAllPath
                                .get(mPosition)));
                    }
                    mPagerAdapter.notifyDataSetChanged();
                    mPagerAdapter = null;
                    mPagerAdapter = new VideoPagerAdapter(VideoViewPager.this);
                    viewPager.setAdapter(mPagerAdapter);
                    viewPager.setCurrentItem(mPosition, true);
                }
            } else {
            }
        }
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

    @Override
    public void onActivityResault(int requestCode, int resultCode) {
        mShouldLockOnRestart = false;
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivityForResult(intent, 1000);
    }

    private void showAlarmDialogPlayer() {
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(this);
        }
        mDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    boolean isGpFlag = isVideo(Constants.GP_PACKAGE);
                    if (isGpFlag) {
                        if (true) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = Uri
                                    .parse("market://details?id=com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01");
                            intent.setData(uri);
                            ComponentName cn = new ComponentName(
                                    "com.android.vending",
                                    "com.google.android.finsky.activities.MainActivity");
                            intent.setComponent(cn);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = Uri
                                    .parse("market://details?id=com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01");
                            intent.setData(uri);
                            ComponentName cn = new ComponentName(
                                    "com.android.vending",
                                    "com.google.android.finsky.activities.MainActivity");
                            intent.setComponent(cn);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } else {
                        if (true) {
                            Uri uri = Uri
                                    .parse("https://play.google.com/store/apps/details?id=com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01market://details?id= com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        } else {
                            Uri uri = Uri
                                    .parse("https://play.google.com/store/apps/details?id=com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01market://details?id= com.leomaster.videomaster&referrer=utm_source%3Dad_amtuiguang_01");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    }
                }
            }
        });
        mDialog.setTitle(getString(R.string.hide_video_dialog_title));
        mDialog.setContent(getString(R.string.hide_video_dialog_content));
        mDialog.setLeftBtnStr(getString(R.string.cancel));
        mDialog.setRightBtnStr(getString(R.string.button_install));
        mDialog.show();
    }
}
