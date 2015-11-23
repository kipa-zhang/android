package com.st.stchat.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 
 * @author juwei 2014.11.4
 * 
 */
public class InfoUtils {
	/**
	 * 将用户帐户和密码保存起来
	 * 
	 * @param context
	 * @param userAccountName
	 * @param userAccountPassword
	 * @param userJID
	 * @param userStatus
	 * @return
	 */
	public static boolean saveUserAccount(Context context,
			String userAccountName, String userAccountPassword, String userJID,
			String userStatus) {
		SharedPreferences sp = context.getSharedPreferences("account",
				Context.MODE_PRIVATE);
		System.out.println("当前用户 ：" + userAccountName.toLowerCase() + "，密码 ："
				+ userAccountPassword + "    ，JID : " + userJID);
		Editor editor = sp.edit();
		editor.putString("user_account_name", userAccountName.toLowerCase());
		editor.putString("user_account_password", userAccountPassword);
		editor.putString("user_jid", userJID);
		editor.putString("user_status", userStatus);
		return editor.commit();
	}

	/**
	 * 保存当前用户的状态
	 * 
	 * @param context
	 * @param userStatus
	 * @return
	 */
	public static boolean saveUserStatus(Context context, String userStatus) {
		SharedPreferences sp = context.getSharedPreferences("account",
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("user_status", userStatus);
		return editor.commit();
	}

	/**
	 * 将保存的用户账户和密码取出来
	 * 
	 * @param context
	 * @return 集合中两个对象 userAccountName,userAccountPassword
	 */
	public static Map<String, String> getUserAccount(Context context) {
		SharedPreferences sp = context.getSharedPreferences("account",
				Context.MODE_PRIVATE);
		Map<String, String> map = new HashMap<String, String>();
		map.put("userAccountName", sp.getString("user_account_name", ""));
		map.put("userAccountPassword",
				sp.getString("user_account_password", ""));

		return map;
	}

	/**
	 * 将保存的用户账户取出来
	 * 
	 * @param context
	 * @return 登陆的账户userAccountName
	 */
	public static String getUser(Context context) {
		SharedPreferences sp = context.getSharedPreferences("account",
				Context.MODE_PRIVATE);
		String userAccount = sp.getString("user_account_name", "");

		return userAccount;
	}

	/**
	 * 将保存的用户JID取出来
	 * 
	 * @param context
	 * @return 登陆的JID userAccountJID
	 */
	public static String getUserJID(Context context) {
		SharedPreferences sp = context.getSharedPreferences("account",
				Context.MODE_PRIVATE);
		String userJID = sp.getString("user_jid", "");
		return userJID;
	}

	/**
	 * 将保存的用户状态取出来
	 * 
	 * @param context
	 * @return 当前用户状态
	 */
	public static String getUserStatus(Context context) {
		SharedPreferences sp = context.getSharedPreferences("account",
				Context.MODE_PRIVATE);
		String userJID = sp.getString("user_status", "");
		return userJID;
	}

	/**
	 * 删除存储的用户账户和密码和JID
	 * 
	 * @param context
	 * @return
	 */
	public static boolean deleteUserAccount(Context context) {
		SharedPreferences sp = context.getSharedPreferences("account",
				Context.MODE_PRIVATE);
		return sp.edit().clear().commit();
	}

	/**
	 * 保存IP和端口
	 * 
	 * @param context
	 * @param xmppHost
	 * @param xmppPort
	 * @return
	 */
	public static boolean saveXmppAddress(Context context, String xmppHost,
			String xmppPort) {
		SharedPreferences sp = context.getSharedPreferences("xmppAddress",
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("xmppHost", xmppHost);
		editor.putString("xmppPort", xmppPort);
		return editor.commit();
	}

	/**
	 * 拿到IP
	 * 
	 * @param context
	 * @return
	 */
	public static String getXmppHost(Context context) {
		SharedPreferences sp = context.getSharedPreferences("xmppAddress",
				Context.MODE_PRIVATE);
		String xmppHost = sp.getString("xmppHost", "divcloud.net");
		return xmppHost;
	}

	/**
	 * 拿到端口
	 * 
	 * @param context
	 * @return
	 */
	public static String getXmppPort(Context context) {
		SharedPreferences sp = context.getSharedPreferences("xmppAddress",
				Context.MODE_PRIVATE);
		String xmppPort = sp.getString("xmppPort", "5222");
		return xmppPort;
	}

	/**
	 * 删除IP地址和端口
	 * 
	 * @param context
	 * @return
	 */
	public static boolean deleteXmppAddress(Context context) {
		SharedPreferences sp = context.getSharedPreferences("xmppAddress",
				Context.MODE_PRIVATE);
		return sp.edit().clear().commit();
	}

	/**
	 * 保存域名
	 * 
	 * @param context
	 * @param packet
	 * @return
	 */
	public static boolean savePacketDomain(Context context, String packet) {
		SharedPreferences sp = context.getSharedPreferences("packet",
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("packetDomain", packet.toString().split("@")[1]);
		return editor.commit();
	}

	/**
	 * 获取域名
	 * 
	 * @param context
	 * @return
	 */
	public static String getPacketDomain(Context context) {
		SharedPreferences sp = context.getSharedPreferences("packet",
				Context.MODE_PRIVATE);
		return sp.getString("packetDomain", "");
	}

	/**
	 * 删除域名
	 * 
	 * @param context
	 * @return
	 */
	public static boolean deletePacketDomain(Context context) {
		SharedPreferences sp = context.getSharedPreferences("packet",
				Context.MODE_PRIVATE);
		return sp.edit().clear().commit();
	}

	/**
	 * 保存好友当前在线状态
	 * 
	 * @param context
	 * @param xmppHost
	 * @param xmppPort
	 * @return
	 */
	public static boolean saveFriendStatus(Context context,
			String friendAccount, String friendStatus) {
		SharedPreferences sp = context.getSharedPreferences("friendStatus",
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("friendAccount", friendAccount);
		editor.putString("friendStatus", friendStatus);
		return editor.commit();
	}

	/**
	 * 拿到好友当前在线状态
	 * 
	 * @param context
	 * @return
	 */
	public static String getFriendStatus(Context context) {
		SharedPreferences sp = context.getSharedPreferences("friendStatus",
				Context.MODE_PRIVATE);
		String xmppHost = sp.getString("friendAccount", "128.199.252.234");
		return xmppHost;
	}

	/**
	 * 拿到好友当前在线状态资源
	 * 
	 * @param context
	 * @return
	 */
	public static String getFriendStatusByName(Context context) {
		SharedPreferences sp = context.getSharedPreferences("friendStatus",
				Context.MODE_PRIVATE);
		String xmppPort = sp.getString("friendStatus", "5222");
		return xmppPort;
	}

	/**
	 * 删除好友当前在线状态资源
	 * 
	 * @param context
	 * @return
	 */
	public static boolean deleteFriendStatus(Context context) {
		SharedPreferences sp = context.getSharedPreferences("friendStatus",
				Context.MODE_PRIVATE);
		return sp.edit().clear().commit();
	}

}
