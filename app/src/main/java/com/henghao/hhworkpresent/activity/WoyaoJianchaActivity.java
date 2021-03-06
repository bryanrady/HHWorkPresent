package com.henghao.hhworkpresent.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.benefit.buy.library.phoneview.MultiImageSelectorActivity;
import com.benefit.buy.library.utils.tools.ToolsKit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.henghao.hhworkpresent.ActivityFragmentSupport;
import com.henghao.hhworkpresent.Constant;
import com.henghao.hhworkpresent.R;
import com.henghao.hhworkpresent.adapter.JianchaYinhuanListAdpter;
import com.henghao.hhworkpresent.entity.CkInspectrecord;
import com.henghao.hhworkpresent.entity.SaveCheckTaskEntity;
import com.henghao.hhworkpresent.utils.SqliteDBUtils;
import com.henghao.hhworkpresent.views.CustomDialog;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 我要检查界面  可编辑界面
 * Created by ASUS on 2017/9/4.
 */

public class WoyaoJianchaActivity extends ActivityFragmentSupport {

    @ViewInject(R.id.tv_company_name)
    private TextView tv_company_name;

    @ViewInject(R.id.tv_company_type)
    private TextView tv_company_type;

    @ViewInject(R.id.tv_check_time)
    private TextView tv_check_time;

    @ViewInject(R.id.tv_check_people)
    private TextView tv_check_people;

    @ViewInject(R.id.et_check_scene)
    private EditText et_check_scene;

    @ViewInject(R.id.et_check_person)
    private EditText et_check_person;

    @ViewInject(R.id.tv_personal_login)
    private TextView tv_personal_login;

    @ViewInject(R.id.tv_start_check)
    private TextView tv_start_check;

    @ViewInject(R.id.tv_company_name_detail)
    private TextView tv_company_name_detail;

    @ViewInject(R.id.image_proplem_list_up)
    private ImageView image_proplem_list_up;

    @ViewInject(R.id.imageview_woyaojiancha)
    private ImageView imageview_woyaojiancha;

    @ViewInject(R.id.tv_woyao_checked_save)
    private TextView tv_woyao_checked_save;

    @ViewInject(R.id.tv_woyao_checked_cancel)
    private TextView tv_woyao_checked_cancel;

    @ViewInject(R.id.check_yinhuan_listview)
    private ListView check_yinhuan_listview;

    @ViewInject(R.id.layout_problem_list)
    private LinearLayout layout_problem_list;

    private boolean isProblemListOpen = true;

    private SaveCheckTaskEntity saveCheckTaskEntity;
    private List<SaveCheckTaskEntity.JianchaMaterialEntityListBean> mJianchaMaterialEntityList;

    private JianchaYinhuanListAdpter jianchaYinhuanListAdpter;

    private ArrayList<String> mSelectPath;

    private static final int REQUEST_IMAGE = 0x00;

    private ArrayList<String> mImageList=new ArrayList<>();

    private ArrayList<File> mSiteFileList = new ArrayList<>();//点击现场图片被选中的图片文件

    private MyReceiver myReceiver;

    public static int Pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mActivityFragmentView.viewMain(R.layout.activity_woyaojiancha);
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
        mLeftImageView.setVisibility(View.VISIBLE);
        initWithCenterBar();
        mCenterTextView.setText("我要检查");
        mCenterTextView.setVisibility(View.VISIBLE);

        image_proplem_list_up.setImageResource(R.drawable.icon_down);

        check_yinhuan_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(WoyaoJianchaActivity.this,QueryYinhuanInfoActivity.class);
                intent.putExtra("JianchaMaterialEntity",mJianchaMaterialEntityList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        Intent data = getIntent();
        Pid = data.getIntExtra("Pid",0);

        httpRequestCheckTask();

        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.PRESS_SAVE_BUTTON);
        registerReceiver(myReceiver,filter);
    }

    public android.os.Handler mHandler = new android.os.Handler(){};
    /**
     * 根据pid查询检查任务  并把数据添加到我要检查页面
     */
    public void httpRequestCheckTask(){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        final String request_url = "http://172.16.0.81:8080/istration/enforceapp/queryplanbyid?id="+Pid;
        Request request = builder.url(request_url).build();
        Call call = okHttpClient.newCall(request);
        mActivityFragmentView.viewLoading(View.VISIBLE);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mActivityFragmentView.viewLoading(View.GONE);
                        Toast.makeText(getContext(), "网络访问错误！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result_str = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result_str);
                    result_str = jsonObject.getString("data");
                    Gson gson = new Gson();
                    saveCheckTaskEntity = gson.fromJson(result_str,SaveCheckTaskEntity.class);
                    mJianchaMaterialEntityList = saveCheckTaskEntity.getJianchaMaterialEntityList();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mActivityFragmentView.viewLoading(View.GONE);
                            tv_company_type.setText(saveCheckTaskEntity.getEnterprise().getIndustry1());
                            tv_company_name.setText(saveCheckTaskEntity.getEnterprise().getEntname());
                            tv_check_time.setText(saveCheckTaskEntity.getCheckTime());
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    @OnClick({R.id.tv_personal_login,R.id.tv_start_check,R.id.image_proplem_list_up, R.id.tv_company_name_detail,
            R.id.imageview_woyaojiancha,R.id.tv_woyao_checked_save,R.id.tv_woyao_checked_cancel})
    private void viewOnClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.imageview_woyaojiancha:
                choosePicture();
                break;
            case R.id.tv_personal_login:  //陪同检查人员登录
                showLoginDialog();
                break;
            case R.id.tv_start_check:  //打开标准逐项排查
                if(et_check_scene.getText().toString().equals("")){
                    Toast.makeText(WoyaoJianchaActivity.this,"请填写检查现场",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(et_check_person.getText().toString().equals("")){
                    Toast.makeText(WoyaoJianchaActivity.this,"请填写企业现场负责人",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(tv_check_people.getText().toString().equals("")){
                    Toast.makeText(WoyaoJianchaActivity.this,"请登录陪同执法人员",Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("mSelectDescriptData",(Serializable) mJianchaMaterialEntityList);
                intent.setClass(this,JianchaStandardActivity.class);
                startActivity(intent);
                break;
            case R.id.image_proplem_list_up:  //问题列表开关
                if(isProblemListOpen){
                    image_proplem_list_up.setImageResource(R.drawable.icon_down);
                    layout_problem_list.setVisibility(View.VISIBLE);
                    isProblemListOpen = false;
                }else{
                    image_proplem_list_up.setImageResource(R.drawable.icon_up);
                    layout_problem_list.setVisibility(View.GONE);
                    isProblemListOpen = true;
                }
                break;
            case R.id.tv_company_name_detail:   //点击详细资料
                intent.setClass(this,QueryYinhuanInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_woyao_checked_save:    //保存并打开现场检查文书
                //先进行保存数据到服务器的操作
                saveCheckedDataToService();
                //然后再打开文书页面
                bindingDataToSceneJianchaActivity();
                break;
            case R.id.tv_woyao_checked_cancel:  //取消  跳转到添加检查任务界面
                intent.setClass(this,AddJianchaTaskActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }


    /**
     * 保存数据到服务器的操作
     */
    public void saveCheckedDataToService(){
        SqliteDBUtils sqliteDBUtils = new SqliteDBUtils(this);
        SaveCheckTaskEntity saveCheckTaskEntity = new SaveCheckTaskEntity();
        saveCheckTaskEntity.setPid(Pid);
     //   saveCheckTaskEntity.setCompany_name(tv_company_name.getText().toString());
     //   saveCheckTaskEntity.setCheckPeople1(sqliteDBUtils.getLoginFirstName()+ sqliteDBUtils.getLoginGiveName());
     //   saveCheckTaskEntity.setCheckPeople2(tv_check_people.getText().toString());
     //   saveCheckTaskEntity.setCheckTime(tv_check_time.getText().toString());
    //    saveCheckTaskEntity.setJianchaMaterialEntityList(mJianchaMaterialEntityList); 这个数据在添加隐患界面保存过了，就不添加保存了
        saveCheckTaskEntity.setCheckSite(et_check_scene.getText().toString());
        saveCheckTaskEntity.setSiteResponse(et_check_person.getText().toString());
        //上传图片路径 并且还要上传图片文件 现场图片和隐患包含的图片
        if(!ToolsKit.isEmpty(this.mSelectPath)){
            saveCheckTaskEntity.setSiteImagePath(mSelectPath.get(0));
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        //这个要和服务器保持一致 application/json;charset=UTF-8
        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.type(MultipartBuilder.FORM)
                .addFormDataPart("json", com.alibaba.fastjson.JSONObject.toJSONString(saveCheckTaskEntity));//json数据
        for (File siteFile : mSiteFileList) {
            //上传现场图片
            multipartBuilder.addFormDataPart("file", siteFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data"),siteFile));
        }
        RequestBody requestBody = multipartBuilder.build();
        Request request = builder.post(requestBody).url("http://172.16.0.81:8080/istration/enforceapp/updateplanexaminedate").build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        msg("网络请求错误！");
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        msg("数据保存成功！");
                    }
                });
            }
        });
    }

    /**
     * 传递数据并打开页面
     */
    public void bindingDataToSceneJianchaActivity(){
        String checkUnit = tv_company_name.getText().toString();    //被检查单位
        String checkAddress = saveCheckTaskEntity.getEnterprise().getProductaddress();         //单位地址
        String legalRepresentative = saveCheckTaskEntity.getEnterprise().getLegalpeople();     //法定代表人
        String legalDuty = "";      //法定代表人职务没有找到
        String contactNumber =  saveCheckTaskEntity.getEnterprise().getLegalmobilephone();     //法定代表人联系电话
        String checkSite =  et_check_scene.getText().toString();    //检查场所
        String checkTime = tv_check_time.getText().toString();      //检查时间1和检查时间2
        String cityName = "";   //市的名字 先暂时为Null
        String checkPeople1 = new SqliteDBUtils(this).getLoginFirstName()+ new SqliteDBUtils(this).getLoginGiveName();  //检查人员1 也就是系统登录人员
        String checkPeople2 = saveCheckTaskEntity.getTroopemp().getName();  //检查人员2 也就是被选中的执法人员
        String documentsId1 = "";  // =  执法人员1 的编号 也就是系统登录人员的员工编号
        String documentsId2 = saveCheckTaskEntity.getTroopemp().getEmp_NUM(); //执法人员2 的证件号
        String checkCase = "";  // = 检查情况 没有数据
        List<SaveCheckTaskEntity.JianchaMaterialEntityListBean> checkYinhuanList = mJianchaMaterialEntityList;    //被选中的检查问题隐患
        String checkSignature11 = new SqliteDBUtils(this).getLoginFirstName()+ new SqliteDBUtils(this).getLoginGiveName();  //检查人员1的签名
        String checkSignature12 = saveCheckTaskEntity.getTroopemp().getName();  //检查人员2的签名
        String beCheckedPeople = et_check_person.getText().toString();  //被检查企业现场负责人
        String recordingTime = tv_check_time.getText().toString();

        CkInspectrecord ckInspectrecord = new CkInspectrecord();
        ckInspectrecord.setCheckUnit(checkUnit);
        ckInspectrecord.setCheckAddress(checkAddress);
        ckInspectrecord.setLegalRepresentative(legalRepresentative);
        ckInspectrecord.setLegalDuty(legalDuty);
        ckInspectrecord.setContactNumber(contactNumber);
        ckInspectrecord.setCheckSite(checkSite);
        ckInspectrecord.setCheckTime1(checkTime);
        ckInspectrecord.setCheckTime2(checkTime);
        ckInspectrecord.setCityName(cityName);
        ckInspectrecord.setCheckPeople1(checkPeople1);
        ckInspectrecord.setCheckPeople2(checkPeople2);
        ckInspectrecord.setDocumentsId1(documentsId1);
        ckInspectrecord.setDocumentsId2(documentsId2);
        ckInspectrecord.setCheckCase(checkCase);
        ckInspectrecord.setCheckYinhuanList(checkYinhuanList);
        ckInspectrecord.setCheckSignature11(checkSignature11);
        ckInspectrecord.setCheckSignature12(checkSignature12);
        ckInspectrecord.setBeCheckedPeople(beCheckedPeople);
        ckInspectrecord.setRecordingTime(recordingTime);

        Intent intent = new Intent();
        intent.setClass(this, SceneJianchaActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putSerializable("ckInspectrecord", ckInspectrecord);
        mBundle.putString("Pid", String.valueOf(Pid));
        intent.putExtras(mBundle);
        startActivity(intent);
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //监听检查标准页面保存键
            if((Constant.PRESS_SAVE_BUTTON).equals(action)){
                //根据pid查询有隐患的被检查隐患文件列表
                OkHttpClient okHttpClient = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                final String request_url = "http://172.16.0.81:8080/istration/enforceapp/queryjianchabystatus?pid="+Pid;
                Request request = builder.url(request_url).build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mActivityFragmentView.viewLoading(View.GONE);
                                Toast.makeText(getContext(), "网络访问错误！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String result_str = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(result_str);
                            result_str = jsonObject.getString("data");
                            Gson gson = new Gson();
                            mJianchaMaterialEntityList = gson.fromJson(result_str,new TypeToken<ArrayList<SaveCheckTaskEntity.JianchaMaterialEntityListBean>>() {}.getType());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    jianchaYinhuanListAdpter = new JianchaYinhuanListAdpter(WoyaoJianchaActivity.this, mJianchaMaterialEntityList);
                                    setListViewHeightBasedOnChildren(check_yinhuan_listview);
                                    check_yinhuan_listview.setAdapter(jianchaYinhuanListAdpter);
                                    jianchaYinhuanListAdpter.notifyDataSetChanged();
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }
        }
    }

    /**
     * 根据Item数设定ListView高度
     * @param listView
     */
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * listAdapter.getCount());
        listView.setLayoutParams(params);
    }

    /**
     * 展示登录对话框
     */
    public void showLoginDialog(){
        View customView = View.inflate(this,R.layout.layout_person_login_dialog,null);
        final EditText et_person_username = (EditText) customView.findViewById(R.id.et_person_username);
        final EditText et_person_password = (EditText) customView.findViewById(R.id.et_person_password);
        CustomDialog.Builder dialog=new CustomDialog.Builder(this);
        dialog.setTitle("陪同执法人员登录")
                .setContentView(customView)//设置自定义customView
                .setPositiveButton("登录", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        boolean isUsername = et_person_username.getText().toString()!=null && !et_person_username.getText().toString().equals("") && !et_person_username.getText().toString().equals("null");
                        boolean isPassword = et_person_password.getText().toString()!=null && !et_person_password.getText().toString().equals("") && !et_person_password.getText().toString().equals("null");
                        if(isUsername && isPassword){
                            if(saveCheckTaskEntity.getTroopemp().getLoginid().equals(et_person_username.getText().toString()) &&
                                    saveCheckTaskEntity.getTroopemp().getPassword().equals(et_person_password.getText().toString())){
                                tv_check_people.setText(saveCheckTaskEntity.getTroopemp().getName());
                                tv_personal_login.setVisibility(View.GONE);
                            }else{
                                et_person_username.setText("");
                                et_person_password.setText("");
                                Toast.makeText(WoyaoJianchaActivity.this,"用户名或密码错误,请重新输入",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }

    public void choosePicture(){
        // 查看session是否过期
        // int selectedMode = MultiImageSelectorActivity.MODE_SINGLE;
        //设置单选模式
        int selectedMode = MultiImageSelectorActivity.MODE_SINGLE;
        int maxNum = 9;
        Intent picIntent = new Intent(this, MultiImageSelectorActivity.class);
        // 是否显示拍摄图片
        picIntent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大可选择图片数量
        picIntent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, maxNum);
        // 选择模式
        picIntent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, selectedMode);
        // 默认选择
        if ((this.mSelectPath != null) && (this.mSelectPath.size() > 0)) {
            picIntent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, this.mSelectPath);
        }
        startActivityForResult(picIntent, REQUEST_IMAGE);
    }

    private String getImageName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == REQUEST_IMAGE) {
                if ((resultCode == Activity.RESULT_OK) || (resultCode == Activity.RESULT_CANCELED)) {
                    this.mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                    if (!ToolsKit.isEmpty(this.mSelectPath)) {
                        List<String> fileNames = new ArrayList<>();
                        mImageList.clear();
                        mSiteFileList.clear();
                        for (String filePath : mSelectPath) {
                            String imageName = getImageName(filePath);
                            fileNames.add(imageName);
                            File file = new File(filePath);
                            mSiteFileList.add(file);
                            Bitmap bm = BitmapFactory.decodeFile(filePath);
                            //设置图片
                            imageview_woyaojiancha.setImageBitmap(bm);
                        }
                    }
                }
            }
        }
    }

}
