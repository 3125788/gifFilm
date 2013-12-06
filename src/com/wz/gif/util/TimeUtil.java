package com.wz.gif.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    
    /**
     * 输出函数运行时间,用法        
     *  long tmie01 = System.currentTimeMillis();
     *  中间运行的函数
        long time02 = System.currentTimeMillis();
     * @param time01,函数开始时间
     * @param time02，函数结束时间
     * @param name 函数明
     */
    public static void logTime(long time01, long time02, String name) {
        Log.d("函数："+name+" 运行了："+(time02-time01)+"ms");
    }
    
    
    public static String getData(String timeStamp){
        try{
            Date date = new Date(Long.valueOf(timeStamp));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = formatter.format(date);
            return time;
        }catch(Exception e){
            Log.e("error from getData "+e.toString());
        }
        return null;
    }
}
