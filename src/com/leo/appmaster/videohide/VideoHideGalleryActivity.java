
package com.leo.appmaster.videohide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.LockOptionActivity;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.MediaChangeEvent;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageScaleType;

@SuppressLint("NewApi")
public class VideoHideGalleryActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener {
    private static final String TAG = "VideoHideGalleryActivity";
    private GridView mGridView;
    private CommonToolbar mTtileBar;
    private RelativeLayout mNoHidePictureHint;
    private List<VideoBean> hideVideos;
    private TextView mNohideVideo;
    private HideVideoAdapter adapter;
    public static final int REQUEST_CODE_OPTION = 1001;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private ProgressBar loadingBar;

    private void loadDone() {
        mGridView.setAdapter(adapter);
        if (hideVideos != null) {
            if (hideVideos.size() > 0) {
                mNoHidePictureHint.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
            } else {
                mNoHidePictureHint.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mNohideVideo.setText(getString(R.string.app_no_video_gallery_hide));
            }
        }
    }

    private void asyncLoad() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                hideVideos = ((PrivacyDataManager) MgrContext.
                        getManager(MgrContext.MGR_PRIVACY_DATA)).
                        getAllVidFile();
                ThreadManager.executeOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadDone();
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_hide);
        initImageLoder();
        initUI();
        asyncLoad();
    }

//    public void onEvent(MediaChangeEvent event) {
//        if (event == null || !event.isImage) {
//            return;
//        }
//
//        LeoLog.d(TAG, "<ls> onEvent...");
//        mDataChanged = true;
//    }

    private void initImageLoder() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(new ColorDrawable(0xd7d7dd))
                .showImageForEmptyUri(new ColorDrawable(0xd7d7dd))
                .showImageOnFail(new ColorDrawable(0xd7d7dd))
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(ImageLoaderConfiguration.createDefault(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hideVideos != null) {
            if (hideVideos.size() > 0) {
                mNoHidePictureHint.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
            } else {
                mNoHidePictureHint.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.GONE);
                mNohideVideo.setText(getString(R.string.app_no_video_gallery_hide));
            }
        }
    }

    private void initUI() {
        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_video_gallery);
        mTtileBar.setOptionMenuVisible(false);
        RelativeLayout addButton = (RelativeLayout) findViewById(R.id.video_gallery_buttom);
        addButton.setVisibility(View.GONE);
        mNoHidePictureHint = (RelativeLayout) findViewById(R.id.no_hide);
        mNohideVideo = (TextView) findViewById(R.id.nohideTV);
        mGridView = (GridView) findViewById(R.id.Video_hide_folder);
        mGridView.setOnItemClickListener(this);
        adapter = new HideVideoAdapter(VideoHideGalleryActivity.this);
        loadingBar = (ProgressBar) findViewById(R.id.pb_loading_vid_hide);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hideVideos != null) {
            hideVideos.clear();
        }
        if (mImageLoader != null) {
            mImageLoader.clearMemoryCache();
        }
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.add_hide_image:
                Intent intent = new Intent(VideoHideGalleryActivity.this,
                        VideoHideGalleryActivity.class);
                VideoHideGalleryActivity.this.startActivity(intent);
                break;
            case R.id.tv_option_image:
                intent = new Intent(this, LockOptionActivity.class);
                intent.putExtra(LockOptionActivity.TAG_COME_FROM, LockOptionActivity.FROM_IMAGEHIDE);
                startActivityForResult(intent, REQUEST_CODE_OPTION);
                break;
            default:
                break;
        }

    }

    /**
     * HideVideoAdapter
     */
    class HideVideoAdapter extends BaseAdapter {
        Context context;
        LayoutInflater layoutInflater;

        public HideVideoAdapter(Context context) {
            this.context = context;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return hideVideos != null ? hideVideos.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return hideVideos != null ? hideVideos.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            ImageView imageView;
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
            path = hideVideos.get(position).getBitList().get(0).getPath();
            viewHolder.name.setText(hideVideos.get(position).getName());
            viewHolder.amount.setText(hideVideos.get(position).getCount()+"");

//            if (path != null && path.endsWith(Constants.CRYPTO_SUFFIX)) {
//                uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
//            } else {
//                uri = ImageDownloader.Scheme.FILE.wrap(path);
//            }
            String filePath = "voidefile://" + path;
            mImageLoader.displayImage(filePath, viewHolder.img, mOptions);
            return convertView;
        }
    }

    /**
     * Comparator is Date
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
        Intent intent = new Intent(this, VideoGriActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", video);
        bundle.putInt("mode", Constants.SELECT_HIDE_MODE);
        intent.putExtras(bundle);
        try {
            startActivityForResult(intent, 1001);
        } catch (Exception e) {
        }

    }

    public Comparator<VideoBean> folderamparator = new Comparator<VideoBean>() {

        public final int compare(VideoBean a, VideoBean b) {
            File fileA = new File(a.getDirPath());
            File fileB = new File(b.getDirPath());
            if (new Date(fileA.lastModified()).before(new Date(fileB.lastModified())))
                return 1;
            if (new Date(fileA.lastModified()).after(new Date(fileB.lastModified())))
                return -1;
            return 0;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && hideVideos != null) {
            Bundle bundle = data.getExtras();
            boolean needRefresh = false;
            if (bundle != null) {
                ArrayList<String> resultPath = (ArrayList<String>) bundle.get("path");
                if (resultPath != null) {
                    if (resultPath.size() > 0) {
                        for (int i = 0; i < hideVideos.size(); i++) {
                            VideoBean bin = hideVideos.get(i);
                            ArrayList<VideoItemBean> remove = new ArrayList<VideoItemBean>();
                            for (VideoItemBean itemBean : bin.getBitList()) {
                                String path = itemBean.getPath();
                                if (resultPath.contains(path)) {
                                    remove.add(itemBean);
                                    needRefresh = true;
                                }
                            }
                            bin.getBitList().removeAll(remove);

                            try {
                                if (bin.getBitList().size() <= 0) {
                                    hideVideos.remove(bin);
                                }
                            } catch (Exception e) {
                            }
                        }

                        Collections.sort(hideVideos, folderamparator);
                        if (adapter != null && needRefresh) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    private static class NewViewHolder {
        private ImageView img;
        private TextView name;
        private TextView amount;
        private ImageView vicon;
    }
}
