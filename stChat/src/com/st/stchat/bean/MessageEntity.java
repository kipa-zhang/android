package com.st.stchat.bean;

public class MessageEntity {
	private String chatPacketId;
	private String chatAccount;
	private String chatType;
	private String chatFrom;
	private String chatTo;
	private String chatRead;
	private String chatSendStatus;
	private String chatTime;
	private String chatText;
	private String chatStyle;
	private String chatPicPath;

	public MessageEntity() {

	}

	public MessageEntity(String chatPacketId, String chatAccount,
			String chatType, String chatFrom, String chatTo, String chatRead,
			String chatSendStatus, String chatTime, String chatText,
			String chatStyle, String chatPicPath) {
		super();
		this.chatPacketId = chatPacketId;
		this.chatAccount = chatAccount;
		this.chatType = chatType;
		this.chatFrom = chatFrom;
		this.chatTo = chatTo;
		this.chatRead = chatRead;
		this.chatSendStatus = chatSendStatus;
		this.chatTime = chatTime;
		this.chatText = chatText;
		this.chatStyle = chatStyle;
		this.chatPicPath = chatPicPath;
	}

	public String getChatPacketId() {
		return chatPacketId;
	}

	public void setChatPacketId(String chatPacketId) {
		this.chatPacketId = chatPacketId;
	}

	public String getChatSendStatus() {
		return chatSendStatus;
	}

	public void setChatSendStatus(String chatSendStatus) {
		this.chatSendStatus = chatSendStatus;
	}

	public String getChatStyle() {
		return chatStyle;
	}

	public void setChatStyle(String chatStyle) {
		this.chatStyle = chatStyle;
	}

	public String getChatPicPath() {
		return chatPicPath;
	}

	public void setChatPicPath(String chatPicPath) {
		this.chatPicPath = chatPicPath;
	}

	public String getChatRead() {
		return chatRead;
	}

	public void setChatRead(String chatRead) {
		this.chatRead = chatRead;
	}

	public String getChatAccount() {
		return chatAccount;
	}

	public void setChatAccount(String chatAccount) {
		this.chatAccount = chatAccount;
	}

	public String getChatType() {
		return chatType;
	}

	public void setChatType(String chatType) {
		this.chatType = chatType;
	}

	public String getChatFrom() {
		return chatFrom;
	}

	public void setChatFrom(String chatFrom) {
		this.chatFrom = chatFrom;
	}

	public String getChatTo() {
		return chatTo;
	}

	public void setChatTo(String chatTo) {
		this.chatTo = chatTo;
	}

	public String getChatTime() {
		return chatTime;
	}

	public void setChatTime(String chatTime) {
		this.chatTime = chatTime;
	}

	public String getChatText() {
		return chatText;
	}

	public void setChatText(String chatText) {
		this.chatText = chatText;
	}

	@Override
	public String toString() {
		return "MessageEntity [chatAccount=" + chatAccount + ", chatType="
				+ chatType + ", chatFrom=" + chatFrom + ", chatTo=" + chatTo
				+ ", chatRead=" + chatRead + ", chatTime=" + chatTime
				+ ", chatText=" + chatText + ", chatStyle=" + chatStyle
				+ ", chatPicPath=" + chatPicPath + "]";
	}

}
