package com.dylan.common.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends Activity {

	public static final int StartButton_Position_BottomCenter 	= 1;
	public static final int StartButton_Position_TopCenter 		= 2;
	public static final int StartButton_Position_TopLeft 		= 3;
	public static final int StartButton_Position_TopRight 		= 4;
	public static final int StartButton_Position_BottomLeft 	= 5;
	public static final int StartButton_Position_BottomRight 	= 6;

	public static final String BundleKey_Background_Color		= "bgcolor";
	public static final String BundleKey_Pages_Images 			= "images";
	public static final String BundleKey_Image_ScaleType		= "scaleType";
	public static final String BundleKey_Indicater_Radius 		= "radius";
	public static final String BundleKey_Indicater_StrokeColor 	= "strokeColor";
	public static final String BundleKey_Indicater_FillColor 	= "fillColor";
	public static final String BundleKey_Indicater_BottomMargin	= "indicater.bottomMargin";
	public static final String BundleKey_Indicater_AutoHide		= "indicater.autoHide";
	public static final String BundleKey_Button_StartName 		= "startName";
	public static final String BundleKey_Button_TextColor 		= "textColor";
	public static final String BundleKey_Button_TextSize 		= "textSize";
	public static final String BundleKey_Button_BackgroundResId	= "backgroundResId";
	public static final String BundleKey_Button_ImageResId 		= "imageResId";
	public static final String BundleKey_Button_Position		= "startPosition";
	public static final String BundleKey_Button_LeftMargin		= "leftMargin";
	public static final String BundleKey_Button_TopMargin		= "topMargin";
	public static final String BundleKey_Button_RightMargin		= "rightMargin";
	public static final String BundleKey_Button_BottomMargin	= "bottomMargin";
	public static final String BundleKey_Fullscreen_Startup		= "fullscreenStartup";
	
    private ViewPager mViewPager = null;  
    private CirclePageIndicator mIndicator = null;
    private List<Integer> mGuideImages = new ArrayList<Integer>();
    private boolean mFullscreenStartup = false;

	public interface OnGuideOverListener {
		void onOver();
	}

	private static OnGuideOverListener mOnGuideOverListener = null;
	public static void setOnGuideOverListener(OnGuideOverListener onGuideOverListener) {
		GuideActivity.mOnGuideOverListener = onGuideOverListener;
	}

	@SuppressLint("CutPasteId")
	@Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_app_guide);  

		final ImageView.ScaleType scaleType = ImageView.ScaleType.values()[getIntent().getIntExtra(BundleKey_Image_ScaleType, ImageView.ScaleType.CENTER_CROP.ordinal())];
		final int bgcolor = getIntent().getIntExtra(BundleKey_Background_Color, 0xffffffff);
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(BundleKey_Pages_Images)) {
        	int[] image = getIntent().getExtras().getIntArray(BundleKey_Pages_Images);
        	for (int i : image) {
				mGuideImages.add(i);
			}
        }
  
        mViewPager = (ViewPager)findViewById(R.id.content_panel);  
        mViewPager.setAdapter(new PagerAdapter() {
        	private ArrayList<View> mPages = new ArrayList<View>();
        	private LayoutInflater mInflater = LayoutInflater.from(GuideActivity.this);
        	@SuppressLint("InflateParams")
			@Override
        	public Object instantiateItem(View arg0, int arg1) {
        		if (mPages.size() <= arg1) {
        			View page = mInflater.inflate(R.layout.viewpager_app_guide, null);
        			int resId = mGuideImages.get(arg1);
					ImageView iv = (ImageView)page.findViewById(R.id.image);
					iv.setBackgroundColor(bgcolor);
					iv.setImageResource(resId);
					iv.setScaleType(scaleType);
        			if (mFullscreenStartup && arg1 == mGuideImages.size() - 1) {
        				page.setClickable(true);
        				page.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
						    	SplashActivity.setIsFirstStartup(GuideActivity.this, false);
								setResult(Activity.RESULT_OK);
								if (mOnGuideOverListener != null) mOnGuideOverListener.onOver();
								finish();
							}
						});
        			}
        			mPages.add(page);
        		}
        		((ViewPager) arg0).addView(mPages.get(arg1), 0);
        		return mPages.get(arg1);
        	}
        	@Override
        	public void destroyItem(View arg0, int arg1, Object arg2) {
        		((ViewPager) arg0).removeView(mPages.get(arg1));
        	}
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return (arg0 == arg1);
			}
			@Override
			public int getCount() {
				return mGuideImages.size();
			}
		});
		final boolean autoHideIndicator = getIntent().getBooleanExtra(BundleKey_Indicater_AutoHide, false);
        mIndicator = (CirclePageIndicator)findViewById(R.id.pageIndicator);
        mIndicator.setViewPager(mViewPager);  

        if (getIntent() != null && getIntent().getExtras() != null) {
            int screenWidth = Utility.getScreenWidth(this);
            int screenHeight = Utility.getScreenHeight(this);
			Bundle bundle = getIntent().getExtras();
			RelativeLayout.LayoutParams layoutParams = (LayoutParams) mIndicator.getLayoutParams();
			if (bundle.containsKey(BundleKey_Indicater_BottomMargin)) {
				float buttomMargin = bundle.getFloat(BundleKey_Indicater_BottomMargin);
                if (buttomMargin > 2) layoutParams.setMargins(0, 0, 0, (int)buttomMargin);
                else layoutParams.setMargins(0, 0, 0, (int)(buttomMargin * screenHeight));
			}

        	if (bundle.containsKey(BundleKey_Indicater_Radius)) {
        		mIndicator.setRadius(bundle.getFloat(BundleKey_Indicater_Radius));
        	}
        	if (bundle.containsKey(BundleKey_Indicater_StrokeColor)) {
        		mIndicator.setStrokeColor(bundle.getInt(BundleKey_Indicater_StrokeColor));
        	}
        	if (bundle.containsKey(BundleKey_Indicater_FillColor)) {
        		mIndicator.setFillColor(bundle.getInt(BundleKey_Indicater_FillColor));
        	}
        	int position = bundle.getInt(BundleKey_Button_Position, 0);
        	float leftMargin = bundle.getFloat(BundleKey_Button_LeftMargin, -1.f);
            float rightMargin = bundle.getFloat(BundleKey_Button_RightMargin, -1.f);
            float topMargin = bundle.getFloat(BundleKey_Button_TopMargin, -1.f);
            float bottomMargin = bundle.getFloat(BundleKey_Button_BottomMargin, -1.f);
        	int bgResId = bundle.getInt(BundleKey_Button_BackgroundResId, -1);
        	int imageResId = bundle.getInt(BundleKey_Button_ImageResId, 0);
        	int textColor = bundle.getInt(BundleKey_Button_TextColor, 0);
        	int textSize = bundle.getInt(BundleKey_Button_TextSize, 0);
        	mFullscreenStartup = bundle.getBoolean(BundleKey_Fullscreen_Startup, false);

        	if (!mFullscreenStartup) {
                View startup = findViewById(R.id.startButton);
            	if (imageResId != 0) {
            		ImageButton button = (ImageButton)findViewById(R.id.startImageButton);
            		button.setImageResource(imageResId);	
            		startup = button;
            	} else {
            		TextView button = (TextView) findViewById(R.id.startButton);
            		if (bundle.containsKey(BundleKey_Button_StartName)) {
            			button.setText(bundle.getString(BundleKey_Button_StartName));
                	}
            		if (textColor != 0)button.setTextColor(textColor);
            		if (textSize != 0)button.setTextSize(textSize);
            		startup = button; 
            	}
	    		if (bgResId == 0)startup.setBackgroundColor(0x00000000);
	    		else if (bgResId != -1)startup.setBackgroundResource(bgResId);
	    		RelativeLayout.LayoutParams lp = (LayoutParams) startup.getLayoutParams();
	    		switch (position) {
				case StartButton_Position_BottomCenter:
					lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
					lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
					break;
				case StartButton_Position_TopCenter:
					lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);	
					lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
					break;
				case StartButton_Position_TopLeft:
					lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
					lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
					break;
				case StartButton_Position_BottomLeft:
					lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
					lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
					break;
				case StartButton_Position_TopRight:
					lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
					lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
					break;
				case StartButton_Position_BottomRight:
					lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
					lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
					break;
				}
				if (bottomMargin > 2) lp.bottomMargin = (int)bottomMargin;
                else if (bottomMargin > 0.f) lp.bottomMargin = (int)(bottomMargin * screenHeight);
				if (topMargin > 2) lp.topMargin = (int)topMargin;
                else if (topMargin > 0.f) lp.topMargin = (int)(topMargin * screenHeight);
				if (leftMargin > 2) lp.leftMargin = (int)leftMargin;
                else if (leftMargin > 0.f) lp.leftMargin = (int)(leftMargin * screenWidth);
				if (rightMargin > 2) lp.rightMargin = (int)rightMargin;
                else if (rightMargin > 0.f) lp.rightMargin = (int)(rightMargin * screenWidth);
				startup.setLayoutParams(lp);
				startup.requestLayout();
		        
		        final View startupButton = startup;
		        startupButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
				    	SplashActivity.setIsFirstStartup(GuideActivity.this, false);
						setResult(Activity.RESULT_OK);
						if (mOnGuideOverListener != null) mOnGuideOverListener.onOver();
						finish();
					}
				});
        	} else {
        		findViewById(R.id.startButton).setVisibility(View.GONE);
            	findViewById(R.id.startImageButton).setVisibility(View.GONE);
        	}

			mIndicator.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(int arg0) {
					if (arg0 == mGuideImages.size() - 1) {
						if (!mFullscreenStartup) findViewById(R.id.startButton).setVisibility(View.VISIBLE);
						if (autoHideIndicator) mIndicator.setVisibility(View.GONE);
					} else {
						if (!mFullscreenStartup) findViewById(R.id.startButton).setVisibility(View.GONE);
						if (autoHideIndicator) mIndicator.setVisibility(View.VISIBLE);
					}
				}
				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
				}
				@Override
				public void onPageScrollStateChanged(int arg0) {
				}
			});
        }
    }  
    
    public void setupStartButton(RelativeLayout.LayoutParams lp, int bgResId, int imgResId) {
    	ImageButton button = (ImageButton)findViewById(R.id.startImageButton);
    	findViewById(R.id.startButton).setVisibility(View.GONE);
    	button.setLayoutParams(lp);
    	button.requestLayout();
    	if (bgResId != 0)button.setBackgroundResource(bgResId);
    	if (imgResId != 0)button.setImageResource(imgResId);
    }
    public void setupStartButton(RelativeLayout.LayoutParams lp, int bgResId, String text) {
    	TextView button = (TextView)findViewById(R.id.startButton);
    	findViewById(R.id.startImageButton).setVisibility(View.GONE);
    	button.setLayoutParams(lp);
    	button.requestLayout();
    	if (bgResId != 0)button.setBackgroundResource(bgResId);
    	if (text != null)button.setText(text);
    }
    public void addImage(int resId) {
    	mGuideImages.add(resId);
    }
}
