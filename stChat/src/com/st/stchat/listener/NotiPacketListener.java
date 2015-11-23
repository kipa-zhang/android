package com.st.stchat.listener;

import java.util.Calendar;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import android.app.NotificationManager;
import android.content.Intent;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.activity.SingleChatActivity;
import com.st.stchat.model.Notice;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.DateUtil;
import com.st.stchat.utils.NotificationUtil;

public class NotiPacketListener implements PacketListener {
	private NotificationManager notificationManager = (NotificationManager) STChatApplication
			.getInstance().getSystemService(
					android.content.Context.NOTIFICATION_SERVICE);

	@Override
	public void processPacket(Packet arg0) {
		// TODO Auto-generated method stub
		Message message = (Message) arg0;
		if (message != null && message.getBody() != null
				&& !message.getBody().equals("")) {
			String time = DateUtil.date2Str(Calendar.getInstance(),
					ConstantUtils.MS_FORMART);
			String from = message.getFrom().split("@")[0];

			Notice notice = new Notice();
			notice.setTitle("会话信息");
			notice.setNoticeType(Notice.CHAT_MSG);
			notice.setContent(message.getBody());
			notice.setFrom(from);
			notice.setStatus(Notice.UNREAD);
			notice.setNoticeTime(time);

			Intent intent = new Intent(ConstantUtils.NEW_MESSAGE_ACTION);
			intent.putExtra("notice", notice);
			STChatApplication.getInstance().sendBroadcast(intent);
			NotificationUtil.setNotiType(notificationManager,
					STChatApplication.getInstance(), R.drawable.ic_launcher,
					"STChat", notice.getContent(), SingleChatActivity.class,
					message.getFrom(), "chat");

		}
	}

}
