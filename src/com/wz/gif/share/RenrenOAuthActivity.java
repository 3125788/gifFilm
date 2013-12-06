package com.wz.gif.share;

import com.wz.gif.R;
import com.wz.gif.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class RenrenOAuthActivity extends Activity{

    private String RENREN_KEY = "cae86bb16df447d39a4889416614f0f2";
    private String accessToken ;
    
    private UserInfo userInfo;
    private WebView webView;
    private String authUrl = "https://graph.renren.com/oauth/authorize?client_id="  
        + RENREN_KEY+"&response_type=token"  
        + "&redirect_uri=http://graph.renren.com/oauth/login_success.html&display=mobile"  
        + "&scope=status_update photo_upload";  
    
    private String content;
    private String fileName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.renren_oauth);
        
        webView = (WebView)this.findViewById(R.id.renren_webView);
        userInfo = new UserInfo(this);
        initviews();
        Intent intent = getIntent();
        content = intent.getStringExtra("content");
        fileName = intent.getStringExtra("fileName");
    }

    private void initviews() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        webView.loadUrl(authUrl);
        webView.requestFocusFromTouch();
        
        WebViewClient wvc = new WebViewClient(){

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
                //用户名和密码通过验证后就可以得到accessToken
                String retUrl = webView.getUrl();
                Log.d("url is :"+retUrl);
                if(retUrl.indexOf("access_token") != -1){
                    int startPos = retUrl.indexOf("token=") + 6;
                    int endPos = retUrl.indexOf("&expires_in");
                    Log.d("start :"+startPos+" end "+endPos);
                    if(startPos < endPos)
                        accessToken = (String) retUrl.subSequence(startPos, endPos);
                    if(null != accessToken){
                        userInfo.setTokenRenren(accessToken);
                        Toast.makeText(RenrenOAuthActivity.this, "人人登录成功", Toast.LENGTH_SHORT).show();
                        Log.d("get accessToken :"+accessToken);
                        if(!TextUtils.isEmpty(fileName)){
                            Log.d("filename is not null");
                            RenrenOAuth renren = new RenrenOAuth();
                            String renrenResult = renren.shareGif(accessToken, content, fileName);
                        }
                        finish();
                    }
                }
            }
            
        };
        
        webView.setWebViewClient(wvc);
    }
    

}
