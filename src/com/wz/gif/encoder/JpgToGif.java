package com.wz.gif.encoder;

import com.wz.gif.util.Log;
import com.wz.gif.util.TimeUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.util.ArrayList;

public class JpgToGif {
    
    //synchronized,pic[]为图片集合的地址，newPic为新生成的gif图片位置
      public String ToGif(ArrayList<Bitmap> bitmaps,Context context,int interval) {   
            try {   
                String newGifName = Environment.getExternalStorageDirectory()+"/GIFjiaojuan/"+ System.currentTimeMillis()+".gif"; 
//                String newGifName = "/sdcard/test.gif";
                Log.d("file is in :"+newGifName);
                AnimatedGifEncoder e = new AnimatedGifEncoder();    
                e.setRepeat(0);   
                e.start(newGifName);   
                long time01 = System.currentTimeMillis();
                for (int i = 0; i < bitmaps.size(); i++) {   
                    e.setDelay(interval); // 设置播放的延迟时间   
                    e.addFrame(bitmaps.get(i),i); // 添加到帧中   
                }   
                e.finish();//刷新任何未决的数据，并关闭输出文件   
                TimeUtil.logTime(time01,System.currentTimeMillis(),JpgToGif.class.getName());
                return newGifName;
            } catch (Exception e) {   
                e.printStackTrace();   
            }   
            return null;
        } 
    }
