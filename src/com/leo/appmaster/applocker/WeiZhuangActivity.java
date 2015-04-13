
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.imagehide.PhotoAibum;
import com.leo.appmaster.model.WeiZhuangInfo;
import com.leo.appmaster.ui.CommonTitleBar;

public class WeiZhuangActivity extends Activity implements OnItemClickListener {
    private Drawable[] mIcon = new Drawable[1];
    private String[] mName;
    private List<WeiZhuangInfo> mList;
    private WeiZhuangAdapt mAdapt;
    private CommonTitleBar mTtileBar;
    private GridView mGridView;
    private Resources mThemeRes;
    private AppMasterPreference sp_weizhuang;
    private int selected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weizhuang_girdview);
        init();
        fillData();
    }

    private void init() {
        mTtileBar = (CommonTitleBar) findViewById(R.id.weizhuang_title_bar);
        mTtileBar.setTitle(R.string.title_bar_weizhuang);
        mTtileBar.openBackView();

        mGridView = (GridView) findViewById(R.id.gv_weizhuang);

        sp_weizhuang = AppMasterPreference.getInstance(this);
        mThemeRes = this.getResources();
    }

    private void fillData() {

        // Name
        mName = getResources().getStringArray(R.array.weizhuang_type_num);
        // icon
        mIcon[0] = mThemeRes.getDrawable(R.drawable.ic_launcher);

        // list
        mList = new ArrayList<WeiZhuangInfo>();
        int size = mName.length;
        for (int i = 0; i < size; i++) {
            WeiZhuangInfo info = new WeiZhuangInfo();
            info.setAppName(mName[i]);
            info.setIcon(mIcon[0]);
            mList.add(info);
        }

        // set adapter
        mAdapt = new WeiZhuangAdapt(this, mList);
        mGridView.setAdapter(mAdapt);
        mGridView.setOnItemClickListener(this);

    }

    /**
     * adapter
     * 
     * @author hqili
     */
    class WeiZhuangAdapt extends BaseAdapter {
        Context context;
        List<WeiZhuangInfo> list;

        public WeiZhuangAdapt(Context context, List<WeiZhuangInfo> mList) {
            this.context = context;
            this.list = mList;
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
            WeizhuangHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.item_weizhuang_gridview, null);
                viewHolder = new WeizhuangHolder();
                viewHolder.item_all_content = convertView.findViewById(R.id.item_all_content);
                viewHolder.iv_icon = (ImageView) convertView
                        .findViewById(R.id.item_icon);
                viewHolder.iv_selected = (ImageView) convertView
                        .findViewById(R.id.item_selected);
                viewHolder.tv_name = (TextView) convertView
                        .findViewById(R.id.item_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (WeizhuangHolder) convertView.getTag();
            }
            
            selected = sp_weizhuang.getWeiZhuang();
            // who selected
            if (position == selected) {
                viewHolder.iv_selected.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_selected.setVisibility(View.GONE);
            }

            WeiZhuangInfo mInfo = list.get(position);
            viewHolder.iv_icon.setImageDrawable(mInfo.getIcon());
            viewHolder.tv_name.setText(mInfo.getAppName());

            return convertView;
        }

    }

    private static class WeizhuangHolder {
        private View item_all_content;
        private ImageView iv_icon, iv_selected;
        private TextView tv_name;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                // 无

                Toast.makeText(this, "无", 0).show();
                sp_weizhuang.setWeiZhuang(0);
                mAdapt.notifyDataSetChanged();
                break;
            case 1:
                // 应用错误

                Toast.makeText(this, "应用错误", 0).show();
                sp_weizhuang.setWeiZhuang(1);
                mAdapt.notifyDataSetChanged();
                break;
            case 2:
                // 未知来电

                Toast.makeText(this, "未知来电", 0).show();
                sp_weizhuang.setWeiZhuang(2);
                mAdapt.notifyDataSetChanged();
                break;
            case 3:
                // 指纹解锁

                Toast.makeText(this, "指纹解锁", 0).show();
                sp_weizhuang.setWeiZhuang(3);
                mAdapt.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
}
