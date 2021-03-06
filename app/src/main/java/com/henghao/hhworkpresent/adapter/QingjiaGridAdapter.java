package com.henghao.hhworkpresent.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.henghao.hhworkpresent.ActivityFragmentSupport;
import com.henghao.hhworkpresent.R;
import com.henghao.hhworkpresent.WorkflowUrl;
import com.henghao.hhworkpresent.activity.WebViewActivity;
import com.henghao.hhworkpresent.entity.AppGridEntity;
import com.henghao.hhworkpresent.utils.SqliteDBUtils;

import java.util.List;

/**
 * Created by bryanrady on 2017/4/5.
 * 请假、申请、补签适配器
 */

public class QingjiaGridAdapter extends ArrayAdapter<AppGridEntity> {

    private final LayoutInflater inflater;

    private final ActivityFragmentSupport mActivityFragmentSupport;

    public QingjiaGridAdapter(ActivityFragmentSupport activityFragment, List<AppGridEntity> mList){
        super(activityFragment, R.layout.item_work_fragment_adapter, mList);
        this.mActivityFragmentSupport = activityFragment;
        this.inflater = LayoutInflater.from(activityFragment);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HodlerView mHodlerView = null;
        if (convertView == null) {
            mHodlerView = new HodlerView();
            convertView = this.inflater.inflate(R.layout.item_gridview_textimage, null);
            mHodlerView.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            mHodlerView.image_title = (ImageView) convertView.findViewById(R.id.image_title);
            convertView.setTag(mHodlerView);
        } else {
            mHodlerView = (HodlerView) convertView.getTag();
        }
        mHodlerView.image_title.setImageResource(getItem(position).getImageId());
        mHodlerView.tv_title.setVisibility(View.VISIBLE);
        mHodlerView.tv_title.setText(getItem(position).getName());
        viewOnClick(mHodlerView, convertView, position);
        return convertView;
    }

    /**
     * 点击事件
     *
     * @param mHodlerView
     * @param convertView
     * @param position
     */
    private void viewOnClick(HodlerView mHodlerView, View convertView, final int position) {
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SqliteDBUtils sqliteDBUtils = new SqliteDBUtils(mActivityFragmentSupport);
                switch (position) {
                    case 0:
                        //请假
                        WebViewActivity.startToWebActivity(mActivityFragmentSupport,"请假", WorkflowUrl.WORKFLOW_VIEW_URL + sqliteDBUtils.getUsername() + WorkflowUrl.QINGJIA_FLOWID);
                        break;
                    case 1:
                        //外出
                        WebViewActivity.startToWebActivity(mActivityFragmentSupport,"出差", WorkflowUrl.WORKFLOW_VIEW_URL + sqliteDBUtils.getUsername() + WorkflowUrl.CHUCHAI_FLOWID);
                        break;
                    case 2:
                        //补签
                        WebViewActivity.startToWebActivity(mActivityFragmentSupport,"补签", WorkflowUrl.WORKFLOW_VIEW_URL + sqliteDBUtils.getUsername()+ WorkflowUrl.BUQIAN_FLOWID);
                        break;
                }
            }
        });
    }

    class HodlerView {
        TextView tv_title;
        ImageView image_title;
    }
}
