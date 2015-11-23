package com.st.stchat.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.Presence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.AsyncTaskBase;
import com.st.stchat.BaseAsyncTask;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.adapter.NoticeAdapter;
import com.st.stchat.dao.ContactDao;
import com.st.stchat.dao.NoticeDao;
import com.st.stchat.model.Notice;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.StringUtil;
import com.st.stchat.view.LoadingView;
import com.st.stchat.widget.AlertDialog;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 联系人请求消息页面
 * 
 * @author  
 * 
 */
public class NewFriendActivity extends BaseActivity implements
		BaseAsyncTaskListener {

	private TextView title;
	private ListView nNewFriendList;
	private ImageButton buttonTitleLeft;
	private NoticeAdapter noticeAdapter = null;
	private ContacterReceiver receiver = null;
	private List<Notice> inviteNotices = new ArrayList<Notice>();
	private LoadingView mLoadingView;
	private String accountName;
	private BaseAsyncTask task;
	private GetDataTask getDataTask;
	private static final int SEND = 6;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_friend);
		accountName = InfoUtils.getUser(STChatApplication.getInstance());
		initView();
		initListener();

		getDataTask = new GetDataTask(mLoadingView);
		getDataTask.execute(0);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		// 好友请求
		receiver = new ContacterReceiver();
		filter.addAction("roster.subscribe");
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getDataTask.cancel(true);
		// 卸载广播接收器
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		nNewFriendList = (ListView) findViewById(R.id.new_add_user_list);
		buttonTitleLeft = (ImageButton) findViewById(R.id.buttonTitleLeft);
		title = (TextView) findViewById(R.id.textViewTitle);
		title.setText("" + "Recommended Friends");
		mLoadingView = (LoadingView) findViewById(R.id.loadingView);
	}

	private class GetDataTask extends AsyncTaskBase {

		public GetDataTask(LoadingView loadingView) {
			super(loadingView);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			int result = 0;
			inviteNotices = NoticeDao.getInstance().getNoticeListByType(
					Notice.All);
			if (inviteNotices != null) {
				if (inviteNotices.size() > 0) {
					result = 1;
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == 1) {
				noticeAdapter = new NoticeAdapter(NewFriendActivity.this,
						inviteNotices);
				nNewFriendList.setAdapter(noticeAdapter);
			}
		}
	}

	private void initListener() {
		buttonTitleLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				NewFriendActivity.this.finish();
			}
		});
		nNewFriendList.setOnItemClickListener(inviteListClick);
	}

	// list监听
	private OnItemClickListener inviteListClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			final Notice notice = (Notice) view.findViewById(R.id.content)
					.getTag();
			boolean result = ContactDao.getInstance().contactIsExist(
					accountName, notice.getFrom());
			// 判断 消息的方式 false 为被动接收 true 为主动发送
			if (result) {
				// 当收到对方确认信息时 改变消息状态 ---主动确认 这时好友关系已存在联系人列表
				showAddFriendSubmitDialag(notice);
			} else {
				// 当收到好友请求 确认添加好友 ---被动接收
				if (Notice.ADD_FRIEND == notice.getNoticeType()
						&& notice.getStatus() == Notice.UNREAD) {
					showAddFriendDialag(notice);
				}
			}
		}

	};

	/**
	 * 弹出添加好友对话框 ---被动接收
	 * 
	 * @param notice
	 */
	private void showAddFriendDialag(final Notice notice) {
		final String subFrom = notice.getFrom();
		new AlertDialog(NewFriendActivity.this).builder()
				.setTitle("A friend request")
				.setMsg(StringUtil.getUserNameByJid(subFrom))
				.setPositiveButton("Accept", new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!isAlive) {
							Toast.makeText(NewFriendActivity.this,
									"You have dropped", Toast.LENGTH_SHORT)
									.show();
							return;
						}
						// 接受请求
						sendSubscribe(Presence.Type.subscribed, subFrom);
						sendSubscribe(Presence.Type.subscribe, subFrom);

						task = new BaseAsyncTask(NewFriendActivity.this,
								NewFriendActivity.this, SEND);
						task.setDialogMessage("Loading...");
						task.execute();

						NoticeDao.getInstance().updateAddFriendStatus(
								notice.getId(),
								Notice.READ,
								"Accept a friend request from "
										+ StringUtil.getUserNameByJid(notice
												.getFrom()));
						refresh();
					}
				}).setNegativeButton("Refuse", new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!isAlive) {
							Toast.makeText(NewFriendActivity.this,
									"You have dropped", Toast.LENGTH_SHORT)
									.show();
							return;
						}
						// 拒绝请求
						sendSubscribe(Presence.Type.unsubscribe, subFrom);

						NoticeDao.getInstance().updateAddFriendStatus(
								notice.getId(),
								Notice.READ,
								"Refuse a friend request from "
										+ StringUtil.getUserNameByJid(notice
												.getFrom()));
						refresh();
					}
				}).show();
	}

	/**
	 * 弹出确认添加好友对话框 ---主动确认
	 * 
	 * @param notice
	 */
	private void showAddFriendSubmitDialag(final Notice notice) {
		final String subFrom = notice.getFrom();
		new AlertDialog(NewFriendActivity.this).builder()
				.setTitle("Add friends success")
				.setMsg(StringUtil.getUserNameByJid(subFrom))
				.setPositiveButton("Confirm", new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!isAlive) {
							Toast.makeText(NewFriendActivity.this,
									"You have dropped", Toast.LENGTH_SHORT)
									.show();
							return;
						}
						NoticeDao.getInstance().updateAddFriendStatus(
								notice.getId(), Notice.READ,
								"Add friends success");
						refresh();
					}
				}).show();
	}

	/**
	 * 回复一个presence信息给用户
	 * 
	 * @param type
	 * @param to
	 */
	protected void sendSubscribe(Presence.Type type, String to) {
		Presence presence = new Presence(type);
		presence.setTo(to);
		if (XmppConnectionServer.getInstance().isConn()) {
			XmppConnectionServer.getInstance().getConnection()
					.sendPacket(presence);
		}
	}

	// 广播
	private class ContacterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Notice notice = (Notice) intent.getSerializableExtra("notice");
			// String action = intent.getAction();
			inviteNotices.add(notice);
			refresh();
		}
	}

	private void refresh() {
		inviteNotices = NoticeDao.getInstance().getNoticeListByType(Notice.All);
		Collections.sort(inviteNotices);
		noticeAdapter.setNoticeList(inviteNotices);
		noticeAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == event.KEYCODE_BACK) {
			Intent mIntent = new Intent();
			mIntent.putExtra("backResult", "refresh");
			NewFriendActivity.this.setResult(0, mIntent);
			NewFriendActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void doAsyncTaskBefore(int taskId) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object doAsyncTaskIn(int taskId, Object... params) {
		// TODO Auto-generated method stub
		// 睡一会 给一些roster 监听处理的时间
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doCancelled(int taskId) {
		// TODO Auto-generated method stub

	}

}
