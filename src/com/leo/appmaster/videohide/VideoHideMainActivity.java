
package com.leo.appmaster.videohide;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

@SuppressLint("NewApi")
public class VideoHideMainActivity extends BaseActivity implements OnItemClickListener {
    public final static int INIT_UI_DONE = 26;
    public final static int LOAD_DATA_DONE = 27;
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
    public static final String DEFAULT_PATH =
            "xxx/xxx/Coolbrowser/Download/";
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private AppMasterPreference mSpSaveDir;
    private ProgressBar loadingBar;

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
                mGridView.setVisibility(View.VISIBLE);
            } else {
                mNoHidePictureHint.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
                mGridView.setVisibility(View.GONE);
                mNohideVideo.setText(getString(R.string.app_no_video_hide));
            }
        }
    }

    private void asyncLoad() {
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_hide_main);
        initImageLoder();
        initUI();

        getDirFromSp();
        handleIntent();
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
                .showImageOnLoading(R.drawable.video_loading)
                .showImageForEmptyUri(R.drawable.video_loading)
                .showImageOnFail(R.drawable.video_loading)
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
        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_video_hide);
        mTtileBar.setOptionMenuVisible(false);
        mRvAdd = (RippleView) findViewById(R.id.rv_add);
        mRvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VideoHideMainActivity.this,
                        VideoHideGalleryActivity.class);
                VideoHideMainActivity.this.startActivityForResult(intent, REQUEST_CODE_OPTION);
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
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.item_video_gridview_album, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView
                        .findViewById(R.id.video_item_album);
                viewHolder.mImageCbIcon = (ImageView) convertView.findViewById(R.id.iv_cb_icon);
                viewHolder.text = (TextView) convertView
                        .findViewById(R.id.txt_item_album);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            VideoBean video = videos.get(position);
            String path = video.getPath();
            String name = video.getName();
            String secondName = FileOperationUtil.getSecondDirNameFromFilepath(path);
            viewHolder.text.setText(name + "(" + video.getCount()
                    + ")");
            viewHolder.imageView.setBackgroundDrawable(context.getResources()
                    .getDrawable(R.drawable.video_loading));
            LeoLog.d("testIntent", "name is : " + name);
            LeoLog.d("testIntent", "secondName is : " + secondName);
            if (name.equals(LAST_CATALOG) && secondName.equals(SECOND_CATALOG)) {
                viewHolder.mImageCbIcon.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mImageCbIcon.setVisibility(View.GONE);
            }
            String filePath = "voidefile://" + path;
            mImageLoader.displayImage(filePath, viewHolder.imageView, mOptions);
            return convertView;
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
        } catch (Exception e) {
        }
    }
}
