package com.wz.gif;

import com.wz.gif.qq.QQShareActivity;
import com.wz.gif.share.RenrenOAuthActivity;
import com.wz.gif.share.SinaOAuth;
import com.wz.gif.share.UserInfo;
import com.wz.gif.util.Log;
import com.wz.gif.view.SlipSwitch;
import com.wz.gif.view.SlipSwitch.OnSwitchListener;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity{
    
     private TextView text_sina;
     private TextView text_qq;
     private TextView text_renren;
     //三个登录和注销按钮
     private Button btn_sina;
     private Button btn_qq;
     private Button btn_renren;
     private TextView btn_update;
     private TextView btn_feedback;
     private TextView btn_commend;//推荐按钮
     private TextView btn_guide;
     
     private ProgressBar progressBar;
     private Handler updateHandler ;
     
     private SlipSwitch quality_switch;
     
     private UserInfo userInfo;
     private boolean sina_has_login;
     private boolean renren_has_login;
     private boolean qq_has_login;
    
     private SinaOAuth sinaOAuth;
     private String callBackUrl = "myapp://SettingActivity";
     
     /*
      * 0 新浪 1 腾讯 2 人人
      */
     private int btn_id = -1;
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.setting);
        MyApp app = (MyApp) this.getApplication();
        if(app != null ){
            if(app.bitmaps != null){
                app.bitmaps.clear();
            }
            if(app.magicBitmaps != null){
                app.magicBitmaps.clear();
            }
        }
        updateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progressBar.setVisibility(View.GONE);
                switch(msg.arg1){
                    case 0:
                        Toast.makeText(SettingActivity.this, "当前是最新版本", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        //提示用户是否更新，然后更新
                        break;
                }
            }
            
        };
        
        setUpViews();
        initValues();
        setUpListeners();
    }
    
    private void sinaLogin(){
        sina_has_login = true;
        text_sina.setTextColor(Color.GREEN);
        text_sina.setText("已经登录");
        btn_sina.setText("注销");
    }

    private void sinaLogout(){
        sina_has_login = false;
        userInfo.setTokenSina(null);
        text_sina.setTextColor(Color.RED);
        text_sina.setText("请登录");
        btn_sina.setText("登录");
    }
    
    private void qqLogin(){
        Log.d("qq 已经登录");
        qq_has_login = true;
        text_qq.setTextColor(Color.GREEN);
        text_qq.setText("已经登录");
        btn_qq.setText("注销");
    }
    private void qqLogout(){
        qq_has_login = false;
        userInfo.setTokenQQ(null);
        text_qq.setTextColor(Color.RED);
        text_qq.setText("请登录");
        btn_qq.setText("登录");
    }
    private void renrenLogin(){
        renren_has_login = true;
        text_renren.setTextColor(Color.GREEN);
        text_renren.setText("已经登录");
        btn_renren.setText("注销");
    }
    private void renrenLogout(){
        Log.d("renren 没有登录");
        renren_has_login = false;
        userInfo.setTokenRenren(null);
        text_renren.setTextColor(Color.RED);
        text_renren.setText("请登录");
        btn_renren.setText("登录");
    }
    
    private void initValues() {
        userInfo = new UserInfo(this);
        sinaOAuth = new SinaOAuth(userInfo);
        
        text_sina.setText("test");
        
        if( !TextUtils.isEmpty(userInfo.getTokenSina())){
            Log.d("sina 已经登录");
            sinaLogin();
        }else{
            sinaLogout();
        }
        
        if( !TextUtils.isEmpty(userInfo.getTokenQQ()) ){
            qqLogin();
        }else{
            qqLogout();
        }
        
        if( !TextUtils.isEmpty(userInfo.getTokenRenren()) ){
            renrenLogin();
        }else{
            renrenLogout();
        }
        
        quality_switch.setImageResource(R.drawable.bkg_switch, R.drawable.bkg_switch, R.drawable.btn_slip);
    }

    private void setUpViews() {
        text_sina = (TextView)this.findViewById(R.id.setting_text_sina);
        text_renren = (TextView)this.findViewById(R.id.setting_text_renren);
        text_qq = (TextView)this.findViewById(R.id.setting_text_qq);
        
        btn_sina = (Button)this.findViewById(R.id.setting_btn_sina);
        btn_qq = (Button)this.findViewById(R.id.setting_btn_qq);
        btn_renren = (Button)this.findViewById(R.id.setting_btn_renren);
        
        btn_update = (TextView)this.findViewById(R.id.setting_btn_update);
        btn_feedback = (TextView)this.findViewById(R.id.setting_btn_feedback);
        btn_commend = (TextView)this.findViewById(R.id.setting_btn_commend);
        btn_guide = (TextView)this.findViewById(R.id.setting_btn_guide);
//        btn_guide = (Button)this.findViewById(R.id.setting_btn_guide);
        quality_switch = (SlipSwitch)this.findViewById(R.id.setting_slipSwitch);
        progressBar = (ProgressBar)this.findViewById(R.id.feedback_progressBar);
    }

    private void setUpListeners() {
        btn_sina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_id = 0;
                if(sina_has_login){
                    showDialog();
                }else{
                    //TODO:登录
                    sinaOAuth.RequestAccessToken(SettingActivity.this, callBackUrl);
                }
            }
        });
        btn_qq.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                btn_id = 1;
                if(qq_has_login){
                    showDialog();
                }else{
                    //TODO:登录
                    Intent intent = new Intent(SettingActivity.this,QQShareActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        btn_renren.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                btn_id = 2;
                if(renren_has_login){
                    showDialog();
                }else{
                    //TODO:登录
                    Intent intent = new Intent(SettingActivity.this,RenrenOAuthActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        quality_switch.setOnSwitchListener(new OnSwitchListener(){
            @Override
            public void onSwitched(boolean isSwitchOn) {
                
//                MyApp app = (MyApp)getApplication();
//                if(isSwitchOn){
//                    Log.d("high quality");
//                    app.imageHeight = 320;
//                    app.imageWidth = 240;
//                }else{
//                    Log.d("low quality");
//                    app.imageHeight = 200;
//                    app.imageWidth = 266;
//                }
            }
            
        });
        
       btn_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this,GuideActivity.class);
                startActivity(intent);
                finish();
            }
        });
       btn_update.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressBar.setVisibility(View.VISIBLE);
            UpdateThread thread = new UpdateThread();
            thread.start();
        }
    });
       btn_commend.setOnClickListener(new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            //将文本内容放进去 
          intent.putExtra(Intent.EXTRA_TEXT, "我在用GIF胶卷，制作有趣的GIF图片，，你也来玩吧！请到GIF胶卷官网下载APP：）");
            startActivity(Intent.createChooser(intent, "分享"));
            Toast.makeText(SettingActivity.this, "正在转向分享", Toast.LENGTH_SHORT).show();
        }
    });
       
       btn_feedback.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Intent intent = new Intent(SettingActivity.this,FeedbackActivity.class);
//            startActivity(intent);
            Intent mailIntent=new Intent(android.content.Intent.ACTION_SEND); 
            mailIntent.setType("plain/test"); 
            String[] recipients = new String[]{"wuzhi1990@gmail.com", "",};
            mailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
            mailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GIF胶卷反馈"); 
            startActivity(Intent.createChooser(mailIntent, "请选择Email邮件程序")); 
            Toast.makeText(SettingActivity.this, "正在转向Gmail", Toast.LENGTH_SHORT).show();
        }
    });
    }
    
    /**
     * 检查是否有新版本提供下载</br>
     * return 0 do not have new version;
     *  1 otherwise;
     */
    protected int haveNewVersion() {
        return 0;
    }

    /**
     * 显示提醒对话框
     */
    protected void showDialog() {
        new AlertDialog.Builder(SettingActivity.this)
        .setTitle("友情提示")
        .setMessage("是否注销当前帐号并重新登录?")
        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //注销了，显示登录页面
                Log.d("Setting btn_id is "+btn_id);
                switch(btn_id){
                    case 0:
                        sinaLogout();
                        break;
                    case 1:
                        qqLogout();
                        break;
                    case 2:
                        renrenLogout();
                        break;
                }
            }
        })
        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
        .show();
}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent come back");
        if(null != intent && intent.getData() != null){
            Uri uri = intent.getData();
            String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
            if(sinaOAuth == null){
                sinaOAuth = new SinaOAuth(userInfo);
            }
            try {
                if(sinaOAuth.httpOauthprovider == null){
                    Log.d("sinaOAuth.httpOauthprovider is null");
                    sinaOAuth.init();
                }
                sinaOAuth.httpOauthprovider.setOAuth10a(true); 
                sinaOAuth.httpOauthprovider.retrieveAccessToken(sinaOAuth.httpOauthConsumer,verifier);
            } catch (OAuthMessageSignerException ex) {
                ex.printStackTrace();
            } catch (OAuthNotAuthorizedException ex) {
                ex.printStackTrace();
            } catch (OAuthExpectationFailedException ex) {
                ex.printStackTrace();
            } catch (OAuthCommunicationException ex) {
                ex.printStackTrace();
            }
//                SortedSet<String> user_id= sinaOAuth.httpOauthprovider.getResponseParameters().get("user_id");
//                String userId=user_id.first();
            //token
            String userKey = sinaOAuth.httpOauthConsumer.getToken();
            userInfo.setTokenSina(userKey);
            //tokenSecret
            String userSecret = sinaOAuth.httpOauthConsumer.getTokenSecret();
            userInfo.setTokenSecretSina(userSecret);
            sinaLogin();
            Toast.makeText(getApplicationContext(), userKey+" --- " +userSecret, Toast.LENGTH_LONG).show();
            }
        }
    
    private class UpdateThread extends Thread{

        @Override
        public void run() {
            super.run();
            try {
                sleep(2000);
                Message msg = updateHandler.obtainMessage();
                msg.arg1 = haveNewVersion();
                updateHandler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }
}
