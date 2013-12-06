package com.wz.gif.qq;

import com.wz.gif.share.UserInfo;
import com.wz.gif.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

import java.util.Map;

public class QQShareActivity extends Activity{

    private static final String authorize_url = "https://open.t.qq.com/cgi-bin/authorize";
    private static final String token_url = "http://open.t.qq.com/cgi-bin/request_token";
    private static final String accessToken_url = "http://open.t.qq.com/cgi-bin/access_token";
    
    private QOAuth auth;
    private String oauthToken;
    private String oauthSecret;
    
    private String httpMethod = "GET";
    private String callback = "myapp://QQShareActivity";
    private UserInfo userInfo ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
//        setContentView(R.layout.qq_oauth);
        auth = new QOAuth();
        //第一步：
        Map<String,String> map = auth.getAccessToken(token_url,httpMethod,callback,null,null,null);
//        Map<String,String> map = auth.getOAuthToken(token_url);
        oauthToken = map.get("oauth_token");
        oauthSecret = map.get("oauth_token_secret");
        Log.d("first  token is "+oauthToken + " secret "+oauthSecret);
        if(oauthToken == null || oauthToken.equals("")){
            Toast.makeText(QQShareActivity.this, "腾讯服务器无响应", Toast.LENGTH_LONG).show();
               finish();
        }else{
            //第二布：
            StringBuilder sb = new StringBuilder();
            sb.append(authorize_url);
            sb.append("?");
            sb.append("oauth_token="+oauthToken);
            Uri uri = Uri.parse(sb.toString());
            Log.d("get oauth_token uri is :"+uri.toString());
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        }
        
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("come to qq onNewIntent");
        if(intent != null){
            Uri uri = intent.getData();
            String url = uri.toString();
            Log.d("url is "+url);
            int index = url.lastIndexOf("=");
            String verify = url.substring(index+1);
            Log.d("verifier is :"+verify);
            if(verify != null || !verify.equals("")){
                //获取access_token
               Map<String,String> map = auth.getAccessToken(accessToken_url,httpMethod,null,oauthToken,oauthSecret,verify);
               String access_token = map.get("oauth_token");
               String access_secret = map.get("oauth_token_secret");
               Log.d("token "+access_token+" secret "+ access_secret);
               if(null != access_token && null != access_secret){
                   userInfo = new UserInfo(QQShareActivity.this);
                   userInfo.setTokenQQ(access_token);
                   userInfo.setTokenSecretQQ(access_secret);
               }else{
                   Toast.makeText(QQShareActivity.this, "腾讯服务器无响应", Toast.LENGTH_LONG).show();
               }
               QQShareActivity.this.finish();
            }
        }
    }
    
}
