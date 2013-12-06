package com.wz.gif;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public abstract class BaseActivity extends Activity{

   public abstract int getContentViewId();
   public abstract void setUpViews();
   public abstract void setUpListeners();
   public abstract void _onCreate();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(getContentViewId());
        
        setUpViews();
        setUpListeners();
        _onCreate();
    }

}
