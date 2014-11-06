
package com.leo.appmaster.imagehide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
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

import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.FileOperationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ImageGridActivity extends BaseActivity implements OnClickListener {
    private final String TAG = "ImageGridActivity";
    
    private CommonTitleBar mTtileBar;
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
    private ArrayList<String> mAllListPath =  new ArrayList<String>();
    
    public final static int CANCEL_HIDE_MODE = 0;
    public final static int SELECT_HIDE_MODE = 1;
    private int mActicityMode = SELECT_HIDE_MODE;
    
    private boolean mIsEditmode = false;
    private LEOCircleProgressDialog mProgressDialog;
    private LEOAlarmDialog mDialog;
    private boolean mIsBackgoundRunning = false;
    
    private boolean mDontLock = false;

    private int mMinClickPos = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_grid);
        mGridView = (GridView) findViewById(R.id.image_gallery_image);
        mBottomBar = (LinearLayout) findViewById(R.id.bottom_bar);
        mSelectAll = (Button) findViewById(R.id.select_all);
        mHidePicture = (Button) findViewById(R.id.hide_image);
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle("");
        mTtileBar.openBackView();
        onInit();
        initImageLoder();
        loadImageList();
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View conView,
                    int position, long arg3) {
                // TODO Auto-generated method stub
                if (mActicityMode == CANCEL_HIDE_MODE && !mIsEditmode && mAllListPath.size() > 0) {
                    mDontLock = true;
                    Intent intent = new Intent(ImageGridActivity.this, PictureViewPager.class);
                    intent.putStringArrayListExtra("list", mAllListPath);
                    intent.putExtra("pos", position);  
                    
                    startActivityForResult(intent, 0);
                } else {
                    ImageView cView = (ImageView) conView.findViewById(R.id.photo_select);
                    if (!mClickList.contains(mPicturesList.get(position))) {
                        cView.setImageResource(R.drawable.pic_choose_active);
                        mClickList.add(mPicturesList.get(position));
                        mClickPosList.add((Integer)position);
                    } else {
                        cView.setImageResource(R.drawable.pic_choose_normal);
                        mClickList.remove(mPicturesList.get(position));
                        mClickPosList.remove((Integer)position);
                    }
                    if (mClickList.size() < mPicturesList.size()) {
                        mSelectAll.setText(R.string.app_select_all);
                    } else {
                        mSelectAll.setText(R.string.app_select_none);
                    }
                    updateRightButton();
                }
            }
        });
        
        mSelectAll.setOnClickListener(this);
        mHidePicture.setOnClickListener(this);
    }

    private void updateRightButton () {
        if (mClickList.size() > 0) {
            if (mActicityMode == SELECT_HIDE_MODE) {
                mHidePicture.setText(getString(R.string.app_hide_image) + "("
                        + mClickList.size() + ")");
            } else if (mActicityMode == CANCEL_HIDE_MODE) {
                mHidePicture.setText(getString(R.string.app_cancel_hide_image) + "("
                        + mClickList.size() + ")");
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
        // TODO Auto-generated method stub
        super.onRestart();
//        if (mDontLock) {
//            mDontLock = false;
//            return;
//        }
//        
//        Intent intent = new Intent(this, LockScreenActivity.class);
//        int lockType = AppLockerPreference.getInstance(this).getLockType();
//        if (lockType == AppLockerPreference.LOCK_TYPE_PASSWD) {
//            intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
//                    LockFragment.LOCK_TYPE_PASSWD);
//        } else {
//            intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
//                    LockFragment.LOCK_TYPE_GESTURE);
//        }
//        intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
//                LockFragment.FROM_SELF);
//        intent.putExtra(LockScreenActivity.EXTRA_FROM_ACTIVITY,
//                ImageGridActivity.class.getName());
//        startActivity(intent);
//        finish();
    }

    @Override
    public void onBackPressed() {
        if (mActicityMode == CANCEL_HIDE_MODE && mIsEditmode) {
            cancelEditMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (resultCode == RESULT_OK) {
            mAllListPath  = data.getStringArrayListExtra("list");
            
            for (Iterator iterator = mPicturesList.iterator(); iterator.hasNext();) {
                PhotoItem item = (PhotoItem) iterator.next();
                
                if (!mAllListPath.contains(item.getPath())) {
                    iterator.remove();
                }
            }

            if (mImageAdapter != null) {
                mImageAdapter.notifyDataSetChanged();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    private void initImageLoder() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.photo_bg_loding)
                .showImageForEmptyUri(R.drawable.photo_bg_loding)
                .showImageOnFail(R.drawable.photo_bg_loding)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        
        mImageLoader = ImageLoader.getInstance();
    }
    
    private void onInit() {
        Intent intent = getIntent();
        mActicityMode = intent.getIntExtra("mode", SELECT_HIDE_MODE);
        mPhotoAibum = (PhotoAibum) intent.getExtras().get("data");
        if (mPhotoAibum == null) {
            mPhotoAibumPos = (int) intent.getIntExtra("pos", 0);
        }
        
        if (mPhotoAibum != null) {
            mPicturesList = mPhotoAibum.getBitList();
            mTtileBar.setTitle(mPhotoAibum.getName());
            for (PhotoItem item : mPicturesList) {
                mAllListPath.add(item.getPath());
            }
        }
        
        if (mActicityMode == SELECT_HIDE_MODE) {
            mHidePicture.setText(R.string.app_hide_image);
        } else if (mActicityMode == CANCEL_HIDE_MODE) {
            mHidePicture.setText(R.string.app_cancel_hide_image);
            Drawable topDrawable = getResources().getDrawable(R.drawable.unhide_picture_selector);
            topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
            mHidePicture.setCompoundDrawables(null,topDrawable, null, null);
            mTtileBar.setOptionTextVisibility(View.VISIBLE);
            mTtileBar.setOptionText(getString(R.string.app_hide_image_edit));
            mTtileBar.setOptionListener(this);
            
            mBottomBar.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
    
    public class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (mPhotoAibum == null) return 0;
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
                view = getLayoutInflater().inflate(R.layout.item_grid_image,
                        parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                holder.clickView = (ImageView) view .findViewById(R.id.photo_select);
                holder.pictureName = (TextView) view.findViewById(R.id.txt_item_picture);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            String path = mPicturesList.get(position).getPath();
            if (mActicityMode == CANCEL_HIDE_MODE && !mIsEditmode) {
                holder.clickView.setVisibility(View.GONE);
            } else {
                holder.clickView.setVisibility(View.VISIBLE);
                if (mClickList.contains(mPicturesList.get(position))) {
                    holder.clickView.setImageResource(R.drawable.pic_choose_active);
                } else {
                    holder.clickView.setImageResource(R.drawable.pic_choose_normal);
                }
            }
            holder.pictureName.setText(FileOperationUtil.getNoExtNameFromHideFilepath(path));
            mImageLoader.displayImage("file://" + path, holder.imageView, mOptions);
            return view;
        }

        class ViewHolder {
            ImageView imageView;
            ImageView clickView;
            TextView pictureName;
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
                } else {
                    mClickList.clear();
                    mSelectAll.setText(R.string.app_select_all);
                }
                updateRightButton();
                mImageAdapter.notifyDataSetChanged();
              break;
            case R.id.hide_image:
                showAlarmDialog();
                break;
            case R.id.tv_option_text:
                if (mActicityMode == CANCEL_HIDE_MODE) {
                    mIsEditmode = !mIsEditmode;
                    if (!mIsEditmode) {
                        cancelEditMode();
                    } else {
                        mBottomBar.setVisibility(View.VISIBLE);
                        mTtileBar.setOptionText(getString(R.string.cancel));
                    }
                    mImageAdapter.notifyDataSetChanged();
                }
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
        mTtileBar.setOptionText(getString(R.string.app_hide_image_edit));
    }
    
    /**
     * bath rename files
     * @param pathList
     */
    private void batchRenameImageFile(ArrayList<PhotoItem> itemList, boolean isHide) {
        String newFileName;
        if (itemList != null && itemList.size() > 0) {
            if (isHide) {
                for (PhotoItem item : itemList) {
                    newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                    if (newFileName.startsWith(".")) {
                        newFileName = newFileName+".leotmp";
                    } else {
                        newFileName = "."+newFileName+".leotmp";
                    }
                    if (FileOperationUtil.RenameFile(item.getPath(), newFileName)) {
                        FileOperationUtil.saveFileMediaEntry(FileOperationUtil.makePath(FileOperationUtil.getDirPathFromFilepath(item.getPath()), newFileName),this);
                        FileOperationUtil.deleteImageMediaEntry(item.getPath(),this);
                        mPicturesList.remove(item);
                        mAllListPath.remove(item.getPath());
                    }
                }
            } else {
                for (PhotoItem item : itemList) {
                    newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                    newFileName = newFileName.substring(1, newFileName.indexOf(".leotmp"));
                    if (FileOperationUtil.RenameFile(item.getPath(), newFileName)) {
                        FileOperationUtil.saveImageMediaEntry(FileOperationUtil.makePath(FileOperationUtil.getDirPathFromFilepath(item.getPath()), newFileName),this);
                        FileOperationUtil.deleteFileMediaEntry(item.getPath(),this);
                        mPicturesList.remove(item);
                        mAllListPath.remove(item.getPath());
                    }
                }
            }
        }
        
        itemList.clear();
        if (mPicturesList.size() > 0) {
            mImageAdapter.notifyDataSetChanged();
        } else {
            finish();
        }

    }


    private class BackgoundTask extends AsyncTask<Boolean ,Integer,Boolean>{
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
                    for (PhotoItem item : mClickList) {
                        if (!mIsBackgoundRunning) break;
                        newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                        if (newFileName.startsWith(".")) {
                            newFileName = newFileName+".leotmp";
                        } else {
                            newFileName = "."+newFileName+".leotmp";
                        }
                        if (FileOperationUtil.RenameFile(item.getPath(), newFileName)) {
                            FileOperationUtil.saveFileMediaEntry(FileOperationUtil.makePath(FileOperationUtil.getDirPathFromFilepath(item.getPath()), newFileName),context);
                            FileOperationUtil.deleteImageMediaEntry(item.getPath(),context);
                            mPicturesList.remove(item);
                            mAllListPath.remove(item.getPath());
                        } else {
                            isSuccess = false;
                        }

                    }
                } else {
                    for (PhotoItem item : mClickList) {
                        if (!mIsBackgoundRunning) break;
                        newFileName = FileOperationUtil.getNameFromFilepath(item.getPath());
                        newFileName = newFileName.substring(1, newFileName.indexOf(".leotmp"));
                        if (FileOperationUtil.RenameFile(item.getPath(), newFileName)) {
                            FileOperationUtil.saveImageMediaEntry(FileOperationUtil.makePath(FileOperationUtil.getDirPathFromFilepath(item.getPath()), newFileName),context);
                            FileOperationUtil.deleteFileMediaEntry(item.getPath(),context);
                            mPicturesList.remove(item);
                            mAllListPath.remove(item.getPath());
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
                Toast.makeText(ImageGridActivity.this, getString(R.string.app_hide_image_fail), Toast.LENGTH_SHORT).show();
            }
            dismissProgressDialog();
            if (mPicturesList.size() > 0) {
//                mImageAdapter.notifyDataSetChanged();
//                if (minPos < mGridView.getLastVisiblePosition()) {
//                    final ViewTreeObserver observer = mGridView.getViewTreeObserver();
//                    observer.addOnPreDrawListener(new OnPreDrawListener() {
//                        
//                        @Override
//                        public boolean onPreDraw() {
//                            observer.removeOnPreDrawListener(this);
////                            animateReorder(minPos, mGridView.getLastVisiblePosition() + 1 );
//                            int count = mGridView.getChildCount();
//                            animateReorder();
//                            return true;
//                        }
//                    } );
//                }
                animateReorder();
                updateRightButton();
            } else {
                finish();
            }
        }
    }
    
    private void animateReorder() {
        int length = mClickPosList.size();
        List<Animator> resultList = new LinkedList<Animator>();
        int fistVisblePos = mGridView.getFirstVisiblePosition();
        int lastVisblePos = mGridView.getLastVisiblePosition();
        int pos;
        final List<Integer> viewList = new LinkedList<Integer>();
        for (int i = 0; i < length;  i++) {
            pos = mClickPosList.get(i);
           if (pos >= fistVisblePos && pos <= lastVisblePos) {
               View view = mGridView.getChildAt(pos - fistVisblePos);
               viewList.add((Integer)(pos - fistVisblePos));
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
                    mGridView.getChildAt(view).setAlpha(1);
                    mGridView.getChildAt(view).setScaleX(1);
                    mGridView.getChildAt(view).setScaleY(1);
                }
                mClickPosList.clear();
            }
        });
        resultSet.start();
    }
    
    private Animator createZoomAnimations(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f);
        ObjectAnimator zoomIn = ObjectAnimator.ofFloat(view,  "alpha", 1f, 0f);
        AnimatorSet animZoom = new AnimatorSet();
        animZoom.playTogether( scaleX, scaleY,zoomIn);
        return animZoom;
    }
    
    
    private void animateReorder(final int oldPosition, final int newPosition) {
        List<Animator> resultList = new LinkedList<Animator>();
        int numColumns = mGridView.getNumColumns();
        int fistVisblePos = mGridView.getFirstVisiblePosition();
        
        if (oldPosition >= fistVisblePos) {
            for (int pos = oldPosition; pos < newPosition; pos++) {
                View view = mGridView.getChildAt(pos - fistVisblePos);
                if ((pos + 1) % numColumns == 0) {
                    resultList.add(createTranslationAnimations(view,
                            - (view.getWidth()/* + mHorizontalSpacing*/) * (numColumns  - 1), 0,
                            view.getHeight()/* + mVerticalSpacing*/, 0));
                } else {
                    resultList.add(createTranslationAnimations(view,
                            view.getWidth()/* + mHorizontalSpacing*/, 0, 0, 0));
                }
            }
        } else {
            
        }


        AnimatorSet resultSet = new AnimatorSet();
        resultSet.playTogether(resultList);
        resultSet.setDuration(300);
        resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
        resultSet.start();
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
    
    private void showProgressDialog(String title,boolean indeterminate, boolean cancelable) {
        if(mProgressDialog == null) {
            mProgressDialog = new LEOCircleProgressDialog(this);
            mProgressDialog.setOnCancelListener(new OnCancelListener() {            
                @Override
                public void onCancel(DialogInterface dialog) {
                    mIsBackgoundRunning = false;
                }
            });
        }
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setButtonVisiable(cancelable);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(indeterminate);
        mProgressDialog.setTitle(title);
        mProgressDialog.show();
    }
    
    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
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
                        if (mActicityMode == SELECT_HIDE_MODE) {
                            showProgressDialog(getString(R.string.app_hide_image), true,true);
                            BackgoundTask task = new BackgoundTask(ImageGridActivity.this);
                            task.execute(true);
                        } else if (mActicityMode == CANCEL_HIDE_MODE) {
                            showProgressDialog(getString(R.string.app_cancel_hide_image), true,true);
                            BackgoundTask task = new BackgoundTask(ImageGridActivity.this);
                            task.execute(false);
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
    
    private void loadImageList() {
        if (mPhotoAibum == null) { 
            LoaderImageListTask task = new LoaderImageListTask(this);
            task.execute();
        } else {
            mImageAdapter = new ImageAdapter();
            mGridView.setAdapter(mImageAdapter);
        }
    }
    
    private class LoaderImageListTask extends AsyncTask<Void,Integer,Integer>{  
        private Context context;  
        LoaderImageListTask(Context context) {  
            this.context = context;  
        }  

        @Override  
        protected void onPreExecute() {  

        }  

        @Override  
        protected Integer doInBackground(Void... params) {  
            mPhotoAibum = FileOperationUtil.getPhotoAlbum(context).get(mPhotoAibumPos);
            return 0;  
        }  
  
        @Override  
        protected void onPostExecute(Integer integer) {
            if (mPhotoAibum != null) {
                mPicturesList = mPhotoAibum.getBitList();
                mTtileBar.setTitle(mPhotoAibum.getName());
                for (PhotoItem item : mPicturesList) {
                    mAllListPath.add(item.getPath());
                }
            }
            
            mImageAdapter = new ImageAdapter();
            mGridView.setAdapter(mImageAdapter);
        }  
 
    }  
}
