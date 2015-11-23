package com.st.stchat.service;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.activity.NewFriendActivity;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.dao.NoticeDao;
import com.st.stchat.manager.ContactManager;
import com.st.stchat.model.Notice;
import com.st.stchat.model.User;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.NotificationUtil;
import com.st.stchat.utils.StringUtil;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 联系人的服务
 * 
 * @author  
 * 
 */
public class ContactService extends Service {

	private Roster roster = null;
	private Context context;
	SoundPool sp; // 声明SoundPool的引用
	HashMap<Integer, Integer> hm; // 声明一个HashMap来存放声音文件
	int currStreamId;// 当前正播放的streamId
	private NotificationManager notificationManager;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		context = this;
		addSubscriptionListener();
		initSoundPool();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		initRoster();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// 释放资源
		if (XmppConnectionServer.getInstance().isConn()) {
			XmppConnectionServer.getInstance().getConnection()
					.removePacketListener(subscriptionPacketListener);
		}
		// ContactManager.destroy();
		super.onDestroy();
	}

	/**
	 * 添加一个监听，监听好友添加请求。
	 */
	private void addSubscriptionListener() {
		PacketFilter filter = new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				if (packet instanceof Presence) {
					Presence presence = (Presence) packet;
					if (presence.getType().equals(Presence.Type.subscribe)) {
						System.out.println(Presence.Type.subscribe);
						return true;
					}
				}
				return false;
			}
		};
		if (XmppConnectionServer.getInstance().isConn()) {
			XmppConnectionServer.getInstance().getConnection()
					.addPacketListener(subscriptionPacketListener, filter);
		}
	}

	/**
	 * 初始化花名册 服务重启时，更新花名册
	 */
	private void initRoster() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (XmppConnectionServer.getInstance().isConn()) {
			roster = XmppConnectionServer.getInstance().getConnection()
					.getRoster();
			roster.removeRosterListener(rosterListener);
			roster.addRosterListener(rosterListener);
		}

	}

	private PacketListener subscriptionPacketListener = new PacketListener() {

		@Override
		public void processPacket(Packet packet) {
			// TODO Auto-generated method stub
			System.out.println("获取到一个数据包");
			Map<String, String> userMap = InfoUtils
					.getUserAccount(STChatApplication.getInstance());
			String user = userMap.get("userAccountName");
			if (StringUtil.getUserNameByJid(packet.getFrom()).equalsIgnoreCase(
					user)) {
				return;
			}
			String from = packet.getFrom();
			System.out.println("好友请求来自于=========" + from);
			// if (Roster.getDefaultSubscriptionMode().equals(
			// SubscriptionMode.accept_all)) {
			// System.out.println("申请加您为好友1");
			// Presence subscription = new Presence(Presence.Type.subscribe);
			// subscription.setTo(packet.getFrom());
			// if(XmppConnectionServer.getInstance().isConn()){
			// XmppConnectionServer.getInstance().getConnection()
			// .sendPacket(subscription);
			// }
			// System.out.println(StringUtil.getUserNameByJid(packet.getFrom()+
			// "申请加您为好友1"));
			// }else{

			// String from =
			// StringUtil.getJidByName(StringUtil.getUserNameByJid(packet.getFrom()),
			// InfoUtils.getXmppHost(STChatApplication.getInstance()));
			// String to =
			// StringUtil.getJidByName(StringUtil.getUserNameByJid(packet.getTo()),
			// InfoUtils.getXmppHost(STChatApplication.getInstance()));
			InfoUtils.savePacketDomain(STChatApplication.getInstance(),
					packet.getFrom());
			System.out
					.println("domian-----"
							+ InfoUtils.getPacketDomain(STChatApplication
									.getInstance()));
			NoticeDao dao = new NoticeDao();
			Notice notice = new Notice();
			notice.setTitle(StringUtil.getUserNameByJid(packet.getFrom()));
			notice.setNoticeType(Notice.ADD_FRIEND);
			notice.setContent(StringUtil.getUserNameByJid(packet.getFrom())
					+ " applied to add you as a friend");
			notice.setFrom(packet.getFrom());
			notice.setTo(packet.getTo());
			// notice.setTo(user);
			notice.setStatus(Notice.UNREAD);
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			notice.setNoticeTime(format.format(new Date()));
			long noticeId = dao.saveNotice(notice);
			if (noticeId != -1) {
				Intent intent = new Intent();
				intent.setAction("roster.subscribe");
				intent.putExtra("notice", notice);
				sendBroadcast(intent);
				NotificationUtil.setNotiType(notificationManager, context,
						R.drawable.ic_launcher, "StChat", notice.getContent(),
						NewFriendActivity.class, from, "roster");
			}
			// }
		}
	};

	private RosterListener rosterListener = new RosterListener() {

		@Override
		public void presenceChanged(Presence presence) {
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
			System.out.println("发送花名册更新......");
			for (String address : addresses) {
				System.out.println("发送花名册更新 address: " + address);
				Intent intent = new Intent();
				intent.setAction("roster.updated");
				// 获得状态改变的entry
				RosterEntry userEntry = roster.getEntry(address);
				User user = ContactManager.transEntryToUser(userEntry, roster);
				System.out.println("花名册更新 type = "
						+ userEntry.getType().toString());
				if (ContactManager.contacters.get(address) != null) {
					System.out.println("花名册更新进来了吗");
					// 这里发布的是更新前的user
					intent.putExtra(User.userKey,
							ContactManager.contacters.get(address));
					// 将发生改变的用户更新到userManager
					ContactManager.contacters.remove(address);
					ContactManager.contacters.put(address, user);
				} else {
					ContactManager.contacters.put(address, user);
				}
				// 当好友关系更新为both时才发送广播
				if (userEntry.getType().toString().equals("both")) {
					sendBroadcast(intent);
				}
				// 用户更新，getEntries会更新
				// roster.getUnfiledEntries中的entry不会更新
			}
		}

		@Override
		public void entriesDeleted(Collection<String> addresses) {
			for (String address : addresses) {
				Intent intent = new Intent();
				intent.setAction("roster.deleted");
				System.out.println("删除了：" + address);
				// User user = null;
				if (ContactManager.contacters.containsKey(address)) {
					// user = ContactManager.contacters.get(address);
					ContactManager.contacters.remove(address);
				}
				intent.putExtra("deleteKey", address);
				ChatMessageDao chatMessageDao = new ChatMessageDao();
				int result = chatMessageDao.delChatWithSb(StringUtil
						.getUserNameByJid(address));
				if (result > 0) {
					System.out.println("与该用户的聊天消息删除成功，发送广播 更新页面");
					sendBroadcast(intent);
				}
			}
		}

		@Override
		public void entriesAdded(Collection<String> addresses) {
			System.out.println("发送添加联系人......");

			for (String address : addresses) {
				System.out.println("address:" + address);
				if (ContactManager.contacters.get(address) == null) {
					RosterEntry userEntry = roster.getEntry(address);
					User user = ContactManager.transEntryToUser(userEntry,
							roster);
					ContactManager.contacters.put(address, user);
				}
				// intent.putExtra("address", address);
				Intent intent = new Intent();
				intent.setAction("roster.added");
				RosterEntry userEntry = roster.getEntry(address);
				User user = ContactManager.transEntryToUser(userEntry, roster);
				// ContactManager.contacters.put(address, user);
				// intent.putExtra("address", address);
				// intent.putExtra(User.userKey, user);
				// sendBroadcast(intent);
			}
		}
	};

	// 初始化声音池的方法
	public void initSoundPool() {
		sp = new SoundPool(4, AudioManager.STREAM_MUSIC, 0); // 创建SoundPool对象
		hm = new HashMap<Integer, Integer>(); // 创建HashMap对象
		// hm.put(1, sp.load(this, R.raw.musictest, 1)); //
		// 加载声音文件musictest并且设置为1号声音放入hm中
	}

	// 播放声音的方法
	public void playSound(int sound, int loop) { // 获取AudioManager引用
		AudioManager am = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		// 获取当前音量
		float streamVolumeCurrent = am
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		// 获取系统最大音量
		float streamVolumeMax = am
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		// 计算得到播放音量
		float volume = streamVolumeCurrent / streamVolumeMax;
		// 调用SoundPool的play方法来播放声音文件
		currStreamId = sp.play(hm.get(sound), volume, volume, 1, loop, 1.0f);
	}

}
