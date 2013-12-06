package com.wz.gif.util;

import android.content.Context;


public class ConstantInfo {
    public static String EFFECT = "none";
    
    public static final String DATA_STREAM = "data_stream";
    public static final String LAST_MONTH = "last_month";
    
    public static final String PREFERENCE_NAME = "UserInfo";
    
    public static final String TOKEN_SINA = "token_sina";
    public static final String SECRET_SINA = "secret_sina";
    
    public static final String TOKEN_RENREN = "token_renren";
    public static final String SECRET_RENREN = "secret_renren";
    
    public static final String TOKEN_QQ = "token_qq";
    public static final String SECRET_QQ = "secret_qq";
    
    public static boolean smallScreen = false;
    
    public static boolean magicMode = false;
    
    /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }
    
}
