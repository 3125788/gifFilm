package com.wz.gif.util;

public class Log {
    
    private static final String GIF = "gif";
    
    public static void d(String msg){
        android.util.Log.d(GIF, msg);
    }
    
    public static void e(String msg){
        android.util.Log.e(GIF, msg);
    }
}
