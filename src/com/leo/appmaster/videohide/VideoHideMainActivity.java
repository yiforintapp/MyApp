
package com.leo.appmaster.videohide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.MediaColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockOptionActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.appsetting.AboutActivity;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.videohide.AsyncLoadImage.ImageCallback;

@SuppressLint("NewApi")
public class VideoHideMainActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener {
    private GridView mGridView;
    private CommonTitleBar mTtileBar;
    private Button mAddButton;
    private RelativeLayout mNoHidePictureHint;
    private Cursor mCursor;
    private List<VideoBean> hideVideos;
    private TextView mNohideVideo;
    private HideVideoAdapter adapter;
    private boolean mShouldLockOnRestart = true;
    public static final int REQUEST_CODE_LOCK = 1000;
    public static final int REQUEST_CODE_OPTION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_hide);
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideVideos = getVideoInfo();
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

    private void initUI() {
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle(R.string.app_video_hide);
        mTtileBar.openBackView();
        mTtileBar.setOptionImage(R.drawable.selector_applock_setting);
        mTtileBar.setOptionImageVisibility(View.VISIBLE);
        mTtileBar.setOptionListener(this);
        mAddButton = (Button) findViewById(R.id.add_hide_image);
        mAddButton.setOnClickListener(this);
        mNoHidePictureHint = (RelativeLayout) findViewById(R.id.no_hide);
        mNohideVideo = (TextView) findViewById(R.id.nohideTV);
        mGridView = (GridView) findViewById(R.id.Video_hide_folder);
        mGridView.setOnItemClickListener(this);

    }

    @Override
    protected void onDestroy() {
        mCursor.close();
        super.onDestroy();
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.add_hide_image:
                Intent intent = new Intent(VideoHideMainActivity.this,
                        VideoHideGalleryActivity.class);
                VideoHideMainActivity.this.startActivityForResult(intent, REQUEST_CODE_OPTION);
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
        AsyncLoadImage asyncLoadImage;
        Context context;
        List<VideoBean> videos;
        LayoutInflater layoutInflater;

        public HideVideoAdapter(Context context, List<VideoBean> videos) {
            this.context = context;
            this.videos = videos;
            layoutInflater = LayoutInflater.from(context);
            asyncLoadImage = new AsyncLoadImage();
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
            ImageView imageView;
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
                        .findViewById(R.id.img_item_album);
                ;
                viewHolder.text = (TextView) convertView
                        .findViewById(R.id.txt_item_album);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            VideoBean video = videos.get(position);
            final String path = video.getPath();
            final ImageView imageView = viewHolder.imageView;
            imageView.setTag(path);
            viewHolder.text.setText(video.getName() + "(" + video.getCount()
                    + ")");
            viewHolder.imageView.setBackgroundDrawable(context.getResources()
                    .getDrawable(R.drawable.video_loading));
            Drawable drawableCache = asyncLoadImage.loadImage(imageView, path,
                    new ImageCallback() {

                        @Override
                        public void imageLoader(Drawable drawable) {
                            if (imageView != null
                                    && imageView.getTag().equals(path) && drawable!=null) {
                                imageView.setBackgroundDrawable(drawable);
                            }
                        }
                    });
            if (drawableCache != null) {
                viewHolder.imageView.setBackgroundDrawable(drawableCache);
            }
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
        try {
            mCursor = getContentResolver().query(uri, null, selection, null,
                    MediaColumns.DATE_ADDED + " desc");
        } catch (Exception e) {
        }
        if (mCursor != null) {
            Map<String, VideoBean> countMap = new HashMap<String, VideoBean>();
            while (mCursor.moveToNext()) {
                VideoBean video = new VideoBean();
                String path = mCursor
                        .getString(mCursor
                                .getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                String dirName = FileOperationUtil.getDirNameFromFilepath(path);
                String dirPath = FileOperationUtil.getDirPathFromFilepath(path);
                video.setDirPath(dirPath);
                video.setName(dirName);

                VideoBean vb = null;
                if (!countMap.containsKey(dirPath)) {
                    vb = new VideoBean();
                    vb.setName(dirName);
                    vb.setCount("1");
                    vb.setDirPath(dirPath);
                    vb.getBitList().add(new VideoItemBean(path));
                    vb.setPath(path);
                    countMap.put(dirPath, vb);
                } else {
                    vb = countMap.get(dirPath);
                    vb.setCount(String.valueOf(Integer.parseInt(vb.getCount()) + 1));
                    vb.getBitList().add(new VideoItemBean(path));
                }
            }
            Iterable<String> it = countMap.keySet();
            for (String key : it) {
                videoBeans.add(countMap.get(key));
            }
             Collections.sort(videoBeans, mFolderCamparator);
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
        Intent intent = new Intent(this, VideoGriActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", video);
        bundle.putInt("mode", Constants.CANCLE_HIDE_MODE);
        intent.putExtras(bundle);
        try {
            startActivityForResult(intent, REQUEST_CODE_OPTION);
        } catch (Exception e) {
        }

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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivityForResult(intent, REQUEST_CODE_LOCK);
    }

    @Override
    public void onActivityResault(int requestCode, int resultCode) {
        if (REQUEST_CODE_LOCK == requestCode) {
            mShouldLockOnRestart = false;
        } else if (REQUEST_CODE_OPTION == requestCode) {
            mShouldLockOnRestart = false;
        }
    }

}
