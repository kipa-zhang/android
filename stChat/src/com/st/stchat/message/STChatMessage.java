package com.st.stchat.message;

/**
 * 
 * @author juwei 2014.11.27
 * 
 */
public class STChatMessage {
	// 定义3种布局类型
	public static final int MessageType_Time = 0;
	public static final int MessageType_From = 1;
	public static final int MessageType_To = 2;
	// 定义四种消息样式
	// 消息样式类型 "1"为文本，"2"为图片，"3"为语音， "4"为表情
	public static final String MessageStyle_Text = "1";
	public static final String MessageStyle_Pic = "2";
	public static final String MessageStyle_Voice = "3";
	public static final String MessageStyle_Emoji = "4";

	// 消息packetID
	private String mPacketId;
	// 消息发送状态
	private String mStatus;
	// 消息类型
	private int mType; // 本地的阅读类型
	// 消息内容
	private String mContent;
	// 消息来源
	private String mfrom;
	// 消息时间文本（时分long）
	private long mTime;
	// 消息样式
	private String mStyle; // 1文本 ，2图片
	// 本地图片路径
	private String mPicPath;

	/**
	 * 实体类，用于描述消息实体
	 * 
	 * @param Type
	 * @param Content
	 */
	// public STChatMessage(int Type, String Content) {
	// this.mType = Type;
	// this.mContent = Content;
	// }

	// public STChatMessage(int Type, String Content, String from) {
	// this.mType = Type;
	// this.mContent = Content;
	// this.mfrom = from;
	// }

	// public STChatMessage(int mType, String mContent, long mTime) {
	// this.mType = mType;
	// this.mContent = mContent;
	// this.mTime = mTime;
	// }

	// public STChatMessage(int mType, String mContent, String mfrom, long
	// mTime) {
	// this.mType = mType;
	// this.mContent = mContent;
	// this.mfrom = mfrom;
	// this.mTime = mTime;
	// }
	public STChatMessage(int mType, String mContent, String mfrom, long mTime,
			String mStyle, String mPicPath) {

		this.mType = mType;
		this.mContent = mContent;
		this.mfrom = mfrom;
		this.mTime = mTime;
		this.mStyle = mStyle;
		this.mPicPath = mPicPath;
	}

	public STChatMessage(String mPacketId, String mStatus, int mType,
			String mContent, String mfrom, long mTime, String mStyle,
			String mPicPath) {
		this.mPacketId = mPacketId;
		this.mStatus = mStatus;
		this.mType = mType;
		this.mContent = mContent;
		this.mfrom = mfrom;
		this.mTime = mTime;
		this.mStyle = mStyle;
		this.mPicPath = mPicPath;
	}

	// 获取类型
	public int getType() {
		return mType;
	}

	// 设置类型
	public void setType(int mType) {
		this.mType = mType;
	}

	// 获取内容
	public String getContent() {
		return mContent;
	}

	// 设置内容
	public void setContent(String mContent) {
		this.mContent = mContent;
	}

	public String getfrom() {
		return mfrom;
	}

	public void setfrom(String mfrom) {
		this.mfrom = mfrom;
	}

	public long getTime() {
		return mTime;
	}

	public void setTime(long mTime) {
		this.mTime = mTime;
	}

	public String getStyle() {
		return mStyle;
	}

	public void setStyle(String mStyle) {
		this.mStyle = mStyle;
	}

	public String getPicPath() {
		return mPicPath;
	}

	public void setPicPath(String mPicPath) {
		this.mPicPath = mPicPath;
	}

	public String getPacketId() {
		return mPacketId;
	}

	public void setPacketId(String mPacketId) {
		this.mPacketId = mPacketId;
	}

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String mStatus) {
		this.mStatus = mStatus;
	}

}
