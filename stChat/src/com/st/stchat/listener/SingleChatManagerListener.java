package com.st.stchat.listener;

import java.util.Date;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.bean.MessageItem;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.message.STChatMessage;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.DateUtil;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.StringUtil;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 单人聊天信息监听类
 * 
 * @author juwei 2014. 11 26
 * 
 */
public class SingleChatManagerListener implements ChatManagerListener {
	private static final String TAG = "SingleChatManagerListener";

	@Override
	public void chatCreated(Chat chat, boolean bool) {
		chat.addMessageListener(new MessageListener() {
			@Override
			public void processMessage(Chat chat, Message msg) {
				if (!XmppConnectionServer.getInstance().isConn()) {
					return;
				}
				Log.i(TAG, "收到消息，类型为 ：" + msg.getType().toString());
				if (msg.getType().toString().equals("normal")) {
					Log.e(TAG, "此消息为服务器发来的系统消息");
					return;
				} else {

				}
				Log.e(TAG, "msg -->>>>>" + msg.toString());
				System.out.println("PacketID() ---- " + msg.getPacketID());
				System.out.println("From()     ---- " + msg.getFrom());
				System.out.println("To()       ---- " + msg.getTo());
				System.out.println("Subject()  ---- " + msg.getSubject());
				System.out.println("Xmlns()    ---- " + msg.getXmlns());
				System.out.println("Type()     ---- " + msg.getType());
				// if(msg.getXmlns().equals("urn:xmpp:receipts")){
				// System.out.println("该消息需要回执");
				// }

				// 当前用户
				String cacheAccount = InfoUtils.getUser(STChatApplication
						.getInstance());
				// 返回的登录用户
				String shartCacheUserName = StringUtils
						.parseName(XmppConnectionServer.getInstance()
								.getConnection().getUser());

				// 发送消息用户
				String from = msg.getFrom();
				Log.d(TAG,
						"----------------------------------------------------");
				Log.d(TAG, "| **** " + from + "**** 来消息啦！！！！！");
				Log.d(TAG, "| **** 消息类型为**** " + msg.getType() + " ");
				// 判断该消息类型 不包含 conference接收聊天室邀请的message
				if (!from.contains("conference")) {
					String receiveTo = StringUtil.getUserNameByJid(msg.getTo());
					String receiveFrom = StringUtil.getUserNameByJid(msg
							.getFrom());
					long receiveTime = new Date().getTime();
					String receiveText = "";
					if (msg.getBody() != null) {
						receiveText = msg.getBody();
					} else {
						return;
					}

					if (TextUtils.isEmpty(cacheAccount)) {
						cacheAccount = shartCacheUserName;
					}

					// 将消息存入数据库
					ChatMessageDao cmd = new ChatMessageDao();
					long lon = cmd.add("", cacheAccount, "2", receiveFrom,
							receiveTo, ConstantUtils.MESSAGE_RECEIVE_NOT_READ,
							"", "" + receiveTime, receiveText,
							STChatMessage.MessageStyle_Text, "");
					Log.d(TAG, "| ---- 存进去第：" + lon + " 条聊天数据 ----设置为：未读");

					int unReadSum = cmd.findNotReadByAccount(cacheAccount,
							receiveFrom);

					// 添加消息到message缓存里面
					Date date = new Date(Long.parseLong(receiveTime + ""));
					// 判断该用户消息是否存在缓存中 不存就创建item
					if (STChatApplication.messageCache.get(receiveFrom) == null) {
						// 创建
						MessageItem item = new MessageItem();
						item.setIconRes(R.drawable.default_nor_man);
						item.setMsg(receiveText);
						item.setTime(DateUtil.date2Str(date));
						item.setTitle(receiveFrom);
						item.setUnReadSum(unReadSum);
						STChatApplication.messageCache.put(receiveFrom, item);
					} else {
						// 修改
						STChatApplication.messageCache.get(receiveFrom).setMsg(
								receiveText);
						STChatApplication.messageCache.get(receiveFrom)
								.setUnReadSum(unReadSum);
						STChatApplication.messageCache.get(receiveFrom)
								.setTime(DateUtil.date2Str(date));
					}

					// 发送广播(四个参数：谁发来，发给谁，接受时间，消息内容，当前总共未读条数)
					String[] message = new String[] { receiveFrom, receiveTo,
							"" + receiveTime, "" + receiveText, unReadSum + "" };
					Log.d(TAG, "| sendBroadcast >>>>  " + message[0] + " 发给 "
							+ message[1] + " 的消息，在 " + message[2] + " 毫秒，内容是："
							+ message[3]);

					Intent intent = new Intent();
					intent.setAction("com.st.singlechat");
					intent.putExtra("msg", message);
					STChatApplication.getInstance().sendBroadcast(intent);
					Log.d(TAG,
							"----------------------------------------------------");

				}

				//
				// // 消息内容
				// String body = msg.getBody();
				// boolean left = body.substring(0, 1).equals("{");
				// boolean right = body
				// .substring(body.length() - 1, body.length())
				// .equals("}");
				// if (left && right) {
				// try {
				// JSONObject obj = new JSONObject(body);
				// String type = obj.getString("messageType");
				// String chanId = obj.getString("chanId");
				// String chanName = obj.getString("chanName");
				// } catch (JSONException e) {
				// e.printStackTrace();
				// }
				//
				// }
			}
		});
	}
}