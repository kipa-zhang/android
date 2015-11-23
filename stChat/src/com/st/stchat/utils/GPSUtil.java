package com.st.stchat.utils;

import android.content.Context;
import android.location.LocationManager;

import com.st.stchat.STChatApplication;

/**
 * 描述：检测手机GPS是否开启
 * 
 * @author juwei 创建时间：2014年6月4日
 * 
 */
public class GPSUtil {

	/**
	 * 判断GPS是否可用
	 * 
	 * @return
	 */
	public static boolean checkGPSIsAvaliable() {
		LocationManager locationMan = (LocationManager) STChatApplication
				.getInstance().getSystemService(Context.LOCATION_SERVICE);
		if (locationMan != null) {
			return locationMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}
		return false;
	}
}
