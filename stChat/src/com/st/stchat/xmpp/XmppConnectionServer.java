package com.st.stchat.xmpp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.carbons.Carbon;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.forward.Forwarded;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.st.stchat.STChatApplication;
import com.st.stchat.dao.ChatRoomDao;
import com.st.stchat.listener.ChatFileTransferListener;
import com.st.stchat.listener.ChatRoomMsgListener;
import com.st.stchat.listener.ContactRosterListener;
import com.st.stchat.listener.ContacterRequestListener;
import com.st.stchat.listener.InvitationUserListener;
import com.st.stchat.listener.NotiPacketListener;
import com.st.stchat.listener.ReLoginConnectionListener;
import com.st.stchat.listener.SingleChatManagerListener;
import com.st.stchat.listener.SingleChatRRListener;
import com.st.stchat.manager.ChatRoomManager;
import com.st.stchat.model.ChatRoom;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.FormatTools;
import com.st.stchat.utils.InfoUtils;

/**
 * XmppConnection 工具类
 * 
 */
public class XmppConnectionServer {
	private static final String TAG = "XmppConnectionServer";

	// 客户端名称和类型。主要是向服务器登记，有点类似QQ显示iphone或者Android手机在线的功能
	// public static final String XMPP_IDENTITY_NAME = "XMPP";// 客户端名称
	// public static final String XMPP_IDENTITY_TYPE = "Android";// 客户端类型

	private static int SERVER_PORT = 5222;
	private static String SERVER_HOST = "127.0.0.1";
	private static XMPPConnection connection = null;
	private static String SERVER_NAME = "ubuntuserver4java";
	private static XmppConnectionServer xmppConnectionServer;
	private static ReLoginConnectionListener reLoginConnectionListener;
	private static SingleChatManagerListener chatManagerListener;
	private static ChatRoomMsgListener chatRoomMsgListener;
	private static InvitationUserListener invitationUserListener;
	private static NotiPacketListener notiPacketListener;
	private static ContacterRequestListener contacterRequestListener;
	private static ContactRosterListener contactRosterListener;
	private static ChatFileTransferListener chatFileTransferListener;
	private static FileTransferManager fileTransferManager;
	private static SmackAndroid smackAndroid;
	private static DeliveryReceiptManager mDeliveryReceiptManager;// 回执管理对象
	private static SingleChatRRListener mSingleChatRRListener;// 消息接收回执监听

	public static Map<String, String[]> map = new HashMap<String, String[]>();
	/**
	 * 聊天窗口管理map集合
	 */
	private static Map<String, Chat> chatManage;

	// static {
	// try {
	//
	// Log.i(TAG, "加载掉线自动重连接模块儿");
	// Class.forName("org.jivesoftware.smack.ReconnectionManager");
	// } catch (Exception e) {
	// Log.e(TAG, "Load ReconnectionManager error");
	// e.printStackTrace();
	// }
	// }

	// private ConnThread ct = new ConnThread();

	private XmppConnectionServer() {

	}

	/**
	 * 单例模式
	 * 
	 * @return 一个该服务类的单例对象，该对象持有自己
	 */
	public static XmppConnectionServer getInstance() {

		// 在这里拿到IP地址端口之类的
		XmppConnectionServer.SERVER_HOST = InfoUtils
				.getXmppHost(STChatApplication.getInstance());
		String s = InfoUtils.getXmppPort(STChatApplication.getInstance());
		XmppConnectionServer.SERVER_PORT = Integer.parseInt(s);

		if (xmppConnectionServer == null) {
			xmppConnectionServer = new XmppConnectionServer();
		}
		Log.i(TAG, "ReadyConn: " + " server----" + SERVER_HOST + ",port----"
				+ SERVER_PORT);
		return xmppConnectionServer;
	}

	/**
	 * 得到XMPPConnection连接(该连接永远不为null) 因为在向服务器取一些数据的时候你必须要通过getConnection()
	 * 来拿到connection ,所以在调用这个方法之前先， 做一个isConn()的判断，如果是false，
	 * 就千万不要去调用getConnection()
	 */
	public synchronized XMPPConnection getConnection() {
		if (connection == null) {
			System.out.println("创建新连接");
			openConnection();
		} else {
			System.out.println("使用之前的连接");
		}
		if (connection != null && !connection.isConnected()) {
			try {
				Log.e(TAG, "正在连接....");
				connection.connect();
				Log.e(TAG, "连接成功");
			} catch (XMPPException xe) {
				Log.e(TAG, "连接异常");
				// if (xe.getXMPPError().toString()
				// .equalsIgnoreCase("remote-server-error(502)")) {
				// Log.e(TAG, "创建连接时----服务器都连不上");
				// } else {
				xe.printStackTrace();
				// }
				// 在这里发送掉线广播
				// Intent intent = new Intent();
				// intent.setAction("com.st.singlechat.UNALIVE");
				// STChatApplication.getInstance().sendBroadcast(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 连接到服务器
			Log.i(TAG, "连接到Xmpp服务器 ：" + connection.isConnected());
		} else {
			System.out.println("持续连接");
		}
		System.out.println("connection 永远不可能等于空：" + (connection != null));
		return connection;
	}

	/**
	 * 配置连接并且创建连接
	 */
	private void openConnection() {
		if (smackAndroid == null) {
			smackAndroid = SmackAndroid.init(STChatApplication.getInstance());
		}
		Log.d(TAG, "开始连接配置");
		XMPPConnection.DEBUG_ENABLED = true;// 开启DEBUG模式
		// 配置连接
		ConnectionConfiguration config = new ConnectionConfiguration(
				SERVER_HOST, SERVER_PORT, SERVER_NAME);
		config.setReconnectionAllowed(false);
		config.setSendPresence(true); // 状态设为离线，目的为了取离线消息
		config.setCompressionEnabled(true);// 设置是否启用压缩
		config.setSASLAuthenticationEnabled(false); // 是否启用安全验证
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		config.setTruststorePath("/system/etc/security/cacerts.bks"); // 创建一个加密权限的备份文件
		config.setTruststorePassword("changeit");
		config.setTruststoreType("bks");
		// config.setRosterLoadedAtLogin(true);
		connection = new XMPPConnection(config);
		// 配置各种Provider，如果不配置，则会无法解析数据
		configureConnection(ProviderManager.getInstance());
		Log.d(TAG, "连接配置OK");
	}

	/**
	 * 在全局都调用该方法来判断当前是否允许与服务器交换数据
	 * 
	 * @return
	 */
	public boolean isConn() {
		boolean bool = false;
		if (connection != null) {
			bool = connection.isConnected();
		}
		return bool;
	}

	/**
	 * 关闭连接
	 */
	public boolean closeConnection() {
		if (connection != null) {
			if (smackAndroid != null) {
				smackAndroid.onDestroy();
			}
			// 清空聊天管理集合
			if (chatManage != null) {
				chatManage.clear();
			}
			// 移除聊天室消息监听
			if (chatRoomMsgListener != null) {
				connection.removePacketListener(chatRoomMsgListener);
			}
			// 移除房间邀请监听
			if (invitationUserListener != null) {
				MultiUserChat.removeInvitationListener(connection,
						invitationUserListener);
			}
			// 移除聊天图片的监听
			if (chatFileTransferListener != null) {
				if (getFileTransferManager() != null) {
					getFileTransferManager().removeFileTransferListener(
							chatFileTransferListener);
				}
			}
			// 移除聊天消息监听
			if (chatManagerListener != null) {
				connection.getChatManager().removeChatListener(
						chatManagerListener);
			}

			// 移除联系人请求监听
			if (contacterRequestListener != null) {
				connection.removePacketListener(contacterRequestListener);
			}
			// 移除花名册变化监听
			if (contactRosterListener != null) {
				connection.getRoster().removeRosterListener(
						contactRosterListener);
			}
			// 移除消息推送监听
			if (notiPacketListener != null) {
				connection.removePacketListener(notiPacketListener);
			}
			// 移除消息接收回执监听
			if (mSingleChatRRListener != null
					&& mDeliveryReceiptManager != null) {
				mDeliveryReceiptManager
						.removeReceiptReceivedListener(mSingleChatRRListener);
			}
			// 再移除连接监听
			if (reLoginConnectionListener != null) {
				connection.removeConnectionListener(reLoginConnectionListener);
			}
			// ct.destroy();
			connection.disconnect();

			if (map != null) {
				map.clear();
			}
			// ct= null;
			// 为了避免重复添加监听，接受重复消息,所以在这里将监听器还原
			chatManage = null;
			chatRoomMsgListener = null;
			invitationUserListener = null;
			chatFileTransferListener = null;
			chatManagerListener = null;
			reLoginConnectionListener = null;

			contacterRequestListener = null;
			contactRosterListener = null;
			notiPacketListener = null;
			fileTransferManager = null;
			mSingleChatRRListener = null;
			mDeliveryReceiptManager = null;
			connection = null;
			Log.i(TAG, "关闭连接,得到conn == null   : " + (connection == null));
			// System.out.println("退出之后拿到的用户  ：" + connection.getUser());
			return true;
		} else {
			Log.e(TAG, "conn = null时不需要关闭");
			return false;
		}
	}

	/**
	 * 登录
	 * 
	 * @param account
	 *            登录帐号
	 * @param password
	 *            登录密码
	 * @return 0、登陆成功 1、 登陆失败2、这个账号已经被登陆3、服务器没有返回结果
	 */
	public String login(String account, String password) {
		try {
			System.out.println("登陆之前连接成功了吗？" + connection.isConnected());
			connection.login(account, password);

			if (connection.isAuthenticated()) {
				// 发送连接在线广播
				Intent intent = new Intent();
				intent.setAction("com.st.singlechat.ALIVE");
				STChatApplication.getInstance().sendBroadcast(intent);
			}

			System.out.println("当前登陆user: " + connection.getUser());
			// ct.start();
			// 添加连接监听器
			reLoginConnectionListener = new ReLoginConnectionListener();
			connection.addConnectionListener(reLoginConnectionListener);

			// 为回执添加监听器
			mSingleChatRRListener = new SingleChatRRListener();
			getDeliveryReceiptManager().addReceiptReceivedListener(
					mSingleChatRRListener);
			// 添加通知栏消息通知监听器
			notiPacketListener = new NotiPacketListener();
			connection.addPacketListener(notiPacketListener,
					new MessageTypeFilter(
							org.jivesoftware.smack.packet.Message.Type.chat));
			// 花名册变化的监听
			contactRosterListener = new ContactRosterListener();
			connection.getRoster().addRosterListener(contactRosterListener);
			// 联系人请求监听
			contacterRequestListener = new ContacterRequestListener();
			connection.addPacketListener(contacterRequestListener,
					new PacketFilter() {
						@Override
						public boolean accept(Packet packet) {
							if (packet instanceof Presence) {
								Presence presence = (Presence) packet;
								if (presence.getType().equals(
										Presence.Type.subscribe)) {
									// System.out.println(Presence.Type.subscribe);
									return true;
								}
							}
							return false;
						}
					});

			// 添加聊天消息监听器
			chatManagerListener = new SingleChatManagerListener();
			connection.getChatManager().addChatListener(chatManagerListener);
			// 添加图片文件消息的监听
			chatFileTransferListener = new ChatFileTransferListener();
			getFileTransferManager().addFileTransferListener(
					chatFileTransferListener);
			// 监听房间邀请
			invitationUserListener = new InvitationUserListener();
			MultiUserChat.addInvitationListener(connection,
					invitationUserListener);
			// 给用户进入过的房间加监听
			List<ChatRoom> chatRooms = ChatRoomDao.getInstance()
					.getChatRoomByAccount(account);
			for (ChatRoom chatRoom : chatRooms) {
				chatRoomMsgListener = new ChatRoomMsgListener();
				MultiUserChat multiUserChat = ChatRoomManager
						.joinMultiUserChat(chatRoom.getAccountName(),
								chatRoom.getRoomJid());
				// 添加监听
				multiUserChat.addMessageListener(chatRoomMsgListener);
			}
			// 创建聊天窗口管理集合
			chatManage = new HashMap<String, Chat>();
			Log.v(TAG, "----------------------SERVER----------");
			Collection<RosterEntry> coll = connection.getRoster().getEntries();
			for (RosterEntry c : coll) {
				// Log.e(TAG, "getUser() : " + c.getUser()
				// + " \n ----getStatus() : " + c.getStatus());
				//
				// Log.e(TAG, "getPresenceFrom:"
				// + connection.getRoster().getPresence(c.getUser())
				// .getFrom());
				// Log.e(TAG, "getPresenceStatu : "
				// + connection.getRoster().getPresence(c.getUser())
				// .getStatus());

				// Presence.Mode.chat
				// Log.e(TAG, "SSS---:"+connection.getRoster().);
				// Log.e(TAG,
				// "FromStatusIS : "+connection.getRoster().getPresenceResource(c.getUser()).isAvailable());
				Log.v(TAG, "----------------------SERVER ok----------");
			}
			// 更改在线状态
			setPresence(1);
			configPingManager();
			return "0";
		} catch (XMPPException xe) {
			if (xe.getXMPPError().toString()
					.equalsIgnoreCase("remote-server-error(502)")) {
				Log.e(TAG, "登陆时----服务器都连不上");
				return "3";
			} else if (xe.getXMPPError().toString()
					.equalsIgnoreCase("not-authorized(401)")) {
				Log.e(TAG, "登陆时----用户验证不通过");
				return "4";
			} else {
				Log.e(TAG, "登陆时----登陆异常");
				xe.printStackTrace();
				return "1";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "5";
		}

	}

	/**
	 * 注册
	 * 
	 * @param account
	 *            注册帐号
	 * @param password
	 *            注册密码
	 * @return 0、注册成功 1、 注册失败 2、这个账号已经存在3、服务器没有返回结果
	 */
	public String regist(String account, String password) {
		Registration reg = new Registration();
		reg.setType(IQ.Type.SET);
		System.out.println("connection.getServiceName()====="
				+ connection.getServiceName());
		reg.setTo(connection.getServiceName());
		// 注意这里createAccount注册时，参数是UserName，不是jid，是"@"前面的部分。
		reg.setUsername(account);
		reg.setPassword(password);
		// 这边addAttribute不能为空，否则出错。所以做个标志是android手机创建的吧！！！！！
		reg.addAttribute("android", "geolo_createUser_android");
		PacketFilter filter = new AndFilter(new PacketIDFilter(
				reg.getPacketID()), new PacketTypeFilter(IQ.class));
		PacketCollector collector = connection.createPacketCollector(filter);
		connection.sendPacket(reg);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		// Stop queuing results停止请求results（是否成功的结果）
		collector.cancel();
		if (result == null) {
			Log.e("regist", "服务器没有响应");
			return "-1";
		} else if (result.getType() == IQ.Type.RESULT) {
			Log.v("regist", "regist success.");
			return "0";
		} else { // if (result.getType() == IQ.Type.ERROR)
			if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
				Log.e("regist", "IQ.Type.ERROR: "
						+ result.getError().toString());
				return "2";
			} else {
				Log.e("regist", "IQ.Type.ERROR: "
						+ result.getError().toString());
				return "1";
			}
		}
	}

	/**
	 * 更改用户状态
	 * 
	 * @param code
	 *            -1_未知错误,0_空闲,1_设置在线,2_设置离开,3_正在通话,4_自动离开,5_在路上
	 *            6_忙碌,7_隐身,8_设置离线
	 */
	public boolean setPresence(int code) {
		int ret = -100;
		Presence presence;
		switch (code) {

		case -1:
			presence = new Presence(Presence.Type.error);
			connection.sendPacket(presence);
			Log.v("state", "未知错误");
			ret = code;
			break;
		case 0:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.chat);
			presence.setStatus(ConstantUtils.STATUS_FREE_TO_CHAT);
			connection.sendPacket(presence);
			Log.v("state", "空闲");
			ret = code;
			break;
		case 1:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.available);
			connection.sendPacket(presence);
			Log.v("state", "在线");
			ret = code;
			break;

		case 2:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.away);
			presence.setStatus(ConstantUtils.STATUS_AWAY);
			connection.sendPacket(presence);
			Log.v("state", "设置离开");
			ret = code;
			break;
		case 3:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.away);
			presence.setStatus(ConstantUtils.STATUS_ON_PHONE);
			connection.sendPacket(presence);
			Log.v("state", "正在通话");
			ret = code;
			break;
		case 4:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.xa);
			presence.setStatus(ConstantUtils.STATUS_EXTENDED_AWAY);
			connection.sendPacket(presence);
			Log.v("state", "自动离开");
			ret = code;
			break;
		case 5:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.xa);
			presence.setStatus(ConstantUtils.STATUS_ON_THE_ROAD);
			connection.sendPacket(presence);
			Log.v("state", "在路上");
			ret = code;
			break;
		case 6:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.dnd);
			presence.setStatus(ConstantUtils.STATUS_DO_NOT_DISTURB);
			connection.sendPacket(presence);
			Log.v("state", "设置忙碌");
			ret = code;
			break;
		case 7:
			Roster roster = connection.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			for (RosterEntry entry : entries) {
				presence = new Presence(Presence.Type.unavailable);
				presence.setPacketID(Packet.ID_NOT_AVAILABLE);
				presence.setFrom(connection.getUser());
				presence.setTo(entry.getUser());
				connection.sendPacket(presence);
				Log.v("state", presence.toXML());
			}
			// 向同一用户的其他客户端发送隐身状态
			presence = new Presence(Presence.Type.unavailable);
			presence.setPacketID(Packet.ID_NOT_AVAILABLE);
			presence.setFrom(connection.getUser());
			presence.setTo(StringUtils.parseBareAddress(connection.getUser()));
			connection.sendPacket(presence);
			Log.v("state", "设置隐身");
			ret = code;
			break;
		case 8:
			presence = new Presence(Presence.Type.unavailable);
			connection.sendPacket(presence);
			Log.v("state", "设置离线");
			ret = code;
			break;

		case 17:
			presence = new Presence(Presence.Type.subscribe);
			connection.sendPacket(presence);
			Log.v("state", "订阅");
			ret = code;
			break;
		case 18:
			presence = new Presence(Presence.Type.subscribed);
			connection.sendPacket(presence);
			Log.v("state", "已订阅");
			ret = code;
			break;
		case 19:
			presence = new Presence(Presence.Type.unsubscribe);
			connection.sendPacket(presence);
			Log.v("state", "取消订阅");
			ret = code;
			break;
		case 20:
			presence = new Presence(Presence.Type.unsubscribed);
			connection.sendPacket(presence);
			Log.v("state", "已取消订阅");
			ret = code;
			break;
		default:
			break;
		}

		if (ret != -100) {
			return true;
		} else {
			return false;
		}
	}

	// /**
	// * 获取所有组
	// *
	// * @return 所有组集合
	// */
	// public List<RosterGroup> getGroups() {
	// List<RosterGroup> grouplist = new ArrayList<RosterGroup>();
	// Collection<RosterGroup> rosterGroup = connection.getRoster()
	// .getGroups();
	// Iterator<RosterGroup> i = rosterGroup.iterator();
	// while (i.hasNext()) {
	// grouplist.add(i.next());
	// }
	// return grouplist;
	// }

	// /**
	// * 获取某个组里面的所有好友
	// *
	// * @param roster
	// * @param groupName
	// * 组名
	// * @return
	// */
	// public List<RosterEntry> getEntriesByGroup(String groupName) {

	// List<RosterEntry> Entrieslist = new ArrayList<RosterEntry>();
	// RosterGroup rosterGroup = connection.getRoster().getGroup(
	// groupName);
	// Collection<RosterEntry> rosterEntry = rosterGroup.getEntries();
	// Iterator<RosterEntry> i = rosterEntry.iterator();
	// while (i.hasNext()) {
	// Entrieslist.add(i.next());
	// }
	// return Entrieslist;
	// }

	// /**
	// * 获取所有好友信息
	// *
	// * @return
	// */
	// public List<RosterEntry> getAllEntries() {

	// List<RosterEntry> Entrieslist = new ArrayList<RosterEntry>();
	// Collection<RosterEntry> rosterEntry = connection.getRoster()
	// .getEntries();
	// Iterator<RosterEntry> i = rosterEntry.iterator();
	// while (i.hasNext()) {
	// Entrieslist.add(i.next());
	// }
	// return Entrieslist;
	// }

	// /**
	// * 获取用户VCard信息
	// *
	// * @param connection
	// * @param user
	// * @return
	// * @throws XMPPException
	// */
	// public VCard getUserVCard(String userJID) {

	// VCard vcard = new VCard();
	// try {
	// vcard.load(connection, userJID);
	// } catch (XMPPException e) {
	// e.printStackTrace();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return vcard;
	// }

	// /**
	// * 添加一个分组
	// *
	// * @param groupName
	// * @return
	// */
	// public boolean addGroup(String groupName) {

	// try {
	// connection.getRoster().createGroup(groupName);
	// Log.v("addGroup", groupName + "創建成功");
	// return true;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	// /**
	// * 删除分组
	// *
	// * @param groupName
	// * @return
	// */
	// public boolean removeGroup(String groupName) {
	// return true;
	// }

	// /**
	// * 添加好友 无分组
	// *
	// * @param userName
	// * @param name
	// * @return
	// */
	// public boolean addUser(String userName, String name) {

	// try {
	// connection.getRoster().createEntry(userName, name, null);
	// return true;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	// /**
	// * 添加好友 有分组
	// *
	// * @param userName
	// * @param name
	// * @param groupName
	// * @return
	// */
	// public boolean addUser(String userName, String name, String groupName) {

	// try {
	// Presence subscription = new Presence(Presence.Type.subscribed);
	// subscription.setTo(userName);
	// userName += "@" + connection.getServiceName();
	// connection.sendPacket(subscription);
	// connection.getRoster().createEntry(userName, name,
	// new String[] { groupName });
	// return true;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	// /**
	// * 删除好友
	// *
	// * @param userName
	// * @return
	// */
	// public boolean removeUser(String userName) {

	// try {
	// RosterEntry entry = null;
	// if (userName.contains("@"))
	// entry = connection.getRoster().getEntry(userName);
	// else
	// entry = connection.getRoster().getEntry(
	// userName + "@" + connection.getServiceName());
	// if (entry == null)
	// entry = connection.getRoster().getEntry(userName);
	// connection.getRoster().removeEntry(entry);
	//
	// return true;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	// /**
	// * 查询用户
	// *
	// * @param userName
	// * @return
	// * @throws XMPPException
	// */
	// public List<HashMap<String, String>> searchUsers(String userName) {

	// HashMap<String, String> user = null;
	// List<HashMap<String, String>> results = new ArrayList<HashMap<String,
	// String>>();
	// try {
	// new ServiceDiscoveryManager(connection);
	//
	// UserSearchManager usm = new UserSearchManager(connection);
	//
	// Form searchForm = usm.getSearchForm(connection
	// .getServiceName());
	// Form answerForm = searchForm.createAnswerForm();
	// answerForm.setAnswer("userAccount", true);
	// answerForm.setAnswer("userPhote", userName);
	// ReportedData data = usm.getSearchResults(answerForm, "search"
	// + connection.getServiceName());
	//
	// Iterator<Row> it = data.getRows();
	// Row row = null;
	// while (it.hasNext()) {
	// user = new HashMap<String, String>();
	// row = it.next();
	// user.put("userAccount", row.getValues("userAccount").next()
	// .toString());
	// user.put("userPhote", row.getValues("userPhote").next()
	// .toString());
	// results.add(user);
	// // 若存在，则有返回,UserName一定非空，其他两个若是有设，一定非空
	// }
	// } catch (XMPPException e) {
	// e.printStackTrace();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return results;
	// }

	// /**
	// * 修改心情
	// *
	// * @param connection
	// * @param status
	// */
	// public void changeStateMessage(String status) {

	// Presence presence = new Presence(Presence.Type.available);
	// presence.setStatus(status);
	// connection.sendPacket(presence);
	// }

	/**
	 * 文件转字节
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private byte[] getFileBytes(File file) throws IOException {
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			int bytes = (int) file.length();
			byte[] buffer = new byte[bytes];
			int readBytes = bis.read(buffer);
			if (readBytes != buffer.length) {
				throw new IOException("Entire file not read");
			}
			return buffer;
		} finally {
			if (bis != null) {
				bis.close();
			}
		}
	}

	// /**
	// * 向服务器删除当前用户
	// *
	// * @return
	// */
	// public boolean deleteAccount() {

	// try {
	// connection.getAccountManager().deleteAccount();
	// return true;
	// } catch (XMPPException e) {
	// return false;
	// }
	// }

	// /**
	// * 修改密码
	// *
	// * @return
	// */
	// public boolean changePassword(String pwd) {

	// }
	// try {
	// connection.getAccountManager().changePassword(pwd);
	// return true;
	// } catch (XMPPException e) {
	// e.printStackTrace();
	// return false;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	// /**
	// * 初始化会议室列表
	// */
	// public List<HostedRoom> getHostRooms() {

	// Collection<HostedRoom> hostrooms = null;
	// List<HostedRoom> roominfos = new ArrayList<HostedRoom>();
	// try {
	// new ServiceDiscoveryManager(connection);
	// hostrooms = MultiUserChat.getHostedRooms(connection,
	// connection.getServiceName());
	// for (HostedRoom entry : hostrooms) {
	// roominfos.add(entry);
	// Log.i("room",
	// "名字：" + entry.getName() + " - ID:" + entry.getJid());
	// }
	// Log.i("room", "服务会议数量:" + roominfos.size());
	// } catch (XMPPException e) {
	// e.printStackTrace();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return roominfos;
	// }

	// /**
	// * 创建房间
	// *
	// * @param roomName
	// * 房间名称
	// */
	// public MultiUserChat createRoom(String user, String roomName,
	// String password) {

	//
	// MultiUserChat muc = null;
	// try {
	// // 创建一个MultiUserChat
	// muc = new MultiUserChat(connection, roomName + "@conference."
	// + connection.getServiceName());
	// // 创建聊天室
	// muc.create(roomName);
	// // 获得聊天室的配置表单
	// Form form = muc.getConfigurationForm();
	// // 根据原始表单创建一个要提交的新表单。
	// Form submitForm = form.createAnswerForm();
	// // 向要提交的表单添加默认答复
	// for (Iterator<FormField> fields = form.getFields(); fields
	// .hasNext();) {
	// FormField field = (FormField) fields.next();
	// if (!FormField.TYPE_HIDDEN.equals(field.getType())
	// && field.getVariable() != null) {
	// // 设置默认值作为答复
	// submitForm.setDefaultAnswer(field.getVariable());
	// }
	// }
	// // 设置聊天室的新拥有者
	// List<String> owners = new ArrayList<String>();
	// owners.add(connection.getUser());// 用户JID
	// submitForm.setAnswer("muc#roomconfig_roomowners", owners);
	// // 设置聊天室是持久聊天室，即将要被保存下来
	// submitForm.setAnswer("muc#roomconfig_persistentroom", true);
	// // 房间仅对成员开放
	// submitForm.setAnswer("muc#roomconfig_membersonly", false);
	// // 允许占有者邀请其他人
	// submitForm.setAnswer("muc#roomconfig_allowinvites", true);
	// if (!password.equals("")) {
	// // 进入是否需要密码
	// submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
	// true);
	// // 设置进入密码
	// submitForm.setAnswer("muc#roomconfig_roomsecret", password);
	// }
	// // 能够发现占有者真实 JID 的角色
	// // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
	// // 登录房间对话
	// submitForm.setAnswer("muc#roomconfig_enablelogging", true);
	// // 仅允许注册的昵称登录
	// submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
	// // 允许使用者修改昵称
	// submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
	// // 允许用户注册房间
	// submitForm.setAnswer("x-muc#roomconfig_registration", false);
	// // 发送已完成的表单（有默认值）到服务器来配置聊天室
	// muc.sendConfigurationForm(submitForm);
	// return muc;
	// } catch (XMPPException e) {
	// e.printStackTrace();
	// return null;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return null;
	// }
	//
	// }

	// /**
	// * 加入会议室
	// *
	// * @param user
	// * 昵称
	// * @param password
	// * 会议室密码
	// * @param roomsName
	// * 会议室名
	// */
	// public MultiUserChat joinMultiUserChat(String user, String roomsName,
	// String password) {

	// try {
	// // 使用XMPPConnection创建一个MultiUserChat窗口
	// MultiUserChat muc = new MultiUserChat(connection, roomsName
	// + "@conference." + connection.getServiceName());
	// // 聊天室服务将会决定要接受的历史记录数量
	// DiscussionHistory history = new DiscussionHistory();
	// history.setMaxChars(0);
	// // history.setSince(new Date());
	// // 用户加入聊天室
	// muc.join(user, password, history,
	// SmackConfiguration.getPacketReplyTimeout());
	// Log.i("MultiUserChat", "会议室【" + roomsName + "】加入成功........");
	// return muc;
	// } catch (XMPPException e) {
	// e.printStackTrace();
	// Log.i("MultiUserChat", "会议室【" + roomsName + "】加入失败........");
	// return null;
	// } catch (Exception e) {
	// return null;
	// }
	// }

	// /**
	// * 查询会议室成员名字
	// *
	// * @param muc
	// */
	// public List<String> findMulitUser(MultiUserChat muc) {

	// List<String> listUser = new ArrayList<String>();
	// Iterator<String> it = muc.getOccupants();
	// // 遍历出聊天室人员名称
	// while (it.hasNext()) {
	// // 聊天室成员名字
	// String name = StringUtils.parseResource(it.next());
	// listUser.add(name);
	// }
	// return listUser;
	// }
	/**
	 * 拿到消息回执管理器
	 * 
	 * @return
	 */
	private DeliveryReceiptManager getDeliveryReceiptManager() {
		if (mDeliveryReceiptManager == null) {
			mDeliveryReceiptManager = DeliveryReceiptManager
					.getInstanceFor(connection);
		}
		// 初始化回执对象为消息接收自动回执
		mDeliveryReceiptManager.enableAutoReceipts();
		return mDeliveryReceiptManager;
	}

	/**
	 * 得到文件传输管理器
	 * 
	 * @return
	 */
	private FileTransferManager getFileTransferManager() {
		// 创建文件传输管理器
		if (fileTransferManager == null) {
			fileTransferManager = new FileTransferManager(connection);
		}
		return fileTransferManager;
	}

	/**
	 * 得到对某用户的文件输出传输器
	 * 
	 * @param user
	 * @param filePath
	 */
	public OutgoingFileTransfer getOutgoingFileTransfer(String user) {
		if (getFileTransferManager() == null) {
			Log.e(TAG, "文件传输管理器:----当前无法得到文件传输管理器");
			return null;
		}
		// 创建输出的文件传输
		OutgoingFileTransfer transfer = getFileTransferManager()
				.createOutgoingFileTransfer(user);
		return transfer;

	}

	// /**
	// * 获取离线消息
	// *
	// * @return
	// */
	// public Map<String, List<HashMap<String, String>>> getHisMessage() {

	// Map<String, List<HashMap<String, String>>> offlineMsgs = null;
	//
	// try {
	// OfflineMessageManager offlineManager = new OfflineMessageManager(
	// connection);
	// Iterator<Message> it = offlineManager.getMessages();
	//
	// int count = offlineManager.getMessageCount();
	// if (count <= 0)
	// return null;
	// offlineMsgs = new HashMap<String, List<HashMap<String, String>>>();
	//
	// while (it.hasNext()) {
	// Message message = it.next();
	// String fromUser = StringUtils.parseName(message.getFrom());
	// ;
	// HashMap<String, String> histrory = new HashMap<String, String>();
	// histrory.put("useraccount",
	// StringUtils.parseName(connection.getUser()));
	// histrory.put("friendaccount", fromUser);
	// histrory.put("info", message.getBody());
	// histrory.put("type", "left");
	// if (offlineMsgs.containsKey(fromUser)) {
	// offlineMsgs.get(fromUser).add(histrory);
	// } else {
	// List<HashMap<String, String>> temp = new ArrayList<HashMap<String,
	// String>>();
	// temp.add(histrory);
	// offlineMsgs.put(fromUser, temp);
	// }
	// }
	// offlineManager.deleteMessages();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return offlineMsgs;
	// }

	/**
	 * 判断OpenFire用户的状态 strUrl : url格式 -
	 * http://my.openfire.com:9090/plugins/presence
	 * /status?jid=user1@SERVER_NAME&type=xml 返回值 : 0 - 用户不存在; 1 - 用户在线; 2 -用户离线
	 * 说明 ：必须要求 OpenFire加载 presence 插件，同时设置任何人都可以访问
	 */
	public int isUserOnLine(String user) {
		String urlStr = "http://" + SERVER_HOST
				+ ":9090/plugins/presence/status?" + "jid=" + user + "@"
				+ connection.getServiceName() + "&type=xml";
		int shOnLineState = 0; // 不存在
		try {
			URL url = new URL(urlStr);
			URLConnection urlConn = url.openConnection();
			if (urlConn != null) {
				BufferedReader oIn = new BufferedReader(new InputStreamReader(
						urlConn.getInputStream()));
				if (null != oIn) {
					String strFlag = oIn.readLine();
					oIn.close();
					System.out.println("strFlag" + strFlag);
					if (strFlag.indexOf("type=\"unavailable\"") >= 0) {
						shOnLineState = 2;
					}
					if (strFlag.indexOf("type=\"error\"") >= 0) {
						shOnLineState = 0;
					} else if (strFlag.indexOf("priority") >= 0
							|| strFlag.indexOf("id=\"") >= 0) {
						shOnLineState = 1;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return shOnLineState;
	}

	/**
	 * 加入providers的函数 ASmack在/META-INF缺少一个smack.providers 文件
	 * 
	 * @param pm
	 */
	private void configureConnection(ProviderManager pm) {
		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());
		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient",
					"Can't load class for org.jivesoftware.smackx.packet.Time");
		}
		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());
		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());
		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());
		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());
		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());
		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());
		// Delayed Delivery
		pm.addExtensionProvider("delay", "urn:xmpp:delay",
				new DelayInfoProvider());
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());
		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());
		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());
		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());
		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());
		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());
		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
		// add carbons and forwarding
		pm.addExtensionProvider("forwarded", Forwarded.NAMESPACE,
				new Forwarded.Provider());
		pm.addExtensionProvider("sent", Carbon.NAMESPACE, new Carbon.Provider());
		pm.addExtensionProvider("received", Carbon.NAMESPACE,
				new Carbon.Provider());
		// add delivery receipts
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT,
				DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT,
				DeliveryReceipt.NAMESPACE,
				new DeliveryReceiptRequest.Provider());
		// add XMPP Ping (XEP-0199)
		pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
	}

	/**
	 * 获取或创建聊天窗口
	 * 
	 * @param friend
	 *            好友名
	 * @param listenter
	 *            聊天监听器
	 * @return
	 */
	public Chat getFriendChat(String friend, MessageListener listenter) {
		if (!connection.isAuthenticated()) {
			return null;
		}
		/** 判断是否创建聊天窗口 */
		for (String fristr : chatManage.keySet()) {
			if (fristr.equals(friend)) {
				// 存在聊天窗口，则返回对应聊天窗口
				return chatManage.get(fristr);
			}
		}
		/** 创建聊天窗口 */
		Chat chat = connection.getChatManager().createChat(
				friend + "@" + connection.getServiceName(), listenter);
		/** 添加聊天窗口到chatManage */
		chatManage.put(friend, chat);
		return chat;
	}

	/**
	 * 获取用户头像信息
	 * 
	 * @param connection
	 * @param user
	 *            当前登陆的用户名
	 * @return 该头像的Bitmap对象
	 */
	public Bitmap getUserImage(String user) {
		ByteArrayInputStream bais = null;
		try {
			VCard vcard = new VCard();
			// 加入这句代码，解决No VCard for
			// ProviderManager.getInstance().addIQProvider("vCard",
			// "vcard-temp",
			// new org.jivesoftware.smackx.provider.VCardProvider());
			if (user == "" || user == null || user.trim().length() <= 0) {
				Log.e(TAG, "用户名无效");
				return null;
			}
			vcard.load(connection, user + "@" + connection.getServiceName());
			if (vcard == null || vcard.getAvatar() == null) {
				// Log.e(TAG,
				// "!!!! vcard == null ? " + (vcard == null)
				// + "!!!! vcard.getAvatar() == null ? "
				// + (vcard.getAvatar() == null));
				Log.e(TAG, user + "没有头像图片资源");
				return null;
			}
			byte[] avatarBytes = vcard.getAvatar();
			bais = new ByteArrayInputStream(avatarBytes);
			return FormatTools.getInstance().InputStream2Bitmap(bais);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "异常在了44444444");
			return null;
		} finally {
			try {
				if (bais != null) {
					bais.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 对当前登陆的用户设置头像
	 * 
	 * @param file
	 *            照片路径
	 * @return 是否设置成功，成功，返回头像的Bitmap,如果不成功，则为 null
	 */
	public Bitmap changeUserImage(File file) {
		VCard vcard = new VCard();
		ByteArrayInputStream bais = null;
		try {
			vcard.load(connection);
			byte[] bytes;
			bytes = getFileBytes(file);
			vcard.setFirstName(InfoUtils.getUser(STChatApplication
					.getInstance()));
			vcard.setAvatar(bytes, "image/jpeg");
			vcard.save(connection);
			Log.e(TAG, "save ok----save ok----save ok----save ok");
			bais = new ByteArrayInputStream(vcard.getAvatar());
			return FormatTools.getInstance().InputStream2Bitmap(bais);
		} catch (XMPPException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (bais != null) {
					bais.close();
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	public static void printAllStaticIsNull() {
		Log.e(TAG, "SERVER_HOST == " + (SERVER_HOST == null ? null : ""));
		Log.e(TAG, "connection == " + (connection == null ? null : ""));
		Log.e(TAG, "SERVER_NAME == " + (SERVER_NAME == null ? null : ""));
		Log.e(TAG, "xmppConnectionServer == "
				+ (xmppConnectionServer == null ? null : ""));
		Log.e(TAG, "reLoginConnectionListener == "
				+ (reLoginConnectionListener == null ? null : ""));
		Log.e(TAG, "chatManagerListener == "
				+ (chatManagerListener == null ? null : ""));
		Log.e(TAG, "chatRoomMsgListener == "
				+ (chatRoomMsgListener == null ? null : ""));
		Log.e(TAG, "invitationUserListener == "
				+ (invitationUserListener == null ? null : ""));
		Log.e(TAG, "notiPacketListener == "
				+ (notiPacketListener == null ? null : ""));
		Log.e(TAG, "contacterRequestListener == "
				+ (contacterRequestListener == null ? null : ""));
		Log.e(TAG, "contactRosterListener == "
				+ (contactRosterListener == null ? null : ""));
		Log.e(TAG, "chatFileTransferListener == "
				+ (chatFileTransferListener == null ? null : ""));
		Log.e(TAG, "fileTransferManager == "
				+ (fileTransferManager == null ? null : ""));
		Log.e(TAG, "smackAndroid == " + (smackAndroid == null ? null : ""));
		Log.e(TAG, "mDeliveryReceiptManager == "
				+ (mDeliveryReceiptManager == null ? null : ""));
		Log.e(TAG, "mSingleChatRRListener == "
				+ (mSingleChatRRListener == null ? null : ""));
		Log.e(TAG, "map == " + (map == null ? null : ""));
		Log.e(TAG, "chatManage == " + (chatManage == null ? null : ""));

	}

	/**
	 * 设置对服务器的ping包回执
	 */
	public static void configPingManager() {

		// reference PingManager, set ping flood protection to 10s
		PingManager.getInstanceFor(connection).setPingMinimumInterval(1 * 1000);
	}

	private class ConnThread extends Thread {
		@Override
		public void run() {
			while (true) {

				if (connection != null) {
					System.out.println(""
							+ (connection.isConnected() ? "有连接" : "无连接"));
				}
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
