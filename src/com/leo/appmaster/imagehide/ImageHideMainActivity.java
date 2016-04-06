
package com.leo.appmaster.imagehide;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.GradeEvent;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.mgr.impl.PrivacyDataManagerImpl;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.FastBlur;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.QuickHelperUtils;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageDownloader;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageSize;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;

import java.util.ArrayList;
import java.util.List;

public class ImageHideMainActivity extends BaseActivity implements OnItemClickListener {

    private static final String TAG = "ImageHideMainActivity";
    private List<PhotoAibum> mAlbumList = null;
    private List<PhotoItem> mNewAddPic = null;
    private GridView mGridView;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private CommonToolbar mTtileBar;
    private RelativeLayout mNoHidePictureHint;
    private RippleView mRvAdd;
    private boolean mHasShowNew = false;
    private ProgressBar loadingBar;
    private LeoPreference mPt;
    private final int ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT = 3;
    private LEOAlarmDialog mDialogAskCreateShotcut;
    private final int NEW_PIC_MAX_SHOW_AMOUNT = 5;

    private HideAlbumAdapt mHideAlbumAdapt = new HideAlbumAdapt(this);

    private NewPicAdapter mNewPicAdapter = new NewPicAdapter(this);

    private View mIncludeLayoutNewPic;
    private GridView mGvNewPic;
    private TextView mTvNewAmountTips;
    private RippleView mRvHideNew;
    private TextView mTvIgnoreNew;
    private RelativeLayout mRlWholeShowContent;

    public static final int REQUEST_CODE_OPTION = 1001;

    private Toast mToast;

    private ImageSize mImageSize;

    public static boolean mIsFromConfirm;

    public void onBackPressed() {
        LeoLog.d(TAG, "mPt.getBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC, false) = " + mPt.getBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC, false));
        LeoLog.d(TAG, "mPt.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, 0) = " + mPt.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, 0));
        if (!mPt.getBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC, false) && mPt.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, 0) >= ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT) {
            mPt.putBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC, true);
            if (mDialogAskCreateShotcut == null) {
                mDialogAskCreateShotcut = new LEOAlarmDialog(this);
            }
            mDialogAskCreateShotcut.setTitleVisiable(false);
            mDialogAskCreateShotcut.setDialogIconVisibility(true);
            mDialogAskCreateShotcut.setDialogIcon(R.drawable.qh_image_icon);
            mDialogAskCreateShotcut.setContent(getString(R.string.ask_create_shortcut_content_imagehide));
            mDialogAskCreateShotcut.setLeftBtnStr(getString(R.string.cancel));
            mDialogAskCreateShotcut.setRightBtnStr(getString(R.string.ask_create_shortcut_button_right));
            mDialogAskCreateShotcut.setRightBtnListener(new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SDKWrapper.addEvent(ImageHideMainActivity.this, SDKWrapper.P1, "assistant", "shortcut_hidepic");
                    Intent intent = new Intent();
                    intent = new Intent(AppMasterApplication.getInstance(), ImageHideMainActivity.class);
                    intent.putExtra("from_quickhelper", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    QuickHelperUtils.createQuickHelper(getString(R.string.quick_helper_pic_hide), R.drawable.qh_image_icon, intent, ImageHideMainActivity.this);
                    Toast.makeText(ImageHideMainActivity.this, getString(R.string.quick_help_add_toast), Toast.LENGTH_SHORT).show();
                    mDialogAskCreateShotcut.dismiss();
                }
            });
            mDialogAskCreateShotcut.setLeftBtnListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SDKWrapper.addEvent(ImageHideMainActivity.this, SDKWrapper.P1, "assistant", "shortcut_hidepic_no");
                    mDialogAskCreateShotcut.dismiss();
                }
            });
            mDialogAskCreateShotcut.show();
        } else {
            if (mAlbumList != null && mAlbumList.size() == 0 && !mIsFromConfirm) {
                LeoEventBus.getDefaultBus().postSticky(new GradeEvent(GradeEvent.FROM_PIC, false));
            }
            super.onBackPressed();
        }
    }

    ;

    private void loadDone() {
        if (mAlbumList != null) {
            if (mAlbumList.size() > 0) {
                mNoHidePictureHint.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mRlWholeShowContent.setVisibility(View.VISIBLE);
            } else {
                mNoHidePictureHint.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
//                mRlWholeShowContent.setVisibility(View.GONE);
            }
            if (mHideAlbumAdapt != null) {
                mHideAlbumAdapt.setDataList(mAlbumList);
                mHideAlbumAdapt.notifyDataSetChanged();
            }

        }
    }

    private void updateTips() {
        String string1 = getString(R.string.find_new_pic_tips);
        String s2 = String.format(string1, mNewAddPic.size());
        mTvNewAmountTips.setText(Html.fromHtml(s2));
    }

    private void asyncLoad() {

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mAlbumList = ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).
                        getHidePicAlbum(PrivacyDataManagerImpl.CHECK_APART);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadDone();
                    }
                });
            }
        });
    }

    private void newLoadDone() {
        if (mHasShowNew) {
            mIncludeLayoutNewPic.setVisibility(View.GONE);
        }
        if (mNewPicAdapter != null) {
            if (mNewAddPic == null || mNewAddPic.size() == 0) {
                mHasShowNew = true;
                mIncludeLayoutNewPic.setVisibility(View.GONE);
            } else if (!mHasShowNew){
                mHasShowNew = true;
                mIncludeLayoutNewPic.setVisibility(View.VISIBLE);
                mNewPicAdapter.setDataList(mNewAddPic);
                mNewPicAdapter.notifyDataSetChanged();
                updateTips();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_hide);
        mPt = LeoPreference.getInstance();
        initImageLoder();
        handleIntent();
        initUI();
    }

    private void handleIntent() {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {
            SDKWrapper.addEvent(ImageHideMainActivity.this, SDKWrapper.P1,
                    "assistant", "hidepic_cnts");
        }
        if (!getIntent().getBooleanExtra("hidePicFinish", true)) {
            if (mToast == null) {
                mToast = Toast.makeText(ImageHideMainActivity.this,
                        getString(R.string.toast_hide_ing), Toast.LENGTH_SHORT);
            } else {
                mToast.setText(getString(R.string.toast_hide_ing));
            }
            mToast.show();
        }
        mIsFromConfirm = getIntent().getBooleanExtra(Constants.FROM_CONFIRM_FRAGMENT, false);
    }

    private void goNewHideImageActivity() {
        ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).haveCheckedPic();
        Intent intent = new Intent(ImageHideMainActivity.this, NewHideImageActivity.class);
        startActivity(intent);
    }


    private void initUI() {
        mRlWholeShowContent = (RelativeLayout) findViewById(R.id.rl_whole_show_content);
        mIncludeLayoutNewPic = findViewById(R.id.layout_newpic);
        mRvHideNew = (RippleView) mIncludeLayoutNewPic.findViewById(R.id.rv_hide_new);
        mRvHideNew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goNewHideImageActivity();
            }
        });
        mTvIgnoreNew = (TextView) mIncludeLayoutNewPic.findViewById(R.id.tv_ignore);
        mTvIgnoreNew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).haveCheckedPic();
                hideHeadLayout();
            }
        });

        mTvNewAmountTips = (TextView) mIncludeLayoutNewPic.findViewById(R.id.tv_new_amount_tip);
        mGvNewPic = (GridView) mIncludeLayoutNewPic.findViewById(R.id.gv_newpic);
        mGvNewPic.setAdapter(mNewPicAdapter);

        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_image_hide);
        mTtileBar.setOptionMenuVisible(false);
        mTtileBar.setNavigationClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        loadingBar = (ProgressBar) findViewById(R.id.pb_loading_pic);
        mGridView = (GridView) findViewById(R.id.Image_hide_folder);
        mGridView.setOnItemClickListener(this);
        mGridView.setAdapter(mHideAlbumAdapt);
        mRvAdd = (RippleView) findViewById(R.id.rv_add);
        mRvAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImageHideMainActivity.this, ImageGalleryActivity.class);
                startActivityForResult(intent, REQUEST_CODE_OPTION);
                ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).haveCheckedVid();
            }
        });
        mNoHidePictureHint = (RelativeLayout) findViewById(R.id.no_hide);
    }

    private void hideHeadLayout() {
        final float initialY = mRlWholeShowContent.getY();
        PropertyValuesHolder m = PropertyValuesHolder.ofFloat("y",mRlWholeShowContent.getY(),mRlWholeShowContent.getY() - mIncludeLayoutNewPic.getHeight());
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mRlWholeShowContent, m);
        anim.setDuration(500);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIncludeLayoutNewPic.setVisibility(View.GONE);
                mRlWholeShowContent.setY(initialY);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                mIncludeLayoutNewPic.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });
//        TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -mIncludeLayoutNewPic.getHeight());
//        ta.setDuration(500);
//        ta.setFillAfter(false);
//        mRlWholeShowContent.setAnimation(ta);
//        mRlWholeShowContent.startAnimation(ta);
//        ta.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                mIncludeLayoutNewPic.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });
    }

    private void initImageLoder() {
        mOptions = ImagePreviewUtil.getPreviewOptions();
        mImageSize = ImagePreviewUtil.getPreviewSize();
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImageLoader != null) {
            mImageLoader.stop();
            mImageLoader.clearMemoryCache();
        }
        if (mToast != null) {
            mToast.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        asyncLoad();
        mNewAddPic = PrivacyHelper.getImagePrivacy().getNewList();
        LeoLog.i("newpic", "mNewAddPic size = " + mNewAddPic.size());
        newLoadDone();

        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        pdm.haveCheckedPic();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent intent = new Intent(ImageHideMainActivity.this,
                ImageGridActivity.class);
        Bundle bundle = new Bundle();
        int size = 0;
        PhotoAibum photoAibum = null;
        if (mAlbumList != null && mAlbumList.size() > 0) {
            photoAibum = mAlbumList.get(position);
            size = photoAibum.getBitList().size();
        }
        if (size < 800) {
            bundle.putSerializable("data", photoAibum);
        }
        intent.putExtra("pos", position);
        intent.putExtra("mode", ImageGridActivity.CANCEL_HIDE_MODE);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_OPTION);
        ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).haveCheckedVid();
    }


    class NewPicAdapter extends BaseAdapter {
        Context context;
        List<PhotoItem> list = new ArrayList<PhotoItem>();

        public NewPicAdapter(Context context) {
            this.context = context;
        }

        public void setDataList(List<PhotoItem> alist) {
            list.clear();
            this.list.addAll(alist);
        }

        @Override
        public int getCount() {
            if (list == null) {
                return 0;
            }
//            LeoLog.i("newpic", "showed size = " + Math.min(NEW_PIC_MAX_SHOW_AMOUNT, list.size()));
            return Math.min(NEW_PIC_MAX_SHOW_AMOUNT, list.size());

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            final View v = LayoutInflater.from(ImageHideMainActivity.this).inflate(R.layout.item_gv_new_pic, parent, false);
            final ImageView iv = (ImageView) v.findViewById(R.id.iv_pic);
            TextView tv = (TextView) v.findViewById(R.id.tv_more);
            if (list.size() > 5 && position == 4) {
                tv.setText("+" + (list.size() - 4));
            }
            String path = list.get(position).getPath();

            String uri = null;
            if (path != null && path.endsWith(Constants.CRYPTO_SUFFIX)) {
                uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
            } else {
                uri = ImageDownloader.Scheme.FILE.wrap(path);
            }
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    goNewHideImageActivity();
                }
            });

//            mImageLoader.displayImage(uri, iv, mOptions);


//            if (list.size() > 5 && position == 5) {
//                try {
//                    BitmapDrawable bd = (BitmapDrawable) iv.getDrawable();
//
//                } catch (Throwable t) {
//
//                }
//            }

            mImageLoader.loadImage(uri,mOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    LeoLog.i("newpic","loading failed    " + imageUri);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (list.size() > 5 && position == 4) {
                        LeoLog.i("newpic","try blur ");
                        try {
                            loadedImage = Bitmap.createScaledBitmap(loadedImage,50,50,true);
                            loadedImage = FastBlur.doBlur(loadedImage, 25, true);
                            iv.setBackgroundDrawable(new BitmapDrawable(loadedImage));
                        } catch (Throwable t) {
                            LeoLog.i("newpic","blur error");
                        }
                    } else {
                        iv.setImageBitmap(loadedImage);
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });


//            iv.setImageResource(R.drawable.ic_launcher);
            return v;
        }
    }


    class HideAlbumAdapt extends BaseAdapter {
        Context context;
        List<PhotoAibum> list = new ArrayList<PhotoAibum>();

        public HideAlbumAdapt(Context context) {
            this.context = context;
        }

        public void setDataList(List<PhotoAibum> alist) {
            list.clear();
            this.list.addAll(alist);
        }

        @Override
        public int getCount() {
            return list.size();
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
//            ViewHolder viewHolder;
//            String path;
//            if (convertView == null) {
//                convertView = getLayoutInflater().inflate(R.layout.item_gridview_album, parent, false);
//                viewHolder = new ViewHolder();
//                viewHolder.img = (ImageView) convertView
//                        .findViewById(R.id.img_item_album);
//                viewHolder.txt = (TextView) convertView
//                        .findViewById(R.id.txt_item_album);
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//            path = list.get(position).getBitList().get(0).getPath();
//            viewHolder.txt.setText(list.get(position).getName() + "("
//                    + list.get(position).getCount() + ")");
//
//            String uri = null;
//            if (path != null && path.endsWith(Constants.CRYPTO_SUFFIX)) {
//                uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
//            } else {
//                uri = ImageDownloader.Scheme.FILE.wrap(path);
//            }
//            mImageLoader.displayImage(uri, viewHolder.img, mOptions, mImageSize);
//            return convertView;
//        }

            NewViewHolder viewHolder;
            String path;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_gridview_album_nobg, parent, false);
                viewHolder = new NewViewHolder();
                viewHolder.img = (ImageView) convertView.findViewById(R.id.iv_pic);
                viewHolder.name = (TextView) convertView.findViewById(R.id.tv_folder_name);
                viewHolder.amount = (TextView) convertView.findViewById(R.id.tv_folder_size);
                viewHolder.vicon = (ImageView) convertView.findViewById(R.id.iv_video_icon);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (NewViewHolder) convertView.getTag();
            }
            viewHolder.vicon.setVisibility(View.GONE);
            path = list.get(position).getBitList().get(0).getPath();
            viewHolder.name.setText(list.get(position).getName());
            viewHolder.amount.setText(list.get(position).getCount());

            String uri = null;
            if (path != null && path.endsWith(Constants.CRYPTO_SUFFIX)) {
                uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
            } else {
                uri = ImageDownloader.Scheme.FILE.wrap(path);
            }
            mImageLoader.displayImage(uri, viewHolder.img, mOptions, mImageSize);
            return convertView;
        }


    }

    private static class ViewHolder {
        private ImageView img;
        private TextView txt;
    }

    private static class NewViewHolder {
        private ImageView img;
        private TextView name;
        private TextView amount;
        private ImageView vicon;
    }

}
