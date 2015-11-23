package com.st.stchat.utils;

import java.util.regex.Pattern;

/**
 * 
 * @author juwei 2014.11.20
 * 
 */
public class IsUtils {

	/**
	 * 判断是否是IP地址
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isIPAdress(String str) {
		Pattern pattern = Pattern
				.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

}
