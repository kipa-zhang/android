package com.st.stchat.manager;

import java.util.Collection;
import java.util.Iterator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.OfflineMessageManager;

import android.content.Context;

import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 
 * @author juwei 2014.11.26
 * 
 */
public class ChatManager {
	/**
	 * 利用聊天窗口对象调用该sendMessage发送消息
	 * 
	 * @param friend
	 *            好友名，不是JID
	 * @param listenter
	 *            MessageListener监听器可以传null
	 */
	public static boolean sendMeassage(Context context, String friend,
			Message message, MessageListener listenter) {
		Chat chat = XmppConnectionServer.getInstance().getFriendChat(friend,
				listenter);
		try {
			// String msgjson =
			// "{\"messageType\":\""+messageType+"\",\"chanId\":\""+chanId+"\",\"chanName\":\""+chanName+"\"}";
			// 在这里传json 字符串比较容易控制消息内容
			if (chat != null) {

				// Message mMessage = new Message();

				chat.sendMessage(message);
				System.out.println("发送出去的ID：" + message.getPacketID());
				// 如果发送成功，就在这里将发送状态改变

				return true;
			} else {
				return false;
			}

		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @author wu 获取离线消息
	 */

	public static void getOfflineMsg() {
		OfflineMessageManager offlineManager = new OfflineMessageManager(
				XmppConnectionServer.getInstance().getConnection());
		// System.out.println(offlineManager.supportsFlexibleRetrieval());
		// //获取支持灵活的检索状态，正常应该是为true，个人理解为服务器的离线消息功能支持开关
		try {
			System.out.println("离线消息数?: " + offlineManager.getMessageCount());
			Iterator<Message> it = offlineManager.getMessages();
			while (it.hasNext()) {
				Message message = it.next();
				System.out.println("离线消息=====" + message.getBody());
			}
		} catch (XMPPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			offlineManager.deleteMessages();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 上报服务器已获取，需删除服务器备份，不然下次登录会重新获取
		Presence presence = new Presence(Presence.Type.available);// 此时再上报用户状态
		XmppConnectionServer.getInstance().getConnection().sendPacket(presence);

	}

	public static void getUnreadContact() {
		Collection<RosterEntry> unfiledEntries = XmppConnectionServer
				.getInstance().getConnection().getRoster().getUnfiledEntries();
		Iterator<RosterEntry> iter = unfiledEntries.iterator();
		while (iter.hasNext()) {
			RosterEntry entry = iter.next();
			System.out.println("离线好友请求===" + entry.getName());
		}
	}

}
