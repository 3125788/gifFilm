package com.wz.gif;

import com.wz.gif.encoder.JpgToGif;
import com.wz.gif.util.ConstantInfo;
import com.wz.gif.util.Log;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.util.ArrayList;

public class GifMaker extends Activity{

    private ImageView imageView;
    private SeekBar seekBar;
    private Button btn_cancle;
    private Button btn_share;
    private Button btn_ok;
    
    private ArrayList<Bitmap> bitmaps;
    
    private int interval = 500;
    private DrawThread drawThread ;
    private ToGifThread toGifThread;
    private JpgToGif toGif;
    private ProgressDialog progressDialog;
    
    private boolean isRun = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gif_maker);
        MyApp app = (MyApp)getApplication();
        bitmaps = new ArrayList<Bitmap>();
        int[] selectArray = app.getSelectArray();
        for(int i = 0 ;i<selectArray.length;i++){
            if(selectArray[i] == 1)
                bitmaps.add(app.bitmaps.get(i));
        }
        Log.d("选择了"+ bitmaps.size()+" 张图");
        
        setUpViews();
        setUpListeners();
        getSupportSurface();
        
        //TODO:
//        bitmaps = ConstantInfo.bitmaps;
        Log.d("bitmap has "+ bitmaps.size());
        imageView.setImageBitmap(bitmaps.get(0));
        drawThread = new DrawThread();
        drawThread.start();
        toGifThread = new ToGifThread();
        toGif = new JpgToGif();
        progressDialog = new ProgressDialog(GifMaker.this);

    }
    
    /**
     * 根据不同的分辨率选择合适的显示大小
     */
    private void getSupportSurface() {
        // TODO Auto-generated method stub
        if(ConstantInfo.smallScreen){
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(210, 280);
            imageView.setLayoutParams(params); 
        }
    }
    private void setUpViews() {
        imageView = (ImageView)this.findViewById(R.id.maker_surface_image);
        
        seekBar = (SeekBar)this.findViewById(R.id.maker_seekBar);
        
        btn_cancle = (Button)this.findViewById(R.id.maker_btn_delete);
        btn_share = (Button)this.findViewById(R.id.maker_btn_effect);
        btn_ok = (Button)this.findViewById(R.id.maker_btn_ok);
    }

    private void setUpListeners() {
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                interval = progress*9 + 100;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btn_share.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
            }
        });
        
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("please wait");
                progressDialog.setMessage("Making your GIF...");
                progressDialog.show();
                isRun = false;
                toGifThread.start();
            }
        });
    }
    
    private void goToShare(){
        Intent intent = new Intent(GifMaker.this,ShareActivity.class);
        
        intent.putExtra("fileName", fileName);
        startActivity(intent);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause");
        isRun = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRun = true;
    }

    /**
     * 用来表示第几帧图片
     */
    private int index = 0;
    
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = bitmaps.size();
            if(i != 0){
                index = index%i;
            }
            Log.d(index+"");
            imageView.setImageBitmap(bitmaps.get(index));
            imageView.invalidate();
            index++;
        }
        
    };
    
    private void reDraw() {
        if(null != handler){
            Message msg = handler.obtainMessage();
            handler.sendMessage(msg);
        }
    }
    
    class DrawThread extends Thread{

        @Override
        public void run() {
            super.run();
            if(bitmaps.size() == 1){
                reDraw();
                return;
            }
            while(isRun){
                reDraw();
                SystemClock.sleep(interval);
            }
        }
    }

    private String fileName;
    
    class ToGifThread extends Thread{
        @Override
        public void run() {
            super.run();
            fileName = toGif.ToGif(bitmaps, GifMaker.this,interval);
            progressDialog.dismiss();
            goToShare();
        }
    }

}
