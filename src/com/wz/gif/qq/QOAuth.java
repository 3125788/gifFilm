package com.wz.gif.qq;

import com.wz.gif.util.Log;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QOAuth {
    public static final String CONSUMER_KEY = "801093644";
    public static final String CONSUMER_SECRET = "4e47ffaa16fb933efd6f39eb44297953";
    public static final String SIGNATURE_METHOD = "HMAC-SHA1";
    
    private String timeStamp = getTimeStamp();
    private String nonce = getNonce(false);
    private String callback = "myapp://QQShareActivity";
    
    public Map<String, String> getOAuthToken(String url){
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter("oauth_consumer_key", CONSUMER_KEY));  
        params.add(new Parameter("oauth_signature_method", SIGNATURE_METHOD));  
        params.add(new Parameter("oauth_timestamp", timeStamp));  
        params.add(new Parameter("oauth_nonce", nonce));  
        params.add(new Parameter("oauth_callback", encode(callback)));  
        params.add(new Parameter("oauth_version", "1.0"));  
        
        String signature = getSignature("GET", url, params, CONSUMER_SECRET, null);  
        Log.d("signature is "+signature);
        params.add(new Parameter("oauth_signature",signature));
        
//        //step 1：构造http请求参数
//        StringBuilder sb = new StringBuilder();
//        sb.append("GET");
//        sb.append("&");
//        sb.append(encode(url));
//        sb.append("&");
//        
//        //所有参数按key进行字典升序排列
//        Collections.sort(params);
//        for(Parameter param:params){
//            String name = encode(param.getName());
//            String value = encode(param.getValue());
//            sb.append(name);
//            sb.append("%3D");//字符 = 
//            sb.append(value);
//            sb.append("%26");// 字符 &
//        }
//        
//        //删除末尾的%26
//        sb.delete(sb.length() -3, sb.length());
//        
//        //step 2:构造密钥
//        String oauthKey = "";
//        if(null == tokenSecret || tokenSecret.equals("")){
//            oauthKey = encode(CONSUMER_SECRET) + "&";
//        }else{
//            oauthKey = encode(CONSUMER_SECRET) + "&" + encode(tokenSecret);
//        }
//        //生成签名值,签名值由两个算法构成：HMAC_SHA1和Base6组成
//        try {
//            byte[] encryptBytes = HMAC_SHA1.macSHA1Encrypt(sb.toString(),oauthKey);
//            String signature = Base64.encode(encryptBytes);
//            params.add(new Parameter("oauth_signature",signature));
//        } catch (Exception e) {
//            Log.e("error form HMAC_SHA1 "+ e.toString());
//        }
        
        StringBuilder urlBuilder = new StringBuilder();
        for(Parameter param : params){
            urlBuilder.append(param.getName());  
            urlBuilder.append("=");  
            urlBuilder.append(param.getValue());  
            urlBuilder.append("&"); 
        }
        urlBuilder.deleteCharAt(urlBuilder.length() - 1);  
        String urlParams = urlBuilder.toString(); 
        QSyncHttp http = new QSyncHttp();
        String response = http.httpGet(url, urlParams);
        Map<String,String> map = splitResponse(response);
//        String oauthToken = map.get("oauth_token");
//        return oauthToken;
        return map;
    }
    

    public Map<String,String> getAccessToken(String url, String httpMethod, String callBack,
            String oauthToken, String oauthSecret, String verify) {
        // 保存参数集合  
        List<Parameter> params = new ArrayList<Parameter>();  
        // 获取时间戳  
        String timestamp = getTimeStamp();  
        // 获取单次值  
        String nonce = getNonce(false);  
        // 添加参数  
        params.add(new Parameter("oauth_consumer_key", CONSUMER_KEY));  
        params.add(new Parameter("oauth_signature_method", SIGNATURE_METHOD));  
        params.add(new Parameter("oauth_timestamp", timestamp));  
        params.add(new Parameter("oauth_nonce", nonce));  
        params.add(new Parameter("oauth_version", "1.0"));  
        if (!TextUtils.isEmpty(callBack))  
        {  
            params.add(new Parameter("oauth_callback", encode(callback)));  
        }  
        //验证码  
        if (!TextUtils.isEmpty(verify))  
        {  
            params.add(new Parameter("oauth_verifier", verify));  
        }  
        //oauthToken  
        if (!TextUtils.isEmpty(oauthToken))  
        {  
            params.add(new Parameter("oauth_token", oauthToken));  
        }  
      
        // 获取签名值  
        String signature = getSignature("GET", url, params, CONSUMER_SECRET, oauthSecret);  
        params.add(new Parameter("oauth_signature", signature));  
      
        // 构造请求参数字符串  
        StringBuilder urlBuilder = new StringBuilder();  
        for (Parameter param : params)  
        {  
            urlBuilder.append(param.getName());  
            urlBuilder.append("=");  
            urlBuilder.append(param.getValue());  
            urlBuilder.append("&");  
        }  
        // 删除最后“&”字符  
        urlBuilder.deleteCharAt(urlBuilder.length() - 1);  
        Log.d("params="+urlBuilder.toString());  
        String urlParams = urlBuilder.toString(); 
        QSyncHttp http = new QSyncHttp();
        String response = http.httpGet(url, urlParams);
        Log.d("getResponse is "+response);
        Map<String,String> map = splitResponse(response);
        return map;
    }
    
    /**
     * 构造密钥
     * @param httpMethod
     * @param url
     * @param params
     * @param consumerSecret
     * @param tokenSecret
     * @return
     */
    private static String getSignature(String httpMethod, String url, List<Parameter> params,
            String consumerSecret, String tokenSecret) {
        //step 1：构造http请求参数
        StringBuilder sb = new StringBuilder();
        sb.append(httpMethod);
        sb.append("&");
        sb.append(encode(url));
        sb.append("&");
        
        //所有参数按key进行字典升序排列
        Collections.sort(params);
        for(Parameter param:params){
            String name = encode(param.getName());
            String value = encode(param.getValue());
            sb.append(name);
            sb.append("%3D");//字符 = 
            sb.append(value);
            sb.append("%26");// 字符 &
        }
        
        //删除末尾的%26
        sb.delete(sb.length() -3, sb.length());
        
        //step 2:构造密钥
        String oauthKey = "";
        if(null == tokenSecret || tokenSecret.equals("")){
            oauthKey = encode(consumerSecret) + "&";
        }else{
            oauthKey = encode(consumerSecret) + "&" + encode(tokenSecret);
        }
        //生成签名值,签名值由两个算法构成：HMAC_SHA1和Base6组成
            byte[] encryptBytes;
            String signature = null;
            try {
                encryptBytes = HMAC_SHA1.macSHA1Encrypt(sb.toString(),oauthKey);
                signature = Base64.encode(encryptBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return signature;
    }


    /**
     * 将返回的参数放入到hashMap中
     * @param response
     * @return
     */
    public static Map<String,String> splitResponse(String response){
        Map<String,String> map = new HashMap<String,String>();
        if(!TextUtils.isEmpty(response)){
            //对“&”进行分隔
            String[] array = response.split("&");
            if(array.length>2){
                String tokenStr = array[0];
                String secretStr = array[1];
                String[] token = tokenStr.split("=");
                if(token.length == 2){
                    map.put("oauth_token", token[1]);
                }
                String[] secret = secretStr.split("=");  
                if (secret.length == 2)  
                {  
                    map.put("oauth_token_secret", secret[1]);  
                }  
            }
        }
        return map;
    }
    
    /**
     * 对参数进行url编码
     * @param s
     * @return
     */
    private static String encode(String s) {

        if (s == null)  
        {  
            return "";  
        }  
        String encoded = "";  
        try  
        {  
            encoded=URLEncoder.encode(s, "US-ASCII");  
        } catch (UnsupportedEncodingException e)  
        {  
            throw new RuntimeException(e.getMessage(), e);  
        }  
        StringBuilder sBuilder =new StringBuilder();  
        for(int i=0;i<encoded.length();i++)  
        {  
            char c = encoded.charAt(i);  
            if (c == '+')  
            {  
                sBuilder.append("%20");  
            }  
            else if (c == '*')  
            {  
                sBuilder.append("%2A");  
            }  
            //URLEncoder.encode()会把“~”使用“%7E”表示，因此在这里我们需要变成“~”  
            else if ((c == '%')&& ((i + 1) < encoded.length())&&((i + 2) < encoded.length())&  
                     (encoded.charAt(i + 1) == '7')&&(encoded.charAt(i + 2) == 'E'))   
            {  
                sBuilder.append("~");  
                i+=2;  
            }  
            else  
            {  
                sBuilder.append(c);  
            }  
        }  
        return sBuilder.toString();  
    }

    /**
     * 得到时间撮
     * @return
     */
    private static String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);  
    }
    
    /**
     * 得到32位随机生成串，与时间撮一起，防止重放
     * @return
     */
    private static String getNonce(boolean is32) {
        Random random = new Random();  
        // 产生123400至9999999随机数  
        String result = String.valueOf(random.nextInt(9876599) + 123400);  
        if (is32)  
        {  
            // 进行MD5加密  
            try  
            {  
                MessageDigest md = MessageDigest.getInstance("MD5");  
                md.update(result.getBytes());  
                byte b[] = md.digest();  
                int i;  
  
                StringBuffer buf = new StringBuffer("");  
                for (int offset = 0; offset < b.length; offset++)  
                {  
                    i = b[offset];  
                    if (i < 0)  
                        i += 256;  
                    if (i < 16)  
                        buf.append("0");  
                    buf.append(Integer.toHexString(i));  
                }  
                result = buf.toString();  
            } catch (NoSuchAlgorithmException e)  
            {  
                e.printStackTrace();  
            }  
        }  
        return result;  
    }


    public static String shareGif(String content, String token, String tokenSecret) {
        String shareUrl = "http://open.t.qq.com/api/t/add_pic";
        List<Parameter> params = new ArrayList<Parameter>();  
        // 获取时间戳  
        String timestamp = getTimeStamp();  
        // 获取单次值  
        String nonce = getNonce(false);  
        // 添加参数  
        params.add(new Parameter("oauth_consumer_key", CONSUMER_KEY));  
        params.add(new Parameter("oauth_signature_method", SIGNATURE_METHOD));  
        params.add(new Parameter("oauth_timestamp", timestamp));  
        params.add(new Parameter("oauth_nonce", nonce));  
        params.add(new Parameter("oauth_token",token)); 

//        String urlParams = shareUrl+"?"+urlBuilder.toString(); 
        try {
            params.add(new Parameter("content",new String(content.getBytes("UTF-8"))));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.add(new Parameter("format","json"));
        params.add(new Parameter("clientip","192.13.2.1"));
        params.add(new Parameter("pic","test.gif"));
        
        String signature = getSignature("POST", shareUrl, params, CONSUMER_SECRET, tokenSecret);  
        params.add(new Parameter("oauth_signature", signature));  
        
        // 构造请求参数字符串  
        StringBuilder urlBuilder = new StringBuilder();
        for (Parameter param : params)  
        {  
            urlBuilder.append(param.getName());  
            urlBuilder.append("=");  
            urlBuilder.append(param.getValue());  
            urlBuilder.append("&");  
        }  
        // 删除最后“&”字符  
        urlBuilder.deleteCharAt(urlBuilder.length() - 1);  
        Log.d("params="+urlBuilder.toString());  
        
        String response = QSyncHttp.shareGif(urlBuilder.toString(),shareUrl,params);
        Log.d(response);
        return response;
    }

}
