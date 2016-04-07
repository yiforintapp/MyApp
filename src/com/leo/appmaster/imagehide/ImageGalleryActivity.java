package com.leo.appmaster.imagehide;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.mgr.impl.PrivacyDataManagerImpl;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageDownloader;
import com.leo.imageloader.core.ImageSize;

/**
 * @author linxiongzhou
 */
public class ImageGalleryActivity extends BaseActivity implements OnItemClickListener {
    public final static int INIT_UI_DONE = 22;
    public final static int LOAD_DATA_DONE = 23;
    private List<PhotoAibum> mAlbumList = null;
    private GridView mGridView;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private CommonToolbar mTtileBar;
    private RelativeLayout mNoPictureHint;
    private AlbumAdapt mAlbumAdapt = new AlbumAdapt(this);

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
    private ImageSize mImageSize;

    private void loadDone() {
        if (mAlbumList != null) {
            if (mAlbumList.size() > 0) {
                mNoPictureHint.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
                mAlbumAdapt.setDataList(mAlbumList);
                mAlbumAdapt.notifyDataSetChanged();
            } else {
                mNoPictureHint.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
                mGridView.setVisibility(View.GONE);
            }
        }
    }

    private void asyncLoad() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mAlbumList = ((PrivacyDataManager) MgrContext.
                        getManager(MgrContext.MGR_PRIVACY_DATA)).getAllPicFile(PrivacyDataManagerImpl.CHECK_APART);
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(LOAD_DATA_DONE);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_gallery);
        initImageLoder();
        initUI();
    }

    private void initUI() {
        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_image_gallery);
        mTtileBar.setOptionMenuVisible(false);
        mGridView = (GridView) findViewById(R.id.image_gallery_folder);
        mGridView.setOnItemClickListener(this);
        mGridView.setAdapter(mAlbumAdapt);
        mNoPictureHint = (RelativeLayout) findViewById(R.id.no_picture);
        loadingBar = (ProgressBar) findViewById(R.id.pb_loading_hide_pic);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mImageLoader != null) {
            mImageLoader.stop();
            mImageLoader.clearMemoryCache();
        }
    }


    @Override
    public void finish() {
        super.finish();
        if (mImageLoader != null) {
            mImageLoader.stop();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onResume() {
        mHandler.sendEmptyMessage(INIT_UI_DONE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initImageLoder() {
        mOptions = ImagePreviewUtil.getPreviewOptions();
        mImageSize = ImagePreviewUtil.getPreviewSize();
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (position < mAlbumList.size()) {
            Intent intent = new Intent(ImageGalleryActivity.this,
                    ImageGridActivity.class);
            Bundle bundle = new Bundle();
            PhotoAibum photoAibum = mAlbumList.get(position);
            int size = photoAibum.getBitList().size();
            if (size < 800) {
                bundle.putSerializable("data", photoAibum);
            }
            intent.putExtra("pos", position);
            intent.putExtra("mode", ImageGridActivity.SELECT_HIDE_MODE);
            intent.putExtras(bundle);
            startActivityForResult(intent, 1001);
        }
    }

    class AlbumAdapt extends BaseAdapter {
        Context context;
        List<PhotoAibum> list = new ArrayList<PhotoAibum>();

        public AlbumAdapt(Context context) {
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
            viewHolder.vicon.setVisibility(View.INVISIBLE);
            path = list.get(position).getBitList().get(0).getPath();
            viewHolder.name.setText(list.get(position).getName());
            viewHolder.amount.setText(list.get(position).getCount()+"");
            String uri = null;
            if (path != null && path.endsWith(Constants.CRYPTO_SUFFIX)) {
                uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
            } else {
                uri = ImageDownloader.Scheme.FILE.wrap(path);
            }
//            String filePath = "voidefile://" + path;
            mImageLoader.displayImage(uri, viewHolder.img, mOptions);
            return convertView;











//            ViewHolder viewHolder;
//            String path;
//            if (convertView == null) {
//                convertView = getLayoutInflater().inflate(
//                        R.layout.item_gridview_album, parent, false);
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
//            mImageLoader.displayImage(ImageDownloader.Scheme.FILE.wrap(path),
//                    viewHolder.img, mOptions, mImageSize);
//            return convertView;
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
