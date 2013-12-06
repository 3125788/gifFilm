package com.wz.gif.qq;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMAC_SHA1  
{  
    private static final String MAC_NAME = "HmacSHA1";  
    private static final String ENCODING = "US-ASCII";  
  
    /** 
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名 
     *  
     * @param encryptText 
     *            被签名的字符串 
     * @param encryptKey 
     *            密钥 
     * @return 
     * @throws NoSuchAlgorithmException 
     * @throws UnsupportedEncodingException 
     * @throws InvalidKeyException 
     * @see <a href = 
     *      "http://tools.ietf.org/html/draft-hammer-oauth-10#section-3.4.2">HMAC-SHA1</a> 
     */  
    public static byte[] macSHA1Encrypt(String encryptText, String encryptKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException  
    {  
        Mac mac = Mac.getInstance(MAC_NAME);  
        SecretKeySpec spec = new SecretKeySpec(encryptKey.getBytes("US-ASCII"), MAC_NAME);  
        mac.init(spec);  
        byte[] text = encryptText.getBytes(ENCODING);  
        return mac.doFinal(text);  
    }  
}  