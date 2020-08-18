package com.dylan.dyn3rdparts.imageslider;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Message;
import androidx.viewpager.widget.PagerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.dylan.dyn3rdparts.R;
import com.dylan.dyn3rdparts.imageslider.animations.BaseAnimationInterface;
import com.dylan.dyn3rdparts.imageslider.sliderTypes.BaseSliderView;
import com.dylan.dyn3rdparts.imageslider.transformers.AccordionTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.BackgroundToForegroundTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.BaseTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.CubeInTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.DefaultTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.DepthPageTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.FadeTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.FlipHorizontalTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.FlipPageViewTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.ForegroundToBackgroundTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.RotateDownTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.RotateUpTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.StackTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.TabletTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.ZoomInTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.ZoomOutSlideTransformer;
import com.dylan.dyn3rdparts.imageslider.transformers.ZoomOutTransformer;
import com.dylan.dyn3rdparts.imageslider.tricks.FixedSpeedScroller;
import com.dylan.dyn3rdparts.imageslider.tricks.InfinitePagerAdapter;
import com.dylan.dyn3rdparts.imageslider.tricks.InfiniteViewPager;
import com.dylan.dyn3rdparts.imageslider.tricks.ViewPagerEx;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

public class SliderLayout extends RelativeLayout{

    private Context mContext;
    private InfiniteViewPager mViewPager;
    private SliderAdapter mSliderAdapter;
    private PagerIndicator mIndicator;
    private Timer mCycleTimer;
    private TimerTask mCycleTask;
    private Timer mResumingTimer;
    private TimerTask mResumingTask;
    private boolean mCycling;

    private boolean mAutoRecover = true;
    private int mTransformerId;
    private int mTransformerSpan = 1100;
    private boolean mAutoCycle;
    private long mSliderDuration = 4000;

    private PagerIndicator.IndicatorVisibility mIndicatorVisibility = PagerIndicator.IndicatorVisibility.Visible;
    private BaseTransformer mViewPagerTransformer;
    private BaseAnimationInterface mCustomAnimation;
    public SliderLayout(Context context) {
        this(context,null);
    }

    public SliderLayout(Context context, AttributeSet attrs) {
        this(context,attrs, R.attr.SliderStyle);
    }

    public SliderLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.imageslider_slider_layout, this, true);
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs,R.styleable.SliderLayout, defStyle,0);
        mTransformerSpan = attributes.getInteger(R.styleable.SliderLayout_pager_animation_span, 1100);
        mTransformerId = attributes.getInt(R.styleable.SliderLayout_pager_animation, Transformer.Default.ordinal());
        mAutoCycle = attributes.getBoolean(R.styleable.SliderLayout_auto_cycle,true);
        int visibility = attributes.getInt(R.styleable.SliderLayout_indicator_visibility,0);
        for(PagerIndicator.IndicatorVisibility v: PagerIndicator.IndicatorVisibility.values()){
            if(v.ordinal() == visibility){
                mIndicatorVisibility = v;
                break;
            }
        }
        mSliderAdapter = new SliderAdapter(mContext);
        PagerAdapter wrappedAdapter = new InfinitePagerAdapter(mSliderAdapter);

        mViewPager = (InfiniteViewPager)findViewById(R.id.slider_viewpager);
        mViewPager.setAdapter(wrappedAdapter);

        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                     case MotionEvent.ACTION_UP:
                        recoverCycle();
                        break;
                }
                return false;
            }
        });

        attributes.recycle();
        setPresetIndicator(PresetIndicators.Center_Bottom);
        setPresetTransformer(mTransformerId);
        setSliderTransformDuration(mTransformerSpan,null);
        setIndicatorVisibility(mIndicatorVisibility);
        if(mAutoCycle){
            startAutoCycle();
        }
    }

    public void addOnPageChangeListener(ViewPagerEx.OnPageChangeListener onPageChangeListener){
        if(onPageChangeListener != null){
            mViewPager.addOnPageChangeListener(onPageChangeListener);
        }
    }

    public void removeOnPageChangeListener(ViewPagerEx.OnPageChangeListener onPageChangeListener) {
        mViewPager.removeOnPageChangeListener(onPageChangeListener);
    }

    public void setCustomIndicator(PagerIndicator indicator){
        if(mIndicator != null){
            mIndicator.destroySelf();
        }
        mIndicator = indicator;
        mIndicator.setIndicatorVisibility(mIndicatorVisibility);
        mIndicator.setViewPager(mViewPager);
        mIndicator.redraw();
    }

    public <T extends BaseSliderView> void addSlider(T imageContent){
        mSliderAdapter.addSlider(imageContent);
    }

    private android.os.Handler mh = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            moveNextPosition(true);
        }
    };

    public void startAutoCycle(){
        startAutoCycle(mSliderDuration, mSliderDuration, mAutoRecover);
    }
    public void startAutoCycle(long delay,long duration,boolean autoRecover){
        if(mCycleTimer != null) mCycleTimer.cancel();
        if(mCycleTask != null) mCycleTask.cancel();
        if(mResumingTask != null) mResumingTask.cancel();
        if(mResumingTimer != null) mResumingTimer.cancel();
        mSliderDuration = duration;
        mCycleTimer = new Timer();
        mAutoRecover = autoRecover;
        mCycleTask = new TimerTask() {
            @Override
            public void run() {
                mh.sendEmptyMessage(0);
            }
        };
        mCycleTimer.schedule(mCycleTask,delay,mSliderDuration);
        mCycling = true;
        mAutoCycle = true;
    }

    private void pauseAutoCycle(){
        if(mCycling){
            mCycleTimer.cancel();
            mCycleTask.cancel();
            mCycling = false;
        }else{
            if(mResumingTimer != null && mResumingTask != null){
                recoverCycle();
            }
        }
    }

    public void setDuration(long duration){
        if(duration >= 500){
            mSliderDuration = duration;
            if(mAutoCycle && mCycling){
                startAutoCycle();
            }
        }
    }

    public void stopAutoCycle(){
        if(mCycleTask!=null){
            mCycleTask.cancel();
        }
        if(mCycleTimer!= null){
            mCycleTimer.cancel();
        }
        if(mResumingTimer!= null){
            mResumingTimer.cancel();
        }
        if(mResumingTask!=null){
            mResumingTask.cancel();
        }
        mAutoCycle = false;
        mCycling = false;
    }

    private void recoverCycle(){
        if(!mAutoRecover || !mAutoCycle){
            return;
        }
        if(!mCycling){
            if(mResumingTask != null && mResumingTimer!= null){
                mResumingTimer.cancel();
                mResumingTask.cancel();
            }
            mResumingTimer = new Timer();
            mResumingTask = new TimerTask() {
                @Override
                public void run() {
                    startAutoCycle();
                }
            };
            mResumingTimer.schedule(mResumingTask, 6000);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                pauseAutoCycle();
                break;
        }
        return false;
    }
    public void setPagerTransformer(boolean reverseDrawingOrder,BaseTransformer transformer){
        mViewPagerTransformer = transformer;
        mViewPagerTransformer.setCustomAnimationInterface(mCustomAnimation);
        mViewPager.setPageTransformer(reverseDrawingOrder,mViewPagerTransformer);
    }

    public void setSliderTransformDuration(int period,Interpolator interpolator){
        try{
            Field mScroller = ViewPagerEx.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mViewPager.getContext(),interpolator, period);
            mScroller.set(mViewPager,scroller);
        }catch (Exception e){

        }
    }

    public enum Transformer{
        Default("Default"),
        Accordion("Accordion"),
        Background2Foreground("Background2Foreground"),
        CubeIn("CubeIn"),
        DepthPage("DepthPage"),
        Fade("Fade"),
        FlipHorizontal("FlipHorizontal"),
        FlipPage("FlipPage"),
        Foreground2Background("Foreground2Background"),
        RotateDown("RotateDown"),
        RotateUp("RotateUp"),
        Stack("Stack"),
        Tablet("Tablet"),
        ZoomIn("ZoomIn"),
        ZoomOutSlide("ZoomOutSlide"),
        ZoomOut("ZoomOut");

        private final String name;

        private Transformer(String s){
            name = s;
        }
        public String toString(){
            return name;
        }

        public boolean equals(String other){
            return (other == null)? false:name.equals(other);
        }
    };
    public void setPresetTransformer(int transformerId){
        for(Transformer t : Transformer.values()){
            if(t.ordinal() == transformerId){
                setPresetTransformer(t);
                break;
            }
        }
    }
    public void setPresetTransformer(String transformerName){
        for(Transformer t : Transformer.values()){
            if(t.equals(transformerName)){
                setPresetTransformer(t);
                return;
            }
        }
    }

    public void setCustomAnimation(BaseAnimationInterface animation) {
        mCustomAnimation = animation;
        if (mViewPagerTransformer != null) {
            mViewPagerTransformer.setCustomAnimationInterface(mCustomAnimation);
        }
    }

    public void setPresetTransformer(Transformer ts){
        BaseTransformer t = null;
        switch (ts){
            case Default:
                t = new DefaultTransformer();
                break;
            case Accordion:
                t = new AccordionTransformer();
                break;
            case Background2Foreground:
                t = new BackgroundToForegroundTransformer();
                break;
            case CubeIn:
                t = new CubeInTransformer();
                break;
            case DepthPage:
                t = new DepthPageTransformer();
                break;
            case Fade:
                t = new FadeTransformer();
                break;
            case FlipHorizontal:
                t = new FlipHorizontalTransformer();
                break;
            case FlipPage:
                t = new FlipPageViewTransformer();
                break;
            case Foreground2Background:
                t = new ForegroundToBackgroundTransformer();
                break;
            case RotateDown:
                t = new RotateDownTransformer();
                break;
            case RotateUp:
                t = new RotateUpTransformer();
                break;
            case Stack:
                t = new StackTransformer();
                break;
            case Tablet:
                t = new TabletTransformer();
                break;
            case ZoomIn:
                t = new ZoomInTransformer();
                break;
            case ZoomOutSlide:
                t = new ZoomOutSlideTransformer();
                break;
            case ZoomOut:
                t = new ZoomOutTransformer();
                break;
        }
        setPagerTransformer(true,t);
    }

    public void setIndicatorVisibility(PagerIndicator.IndicatorVisibility visibility){
        if(mIndicator == null){
            return;
        }
        mIndicator.setIndicatorVisibility(visibility);
    }

    public PagerIndicator.IndicatorVisibility getIndicatorVisibility(){
        if(mIndicator == null){
            return mIndicator.getIndicatorVisibility();
        }
        return PagerIndicator.IndicatorVisibility.Invisible;

    }

    public PagerIndicator getPagerIndicator(){
        return mIndicator;
    }
    public enum PresetIndicators{
        Center_Bottom("Center_Bottom",R.id.default_center_bottom_indicator),
        Right_Bottom("Right_Bottom",R.id.default_bottom_right_indicator),
        Left_Bottom("Left_Bottom",R.id.default_bottom_left_indicator),
        Center_Top("Center_Top",R.id.default_center_top_indicator),
        Right_Top("Right_Top",R.id.default_center_top_right_indicator),
        Left_Top("Left_Top",R.id.default_center_top_left_indicator);

        private final String name;
        private final int id;
        private PresetIndicators(String name,int id){
            this.name = name;
            this.id = id;
        }

        public String toString(){
            return name;
        }

        public int getResourceId(){
            return id;
        }
    }
    public void setPresetIndicator(PresetIndicators presetIndicator){
        PagerIndicator pagerIndicator = (PagerIndicator)findViewById(presetIndicator.getResourceId());
        setCustomIndicator(pagerIndicator);
    }

    private InfinitePagerAdapter getWrapperAdapter(){
        PagerAdapter adapter = mViewPager.getAdapter();
        if(adapter!=null){
            return (InfinitePagerAdapter)adapter;
        }else{
            return null;
        }
    }

    private SliderAdapter getRealAdapter(){
        PagerAdapter adapter = mViewPager.getAdapter();
        if(adapter!=null){
            return ((InfinitePagerAdapter)adapter).getRealAdapter();
        }
        return null;
    }

    public int getCurrentPosition(){
        if(getRealAdapter() == null)
            throw new IllegalStateException("You did not set a slider adapter");
        return mViewPager.getCurrentItem() % getRealAdapter().getCount();
    }
    public BaseSliderView getCurrentSlider(){
        if(getRealAdapter() == null)
            throw new IllegalStateException("You did not set a slider adapter");
        int count = getRealAdapter().getCount();
        int realCount = mViewPager.getCurrentItem() % count;
        return  getRealAdapter().getSliderView(realCount);
    }
    public void removeSliderAt(int position){
        if(getRealAdapter()!=null){
            getRealAdapter().removeSliderAt(position);
            mViewPager.setCurrentItem(mViewPager.getCurrentItem(),false);
        }
    }
    public void removeAllSliders(){
        if(getRealAdapter()!=null){
            int count = getRealAdapter().getCount();
            getRealAdapter().removeAllSliders();
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() +  count,false);
        }
    }

    public void setCurrentPosition(int position, boolean smooth) {
        if (getRealAdapter() == null)
            throw new IllegalStateException("You did not set a slider adapter");
        if(position >= getRealAdapter().getCount()){
            throw new IllegalStateException("Item position is not exist");
        }
        int p = mViewPager.getCurrentItem() % getRealAdapter().getCount();
        int n = (position - p) + mViewPager.getCurrentItem();
        mViewPager.setCurrentItem(n, smooth);
    }

    public void setCurrentPosition(int position) {
        setCurrentPosition(position, true);
    }
    public void movePrevPosition(boolean smooth) {
        if (getRealAdapter() == null)
            throw new IllegalStateException("You did not set a slider adapter");
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, smooth);
    }
    public void movePrevPosition(){
        movePrevPosition(true);
    }
    public void moveNextPosition(boolean smooth) {
        if (getRealAdapter() == null)
            throw new IllegalStateException("You did not set a slider adapter");
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, smooth);
    }
    public void moveNextPosition() {
        moveNextPosition(true);
    }
}
