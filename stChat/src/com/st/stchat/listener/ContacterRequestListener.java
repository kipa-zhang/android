package com.st.stchat.listener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import android.app.NotificationManager;
import android.content.Intent;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.activity.NewFriendActivity;
import com.st.stchat.dao.NoticeDao;
import com.st.stchat.model.Notice;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.NotificationUtil;
import com.st.stchat.utils.StringUtil;

/**
 * 处理好友请求的监听
 * 
 * @author  
 * 
 */
public class ContacterRequestListener implements PacketListener {
	private NotificationManager notificationManager = (NotificationManager) STChatApplication
			.getInstance().getSystemService(
					android.content.Context.NOTIFICATION_SERVICE);

	@Override
	public void processPacket(Packet packet) {
		// TODO Auto-generated method stub
		Map<String, String> userMap = InfoUtils
				.getUserAccount(STChatApplication.getInstance());
		String user = userMap.get("userAccountName");
		if (StringUtil.getUserNameByJid(packet.getFrom())
				.equalsIgnoreCase(user)) {
			return;
		}
		String from = packet.getFrom();
		System.out.println("收到来自" + from + "的好友请求");
		// if (Roster.getDefaultSubscriptionMode().equals(
		// SubscriptionMode.accept_all)) {
		// System.out.println("申请加您为好友1");
		// Presence subscription = new Presence(Presence.Type.subscribe);
		// subscription.setTo(packet.getFrom());
		// XmppConnectionServer.getInstance().getConnection()
		// .sendPacket(subscription);
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
		System.out.println("domian-----"
				+ InfoUtils.getPacketDomain(STChatApplication.getInstance()));
		// NoticeDao dao = new NoticeDao();
		Notice notice = new Notice();
		notice.setTitle(StringUtil.getUserNameByJid(packet.getFrom()));
		notice.setNoticeType(Notice.ADD_FRIEND);
		notice.setContent(StringUtil.getUserNameByJid(packet.getFrom())
				+ " applied to add you as a friend");
		notice.setFrom(packet.getFrom());
		notice.setTo(packet.getTo());
		// notice.setTo(user);
		notice.setStatus(Notice.UNREAD);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		notice.setNoticeTime(format.format(new Date()));
		long noticeId = NoticeDao.getInstance().saveNotice(notice);
		if (noticeId != -1) {
			Intent intent = new Intent();
			intent.setAction(ConstantUtils.ROSTER_SUBSCRIPTION_ACTION);
			intent.putExtra("notice", notice);
			STChatApplication.getInstance().sendBroadcast(intent);
			NotificationUtil.setNotiType(notificationManager,
					STChatApplication.getInstance(), R.drawable.ic_launcher,
					"StChat", notice.getContent(), NewFriendActivity.class,
					from, "roster");
		}
	}

}
