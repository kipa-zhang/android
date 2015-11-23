package com.st.stchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.BaseAsyncTask;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.widget.AlertDialog;
import com.st.stchat.xmpp.XmppConnectionServer;

public class SettingActivity extends BaseActivity implements OnClickListener,
		BaseAsyncTaskListener {
	private static final String TAG = "SettingActivity";
	private static final int EXIT = 1;
	private ImageButton buttonTitleLeft;
	private LinearLayout ll_about, ll_exit;
	private BaseAsyncTask task;
	private TextView textViewTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);

		initView();
		initListener();
		textViewTitle.setVisibility(View.VISIBLE);
		textViewTitle.setText("" + "Settings");
	}

	private void initView() {
		buttonTitleLeft = (ImageButton) findViewById(R.id.buttonTitleLeft);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		ll_about = (LinearLayout) findViewById(R.id.ll_about);
		ll_exit = (LinearLayout) findViewById(R.id.ll_exit);
	}

	private void initListener() {
		buttonTitleLeft.setOnClickListener(this);
		ll_about.setOnClickListener(this);
		ll_exit.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_about:
			// 该功能尚未开放
			Toast.makeText(this, "This function is not yet open",
					Toast.LENGTH_SHORT).show();
			XmppConnectionServer.printAllStaticIsNull();
			break;
		case R.id.ll_exit:
			zhuXiaoDialog();
			break;
		case R.id.buttonTitleLeft:
			SettingActivity.this.finish();
		default:
			break;
		}

	}

	@Override
	protected void onPause() {

		super.onPause();
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	// 注销
	private void zhuXiaoDialog() {

		new AlertDialog(SettingActivity.this).builder()
				.setTitle("Are you sure you want to exit.")
				.setPositiveButton("Log Out", new OnClickListener() {
					@Override
					public void onClick(View v) {
						task = new BaseAsyncTask(SettingActivity.this,
								SettingActivity.this, EXIT);
						task.setDialogMessage("Unregistering....");
						task.execute();
						// 如果所有的用户数据清除，成功，则跳转到登陆界面
					}
				}).setNegativeButton("Cancel", new OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				}).show();

	}

	@Override
	public void doAsyncTaskBefore(int taskId) {

	}

	@Override
	public Object doAsyncTaskIn(int taskId, Object... params) {
		boolean bool = false;

		if (XmppConnectionServer.getInstance().isConn()) {
			bool = XmppConnectionServer.getInstance().closeConnection();
		}

		if (bool) {
			Log.i(TAG, "----向服务器注销成功：----");
		} else {
			Log.e(TAG, "----向服务器注销失败：----");
		}
		try {
			Thread.sleep(111);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return bool;
	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {
		boolean bool = (Boolean) result;
		if (bool) {
			boolean b = InfoUtils.deleteUserAccount(STChatApplication
					.getInstance());
			if (b) {
				Log.i(TAG, "----清楚本地账户成功----" + b);
				// 首先要从task里面关闭MainActivity以及后台的activity
				((STChatApplication) STChatApplication.getInstance())
						.clearActivitys(SettingActivity.this);
				// 清空消息缓存
				STChatApplication.messageCache.clear();
				// 还原第一次加载messagefragment标示
				STChatApplication.firstLoadMessageTag = true;
				// 其次，关闭当前activity，并且跳转到登陆界面
				Intent intent = new Intent();
				intent.setClass(SettingActivity.this, LoginActivity.class);
				intent.putExtra("isExit", true);
				SettingActivity.this.startActivity(intent);
				Log.i(TAG, "----本地注销成功----");
				SettingActivity.this.finish();
			} else {
				Log.e(TAG, "----本地注销失败----");
			}
		} else {
			Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void doCancelled(int taskId) {

	}
}
