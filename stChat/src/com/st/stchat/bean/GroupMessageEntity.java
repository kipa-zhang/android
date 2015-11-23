package com.st.stchat.bean;

public class GroupMessageEntity {
	private String messageId;
	private String chatAccount;
	private String chatType;
	private String chatFrom;
	private String roomJid;
	private String chatRead;
	private String chatTime;
	private String chatText;

	public GroupMessageEntity() {

	}

	public GroupMessageEntity(String messageId, String chatAccount,
			String chatType, String chatFrom, String roomJid, String chatRead,
			String chatTime, String chatText) {
		this.messageId = messageId;
		this.chatAccount = chatAccount;
		this.chatType = chatType;
		this.chatFrom = chatFrom;
		this.roomJid = roomJid;
		this.chatRead = chatRead;
		this.chatTime = chatTime;
		this.chatText = chatText;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getRoomJid() {
		return roomJid;
	}

	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
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
				+ chatType + ", chatFrom=" + chatFrom + ", roomJid=" + roomJid
				+ ", chatTime=" + chatTime + ", chatText=" + chatText + "]";
	}

}
