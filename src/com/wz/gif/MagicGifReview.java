package com.wz.gif;

import com.wz.gif.util.ConstantInfo;
import com.wz.gif.util.GalleryListener;
import com.wz.gif.util.Log;
import com.wz.gif.view.MagicView;
import com.wz.gif.view.PopupBackImage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import java.util.ArrayList;

public class MagicGifReview extends Activity{
    
    private MagicView magicView;
    private Button btn_ok;
    private Button btn_change_back;
    private SeekBar paint_seekBar;
    
    private ImageView pen_size_image;
    private Button btn_pen;
    private Button btn_eraser;
    private ImageView pen_mask;
    private ImageView eraser_mask;
    
    private MyApp app;
    private ArrayList<Bitmap> bitmaps;
    private PaintThread paintThread;
    
    private Handler handler ;
    private int backgroudIndex = 0;
    
    private Bitmap penSizeImageBmp;//改变画笔大小之后在的图片反馈
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.magic_gif_maker);
        
        init();
        setUpViews();
        setUpListeners();
        getSupportSurface();
        paintThread = new PaintThread();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Intent intent = new Intent(MagicGifReview.this,MagicGifMaker.class);
                startActivity(intent);
            }
            
        };
    }

    private void getSupportSurface() {
        // TODO Auto-generated method stub
        if(ConstantInfo.smallScreen){
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(210, 280);
            magicView.setLayoutParams(params); 
        }
    }

    private void init() {
        app = (MyApp)getApplication();
        bitmaps = new ArrayList<Bitmap>();
        int[] selectArray = app.getSelectArray();
        for(int i = 0 ;i<selectArray.length;i++){
            if(selectArray[i] == 1)
                bitmaps.add(app.bitmaps.get(i));
        }
        penSizeImageBmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.pen_size_image);
    }

    private void setUpViews() {
        magicView = (MagicView)this.findViewById(R.id.magic_image);
        btn_ok = (Button)this.findViewById(R.id.magic_btn_ok);
        btn_change_back = (Button)this.findViewById(R.id.magic_btn_changeback);
        paint_seekBar = (SeekBar)this.findViewById(R.id.magic_paintSize_seekBar);
        
        pen_size_image = (ImageView)this.findViewById(R.id.magic_pen_size_image);
        
        btn_pen = (Button)this.findViewById(R.id.magic_btn_pen);
        btn_eraser = (Button)this.findViewById(R.id.magic_btn_eraser);
        pen_mask = (ImageView)this.findViewById(R.id.magic_pen_mask);
        eraser_mask = (ImageView)this.findViewById(R.id.magic_eraser_mask);
        
        Drawable drawable = new BitmapDrawable(app.bitmaps.get(0));  
        int height = ConstantInfo.dip2px(this, 320);
        int width = ConstantInfo.dip2px(this, 240);
        Log.d("width "+ width + " "+height);
        magicView.init(width, height);
        magicView.setBackgroundDrawable(drawable);
    }

    private void setUpListeners() {
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(paintThread.isAlive()){
                }else{
                    if(magicView.havePained){
                        paintThread.start();
                        paintThread = new PaintThread();
                    }else{
                        Toast.makeText(getApplicationContext(), "您还没有图选区域", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        
        btn_change_back.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                PopupBackImage pop = new PopupBackImage(MagicGifReview.this,app,btn_change_back);
                pop.setGalleryListener(new GalleryListener(){
                    @Override
                    public void magicBackChanged(int position) {
                        backgroudIndex = position;
                        Drawable drawable = new BitmapDrawable(app.bitmaps.get(backgroudIndex));  
                        magicView.setBackgroundDrawable(drawable);
                        magicView.clearTracks();
                    }
                    
                });
                pop.show();
            }
        });
        
        paint_seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int penSize = progress + 10;
                magicView.setPenSize(penSize);
                changePenSizeImaeg(penSize);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(alphaAnimation == null){
                    alphaAnimation = new AlphaAnimation(1,0);
                    alphaAnimation.setDuration(550);
                }
                pen_size_image.startAnimation(alphaAnimation);
                pen_size_image.setVisibility(View.INVISIBLE);
            }
        });
        
        btn_pen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                magicView.setPenType(0);
                pen_mask.setVisibility(View.VISIBLE);
                eraser_mask.setVisibility(View.INVISIBLE);
            }
        });
        
        btn_eraser.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                magicView.setPenType(1);
                pen_mask.setVisibility(View.INVISIBLE);
                eraser_mask.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private AlphaAnimation alphaAnimation;
    
    protected void changePenSizeImaeg(int penSeekSize) {
        // TODO Auto-generated method stub
        pen_size_image.setVisibility(View.VISIBLE);
        final int penImageSize = ConstantInfo.dip2px(this, 42);
        int penImageCurrentSize = penImageSize * penSeekSize /30;
        Bitmap b = Bitmap.createScaledBitmap(penSizeImageBmp, penImageCurrentSize, penImageCurrentSize, true);
        pen_size_image.setImageBitmap(b);
    }
    private class PaintThread extends Thread{
        @Override
        public void run() {
            super.run();
            if(app.magicBitmaps.size()>0){
                app.removeAllMagicBitmap();
            }
            int width = app.bitmaps.get(0).getWidth();
            int height = app.bitmaps.get(0).getHeight();
//            int width = 240;
//            int height = 320;
            Bitmap srcBmp = app.bitmaps.get(backgroudIndex);
            
            int[] a = new int[width * height];
            int[] b = new int[width * height];
            int[] c = new int[width * height];
            srcBmp.getPixels(a, 0, width, 0, 0, width, height);
            
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(magicView.mBitmap, width, height, true);
            
//            magicView.mBitmap.getPixels(backPixels, 0, width, 0, 0, width, height);
            scaledBitmap.getPixels(c, 0, width, 0, 0, width, height);
            int[] selectArray = app.getSelectArray();
            for(int k = 0 ;k<app.getSelectArray().length;k++){
                if(selectArray[k] == 1){
                    Bitmap layBmp = app.bitmaps.get(k);
                    layBmp.getPixels(b, 0, width, 0, 0, width, height);
//                    layBmp.recycle();
                    int pos = 0;
                    for(int i =0;i<height;i++){
                        for(int j= 0 ;j<width ;j++){
//                            if(magicView.rects.get(0).contains(j, i)){
                             pos = i*width +j;
                             if(c[pos] != 0){
                                 a[pos] = b[pos];
//                                 Log.d(backPixels[pos] + "");
//                             }
                            }
                        }
                    }
                  Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
                  bitmap.setPixels(a, 0, width, 0, 0, width, height);
                  app.addMagicBitmap(bitmap);
                }
            }
            //            Log.d("time spends "+(end - start));
            Message msg = handler.obtainMessage();
            handler.sendMessage(msg);
        }
    }
}
