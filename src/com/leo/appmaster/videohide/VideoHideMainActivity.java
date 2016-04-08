
package com.leo.appmaster.videohide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.GradeEvent;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.FastBlur;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.QuickHelperUtils;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageScaleType;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class VideoHideMainActivity extends BaseActivity implements OnItemClickListener {
    public final static int INIT_UI_DONE = 26;
    public final static int LOAD_DATA_DONE = 27;
    private static final String TAG = "VideoHideMainActivity";
    private static final int ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT = 3;
    private GridView mGridView;
    private CommonToolbar mTtileBar;
    private RippleView mRvAdd;
    private RelativeLayout mNoHidePictureHint;
    private List<VideoBean> hideVideos;
    private TextView mNohideVideo;
    private HideVideoAdapter adapter;
    public static final int REQUEST_CODE_OPTION = 1001;
    public static String CB_PACKAGENAME = "com.cool.coolbrowser";
    public static String URL_CB = "http://m.coobrowser.com/";
    public static String SECOND_CATALOG;
    public static String LAST_CATALOG;
    private LEOAlarmDialog mDialogAskCreateShotcut;
    public static final String DEFAULT_PATH ="xxx/xxx/Coolbrowser/Download/";
    private final int NEW_PIC_MAX_SHOW_AMOUNT = 5;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private AppMasterPreference mSpSaveDir;
    private ProgressBar loadingBar;
    private LeoPreference mPt;
    private View mIncludeLayoutNewVid;
    private GridView mGvNewVid;
    private TextView mTvNewAmountTips;
    private RippleView mRvHideNew;
    private TextView mTvIgnoreNew;
    private RelativeLayout mRlWholeShowContent;
    private List<VideoItemBean> mNewDataList = null;
    private NewVidAdapter mNewVidAdapter = new NewVidAdapter(this);
    private boolean mHasShowNew = false;
    public static boolean mIsFromConfirm;

    private final int REQUEST_CODE_GO_NEW = 10;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case INIT_UI_DONE:
                    asyncLoad();
                    break;
                case LOAD_DATA_DONE:
                    loadDone();
                    break;
            }
        }
    };

    private void loadDone() {
        adapter = new HideVideoAdapter(this, hideVideos);
        mGridView.setAdapter(adapter);
        if (hideVideos != null) {
            if (hideVideos.size() > 0) {
                mNoHidePictureHint.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mRlWholeShowContent.setVisibility(View.VISIBLE);
            } else {
                mNoHidePictureHint.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
//                mRlWholeShowContent.setVisibility(View.GONE);
                mNohideVideo.setText(getString(R.string.app_no_video_hide));
            }
//            if (mNewVidAdapter != null) {
//                if (mNewDataList == null || mNewDataList.size() == 0) {
//                    mIncludeLayoutNewVid.setVisibility(View.GONE);
//                } else {
//                    mIncludeLayoutNewVid.setVisibility(View.VISIBLE);
//                    mNewVidAdapter.setDataList(mNewDataList);
//                    mNewVidAdapter.notifyDataSetChanged();
//                    updateTips();
//                }
//            }

//            if (mNewPicAdapter != null) {
//                if (mNewDataList == null || mNewDataList.size() == 0) {
//                    mIncludeLayoutNewPic.setVisibility(View.GONE);
//                } else {
//                    mIncludeLayoutNewPic.setVisibility(View.VISIBLE);
////                    mNewPicAdapter.setDataList(mNewDataList);
////                    mNewPicAdapter.notifyDataSetChanged();
////                    updateTips();
//                }
//            }
        }
    }

    private void updateTips() {
        String string1 = getString(R.string.find_new_vid_tips);
        String s2 = String.format(string1, mNewDataList.size());
        mTvNewAmountTips.setText(Html.fromHtml(s2));
    }

    private void asyncLoad() {
        mNewDataList = PrivacyHelper.getVideoPrivacy().getNewList();
        newLoadDone();
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                hideVideos = ((PrivacyDataManager) MgrContext.
                        getManager(MgrContext.MGR_PRIVACY_DATA)).getHideVidAlbum("");
                makeCbFloderFirst();
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(LOAD_DATA_DONE);
                }
            }
        });
        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
    }


    private void newLoadDone() {
        if (mHasShowNew) {
            mIncludeLayoutNewVid.setVisibility(View.GONE);
        }
        if (mNewVidAdapter != null) {
            if (mNewDataList == null || mNewDataList.size() == 0 || PrivacyHelper.getVideoPrivacy().getTotalCount() == PrivacyHelper.getVideoPrivacy().getNewCount()) {
                mHasShowNew = true;
                mIncludeLayoutNewVid.setVisibility(View.GONE);
            } else if (!mHasShowNew) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "hide_Video", "vid_card");
                mHasShowNew = true;
                mIncludeLayoutNewVid.setVisibility(View.VISIBLE);
                mNewVidAdapter.setDataList(mNewDataList);
                mNewVidAdapter.notifyDataSetChanged();
                updateTips();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_hide_main);
        initImageLoder();
        initUI();
        mPt = LeoPreference.getInstance();
        getDirFromSp();
        handleIntent();
    }
    
    @Override
    public void onBackPressed() {
        if (!mPt.getBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_VID, false) && mPt.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_VIDEO, 0) >= ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT) {
            mPt.putBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_VID, true);
            if (mDialogAskCreateShotcut == null) {
                mDialogAskCreateShotcut = new LEOAlarmDialog(this);
            }
            mDialogAskCreateShotcut.setDialogIconVisibility(true);
            mDialogAskCreateShotcut.setTitleVisiable(false);
            mDialogAskCreateShotcut.setDialogIcon(R.drawable.qh_video_icon);
            mDialogAskCreateShotcut.setContent(getString(R.string.ask_create_shortcut_content_videohide));
            mDialogAskCreateShotcut.setLeftBtnStr(getString(R.string.cancel));
            mDialogAskCreateShotcut.setRightBtnStr(getString(R.string.ask_create_shortcut_button_right));
            mDialogAskCreateShotcut.setRightBtnListener(new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SDKWrapper.addEvent(VideoHideMainActivity.this, SDKWrapper.P1, "assistant", "shortcut_hidevid");
                    Intent intent = new Intent();
                    intent = new Intent(AppMasterApplication.getInstance(), VideoHideMainActivity.class);
                    intent.putExtra("from_quickhelper", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    QuickHelperUtils.createQuickHelper(getString(R.string.quick_helper_video_hide), R.drawable.qh_video_icon, intent, VideoHideMainActivity.this);
                    Toast.makeText(VideoHideMainActivity.this, getString(R.string.quick_help_add_toast), Toast.LENGTH_SHORT).show();
                    mDialogAskCreateShotcut.dismiss();
                }
            });
            mDialogAskCreateShotcut.setLeftBtnListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SDKWrapper.addEvent(VideoHideMainActivity.this, SDKWrapper.P1, "assistant", "shortcut_hidevid_no");
                    mDialogAskCreateShotcut.dismiss();
                }
            });
            mDialogAskCreateShotcut.show();
        } else {
            if (hideVideos != null && hideVideos.size() == 0 && !mIsFromConfirm) {
                LeoEventBus.getDefaultBus().postSticky(new GradeEvent(GradeEvent.FROM_VID, false));
            }
            super.onBackPressed();
        }
    
    }

    private void getDirFromSp() {
        mSpSaveDir = AppMasterPreference.getInstance(this);
        LAST_CATALOG = mSpSaveDir.getLastDir();
        SECOND_CATALOG = mSpSaveDir.getSecondDir();
        LeoLog.d("testIntent", "getFromSp Last  is : " + LAST_CATALOG);
        LeoLog.d("testIntent", "getFromSp Second is : " + SECOND_CATALOG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(INIT_UI_DONE);
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        mIsFromConfirm = intent.getBooleanExtra(Constants.FROM_CONFIRM_FRAGMENT, false);
        if (intent.getBooleanExtra("from_quickhelper", false)) {
            SDKWrapper.addEvent(VideoHideMainActivity.this, SDKWrapper.P1,
                    "assistant", "hidevid_cnts");
        }

        String mPath = intent.getStringExtra("cb_download_path");
        if (mPath != null) {
            SDKWrapper.addEvent(VideoHideMainActivity.this, SDKWrapper.P1, "hidevd_cb",
                    "hide");
        }
        LeoLog.d("testIntent", "mPath : " + mPath);
        if (LAST_CATALOG.isEmpty() || SECOND_CATALOG.isEmpty()) {
            if (mPath == null) {
                mPath = DEFAULT_PATH;
                LAST_CATALOG = FileOperationUtil.getDirNameFromFilepath(mPath);
                SECOND_CATALOG = FileOperationUtil.getSecondDirNameFromFilepath(mPath);
            } else {
                LAST_CATALOG = FileOperationUtil.getDirNameFromFilepath(mPath);
                SECOND_CATALOG = FileOperationUtil.getSecondDirNameFromFilepath(mPath);
                // save to sp
                mSpSaveDir.setLastDir(LAST_CATALOG);
                mSpSaveDir.setSecondDi(SECOND_CATALOG);
            }
        } else {
            if (mPath != null) {
                LAST_CATALOG = FileOperationUtil.getDirNameFromFilepath(mPath);
                SECOND_CATALOG = FileOperationUtil.getSecondDirNameFromFilepath(mPath);
                // save to sp
                mSpSaveDir.setLastDir(LAST_CATALOG);
                mSpSaveDir.setSecondDi(SECOND_CATALOG);
            }
        }

        LeoLog.d("testIntent", "mLastName is : " + LAST_CATALOG);
        LeoLog.d("testIntent", "mSecondName is : " + SECOND_CATALOG);
    }

    /**
     * 判断是否有cb的文件夹，有的话排第一位
     */
    private void makeCbFloderFirst() {
        for (int i = 0; i < hideVideos.size(); i++) {
            VideoBean info = hideVideos.get(i);
            String mName = info.getName();
            String mPath = info.getPath();
            String mSecondName = FileOperationUtil.getSecondDirNameFromFilepath(mPath);
            if (mName != null && mName.equals(LAST_CATALOG) && mSecondName.equals(SECOND_CATALOG)) {
                if (i != 0) {
                    hideVideos.remove(i);
                    hideVideos.add(0, info);
                    break;
                }
            }
        }
    }



    private void initImageLoder() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(new ColorDrawable(0xd7d7dd))
                .showImageForEmptyUri(new ColorDrawable(0xd7d7dd))
                .showImageOnFail(new ColorDrawable(0xd7d7dd))
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(ImageLoaderConfiguration.createDefault(this));
    }

    private void initUI() {

        mRlWholeShowContent = (RelativeLayout) findViewById(R.id.rl_whole_show_content);
        mIncludeLayoutNewVid = findViewById(R.id.layout_newpic);
        ImageView f = (ImageView)mIncludeLayoutNewVid.findViewById(R.id.iv_hide_type);
        f.setImageResource(R.drawable.tips_video_icon);
        mRvHideNew = (RippleView) mIncludeLayoutNewVid.findViewById(R.id.rv_hide_new);
        mRvHideNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNewHideVActivity();
            }
        });
        mTvIgnoreNew = (TextView) mIncludeLayoutNewVid.findViewById(R.id.tv_ignore);
        mTvIgnoreNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).haveCheckedVid();
                hideHeadLayout();
            }
        });

        mTvNewAmountTips = (TextView) mIncludeLayoutNewVid.findViewById(R.id.tv_new_amount_tip);
        mGvNewVid = (GridView) mIncludeLayoutNewVid.findViewById(R.id.gv_newpic);
        mGvNewVid.setAdapter(mNewVidAdapter);

        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_video_hide);
        mTtileBar.setOptionMenuVisible(false);
        mTtileBar.setNavigationClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mRvAdd = (RippleView) findViewById(R.id.rv_add);
        mRvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VideoHideMainActivity.this, VideoHideGalleryActivity.class);
                VideoHideMainActivity.this.startActivityForResult(intent, REQUEST_CODE_OPTION);
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).haveCheckedPic();
                    }
                });
            }
        });
//        mRvAdd.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//            @Override
//            public void onRippleComplete(RippleView rippleView) {
//                Intent intent = new Intent(VideoHideMainActivity.this,
//                        VideoHideGalleryActivity.class);
//                VideoHideMainActivity.this.startActivityForResult(intent, REQUEST_CODE_OPTION);
//            }
//        });

        mNoHidePictureHint = (RelativeLayout) findViewById(R.id.no_hide);
        mNohideVideo = (TextView) findViewById(R.id.nohideTV);
        mGridView = (GridView) findViewById(R.id.Video_hide_folder);
        mGridView.setOnItemClickListener(this);
        loadingBar = (ProgressBar) findViewById(R.id.pb_loading_vid);
    }

    private void hideHeadLayout() {
        SDKWrapper.addEvent(this, SDKWrapper.P1, "hide_Video", "vid_card_no");
        final float initialY = mRlWholeShowContent.getY();
        PropertyValuesHolder m = PropertyValuesHolder.ofFloat("y",mRlWholeShowContent.getY(),mRlWholeShowContent.getY() - mIncludeLayoutNewVid.getHeight());
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mRlWholeShowContent, m);
        anim.setDuration(500);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIncludeLayoutNewVid.setVisibility(View.GONE);
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

//        TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -mIncludeLayoutNewVid.getHeight());
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
//                mIncludeLayoutNewVid.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if(hideVideos != null) {
            hideVideos.clear();
        }
        if (mImageLoader != null) {
            mImageLoader.clearMemoryCache();
        }
    }


    /**
     * HideVideoAdapter
     */
    class HideVideoAdapter extends BaseAdapter {
        Context context;
        List<VideoBean> videos;
        LayoutInflater layoutInflater;

        public HideVideoAdapter(Context context, List<VideoBean> videos) {
            this.context = context;
            this.videos = videos;
            layoutInflater = LayoutInflater.from(context);
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
            ImageView imageView, mImageCbIcon;
            TextView text;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            NewViewHolder viewHolder;
            String path;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_gridview_videos_nobg, parent, false);
                viewHolder = new NewViewHolder();
                viewHolder.img = (ImageView) convertView.findViewById(R.id.iv_pic);
                viewHolder.name = (TextView) convertView.findViewById(R.id.tv_folder_name);
                viewHolder.amount = (TextView) convertView.findViewById(R.id.tv_folder_size);
                viewHolder.vicon = (ImageView) convertView.findViewById(R.id.iv_video_icon);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (NewViewHolder) convertView.getTag();
            }
            viewHolder.vicon.setVisibility(View.VISIBLE);
            path = videos.get(position).getBitList().get(0).getPath();
            viewHolder.name.setText(videos.get(position).getName());
            viewHolder.amount.setText(videos.get(position).getCount()+"");

//            if (path != null && path.endsWith(Constants.CRYPTO_SUFFIX)) {
//                uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
//            } else {
//                uri = ImageDownloader.Scheme.FILE.wrap(path);
//            }
            String filePath = "voidefile://" + path;
            mImageLoader.displayImage(filePath, viewHolder.img, mOptions);
            return convertView;

//
//            ViewHolder viewHolder = null;
//            if (convertView == null) {
//                convertView = getLayoutInflater().inflate(
//                        R.layout.item_video_gridview_album, parent, false);
//                viewHolder = new ViewHolder();
//                viewHolder.imageView = (ImageView) convertView
//                        .findViewById(R.id.video_item_album);
//                viewHolder.mImageCbIcon = (ImageView) convertView.findViewById(R.id.iv_cb_icon);
//                viewHolder.text = (TextView) convertView
//                        .findViewById(R.id.txt_item_album);
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//            VideoBean video = videos.get(position);
//            String path = video.getPath();
//            String name = video.getName();
//            String secondName = FileOperationUtil.getSecondDirNameFromFilepath(path);
//            viewHolder.text.setText(name + "(" + video.getCount()
//                    + ")");
//            viewHolder.imageView.setBackgroundDrawable(context.getResources()
//                    .getDrawable(R.drawable.video_loading));
//            LeoLog.d("testIntent", "name is : " + name);
//            LeoLog.d("testIntent", "secondName is : " + secondName);
//            if (name.equals(LAST_CATALOG) && secondName.equals(SECOND_CATALOG)) {
//                viewHolder.mImageCbIcon.setVisibility(View.VISIBLE);
//            } else {
//                viewHolder.mImageCbIcon.setVisibility(View.GONE);
//            }
//            String filePath = "voidefile://" + path;
//            mImageLoader.displayImage(filePath, viewHolder.imageView, mOptions);
//            return convertView;
        }

    }


    /**
     * GrideView onItemClick
     */
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        VideoBean video = hideVideos.get(position);
        Intent intent = new Intent(this, VideoGriActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", video);
        bundle.putInt("mode", Constants.CANCLE_HIDE_MODE);
        bundle.putInt("fromwhere", 1);
        intent.putExtras(bundle);

        try {
            startActivityForResult(intent, REQUEST_CODE_OPTION);
            ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).haveCheckedVid();
        } catch (Exception e) {
        }
    }


    class NewVidAdapter extends BaseAdapter {
        Context context;
        List<VideoItemBean> list = new ArrayList<VideoItemBean>();

        public NewVidAdapter(Context context) {
            this.context = context;
        }

        public void setDataList(List<VideoItemBean> alist) {
            list.clear();
            this.list.addAll(alist);
        }

        @Override
        public int getCount() {
            if (list == null) {
                return 0;
            }
            LeoLog.i("newpic", "showed size = " + Math.min(NEW_PIC_MAX_SHOW_AMOUNT, list.size()));
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
            if (convertView == null) {
                convertView = LayoutInflater.from(VideoHideMainActivity.this).inflate(R.layout.item_gv_new_pic, parent, false);
            }
            final ImageView iv = (ImageView) convertView.findViewById(R.id.iv_pic);
            TextView tv = (TextView) convertView.findViewById(R.id.tv_more);
            if (list.size() > 5 && position == 4) {
                tv.setText("+" + (list.size() - 4));
            }

            String path = list.get(position).getPath();
            String uri = "voidefile://" + path;
//            if (path != null && path.endsWith(Constants.CRYPTO_SUFFIX)) {
//                uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
//            } else {
//                uri = ImageDownloader.Scheme.FILE.wrap(path);
//            }
//            mImageLoader.displayImage(uri, iv, mOptions);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goNewHideVActivity();
                }
            });

            mImageLoader.loadImage(uri, mOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    LeoLog.i("newpic", "loading failed    " + imageUri);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (list.size() > 5 && position == 4) {
                        LeoLog.i("newpic", "try blur ");
                        try {
                            loadedImage = Bitmap.createScaledBitmap(loadedImage, 50, 50, true);
                            loadedImage = FastBlur.doBlur(loadedImage, 25, true);
                            iv.setBackgroundDrawable(new BitmapDrawable(loadedImage));
                        } catch (Throwable t) {
                            LeoLog.i("newpic", "blur error");
                        }
                    } else {
                        iv.setImageBitmap(loadedImage);
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });

            return convertView;
        }
    }

    private void goNewHideVActivity() {
        //TODO
        Intent intent = new Intent(this, NewHideVidActivity.class);
        startActivityForResult(intent, REQUEST_CODE_GO_NEW);

        SDKWrapper.addEvent(this, SDKWrapper.P1, "hide_Video", "vid_card_add");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GO_NEW) {
            ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).haveCheckedVid();
        }
    }

    private static class NewViewHolder {
        private ImageView img;
        private TextView name;
        private TextView amount;
        private ImageView vicon;
    }
}
