package com.dylan.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FontManager {
	public static void changeFonts(ViewGroup root, Context context, String fileInAsset) {  	    
		Typeface tf = Typeface.createFromAsset(context.getAssets(), fileInAsset);  	    
       	for (int i = 0; i < root.getChildCount(); i++) {  
       		View v = root.getChildAt(i);  
       		if (v instanceof TextView) {  
       			((TextView) v).setTypeface(tf);  
       		} else if (v instanceof Button) {  
       			((Button) v).setTypeface(tf);  
       		} else if (v instanceof EditText) {  
       			((EditText) v).setTypeface(tf);  
       		} else if (v instanceof ViewGroup) {  
       			changeFonts((ViewGroup) v, context, fileInAsset);  
       		}  
       	}  	    
    } 
	public static void changeFonts(ViewGroup root, Typeface tf) {   	    
		for (int i = 0; i < root.getChildCount(); i++) {  
			View v = root.getChildAt(i);  
			if (v instanceof TextView) {  
				((TextView) v).setTypeface(tf);  
			} else if (v instanceof Button) {  
				((Button) v).setTypeface(tf);  
			} else if (v instanceof EditText) {  
				((EditText) v).setTypeface(tf);  
			} else if (v instanceof ViewGroup) {  
				changeFonts((ViewGroup) v, tf);  
			}  
       	}  	    
    } 

	public static void changeFonts(Activity activity, String fileInAsset) {  	   
		ViewGroup root = (ViewGroup)activity.getWindow().getDecorView().getRootView();
		if (root == null)return;  	    
		changeFonts(root, activity, fileInAsset);
    } 
	public static void changeFonts(Activity activity, Typeface tf) {   	    
		ViewGroup root = (ViewGroup)activity.getWindow().getDecorView().getRootView();
		if (root == null)return;  	    
		changeFonts(root, tf);  	    
    } 
}
