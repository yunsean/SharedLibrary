package com.dylan.common.sketch;

import android.content.Context;
import android.content.res.ColorStateList;

import com.dylan.common.utils.Utility;

public class Colors {
	public static ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) {
		int[] colors = new int[] { pressed, focused, normal, focused, unable, normal };
		int[][] states = new int[6][];
		states[0] = new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled };
		states[1] = new int[] { android.R.attr.state_enabled, android.R.attr.state_focused };
		states[2] = new int[] { android.R.attr.state_enabled };
		states[3] = new int[] { android.R.attr.state_focused };
		states[4] = new int[] { android.R.attr.state_window_focused };
		states[5] = new int[] {};
		ColorStateList colorList = new ColorStateList(states, colors);
		return colorList;
	}
	public static ColorStateList createColorStateList(int normal, int pressed, int focused, int unable, int selected) {
		int[] colors = new int[] { pressed, selected, focused, normal, focused, unable, normal };
		int[][] states = new int[7][];
		states[0] = new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled };
		states[1] = new int[] { android.R.attr.state_selected, android.R.attr.state_enabled };
		states[2] = new int[] { android.R.attr.state_enabled, android.R.attr.state_focused };
		states[3] = new int[] { android.R.attr.state_enabled };
		states[4] = new int[] { android.R.attr.state_focused };
		states[5] = new int[] { android.R.attr.state_window_focused };
		states[6] = new int[] {};
		ColorStateList colorList = new ColorStateList(states, colors);
		return colorList;
	}
	public static ColorStateList createColorStateList(int normal, int pressed) {
		int[] colors = new int[] { pressed, normal, normal };
		int[][] states = new int[3][];
		states[0] = new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled };
		states[1] = new int[] { android.R.attr.state_enabled };
		states[2] = new int[] {};
		ColorStateList colorList = new ColorStateList(states, colors);
		return colorList;
	}

	public static int getColor(Context context, int resId) {
		if (Utility.isMOrLater()) {
			return context.getResources().getColor(resId, null);
		} else {
			return context.getResources().getColor(resId);
		}
	}
}
