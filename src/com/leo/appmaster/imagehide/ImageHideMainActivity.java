
package com.leo.appmaster.imagehide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.MediaColumns;
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

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;

public class ImageHideMainActivity extends BaseActivity implements OnClickListener {

    private List<PhotoAibum> mAlbumList = null;
    private GridView mGridView;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private CommonTitleBar mTtileBar;
    private Button mAddButton;
    private RelativeLayout mNoHidePictureHint;
    private LoaderHideImageFolderTask mLoadTask;

    private HideAlbumAdapt mHideAlbumAdapt = new HideAlbumAdapt(this);

    String[] STORE_HIDEIMAGES = new String[] {
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns._ID, //
    };

    public static final int REQUEST_CODE_LOCK = 1000;
    public static final int REQUEST_CODE_OPTION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initImageLoder();
        setContentView(R.layout.activity_image_hide);
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle(R.string.app_image_hide);
        mTtileBar.openBackView();
        // mTtileBar.setOptionImage(R.drawable.selector_applock_setting);
        // mTtileBar.setOptionImageVisibility(View.VISIBLE);
        // mTtileBar.setOptionText(getString(R.string.setting));
        // mTtileBar.setOptionTextVisibility(View.VISIBLE);
        // mTtileBar.setOptionListener(this);
        mGridView = (GridView) findViewById(R.id.Image_hide_folder);
        mGridView.setAdapter(mHideAlbumAdapt);
        mAddButton = (Button) findViewById(R.id.add_hide_image);
        mAddButton.setOnClickListener(this);
        mNoHidePictureHint = (RelativeLayout) findViewById(R.id.no_hide);
    }

    private void initImageLoder() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.photo_bg_loding)
                .showImageForEmptyUri(R.drawable.photo_bg_loding)
                .showImageOnFail(R.drawable.photo_bg_loding)
                .cacheInMemory(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
        }
        if (mImageLoader != null) {
            mImageLoader.stop();
            mImageLoader.clearMemoryCache();
        }
    }

    @Override
    protected void onResume() {
        if (mLoadTask != null) {
            mLoadTask.cancel(false);
        }
        mLoadTask = new LoaderHideImageFolderTask(this);
        mLoadTask.execute();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
    }

    @Override
    public void finish() {
        super.finish();
        if (mLoadTask != null) {
            mLoadTask.cancel(false);
        }
        if (mImageLoader != null) {
            mImageLoader.stop();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.add_hide_image:
                intent = new Intent(this, ImageGalleryActivity.class);
                startActivityForResult(intent, REQUEST_CODE_OPTION);
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

    /*
     * get image folder list
     */
    private List<PhotoAibum> getHidePhotoAlbum(Context context) {
        List<PhotoAibum> aibumList = new ArrayList<PhotoAibum>();
        Uri uri = Files.getContentUri("external");
        String selection = MediaColumns.DATA + " LIKE '%.leotmp'" + " or " + MediaColumns.DATA
                + " LIKE '%.leotmi'";

        Cursor cursor = getContentResolver().query(uri, STORE_HIDEIMAGES, selection, null,
                MediaColumns.DATE_ADDED + " desc");
        if (cursor != null) {
            try {
                Map<String, PhotoAibum> countMap = new HashMap<String, PhotoAibum>();
                PhotoAibum pa = null;
                while (cursor.moveToNext()) {
                    String path = cursor.getString(1);
                    String dirName = FileOperationUtil.getDirNameFromFilepath(path);
                    String dirPath = FileOperationUtil.getDirPathFromFilepath(path);
                    if (!countMap.containsKey(dirPath)) {
                        pa = new PhotoAibum();
                        pa.setName(dirName);
                        pa.setCount("1");
                        pa.setDirPath(dirPath);
                        pa.getBitList().add(new PhotoItem(path));
                        countMap.put(dirPath, pa);
                    } else {
                        pa = countMap.get(dirPath);
                        pa.setCount(String.valueOf(Integer.parseInt(pa.getCount()) + 1));
                        pa.getBitList().add(new PhotoItem(path));
                    }
                }
                Iterable<String> it = countMap.keySet();
                for (String key : it) {
                    aibumList.add(countMap.get(key));
                }
                Collections.sort(aibumList, FileOperationUtil.mFolderCamparator);
            } catch (Exception e) {

            } finally {
                cursor.close();
            }
        }

        return aibumList;
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

    private class LoaderHideImageFolderTask extends AsyncTask<Void, Integer, Integer> {
        private Context context;

        LoaderHideImageFolderTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... params) {
            mAlbumList = getHidePhotoAlbum(context);
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (mAlbumList != null) {
                if (mAlbumList.size() > 0) {
                    mNoHidePictureHint.setVisibility(View.GONE);
                    mGridView.setVisibility(View.VISIBLE);
                } else {
                    mNoHidePictureHint.setVisibility(View.VISIBLE);
                    mGridView.setVisibility(View.GONE);
                }
                mHideAlbumAdapt.setDataList(mAlbumList);
                mHideAlbumAdapt.notifyDataSetChanged();

                mGridView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                            int position, long arg3) {
                        Intent intent = new Intent(ImageHideMainActivity.this,
                                ImageGridActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("data", mAlbumList.get(position));
                        intent.putExtra("mode", ImageGridActivity.CANCEL_HIDE_MODE);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, REQUEST_CODE_OPTION);
                    }
                });
            }
        }
    }

}
