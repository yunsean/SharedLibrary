package com.dylan.common.gps;

import java.util.List;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public final class LocationFactory implements LocationListener{

	private static final int TIMER = 5000;
	private Context context = null;
	private final LocationHander handler;
	private LocationManager locationManager;
	private Criteria criteria;
	private Geocoder geocoder;
	private Location location;
	private String provider;
	private int gps_flag;
	private boolean looping = false;
	private int interval = 5000;
	private boolean working = false;
	private Thread mThread = null;
	
	public LocationFactory(Context context, boolean coarse){
		this.context = context;
		handler = new LocationHander(context);
		locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		geocoder = new Geocoder(context);
		gps_flag = 0;
		initLocation(coarse);
	}
	protected void finalize() {
		unregisterAddressCallback();
	}
	
	public void registerAddressCallback(final AddressCallback callback) {
		registerAddressCallback(callback, 0);
	}
	public void registerAddressCallback(final AddressCallback callback, int intervalms){
		if(gps_flag == 1){
			Log.d("LocationFactory", "GPS is running");
			return;
		}

		working = true;
		if(callback != null){
			if (intervalms != 0) {
				looping = true; 
				if (intervalms < 100)
					interval = 100;
				else 
					interval = intervalms;
				interval /= 100;
			} else {
				interval = 0;
			}
			mThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					gps_flag = 1;
					int count = 0;
					do {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							Log.e("dylan", e1.getMessage());
							e1.printStackTrace();
							break;
						}
						if (++count < interval)continue;
						count = 0;
						if (!working)break;
						
						Address a = null;
						String address = "";
						try{
							List<Address> list = geocoder.getFromLocation(getLatitude(), getLongitude(), 1);
							for(Address add : list){
								a = add;
								for (int i = 0; i < 10; i++) {
									String text = add.getAddressLine(i);
									if (text == null || text.length() < 1)break;
									address += text;
								}
							}
						}catch(Exception e){
							e.printStackTrace();
						}
						if (a == null) {
							if (!looping) {
								interval = 50;
								continue;
							} else {
								continue;
							}
						}
						handler.sendAddressCallback(callback, a, address);
						if (!looping)break;
					} while(working);
					gps_flag = 0;
				}
			});
			mThread.start();
		}
	}
	
	public void unregisterAddressCallback() {
		working = false;
		looping = false;
		if (mThread != null) {
			try {
				mThread.join(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mThread = null;
		}
	}
	
	public final Location getCurrentLocation(){
		
		provider = locationManager.getBestProvider(criteria, true);
		if(provider != null){
			locationManager.requestLocationUpdates(provider, TIMER, 0, this);
			location = locationManager.getLastKnownLocation(provider);
			criteria = null;
			provider = null;
		}
		return location;
	}

	public final double getLatitude(){
		return location != null ? location.getLatitude() : 0.00000000;
	}
	
	public final double getLongitude(){
		return location != null ? location.getLongitude() : 0.00000000;
	}

	public final String getLocationString(){
		return getLatitude() + ";" + getLongitude();
	}
	
	//////////////////////////////////////////////////////////////////
	@Override
	public void onLocationChanged(Location location) {
		this.location = location;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		handler.sendHintMessage(provider + " open.");
	}

	@Override
	public void onProviderDisabled(String provider) {
		handler.sendHintMessage(provider + " closed.");
	}
	
	public void removeUpdates(){
		locationManager.removeUpdates(this);
	}
	
	//////////////////////////////////////////////////////////////
	private final void initLocation(boolean coarse){
        criteria = new Criteria();
        criteria.setAccuracy(coarse ? Criteria.ACCURACY_COARSE : Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        getCurrentLocation();
	}
}
