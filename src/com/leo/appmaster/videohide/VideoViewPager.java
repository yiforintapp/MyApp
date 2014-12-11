
package com.leo.appmaster.videohide;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
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
    private List<View> mImageView;
    private int mPosition = 0;
    private LEOAlarmDialog mDialog;
    private static final int DIALOG_CANCLE_VIDEO = 0;
    private static final int DIALOG_DELECTE_VIDEO = 1;
    private static final String VIDEO_PLUS_PACKAGE_NAME = "com.leo.xplayer";
    private VideoPagerAdapter mPagerAdapter;
    private ArrayList<String> mResultPath;
    private RelativeLayout mVideoRT;
    private boolean mIsVideoFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);
        mAllPath = new ArrayList<String>();
        mImageView = new ArrayList<View>();
        mResultPath = new ArrayList<String>();
        mVideoRT = (RelativeLayout) findViewById(R.id.videoRT);
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle("");
        mTtileBar.openBackView();
        mBottomButtonBar = (LinearLayout) findViewById(R.id.bottom_button_bar);
        mUnhideVideo = (Button) findViewById(R.id.unhide_video);
        mCancelVideo = (Button) findViewById(R.id.delete_video);
        mCancelVideo.setOnClickListener(this);
        mUnhideVideo.setOnClickListener(this);
        mVideoRT.setOnClickListener(this);
        /* get Path */
        getIntentPath();
        getVideo();
        viewPager = (LeoPictureViewPager) findViewById(R.id.picture_view_pager);
        mPagerAdapter = new VideoPagerAdapter(this);
        viewPager.setAdapter(mPagerAdapter);
        if (mPath != null && !mPath.equals("")) {
            String videoName = FileOperationUtil.getNoExtNameFromHideFilepath(mPath);
            mTtileBar.setTitle(videoName);
            int temp = 0;
            int count = 0;
            for (View image : mImageView) {
                count++;
                if (mPath.equals(image.getTag())) {
                    temp = count - 1;
                }
            }
            viewPager.setCurrentItem(temp, true);
        }
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
            case R.id.videoRT:
                mIsVideoFlag = isVideo();
                if (mIsVideoFlag) {
                    String path = mAllPath.get(mPosition);
                    ComponentName componentName = new ComponentName(VIDEO_PLUS_PACKAGE_NAME,
                            "com.leo.xplayer.VodPlayActivity");
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setDataAndType(Uri.parse("file://" + path), "video/*");
                    it.setComponent(componentName);
                    startActivity(it);
                }
            default:
                break;
        }
    }

    /**
     * isVideo+
     */
    private boolean isVideo() {
        boolean flag = false;
        ArrayList<AppDetailInfo> appInfo = AppLoadEngine.getInstance(this).getAllPkgInfo();
        for (AppDetailInfo appDetailInfo : appInfo) {
            String packageName = appDetailInfo.getPkg();
            if (packageName.equals(VIDEO_PLUS_PACKAGE_NAME)) {
                return flag = true;
            }
        }
        return flag;
    }

    /**
     * getVideo
     */
    public void getVideo() {
        for (String path : mAllPath) {
            ImageView imageView = new ImageView(this);
            LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(param);
            imageView.setTag(path);
            final ImageView imageViewT = imageView;
            final String pathT = path;
            imageViewT.setTag(path);
            imageView.setBackgroundDrawable(VideoViewPager.this.getResources()
                    .getDrawable(R.drawable.photo_bg_loding));
            AsyncLoadImage asyncLoadImage = new AsyncLoadImage();
            Drawable drawableCache = asyncLoadImage.loadImage(imageView, pathT,
                    new ImageCallback() {
                        @Override
                        public void imageLoader(Drawable drawable) {
                            if (imageViewT != null && imageViewT.getTag().equals(pathT)) {
                                imageViewT.setBackgroundDrawable(drawable);
                            }
                        }
                    });
            if (drawableCache != null) {
                imageView.setBackgroundDrawable(drawableCache);
            }
            mImageView.add(imageView);
        }
    }

    /**
     * ViewPagerAdapter PagerAdapter
     */
    private class VideoPagerAdapter extends PagerAdapter {
        // private List<View> mImageView;
        private Context context;

        public VideoPagerAdapter(Context context) {
            // this.mImageView=mImageView;
            this.context = context;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return mImageView.size();
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((LeoPictureViewPager) container).removeView(mImageView.get(position));
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ((LeoPictureViewPager) container).addView(mImageView.get(position));
            return mImageView.get(position);
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
        mImageView.remove(mPosition);
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
                    String str = (String) mImageView.get(mPosition).getTag();
                    mResultPath.add(str);
                    mImageView.remove(mPosition);
                    mPagerAdapter.notifyDataSetChanged();
                    FileOperationUtil.saveFileMediaEntry(
                            FileOperationUtil.makePath(
                                    FileOperationUtil.getDirPathFromFilepath(path), newFileName),
                            context);
                    FileOperationUtil.deleteFileMediaEntry(path, context);
                }
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean isSuccess) {
            if (isSuccess) {
                int number = mImageView.size();
                if (number == 0) {
                    Intent intent = new Intent();
                    intent.setClass(VideoViewPager.this, VideoHideMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    if (mPosition == number) {
                        mPosition = 0;
                    }
                    mPagerAdapter.notifyDataSetChanged();
                    viewPager.setCurrentItem(mPosition, true);
                }
            } else {
                // Toast.makeText(VideoViewPager.this, "取消隐藏失败",
                // Toast.LENGTH_SHORT).show();
            }
        }
    }
}
