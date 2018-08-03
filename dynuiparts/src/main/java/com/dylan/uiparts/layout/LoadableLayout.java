package com.dylan.uiparts.layout;

import android.content.Context;
import android.support.annotation.AnimRes;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.dylan.common.sketch.Sketch;
import com.dylan.uiparts.annimation.AnimationListener;
import com.dylan.uiparts.R;

public class LoadableLayout extends FrameLayout {

    public interface OnShowLoadingListener {
        public @LayoutRes int getLoadingResId();
        public void onShowLoading(View loading);
    }
    public interface OnShowNetErrorListener {
        public void onShowNetError(View neterror);
    }

    private Context context = null;
    public LoadableLayout(Context context) {
        super(context);
        this.context = context;
    }
    public LoadableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }
    public LoadableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }
    public LoadableLayout(Context context, int contentLayoutResID) {
        super(context);
        this.context = context;
        View contentView = LayoutInflater.from(context).inflate(contentLayoutResID, this, false);
        addView(contentView);
    }
    public LoadableLayout(Context context, int contentLayoutResID, int loadingLayoutResID) {
        super(context);
        this.context = context;
        View contentView = LayoutInflater.from(context).inflate(contentLayoutResID, this, false);
        addView(contentView);
        loadinger = LayoutInflater.from(context).inflate(loadingLayoutResID, this, false);
        loadinger.setClickable(true);
        addView(this.loadinger);
    }
    public LoadableLayout(Context context, View contentView) {
        super(context);
        this.context = context;
        addView(contentView);
    }
    public LoadableLayout(Context context, View contentView, int loadingLayoutResID) {
        super(context);
        this.context = context;
        addView(contentView);
        loadinger = LayoutInflater.from(context).inflate(loadingLayoutResID, this, false);
        loadinger.setClickable(true);
        addView(this.loadinger);
    }

    public final static int DismissAnimationType_FadeOut = 1;
    public final static int DismissAnimationType_LeftOut = 2;
    public final static int DismissAnimationType_RightOut = 3;
    public final static int DismissAnimationType_TopOut = 4;
    public final static int DismissAnimationType_BottomOut = 5;
    public final static int DismissAnimationType_Customize = 6;
    public void dismissLoading() {
        dismissLoading(DismissAnimationType_FadeOut, 0, null);
    }
    public void dismissLoading(int dismissAnimation) {
        if (dismissAnimation < DismissAnimationType_FadeOut || dismissAnimation > DismissAnimationType_BottomOut) {
            throw new IllegalArgumentException();
        }
        dismissLoading(dismissAnimation, 0, null);
    }
    public void dismissLoading(int dismissAnimation, AnimationListener listener) {
        dismissLoading(dismissAnimation, 0, listener);
    }
    public void dismissLoading(int dismissAnimationType, @AnimRes int animationResId, AnimationListener listener) {
        if (loadinger == null || loadinger.getVisibility() == View.GONE)return;
        Animation animation = null;
        switch (dismissAnimationType) {
            case DismissAnimationType_FadeOut:
                animation = AnimationUtils.loadAnimation(context, R.anim.loading_fade_out);
                break;
            case DismissAnimationType_LeftOut:
                animation = AnimationUtils.loadAnimation(context, R.anim.loading_left_out);
                break;
            case DismissAnimationType_RightOut:
                animation = AnimationUtils.loadAnimation(context, R.anim.loading_right_out);
                break;
            case DismissAnimationType_TopOut:
                animation = AnimationUtils.loadAnimation(context, R.anim.loading_top_out);
                break;
            case DismissAnimationType_BottomOut:
                animation = AnimationUtils.loadAnimation(context, R.anim.loading_bottom_out);
                break;
            default:
                animation = AnimationUtils.loadAnimation(context, animationResId);
                break;
        }
        if (loadinger != null && loadinger.getVisibility() == View.VISIBLE) {
            loadinger.setAnimation(animation);
            animation.setFillAfter(false);
            if (listener != null) {
                animation.setAnimationListener(listener);
            } else {
                animation.setAnimationListener(new AnimationListener(null, new AnimationListener.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        loadinger.setVisibility(View.GONE);
                    }
                }, null));
            }
            loadinger.startAnimation(animation);
        }
    }

    public LoadableLayout showLoading() {
        return showLoading(null);
    }
    public LoadableLayout showLoading(boolean animated) {
        return showLoading(null, animated);
    }
    public LoadableLayout showLoading(OnShowLoadingListener listener) {
        return showLoading(listener, true);
    }
    public LoadableLayout showLoading(OnShowLoadingListener listener, boolean animated) {
        if (loadinger == null) {
            @LayoutRes int layoutResId = listener == null ? R.layout.layout_loading_placehodler : listener.getLoadingResId();
            if (layoutResId == 0)layoutResId = R.layout.layout_loading_placehodler;
            this.loadinger = LayoutInflater.from(context).inflate(layoutResId, this, false);
            this.loadinger.setClickable(true);
            addView(this.loadinger);
        }
        if (listener != null) {
            listener.onShowLoading(this.loadinger);
        }
        if (animated) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.loading_fade_in);
            loadinger.setVisibility(View.VISIBLE);
            loadinger.setAnimation(animation);
            if (neterror != null && neterror.getVisibility() == View.VISIBLE) {
                animation.setAnimationListener(new AnimationListener(null, new AnimationListener.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dismissNetError();
                    }
                }, null));
            }
            animation.setFillAfter(true);
            animation.start();
        } else {
            loadinger.setVisibility(View.VISIBLE);
        }
        return this;
    }
    public LoadableLayout setLoadingMessage(String message) {
        Sketch.set_tv(this, R.id.dyn_loading_tips, message);
        return this;
    }

    public LoadableLayout showNetError(View.OnClickListener clickListener) {
        return showNetError(R.layout.layout_net_error_placehodler, clickListener, null);
    }
    public LoadableLayout showNetError(View.OnClickListener clickListener, OnShowNetErrorListener showListener) {
        return showNetError(R.layout.layout_net_error_placehodler, clickListener, showListener);
    }
    public LoadableLayout showNetError(String message, OnClickListener clickListener) {
        showNetError(R.layout.layout_net_error_placehodler, clickListener, null);
        Sketch.set_tv(this, R.id.dyn_net_error_tips, message);
        return this;
    }
    public LoadableLayout showNetError(@LayoutRes int netErrorResID, View.OnClickListener clickListener, OnShowNetErrorListener showListener) {
        if (neterror == null) {
            neterror = LayoutInflater.from(context).inflate(netErrorResID, this, false);
            addView(neterror, getChildCount() - 1);
        }
        if (showListener != null)showListener.onShowNetError(neterror);
        neterror.setVisibility(View.VISIBLE);
        neterror.setClickable(true);
        neterror.setOnClickListener(clickListener);
        dismissLoading(DismissAnimationType_FadeOut);
        return this;
    }
    public void dismissNetError() {
        if (neterror != null) {
            neterror.setVisibility(View.GONE);
        }
    }

    private View loadinger = null;
    private View neterror = null;
}
