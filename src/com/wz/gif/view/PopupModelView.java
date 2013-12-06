package com.wz.gif.view;

import com.wz.gif.CameraPreview;
import com.wz.gif.R;
import com.wz.gif.util.ConstantInfo;
import com.wz.gif.util.Log;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupModelView {
    
    private PopupWindow popup;
    private Activity context;
    
    private ListView listView;
    
    private String[] models ={"普通模式","魔术模式"};
    private View backView;
    
    public PopupModelView(final Activity context ,View backView){
        this.context = context;
        this.backView = backView;
        listView = initListView(context);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context,CameraPreview.class);
                context.startActivity(intent);
                switch(position){
                    case 0:
                        ConstantInfo.magicMode = false;
                        break;
                    case 1:
                        ConstantInfo.magicMode = true;
                        break;
                }
                hide();
            }
        });
    }
    
    public void show(){
        Log.d("come to show ");
        backView.setVisibility(View.VISIBLE);
        final LinearLayout popLayout = initPopLayout(context);
        
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.EXACTLY);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.EXACTLY);
        listView.measure(w, h);
//        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
//        int winHeight = metrics.heightPixels;
//        int winWidth = metrics.widthPixels;
//        
//        int popTmpWidth = (int)(winWidth/3.2);
        
//        Log.d(winHeight + " win "+ winWidth);
        
        Log.d(w+" test "+h);
        listView.setBackgroundResource(R.drawable.popup);
        
        popLayout.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        
        popup = new PopupWindow(context);
        popup.setContentView(popLayout);
        
        int popWidth = ConstantInfo.dip2px(context, 110);
        int popHeight = ConstantInfo.dip2px(context, 80);
        popup.setWidth(popWidth);
        popup.setHeight(popHeight);
        popup.setFocusable(true);
        popup.setOutsideTouchable(false);
        popup.setTouchable(true);
        // 设置背景为null，就不会出现黑色背景，按返回键PopupWindow就会消失
        popup.setBackgroundDrawable(null);
        popup.setAnimationStyle(R.style.popup_gallery_in_out);
        int x = ConstantInfo.dip2px(context, 10);
        int y = ConstantInfo.dip2px(context, 60);
        popup.showAtLocation(popLayout, Gravity.RIGHT | Gravity.TOP ,x,y);
    }

    private ListView initListView(Context context) {
        ListView list = new ListView(context);
        list.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        list.setAdapter(new ListAdapter(context));
        list.setCacheColorHint(0x00000000);
        list.setDividerHeight(1);
        return list;
    }

    /**
     * 初始化popupWindow的布局
     * @param context2
     * @return
     */
    private LinearLayout initPopLayout(Context context2) {
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
                    Log.d("action down");
                    hide();
                }
                return false;
            }
            
        });
        
        return layout;
    }
    
    protected void hide() {
        if(popup.isShowing()){
            popup.dismiss();
            backView.setVisibility(View.INVISIBLE);
        }
    }

    private class ListAdapter extends BaseAdapter{

     private LayoutInflater inflater;
        
        public ListAdapter(Context cxt) {
            inflater = LayoutInflater.from(cxt);
        }

        @Override
        public int getCount() {
            return models.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.effect_dialog_item, null);
                text = (TextView)convertView.findViewById(R.id.effect_list_item_text);
                convertView.setTag(text);
            }else{
                text = (TextView)convertView.getTag();
            }
            text.setText(models[position]);
            return convertView;
        }
    }
        
}
