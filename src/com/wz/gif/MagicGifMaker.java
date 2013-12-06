package com.wz.gif;

import com.wz.gif.encoder.JpgToGif;
import com.wz.gif.util.ConstantInfo;
import com.wz.gif.util.Log;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MagicGifMaker extends BaseActivity{

    private ImageView imageView;
    private Button btnOk;
    private Button btnCancle;
    private SeekBar intervalSeekBar;
    private int interval = 100;
    
    private ProgressDialog progressDialog;
    
    private MyApp app;
    private Handler handler;
    private DrawThread drawThread;
    private JpgToGif toGif;
    private ToGifThread toGifThread;
    
    @Override
    public int getContentViewId() {
        return R.layout.gif_maker;
    }

    @Override
    public void setUpViews() {
        imageView = (ImageView)this.findViewById(R.id.maker_surface_image);
        btnOk = (Button)this.findViewById(R.id.maker_btn_ok);
        btnCancle = (Button)this.findViewById(R.id.maker_btn_delete);
        intervalSeekBar = (SeekBar)this.findViewById(R.id.maker_seekBar);
        
        progressDialog = new ProgressDialog(MagicGifMaker.this);
        getSupportSurface();
    }
    
    private void getSupportSurface() {
        // TODO Auto-generated method stub
        if(ConstantInfo.smallScreen){
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(210, 280);
            imageView.setLayoutParams(params); 
        }
    }

    @Override
    public void setUpListeners() {
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("please wait");
                progressDialog.setMessage("Making your GIF...");
                progressDialog.show();
                toGifThread.start();
            }
        });
        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        intervalSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
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
    }

    //表示当前显示的是第几帧
    private int index = 0;
    
    @Override
    public void _onCreate() {
        app = (MyApp)getApplication();
        Log.d("modifyBitmaps " +app.magicBitmaps.size());
        toGif = new JpgToGif();
        toGifThread  = new ToGifThread();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int i = app.magicBitmaps.size();
                if(i>0){
                    index = index % i;
                    imageView.setImageBitmap(app.magicBitmaps.get(index));
                    imageView.invalidate();
                    index++;
                }
            }
        };
        
        if(app.magicBitmaps.size()>0){
            drawThread = new DrawThread();
            drawThread.start();
        }
    }
    private String fileName;
    
    private void goToShare(){
        Intent intent = new Intent(MagicGifMaker.this,ShareActivity.class);
        
        intent.putExtra("fileName", fileName);
        startActivity(intent);
    }
    
    class DrawThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
                SystemClock.sleep(interval);
                if(null != handler){
                    Message msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
            }
        }
    }
    
    class ToGifThread extends Thread{
        @Override
        public void run() {
            super.run();
            fileName = toGif.ToGif(app.magicBitmaps, MagicGifMaker.this,interval);
            progressDialog.dismiss();
            goToShare();
        }
    }
}
