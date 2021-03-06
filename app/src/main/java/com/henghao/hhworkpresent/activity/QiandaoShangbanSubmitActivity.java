package com.henghao.hhworkpresent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.benefit.buy.library.http.query.callback.AjaxStatus;
import com.henghao.hhworkpresent.ActivityFragmentSupport;
import com.henghao.hhworkpresent.R;
import com.henghao.hhworkpresent.entity.BaseEntity;
import com.henghao.hhworkpresent.protocol.QianDaoProtocol;
import com.henghao.hhworkpresent.utils.SqliteDBUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import org.json.JSONException;

/**
 * Created by bryanrady on 2017/3/31.
 * 签到提交页面
 */

public class QiandaoShangbanSubmitActivity extends ActivityFragmentSupport {

    /**
     * 签到时间
     */
    @ViewInject(R.id.tv_time_qiandaosubmit)
    private TextView tv_time_qiandaosubmit;
    /**
     * 签到地点
     */
    @ViewInject(R.id.tv_address_qiandaosubmit)
    private TextView tv_address_qiandaosubmit;
    /**
     * 签到备注
     */
    @ViewInject(R.id.et_note_qiandao)
    private EditText et_note_qiandao;

    /**
     * 当前企业
     */
    @ViewInject(R.id.tv_company_qiandaosubmit)
    private TextView tv_company_qiandaosubmit;

    /**
     * 提交
     */
    @ViewInject(R.id.btn_submit_qiandaosubmit)
    private Button btn_submit_qiandaosubmit;
    private String address;
    private double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivityFragmentView.viewMain(R.layout.activity_qiandao_submit);
        this.mActivityFragmentView.viewEmpty(R.layout.activity_empty);
        this.mActivityFragmentView.viewEmptyGone();
        this.mActivityFragmentView.viewLoading(View.GONE);
        this.mActivityFragmentView.clipToPadding(true);
        setContentView(this.mActivityFragmentView);
        com.lidroid.xutils.ViewUtils.inject(this);
        initWidget();
        initData();
    }

    @Override
    public void initWidget() {
        initWithBar();
        mLeftTextView.setVisibility(View.VISIBLE);
        mLeftTextView.setText("返回");
        initWithCenterBar();
        this.mCenterTextView.setVisibility(View.VISIBLE);
        this.mCenterTextView.setText("签到提交");
    }

    @Override
    public void initData() {
        Intent intent = getIntent();
        String time = intent.getStringExtra("time");
        address = intent.getStringExtra("address");
        latitude = intent.getDoubleExtra("latitude",0);
        longitude = intent.getDoubleExtra("longitude",0);
        tv_time_qiandaosubmit.setText(time);
        if("null".equals(address)||address==null){
            tv_address_qiandaosubmit.setText("暂时没有定位信息");
        } else {
            tv_address_qiandaosubmit.setText(address);
        }
        tv_company_qiandaosubmit.setText("贵阳市安全生产监督管理局");

    }

    @OnClick({R.id.btn_submit_qiandaosubmit})
    private void viewClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit_qiandaosubmit:
                // 提交
                SqliteDBUtils sqliteDBUtils = new SqliteDBUtils(this);
                QianDaoProtocol mQianDaoProtocol = new QianDaoProtocol(this);
                mQianDaoProtocol.addResponseListener(this);
                mQianDaoProtocol.qiandao(sqliteDBUtils.getLoginUid(), longitude+"", latitude+"");
                mActivityFragmentView.viewLoading(View.VISIBLE);
                break;
        }
    }

    @Override
    public void OnMessageResponse(String url, Object jo, AjaxStatus status) throws JSONException {
        super.OnMessageResponse(url, jo, status);
        if (jo instanceof BaseEntity) {
            BaseEntity base = (BaseEntity) jo;
            msg(base.getMsg());
            setResult(RESULT_OK);
            finish();
            return;
        }

    }

}
