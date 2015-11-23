package com.st.stchat.listener;

import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import android.content.Intent;
import android.util.Log;

import com.st.stchat.STChatApplication;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.dao.ContactDao;
import com.st.stchat.dao.NoticeDao;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.StringUtil;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 花名册的监听类
 * 
 * @author  
 * 
 */
public class ContactRosterListener implements RosterListener {
	private static final String TAG = "ContactRosterListener";
	private Roster roster = XmppConnectionServer.getInstance().getConnection()
			.getRoster();

	// public static
	@Override
	public void presenceChanged(Presence presence) {
		System.out.println("----------presenceChanged-------");
		Log.e(TAG, "|getFrom() ---- " + presence.getFrom());
		Log.e(TAG, "|getStatus() ---- " + presence.getStatus());
		Log.e(TAG, "|getMode() ---- " + presence.getMode());
		Log.e(TAG, "|getType() ---- " + presence.getType());
		System.out.println("----------change OK-------");
		System.out.println("----------准备发送好友状态广播-------");
		// String statuStr = StatusUtils.getStatusStr("" + presence.getFrom(),
		// ""
		// + presence.getStatus(), "" + presence.getMode(),
		// "" + presence.getType());
		// 传过去四个参数 ，从哪来 ，状态的status的描述，状态的mode，状态的type类型
		String[] friendStatusArr = new String[] { "" + presence.getFrom(),
				"" + presence.getStatus(), "" + presence.getMode(),
				"" + presence.getType() };
		Intent intent = new Intent();
		intent.setAction("com.st.singlechat.friend.status");
		intent.putExtra("friend_status", friendStatusArr);
		STChatApplication.getInstance().sendBroadcast(intent);

		// 将状态添加进全局的集合
		if (XmppConnectionServer.map.containsKey(""
				+ StringUtil.getUserNameByJid(presence.getFrom()))) {
			XmppConnectionServer.map.remove(StringUtil
					.getUserNameByJid(presence.getFrom()));
		}
		XmppConnectionServer.map.put(
				"" + StringUtil.getUserNameByJid(presence.getFrom()),
				friendStatusArr);

		System.out.println("----------好友状态广播发送完毕-------");
		// System.out.println("发送花名册改变......");
		// Intent intent = new Intent();
		// intent.setAction("roster.presence.changed");
		// String subscriber = presence.getFrom().substring(0,
		// presence.getFrom().indexOf("/"));
		// RosterEntry entry = roster.getEntry(subscriber);
		// System.out.println("花名册改变 type = " + entry.getType().toString());
		// if (ContactManager.contacters.containsKey(subscriber)) {
		// System.out.println("花名册改变进来了吗");
		//
		// // 将状态改变之前的user广播出去
		// intent.putExtra(User.userKey,
		// ContactManager.contacters.get(subscriber));
		// ContactManager.contacters.remove(subscriber);
		// ContactManager.contacters.put(subscriber,
		// ContactManager.transEntryToUser(entry, roster));
		// }
		// sendBroadcast(intent);

	}

	@Override
	public void entriesUpdated(Collection<String> addresses) {
		for (String address : addresses) {
			System.out.println("发送花名册更新 address: " + address);
			Intent intent = new Intent();
			intent.putExtra("updateName", address);
			intent.setAction(ConstantUtils.ROSTER_UPDATED_ACTION);
			// 获得状态改变的entry
			RosterEntry userEntry = roster.getEntry(address);
			// User user = ContactManager.transEntryToUser(userEntry, roster);
			// if (ContactManager.contacters.get(address) != null) {
			// // 这里发布的是更新前的user
			// intent.putExtra(User.userKey,
			// ContactManager.contacters.get(address));
			// // 将发生改变的用户更新到userManager
			// ContactManager.contacters.remove(address);
			// ContactManager.contacters.put(address, user);
			// } else {
			// ContactManager.contacters.put(address, user);
			// }
			// 当好友关系更新为both时存到本地
			if (userEntry.getType().toString().equals("both")) {
				String accountName = InfoUtils.getUser(STChatApplication
						.getInstance());
				if (!ContactDao.getInstance().contactIsExist(accountName,
						address)) {
					ContactDao.getInstance().saveContact(userEntry, roster,
							accountName);
					STChatApplication.getInstance().sendBroadcast(intent);
				}
			}
		}
	}

	@Override
	public void entriesDeleted(Collection<String> addresses) {
		System.out.println("发送删除联系人......");
		Intent intent = new Intent();
		intent.setAction(ConstantUtils.ROSTER_DELETED_ACTION);
		for (String address : addresses) {
			// 清除本地数据库
			ChatMessageDao chatMessageDao = new ChatMessageDao();
			int delChatMessage = chatMessageDao.delChatWithSb(address
					.split("@")[0]);
			int delNotice = NoticeDao.getInstance().delContactReq(address);
			int delContact = ContactDao.getInstance()
					.delContact(
							InfoUtils.getUser(STChatApplication.getInstance()),
							address);
			// 清除该用户缓存
			STChatApplication.messageCache.remove(address.split("@")[0]);

			intent.putExtra("deleteName", address);
			if (delChatMessage >= 0 && delNotice >= 0 && delContact >= 0) {
				intent.putExtra("deleteResult", true);

			} else {
				intent.putExtra("deleteResult", false);
			}
			STChatApplication.getInstance().sendBroadcast(intent);
		}
	}

	@Override
	public void entriesAdded(Collection<String> addresses) {
		// System.out.println("发送添加联系人......");
		//
		// for (String address : addresses) {
		// System.out.println("address:" + address);
		// if (ContactManager.contacters.get(address) == null) {
		// RosterEntry userEntry = roster.getEntry(address);
		// User user = ContactManager.transEntryToUser(userEntry, roster);
		// ContactManager.contacters.put(address, user);
		// }
		// intent.putExtra("address", address);
		// Intent intent = new Intent();
		// intent.setAction("roster.added");
		// RosterEntry userEntry = roster.getEntry(address);
		// User user = ContactManager.transEntryToUser(userEntry, roster);
		// ContactManager.contacters.put(address, user);
		// intent.putExtra("address", address);
		// intent.putExtra(User.userKey, user);
		// sendBroadcast(intent);
		// }
	}

}
