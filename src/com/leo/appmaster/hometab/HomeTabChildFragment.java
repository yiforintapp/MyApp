package com.leo.appmaster.hometab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/21.
 */
public class HomeTabChildFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private List<ListItem> mDatas;
    private ListAdapter mAdapter;
    private View mHeadView;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_user_child;
    }

    @Override
    protected void onInitUI() {
        mListView = (ListView) findViewById(R.id.user_fragment_child_list);
        mDatas = getData();
        mAdapter = new ListAdapter(mActivity, mDatas);
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        mHeadView = layoutInflater.inflate(R.layout.fragment_home_child_listhead, null);
        mListView.addHeaderView(mHeadView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    private List<ListItem> getData() {
        List<ListItem> mList = new ArrayList<ListItem>();
        for (int i = 0; i < 10; i++) {
            ListItem listItem = new ListItem("白银", "4261.00", "0.05%");
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
                convertView = inflater.inflate(R.layout.fragment_home_child_listitem, parent, false);
                holder.name = (TextView) convertView.findViewById(R.id.list_item_name);
                holder.price = (TextView)convertView.findViewById(R.id.list_item_price);
                holder.range = (TextView)convertView.findViewById(R.id.list_item_range);
                convertView.setTag(holder);

            }else {
                holder = (ViewHolder)convertView.getTag();
            }

            ListItem item = items.get(position);
            holder.name.setText(item.mName);
            holder.price.setText(item.mNewPrice);
            holder.range.setText(item.mRange);

            return convertView;
        }
    }

    public class ViewHolder{
        public TextView name;
        public TextView price;
        public TextView range;
    }


    public class ListItem {
        public String mName;
        public String mNewPrice;
        public String mRange;

        public ListItem (String name, String newPrice, String range) {
            mName = name;
            mNewPrice = newPrice;
            mRange = range;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            return;
        }
        Toast.makeText(mActivity, "click:" + position, Toast.LENGTH_SHORT).show();
    }
}
