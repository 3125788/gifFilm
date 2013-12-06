package com.wz.gif.share;

import com.wz.gif.util.ConstantInfo;
import com.wz.gif.util.Log;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class UserInfo {
    
    private String tokenSina;
    private String secretSina;
    
    private String tokenRenren;
    private String secretRenren;
    
    private String tokenQQ;
    private String secretQQ;
    
    /**
     * 流量以KB的形式保存
     */
    private int dataStream;
    private int lastMonth;
    
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    
    /**
     * UserInfo里面存放了用户登录要用到的token和tokenSecret，
     * 此外还有要做用户当月流量的统计
     * @param context
     */
    public UserInfo(Context context){
        
        preferences = context.getSharedPreferences(ConstantInfo.PREFERENCE_NAME, Activity.MODE_PRIVATE);
        editor  = preferences.edit();
        
        tokenSina = preferences.getString(ConstantInfo.TOKEN_SINA, null);
        secretSina = preferences.getString(ConstantInfo.SECRET_SINA, null);
        
        tokenRenren = preferences.getString(ConstantInfo.TOKEN_RENREN, null);
//        secretRenren = preferences.getString(ConstantInfo.SECRET_RENREN, null);
        
        tokenQQ = preferences.getString(ConstantInfo.TOKEN_QQ, null);
        secretQQ = preferences.getString(ConstantInfo.SECRET_QQ, null);
        
        dataStream = preferences.getInt(ConstantInfo.DATA_STREAM, 0);
        lastMonth = preferences.getInt(ConstantInfo.LAST_MONTH, 0);
    }
    
    public void setTokenSecretQQ(String tokenSecretQQ) {
        editor.putString(ConstantInfo.SECRET_QQ, tokenSecretQQ);
        editor.commit();
        this.secretQQ = tokenSecretQQ;
    }
    
    public String getTokenSecretQQ() {
        return secretQQ;
    }
    
    public void setTokenQQ(String tokenQQ) {
        editor.putString(ConstantInfo.TOKEN_QQ, tokenQQ);
        editor.commit();
        this.tokenQQ = tokenQQ;
    }
    
    public String getTokenQQ() {
        return tokenQQ;
    }
    
    public void setTokenSecretRenren(String tokenSecretRenren) {
        this.secretRenren = tokenSecretRenren;
    }
    
    public String getTokenSecretRenren() {
        return secretRenren;
    }
    
    public void setTokenRenren(String tokenRenren) {
        editor.putString(ConstantInfo.TOKEN_RENREN, tokenRenren);
        editor.commit();
        this.tokenRenren = tokenRenren;
    }
    
    public String getTokenRenren() {
        return tokenRenren;
    }
    
    public void setTokenSecretSina(String tokenSecretSina) {
        editor.putString(ConstantInfo.SECRET_SINA, tokenSecretSina);
        editor.commit();
        this.secretSina = tokenSecretSina;
    }
    
    public String getTokenSecretSina() {
        return secretSina;
    }
    
    public void setTokenSina(String tokenSina) {
        editor.putString(ConstantInfo.TOKEN_SINA, tokenSina);
        editor.commit();
        this.tokenSina = tokenSina;
    }
    
    public String getTokenSina() {
        return tokenSina;
    }

    public void setData_stream(long data) {
        //TODO：这里还要判断是月份
        Date date = new Date(System.currentTimeMillis());
        int month = date.getMonth();
        if(lastMonth != month ){
            this.dataStream = 0;
            lastMonth = month;
            editor.putInt(ConstantInfo.LAST_MONTH, lastMonth);
        }
        this.dataStream += data;
        Log.d("data is "+this.dataStream);
        editor.putInt(ConstantInfo.DATA_STREAM, this.dataStream/1024);
        editor.commit();
    }

    public int getData_stream() {
        return dataStream;
    }
    
}
