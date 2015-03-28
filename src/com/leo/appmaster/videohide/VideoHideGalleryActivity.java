
package com.leo.appmaster.videohide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.MediaColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockOptionActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.videohide.AsyncLoadImage.ImageCallback;

@SuppressLint("NewApi")
public class VideoHideGalleryActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener {
    private GridView mGridView;
    private CommonTitleBar mTtileBar;
    private RelativeLayout mNoHidePictureHint;
    private List<VideoBean> hideVideos;
    private TextView mNohideVideo;
    private HideVideoAdapter adapter;
    private AsyncLoadImage asyncLoadImage;
    public static final int REQUEST_CODE_LOCK = 1000;
    public static final int REQUEST_CODE_OPTION = 1001;
    private VideoHideDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_hide);
        asyncLoadImage = new AsyncLoadImage();
        initUI();
        adapter = new HideVideoAdapter(VideoHideGalleryActivity.this);
        dialog = new VideoHideDialog(this);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.5f;
        lp.dimAmount = 0.0f;
        window.setAttributes(lp);
        VideoHideGalleryTask videoTask = new VideoHideGalleryTask();
        videoTask.execute(true);
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
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle(R.string.app_video_gallery);
        mTtileBar.openBackView();
        RelativeLayout addButton = (RelativeLayout) findViewById(R.id.video_gallery_buttom);
        addButton.setVisibility(View.GONE);
        mNoHidePictureHint = (RelativeLayout) findViewById(R.id.no_hide);
        mNohideVideo = (TextView) findViewById(R.id.nohideTV);
        mGridView = (GridView) findViewById(R.id.Video_hide_folder);
        mGridView.setOnItemClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hideVideos != null) {
            hideVideos.clear();
        }
        if(asyncLoadImage != null) {
            asyncLoadImage.cancel();
        }
    }
    
    @Override
    public void finish() {
        super.finish();
        if(asyncLoadImage != null) {
            asyncLoadImage.cancel();
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
        // List<VideoBean> videos;
        LayoutInflater layoutInflater;

        public HideVideoAdapter(Context context) {
            this.context = context;
            // this.videos = videos;
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

        /**
         * TODO
         */
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
            VideoBean video = hideVideos.get(position);
            final String path = video.getBitList().get(0).getPath();
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
                                    && imageView.getTag().equals(path) && drawable != null) {
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
        String selection = Constants.VIDEO_FORMAT;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, selection, null,
                    MediaColumns.DATE_MODIFIED + " desc");
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
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    private class VideoHideGalleryTask extends AsyncTask<Boolean, Integer, List<VideoBean>> {
        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected List<VideoBean> doInBackground(Boolean... params) {
            boolean isHide = params[0];
            List<VideoBean> hideVideos = null;
            if (isHide) {
                hideVideos = getVideoInfo();
            }
            return hideVideos;

        }

        @Override
        protected void onPostExecute(List<VideoBean> videos) {
            dialog.dismiss();
            hideVideos = videos;
            mGridView.setAdapter(adapter);
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
    }
}
