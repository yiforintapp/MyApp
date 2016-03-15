
package com.leo.appmaster.imagehide;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.mgr.impl.PrivacyDataManagerImpl;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.QuickHelperUtils;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FadeInBitmapDisplayer;
import com.leo.imageloader.core.ImageDownloader;

import java.util.ArrayList;
import java.util.List;

public class ImageHideMainActivity extends BaseActivity implements OnItemClickListener {

    public final static int INIT_UI_DONE = 20;
    public final static int LOAD_DATA_DONE = 21;
    private static final String TAG = "ImageHideMainActivity";
    private List<PhotoAibum> mAlbumList = null;
    private GridView mGridView;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private CommonToolbar mTtileBar;
    private RelativeLayout mNoHidePictureHint;
    private RippleView mRvAdd;
    private ProgressBar loadingBar;
    private PreferenceTable mPt;
    private final int ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT = 3;
    private LEOAlarmDialog mDialogAskCreateShotcut;

    private HideAlbumAdapt mHideAlbumAdapt = new HideAlbumAdapt(this);

    public static final int REQUEST_CODE_OPTION = 1001;

    private Toast mToast;

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

    public void onBackPressed() {
        LeoLog.d(TAG, "mPt.getBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC, false) = " + mPt.getBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC, false));
        LeoLog.d(TAG, "mPt.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, 0) = " + mPt.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, 0));
        if (!mPt.getBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC, false) && mPt.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, 0) >= ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT) {
            mPt.putBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_HIDE_PIC, true);
            if (mDialogAskCreateShotcut == null) {
                mDialogAskCreateShotcut = new LEOAlarmDialog(this);
            }
            mDialogAskCreateShotcut.setTitleVisiable(false);
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
            super.onBackPressed();
        }
    };
    
    private void loadDone() {
        if (mAlbumList != null) {
            if (mAlbumList.size() > 0) {
                mNoHidePictureHint.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
            } else {
                mNoHidePictureHint.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
                mGridView.setVisibility(View.GONE);
            }
            mHideAlbumAdapt.setDataList(mAlbumList);
            mHideAlbumAdapt.notifyDataSetChanged();
        }
    }

    private void asyncLoad() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mAlbumList = ((PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA)).
                        getHidePicAlbum(PrivacyDataManagerImpl.CHECK_APART);
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(LOAD_DATA_DONE);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_hide);
        mPt = PreferenceTable.getInstance();
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
    }

    private void initUI() {
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
            }
        });
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
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
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
        mHandler.sendEmptyMessage(INIT_UI_DONE);
        super.onResume();
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
    public void finish() {
        super.finish();

        if (mImageLoader != null) {
            mImageLoader.stop();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent intent = new Intent(ImageHideMainActivity.this,
                ImageGridActivity.class);
        Bundle bundle = new Bundle();

        PhotoAibum photoAibum = mAlbumList.get(position);
        int size = photoAibum.getBitList().size();
        if (size < 800) {
            bundle.putSerializable("data", photoAibum);
        }
        intent.putExtra("pos", position);
        intent.putExtra("mode", ImageGridActivity.CANCEL_HIDE_MODE);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_OPTION);
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
            String uri = ImageDownloader.Scheme.CRYPTO.wrap(path);
            mImageLoader.displayImage(uri, viewHolder.img, mOptions);
            return convertView;
        }
    }

    private static class ViewHolder {
        private ImageView img;
        private TextView txt;
    }

}
