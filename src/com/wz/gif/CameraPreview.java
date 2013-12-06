package com.wz.gif;

import com.wz.gif.util.ConstantInfo;
import com.wz.gif.util.Log;
import com.wz.gif.view.PopupMenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

public class CameraPreview extends Activity implements SurfaceHolder.Callback{
    
    private Button btn_ok;
    private Button btn_cancle;
    private Button btn_effect;
    private SurfaceView surfaceView;
    
    private static Camera camera;
    private SurfaceHolder surfaceHolder;
    private static Camera.Parameters cameraParams;
    
    private boolean previewRunning = false;
    
    private PreviewCallback previewCallback;
    private ProgressBar progressBar;
    private int progressValue = 1;
    private int progressMax;
    private MyApp app ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera_preview);
        app = (MyApp)getApplication();
        app.removeAllBitmap();

        setUpViews();
        setUpListeners();
    }
    
    private long lastTime;
    private long currentTime;
    

    private void setUpViews() {
        
        progressBar = (ProgressBar)this.findViewById(R.id.preview_progress);
        progressMax = progressBar.getMax();
        
        surfaceView = (SurfaceView)this.findViewById(R.id.preview_surface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        
        btn_ok = (Button)this.findViewById(R.id.preview_btn_ok);
        btn_cancle = (Button)this.findViewById(R.id.preview_btn_delete);
        btn_effect = (Button)this.findViewById(R.id.preview_btn_effect);
        btn_ok.setEnabled(false);
        btn_cancle.setEnabled(false);
        btn_ok.setBackgroundResource(R.drawable.finish_icon_disabled);
        btn_cancle.setBackgroundResource(R.drawable.delete_icon_disabled);
        
        previewCallback = new PreviewCallback(){

            @Override
            public void onPreviewFrame(byte[] data, Camera camera01) {
                if(startPreview){
                    if(data != null){
                        Camera.Parameters p = camera01.getParameters();
//                        p.setPreviewFrameRate(4);//设置每秒取的帧数
//                        p.setPreviewSize(192, 256);
//                        camera.setParameters(p);
                        int width = p.getPreviewSize().width;
                        int height = p.getPreviewSize().height;
                        Log.d("preview width: "+ width +" height:"+ height);
                        if(width > 320 || height >240){
                            //TODO:有些手机显示的比较大
                            Toast.makeText(CameraPreview.this, "width "+width +" height" +height, Toast.LENGTH_SHORT).show();
                            
                            //TODO:4月15号
//                            p.setPreviewSize(app.imageWidth, app.imageHeight);
//                            camera.setParameters(p);
                        }
                        progressBar.setProgress(progressValue);
                        if(progressValue > progressMax){
                            camera.stopPreview();
                            finish();
                            Intent intent = new Intent(CameraPreview.this,SelectBitmap.class);
                            Log.d("start SelectBitmap 1 "+progressValue + "max " +progressMax);
                            startActivity(intent);
                           }else{
                               progressValue++;
                           }
                        lastTime = currentTime;//现在的currentTime 记录的就是上次time
                        currentTime = System.currentTimeMillis();
                        Log.d(" 间隔 " + (currentTime - lastTime) + "ms");
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int[] rgbArray = new int[width*height*3];//设置空的图像RGB数组，用于接收转换后数据
                        decodeYUV420SP(rgbArray,data,width,height);
                        Bitmap bitmap = Bitmap.createBitmap(rgbArray, width, height, Bitmap.Config.ARGB_8888);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap bitmap01 = Bitmap.createBitmap(bitmap, 0, 0, width, height,matrix,true);
                        app.addBitmap(bitmap01);
                        bitmap.recycle();
//                        bitmap01.recycle();
                    }
                }
            }
            
        };
    }
    
    /**
     * 将yuv数据转换为rgb数据
     * @param rgbArray
     * @param yuvArray
     * @param w
     * @param h
     */
    protected void decodeYUV420SP(int[] rgbArray, byte[] yuvArray, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
        for (int i = 0; i < width; i++, yp++) {
            int y = (0xff & ((int) yuvArray[yp])) - 16;
            if (y < 0) y = 0;
            if ((i & 1) == 0) {
                v = (0xff & yuvArray[uvp++]) - 128;
                u = (0xff & yuvArray[uvp++]) - 128;
            }

            int y1192 = 1192 * y;
            int r = (y1192 + 1634 * v);
            int g = (y1192 - 833 * v - 400 * u);
            int b = (y1192 + 2066 * u);

            if (r < 0) r = 0; else if (r > 262143) r = 262143;
            if (g < 0) g = 0; else if (g > 262143) g = 262143;
            if (b < 0) b = 0; else if (b > 262143) b = 262143;

            rgbArray[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
        }
    }

    private boolean startPreview = false;
    /**
     * 处理各种click事件
     */ 
    private void setUpListeners() {
        
        surfaceView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                    btn_ok.setEnabled(true);
                    btn_cancle.setEnabled(true);
                    if( !startPreview){
                        btn_ok.setBackgroundResource(R.drawable.finish_icon);
                        btn_cancle.setBackgroundResource(R.drawable.delete_icon);
                    }
                    startPreview = !startPreview;
            }
        });
        
        btn_ok.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                //在intent之间传递复杂数据类型，这里用将bitmap数据保存在MyApp里
                if( !startPreview){
                    Intent intent = new Intent(CameraPreview.this,SelectBitmap.class);
                    Log.d("start selectBitmap 2");
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(CameraPreview.this, "请先暂停照相机", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(CameraPreview.this)
                    .setTitle("友情提示")
                    .setMessage("是否重新开始制作?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            btn_ok.setEnabled(false);
                            btn_cancle.setEnabled(false);
                            btn_ok.setBackgroundResource(R.drawable.finish_icon_disabled);
                            btn_cancle.setBackgroundResource(R.drawable.delete_icon_disabled);
                            progressBar.setProgress(0);
                            progressValue = 0 ;
                            app.removeAllBitmap();
                            camera.stopPreview();
                            camera.startPreview();
                        }
                    })
                    .setNegativeButton("NO", null)
                    .show();
            }
        });
        
        btn_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popMenu = new PopupMenu(CameraPreview.this);
                popMenu.show();
            }
        });
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("surfaceCreated");
        try{
            if(camera != null){
                camera.stopPreview();
                camera.release();
                camera=null;
            }else{
                camera = Camera.open();
//                setCameraOrientation();
                camera.setDisplayOrientation(90);
                
            }
        }catch(Exception e){
            Log.d("error from surfaceCreated " + e.toString());
            Toast.makeText(getApplicationContext(), "调用照相机错误：（", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        Log.d("surfaceChanged");
        if(previewRunning){
            camera.stopPreview();
        }
        if(camera == null){
            surfaceCreated(null);
        }            
        cameraParams = camera.getParameters();
        cameraParams.setColorEffect(ConstantInfo.EFFECT);
        Log.d("width "+width+ " height:"+height);
        setSupportedSize();
//        cameraParams.setPreviewFrameRate(4);//设置每秒多少帧
//        cameraParams.setPreviewSize(width, height);
        try{
            camera.setParameters(cameraParams);
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(previewCallback);
        }catch(Exception e){
            Log.e("error from surfaceChanged "+e.toString());
        }
        camera.startPreview();
        camera.autoFocus(null);
        previewRunning = true;
    }

    /**
     * 设置合适的surfaceSize
     */
    private void setSupportedSize() {
//        TODO:修改相片的大小？获取系统支持的previewSize，然后去显示，这里可以有两个大小，200*266 --- 240*320
        List<Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        int height = 256;
        int width = 0;
        for(int i = 0;i<sizes.size();i++){
            Log.d("supproted height "+sizes.get(i).height+ " width "+sizes.get(i).width);
            if(240 == sizes.get(i).height || 320 == sizes.get(i).height){
                width = sizes.get(i).height * height / sizes.get(i).width;
                int tmpWidth = sizes.get(i).height * 320 /sizes.get(i).width;
                int layoutHeight = ConstantInfo.dip2px(app, 320);
//                int layoutWidth = ConstantInfo.dip2px(app, tmpWidth);
                int layoutWidth = ConstantInfo.px2dip(app, 240);
                Log.d("layoutHeight "+layoutHeight + " layoutWidth "+ layoutWidth); 
            }
            
        }
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        Log.d(metric.heightPixels + " " + metric.widthPixels );
        if(metric.heightPixels < 500){
            Log.d("3.2寸的");
            ConstantInfo.smallScreen = true;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(218, 290);
            surfaceView.setLayoutParams(params);
        }
            int previewHeight = ConstantInfo.dip2px(app, height);
            int previewWidth = ConstantInfo.dip2px(app, width);
            Log.d("previewHeight "+height + " previewWidth "+width);
            cameraParams.setPreviewSize(320,240);
//        cameraParams.setPictureSize(sizes.get(0).width, sizes.get(0).height);
      
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("surfaceDestoryed");
        surfaceHolder.removeCallback(this);
        camera.stopPreview();
        camera.setPreviewCallback(null);
        previewRunning = false;
        camera.release();
        camera = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isNew){
            Log.d("isNew and reset");
            reset();
        }else{
            
        }
    }

    private boolean isNew = false ;
    
    @Override
    protected void onPause() {
        super.onPause();
        isNew = true;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
    
    private void reset() {
        if(null != camera){
            camera.stopPreview();
            camera.startPreview();
        }else{
            Log.d("camera is null");
            surfaceCreated(null);
        }
    }

    /*
     *设置改变之后的效果 
     */
    public static void setEffect() {
        cameraParams = camera.getParameters();
        cameraParams.setColorEffect(ConstantInfo.EFFECT);
        Log.d("effect is :"+ConstantInfo.EFFECT);
        camera.setParameters(cameraParams);
    }
}
