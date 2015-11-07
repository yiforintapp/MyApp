package com.leo.appmaster.imagehide;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.videohide.VideoHideDialog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;

/**
 * @author linxiongzhou
 */
public class ImageGalleryActivity extends BaseActivity {

    private List<PhotoAibum> mAlbumList = null;
    private GridView mGridView;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private CommonToolbar mTtileBar;
    private RelativeLayout mNoPictureHint;
    private AlbumAdapt mAlbumAdapt = new AlbumAdapt(this);

//    private VideoHideDialog mImageDialog;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initImageLoder();
        setContentView(R.layout.activity_image_gallery);
//        mImageDialog = new VideoHideDialog(this);
//        Window window = mImageDialog.getWindow();
//        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        layoutParams.alpha = 0.5f;
//        layoutParams.dimAmount = 0.0f;
//        window.setAttributes(layoutParams);
        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_image_gallery);
        mTtileBar.setOptionMenuVisible(false);
//        mTtileBar.openBackView();
        mGridView = (GridView) findViewById(R.id.image_gallery_folder);
        mGridView.setAdapter(mAlbumAdapt);
        mNoPictureHint = (RelativeLayout) findViewById(R.id.no_picture);
        loadingBar = (ProgressBar) findViewById(R.id.pb_loading_hide_pic);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelDialog();
        if (mImageLoader != null) {
            mImageLoader.stop();
            mImageLoader.clearMemoryCache();
        }
    }

    private void cancelDialog() {
//        if (mImageDialog != null) {
//            mImageDialog.dismiss();
//            mImageDialog = null;
//        }
    }

    @Override
    public void finish() {
        super.finish();
        cancelDialog();
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
        LoaderImageFolderTask task = new LoaderImageFolderTask(this);
        task.execute();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mGridView != null && mGridView.getCount() > 0) {
        }
        super.onPause();
    }

    private void initImageLoder() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.photo_bg_loding)
                .showImageForEmptyUri(R.drawable.photo_bg_loding)
                .showImageOnFail(R.drawable.photo_bg_loding)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        mImageLoader = ImageLoader.getInstance();
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
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder viewHolder;
            String path;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.item_gridview_album, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.img = (ImageView) convertView
                        .findViewById(R.id.img_item_album);
                viewHolder.txt = (TextView) convertView
                        .findViewById(R.id.txt_item_album);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            path = list.get(position).getBitList().get(0).getPath();
            viewHolder.txt.setText(list.get(position).getName() + "("
                    + list.get(position).getCount() + ")");
            mImageLoader.displayImage("file://" + path, viewHolder.img, mOptions);
            return convertView;
        }
    }

    private static class ViewHolder {
        private ImageView img;
        private TextView txt;
    }

    private class LoaderImageFolderTask extends AsyncTask<Void, Integer, Integer> {
        private Context context;

        LoaderImageFolderTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
//            if (mImageDialog != null) {
//                mImageDialog.show();
//            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
//            mAlbumList = FileOperationUtil.getPhotoAlbum(context);
            mAlbumList = ((PrivacyDataManager) MgrContext.
                    getManager(MgrContext.MGR_PRIVACY_DATA)).getAllPicFile();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
//            if (mImageDialog != null) {
//                mImageDialog.dismiss();
//            }
            if (mAlbumList != null) {
                if (mAlbumList.size() > 0) {
                    mNoPictureHint.setVisibility(View.GONE);
                    loadingBar.setVisibility(View.GONE);
                    mGridView.setVisibility(View.VISIBLE);
                    mAlbumAdapt.setDataList(mAlbumList);
                    mAlbumAdapt.notifyDataSetChanged();
                    mGridView.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int position, long arg3) {
                            if (position < mAlbumList.size()) {
                                Intent intent = new Intent(ImageGalleryActivity.this,
                                        ImageGridActivity.class);
                                Bundle bundle = new Bundle();
                                PhotoAibum photoAibum = mAlbumList.get(position);
                                if (photoAibum.getBitList().size() < 1000) {
                                    bundle.putSerializable("data", mAlbumList.get(position));
                                }
                                intent.putExtra("pos", position);
                                intent.putExtra("mode", ImageGridActivity.SELECT_HIDE_MODE);
                                intent.putExtras(bundle);
                                startActivityForResult(intent, 1001);
                            }
                        }
                    });
                } else {
                    mNoPictureHint.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.GONE);
                    mGridView.setVisibility(View.GONE);
                }

            }
        }
    }

}
