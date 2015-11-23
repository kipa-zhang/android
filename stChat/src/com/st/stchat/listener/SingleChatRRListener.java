package com.st.stchat.listener;

import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import android.content.Intent;
import android.util.Log;

import com.st.stchat.STChatApplication;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.utils.ConstantUtils;

public class SingleChatRRListener implements ReceiptReceivedListener {
	private static final String TAG = "SingleChatRRListener";

	@Override
	public void onReceiptReceived(String from, String to, String packetID) {
		// TODO Auto-generated method stub
		// htc@127.0.0.1/Smack
		Log.d(TAG, "--------接收消息回执--------");
		Log.d(TAG, "|-------来自从[ " + from + " ]>>>> [ " + to + " ]的消息回执");
		Log.d(TAG, "|-------packetID:" + packetID + " 的消息已被成功接收");

		// 将数据从已发送改为已接收
		ChatMessageDao cmd = new ChatMessageDao();
		int sum = cmd.updateSendStatusByPacketID(packetID,
				ConstantUtils.MESSAGE_SEND_HASRECEIVED);
		Log.d(TAG, "|-------修改了" + sum + " 条消息的发送和接收状态");
		Log.d(TAG, "--------回执报告完毕--------");

		// 准备发送广播（告知程序哪条信息被成功接收了）
		String[] status = new String[] { from, to, packetID };
		Log.d(TAG, "<<<< sendBroadcast >>>>  从[ " + status[0] + " ]反馈给[ "
				+ status[1] + " ]的消息回执,消息ID: " + status[2]);
		Intent intent = new Intent();
		intent.setAction("com.st.singlechat.message.status");
		intent.putExtra("message_status", status);

		STChatApplication.getInstance().sendBroadcast(intent);
		Log.d(TAG, "--------消息接收状态广播发送完毕--------");

	}

}
