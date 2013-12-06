package com.wz.gif;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class WelcomeActivity extends Activity{

    private ImageView welcomeImage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(1024, 1024);
        
        setContentView(R.layout.welcome);
        welcomeImage = (ImageView)this.findViewById(R.id.welcome_logo);
        AlphaAnimation animation = new AlphaAnimation(0.1f,1.0f);
        animation.setDuration(1500);
        welcomeImage.startAnimation(animation);
        animation.setAnimationListener(new AnimationListener(){

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(WelcomeActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
        });
        
    }

}
