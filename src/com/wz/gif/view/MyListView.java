package com.wz.gif.view;

import com.wz.gif.R;
import com.wz.gif.util.FileUtil;
import com.wz.gif.util.Log;
import com.wz.gif.util.TimeUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

public class MyListView extends ListView{

    private Context context;

    private ArrayList<InputStream> arrayFileList;
    private ArrayList<String> arrayNameList;
    private FileUtil fileUtil;
    private ListAdapter listAdapter;
    
    private ArrayList<Bitmap> bitmapList;
    
    private int bitmapCount;
    
    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("MyListView constract");
        this.context = context;
        fileUtil = new FileUtil(context);
        listAdapter = new ListAdapter(context);
    }
    
    public MyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d("MyListView constract");
        this.context = context;
        fileUtil = new FileUtil(context);
        listAdapter = new ListAdapter(context);
    }
    
    /**
     * 查看文件夹大小是否发生变化
     */
    public void checkChange(){
        arrayFileList = FileUtil.getGIfStream();
        if(bitmapCount != arrayFileList.size() ){
            Log.d("file changed "+bitmapCount + " " + arrayFileList.size());
            arrayNameList = fileUtil.getFileNames();
            bitmapList.clear();
            for(int i = 0;i<arrayFileList.size();i++){
                bitmapList.add(i,  BitmapFactory.decodeStream(arrayFileList.get(i)));
            }
            setAdapter(listAdapter);
        }
    }
    
    public void init(){
        arrayFileList = FileUtil.getGIfStream();
        arrayNameList = fileUtil.getFileNames();
        bitmapCount = arrayFileList.size();
        bitmapList = new ArrayList<Bitmap>();
        if(null != arrayFileList && arrayFileList.size()>0 ){
            Log.d("arrayList has:"+arrayFileList.size());
//            bitmapList = new ArrayList<Bitmap>();
            for(int i = 0;i<arrayFileList.size();i++){
                bitmapList.add(i,  BitmapFactory.decodeStream(arrayFileList.get(i)));
            }
            setAdapter(listAdapter);
            }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            return setOnClickListener(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }
    
    private int currentPosition;
    
    private boolean setOnClickListener(final MotionEvent ev) {
        this.setOnItemLongClickListener(new OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("onLongClick");
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                
                currentPosition = position;
                bitmap = bitmapList.get(position);
                startDrag(bitmap,x,y);
                
                //return ture 表示消费了这个touch事件
                return true;
            }
        });
        return false;
    }
    
    
    private WindowManager windowManager ;
    private android.view.WindowManager.LayoutParams windowParams;
    private ImageView imageDrag;
    private Bitmap bitmapOnDel;
    private Bitmap bitmap;
    
    private int screenHeight;
    private int screenWidth;
    
    /**
     * 
     * @param view
     * @param x
     * @param y
     */
    protected void startDrag(Bitmap view, int x, int y) {
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowParams.x = x - 50;
        windowParams.y = y - 50;
        windowParams.alpha = 0.6f;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
        
        Animation imageInAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_del_in);
        imageDel.setVisibility(View.VISIBLE);
        imageDel.startAnimation(imageInAnimation);
        
        Log.d(screenHeight + " " +screenWidth);
        imageDrag =new ImageView(context);
        bitmapOnDel = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        setStria(bitmapOnDel,view,x,y);
        imageDrag.setImageBitmap(view);
        windowManager.addView(imageDrag, windowParams);
    }
    
    /**
     *TODO:如果区域在删除区域的话，显示红色，然后删除，这个在后来定了图片大小之后再确定
     * 得到处理过的图片，现在是条纹图片
     * @param view
     * @return
     */
    private void setStria(Bitmap bmp,Bitmap originalBmp,int x,int y) {
        Canvas canvas = new Canvas(bmp);
        Paint deafaultPaint = new Paint();  
        deafaultPaint.setAntiAlias(true);  
        canvas.drawBitmap(originalBmp, 0, 0, deafaultPaint);
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0 ,bmp.getWidth(),0,bmp.getHeight(),Color.argb(100, 255, 0, 0), Color.argb(100, 255, 0, 0), TileMode.MIRROR);
        paint.setShader(shader);
//        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawRect(0, 0, bmp.getWidth(), bmp.getHeight(), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(imageDrag != null){
            int x = (int)ev.getX();
            int y = (int)ev.getY();
            switch(ev.getAction()){
                case MotionEvent.ACTION_MOVE:
                    onDrag(x,y);
                    break;
                case MotionEvent.ACTION_UP:
                    stopDrag();
                    onDrop(x,y);
                    break;
            }
        }
        
        return super.onTouchEvent(ev);
    }


    private void onDrop(int x, int y) {
        //TODO:这个判断虽然不是太准确，但是可以适用到一定的范围
        if(y > (screenHeight/1.8)){
            Log.d("删除图片"+currentPosition);
            bitmapList.remove(currentPosition);
            String state = fileUtil.deleteFile(arrayNameList.get(currentPosition));
            if(state.equals("ok")){
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show(); 
            }
            arrayNameList.remove(currentPosition);
            setAdapter(listAdapter);
        }
    }

    private void stopDrag() {
        if(imageDrag != null){
            windowManager.removeView(imageDrag);
            imageDrag = null;
            Animation imageOutAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_del_out);
            imageDel.startAnimation(imageOutAnimation);
            imageOutAnimation.setAnimationListener(new AnimationListener(){
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    imageDel.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    }

    /**
     * 拖拽
     * @param x
     * @param y
     */
    private void onDrag(int x, int y) {
        if(imageDrag != null){
            Log.d( x +" "+y);
            windowParams.x = x - 50;
            windowParams.y = y - 50;
            windowManager.updateViewLayout(imageDrag, windowParams);
            if(y > (screenHeight/1.8)){
                Log.d("change del back");
                imageDrag.setImageBitmap(bitmapOnDel);
                imageDel.setImageResource(R.drawable.del_enable);
            }else{
                imageDrag.setImageBitmap(bitmap);
                imageDel.setImageResource(R.drawable.del_normal);
            }
        }
    }


    private class ListAdapter extends BaseAdapter{

        private Context context;
        private LayoutInflater inflater;
        
        public ListAdapter(Context cxt){
            this.context = cxt;
            inflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() {
            return bitmapList.size();
        }

        @Override
        public Object getItem(int position) {
            return bitmapList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("getView :"+position);
            ViewHolder holder;
            try{
                if(convertView == null){
                    holder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.list_item, null);
                    holder.itemImage = (ImageView)convertView.findViewById(R.id.list_item_imageView);
                    holder.itemText = (TextView)convertView.findViewById(R.id.list_item_textView);
                    convertView.setTag(holder);
                }else{
                    holder = (ViewHolder)convertView.getTag();
                }
                holder.itemImage.setImageBitmap(bitmapList.get(position));
                Log.d(bitmapList.get(position).getHeight()+ ", width :"+bitmapList.get(position).getWidth());
                String timeStamp = arrayNameList.get(position).substring(0, 13);
                holder.itemText.setText(TimeUtil.getData(timeStamp));
            }catch(Exception e){
            }
            return convertView;
        }
    }
    
    public class ViewHolder{
        TextView itemText;
        ImageView itemImage;
    }

    private ImageView imageDel;
   
    public void setDelImage(ImageView imageDel) {
        this.imageDel = imageDel;
    }

    public void onResume() {
        
    }
    
}
