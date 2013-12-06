package com.wz.gif.view;

import com.wz.gif.util.Log;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MagicView extends ImageView{

    private Paint mPaint;
    private Path mPath;
    public Bitmap mBitmap;
    private Canvas mCanvas;
    public boolean havePained = false;
    
    private final static int penColor = 0xffFF6900;
    private static final int TOUCH_TOLERANCE = 4;
    
    private int height ;
    private int width ;

    private MagicView magicView;
    public MagicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        magicView = this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("onMeasure "+width +" "+height);
    }
    public void init(int width ,int height) {
        this.width = width;
        this.height = height;
        mPaint = new Paint();  
        mPaint.setColor(penColor);
        mPaint.setAntiAlias(true);  
      
        mPaint.setDither(true);  
        mPaint.setStyle(Paint.Style.STROKE);  
        mPaint.setStrokeJoin(Paint.Join.ROUND);  
        mPaint.setStrokeCap(Paint.Cap.ROUND);  
        mPaint.setStrokeWidth(20);  
        
        mPath =  new Path();
        mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);  
        mCanvas = new Canvas();  
        mCanvas.setBitmap(mBitmap);  
    }

    /**
     * 清除之前的涂鸦
     */
    public void clearTracks(){
        mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap); 
    }
    
    @Override  
    protected void onDraw(Canvas canvas) {  
        canvas.drawBitmap(mBitmap, 0, 0, null);  
        mCanvas.drawPath(mPath, mPaint);  
        super.onDraw(canvas);  
    }  
    
    private int mX;
    private int mY;
      
    private void touch_start(int x, int y) {  
        mPath.reset();  
        mPath.moveTo(x, y);  
        mX = x;  
        mY = y;  
    }  
    private void touch_move(int x, int y) {  
        float dx = Math.abs(x - mX);  
        float dy = Math.abs(y - mY);  
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {  
//            Log.d(x + " "+ y);
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);  
            mX = x;  
            mY = y;  
        }  
    }  
    
    //TODO：暂时还没有增加对橡皮擦的处理，此外如何判断两个矩形的大小？
    private void touch_up() {  
        mPath.lineTo(mX, mY);  
        mCanvas.drawPath(mPath, mPaint);  
        mPath.reset();  
    }  
    
    Point start = new Point();
    Point mid = new Point();
    float oldDist = 1f;
    static final int PAINT = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = PAINT;
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    
    @Override 
    public boolean onTouchEvent(MotionEvent event) {  
        int x = (int) event.getX();  
        int y = (int) event.getY();  
        havePained = true;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {  
            case MotionEvent.ACTION_DOWN:  
                mode = PAINT;
                touch_start(x, y);  
                invalidate();  
                break;  
            case MotionEvent.ACTION_UP:  
                touch_up();  
                invalidate();  
                break;  
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d("第二个手指down");
                oldDist = spacing(event);
                if(oldDist > 10f){
                    savedMatrix.set(matrix);
                    minPoint(mid,event);
                    mode = ZOOM;
                }else{
                    mode = DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:  
                Log.d(mode +" is the mode ");
                switch(mode){
                    case PAINT:
                        touch_move(x, y);  
                        invalidate();  
                        break;
                    case DRAG:
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x,
                              event.getY() - start.y);
                        magicView.setImageMatrix(matrix);
                        invalidate(); 
                        break;
                    case ZOOM:
                        float newDist = spacing(event);
//                        if (newDist > 10f) {
                           matrix.set(savedMatrix);
                           float scale = newDist / oldDist;
                           matrix.postScale(scale, scale, mid.x, mid.y);
//                        }
                        magicView.setImageMatrix(matrix);
                        invalidate(); 
                        break;
                }
                break;  
        }  
        return true;  
    }

    private void minPoint(Point point, MotionEvent event) {
        int x = (int) (event.getX(0) + event.getX(1));
        int y = (int) (event.getY(0) + event.getY(1));
        point.set(x / 2, y / 2);
    }

    /**
     * 
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        int x = (int) (event.getX(0) - event.getX(1));
        int y = (int) (event.getY(0) - event.getY(1)); 
        return (float) Math.sqrt(x * x + y * y);
    }

    
    /**
     * 这是画笔大小
     * @param i
     */
    public void setPenSize(int penSize) {
        mPaint.setStrokeWidth(penSize);
    }

    /**
     * 设置画笔的类型
     * @param type 0 为铅笔；1 为橡皮擦
     */
    public void setPenType(int type) {
        switch(type){
            case 0:
                mPaint.setXfermode(null);
                break;
            case 1:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));  
                break;
        }
    }
}
