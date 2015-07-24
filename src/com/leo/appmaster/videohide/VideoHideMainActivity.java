
package com.leo.appmaster.videohide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.MediaColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

@SuppressLint("NewApi")
public class VideoHideMainActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener {
    private GridView mGridView;
    private CommonTitleBar mTtileBar;
    private Button mAddButton;
    private RelativeLayout mNoHidePictureHint;
    private List<VideoBean> hideVideos;
    private TextView mNohideVideo;
    private HideVideoAdapter adapter;
    public static final int REQUEST_CODE_LOCK = 1000;
    public static final int REQUEST_CODE_OPTION = 1001;
    // public static final String CB_PACKAGENAME = "com.cool.coolbrowser";
    public static final String URL_CB = "http://www.baidu.com";
    public static final String CB_PACKAGENAME = "com.example.appmaster_service";
    public static final int TARGET_VERSION = 14;
    public static final String SECOND_CATALOG = "Coolbrowser";
    public static final String LAST_CATALOG = "Download";
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private boolean isCbHere = false;
    private int mCbVersionCode = -1;
    private boolean isHaveCbFloder = false;
    private LEOAlarmDialog mDialog;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_hide_main);
        initUI();
        initImageLoder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideVideos = getVideoInfo();
        checkCbAndVersion();
        makeCbFloderFirst();
        adapter = new HideVideoAdapter(this, hideVideos);
        mGridView.setAdapter(adapter);
        if (hideVideos != null) {
            if (hideVideos.size() > 0) {
                mNoHidePictureHint.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
            } else {
                mNoHidePictureHint.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.GONE);
                mNohideVideo.setText(getString(R.string.app_no_video_hide));
            }
        }
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
            if (mName.equals(LAST_CATALOG) && mSecondName.equals(SECOND_CATALOG)) {
                isHaveCbFloder = true;
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
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle(R.string.app_video_hide);
        mTtileBar.openBackView();
        // mTtileBar.setOptionImage(R.drawable.selector_applock_setting);
        // mTtileBar.setOptionImageVisibility(View.VISIBLE);
        // mTtileBar.setOptionListener(this);
        mAddButton = (Button) findViewById(R.id.add_hide_image);
        mAddButton.setOnClickListener(this);
        mNoHidePictureHint = (RelativeLayout) findViewById(R.id.no_hide);
        mNohideVideo = (TextView) findViewById(R.id.nohideTV);
        mGridView = (GridView) findViewById(R.id.Video_hide_folder);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideVideos.clear();
        if (mImageLoader != null) {
            mImageLoader.clearMemoryCache();
        }
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.add_hide_image:
                Intent intent = new Intent(VideoHideMainActivity.this,
                        VideoHideGalleryActivity.class);
                VideoHideMainActivity.this.startActivityForResult(intent, REQUEST_CODE_OPTION);
                break;
            // case R.id.tv_option_image:
            // intent = new Intent(this, LockOptionActivity.class);
            // intent.putExtra(LockOptionActivity.TAG_COME_FROM,
            // LockOptionActivity.FROM_IMAGEHIDE);
            // startActivityForResult(intent, REQUEST_CODE_OPTION);
            // break;
            default:
                break;
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
            // final String path = video.getPath();
            String path = video.getPath();
            String name = video.getName();
            String secondName = FileOperationUtil.getSecondDirNameFromFilepath(path);
            // final ImageView imageView = viewHolder.imageView;
            // imageView.setTag(path);
            viewHolder.text.setText(name + "(" + video.getCount()
                    + ")");
            viewHolder.imageView.setBackgroundDrawable(context.getResources()
                    .getDrawable(R.drawable.video_loading));

            if (name.equals(LAST_CATALOG) && secondName.equals(SECOND_CATALOG)) {
                viewHolder.mImageCbIcon.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mImageCbIcon.setVisibility(View.GONE);
            }

            // Drawable drawableCache = asyncLoadImage.loadImage(imageView,
            // path,
            // new ImageCallback() {
            //
            // @Override
            // public void imageLoader(Drawable drawable) {
            // if (imageView != null
            // && imageView.getTag().equals(path) && drawable != null) {
            // imageView.setBackgroundDrawable(drawable);
            // }
            // }
            // });
            // if (drawableCache != null) {
            // viewHolder.imageView.setBackgroundDrawable(drawableCache);
            // }
            String filePath = "voidefile://" + path;
            mImageLoader.displayImage(filePath, viewHolder.imageView, mOptions);
            return convertView;
        }

    }

    /**
     * getVideoInfo
     */
    public List<VideoBean> getVideoInfo() {

        List<VideoBean> videoBeans = new ArrayList<VideoBean>();
        Uri uri = Files.getContentUri("external");
        String selection = MediaColumns.DATA + " LIKE '%.leotmv'";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, selection, null,
                    MediaColumns.DATE_ADDED + " desc");
            if (cursor != null) {
                Map<String, VideoBean> countMap = new HashMap<String, VideoBean>();
                while (cursor.moveToNext()) {
                    VideoBean video = new VideoBean();
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                    String dirName = FileOperationUtil.getDirNameFromFilepath(path);
                    String dirPath = FileOperationUtil.getDirPathFromFilepath(path);
                    video.setDirPath(dirPath);
                    video.setName(dirName);
                    File videoFile = new File(path);
                    boolean videoExists = videoFile.exists();
                    if (videoExists) {
                        VideoBean vb = null;
                        if (!countMap.containsKey(dirPath)) {
                            vb = new VideoBean();
                            vb.setName(dirName);
                            vb.setDirPath(dirPath);
                            vb.getBitList().add(new VideoItemBean(path));
                            vb.setPath(path);
                            countMap.put(dirPath, vb);
                        } else {
                            vb = countMap.get(dirPath);
                            vb.getBitList().add(new VideoItemBean(path));
                        }
                    }
                }

                Iterable<String> it = countMap.keySet();
                for (String key : it) {
                    videoBeans.add(countMap.get(key));
                }
                Collections.sort(videoBeans, mFolderCamparator);
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return videoBeans;
    }

    /**
     * Comparator date
     */
    public Comparator<VideoBean> mFolderCamparator = new Comparator<VideoBean>() {

        public final int compare(VideoBean a, VideoBean b) {
            if (a.getmLastModifyDate().before(b.getmLastModifyDate()))
                return 1;
            if (a.getmLastModifyDate().after(b.getmLastModifyDate()))
                return -1;
            return 0;
        }
    };

    /**
     * GrideView onItemClick
     */
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        VideoBean video = hideVideos.get(position);
        if (isHaveCbFloder && position == 0 && !isCbHere) {
            // showDialog to download CB
            if (mDialog == null) {
                mDialog = new LEOAlarmDialog(this);
            }
            mDialog.setOnClickListener(new OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    if (which == 1) {
                        //getURL and go browser
                        requestUrl();
                    }
                }
            });
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setContent(getString(R.string.video_hide_need_cb));
            mDialog.setSureButtonText(getString(R.string.button_install));
            mDialog.show();
        } else {
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

    private void requestUrl() {
        Uri uri = Uri.parse(URL_CB);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        this.startActivity(intent);
    }

    private void checkCbAndVersion() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> list = packageManager
                .getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo packageInfo : list) {
            String packNameString = packageInfo.packageName;
            if (packNameString.equals(VideoHideMainActivity.CB_PACKAGENAME)) {
                isCbHere = true;
                mCbVersionCode = packageInfo.versionCode;
                LeoLog.d("testCb", "Cb is here!! version is : " + mCbVersionCode);
            }
        }
    }

}
