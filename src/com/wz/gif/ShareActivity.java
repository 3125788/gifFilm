package com.wz.gif;

import com.wz.gif.decoder.GifView;
import com.wz.gif.decoder.GifView.GifImageType;
import com.wz.gif.qq.QOAuth;
import com.wz.gif.qq.QQShareActivity;
import com.wz.gif.share.RenrenOAuth;
import com.wz.gif.share.RenrenOAuthActivity;
import com.wz.gif.share.SinaOAuth;
import com.wz.gif.share.UserInfo;
import com.wz.gif.util.BtnAnimation;
import com.wz.gif.util.FileUtil;
import com.wz.gif.util.FileUtil.FileType;
import com.wz.gif.util.Log;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class ShareActivity extends Activity{

    private View btns;
    private Button btn_ok;
    private boolean btn_ok_clicked = false;
    private View virtualLayout;
    
    private ImageButton btn_renren;
    private ImageButton btn_sina;
    private ImageButton btn_qq;
    
    private ProgressBar progressBar;
    private EditText editText;
    private GifView gifView;
    private TextView wordText;
    
    private TranslateAnimation translateAnimation;
    private InputStream ips;
    
    private UserInfo userInfo;
    
    private SinaOAuth sinaOAuth;
    
    private String callBackUrl = "myapp://ShareActivity";
    private BtnAnimation btn_animation;
    /**
     * 用于判断点击的是哪一个btn，0：renren，1：sina，2：qq
     */
    private int btn_id;
    private boolean netAvailable;
    private RequestThread resuqestThread;
    
    private FileUtil fileUtil;
    private int index;
    private String content;
    private String fileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.d("shareActivity onCreate");
        
        setContentView(R.layout.share);
        
        setUpViews();
        setUpListeners();
        
        Intent intent =  getIntent();
        fileUtil = new FileUtil(ShareActivity.this,FileType.GIF);
        
        Log.d("bitmap index id  "+index);
        content = intent.getStringExtra(Intent.EXTRA_TEXT);
        //TODO：这个content是从别的应用分享过来的，所以这里就没必要加图片
        if(!TextUtils.isEmpty(content)){
            editText.setText(content);
            
        }
        //得到分享时的text，如果当前activity是从我们的程序跳转过来的话tmp为空
        String dir = intent.getDataString();
        if(!TextUtils.isEmpty(dir)){
            Log.d("dir is not null "+dir);
            fileName = (String) dir.subSequence(7, dir.length());
            ips = fileUtil.getFileStream(fileName);
            Bitmap bm = BitmapFactory.decodeStream(ips);
            gifView.setImageBitmap(bm);
            gifView.setGifImageType(GifImageType.ONE_FRAME);
        }else{
            //下面就是GIF胶卷自身应该打开的图片
//            这是从gifMaker中传过来的
            fileName = intent.getStringExtra("fileName");
            if(!TextUtils.isEmpty(fileName)){
            }else{
                //这是从welcomeActivity传过来的
                index = intent.getIntExtra("index", 0);
                fileName = fileUtil.DIR + fileUtil.getFileNames().get(index);
            }
            Log.d("shareActivity getFileName is " + fileName);
            ips = fileUtil.getFileStream(fileName);
            gifView.setGifImageType(GifImageType.SYNC_DECODER);
        }
        
        gifView.setGifImage(ips);
        
        userInfo = new UserInfo(ShareActivity.this);
        
        sinaOAuth = new SinaOAuth(userInfo);
        
        btn_animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toggleBtns();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
              toggleBtns();
              resuqestThread = new RequestThread();
              resuqestThread.start();
//                sendRequest();
            }
        });
        
        netAvailable = getNetState();
        if( !netAvailable){
            Toast.makeText(ShareActivity.this, "检测到网络不可用，请配置网络服务", Toast.LENGTH_LONG).show();
        }
        
        btnHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                virtualLayout.setVisibility(View.GONE);
                btn_ok.setText("上传");
                Log.d("hanlder message");
                switch(msg.what){
                    case 0:
                        //点击相应的分享图片后，设置分享图片
                        progressBar.setVisibility(View.VISIBLE);
                        switch(btn_id){
                            case 0:
                                btn_renren.setAnimation(btn_animation);
                                btn_renren.startAnimation(btn_animation);
                                break;
                            case 1:
                                btn_sina.setAnimation(btn_animation);
                                btn_sina.startAnimation(btn_animation);
                                break;
                            case 2:
                                btn_qq.setAnimation(btn_animation);
                                btn_qq.startAnimation(btn_animation);
                                break;
                        }
                        break;
                    case 1:
                        //TODO:分享成功,统计流量关键是把这次上传的gif图大小统计一下
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "分享成功！", Toast.LENGTH_LONG).show();
                        userInfo.setData_stream(fileUtil.getFileSize(index));

                        break;
                    case 2:
                        progressBar.setVisibility(View.GONE);
                        break;
                }
            }       
        };
    }

    /**
     * 判断当前网络状态
     */
    private boolean getNetState(){
        ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if(netInfo != null){
            Log.d(netInfo.toString()+" 是吗 ");
            return netInfo.isAvailable();
        }
        return false;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ShareActivity onActivityResult "+ requestCode + " resultCode " + resultCode );
    }

    /**
     * 发送图片的线程，将结果发送到UI线程处理
     * 标志：
     * 0：点击了分享按钮，播放动画
     * 1：分享成功
     * 2：需要认证
     * @author wuzhi
     *
     */
    class RequestThread extends Thread{

        @Override
        public void run() {
            super.run();
             //向服务器发送请求
                Message msg = btnHandler.obtainMessage();
                if(netAvailable){
                    switch(btn_id){
                        case 0:
                          if(userInfo.getTokenRenren() == null){
//                          Toast.makeText(ShareActivity.this, "人人新用户，将去进行认证", Toast.LENGTH_LONG).show();
                          Log.d("人人新用户，人人认证");
                          RENREN_REQUEST = true;
                          Intent intent = new Intent(ShareActivity.this,RenrenOAuthActivity.class);
                          intent.putExtra("content", editText.getEditableText().toString());
                          intent.putExtra("fileName", fileName);
                          startActivity(intent);
                          msg.what = 2;
                          btnHandler.sendMessage(msg);
//                          startActivityForResult(intent, btn_id);
                      }else{
                          //分享
                          RenrenOAuth renren = new RenrenOAuth();
                          String renrenResult = renren.shareGif(userInfo.getTokenRenren(),editText.getEditableText().toString(),fileName);
                          if(!renrenResult.equals("")){
                              //分享成功
                              msg.what = 1;
                              btnHandler.sendMessage(msg);
                          }
                      }
                            break;
                        case 1:
                            if(userInfo.getTokenSina() == null || userInfo.getTokenSecretSina() == null){
//                                Toast.makeText(ShareActivity.this, "新浪微博新用户，将去进行认证", Toast.LENGTH_LONG).show();
                                Log.d("微博新用户，将去进行认证");
                                sinaOAuth.RequestAccessToken(ShareActivity.this, callBackUrl);
                                msg.what = 2;
                                btnHandler.sendMessage(msg);
                            }else{
                                //分享
                               String sinaResult = sinaOAuth.shareGif(editText.getEditableText().toString(),"tmp.jpg",fileName);
                               if(sinaResult != null ){
                                   msg.what = 1;
                                   btnHandler.sendMessage(msg);
                               }
                            }
                            break;
                        case 2:
                            if(userInfo.getTokenQQ() == null || userInfo.getTokenQQ().equals("")){
//                                Toast.makeText(ShareActivity.this, "腾讯微博新用户，将去进行认证", Toast.LENGTH_LONG).show();
                                Log.d("腾讯微博新用户，将去进行认证");
                                Intent intent = new Intent(ShareActivity.this,QQShareActivity.class);
                                startActivity(intent);
                                msg.what = 2;
                                btnHandler.sendMessage(msg);
                            }else{
                                //分享
                               String qqResult = QOAuth.shareGif(editText.getEditableText().toString(),userInfo.getTokenQQ(),userInfo.getTokenSecretQQ());
                               if(qqResult != null){
                                   msg.what = 1;
                                   btnHandler.sendMessage(msg);
                               }
                            }
                            break;
                    }
                }else{
                }
            }
        }
    

    private void setUpViews() {
        
        AnimationUtils.loadAnimation(this, R.anim.animation_in);
        AnimationUtils.loadAnimation(this,R.anim.animtation_out);
        
        btns = (View)this.findViewById(R.id.share_btns);
        btn_ok = (Button)this.findViewById(R.id.share_btn_ok);
        virtualLayout = (View)this.findViewById(R.id.share_virtualLayout);
        
        btn_renren = (ImageButton)this.findViewById(R.id.share_btn_renren);
        btn_sina = (ImageButton)this.findViewById(R.id.share_btn_sina);
        btn_qq = (ImageButton)this.findViewById(R.id.share_btn_qq);
        
//        animations = (AnimationDrawable) progressImage.getBackground();
//        animations.start();
        
        editText = (EditText)this.findViewById(R.id.share_edit);
        wordText = (TextView)this.findViewById(R.id.share_text_word);
        gifView = (GifView)this.findViewById(R.id.share_gifView);
        progressBar = (ProgressBar)this.findViewById(R.id.share_progressBar);
        
        translateAnimation = new TranslateAnimation(400,200,0,0);
        translateAnimation.setDuration(700);
        
        btn_animation = new BtnAnimation(300);
    }

    /**
     * 设置各种监听器
     */
    private void setUpListeners() {
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!btn_ok_clicked){
                    showShareBts();
                }else{
                    hindShareBts();
                }
                btn_ok_clicked = !btn_ok_clicked;
            }
        });
        
        btn_renren.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                btn_id = 0;
                Message msg = btnHandler.obtainMessage();
                msg.what = 0;
                btnHandler.sendMessage(msg);
            }
        });
        
        btn_sina.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                btn_id = 1;
                Message msg = btnHandler.obtainMessage();
                msg.what = 0;
                btnHandler.sendMessage(msg);
            }
        });
        
        btn_qq.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                //TODO:腾讯分享失败准备近期做出来
                btn_id = 2;
                Message msg = btnHandler.obtainMessage();
                msg.what = 0;
                btnHandler.sendMessage(msg);
            }
        });
        
        editText.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
  
            }
            @Override
            public void afterTextChanged(Editable s) {
                int len = s.toString().length();
                wordText.setText((144-len)+"");
            }
            
        });
    }
    
    /**
     * 打开分享btns
     */
    protected void hindShareBts() {
        // TODO Auto-generated method stub
        Animation alphaAnimation = new AlphaAnimation(1,0);
        alphaAnimation.setDuration(1000);
        virtualLayout.startAnimation(alphaAnimation);
        btns.startAnimation(alphaAnimation);
        btns.setVisibility(View.GONE);
        virtualLayout.setVisibility(View.GONE);
        btn_ok.setText("上传");
    }

    /**
     * 打开分享btns
     */
    protected void showShareBts() {
        // TODO Auto-generated method stub
        btns.setVisibility(View.VISIBLE);
        btn_renren.setVisibility(View.VISIBLE);
        btn_qq.setVisibility(View.VISIBLE);
        btn_sina.setVisibility(View.VISIBLE);
        virtualLayout.setVisibility(View.VISIBLE);
        Animation translateAnimation = new TranslateAnimation(0.0f,0.0f,-150f,0.0f);
        Animation alphaAnimation = new AlphaAnimation(0.0f,1);
        translateAnimation.setDuration(1000);
        translateAnimation.setInterpolator(AnimationUtils.loadInterpolator(ShareActivity.this,
                android.R.anim.bounce_interpolator));
        alphaAnimation.setDuration(1000);
        virtualLayout.startAnimation(alphaAnimation);
        btns.startAnimation(translateAnimation);
        btn_ok.setText("取消");
    }
    /**
     * 将打开分享btns
     */
    private boolean isBtnShowing = true;
    
    protected void toggleBtns() {
        if(isBtnShowing){
            btn_renren.setVisibility(View.VISIBLE);
            btn_qq.setVisibility(View.VISIBLE);
            btn_sina.setVisibility(View.VISIBLE);
        }else{
            btn_renren.setVisibility(View.GONE);
            btn_qq.setVisibility(View.GONE);
            btn_sina.setVisibility(View.GONE);
        }
        isBtnShowing = ! isBtnShowing;
    }

    /**
     * 点击按钮之后显示
     */
    private Handler btnHandler;
    
    /**
     * 人人认证的时候不需要onNewIntent，所以用这个变量来区分要不要在onNewIntent里面获取数据
     */
    private static boolean RENREN_REQUEST = false;
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent come back");
        if(true == RENREN_REQUEST){
            
        }else{
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
//            SortedSet<String> user_id= sinaOAuth.httpOauthprovider.getResponseParameters().get("user_id");
//            String userId=user_id.first();
            //token
            String userKey = sinaOAuth.httpOauthConsumer.getToken();
            userInfo.setTokenSina(userKey);
            //tokenSecret
            String userSecret = sinaOAuth.httpOauthConsumer.getTokenSecret();
            userInfo.setTokenSecretSina(userSecret);
            Toast.makeText(getApplicationContext(), userKey+" --- " +userSecret, Toast.LENGTH_LONG).show();
            if(fileName != null){
                String sinaResult = sinaOAuth.shareGif(editText.getEditableText().toString(),"tmp.jpg",fileName);
                if(sinaResult != null ){
                    Message msg = btnHandler.obtainMessage();
                    msg.what = 1;
                    btnHandler.sendMessage(msg);
                }
            }
        }
        }
}
