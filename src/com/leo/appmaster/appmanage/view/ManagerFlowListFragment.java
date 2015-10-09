
package com.leo.appmaster.appmanage.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.model.TrafficsInfo;
import com.leo.appmaster.ui.TrafficInfoPackage;
import com.leo.appmaster.utils.ManagerFlowUtils;

public class ManagerFlowListFragment extends BaseFragment {

    private ListView lv_flow_list;
    private View content_listview, content_show_nothing;
    private List<TrafficsInfo> appTrafficInfo;
    private List<TrafficsInfo> readytosort;
    private FlowListAsyncTask flowAsyncTask;
    private ProgressBar pb_loading;

    @Override
    protected int layoutResourceId() {
        return R.layout.activity_appmanager_flowlist;
    }

    @Override
    protected void onInitUI() {
        init();
        flowAsyncTask = new FlowListAsyncTask();
        flowAsyncTask.execute("");
    }

    private void fillData() {

        readytosort = new TrafficInfoPackage(mActivity).getRunningProcess(true);
        for (int i = 0; i < readytosort.size(); i++) {
            if (readytosort.get(i).getApp_all_traffic().equals("0KB")
                    || (readytosort.get(i).getRx().equals("0KB") && readytosort.get(i).getTx()
                            .equals("0KB"))) {
                readytosort.remove(i);
            }
        }
        appTrafficInfo = ManagerFlowUtils.makeSort(readytosort);

    }

    private void init() {
        lv_flow_list = (ListView) findViewById(R.id.lv_flow_list);
        content_listview = findViewById(R.id.content_listview);
        content_show_nothing = findViewById(R.id.content_show_nothing);
        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);

        appTrafficInfo = new ArrayList<TrafficsInfo>();
        readytosort = new ArrayList<TrafficsInfo>();
    }

    class FlowListAsyncTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object... params) {
            fillData();
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (appTrafficInfo.size() > 0) {
                content_listview.setVisibility(View.VISIBLE);
            } else {
                content_show_nothing.setVisibility(View.VISIBLE);
            }
            pb_loading.setVisibility(View.GONE);
            MyFlowAdapter myAdater = new MyFlowAdapter(mActivity, appTrafficInfo);
            lv_flow_list.setAdapter(myAdater);
        }
    }

    public class MyFlowAdapter extends BaseAdapter {
        private Context mContext;
        private List<TrafficsInfo> mList;

        public MyFlowAdapter(Context context, List<TrafficsInfo> appTrafficInfo) {
            this.mContext = context;
            this.mList = appTrafficInfo;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrafficsInfo traInfo = mList.get(position);
            ViewHolderTraffic holder = null;
            View view;
            if (convertView != null && convertView instanceof RelativeLayout) {
                view = convertView;
                holder = (ViewHolderTraffic) view.getTag();
            } else {
                view = View.inflate(mContext, R.layout.manager_flow_list_item, null);
                holder = new ViewHolderTraffic();

                holder.imgage = (ImageView) view.findViewById(R.id.list_image);
                holder.text = (TextView) view.findViewById(R.id.list_text);
                holder.gprs_rev = (TextView) view.findViewById(R.id.gprs_down);
                holder.gprs_send = (TextView) view.findViewById(R.id.gprs_upload);
                holder.gprs_total = (TextView) view.findViewById(R.id.gprs_total);

                view.setTag(holder);
            }

            holder.imgage.setImageDrawable(traInfo.getIcon());
            holder.text.setText(traInfo.getAppName());
            holder.gprs_rev.setText(traInfo.getRx());
            holder.gprs_send.setText(traInfo.getTx());
            holder.gprs_total.setText(traInfo.getApp_all_traffic());

            return view;
        }
    }

    class ViewHolderTraffic {
        TextView text;
        ImageView imgage;
        TextView gprs_send, gprs_rev, gprs_total;
    }
}
