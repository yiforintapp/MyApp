
package com.leo.appmaster.intruderprotection;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.IntruderPhotoInfo;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.ImageLoader;

public class IntruderGalleryActivity extends BaseActivity {
    private ViewPager mVPPhotos;
    private IntrudeSecurityManager mISManager;
    private ArrayList<IntruderPhotoInfo> mSrcInfos;
    private ArrayList<IntruderPhotoInfo> mInfosSorted;
    private LayoutInflater mInflater;
    private int mCurrentPosition;
    private CommonToolbar  mCtb;
//    private TextView mTvPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallary_intruder);
        Intent intent = getIntent();
        mCurrentPosition = intent.getIntExtra("current_position", 0);
        init();
    }

    /**
     * 初始化，这里在onCreate时做一次初始化，以后不再重新查询数据库
     */
    private void init() {
        mCtb = (CommonToolbar) findViewById(R.id.ctb_intruder_gallery);
        mCtb.setOptionImageResource(R.drawable.toolbar_delete);
        mCtb.setToolbarTitle(R.string.intruder_gallery_title);
        mCtb.setOptionMenuVisible(true);
//        mTvPosition = (TextView) findViewById(R.id.tv_position);
        mVPPhotos = (ViewPager) findViewById(R.id.vp_photos);
        mISManager = (IntrudeSecurityManager) MgrContext
                .getManager(MgrContext.MGR_INTRUDE_SECURITY);
        mInflater = LayoutInflater.from(this);
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mSrcInfos = mISManager.getPhotoInfoList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onQueryFinished();
                    }
                });
            }
        });
    }

    /**
     * 对记录做排序，按照时间顺序
     */
    private void sortInfos() {
        mInfosSorted = mISManager.sortInfosByTimeStamp(mSrcInfos);
    }

    class MyPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            if (mInfosSorted != null)
                return mInfosSorted.size();
            return 0;
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO Auto-generated method stub
            return POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mInflater.inflate(R.layout.item_intruder_gallery, null);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int visibility = mCtb.getVisibility();
                    if (visibility == View.VISIBLE) {
                        mCtb.setVisibility(View.GONE);
                    } else {
                        mCtb.setVisibility(View.VISIBLE);
                    }
                }
            });
            ImageView iv = (ImageView) view.findViewById(R.id.iv_IGitem_main);
            ImageLoader.getInstance().displayImage(
                    "file:///" + mInfosSorted.get(position).getFilePath(), iv);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private void onQueryFinished() {
        sortInfos();
        if (mInfosSorted == null || mInfosSorted.isEmpty())
            return;
        final MyPagerAdapter adapter = new MyPagerAdapter();
        mVPPhotos.setAdapter(adapter);
        mVPPhotos.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
//                updatePositionTips();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        mVPPhotos.setCurrentItem(mCurrentPosition);
        // 为相册显示当前位置
//        updatePositionTips();
        // 删除当前位置的照片（数据库和UI）//TODO
        mCtb.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
                final int currentItem = mVPPhotos.getCurrentItem();
                LEOAlarmDialog dialog = new LEOAlarmDialog(IntruderGalleryActivity.this);
                String askforsure = getString(R.string.sure_delete_one);
                dialog.setContent(askforsure);
                dialog.setRightBtnStr(IntruderGalleryActivity.this.getString(R.string.makesure));
                dialog.setLeftBtnStr(IntruderGalleryActivity.this.getString(R.string.cancel));
                dialog.setRightBtnListener(new DialogInterface.OnClickListener()  {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String filePath = mInfosSorted.get(currentItem).getFilePath();
                        mISManager.deletePhotoInfo(filePath);
                        ThreadManager.executeOnFileThread(new Runnable() {
                            @Override
                            public void run() {
                                    FileOperationUtil.deleteFile(filePath);
                                    FileOperationUtil.deleteFileMediaEntry(filePath, IntruderGalleryActivity.this);
                                    LeoLog.i("poha", "delete pic");
                            }
                        });
                        mInfosSorted.remove(currentItem);
                        if(mInfosSorted.size() != 0){
                            adapter.notifyDataSetChanged();
                        }else{
                            IntruderGalleryActivity.this.finish();
                        }
                        dialog.dismiss();
                    }
                });
                dialog.show();
                LeoLog.i("poha_intruder_gallery", "currentItem = "+currentItem);
            }
        });
    }
}
