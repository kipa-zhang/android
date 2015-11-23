package com.st.stchat.listener;

import java.util.Date;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.bean.MessageItem;
import com.st.stchat.dao.ChatRoomMessageDao;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.DateUtil;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 会议室消息监听事件
 * 
 * @author  
 * 
 */
public class ChatRoomMsgListener implements PacketListener {
	private static final String TAG = "ChatRoomMsgListener";

	@Override
	public void processPacket(Packet packet) {

		Message message = (Message) packet;
		// 接收来自聊天室的聊天信息
		String from = StringUtils.parseResource(message.getFrom());
		// String fromRoomName = StringUtils.parseName(message.getFrom());
		String roomJid = StringUtils.parseBareAddress(message.getFrom());
		String messageId = message.getPacketID();
		String accountName = InfoUtils.getUser(STChatApplication.getInstance());
		System.out.println("房间roomJid=" + roomJid + "有来自于=" + from
				+ "的消息,messageId:" + messageId);
		String receiveText = message.getBody();

		// 返回的登录用户
		String shartCacheUserName = StringUtils.parseName(XmppConnectionServer
				.getInstance().getConnection().getUser());

		// 判断该消息类型 不包含 conference接收聊天室邀请的message
		if (from.length() > 0 && messageId.length() > 0) {
			if (receiveText != null) {
				// 将消息存入数据库
				// 1、判断该消息在数据库是否已经存在，不存在即存
				ChatRoomMessageDao messageDao = new ChatRoomMessageDao();
				boolean result = messageDao.find(message.getPacketID());
				if (result == true) {
					System.out.println("该消息已经存在，不存了");
				} else {
					System.out.println("该消息不存在,下面存到数据库之前再次判断是否是自己的消息");
					// 判断监听到的消息中是否包含自己的消息 过滤一下
					if (TextUtils.isEmpty(accountName)) {
						accountName = shartCacheUserName;
					}
					if (!from.equalsIgnoreCase(accountName)) {
						long receiveTime = new Date().getTime();

						// 将消息写入数据库中
						long lon = messageDao.add(message.getPacketID(),
								accountName, "2", from, roomJid,
								ConstantUtils.MESSAGE_RECEIVE_NOT_READ,
								receiveTime + "", receiveText);
						Log.d(TAG, "| ---- Chatroom存进去第：" + lon + " 条聊天数据 ----");

						int unReadSum = messageDao.findNotReadRoomMsg(
								accountName, roomJid);

						// 添加消息到message缓存里面
						Date date = new Date(Long.parseLong(receiveTime + ""));
						// 判断该用户消息是否存在缓存中 不存就创建item
						if (STChatApplication.messageCache.get(roomJid) == null) {
							// 创建
							MessageItem item = new MessageItem();
							item.setIconRes(R.drawable.default_nor_room);
							item.setMsg(receiveText);
							item.setTime(DateUtil.date2Str(date));
							item.setTitle(roomJid);
							item.setUnReadSum(unReadSum);
							STChatApplication.messageCache.put(roomJid, item);
							item = null;
						} else {
							// 修改
							STChatApplication.messageCache.get(roomJid).setMsg(
									receiveText);
							STChatApplication.messageCache.get(roomJid)
									.setUnReadSum(unReadSum);
							STChatApplication.messageCache.get(roomJid)
									.setTime(DateUtil.date2Str(date));
						}

						// 发送广播
						String[] msgData = new String[] { roomJid, from,
								receiveTime + "", message.getBody(),
								message.getPacketID(), unReadSum + "" };
						Log.d(TAG, "| sendBroadcast >>>>  " + msgData[0]
								+ " 收到 " + msgData[1] + " 的消息，在 " + msgData[2]
								+ " 毫秒，内容是：" + msgData[3] + "该消息的Id："
								+ msgData[4]);

						Intent intent = new Intent();
						intent.setAction("com.st.groupchat");
						intent.putExtra("msg", msgData);
						STChatApplication.getInstance().sendBroadcast(intent);

					} else {
						System.out.println(from + "是自己发的历史消息 不存了 也不发送广播");
					}
				}
			}
		}

	}
}
