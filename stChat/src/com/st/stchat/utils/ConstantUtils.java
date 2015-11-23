package com.st.stchat.utils;

public final class ConstantUtils {
	public static final String TOALIVE_ACTION = "com.st.singlechat.ALIVE"; // 在线广播
	public static final String TOUNALIVE_ACTION = "com.st.singlechat.UNALIVE"; // 掉线广播
	public static final String OUTALIVE_ACTION = "com.st.singlechat.OUTALIVE"; // 强制下线广播
	public static final String ROSTER_DELETED_ACTION = "roster.deleted";// 花名册有删除
	public static final String CONTACT_DELETED_ACTION = "contact.deleted";// 本地联系人删除有删除
	public static final String ROSTER_SUBSCRIPTION_ACTION = "roster.subscribe";// 收到好友邀请请求
	public static final String ROSTER_UPDATED_ACTION = "roster.updated";// 花名册有更新
	public static final String MESSAGE_DELETED_ACTION = "message.deleted";// 花名册有更新

	public static final String MS_FORMART = "yyyy-MM-dd HH:mm:ss SSS";
	public static final String NEW_MESSAGE_ACTION = "roster.newmessage";
	public static final String IMMESSAGE_KEY = "immessage.key";
	public static final String SINGLE_CHAT_ACTION = "com.st.singlechat"; // 单人聊天消息广播
	public static final String SINGLE_CHAT_PIC_ACTION = "com.st.singlechat.pic"; // 单人聊天图片广播
	public static final String SINGLE_CHAT_MESSAGE_STATUS = "com.st.singlechat.message.status"; // 单人聊天消息发送状态广播
	public static final String SINGLE_CHAT_FRIEND_STATUS = "com.st.singlechat.friend.status"; // 单人聊天消息发送状态广播

	public static final String GROUP_CHAT_ACTION = "com.st.groupchat"; // 群组聊天消息广播

	public static final String STATUS_FREE_TO_CHAT = "Free To Chat";
	public static final String STATUS_AVAULABLE = "Available";
	public static final String STATUS_AWAY = "Away";
	public static final String STATUS_ON_PHONE = "On Phone";
	public static final String STATUS_EXTENDED_AWAY = "Extended Away";
	public static final String STATUS_ON_THE_ROAD = "On The Road";
	public static final String STATUS_DO_NOT_DISTURB = "Do Not Disturb";
	public static final String STATUS_UNAVAILABLE = "unavailable";
	public static final String STATUS_UNKNOWN = "unknown";

	/**
	 * 消息已读，标记为1
	 */
	public static final String MESSAGE_RECEIVE_READ = "1";
	/**
	 * 消息未读，标记为0
	 */
	public static final String MESSAGE_RECEIVE_NOT_READ = "0";
	/**
	 * 消息 ---正在发送
	 */
	public static final String MESSAGE_SEND_SSSS = "-1";
	/**
	 * 消息 ---发送成功
	 */
	public static final String MESSAGE_SEND_SUCCESS = "0";
	/**
	 * 消息 --- 发送失败
	 */
	public static final String MESSAGE_SEND_ERROR = "1";
	/**
	 * 消息---对方已接受
	 */
	public static final String MESSAGE_SEND_HASRECEIVED = "2";

	private ConstantUtils() {
	}

	/**
	 * 配置开发者模式
	 * 
	 * @author juwei
	 * 
	 */
	public static class Config {
		public static final boolean DEVELOPER_MODE = true;
	}
}
