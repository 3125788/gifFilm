package com.wz.gif.qq;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class QQAuth01 {
    
    private static final String OAuthVersion = "1.0";
    private static final String OAuthConsumerKeyKey = "oauth_consumer_key";
    private static final String OAuthCallbackKey = "oauth_callback";
    private static final String OAuthVersionKey = "oauth_version";
    private static final String OAuthSignatureMethodKey = "oauth_signature_method";
    private static final String OAuthTimestampKey = "oauth_timestamp";
    private static final String OAuthNonceKey = "oauth_nonce";
    private static final String OAuthTokenKey = "oauth_token";
    private static final String oAauthVerifier = "oauth_verifier";
    private static final String HMACSHA1SignatureType = "HmacSHA1";
    private static final String HMACSHA1SignatureType_TEXT = "HMAC-SHA1";
    
    public void test01(String customKey, String customSecret,
            String requestToken, String requestTokenSecrect, String content,
            String fileName){
        List<Parameter> files = new ArrayList<Parameter>();
        
        String url = "http://open.t.qq.com/api/t/add_pic";
        String httpMethod = "POST";
        
        files.add(new Parameter("pic",fileName));
        List<Parameter> params = new ArrayList<Parameter>();
        try {
            params .add(new Parameter("content",new String(content.getBytes("UTF-8"))));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.add(new Parameter("format","json"));
        params.add(new Parameter("clientip","192.13.2.1"));
        
        test02(url, httpMethod,  customKey,  customSecret,
                 requestToken,  requestTokenSecrect, params,
                files);

    }

    private void test02(String url, String httpMethod, String customKey, String customSecret,
            String requestToken, String requestTokenSecrect, List<Parameter> params,
            List<Parameter> files) {
        
        StringBuffer sbQueryString = new StringBuffer();
        
        String oauthUrl = test03(url, httpMethod, customKey,
                customSecret, requestToken, requestTokenSecrect,
                params, sbQueryString);
        
        String queryString = sbQueryString.toString();
        sendData(oauthUrl,queryString ,files);
    }

    private void sendData(String oauthUrl, String queryString, List<Parameter> files) {
    }

    private String test03(String url, String httpMethod, String customKey, String customSecret,
            String requestToken, String requestTokenSecrect, List<Parameter> params,
            StringBuffer queryString) {
        String parameterString = normalizeRequestParameters(params);
        
        String urlWithQParameter = url;
        if (parameterString != null && !parameterString.equals("")) {
            urlWithQParameter += "?" + parameterString;
        }
        URL aUrl = null;
        try {
            aUrl = new URL(urlWithQParameter);
        } catch (MalformedURLException e) {
            System.err.println("URL parse error:" + e.getLocalizedMessage());
        }
        String nonce = getNonce();
        String timeStamp = getTimeStamp();
        

        params.add(new Parameter(OAuthVersionKey, OAuthVersion));
        params.add(new Parameter(OAuthNonceKey, nonce));
        params.add(new Parameter(OAuthTimestampKey, timeStamp));
        params.add(new Parameter(OAuthSignatureMethodKey,
                HMACSHA1SignatureType_TEXT));
        params.add(new Parameter(OAuthConsumerKeyKey, customKey));
        params.add(new Parameter(OAuthTokenKey, requestToken));
        
        StringBuffer normalizedUrl = new StringBuffer();
        String signature = generateSignature(aUrl, customSecret, requestTokenSecrect,
                httpMethod, params, normalizedUrl, queryString);
        
        queryString.append("&oauth_signature=");
        queryString.append(encode(signature));
        
        return normalizedUrl.toString();
    }

    private String generateSignature(URL url, String customSecret, String requestTokenSecrect,
            String httpMethod, List<Parameter> params, StringBuffer normalizedUrl,
            StringBuffer queryString) {
        String signatureBase = generateSignatureBase(url, httpMethod,
                params, normalizedUrl, queryString);
        byte[] oauthSignature = null;
        try {
            Mac mac = Mac.getInstance(HMACSHA1SignatureType);
            String oauthKey = encode(customSecret)
                    + "&"
                    + ((requestTokenSecrect == null || requestTokenSecrect.equals("")) ? ""
                            : encode(requestTokenSecrect));
            SecretKeySpec spec = new SecretKeySpec(
                    oauthKey.getBytes("US-ASCII"), HMACSHA1SignatureType);
            mac.init(spec);
            oauthSignature = mac.doFinal(signatureBase.getBytes("US-ASCII"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return Base64.encode(oauthSignature);
    }

    private String generateSignatureBase(URL url, String httpMethod, List<Parameter> params,
            StringBuffer normalizedUrl, StringBuffer queryString) {
        normalizedUrl.append(url.getProtocol());
        normalizedUrl.append("://");
        normalizedUrl.append(url.getHost());
        if ((url.getProtocol().equals("http") || url.getProtocol().equals(
                "https"))
                && url.getPort() != -1) {
            normalizedUrl.append(":");
            normalizedUrl.append(url.getPort());
        }
        normalizedUrl.append(url.getPath());
        queryString.append(formEncodeParameters(params));

        StringBuffer signatureBase = new StringBuffer();
        signatureBase.append(httpMethod.toUpperCase());
        signatureBase.append("&");
        signatureBase.append(encode(normalizedUrl.toString()));
        signatureBase.append("&");
        signatureBase.append(encode(queryString.toString()));
        return signatureBase.toString();
    }

    private Object formEncodeParameters(List<Parameter> params) {
        List<Parameter> encodeParams = new ArrayList<Parameter>();
        for (Parameter a : params) {
            encodeParams.add(new Parameter(a.getName(), encode(a.getValue())));
        }

        return normalizeRequestParameters(encodeParams);
    }

    private String getNonce() {
        Random random = new Random();  
        // 产生123400至9999999随机数  
        String result = String.valueOf(random.nextInt(9876599) + 123400);  
        return result;
    }

    private String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);  
     }

    private String normalizeRequestParameters(List<Parameter> params) {
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
        return urlBuilder.toString();
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
}
