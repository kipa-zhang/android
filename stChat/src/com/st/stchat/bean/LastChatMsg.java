package com.st.stchat.bean;

/**
 * 与某人最后一条聊天的消息
 * 
 * @author  
 * 
 */
public final class LastChatMsg {

	private String id; // 主键
	private String title; // 标题
	private String content; // 最后内容
	private Integer status; // 最后状态 0已读 1未读
	private String from; // 最后通知来源
	private String to; // 最后通知去想
	private String noticeTime; // 最后通知时间
	private String noticeSum;// 收到未读消息总数、

	public String getNoticeSum() {
		return noticeSum;
	}

	public void setNoticeSum(String noticeSum) {
		this.noticeSum = noticeSum;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getNoticeTime() {
		return noticeTime;
	}

	public void setNoticeTime(String noticeTime) {
		this.noticeTime = noticeTime;
	}

}
