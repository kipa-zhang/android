package com.st.stchat.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.content.Context;
import android.util.Log;

import com.st.stchat.STChatApplication;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 
 * @author wu
 * 
 */
public class ChatRoomManager {
	private static XMPPConnection getConnection() {
		return XmppConnectionServer.getInstance().getConnection();
	}

	/**
	 * 发送消息
	 * 
	 * @param context
	 * @param message
	 * @param roomJid
	 */
	public static void sendMeassage(Context context, String message,
			String roomJid) {
		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return;
		try {
			MultiUserChat muc = new MultiUserChat(getConnection(), roomJid);
			muc.sendMessage(message);
			Log.i("MultiUserChat", "消息【" + message + "】发送成功........");
			return;
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			Log.i("MultiUserChat", "消息【" + message + "】发送失败........");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 创建房间
	 * 
	 * @param roomName
	 *            房间名称
	 */
	public static MultiUserChat createRoom(String user, String roomName,
			String password) {
		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return null;

		MultiUserChat muc = null;
		try {
			// 创建一个MultiUserChat
			muc = new MultiUserChat(getConnection(), roomName + "@conference."
					+ getConnection().getServiceName());
			// 创建聊天室
			muc.create(InfoUtils.getUser(STChatApplication.getInstance()));
			// 获得聊天室的配置表单
			Form form = muc.getConfigurationForm();
			// 根据原始表单创建一个要提交的新表单。
			Form submitForm = form.createAnswerForm();
			// 向要提交的表单添加默认答复
			for (Iterator<FormField> fields = form.getFields(); fields
					.hasNext();) {
				FormField field = (FormField) fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType())
						&& field.getVariable() != null) {
					// 设置默认值作为答复
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			// 设置聊天室的新拥有者
			List<String> owners = new ArrayList<String>();
			owners.add(getConnection().getUser());// 用户JID
			submitForm.setAnswer("muc#roomconfig_roomowners", owners);
			// 设置聊天室是持久聊天室，即将要被保存下来
			submitForm.setAnswer("muc#roomconfig_persistentroom", true);
			// 房间仅对成员开放
			submitForm.setAnswer("muc#roomconfig_membersonly", true);
			// 允许占有者邀请其他人
			submitForm.setAnswer("muc#roomconfig_allowinvites", true);
			if (!password.equals("")) {
				// 进入是否需要密码
				submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
						true);
				// 设置进入密码
				submitForm.setAnswer("muc#roomconfig_roomsecret", password);
			}
			// 能够发现占有者真实 JID 的角色
			// submitForm.setAnswer("muc#roomconfig_whois", "anyone");
			// 登录房间对话
			submitForm.setAnswer("muc#roomconfig_enablelogging", true);
			// 仅允许注册的昵称登录
			submitForm.setAnswer("x-muc#roomconfig_reservednick", false);
			// 允许使用者修改昵称
			submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
			// 允许用户注册房间
			submitForm.setAnswer("x-muc#roomconfig_registration", false);
			// 发送已完成的表单（有默认值）到服务器来配置聊天室
			muc.sendConfigurationForm(submitForm);
			muc.addMessageListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					packet.toXML();
					Message mess = (Message) packet;
					System.out.println(mess.getBody());
				}
			});
		} catch (XMPPException e) {
			e.printStackTrace();
			return null;
		}
		return muc;
	}

	/**
	 * 初始化会议室列表
	 */
	public static List<HostedRoom> getHostRooms() {
		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return null;
		Collection<HostedRoom> hostrooms = null;
		List<HostedRoom> roominfos = new ArrayList<HostedRoom>();
		try {
			new ServiceDiscoveryManager(getConnection());
			hostrooms = MultiUserChat.getHostedRooms(getConnection(),
					"conference." + getConnection().getServiceName());
			for (HostedRoom entry : hostrooms) {

				roominfos.add(entry);
				Log.i("room",
						"名字：" + entry.getName() + " - ID:" + entry.getJid());
			}
			Log.i("room", "服务会议数量:" + roominfos.size());
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return roominfos;
	};

	/**
	 * 获取已经加入的聊天室
	 */
	// public static void getMyRooms() {
	// System.out.println("getMyRooms() :" + getConnection().getUser());
	// // ServiceDiscoveryManager sdm = new
	// // ServiceDiscoveryManager((getConnection()));
	// // sdm.addFeature("http://jabber.org/protocol/disco#info");
	// // sdm.addFeature("http://jabber.org/protocol/disco#item");
	// // sdm.addFeature("http://jabber.org/protocol/muc");
	// // boolean supports = MultiUserChat.isServiceEnabled(getConnection(),
	// // getConnection().getUser());
	// //
	// // AddPacketListener();
	// // System.out.println(supports);
	// // //获取某用户所加入的聊天室
	// // // if(supports) {
	// // //List myAllRooms = new ArrayList<String>();
	// //AddPacketListener();
	// ServiceDiscoveryManager sdm = ServiceDiscoveryManager
	// .getInstanceFor(getConnection());
	// sdm.addFeature("http://jabber.org/protocol/disco#info");
	// sdm.addFeature("http://jabber.org/protocol/disco#item");
	// // 对于ServiceDiscoveryManager，需要加上muc协议
	// sdm.addFeature("http://jabber.org/protocol/muc");
	// Iterator<String> joinedRooms = MultiUserChat.getJoinedRooms(
	// getConnection(), getConnection().getUser());
	// // AddPacketListener();
	// System.out.println("join rooms size:" + joinedRooms);
	// while (joinedRooms.hasNext()) {
	// System.out.println("迭代器进来了吗");
	// String room = joinedRooms.next();
	// System.out.println("我已经加入的聊天室======" + room);
	// // myAllRooms.add(room);
	// }
	//
	// // }
	// // return myAllRooms;
	//
	// }

	// public static void AddPacketListener() {
	// PacketFilter filter = new IQTypeFilter(IQ.Type.RESULT);
	// getConnection().addPacketListener(new PacketListener() {
	// public void processPacket(Packet paramPacket) {
	//
	// if (paramPacket.getFrom().equalsIgnoreCase(getConnection().getUser())) {
	// String xml = paramPacket.toXML();
	// String from[] = null;
	//
	// System.out.println("我进来了呀==========" + xml);
	// from = paramPacket.getFrom().split("/");
	// Pattern pattern = Pattern.compile("<item jid=\"(.*?)/>");
	// Matcher matcher = pattern.matcher(xml);
	// String parts[] = null;
	//
	// while (matcher.find()) {
	// System.out.println("result-----"
	// + matcher.group(1).split("@"));
	// }
	// return;
	// }
	//
	// }
	// }, filter);
	//
	// }

	/**
	 * 加入会议室
	 * 
	 * @param user
	 *            昵称
	 * @param password
	 *            会议室密码
	 * @param roomsName
	 *            会议室名
	 */
	public static MultiUserChat joinMultiUserChat(String user, String roomsName) {
		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return null;
		try {
			MultiUserChat muc = new MultiUserChat(getConnection(), roomsName);
			// 聊天室服务将会决定要接受的历史记录数量
			DiscussionHistory history = new DiscussionHistory();
			history.setMaxStanzas(20);
			muc.join(user);
			Log.i("MultiUserChat", "会议室【" + roomsName + "】加入成功........");
			return muc;
		} catch (XMPPException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			Log.i("MultiUserChat", "会议室【" + roomsName + "】加入失败........");
			return null;
		}
	}

	/**
	 * 邀请别人加入聊天室
	 * 
	 * @param userName
	 *            加入者的名字
	 * @param roomsName
	 *            会议室名
	 * @throws XMPPException
	 */

	public static void inviteChatRoom(String roomName, String userName)
			throws XMPPException {
		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return;
		MultiUserChat muc2 = new MultiUserChat(getConnection(), roomName
				+ "@conference." + getConnection().getServiceName());
		// muc2.join(userName);
		// User2 listens for invitation rejections

		// muc2.addInvitationRejectionListener(new InvitationRejectionListener()
		// {
		//
		// @Override
		// public void invitationDeclined(String arg0, String arg1) {
		// // TODO Auto-generated method stub
		//
		// }
		// });

		// User2 invites user3 to join to the room
		muc2.invite(userName, "welcome");

	}

	/**
	 * 查询会议室成员名字
	 * 
	 * @param muc
	 * @throws XMPPException
	 */
	public static List<String> findMulitUser(String roomJid)
			throws XMPPException {

		if (!XmppConnectionServer.getInstance().getConnection().isConnected())
			return null;
		MultiUserChat muc = new MultiUserChat(getConnection(), roomJid);
		List<String> listUser = new ArrayList<String>();
		Collection<Affiliate> admins = muc.getOwners();
		Iterator<Affiliate> iterator = admins.iterator();
		while (iterator.hasNext()) {
			Affiliate dude = iterator.next();
			listUser.add(dude.getJid());
		}
		Iterator<Affiliate> iter = muc.getMembers().iterator();
		while (iter.hasNext()) {
			Affiliate dude = iter.next();
			listUser.add(dude.getJid());
		}
		// Iterator<String> it = muc.getOccupants();
		// //System.out.println("roomJid sum:"+ muc.getOccupantsCount());
		// // 遍历出聊天室人员名称
		// while (it.hasNext()) {
		// // 聊天室成员名字
		// String name = StringUtils.parseResource(it.next());
		// System.out.println("成员--" + name);
		// listUser.add(name);
		// }
		return listUser;
	}

}
