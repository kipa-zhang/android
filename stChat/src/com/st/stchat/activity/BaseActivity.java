package com.st.stchat.activity;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.st.stchat.STChatApplication;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 
 * @author juwei 2014.11.20
 * 
 */
public class BaseActivity extends Activity {
	private static final String TAG = "BaseActivity";
	protected Context mContext;
	private String pageName = "";
	private static boolean isActive;

	public static boolean isAlive = false;
	private static final int ALIVE = 0;
	private static final int UNALIVE = 1;
	private static final int OUTALIVE = -1;

	private IsAliveReceiver mAliveReceiver;
	private Handler isAliveHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ALIVE:
				Log.d(TAG, "Handler  -----case 连接成功");
				isAlive = true;
				// Toast.makeText(mContext, "与服务器连接成功",
				// Toast.LENGTH_SHORT).show();
				break;
			case UNALIVE:
				Log.e(TAG, "Handler  -------case 失去连接");
				isAlive = false;
				// if(!isActive){
				// Toast.makeText(mContext, "与服务器连接失败，请检查您的网络",
				// Toast.LENGTH_SHORT)
				// .show();
				// }

				break;
			case OUTALIVE:
				Log.i(TAG, "Handler  -------case 异地登陆");
				Toast.makeText(mContext, "对不起，该账号已在其他设备登录", Toast.LENGTH_LONG)
						.show();
				// 在这里执行注销的一系列动作，将用户弹出到登陆界面
				// TODO这里有个Bug,就是在掉线的情况下，如果该账户在
				// 其他地方登陆，然后当有网络自动连接后的处理方式需要添加进来
				XmppConnectionServer.getInstance().closeConnection();

				((STChatApplication) STChatApplication.getInstance())
						.clearActivitys((Activity) mContext);
				// 清空消息缓存
				STChatApplication.messageCache.clear();
				// 还原第一次加载messagefragment标示
				STChatApplication.firstLoadMessageTag = true;
				// 其次，关闭当前activity，并且跳转到登陆界面
				Intent intent = new Intent();
				intent.setClass(mContext, LoginActivity.class);
				intent.putExtra("isOutAlive", true);
				mContext.startActivity(intent);
				Log.i(TAG, "----强制下线成功----");
				((Activity) mContext).finish();

				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "BaseActivity on crate()");
		((STChatApplication) STChatApplication.getInstance()).addActivity(this);
		Class<?> clazz = getClass();
		pageName = clazz.getSimpleName();
		Log.d("当前pageName:", pageName);
		mContext = this;

		mAliveReceiver = new IsAliveReceiver();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConstantUtils.TOALIVE_ACTION);
		filter.addAction(ConstantUtils.TOUNALIVE_ACTION);
		filter.addAction(ConstantUtils.OUTALIVE_ACTION);
		this.registerReceiver(mAliveReceiver, filter);

	}

	@Override
	protected void onStart() {

		super.onStart();

	}

	@Override
	protected void onRestart() {

		super.onRestart();
	}

	@Override
	protected void onResume() {

		super.onResume();
		((STChatApplication) STChatApplication.getInstance())
				.addActivityTop(this);
		if (!isActive) {
			// app 从后台唤醒，进入前台
			Log.i("BaseActivity", "进入前台");

			isActive = true;
		}

	}

	@Override
	protected void onPause() {

		super.onPause();
		System.err.println("notification pause:" + this.getClass());
		((STChatApplication) STChatApplication.getInstance())
				.removeActivityTop(this);

	}

	@Override
	protected void onStop() {

		super.onStop();

		if (!isAppOnForeground()) {
			// app 进入后台
			isActive = false;
			Log.i("BaseActivity", "进入后台");

			// 全局变量isActive = false 记录当前已经进入后台
		}

	}

	@Override
	protected void onDestroy() {

		unregisterReceiver(mAliveReceiver);
		super.onDestroy();

	}

	@Override
	public void finish() {

		super.finish();
	}

	public boolean isAppOnForeground() {
		// Returns a list of application processes that are running on the
		// device

		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = getApplicationContext().getPackageName();

		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}

		return false;
	}

	public static boolean isBackground(Context context) {

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
					Log.i("后台", appProcess.processName);
					return true;
				} else {
					Log.i("前台", appProcess.processName);
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {

		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		return super.onKeyUp(keyCode, event);
	}

	private class IsAliveReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, final Intent intent) {

			Log.d(TAG, "Receiver ---- 接收到连接状态广播");
			Message message = new Message();
			if (intent.getAction().equals(ConstantUtils.TOALIVE_ACTION)) {

				Log.d(TAG, "Receiver ---- 连接通畅 ----");
				message.what = ALIVE;
			} else if (intent.getAction()
					.equals(ConstantUtils.TOUNALIVE_ACTION)) {
				Log.e(TAG, "Receiver ---- 失去连接 ----");
				message.what = UNALIVE;
			} else if (intent.getAction().equals(ConstantUtils.OUTALIVE_ACTION)) {
				Log.e(TAG, "Receiver ---- 被踢下线 ----");
				message.what = OUTALIVE;
			}
			isAliveHandler.sendMessage(message);
		}
	}

}
