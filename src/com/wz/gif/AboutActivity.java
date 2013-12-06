package com.wz.gif;

import android.app.Activity;
import android.os.Bundle;

public class AboutActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(1024, 1024);
        
        setContentView(R.layout.about);
    }

}
