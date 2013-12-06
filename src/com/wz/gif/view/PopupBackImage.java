package com.wz.gif.view;

import com.wz.gif.MyApp;
import com.wz.gif.R;
import com.wz.gif.util.ConstantInfo;
import com.wz.gif.util.GalleryListener;
import com.wz.gif.util.Log;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;

public class PopupBackImage {
    
    private PopupWindow popup;
    private Context context;
    private Gallery gallery;
    
    private int[] selectArray;
    private ArrayList<Bitmap> bitmaps;
    private View showView;
    private GalleryListener galleryListener;
    
    public PopupBackImage(Context cxt,MyApp app,View showView){
        this.context = cxt;
        selectArray = app.getSelectArray();
        bitmaps = new ArrayList<Bitmap>();
        this.showView = showView;
        for(int i = 0;i<selectArray.length;i++){
            if(selectArray[i] == 1){
                //制作缩略图
                Bitmap bitmap = Bitmap.createScaledBitmap(app.getBitmap(i), 70,70, true);
                bitmaps.add(bitmap);
            }
        }
        gallery = initGallery();
        gallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("select item "+position);
                galleryListener.magicBackChanged(position);
                popup.dismiss();
            }
        });
        gallery.setBackgroundResource(R.drawable.popup);
        
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int winWidth = metrics.widthPixels;
        int winHeight = metrics.heightPixels;
        Log.d("width " +winWidth +" " + winHeight);
        
        final LinearLayout popLayout = initLayout();
        popLayout.addView(gallery, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        popup = new PopupWindow(context);
        popup.setContentView(popLayout);
        popup.setFocusable(true);
        popup.setHeight(ConstantInfo.dip2px(context, 80));//因为我们缩略图的大小为50
        popup.setWidth(winWidth);
        popup.setBackgroundDrawable(null);
        popup.setAnimationStyle(R.style.popup_gallery_in_out);
    }

    private LinearLayout initLayout() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setFadingEdgeLength(0);
        layout.setGravity(Gravity.CENTER);
        
        layout.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    if(popup.isShowing())
                        popup.dismiss();
                }
                return false;
            }
            
        });
        
        return layout;
    }

    public void show() {
        if (popup.isShowing())
        {
            popup.dismiss();
        }else{
            popup.showAsDropDown(showView, 0, 0);
        }
    }

    private Gallery initGallery() {
        Gallery gallery = new Gallery(context);
        gallery.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        gallery.setAdapter(new GalleryAdapter(context));
        int paddingTop = ConstantInfo.dip2px(context, 10);
        gallery.setPadding(3, paddingTop, 3, paddingTop);
        gallery.setSpacing(3);
        gallery.setSelection(bitmaps.size()/2);
        return gallery;
    }
    
    class GalleryAdapter extends BaseAdapter{

        private LayoutInflater inflater;
        
        public GalleryAdapter(Context context){
            inflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() {
            return bitmaps.size();
        }

        @Override
        public Object getItem(int position) {
            return bitmaps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.gallery_item, null);
                imageView = (ImageView)convertView.findViewById(R.id.gallery_item_image);
                convertView.setTag(imageView);
            }else{
                imageView = (ImageView) convertView.getTag();
            }
            imageView.setImageBitmap(bitmaps.get(position));
            return convertView;
        }
    }

    public void setGalleryListener(GalleryListener galleryListener) {
        this.galleryListener = galleryListener;
    }
    
}
