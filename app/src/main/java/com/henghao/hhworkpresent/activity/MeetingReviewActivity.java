package com.henghao.hhworkpresent.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.benefit.buy.library.utils.ToastUtils;
import com.benefit.buy.library.utils.tools.ToolsSecret;
import com.google.gson.Gson;
import com.henghao.hhworkpresent.ActivityFragmentSupport;
import com.henghao.hhworkpresent.ProtocolUrl;
import com.henghao.hhworkpresent.R;
import com.henghao.hhworkpresent.entity.JPushToUser;
import com.henghao.hhworkpresent.entity.MeetingEntity;
import com.henghao.hhworkpresent.utils.SqliteDBUtils;
import com.henghao.hhworkpresent.views.CustomDialog;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


/**
 * 会议审批界面
 * Created by ASUS on 2017/9/26.
 */

public class MeetingReviewActivity extends ActivityFragmentSupport {

    @ViewInject(R.id.tv_meeting_theme)
    private TextView tv_meeting_theme;

    @ViewInject(R.id.tv_meeting_faqiren)
    private TextView tv_meeting_faqiren;

    @ViewInject(R.id.tv_meeting_place)
    private TextView tv_meeting_place;

    @ViewInject(R.id.tv_meeting_start_time)
    private TextView tv_meeting_start_time;

    @ViewInject(R.id.tv_meeting_duration)
    private TextView tv_meeting_duration;

    @ViewInject(R.id.tv_join_meeting_people_num)
    private TextView tv_join_meeting_people_num;

    @ViewInject(R.id.tv_join_meeting_people)
    private TextView tv_join_meeting_people;

    @ViewInject(R.id.tv_meeting_agree)
    private TextView tv_meeting_agree;

    @ViewInject(R.id.tv_meeting_reject)
    private TextView tv_meeting_reject;

    private long msg_id;
    private int mid;
    private Handler mHandler = new Handler(){};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mActivityFragmentView.viewMain(R.layout.activity_meeting_review);
        this.mActivityFragmentView.viewEmpty(R.layout.activity_empty);
        this.mActivityFragmentView.viewEmptyGone();
        this.mActivityFragmentView.viewLoading(View.GONE);
        this.mActivityFragmentView.clipToPadding(true);
        ViewUtils.inject(this, this.mActivityFragmentView);
        setContentView(this.mActivityFragmentView);
        initWidget();
        initData();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        initWithBar();
        mLeftTextView.setVisibility(View.VISIBLE);
        initWithCenterBar();
        mCenterTextView.setText(R.string.meetingShenpi);
        mCenterTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void initData() {
        super.initData();
        Intent intent = getIntent();
        msg_id = intent.getLongExtra("msg_id",0);
        httpRequestMeetingContent();
    }

    @OnClick({R.id.tv_meeting_agree,R.id.tv_meeting_reject})
    private void viewOnClick(View v) {
        switch (v.getId()){
            case R.id.tv_meeting_agree:
                httpRequestAgreeOrCancel(1,getString(R.string.tv_null));
                break;
            case R.id.tv_meeting_reject:
                showNoPassDialog();
                break;
        }
    }

    /**
     * 展示不同意对话框
     */
    private void showNoPassDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.processingMode);
        builder.setMessage(R.string.meetingIsReject);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 执行点击确定按钮的业务逻辑
                dialog.dismiss();
                showNoPassReasonDialog();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 显示不同意理由对话框
     */
    private void showNoPassReasonDialog(){
        final View customView = View.inflate(this,R.layout.layout_no_pass_dialog,null);
        final EditText et_no_pass_reason = (EditText) customView.findViewById(R.id.et_no_pass_reason);
        CustomDialog.Builder dialog=new CustomDialog.Builder(this);
        dialog.setTitle(R.string.edit_reject_reason)
                .setContentView(customView)//设置自定义customView
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(et_no_pass_reason.getText().toString().equals("")){
                            Toast.makeText(MeetingReviewActivity.this,R.string.mustbe_edit_reject_reason,Toast.LENGTH_SHORT).show();
                            return;
                        }
                        httpRequestAgreeOrCancel(2,et_no_pass_reason.getText().toString());
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }

    /**
     * 1代表同意 2代表不同意
     * 点击同意或取消会走的接口并且上传理由
     * @param whetherPass
     */
    private void httpRequestAgreeOrCancel(final int whetherPass,String noPassReason){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.type(MultipartBuilder.FORM)
                .addFormDataPart("mid", String.valueOf(mid))
                .addFormDataPart("whetherPass",String.valueOf(whetherPass))
                .addFormDataPart("noPassReason",noPassReason);
        RequestBody requestBody = multipartBuilder.build();
        String request_url = ProtocolUrl.ROOT_URL + ProtocolUrl.APP_ONCLICK_AGREE_OR_REJECT;
        Request request = builder.post(requestBody).url(request_url).build();
        Call call = okHttpClient.newCall(request);
        mActivityFragmentView.viewLoading(View.VISIBLE);
        tv_meeting_agree.setEnabled(false);
        tv_meeting_reject.setEnabled(false);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mActivityFragmentView.viewLoading(View.GONE);
                        ToastUtils.show(getContext(),R.string.app_network_failure);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mActivityFragmentView.viewLoading(View.GONE);
                        msg(getString(R.string.app_upload_succeed));
                        Intent intent = new Intent();
                        intent.setClass(MeetingReviewActivity.this,MeetingShenpiResultsActivity.class);
                        intent.putExtra("msg_id",msg_id);
                        startActivity(intent);
                        finish();
                    }
                });
            }

        });
    }

    /**
     * 从后台获取会议数据和消息数据
     */
    private void httpRequestMeetingContent(){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.type(MultipartBuilder.FORM)
                .addFormDataPart("msg_id", String.valueOf(msg_id))
                .addFormDataPart("uid", new SqliteDBUtils(this).getLoginUid());
        RequestBody requestBody = multipartBuilder.build();
        String request_url = ProtocolUrl.ROOT_URL + ProtocolUrl.APP_QUERY_TUI_SONG_MESSAGE;
        Request request = builder.post(requestBody).url(request_url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mActivityFragmentView.viewLoading(View.GONE);
                        ToastUtils.show(getContext(),R.string.app_network_failure);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result_str = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result_str);
                    int status = jsonObject.getInt("status");
                    if(status==0){
                        result_str = jsonObject.getString("data");
                        Gson gson = new Gson();
                        final MeetingEntity meetingEntity = gson.fromJson(result_str,MeetingEntity.class);
                        mid = meetingEntity.getMid();
                        final List<JPushToUser> jPushToUserList = meetingEntity.getjPushToUser();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_meeting_theme.setText(meetingEntity.getMeetingTheme());
                                for(JPushToUser jPushToUser : jPushToUserList){
                                    if(jPushToUser.getMsg_id()==msg_id){
                                        tv_meeting_faqiren.setText(jPushToUser.getMessageSendPeople());
                                    }
                                }
                                tv_meeting_place.setText(meetingEntity.getMeetingPlace());
                                tv_meeting_start_time.setText(meetingEntity.getMeetingStartTime());
                                tv_meeting_duration.setText(meetingEntity.getMeetingDuration());
                                String name = meetingEntity.getUserIds();   //获取参会人员
                                String[] strings = name.split(getString(R.string.comma));
                                tv_join_meeting_people_num.setText(String.valueOf(strings.length));
                                tv_join_meeting_people.setText(name);
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

}
