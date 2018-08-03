package com.dylan.common.gps;

import android.content.Context;
import android.location.Address;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class LocationHander extends Handler {

	private static final int MSG_HINT = 1 ; 
	private static final int MSG_ADDRESS = MSG_HINT + 1 ; 
	private Context context ;
	private Address address ;
	private String text ;
	
	protected LocationHander(Context context){
		this.context = context ;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what){
		case MSG_HINT:
			Toast.makeText(context, msg.obj != null ? msg.obj.toString() : "", 2000).show();
			break ;
		case MSG_ADDRESS :
			if(msg.obj instanceof AddressCallback){
				AddressCallback callback = (AddressCallback)msg.obj ;
				callback.callback(address, text);
			}
			break ;
		}
	}
	
	public void sendAddressCallback(AddressCallback callback,Address add,String text){
		this.address = add;
		this.text = text ;
		Message m = obtainMessage(MSG_ADDRESS);
		m.obj = callback;
		sendMessage(m);
	}
	
	public void sendHintMessage(String msg){
		Message m = obtainMessage(MSG_HINT);
		m.obj = msg;
		sendMessage(m);
	}
}
