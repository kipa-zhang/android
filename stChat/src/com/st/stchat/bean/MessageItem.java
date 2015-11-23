package com.st.stchat.bean;

import com.st.stchat.slideview.SlideView;

public class MessageItem {
	private int iconRes;
	private String title;
	private String msg;
	private String time;
	private SlideView slideView;
	private int unReadSum = 0;

	public int getIconRes() {
		return iconRes;
	}

	public void setIconRes(int iconRes) {
		this.iconRes = iconRes;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public SlideView getSlideView() {
		return slideView;
	}

	public void setSlideView(SlideView slideView) {
		this.slideView = slideView;
	}

	public int getUnReadSum() {
		return unReadSum;
	}

	public void setUnReadSum(int unReadSum) {
		this.unReadSum = unReadSum;
	}

}
