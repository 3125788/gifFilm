package com.wz.gif.share;

import com.wz.gif.util.FileUtil;
import com.wz.gif.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class SinaOAuth {
    public CommonsHttpOAuthConsumer httpOauthConsumer;
    public OAuthProvider httpOauthprovider;
    public String consumerKey = "3658466486";
    public String consumerSecret ="520a3ae28076520d60119a5f3812105c";
    public static final String HTTPGET="GET";
    public static final String HTTPPOST="POST";
    
    public String authUrl;
    
    private UserInfo userInfo;
    
    public static final String BOUNDARY = "7cd4a6d158c";
    public static final String MP_BOUNDARY = "--" + BOUNDARY;
    public static final String END_MP_BOUNDARY = "--" + BOUNDARY + "--";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
 
    
    public SinaOAuth(UserInfo user){
        userInfo = user;
        httpOauthConsumer = new CommonsHttpOAuthConsumer(consumerKey,consumerSecret);
        httpOauthprovider = new DefaultOAuthProvider("http://api.t.sina.com.cn/oauth/request_token","http://api.t.sina.com.cn/oauth/access_token","http://api.t.sina.com.cn/oauth/authorize");
    }
    
    /**登录
     * @param activity
     * @param callBackUrl
     * @return
     */
    public Boolean RequestAccessToken(Activity activity,String callBackUrl){
        Log.d("request access token");
        Boolean ret=false;
        try{
            authUrl = httpOauthprovider.retrieveRequestToken(httpOauthConsumer, callBackUrl);
            
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
            ret=true;
        }catch(Exception e){
        }
        return ret;
    }
    
    public void init(){
        httpOauthConsumer = new CommonsHttpOAuthConsumer(consumerKey,consumerSecret);
        httpOauthprovider = new DefaultOAuthProvider("http://api.t.sina.com.cn/oauth/request_token","http://api.t.sina.com.cn/oauth/access_token","http://api.t.sina.com.cn/oauth/authorize");
    
    }
    /**
     * 
     * @param content
     * @param uri
     * @param ips
     */
    public String shareGif(String content, String uri, String fileName) {
        String url = "http://api.t.sina.com.cn/statuses/upload.json";
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("source", consumerKey)); 
        params.add(new BasicNameValuePair("status",content));
        params.add(new BasicNameValuePair("pic","test.jpg"));
        Log.d("token is "+userInfo.getTokenSina());
        HttpResponse response = signRequest(userInfo.getTokenSina(), userInfo.getTokenSecretSina(), url, params,fileName);
        if(response.getStatusLine().getStatusCode() == 200){
            Log.d("send ok");
            return "ok";
        }else{
            Log.d(response.getStatusLine().getStatusCode()+"");
        }
        return null;
    }

    private HttpResponse signRequest(String tokenSina, String tokenSecretSina, String url,
            ArrayList<BasicNameValuePair> params,String fileName) {
        Log.d("signRequest");
        HttpPost post = new HttpPost(url);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*50);//the data must be less than 5M
        byte[] data = null;
        paramsToUpload(bos,params);     
        post.setHeader("Content-Type",
                MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);
//        try {
//            Log.d(ips.read()+" so we can read at signRequest");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //TODO:----------------------------------
        InputStream inputStream = FileUtil.getFileStream(fileName);
        //----------------------------------------------
        gifContentToUpload(bos,inputStream);
        data = bos.toByteArray();
        try {
            bos.close();
            ByteArrayEntity formEntity = new ByteArrayEntity(data);
            post.setEntity(formEntity);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        
        return signHttpRequest(tokenSina,tokenSecretSina,post);
    }

    private HttpResponse signHttpRequest(String tokenSina, String tokenSecretSina, HttpPost post) {
        Log.d("come to post.token:"+tokenSina+" secret "+tokenSecretSina);
        httpOauthConsumer = new CommonsHttpOAuthConsumer(consumerKey,consumerSecret);
        httpOauthConsumer.setTokenWithSecret(tokenSina,tokenSecretSina);
        HttpResponse response = null;
        try {
            httpOauthConsumer.sign(post);
        } catch (OAuthMessageSignerException e) {
            e.printStackTrace();
        } catch (OAuthExpectationFailedException e) {
            e.printStackTrace();
        } catch (OAuthCommunicationException e) {
            e.printStackTrace();
        }
        try {
            response = new DefaultHttpClient().execute(post);
        } catch (ClientProtocolException e) {
            Log.e("error from QOAuth:"+e.toString());
        } catch (IOException e) {
           Log.e("error from QOAuth:"+e.toString());
        }
        if(response == null)    {
            Log.e("response == null");
        }else{
            Log.e(response.toString());
        }
        return response;
    }

    private void gifContentToUpload(OutputStream bos, InputStream ips) {
        StringBuilder  temp = new StringBuilder();
        temp.append(MP_BOUNDARY).append("\r\n");
        temp.append("Content-Disposition: form-data; name=\"pic\"; filename=\"")
                .append("news_image").append("\"\r\n");
        String filetype = "image/png";
        temp.append("Content-Type: ").append(filetype).append("\r\n\r\n");
        byte[] res = temp.toString().getBytes();
        try {
            bos.write(res);
            int ch = 0 ;
            while((ch = ips.read()) != -1){
                bos.write(ch);
            }
//            while((ch = ips.read(tmpByte, 0, 100))>0){
//                Log.d("read byte:"+ch);
//                bos.write(tmpByte, 0, ch);
//            }
            bos.write("\r\n".getBytes());
            bos.write(("\r\n" + END_MP_BOUNDARY).getBytes());
        } catch (Exception e) {
            Log.e("error from gifContentToUpload "+e.toString());
        }
    }

    private void paramsToUpload(ByteArrayOutputStream bos, ArrayList<BasicNameValuePair> params) {
        
        String key = "";
        for(int i=0;i<params.size();i++){
            key = params.get(i).getName();
            StringBuilder temp = new StringBuilder(10);
            temp.setLength(0);
            temp.append(MP_BOUNDARY).append("\r\n");
            temp.append("content-disposition: form-data; name=\"").append(key)
                    .append("\"\r\n\r\n");
            temp.append(params.get(i).getValue()).append("\r\n");
            byte[] res = temp.toString().getBytes();
            try{
                bos.write(res);
            }catch(IOException e){
                Log.e("error from paramsToUpload:"+e.toString() );
            }
        }
    }
}
