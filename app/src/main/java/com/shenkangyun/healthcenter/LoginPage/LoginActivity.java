package com.shenkangyun.healthcenter.LoginPage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.shenkangyun.healthcenter.BaseFolder.AppConst;
import com.shenkangyun.healthcenter.BaseFolder.Base;
import com.shenkangyun.healthcenter.BeanFolder.InsertBean;
import com.shenkangyun.healthcenter.BeanFolder.LoginBean;
import com.shenkangyun.healthcenter.DBFolder.User;
import com.shenkangyun.healthcenter.MainPage.Activity.MainActivity;
import com.shenkangyun.healthcenter.R;
import com.shenkangyun.healthcenter.UtilFolder.GsonCallBack;
import com.shenkangyun.healthcenter.UtilFolder.NToast;
import com.shenkangyun.healthcenter.UtilFolder.TagAliasOperatorHelper;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.litepal.LitePal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

import static com.shenkangyun.healthcenter.UtilFolder.TagAliasOperatorHelper.ACTION_SET;
import static com.shenkangyun.healthcenter.UtilFolder.TagAliasOperatorHelper.sequence;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.de_login_phone)
    EditText deLoginPhone;
    @BindView(R.id.de_login_password)
    EditText deLoginPassword;

    private String phoneString;
    private String passwordString;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private int age;
    private int id;
    private int degree;
    private String name;
    private String idCard;
    private String brithday;
    private String mobile;
    private String loginName;
    private String height;
    private String weight;
    private int childWeeks;
    private int husbandAge;
    private int profession;
    private String complication;
    private int husbandProfession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarUtil.setTranslucent(LoginActivity.this, 55);
        ButterKnife.bind(this);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        initView();
    }

    private void initView() {
        String oldPhone = sp.getString(AppConst.LOGING_PHONE, "");
        String oldPassword = sp.getString(AppConst.LOGING_PASSWORD, "");
        if (!TextUtils.isEmpty(oldPhone) && !TextUtils.isEmpty(oldPassword)) {
            phoneString = oldPhone;
            passwordString = oldPassword;
            deLoginPhone.setText(oldPhone);
            deLoginPassword.setText(oldPassword);
            goToMain();
        }
        if (!TextUtils.isEmpty(oldPhone) && TextUtils.isEmpty(oldPassword)) {
            phoneString = oldPhone;
            deLoginPhone.setText(oldPhone);
        }
    }

    @OnClick({R.id.de_login_sign, R.id.de_login_register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.de_login_sign:
                phoneString = deLoginPhone.getText().toString().trim();
                passwordString = deLoginPassword.getText().toString().trim();
                if (TextUtils.isEmpty(phoneString)) {
                    NToast.shortToast(this, R.string.phone_number_is_null);
                    return;
                }
                if (TextUtils.isEmpty(passwordString)) {
                    NToast.shortToast(this, R.string.password_is_null);
                    return;
                }
                if (passwordString.contains(" ")) {
                    NToast.shortToast(this, R.string.password_cannot_contain_spaces);
                    return;
                }
                goToMain();
                break;
//            case R.id.de_login_forgot:
//                Intent intentFor = new Intent(this, ForgotActivity.class);
//                startActivity(intentFor);
//                break;
            case R.id.de_login_register:
                Intent intentReg = new Intent(this, RegisterActivity.class);
                startActivityForResult(intentReg, 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String userName = data.getStringExtra("UserName");
        String passWord = data.getStringExtra("PassWord");
        if (requestCode == 0 && resultCode == 1
                && !TextUtils.isEmpty(userName) && !TextUtils.isEmpty(passWord)) {
            deLoginPhone.setText(userName);
            deLoginPassword.setText(passWord);
        }
    }

    private void goToMain() {
        LitePal.deleteAll(User.class);
        OkHttpUtils.post()
                .url(Base.URL)
                .addParams("act", "login")
                .addParams("data", new Login(Base.appKey, Base.timeSpan, "1", "1", phoneString, passwordString).toJson())
                .build()
                .execute(new GsonCallBack<LoginBean>() {
                    @Override
                    public void onSuccess(LoginBean response) {
                        String status = response.getStatus();
                        if ("0".equals(status)) {
                            initUserInfo(response);
                            onTagAliasAction(mobile);
                            initJMessageLogin();
                            editor.putString(AppConst.LOGING_PHONE, phoneString);
                            editor.putString(AppConst.LOGING_PASSWORD, passwordString);
                            editor.commit();
                            NToast.shortToast(LoginActivity.this, R.string.login_success);
                            String name = response.getData().getPatient().getName();
                            if (TextUtils.isEmpty(name)) {
                                Intent intent = new Intent(LoginActivity.this, ConsummateActivity.class);
                                intent.putExtra("idCard", idCard);
                                intent.putExtra("mobile", mobile);
                                intent.putExtra("loginName", loginName);
                                startActivity(intent);
                                finish();
                            } else {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

    }

    private void initUserInfo(LoginBean response) {

        id = response.getData().getPatient().getId();
        age = response.getData().getPatient().getAge();
        name = response.getData().getPatient().getName();
        idCard = response.getData().getPatient().getIdCard();
        brithday = response.getData().getPatient().getBrithday();
        mobile = response.getData().getPatient().getMobile();
        degree = response.getData().getPatient().getDegree();
        loginName = response.getData().getPatient().getLoginName();
        height = response.getData().getPatient().getHeight();
        weight = response.getData().getPatient().getWeight();
        childWeeks = response.getData().getPatient().getChildWeeks();
        husbandAge = response.getData().getPatient().getHusbandAge();
        profession = response.getData().getPatient().getProfession();
        complication = response.getData().getPatient().getComplication();
        husbandProfession = response.getData().getPatient().getHusbandProfession();

        User user = new User();
        user.setUserID(id);
        user.setName(name);
        user.setMobile(mobile);
        user.setIdCard(idCard);
        user.setBrithday(brithday);
        user.setDegree(degree);
        user.setLoginName(loginName);
        user.setAge(age);
        user.setHeight(height);
        user.setWeight(weight);
        user.setChildWeeks(childWeeks);
        user.setHusbandAge(husbandAge);
        user.setHusbandProfession(husbandProfession);
        user.setProfession(profession);
        user.setComplication(complication);
        user.saveThrows();
    }

    private void initJMessageLogin() {
        JMessageClient.login(phoneString, passwordString, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if ("0".equals(String.valueOf(i))) {
                    initGetUserInfo();
                }
            }
        });
    }

    private void initGetUserInfo() {
        JMessageClient.getUserInfo(phoneString, new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, UserInfo userInfo) {
                if ("0".equals(String.valueOf(i))) {
                    long userID = userInfo.getUserID();
                    OkHttpUtils.post()
                            .url(Base.URL)
                            .addParams("act", "insertJmessage")
                            .addParams("data", new insertJmessage(Base.appKey, Base.timeSpan, "1", "1",
                                    String.valueOf(userID), "").toJson())
                            .build()
                            .execute(new GsonCallBack<InsertBean>() {
                                @Override
                                public void onSuccess(InsertBean response) throws JSONException {
                                    String status = response.getStatus();
                                    if ("0".equals(status)) {

                                    }
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                }
            }
        });

    }

    public void onTagAliasAction(String alias) {
        int action = -1;
        boolean isAliasAction = true;

        if (TextUtils.isEmpty(alias)) {
            return;
        }
        action = ACTION_SET;

        TagAliasOperatorHelper.TagAliasBean tagAliasBean = new TagAliasOperatorHelper.TagAliasBean();
        tagAliasBean.action = action;
        sequence++;
        tagAliasBean.alias = alias;

        tagAliasBean.isAliasAction = isAliasAction;
        TagAliasOperatorHelper.getInstance().handleAction(getApplicationContext(), sequence, tagAliasBean);
    }


    static class Login {
        private String appKey;
        private String timeSpan;
        private String mobileType;
        private String appType;
        private String username;
        private String password;

        public Login(String appKey, String timeSpan, String mobileType, String appType, String username, String password) {
            this.appKey = appKey;
            this.timeSpan = timeSpan;
            this.mobileType = mobileType;
            this.appType = appType;
            this.username = username;
            this.password = password;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }

    static class insertJmessage {
        private String appKey;
        private String timeSpan;
        private String mobileType;
        private String patientID;
        private String imID;
        private String organizeID;

        public insertJmessage(String appKey, String timeSpan, String mobileType, String patientID, String imID, String organizeID) {
            this.appKey = appKey;
            this.timeSpan = timeSpan;
            this.mobileType = mobileType;
            this.patientID = patientID;
            this.imID = imID;
            this.organizeID = organizeID;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }
}
