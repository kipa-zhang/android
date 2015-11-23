package com.st.stchat.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeUtil {
	/**
	 * 将毫秒转换为年月日时分秒
	 * 
	 * @author juwei 2014.11.27
	 */
	public static String getYearMonthDayHourMinuteSecond(long timeMillis) {
		int timezone = 8; // 时区
		long totalSeconds = timeMillis / 1000;
		totalSeconds += 60 * 60 * timezone;
		int second = (int) (totalSeconds % 60);// 秒
		long totalMinutes = totalSeconds / 60;
		int minute = (int) (totalMinutes % 60);// 分
		long totalHours = totalMinutes / 60;
		int hour = (int) (totalHours % 24);// 时
		int totalDays = (int) (totalHours / 24);
		int _year = 1970;
		int year = _year + totalDays / 366;
		int month = 1;
		int day = 1;
		int diffDays;
		boolean leapYear;
		while (true) {
			int diff = (year - _year) * 365;
			diff += (year - 1) / 4 - (_year - 1) / 4;
			diff -= ((year - 1) / 100 - (_year - 1) / 100);
			diff += (year - 1) / 400 - (_year - 1) / 400;
			diffDays = totalDays - diff;
			leapYear = (year % 4 == 0) && (year % 100 != 0)
					|| (year % 400 == 0);
			if (!leapYear && diffDays < 365 || leapYear && diffDays < 366) {
				break;
			} else {
				year++;
			}
		}

		int[] monthDays;
		if (diffDays >= 59 && leapYear) {
			monthDays = new int[] { -1, 0, 31, 60, 91, 121, 152, 182, 213, 244,
					274, 305, 335 };
		} else {
			monthDays = new int[] { -1, 0, 31, 59, 90, 120, 151, 181, 212, 243,
					273, 304, 334 };
		}
		for (int i = monthDays.length - 1; i >= 1; i--) {
			if (diffDays >= monthDays[i]) {
				month = i;
				day = diffDays - monthDays[i] + 1;
				break;
			}
		}
		return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":"
				+ second;
	}

	/**
	 * 将毫秒转换为年月日时分秒
	 * 
	 * @author 2014.12.3
	 * @param type
	 *            需要被转化的类型 1为年月日时分秒，2为年月日时分，3为年月日 ， 4为月日时分，5为时分
	 * @param timeMillis
	 * @return 1为年月日时分秒，2为年月日时分，3为年月日 ， 4为月日时分，5为时分
	 */
	public static String millisToData(int type, long timeMillis) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		calendar.setTimeInMillis(timeMillis);
		int year = calendar.get(Calendar.YEAR);

		int month = calendar.get(Calendar.MONTH) + 1;
		String mToMonth = null;
		if (String.valueOf(month).length() == 1) {
			mToMonth = "0" + month;
		} else {
			mToMonth = String.valueOf(month);
		}

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String dToDay = null;
		if (String.valueOf(day).length() == 1) {
			dToDay = "0" + day;
		} else {
			dToDay = String.valueOf(day);
		}

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		String hToHour = null;
		if (String.valueOf(hour).length() == 1) {
			hToHour = "0" + hour;
		} else {
			hToHour = String.valueOf(hour);
		}

		int minute = calendar.get(Calendar.MINUTE);
		String mToMinute = null;
		if (String.valueOf(minute).length() == 1) {
			mToMinute = "0" + minute;
		} else {
			mToMinute = String.valueOf(minute);
		}

		int second = calendar.get(Calendar.SECOND);
		String sToSecond = null;
		if (String.valueOf(second).length() == 1) {
			sToSecond = "0" + second;
		} else {
			sToSecond = String.valueOf(second);
		}
		// 1为年月日时分秒，2为年月日时分，3为年月日 ， 4为月日时分，5为时分
		if (type == 1) {
			return year + "-" + mToMonth + "-" + dToDay + " " + hToHour + ":"
					+ mToMinute + ":" + sToSecond;
		} else if (type == 2) {
			return year + "年" + mToMonth + "月" + dToDay + "日 " + hToHour + ":"
					+ mToMinute;
		} else if (type == 3) {
			return year + "年" + mToMonth + "月" + dToDay + "日";
		} else if (type == 4) {
			return mToMonth + "月" + dToDay + "日 " + hToHour + ":" + mToMinute;
		} else if (type == 5) {
			return hToHour + ":" + mToMinute;
		} else {
			return "";
		}

	}

	/**
	 * 将年月日时分秒转化成今天，昨天，前天，日期
	 * 
	 * @author juwei 2014.12.8
	 * @param createTime
	 *            传进来"yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
	public static String parseDate(String createTime) {
		try {
			String ret = "";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			long create = sdf.parse(createTime).getTime();
			Calendar now = Calendar.getInstance();
			long ms = 1000 * (now.get(Calendar.HOUR_OF_DAY) * 3600
					+ now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND));// 毫秒数
			long ms_now = now.getTimeInMillis();
			if (ms_now - create < ms) {
				ret = TimeUtil
						.millisToData(5, Long.valueOf(create).longValue());
			} else if (ms_now - create < (ms + 24 * 3600 * 1000)) {
				ret = "昨天";
			} else if (ms_now - create < (ms + 24 * 3600 * 1000 * 2)) {
				ret = "前天";
			} else {
				ret = TimeUtil
						.millisToData(3, Long.valueOf(create).longValue());
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
