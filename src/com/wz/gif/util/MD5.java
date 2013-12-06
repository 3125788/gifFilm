package com.wz.gif.util;

import java.security.MessageDigest;

/**
 * MD5工具类，用于产生字符串的MD5值
 * @author wuzhi
 *
 */
public class MD5 {

    public static String getMD5(String sb) {
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            
            byte[] byteArray = sb.getBytes("ISO-8859-1");
            byte[] md5Bytes = md5.digest(byteArray);
            
            StringBuilder hexValue = new StringBuilder();
            for(int i = 0 ;i<md5Bytes.length;i++){
                int val = ((int)md5Bytes[i]) & 0xff;
                if(val < 15){
                    hexValue.append("0");
                }                    
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        }catch(Exception e){
            Log.e("error from getMD5 "+e.toString());
        }
        return null;
    }
}
