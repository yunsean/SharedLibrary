/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 * Nitin Khanna, Hughes Systique Corp. (Reason: Android specific change, optmization, bug fix) 
 */

package com.dylan.common.utils;
import com.dylan.common.utils.InnerTimer.InnerTimerListener;

public class Timer implements InnerTimerListener {
	
	public interface TimerListener {
		public void onTimeout(Timer t);
	}
	
	public static boolean SINGLE_THREAD = true;
	
	public Timer(long t_msec) {
		init(t_msec, null, null); 
	}
	public Timer(long t_msec, String t_label) { 
		init(t_msec, t_label, null); 
	}
	public Timer(long t_msec, TimerListener t_listener) {
		init(t_msec, null, t_listener);
	}
	public Timer(long t_msec, String t_label, TimerListener t_listener) {
		init(t_msec, t_label, t_listener);
	}

	public String getLabel() {
		return label;
	}
	public long getTime() {
		return time;
	}

	public void halt() {
		active = false;
		listener = null;
	}
	public void start() {
		active = true;
		if (SINGLE_THREAD)
			new InnerTimerST(time, this);
		else
			new InnerTimer(time, this);
	}
	public void onInnerTimeout() {
		if (active && listener != null)
			listener.onTimeout(this);
		listener = null;
		active = false;
	}
	
	TimerListener listener;
	long time;
	String label;
	boolean active;
	void init(long t_msec, String t_label, TimerListener t_listener) { // listener_list=new
		listener = t_listener;
		time = t_msec;
		label = t_label;
		active = false;
	}
}
