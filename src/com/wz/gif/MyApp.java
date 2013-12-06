package com.wz.gif;

import com.wz.gif.util.Log;

import android.app.Application;
import android.graphics.Bitmap;

import java.util.ArrayList;

public class MyApp extends Application{
    ArrayList<Bitmap> bitmaps;
    ArrayList<Bitmap> magicBitmaps;
    
    int imageHeight = 192;
    int imageWidth = 256;
    
    /**
     * 用来表示那些图片被选择了，这样做既可以让用户重新选择图片（因为没有用remove方法），也避免了一些错误
     */
    private  int[] selectArray;
    
    public MyApp(){
        super();
        bitmaps = new ArrayList<Bitmap>();
        magicBitmaps = new ArrayList<Bitmap>();
    }
    
    public void setSelectArray(int[] selectArray){
        this.selectArray = selectArray;
    }
    
    public int[] getSelectArray(){
        return this.selectArray;
    }
    
    public void removeBitmap(int index){
        bitmaps.remove(index);
    }
    //*************************下面是为MagicView准备的******************************************
    public Bitmap getMagicBitmap(int index){
        return bitmaps.get(index);
    }
    
    public void addMagicBitmap(Bitmap bitmap){
        if(magicBitmaps == null){
            magicBitmaps = new ArrayList<Bitmap>();
        }
        Log.d("add magicBitmap");
        magicBitmaps.add(bitmap);
    }

    public void removeAllMagicBitmap() {
        if(magicBitmaps.size() > 0){
            magicBitmaps.clear();
        }
    }
    //*********************************************************************
    
    public Bitmap getBitmap(int index){
        return bitmaps.get(index);
    }
    
    public void addBitmap(Bitmap bitmap){
        if(bitmaps == null){
            bitmaps = new ArrayList<Bitmap>();
        }
        bitmaps.add(bitmap);
    }

    public void removeAllBitmap() {
        if(bitmaps.size() > 0){
            bitmaps.clear();
        }
    }
}
