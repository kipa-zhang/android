package com.st.stchat.listener;

import java.util.Date;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import android.content.Intent;
import android.util.Log;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.bean.MessageItem;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.message.STChatMessage;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.DateUtil;
import com.st.stchat.utils.FileUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.StringUtil;
import com.st.stchat.xmpp.XmppConnectionServer;

public class ChatFileTransferListener implements FileTransferListener {
	private static final String TAG = "ChatFileTransferListener";

	@Override
	public void fileTransferRequest(final FileTransferRequest arg0) {
		// TODO Auto-generated method stub
		System.out.println("监听器监听到图片来了");
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!XmppConnectionServer.getInstance().isConn()) {
					return;
				}
				// TODO Auto-generated method stub
				// 当前用户
				String cacheAccount = InfoUtils.getUser(STChatApplication
						.getInstance());
				// 返回的登录用户
				String shartCacheUserName = StringUtils
						.parseName(XmppConnectionServer.getInstance()
								.getConnection().getUser());
				// 发送方名称（从谁接受过来的图片）
				String receiveFrom = StringUtil.getUserNameByJid(arg0
						.getRequestor());
				Log.d(TAG, "| **** " + arg0.getRequestor() + "**** 发来图片啦！！！！！");
				IncomingFileTransfer incoming = arg0.accept();
				System.out.println("接收文件监听器arg0.getRequestor()  ---"
						+ arg0.getRequestor());
				System.out.println("接收文件监听器arg0.getFileName()   ---"
						+ arg0.getFileName());
				System.out.println("接收文件监听器arg0.getDescription()---"
						+ arg0.getDescription());
				System.out.println("接收文件监听器arg0.getFileSize()   ---"
						+ arg0.getFileSize());
				System.out.println("接收文件监听器arg0.getMimeType()   ---"
						+ arg0.getMimeType());
				System.out.println("接收文件监听器arg0.getStreamID()   ---"
						+ arg0.getStreamID());
				System.out.println("incoming.getFilePath() ----"
						+ incoming.getFilePath());
				System.out.println("incoming.getPeer() ----"
						+ incoming.getPeer());

				FileUtils fileUtils = new FileUtils();
				String path = "";
				try {
					path = fileUtils.writeToSDFromInput(incoming.recieveFile(),
							incoming.getFileSize(), null);
					System.out.println("成功写入路径：" + path);
					// while(!incoming.isDone()){
					// if(incoming.getStatus().equals(Status.error)){
					// System.out.println("ERROR!!! " + incoming.getError());
					// }else{
					// System.out.println(incoming.getStatus()+"进度 "+incoming.getProgress());
					// }
					// try {
					// Thread.sleep(500);
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					// }
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long receiveTime = new Date().getTime();

				// 将消息存入数据库
				ChatMessageDao cmd = new ChatMessageDao();
				long lon = cmd.add("", cacheAccount, "2", receiveFrom,
						cacheAccount, ConstantUtils.MESSAGE_RECEIVE_NOT_READ,
						"", "" + receiveTime, "[picture]",
						STChatMessage.MessageStyle_Pic, path);
				Log.d(TAG, "| ---- 存进去第：" + lon + " 条图片数据 ----");
				System.out.println("路径file.getPath()---- " + path);

				int unReadSum = cmd.findNotReadByAccount(cacheAccount,
						receiveFrom);// 计算未读

				// 添加消息到message缓存里面
				Date date = new Date(Long.parseLong(receiveTime + ""));
				// 判断该用户消息是否存在缓存中 不存就创建item
				if (STChatApplication.messageCache.get(receiveFrom) == null) {
					// 创建
					MessageItem item = new MessageItem();
					item.setIconRes(R.drawable.default_nor_man);
					item.setMsg("[picture]");
					item.setTime(DateUtil.date2Str(date));
					item.setTitle(receiveFrom);
					item.setUnReadSum(unReadSum);
					STChatApplication.messageCache.put(receiveFrom, item);
				} else {
					// 修改
					STChatApplication.messageCache.get(receiveFrom).setMsg(
							"[picture]");
					STChatApplication.messageCache.get(receiveFrom)
							.setUnReadSum(unReadSum);
					STChatApplication.messageCache.get(receiveFrom).setTime(
							DateUtil.date2Str(date));
				}

				// 保存成功之后发送广播
				// 发送广播（字段依次是：谁发来，发给谁，内容是图片，接收时间，总共未读条数，本地存储路径）
				String[] message = new String[] { receiveFrom, cacheAccount,
						"[picture]", "" + receiveTime, unReadSum + "", path };
				Log.d(TAG, "| sendBroadcast >>>>  " + message[0] + " 发给 "
						+ message[1] + " 的" + message[2] + "，在 " + message[3]
						+ " 毫秒，本地路径是：" + message[5]);

				Intent intent = new Intent();
				intent.setAction("com.st.singlechat.pic");
				intent.putExtra("msg_pic", message);
				STChatApplication.getInstance().sendBroadcast(intent);
				Log.d(TAG,
						"----------------------------------------------------");
			}
		}).start();
	}

}
