package com.dylan.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetCheckReceiver extends BroadcastReceiver {

    public enum ConnectStatus {
        None(0, "无连接"),
        Wifi(1, "Wifi连接"),
        Data(2, "数据链接");
        ConnectStatus(int code, String status) {
            this.code = code;
            this.status = status;
        }
        private int code;
        private String status;
        @Override
        public String toString() {
            return status;
        }
        public int getCode() {
            return code;
        }
    };

	public NetCheckReceiver(OnNetChangedListener listener) {
		mListener = listener;
	}
    
    public static NetCheckReceiver register(Context context, OnNetChangedListener listener) {
    	IntentFilter filter = new IntentFilter();  
    	filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); 
    	NetCheckReceiver receiver = new NetCheckReceiver(listener);
    	context.registerReceiver(receiver, filter); 
    	return receiver;
    }
    public void unregister(Context context) {
    	context.unregisterReceiver(this); 
    }
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	if (mListener == null)return;
        ConnectStatus status = getConnectStatus(context, true);
        if (status != null && mListener != null) {
            mListener.onNetChanged(status);
        }
    }
    private ConnectStatus getConnectStatus(Context context, boolean inner) {
        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (connManager == null) {
            return ConnectStatus.None;
        }
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            return ConnectStatus.None;
        }
        if (networkInfo.getState() != NetworkInfo.State.CONNECTED) {
            if (inner) {
                return null;
            } else {
                return ConnectStatus.None;
            }
        }
        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_MOBILE_DUN:
                return ConnectStatus.Data;
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_ETHERNET:
                return ConnectStatus.Wifi;
            default:
                return ConnectStatus.None;
        }
    }
    public ConnectStatus getConnectStatus(Context context) {
        return getConnectStatus(context, false);
    }

    private OnNetChangedListener mListener = null;
    public interface OnNetChangedListener {
        void onNetChanged(ConnectStatus status);
    }
}
