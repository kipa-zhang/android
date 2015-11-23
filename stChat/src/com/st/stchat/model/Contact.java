package com.st.stchat.model;

/**
 * 联系人实例
 * 
 * @author  
 * 
 */
public class Contact {

	private String accountName;// 账户
	private String contactName;// 联系人
	private String contactNickname;// 联系昵称

	public Contact(String accountName, String contactName,
			String contactNickname) {
		// TODO Auto-generated constructor stub
		this.accountName = accountName;
		this.contactName = contactName;
		this.contactNickname = contactNickname;
	}

	public String getContactNickname() {
		return contactNickname;
	}

	public void setContactNickname(String contactNickname) {
		this.contactNickname = contactNickname;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

}
