package com.st.stchat.model;

/**
 * 聊天室实体类
 * 
 * @author  
 * 
 */
public class ChatRoom {

	// 当前账号
	private String accountName;
	// 房间的jid
	private String roomJid;
	// 数据的来源
	private String source;

	public ChatRoom(String source, String name, String roomJid) {
		this.source = source;
		this.accountName = name;
		this.roomJid = roomJid;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getRoomJid() {
		return roomJid;
	}

	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
	}

}
