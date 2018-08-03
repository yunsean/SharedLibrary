package com.dylan.common.data;

public class GpsUtil {
//	WGS-84：是国际标准，GPS坐标（Google Earth使用、或者GPS模块）
//	GCJ-02：中国坐标偏移标准，Google Map、高德、腾讯使用
//	BD-09：百度坐标偏移标准，Baidu Map使用
	public static final class LatLng {
		public LatLng(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
		public double latitude;
		public double longitude;
	}
	   
    public static final LatLng GCJ2WGS(LatLng ll) {
        if (outOfChina(ll))
        	return ll;
        LatLng d = delta(ll);
        return new LatLng(ll.latitude - d.latitude, ll.longitude - d.longitude);
    }    
    public static final LatLng WGS2GCJ(LatLng ll) {
        if (outOfChina(ll))
        	return ll;
        LatLng d = delta(ll);
        return new LatLng(ll.latitude + d.latitude, ll.longitude + d.longitude);
    }
    public static final LatLng GCJ2BD(LatLng gcj) {
        double x = gcj.longitude, y = gcj.latitude;  
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);  
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);  
        double bdLon = z * Math.cos(theta) + 0.0065;  
        double bdLat = z * Math.sin(theta) + 0.006; 
        return new LatLng(bdLat, bdLon);
    }
    public static final LatLng BD2GCJ(LatLng ll) {
        double x = ll.longitude - 0.0065, y = ll.latitude - 0.006;  
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);  
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);  
        double gcjLon = z * Math.cos(theta);  
        double gcjLat = z * Math.sin(theta);
        return new LatLng(gcjLat, gcjLon);
    }
    public static final LatLng GCJ2WGSExact(LatLng ll) {
    	double gcjLat = ll.latitude, gcjLon = ll.longitude;
        double initDelta = 0.01;
        double threshold = 0.000000001;
        double dLat = initDelta, dLon = initDelta;
        double mLat = gcjLat - dLat, mLon = gcjLon - dLon;
        double pLat = gcjLat + dLat, pLon = gcjLon + dLon;
        double wgsLat = 0, wgsLon = 0, i = 0;
        while (true) {
            wgsLat = (mLat + pLat) / 2;
            wgsLon = (mLon + pLon) / 2;
            LatLng tmp = WGS2GCJ(new LatLng(wgsLat, wgsLon));
            dLat = tmp.latitude - gcjLat;
            dLon = tmp.longitude - gcjLon;
            if ((Math.abs(dLat) < threshold) && (Math.abs(dLon) < threshold))
                break; 
            if (dLat > 0) pLat = wgsLat; 
            else mLat = wgsLat;
            if (dLon > 0) pLon = wgsLon; 
            else mLon = wgsLon; 
            if (++i > 10000) break;
        }
        return new LatLng(wgsLat, wgsLon);
    }
    
    private final static double PI = 3.14159265358979324;
    private final static double x_pi = 0;
    private static final double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    } 
    private static final double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
    private static final LatLng delta(LatLng ll) {
        double a = 6378245.0;
        double ee = 0.00669342162296594323;
        double lat = transformLat(ll.longitude - 105.0, ll.latitude - 35.0);
        double lon = transformLon(ll.longitude - 105.0, ll.latitude - 35.0);
        double radLat = ll.latitude / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        lat = (lat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        lon = (lon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        return new LatLng(lat, lon);
    } 
	private static final boolean outOfChina(LatLng ll) {
        if (ll.longitude < 72.004 || ll.longitude > 137.8347)
            return true;
        if (ll.latitude < 0.8293 || ll.latitude > 55.8271)
            return true;
        return false;    
    }  
}
