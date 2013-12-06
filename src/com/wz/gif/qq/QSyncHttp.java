package com.wz.gif.qq;

import com.wz.gif.util.FileUtil;
import com.wz.gif.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class QSyncHttp {

    private static final int CONNECTION_TIMEOUT = 1000 * 5;//http 连接超时时间
    

    public static final String BOUNDARY = "7cd4a6d158c";
    public static final String MP_BOUNDARY = "--" + BOUNDARY;
    public static final String END_MP_BOUNDARY = "--" + BOUNDARY + "--";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
 
    
    public String httpGet(String url ,String params){
        HttpResponse response = null;
        if( null != params && !params.equals("")){
            url += "?" +params;
        }
        HttpGet get = new HttpGet(url);
        try {
            response = new DefaultHttpClient().execute(get);
        } catch (Exception e) {
            Log.e("error from QhttpGet:"+e.toString());
        }
        if(response == null){
            Log.e("response is null at QSyncHttp");
            return "";
        }
        if(200 == response.getStatusLine().getStatusCode())
        {
            Log.d("responseCode is 200 :"+response.toString());
        }
        return responseToStr(response);
    }

    private String responseToStr(HttpResponse response) {
        if(200 == response.getStatusLine().getStatusCode())
        {
            try{
                InputStream is = response.getEntity().getContent();
                StringBuilder sb = new StringBuilder();
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                for(String line = r.readLine();line != null;line = r.readLine()){
                    sb.append(line);
                }
                is.close();
                String string = sb.toString();
                Log.d("response is :"+string);
                return string;
            }catch (IllegalStateException e) {
                Log.e("error:"+e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("error:"+e.toString());
            } 
        }
        return null;
    }

    public static String shareGif(String urlWithParms,String shareUrl, List<Parameter> params) {
        HttpURLConnection urlConn = null;
        String response = null;
        
        try{
            
        URL url = new URL(shareUrl + "?" +urlWithParms);
        Log.d("qq url is "+ url.toString());
        urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setConnectTimeout(5000);//ms
        urlConn.setReadTimeout(5000);//ms
        urlConn.setDoOutput(true);
        
        urlConn.setRequestProperty("connection", "keep-alive");
        
        String boundary = "--------------123";//分割线
        urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//        urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        
        boundary = "--" + boundary;
        StringBuffer buffer = new StringBuffer();
        if(params != null){
            Collections.sort(params);
            for(Parameter param:params){
                String name = param.getName();
                String value = param.getValue();
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
                + "gif胶卷.gif" + "\"\r\n\r\n");
        sbd.append("Content-Type: " + "image/gif" + "\r\n\r\n");
        Log.d("params1 is :"+sbd);
        
        byte[] fileDiv = sbd.toString().getBytes();
        byte[] endData = ("\r\n--" + boundary + "--\r\n").getBytes();
        byte[] ps = buffer.toString().getBytes();
        //获取gif动画的数据
        ArrayList<InputStream> list = FileUtil.getGIfStream();
        InputStream ips = list.get(0);
        int ch = 0;
        
        OutputStream ops = urlConn.getOutputStream();
        ops.write(ps);
        ops.write(fileDiv);
        while((ch = ips.read()) != -1){
            ops.write(ch);
        }
        ops.write(endData);
        
        ops.flush();
        ops.close();
        InputStream is = urlConn.getInputStream();
        response = read(is);
    }catch (Exception e) {
        Log.d("exception from :shareGIf "+e.toString());
        e.printStackTrace();
    }       
    Log.d(response);
    return response;
    }
    
    private static void gifToUpload(ByteArrayOutputStream bos) {
        StringBuilder  temp = new StringBuilder();
        temp.append(MP_BOUNDARY).append("\r\n");
        temp.append("Content-Disposition: form-data; name=\"pic\"; filename=\"")
                .append("news_image").append("\"\r\n");
        String filetype = "image/gif";
        temp.append("Content-Type: ").append(filetype).append("\r\n\r\n");
        byte[] res = temp.toString().getBytes();
        
      //获取gif动画的数据
      ArrayList<InputStream> list = FileUtil.getGIfStream();
      InputStream ips = list.get(0);
      int ch = 0;

      try {
            while((ch = ips.read()) != -1){
                  bos.write(ch);
              }
            bos.write("\r\n".getBytes());
            bos.write(("\r\n" + END_MP_BOUNDARY).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void paramsToUpload(ByteArrayOutputStream bos, List<Parameter> params) {
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
