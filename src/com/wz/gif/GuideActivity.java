package com.wz.gif;

import com.wz.gif.util.Log;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;

public class GuideActivity extends Activity{

    private  ViewPager viewPager;
    private ArrayList<View> viewList;
    private ViewGroup viewGroup;
    private TextView textView;
    private TextView[] texts;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.guide);
        setUpViews();
        
        viewPager.setAdapter(new GuideAdapter());
        viewPager.setOnPageChangeListener(new GuidePageListener());
    }

    private void setUpViews() {
        viewGroup = (ViewGroup)this.findViewById(R.id.guide_viewGroup);
        viewPager = (ViewPager)this.findViewById(R.id.guide_viewPager);
        
        LayoutInflater inflater = getLayoutInflater();
        viewList = new ArrayList<View>();
        try{
            viewList.add(inflater.inflate(R.layout.guide_item01, null));
            viewList.add(inflater.inflate(R.layout.guide_item02, null));
            viewList.add(inflater.inflate(R.layout.guide_item03, null));
            viewList.add(inflater.inflate(R.layout.guide_item04, null));
            viewList.add(inflater.inflate(R.layout.guide_item05, null)); 
        }catch(Exception e){
            Log.e("error form GuideActivity "+ e.toString());
        }
        
        texts = new TextView[viewList.size()];
        
        for (int i = 0; i < viewList.size(); i++)
        {
            textView=new TextView(GuideActivity.this);           
//            textView.setLayoutParams(new LayoutParams(30,30));
            textView.setPadding(0, 0, 2, 0);            
            texts[i]=textView;
            if (i==0)
            {   
                texts[i].setBackgroundResource(R.drawable.radio_sel);                       
            }else {
                texts[i].setBackgroundResource(R.drawable.radio);
            }
            viewGroup.addView(texts[i]);            
        }
    }
    
    class GuideAdapter extends PagerAdapter{

        @Override
        public int getCount()
        {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(
                View arg0, Object arg1)
        {
            return arg0==arg1;
        }

        @Override
        public void destroyItem(
                ViewGroup container,
                int position, Object object)
        {
            ((ViewPager)container).removeView(viewList.get(position));          
        }

        @Override
        public void finishUpdate(
                ViewGroup container)
        {
            super.finishUpdate(container);
        }

        @Override
        public int getItemPosition(Object object)
        {
            return super.getItemPosition(object);
        }

        @Override
        public CharSequence getPageTitle(
                int position)
        {
            return super.getPageTitle(position);
        }

        @Override
        public Object instantiateItem(
                ViewGroup container, int position)
        {
            ((ViewPager)container).addView(viewList.get(position));
            return viewList.get(position);
        }       
    }

    class GuidePageListener implements OnPageChangeListener{

        @Override
        public void onPageScrolled(int i, float f, int j) {
        }

        @Override
        public void onPageSelected(int arg0) {
            Log.d(arg0 + "was selected");
            for(int i=0;i<texts.length;i++) {               
                texts[arg0].setBackgroundResource(R.drawable.radio_sel);
                if (arg0!=i)
                {                   
                    texts[i].setBackgroundResource(R.drawable.radio);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
//            Log.d(arg0 +" state changed");
        }
        
    }
}
