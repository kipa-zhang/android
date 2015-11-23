package com.st.stchat.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.search.UserSearchManager;

import com.st.stchat.model.User;
import com.st.stchat.utils.StringUtil;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 联系人管理类
 * 
 * @author  
 * 
 */
public class ContactManager {
	/**
	 * 保存着所有的联系人信息
	 */
	public static Map<String, User> contacters = null;

	public static void init(Connection connection) {
		contacters = new HashMap<String, User>();
		for (RosterEntry entry : connection.getRoster().getEntries()) {
			User user = transEntryToUser(entry, connection.getRoster());
			if (user.getType().toString().equals("both")) {
				contacters.put(entry.getUser().split("@")[0],
						transEntryToUser(entry, connection.getRoster()));
			}
		}
	}

	public static void destroy() {
		contacters = null;
	}

	/**
	 * 获得所有的联系人列表根据type 是both的用户
	 * 
	 * @return
	 */
	// Bug:当这里contacters == null 的时候，后面的代码继续执行，就会包空指针异常
	public static List<User> getContacterList() {
		List<User> userList = new ArrayList<User>();
		if (contacters != null) {
			for (String key : contacters.keySet()) {
				if (contacters.get(key).getType().toString().equals("both")) {
					System.out.println("contacters map key:" + key);
					userList.add(contacters.get(key));
				}
			}
		}
		return userList;
	}

	/**
	 * 获取所有类型的用户to from none both
	 * 
	 * @return
	 */
	public static List<User> getAllContactList() {
		List<User> userList = new ArrayList<User>();
		if (contacters != null) {
			for (String key : contacters.keySet()) {
				userList.add(contacters.get(key));
			}
		}
		return userList;
	}

	/**
	 * 从花名册中删除用户
	 * 
	 * @param userJid
	 * @throws XMPPException
	 */
	public static boolean deleteUser(String userName) throws XMPPException {

		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return false;

		Roster roster = XmppConnectionServer.getInstance().getConnection()
				.getRoster();

		RosterEntry entry = roster.getEntry(StringUtil.getJidByName(userName,
				XmppConnectionServer.getInstance().getConnection()
						.getServiceName()));
		if (entry != null) {
			XmppConnectionServer.getInstance().getConnection().getRoster()
					.removeEntry(entry);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * 根据jid获得用户昵称
	 * 
	 * @param Jid
	 * @param connection
	 * @return
	 */
	// public static User getNickname1(String Jid, XMPPConnection connection) {
	// Roster roster = connection.getRoster();
	// for (RosterEntry entry : roster.getEntries()) {
	// String params = entry.getUser();
	// if (params.split("/")[0].equalsIgnoreCase(Jid)) {
	// return transEntryToUser(entry, roster);
	// }
	// }
	// return null;
	//
	// }

	/**
	 * 根据RosterEntry创建一个User
	 * 
	 * @param entry
	 * @return
	 */
	public static User transEntryToUser(RosterEntry entry, Roster roster) {
		User user = new User();
		if (entry.getName() == null) {
			user.setName(StringUtil.getUserNameByJid(entry.getUser()));
		} else {
			user.setName(entry.getName());
		}
		user.setJID(entry.getUser());
		Presence presence = roster.getPresence(entry.getUser());
		user.setFrom(presence.getFrom());
		user.setStatus(presence.getStatus());
		user.setSize(entry.getGroups().size());
		user.setAvailable(presence.isAvailable());
		user.setType(entry.getType().toString());
		return user;
	}

	/**
	 * 添加好友 无分组
	 * 
	 * @author wu
	 * @param userName
	 *            userJID
	 * @param name
	 *            nikename
	 * @return
	 */
	public static boolean addUser(String userName, String name) {

		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return false;
		try {
			Roster roster = XmppConnectionServer.getInstance().getConnection()
					.getRoster();
			roster.setSubscriptionMode(SubscriptionMode.accept_all);
			Presence subscription = new Presence(Presence.Type.subscribed);
			userName += "@"
					+ XmppConnectionServer.getInstance().getConnection()
							.getServiceName();
			subscription.setTo(userName);
			XmppConnectionServer.getInstance().getConnection()
					.sendPacket(subscription);
			roster.createEntry(userName, name, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 查询用户
	 * 
	 * @author wu
	 * @param userName
	 * @return
	 * @throws XMPPException
	 */
	@SuppressWarnings("deprecation")
	public static List<String> searchUsers(String userName) {
		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return null;
		List<String> list = new ArrayList<String>();
		try {
			new ServiceDiscoveryManager(XmppConnectionServer.getInstance()
					.getConnection());

			UserSearchManager usm = new UserSearchManager(XmppConnectionServer
					.getInstance().getConnection());

			Form searchForm = usm.getSearchForm("search."
					+ XmppConnectionServer.getInstance().getConnection()
							.getServiceName());
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("search", userName);
			ReportedData data = usm.getSearchResults(answerForm, "search."
					+ XmppConnectionServer.getInstance().getConnection()
							.getServiceName());
			Iterator<Row> rows = data.getRows();
			Row row = null;
			while (rows.hasNext()) {
				row = rows.next();
				list.add(row.getValues("Username").next().toString());
			}
		} catch (XMPPException e) {
			e.printStackTrace();

		}

		return list;
	}

	/**
	 * @author wu
	 * @param userName
	 * @return
	 */
	public static boolean checkContactIsExist(String userName) {
		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return false;

		userName += "@"
				+ XmppConnectionServer.getInstance().getConnection()
						.getServiceName();
		List<User> contacterList = getContacterList();
		for (int i = 0; i < contacterList.size(); i++) {
			if (contacterList.get(i).getJID().equalsIgnoreCase(userName)) {
				return true;
			}
		}

		return false;

	}

}
