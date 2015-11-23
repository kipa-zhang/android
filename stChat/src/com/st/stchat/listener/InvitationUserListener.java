package com.st.stchat.listener;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.util.Log;

import com.st.stchat.STChatApplication;
import com.st.stchat.dao.ChatRoomDao;
import com.st.stchat.manager.ChatRoomManager;
import com.st.stchat.utils.InfoUtils;

public class InvitationUserListener implements InvitationListener {
	private static final String TAG = "InvitationUserListener";

	@Override
	public void invitationReceived(Connection conn, String room,
			String inviter, String reason, String password, Message message) {
		// TODO Auto-generated method stub
		Log.i(TAG, "收到来自 " + message.getFrom() + " 的聊天室邀请。邀请附带内容：" + reason);
		String roomJid = message.getFrom();
		// 加入该房间
		MultiUserChat multiUserChat = ChatRoomManager.joinMultiUserChat(
				InfoUtils.getUser(STChatApplication.getInstance()), roomJid);
		// 给房间设置监听
		ChatRoomMsgListener chatRoomMsgListener = new ChatRoomMsgListener();
		multiUserChat.addMessageListener(chatRoomMsgListener);

		// 将已被邀请加入的房间 存入本地数据库
		ChatRoomDao.getInstance().saveChatRoom(
				InfoUtils.getUser(STChatApplication.getInstance()), roomJid);
	}
}
