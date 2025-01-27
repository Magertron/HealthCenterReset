package com.shenkangyun.healthcenter.MainPage.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.shenkangyun.healthcenter.BaseFolder.AppConst;
import com.shenkangyun.healthcenter.BaseFolder.Base;
import com.shenkangyun.healthcenter.BeanFolder.ChangeBean;
import com.shenkangyun.healthcenter.LoginPage.LoginActivity;
import com.shenkangyun.healthcenter.R;
import com.shenkangyun.healthcenter.UtilFolder.ClearWriteEditText;
import com.shenkangyun.healthcenter.UtilFolder.GsonCallBack;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

public class ChangeWordActivity extends AppCompatActivity {

    @BindView(R.id.toolBar_title)
    TextView toolBarTitle;
    @BindView(R.id.toolBar)
    Toolbar toolBar;
    @BindView(R.id.ed_oldWord)
    ClearWriteEditText edOldWord;
    @BindView(R.id.ed_newWord)
    ClearWriteEditText edNewWord;
    @BindView(R.id.ed_confirm)
    ClearWriteEditText edConfirm;
    @BindView(R.id.btn_submit)
    Button btnSubmit;

    private String oldWord;
    private String newWord;
    private String confirm;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_word);
        ButterKnife.bind(this);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.login_red));
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolBarTitle.setText("更改密码");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        sp = getSharedPreferences("config", MODE_PRIVATE);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initGetData();
            }
        });
    }

    private void initGetData() {
        oldWord = edOldWord.getText().toString();
        newWord = edNewWord.getText().toString();
        confirm = edConfirm.getText().toString();
        if (TextUtils.isEmpty(oldWord)) {
            Toast.makeText(this, "请输入原密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newWord)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "请输入确认密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newWord.equals(confirm)) {
            Toast.makeText(this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        initUpdate();
    }

    private void initUpdate() {
        OkHttpUtils.post()
                .url(Base.URL)
                .addParams("act", "updatePassword")
                .addParams("data", new updatePassword(Base.appKey, Base.timeSpan, "1", "1",
                        String.valueOf(Base.getUserID()), oldWord, newWord, confirm).toJson())
                .build()
                .execute(new GsonCallBack<ChangeBean>() {
                    @Override
                    public void onSuccess(ChangeBean response) throws JSONException {
                        String status = response.getStatus();
                        if ("0".equals(status)) {
                            initChangeWord();
                            SharedPreferences.Editor edit = sp.edit();
                            edit.putString(AppConst.LOGING_PASSWORD, "");
                            edit.commit();
                            Toast.makeText(ChangeWordActivity.this, response.getData().getMsg(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            setResult(2, intent);
                            Intent intent1 = new Intent(ChangeWordActivity.this, LoginActivity.class);
                            startActivity(intent1);
                            finish();
                        } else {
                            Toast.makeText(ChangeWordActivity.this, response.getData().getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    private void initChangeWord() {
        JMessageClient.updateUserPassword(oldWord, newWord, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                setResult(1, intent);
                finish();
                break;
        }
        return true;
    }

    static class updatePassword {

        private String appKey;
        private String timeSpan;
        private String appType;
        private String mobileType;
        private String id;
        private String password;
        private String passwordnew;
        private String passwordCopy;

        public updatePassword(String appKey, String timeSpan, String mobileType, String appType, String id, String password,
                              String passwordnew, String passwordCopy) {
            this.appKey = appKey;
            this.timeSpan = timeSpan;
            this.appType = appType;
            this.mobileType = mobileType;
            this.id = id;
            this.password = password;
            this.passwordnew = passwordnew;
            this.passwordCopy = passwordCopy;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }
}
