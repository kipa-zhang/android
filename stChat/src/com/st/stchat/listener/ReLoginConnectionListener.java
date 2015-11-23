package com.st.stchat.listener;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.ConnectionListener;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.st.stchat.STChatApplication;
import com.st.stchat.activity.BaseActivity;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 掉线重登进行连接监听类
 * 
 * @author juwei 2014.11.26
 * 
 */
public class ReLoginConnectionListener implements ConnectionListener {
	private static final String TAG = "ReLoginConnectionListener";
	private Timer tExit;
	private String username;
	private String password;
	private int logintime = 2000;

	@Override
	public void connectionClosed() {
		// 这里是正常关闭连接的事件
		Log.e(TAG, "连接被正常关闭");
		// 关闭连接
		// XmppConnectionServer.getInstance().closeConnection();
		// 重新连接服务器
		tExit = new Timer();
		tExit.schedule(new timetask(), logintime);
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		// 这里就是网络不正常断线激发的事件
		Log.e(TAG, "连接被异常关闭");
		// 当检测到该账号已被登陆时
		boolean error = e.getMessage().equals("stream:error (conflict)");
		if (!error) {

			// 重新连接服务器
			tExit = new Timer();
			tExit.schedule(new timetask(), logintime);
			Log.i(TAG, "tExit----------OK");
			// 在这里发送失去连接（掉线）的广播
			BaseActivity.isAlive = false;
			Intent intent = new Intent();
			intent.setAction("com.st.singlechat.UNALIVE");
			STChatApplication.getInstance().sendBroadcast(intent);
		} else {
			Log.e(TAG, "该帐号已在其他地方登陆");
			// 发送踢下线的广播
			Intent intent = new Intent();
			intent.setAction("com.st.singlechat.OUTALIVE");
			STChatApplication.getInstance().sendBroadcast(intent);
			BaseActivity.isAlive = false;
			Log.e(TAG, "异地登陆广播发送成功");
		}

	}

	class timetask extends TimerTask {
		@Override
		public void run() {
			Log.i(TAG, "----timetask Run----");
			// 关闭连接
			if (XmppConnectionServer.getInstance().getConnection() != null) {
				XmppConnectionServer.getInstance().closeConnection();
			}
			Map<String, String> userMap = InfoUtils
					.getUserAccount(STChatApplication.getInstance());
			username = userMap.get("userAccountName");
			password = userMap.get("userAccountPassword");
			Log.d(TAG, "重新登陆： \n username ==== " + username
					+ ", \n password ==== " + password);
			// System.out.println("获取用户名   ----  "
			// + (username != null));
			// System.out.println("获取密码   ----  "
			// + (password != null));
			if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
				Log.e(TAG, "正在尝试重新登录");
				// 连接服务器
				if (XmppConnectionServer.getInstance().getConnection() != null) {
					if (XmppConnectionServer.getInstance()
							.login(username, password).equals("0")) {
						Log.i(TAG, "重新登录成功");
						BaseActivity.isAlive = true;

						// 在这里发送掉线重新登陆成功之后的广播
						Intent intent = new Intent();
						intent.setAction("com.st.singlechat.ALIVE");
						STChatApplication.getInstance().sendBroadcast(intent);
					} else {
						Log.e(TAG, "尝试重新登录");
						tExit.schedule(new timetask(), logintime);
					}
				} else {
					Log.e(TAG, "连接错误，无法进行重新登陆");
				}
			} else {
				Log.e(TAG, "无法进行重新登陆");
				// juwei@10.41.88.254/Spark
				// juwei@10.41.88.254/Smack 2.9.3
				// juwei@10.41.88.254/Web
			}
		}
	}

	@Override
	public void reconnectingIn(int arg0) {
		// 重新连接的动作正在进行的动作，里面的参数arg0是一个倒计时的数字，
		// 如果连接失败的次数增多，数字会越来越大，开始的时候是14
		Log.i(TAG, "------------reconnectingIn----");
		BaseActivity.isAlive = false;

		// 在这里发送掉线显示Toast的广播

	}

	@Override
	public void reconnectionFailed(Exception arg0) {
		// 重新连接失败
		Log.i(TAG, "------------reconnectionFailed----");
		BaseActivity.isAlive = false;
		// 在这里发送重连失败的广播
	}

	@Override
	public void reconnectionSuccessful() {
		// 当网络断线了，重新连接上服务器触发的事件
		Log.i(TAG, "-------------reconnectionSuccessful----");
		BaseActivity.isAlive = true;

		// 在这里发送掉线重新登陆成功之后的广播
		Intent intent = new Intent();
		intent.setAction("com.st.singlechat.ALIVE");
		STChatApplication.getInstance().sendBroadcast(intent);

	}

}