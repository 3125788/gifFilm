package com.wz.gif.share;

import com.wz.gif.util.FileUtil;
import com.wz.gif.util.Log;
import com.wz.gif.util.MD5;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;

public class RenrenOAuth {

    private static final String API_URL = "http://api.renren.com/restserver.do";
    private static final String renren_secret = "5836cc3cc5e74495b80c96f7a25f7670";
    
    public static final String USER_AGENT_SDK = System.getProperties().getProperty("http.agent")
    + " Renren_Android_SDK_v3.0_beta";
    
    public void shareRenrenContent(String accessToken,String content) {
        String requestMethod = "status.set";//接口名称
        String v = "1.0";//版本号 
        String url = API_URL;//人人开发平台API服务器的地址
        
        //生成签名
        StringBuilder sb = new StringBuilder();
        accessToken = URLDecoder.decode(accessToken);
        sb.append("access_token=").append(accessToken)
            .append("format=").append("JSON")
            .append("method=").append(requestMethod)
            .append("status=").append(content)
            .append("v=").append(v)
            .append(renren_secret);
        
        String signature = MD5.getMD5(sb.toString());
        
        Log.d("sign is :"+signature);
        //封装参数
        Bundle params = new Bundle();
        params.putString("access_token", accessToken);
        params.putString("method", requestMethod);
        params.putString("v", v);
        params.putString("status", content);
        params.putString("format", "JSON");
        params.putString("sig", signature);
        
        signRequest(url,"POST",params);
    }
    
    public String shareGif(String accessToken,String content,String fileName){
        HttpURLConnection conn = null;
        StringBuilder sb = new StringBuilder();
        accessToken = URLDecoder.decode(accessToken);
        
        sb.append("access_token=").append(accessToken)
            .append("format=").append("JSON")
            .append("method=").append("photos.upload")
            .append("v=").append("1.0")
            .append(renren_secret);
        
        String signature = MD5.getMD5(sb.toString());
        
        Log.d("shareGif sign is :"+signature);
        //设置参数
        Bundle params = new Bundle();
        params.putString("format", "JSON");
        params.putString("access_token", accessToken);
        params.putString("method", "photos.upload");
        params.putString("v", "1.0");
        params.putString("sig", signature);
        
        conn = openUrl(params,fileName);
        String response  = "";
        InputStream is = null;
        try {
            is = conn.getInputStream();
            response = read(is);
        } catch (Exception e) {
            Log.e("error from shareGif:"+e.toString());
        }
        Log.d("sharGif get:"+response);
        return response;
        
    }

    private HttpURLConnection openUrl(Bundle params,String fileName) {
        HttpURLConnection urlConn = null;
        
        //连接获取数据
        try {
            URL url = new URL(API_URL);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setConnectTimeout(5000);//ms
            urlConn.setReadTimeout(5000);//ms
            urlConn.setDoOutput(true);
            
            urlConn.setRequestProperty("connection", "keep-alive");
            
            String boundary = "--------------123";//分割线
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            boundary = "--" + boundary;
            StringBuffer buffer = new StringBuffer();
            if(params != null){
                for(Iterator<String> iter = params.keySet().iterator();iter.hasNext();){
                    String name = iter.next();
                    String value = params.getString(name);
                    buffer.append(boundary + "\r\n");
                    buffer.append("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
                    buffer.append(value);
                    buffer.append("\r\n");
                }
            }
            Log.d("params is :"+buffer);
            
            StringBuilder sbd = new StringBuilder();
            sbd.append(boundary);
            sbd.append("\r\n");
            sbd.append("Content-Disposition: form-data; name=\"" + "upload" + "\"; filename=\""
                    + "gif胶卷.gif" + "\"\r\n");
            sbd.append("Content-Type: " + "image/gif" + "\r\n\r\n");
            Log.d("params1 is :"+sbd);
            
            byte[] fileDiv = sbd.toString().getBytes();
            byte[] endData = ("\r\n" + boundary + "--\r\n").getBytes();
            byte[] ps = buffer.toString().getBytes();
            //获取gif动画的数据
            InputStream inputStream = FileUtil.getFileStream(fileName);
            int ch = 0;

            OutputStream ops = urlConn.getOutputStream();
            ops.write(ps);
            ops.write(fileDiv);
            while((ch = inputStream.read()) != -1){
                ops.write(ch);
            }
            ops.write(endData);
            
            ops.flush();
            ops.close();
            
        } catch (Exception e) {
            Log.d("exception from :shareGIf "+e.toString());
            e.printStackTrace();
        }       
        return urlConn;
    }

    private void signRequest(String url, String method, Bundle params) {
        if(method.endsWith("GET")){
            
        }
        String response = "";
        try{
            Log.d("URL is:"+url);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT_SDK);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(FileUtil.encodeUrl(params).getBytes("UTF-8"));
            
            int responseCode = conn.getResponseCode();
            Log.d("responseCode is:"+responseCode);
            InputStream is = null;
            if(200 == responseCode){
                is = conn.getInputStream();
                response = read(is);
            }else{
                is = conn.getErrorStream();
                response = read(is);
            }
            Log.d("getResponse is :"+response);
        }catch(Exception e){
            Log.d("error renren SignRequest "+e.toString());
        }
    }
    private static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }

}
