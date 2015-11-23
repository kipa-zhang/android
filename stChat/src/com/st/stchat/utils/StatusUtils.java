package com.st.stchat.utils;

public class StatusUtils {

	public static String getStatusStr(String fromJID, String status,
			String mode, String type) {
		String resultStr = "";
		if (mode.equals("null")) {
			mode = "available";
		}

		if (type.equals("available")) {
			if (mode.equals("chat")) {
				resultStr = ConstantUtils.STATUS_FREE_TO_CHAT;
			} else if (mode.equals("available")) {
				resultStr = ConstantUtils.STATUS_AVAULABLE;
			} else if (mode.equals("away")) {
				resultStr = ConstantUtils.STATUS_AWAY;
			} else if (mode.equals("away")) {
				resultStr = ConstantUtils.STATUS_ON_PHONE;
			} else if (mode.equals("xa")) {
				resultStr = ConstantUtils.STATUS_EXTENDED_AWAY;
			} else if (mode.equals("xa")) {
				resultStr = ConstantUtils.STATUS_ON_THE_ROAD;
			} else if (mode.equals("dnd")) {
				resultStr = ConstantUtils.STATUS_DO_NOT_DISTURB;
			} else {
				resultStr = ConstantUtils.STATUS_UNKNOWN;
			}

		} else if (type.equals("unavailable")) {
			resultStr = ConstantUtils.STATUS_UNAVAILABLE;
		} else {

		}
		System.out.println("收到一个状态 ：" + resultStr);
		return resultStr;

	}

}
