package com.wz.gif.util;

import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

public class BtnAnimation extends AnimationSet{
    public Direction    direction;

    public enum Direction {
        IN, OUT;
    }

    public BtnAnimation(long l) {
        super(true);
        addOutAnimation();
        setDuration(l);
    }

    private void addOutAnimation() {
        addAnimation(new ScaleAnimation(1F, 4F, 1F, 5F, 1, 0.5F, 1, 0.5F));
        addAnimation(new AlphaAnimation(1F, 0F));
    }
}
