package com.wz.gif.view;

import com.wz.gif.CameraPreview;
import com.wz.gif.R;
import com.wz.gif.util.ConstantInfo;
import com.wz.gif.util.Log;

import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupMenu {
    
    private PopupWindow popup;
    private Context context;
    
    private ListView listView;
    
    private String[] effects ={"正常","月光","反色","曝光","棕褐色","相片","浅绿色"};
    
    public PopupMenu(Context context){
        this.context = context;
        listView = initListView(context);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        ConstantInfo.EFFECT = "none";
                        break;
                    case 1:
                        ConstantInfo.EFFECT = "mono";
                        break;
                    case 2:
                        ConstantInfo.EFFECT = "negative";
                        break;
                    case 3:
                        ConstantInfo.EFFECT = "solarize";
                        break;
                    case 4:
                        ConstantInfo.EFFECT = "sepia";
                        break;
                    case 5:
                        ConstantInfo.EFFECT = "posterize";
                        break;
                    case 6:
                        ConstantInfo.EFFECT = "aqua";
                        break;
                }
                CameraPreview.setEffect();
                hide();
            }
        });
    }
    
    public void show(){
        Log.d("come to show ");
        final LinearLayout popLayout = initPopLayout(context);
        
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.EXACTLY);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.EXACTLY);
        listView.measure(w, h);
        int width =listView.getMeasuredWidth();
        int height =listView.getMeasuredHeight();
        
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int winHeight = metrics.heightPixels;
        int winWidth = metrics.widthPixels;
        
        int popTmpHeight = (int) (winHeight/2.2);
        int popTmpWidth = (int)(winWidth/3.2);
        
        Log.d(winHeight + " win "+ winWidth);
        
        Log.d(width+" test "+height);
        listView.setBackgroundResource(R.drawable.popup);
        
        popLayout.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        
        popup = new PopupWindow(context);
        popup.setContentView(popLayout);
        
        popup.setWidth(popTmpWidth);
        popup.setHeight(popTmpHeight);
        popup.setFocusable(true);
        popup.setOutsideTouchable(false);
        popup.setTouchable(true);
        // 设置背景为null，就不会出现黑色背景，按返回键PopupWindow就会消失
        popup.setBackgroundDrawable(null);
        popup.setAnimationStyle(R.style.popup_effect_in_out);
        popup.showAtLocation(popLayout, Gravity.CENTER | Gravity.BOTTOM ,0,45);
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
        }
    }

    private class ListAdapter extends BaseAdapter{

     private LayoutInflater inflater;
        
        public ListAdapter(Context cxt) {
            inflater = LayoutInflater.from(cxt);
        }

        @Override
        public int getCount() {
            return effects.length;
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
            text.setText(effects[position]);
            return convertView;
        }
    }
        
}
