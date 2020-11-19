package com.dylan.uiparts.tabhost;

import java.util.ArrayList;
import java.util.List;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class TabHost extends FrameLayout implements ViewTreeObserver.OnTouchModeChangeListener {
    private static final int SWITCH_ANIM_DURATION = 500;
    private static final int TABWIDGET_LOCATION_LEFT = 0;
    private static final int TABWIDGET_LOCATION_TOP = 1;
    private static final int TABWIDGET_LOCATION_RIGHT = 2;
    private static final int TABWIDGET_LOCATION_BOTTOM = 3;

    private boolean isAnimationEnd = true;
    private TranslateAnimation mLeftInAnim;
    private TranslateAnimation mLeftOutAnim;
    private TranslateAnimation mRightInAnim;
    private TranslateAnimation mRightOutAnim;
    private TabWidget mTabWidget;
    private FrameLayout mTabContent;
    private List<TabSpec> mTabSpecs = new ArrayList<TabSpec>(2);
    protected int mCurrentTab = -1;
    private View mCurrentView = null;
    protected LocalActivityManager mLocalActivityManager = null;
    private OnTabChangeListener mOnTabChangeListener;
    private OnKeyListener mTabKeyListener;

    public TabHost(Context context) {
        super(context);
        initTabHost();
    }
    public TabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTabHost();
    }
    private void initTabHost() {
        setFocusableInTouchMode(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        mCurrentTab = -1;
        mCurrentView = null;
    }
    
    public void initSwitchAnimation(int width) {
        mLeftInAnim = new TranslateAnimation(-width, 0, 0, 0);
        mLeftOutAnim = new TranslateAnimation(0, -width, 0, 0);
        mRightInAnim = new TranslateAnimation(width, 0, 0, 0);
        mRightOutAnim = new TranslateAnimation(0, width, 0, 0);
        mLeftInAnim.setDuration(SWITCH_ANIM_DURATION);
        mLeftOutAnim.setDuration(SWITCH_ANIM_DURATION);
        mRightInAnim.setDuration(SWITCH_ANIM_DURATION);
        mRightOutAnim.setDuration(SWITCH_ANIM_DURATION);
    }

    public TabSpec newTabSpec(String tag) {
        return new TabSpec(tag);
    }

    public void setup() {
        mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
        if (mTabWidget == null) {
            throw new RuntimeException("Your TabHost must have a TabWidget whose id attribute is 'android.R.id.tabs'");
        }
        mTabKeyListener = new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_ENTER:
                        return false;
                }
                mTabContent.requestFocus(View.FOCUS_FORWARD);
                return mTabContent.dispatchKeyEvent(event);
            }
        };

        mTabWidget.setTabSelectionListener(new TabWidget.OnTabSelectionChanged() {
            public void onTabSelectionChanged(int tabIndex, boolean clicked) {
                setCurrentTab(tabIndex);
                if (clicked) {
                    mTabContent.requestFocus(View.FOCUS_FORWARD);
                }
            }
        });

        mTabContent = (FrameLayout) findViewById(android.R.id.tabcontent);
        if (mTabContent == null) {
            throw new RuntimeException("Your TabHost must have a FrameLayout whose id attribute is 'android.R.id.tabcontent'");
        }
    }

    @Override
    public void sendAccessibilityEvent(int eventType) {
    }
    public void setup(LocalActivityManager activityGroup) {
        setup();
        mLocalActivityManager = activityGroup;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewTreeObserver treeObserver = getViewTreeObserver();
        treeObserver.addOnTouchModeChangeListener(this);
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        final ViewTreeObserver treeObserver = getViewTreeObserver();
        treeObserver.removeOnTouchModeChangeListener(this);
    }
    public void onTouchModeChanged(boolean isInTouchMode) {
        if (!isInTouchMode) {
            if (mCurrentView != null && (!mCurrentView.hasFocus() || mCurrentView.isFocused())) {
                mTabWidget.getChildTabViewAt(mCurrentTab).requestFocus();
            }
        }
    }
    public void addTab(TabSpec tabSpec) {
        if (tabSpec.mIndicatorStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab indicator.");
        }
        if (tabSpec.mContentStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab content");
        }
        View tabIndicator = tabSpec.mIndicatorStrategy.createIndicatorView();
        tabIndicator.setOnKeyListener(mTabKeyListener);
        if (tabSpec.mIndicatorStrategy instanceof ViewIndicatorStrategy) {
            mTabWidget.setStripEnabled(false);
        }
        mTabWidget.addView(tabIndicator);
        mTabSpecs.add(tabSpec);
        if (mCurrentTab == -1) {
            setCurrentTab(0);
        }
    }

    public void clearAllTabs() {
        mTabWidget.removeAllViews();
        initTabHost();
        mTabSpecs.clear();
        requestLayout();
        invalidate();
    }
    public android.widget.TabWidget getTabWidget() {
        return mTabWidget;
    }
    public int getCurrentTab() {
        return mCurrentTab;
    }
    public String getCurrentTabTag() {
        if (mCurrentTab >= 0 && mCurrentTab < mTabSpecs.size()) {
            return mTabSpecs.get(mCurrentTab).getTag();
        }
        return null;
    }
    public View getCurrentTabView() {
        if (mCurrentTab >= 0 && mCurrentTab < mTabSpecs.size()) {
            return mTabWidget.getChildTabViewAt(mCurrentTab);
        }
        return null;
    }
    public View getCurrentView() {
        return mCurrentView;
    }
    public void setCurrentTabByTag(String tag) {
        for (int i = 0; i < mTabSpecs.size(); i++) {
            if (mTabSpecs.get(i).getTag().equals(tag)) {
                setCurrentTab(i);
                break;
            }
        }
    }
    public FrameLayout getTabContentView() {
        return mTabContent;
    }
    private int getTabWidgetLocation() {
        int location = TABWIDGET_LOCATION_TOP;
        switch (mTabWidget.getOrientation())  {
            case LinearLayout.VERTICAL:
                location = (mTabContent.getLeft() < mTabWidget.getLeft()) ? TABWIDGET_LOCATION_RIGHT : TABWIDGET_LOCATION_LEFT;
                break;
            case LinearLayout.HORIZONTAL:
            default:
                location = (mTabContent.getTop() < mTabWidget.getTop()) ? TABWIDGET_LOCATION_BOTTOM : TABWIDGET_LOCATION_TOP;
                break;
        }
        return location;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final boolean handled = super.dispatchKeyEvent(event);
        if (!handled && (event.getAction() == KeyEvent.ACTION_DOWN) && (mCurrentView != null) && (mCurrentView.hasFocus())) {
            int keyCodeShouldChangeFocus = KeyEvent.KEYCODE_DPAD_UP;
            int directionShouldChangeFocus = View.FOCUS_UP;
            int soundEffect = SoundEffectConstants.NAVIGATION_UP;
            switch (getTabWidgetLocation())  {
                case TABWIDGET_LOCATION_LEFT:
                    keyCodeShouldChangeFocus = KeyEvent.KEYCODE_DPAD_LEFT;
                    directionShouldChangeFocus = View.FOCUS_LEFT;
                    soundEffect = SoundEffectConstants.NAVIGATION_LEFT;
                    break;
                case TABWIDGET_LOCATION_RIGHT:
                    keyCodeShouldChangeFocus = KeyEvent.KEYCODE_DPAD_RIGHT;
                    directionShouldChangeFocus = View.FOCUS_RIGHT;
                    soundEffect = SoundEffectConstants.NAVIGATION_RIGHT;
                    break;
                case TABWIDGET_LOCATION_BOTTOM:
                    keyCodeShouldChangeFocus = KeyEvent.KEYCODE_DPAD_DOWN;
                    directionShouldChangeFocus = View.FOCUS_DOWN;
                    soundEffect = SoundEffectConstants.NAVIGATION_DOWN;
                    break;
                case TABWIDGET_LOCATION_TOP:
                default:
                    keyCodeShouldChangeFocus = KeyEvent.KEYCODE_DPAD_UP;
                    directionShouldChangeFocus = View.FOCUS_UP;
                    soundEffect = SoundEffectConstants.NAVIGATION_UP;
                    break;
            }
            if (event.getKeyCode() == keyCodeShouldChangeFocus && mCurrentView.findFocus().focusSearch(directionShouldChangeFocus) == null) {
                mTabWidget.getChildTabViewAt(mCurrentTab).requestFocus();
                playSoundEffect(soundEffect);
                return true;
            }
        }
        return handled;
    }

    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        if (mCurrentView != null) {
            mCurrentView.dispatchWindowFocusChanged(hasFocus);
        }
    }
    public void setCurrentTab(int index) {
    	setCurrentTab(index, true);
    }
    public void setCurrentTab(int index, boolean animated) {
        if (index < 0 || index >= mTabSpecs.size()) {
            return;
        }
        if (index == mCurrentTab) {
            return;
        }
        if (!isAnimationEnd) {
            return;
        }
        if (mCurrentTab != -1) {
            mTabSpecs.get(mCurrentTab).mContentStrategy.tabClosed();
        }
        Boolean rightIn = index > mCurrentTab;

        mCurrentTab = index;
        final TabHost.TabSpec spec = mTabSpecs.get(index);
        mTabWidget.focusCurrentTab(mCurrentTab);
        
        final View old = mCurrentView;
        mCurrentView = spec.mContentStrategy.getContentView();
        if (mCurrentView.getParent() == null) {
            mTabContent.addView(mCurrentView,  new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        if (animated) {
	        if (old != null && mLeftInAnim != null) {
	            isAnimationEnd = false;
	            if (rightIn) {
	                old.startAnimation(mLeftOutAnim);
	                mCurrentView.startAnimation(mRightInAnim);
	            } else {
	                old.startAnimation(mRightOutAnim);
	                mCurrentView.startAnimation(mLeftInAnim);
	            }
	        }	        
	        if (!mTabWidget.hasFocus()) {
	            mCurrentView.requestFocus();
	        }
	        this.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                isAnimationEnd = true;
	            }
	        }, SWITCH_ANIM_DURATION);
        }
        mTabContent.requestFocus(View.FOCUS_FORWARD);
        invokeOnTabChangeListener();
    }
    public void setOnTabChangedListener(OnTabChangeListener l) {
        mOnTabChangeListener = l;
    }
    private void invokeOnTabChangeListener() {
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(getCurrentTabTag());
        }
    }
    public interface OnTabChangeListener {
        void onTabChanged(String tabId);
    }


    public interface TabContentFactory {
        View createTabContent(String tag);
    }
    public class TabSpec {
        private String mTag;
        private IndicatorStrategy mIndicatorStrategy;
        private ContentStrategy mContentStrategy;
        private TabSpec(String tag) {
            mTag = tag;
        }
        public TabSpec setIndicator(CharSequence label) {
            mIndicatorStrategy = new LabelIndicatorStrategy(label);
            return this;
        }
        public TabSpec setIndicator(CharSequence label, Drawable icon) {
            mIndicatorStrategy = new LabelAndIconIndicatorStrategy(label, icon);
            return this;
        }
        public TabSpec setIndicator(View view) {
            mIndicatorStrategy = new ViewIndicatorStrategy(view);
            return this;
        }
        public TabSpec setContent(int viewId) {
            mContentStrategy = new ViewIdContentStrategy(viewId);
            return this;
        }
        public TabSpec setContent(TabContentFactory contentFactory) {
            mContentStrategy = new FactoryContentStrategy(mTag, contentFactory);
            return this;
        }
        public TabSpec setContent(Intent intent) {
            mContentStrategy = new IntentContentStrategy(mTag, intent);
            return this;
        }
        public String getTag() {
            return mTag;
        }
    }

    static interface IndicatorStrategy {
        View createIndicatorView();
    }
    private static interface ContentStrategy {
        View getContentView();
        void tabClosed();
    }

    private class LabelIndicatorStrategy implements IndicatorStrategy {
        private final CharSequence mLabel;
        private LabelIndicatorStrategy(CharSequence label) {
            mLabel = label;
        }

        public View createIndicatorView() {
            TextView tabIndicator = new TextView(getContext());
            tabIndicator.setGravity(Gravity.CENTER);
            tabIndicator.setLayoutParams(new  TableLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            tabIndicator.setMinWidth(80);
            tabIndicator.setMinHeight(60);
            tabIndicator.setText(mLabel);
            return tabIndicator;
        }
    }
    private class LabelAndIconIndicatorStrategy implements IndicatorStrategy {
        private final CharSequence mLabel;
        private final Drawable mIcon;
        private LabelAndIconIndicatorStrategy(CharSequence label, Drawable icon)  {
            mLabel = label;
            mIcon = icon;
        }

        public View createIndicatorView() {
            if (mIcon != null && TextUtils.isEmpty(mLabel)) {
                ImageView icon = new ImageView(getContext());
                icon.setLayoutParams(new  TableLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                icon.setImageDrawable(mIcon);
                return icon;
            }
            TextView tabIndicator = new TextView(getContext());
            tabIndicator.setGravity(Gravity.CENTER);
            tabIndicator.setLayoutParams(new  TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            tabIndicator.setMinWidth(80);
            tabIndicator.setMinHeight(60);
            tabIndicator.setText(mLabel);
            if(mIcon != null) {
                tabIndicator.setCompoundDrawablesWithIntrinsicBounds(null, mIcon, null, null);
                tabIndicator.setCompoundDrawablePadding(10);
            }
            return tabIndicator;
        }
    }

    private class ViewIndicatorStrategy implements IndicatorStrategy {
        private final View mView;
        private ViewIndicatorStrategy(View view) {
            mView = view;
        }
        public View createIndicatorView() {
            return mView;
        }
    }
    private class ViewIdContentStrategy implements ContentStrategy {
        private final View mView;
        private ViewIdContentStrategy(int viewId) {
            mView = mTabContent.findViewById(viewId);
            if (mView != null)  {
                mView.setVisibility(View.GONE);
            } else {
                throw new RuntimeException("Could not create tab content because could not find view with id " + viewId);
            }
        }

        public View getContentView() {
            mView.setVisibility(View.VISIBLE);
            return mView;
        }
        public void tabClosed() {
            mView.setVisibility(View.GONE);
        }
    }

    private class FactoryContentStrategy implements ContentStrategy {
        private View mTabContent;
        private final CharSequence mTag;
        private TabContentFactory mFactory;

        public FactoryContentStrategy(CharSequence tag, TabContentFactory factory) {
            mTag = tag;
            mFactory = factory;
        }
        public View getContentView() {
            if (mTabContent == null) {
                mTabContent = mFactory.createTabContent(mTag.toString());
            }
            mTabContent.setVisibility(View.VISIBLE);
            return mTabContent;
        }
        public void tabClosed() {
            mTabContent.setVisibility(View.GONE);
        }
    }

    private class IntentContentStrategy implements ContentStrategy {
        private final String mTag;
        private final Intent mIntent;
        private View mLaunchedView;
        private IntentContentStrategy(String tag, Intent intent)  {
            mTag = tag;
            mIntent = intent;
        }

        public View getContentView() {
            if (mLocalActivityManager == null) {
                throw new IllegalStateException("Did you forget to call 'public void setup(LocalActivityManager activityGroup)'?");
            }
            final Window w = mLocalActivityManager.startActivity(mTag, mIntent);
            final View wd = w != null ? w.getDecorView() : null;
            if (mLaunchedView != wd && mLaunchedView != null) {
                if (mLaunchedView.getParent() != null)  {
                    mTabContent.removeView(mLaunchedView);
                }
            }
            mLaunchedView = wd;
            if (mLaunchedView != null) {
                mLaunchedView.setVisibility(View.VISIBLE);
                mLaunchedView.setFocusableInTouchMode(true);
                ((ViewGroup) mLaunchedView).setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
            }
            return mLaunchedView;
        }

        public void tabClosed() {
            if (mLaunchedView != null) {
                mLaunchedView.setVisibility(View.GONE);
            }
        }
    }
}