package com.xr.common.portal.feature.device.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * @Description: 作用描述
 * @Author: bigfish
 * @CreateDate: 2022/3/21 10:45
 */
public class ProgressSearchView extends AppCompatImageView {
    private Animation mLoadingAnimation;

    public ProgressSearchView(@NonNull Context context) {
        this(context, null);
    }

    public ProgressSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        int fromDegrees = 0;

        int toDegrees = 360;

        float pivotX = 0.5f;

        float pivotY = 0.5f;

        mLoadingAnimation = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY);

        mLoadingAnimation.setDuration(1500);

        mLoadingAnimation.setRepeatCount(Animation.INFINITE);

        mLoadingAnimation.setInterpolator(new LinearInterpolator());

    }

    public void start() {
        clearAnimation();
        setAnimation(mLoadingAnimation);
        mLoadingAnimation.start();
    }

    public void stop() {
        mLoadingAnimation.cancel();
    }
}
