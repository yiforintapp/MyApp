package com.zlf.appmaster.userTab;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/7/22.
 */
public class UserTabChildFragment extends BaseFragment implements AdapterView.OnItemClickListener, OnRefreshListener2<ListView> {

    private PullToRefreshListView mListView;
    private List<ListItem> mDatas;
    private ListAdapter mAdapter;
    private View mHeadView;
    private View mEmptyView;
    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_user_child;
    }

    @Override
    protected void onInitUI() {
        mListView = (PullToRefreshListView) findViewById(R.id.user_fragment_child_list);
        mHeadView = (View) findViewById(R.id.header_View);
        mHeadView.setVisibility(View.GONE);
        mEmptyView = (View) findViewById(R.id.empty_view);
        mEmptyView.setVisibility(View.GONE);
        mDatas = getData();
        mAdapter = new ListAdapter(mActivity, mDatas);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnRefreshListener(this);
        mListView.setMode(Mode.PULL_FROM_START);
        ILoadingLayout startLabels = mListView
                .getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("下拉刷新...");// 刚下拉时，显示的提示
        startLabels.setRefreshingLabel("正在载入...");// 刷新时
        startLabels.setReleaseLabel("放开刷新...");// 下来达到一定距离时，显示的提示
    }

    private List<ListItem> getData() {
        List<ListItem> mList = new ArrayList<ListItem>();
        for (int i = 0; i < 10; i++) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String t=format.format(new Date());
            ListItem listItem = new ListItem("贵金属早评：再来一波！白银反击尚未终结！", t);
            mList.add(listItem);
        }

        return mList;
    }

    /** 重新获取数据并刷新 */
    private void notifyAdapter() {
        mDatas.clear();
        mDatas = getData();
        if(mAdapter != null){
            mAdapter.setItems(mDatas);
            mAdapter.notifyDataSetChanged();
        }
    }

    class ListAdapter extends BaseAdapter {

        List<ListItem> items;
        LayoutInflater inflater;

        public ListAdapter(Context ctx, List<ListItem> items) {
            super();
            this.items = items;
            inflater = LayoutInflater.from(ctx);
        }

        public void setItems(List<ListItem> list) {
            items = list;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                holder=new ViewHolder();
                convertView = inflater.inflate(R.layout.fragment_user_child_listitem, parent, false);
                holder.content = (TextView) convertView.findViewById(R.id.list_item_content);
                holder.time = (TextView)convertView.findViewById(R.id.list_item_time);
                convertView.setTag(holder);

            }else {
                holder = (ViewHolder)convertView.getTag();
            }

            ListItem item = items.get(position);
            holder.content.setText(item.mContent);
            holder.time.setText(item.mTime);

            return convertView;
        }
    }

    public class ViewHolder{
        public TextView content;
        public TextView time;
    }


    public class ListItem {
        public String mContent;
        public String mTime;

        public ListItem (String content, String time) {
            mContent = content;
            mTime = time;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(mActivity, "click:" + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        new FinishRefresh().execute();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

    }

    private class FinishRefresh extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            return "刷新完毕";
        }

        @Override
        protected void onPostExecute(String result){
            Toast.makeText(mActivity, result, Toast.LENGTH_SHORT).show();
            mListView.onRefreshComplete();
        }
    }
}
