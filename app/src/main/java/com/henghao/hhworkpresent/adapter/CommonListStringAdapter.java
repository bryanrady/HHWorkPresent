package com.henghao.hhworkpresent.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.henghao.hhworkpresent.ActivityFragmentSupport;
import com.henghao.hhworkpresent.R;
import com.henghao.hhworkpresent.entity.DeptEntity;

import java.util.List;


public class CommonListStringAdapter extends ArrayAdapter<DeptEntity> {

    private final LayoutInflater inflater;

    private final ActivityFragmentSupport mActivityFragmentSupport;

    public CommonListStringAdapter(ActivityFragmentSupport activityFragment, List<DeptEntity> mList) {
        super(activityFragment, R.layout.common_textview, mList);
        this.mActivityFragmentSupport = activityFragment;
        this.inflater = LayoutInflater.from(activityFragment);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HodlerView mHodlerView = null;
        if (convertView == null) {
            mHodlerView = new HodlerView();
            convertView = this.inflater.inflate(R.layout.common_textview, null);
            mHodlerView.tv_title = (TextView) convertView.findViewById(R.id.tv_common);
            convertView.setTag(mHodlerView);
        }
        else {
            mHodlerView = (HodlerView) convertView.getTag();
        }
        mHodlerView.tv_title.setText(getItem(position).getDept_NAME());
        return convertView;
    }

    private class HodlerView {

        TextView tv_title;
    }
}
