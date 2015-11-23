package com.st.stchat.activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.R;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.dao.ChatRoomMessageDao;
import com.st.stchat.dao.NoticeDao;
import com.st.stchat.fragment.ContactFragment;
import com.st.stchat.fragment.MessageFragment;
import com.st.stchat.fragment.SettingFragment;
import com.st.stchat.model.Notice;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.InfoUtils;

/**
 * 
 * @author juwei 2014.11.20
 * 
 */
public class MainActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "MainActivity";
	private static FragmentManager mFragmentManager = null;
	private static FragmentTransaction mFragmentTransaction = null;
	private LinearLayout ll_contact, ll_message, ll_setting;
	private ImageView iv_search, iv_right_add, iv_contact, iv_message,
			iv_setting;
	private ContacterReceiver mContacterReceiver = null;
	private TextView unread_msg_contact, unread_chat_contact;
	private View view;
	private PopupWindow pop;
	private Button addFriBtn, createChatRoomBtn;

	private NoticeDao noticeDao = new NoticeDao();
	private RefreshMainLayoutReceiver refreshMainLayoutReceiver;

	private boolean isExit;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			if (msg.what == 1) {
				initChatCount();
			}
			if (msg.what == 0) {
				isExit = false;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "主界面Main初始化启动");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		// 初始化屏幕下方按钮组
		initView();
		setListener();
		// 拿到整个应用的FragmentManager管理器
		mFragmentManager = getFragmentManager();
		// 加载菜单栏图标
		loadMessageIcon();
		// 加载中间界面
		loadFragment(new MessageFragment(), null);
		initPopupWindow();
	}

	private void initView() {
		ll_contact = (LinearLayout) findViewById(R.id.ll_contact);
		ll_message = (LinearLayout) findViewById(R.id.ll_message);
		ll_setting = (LinearLayout) findViewById(R.id.ll_setting);
		iv_search = (ImageView) findViewById(R.id.iv_search);
		iv_right_add = (ImageView) findViewById(R.id.iv_right_add);
		iv_contact = (ImageView) findViewById(R.id.iv_contact);
		iv_message = (ImageView) findViewById(R.id.iv_message);
		iv_setting = (ImageView) findViewById(R.id.iv_setting);
		unread_msg_contact = (TextView) findViewById(R.id.unread_msg_contact);
		unread_msg_contact.setVisibility(View.GONE);
		unread_chat_contact = (TextView) findViewById(R.id.unread_chat_contact);
		unread_chat_contact.setVisibility(View.GONE);
	}

	private void setListener() {
		ll_contact.setOnClickListener(this);
		ll_message.setOnClickListener(this);
		ll_setting.setOnClickListener(this);
		iv_search.setOnClickListener(this);
		iv_right_add.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 启动控制消息气泡广播接收器
		refreshMainLayoutReceiver = new RefreshMainLayoutReceiver();
		IntentFilter filter0 = new IntentFilter();
		filter0.addAction(ConstantUtils.SINGLE_CHAT_ACTION);
		filter0.addAction(ConstantUtils.SINGLE_CHAT_PIC_ACTION);
		filter0.addAction(ConstantUtils.GROUP_CHAT_ACTION);
		filter0.addAction(ConstantUtils.MESSAGE_DELETED_ACTION);
		MainActivity.this.registerReceiver(refreshMainLayoutReceiver, filter0);

		// 初始化控制联系人请求气泡广播接收器
		mContacterReceiver = new ContacterReceiver();
		IntentFilter filter1 = new IntentFilter();
		filter1.addAction(ConstantUtils.ROSTER_DELETED_ACTION);// 好友删除
		filter1.addAction(ConstantUtils.CONTACT_DELETED_ACTION);// 本地好友删除
		filter1.addAction(ConstantUtils.ROSTER_SUBSCRIPTION_ACTION);// 好友请求
		MainActivity.this.registerReceiver(mContacterReceiver, filter1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 加载聊天未读消息数量
		initChatCount();
		// 初始化未加联系人的数量气泡
		reFreshAddFriends();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// System.out.println("MainActivity ----  onPause()");
	}

	@Override
	protected void onStop() {
		// 卸载控制消息气泡广播接收器
		unregisterReceiver(refreshMainLayoutReceiver);
		// 卸载控制好友请求气泡广播接收器
		unregisterReceiver(mContacterReceiver);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_search:
			// 该功能尚未开放
			Toast.makeText(this, "This function is not yet open",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.iv_right_add:
			if (pop.isShowing()) {
				pop.dismiss();
				return;
			} else {
				pop.showAsDropDown(v, 0, -422);
			}
			break;
		case R.id.ll_contact:
			loadContactIcon();
			loadFragment(new ContactFragment(), null);
			break;
		case R.id.ll_message:
			loadMessageIcon();
			loadFragment(new MessageFragment(), null);
			break;
		case R.id.ll_setting:
			loadSettingIcon();
			loadFragment(new SettingFragment(), null);
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 统计未读消息的条数
	 */
	private void initChatCount() {
		String account = InfoUtils.getUser(MainActivity.this);
		Log.d(TAG, "统计 " + account + "的未读消息条数");
		ChatMessageDao cmd = new ChatMessageDao();
		int singleCount = cmd.findNotReadByAccount(account);
		ChatRoomMessageDao roomMessageDao = new ChatRoomMessageDao();
		int roomCount = roomMessageDao.findNotReadRoomMsg(account);
		int unreadNum = singleCount + roomCount;

		Log.e(TAG, "当前有：" + unreadNum + "条未读消息");
		if (unreadNum > 0) {
			unread_chat_contact.setVisibility(View.VISIBLE);
			if (unreadNum > 99) {
				unread_chat_contact.setText("" + "99+");
			} else {
				unread_chat_contact.setText("" + unreadNum);
			}
		} else {
			unread_chat_contact.setVisibility(View.GONE);
		}
	}

	/*
	 * 刷新好友请求的气泡
	 */
	private void reFreshAddFriends() {

		Integer countAdd = noticeDao
				.getUnReadNoticeCountByType(Notice.ADD_FRIEND);
		if (countAdd > 0) {
			unread_msg_contact.setVisibility(View.VISIBLE);
			if (countAdd > 99) {
				unread_msg_contact.setText("" + "99+");
			} else {
				unread_msg_contact.setText("" + countAdd + "");
			}
		} else {
			unread_msg_contact.setVisibility(View.GONE);
		}

	}

	private void exit() {
		if (!isExit) {
			isExit = true;
			Toast.makeText(MainActivity.this,
					"Press again to exit the program", Toast.LENGTH_SHORT)
					.show();// 再按一次退出程序
			mHandler.sendEmptyMessageDelayed(0, 2000);
		} else {
			// 在后台运行而不真实退出
			moveTaskToBack(true);
		}
	}

	// 点击按钮时，加载进来fragment
	@SuppressLint("CommitTransaction")
	public static void loadFragment(Fragment fragment, String tag) {
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mFragmentTransaction.replace(R.id.rl_center_zhijie, fragment, tag);
		mFragmentTransaction.commit();
	}

	private void loadContactIcon() {
		iv_contact.setImageResource(R.drawable.lianxiren_bai);
		iv_message.setImageResource(R.drawable.liaotian_hui);
		iv_setting.setImageResource(R.drawable.geren_hui);
		iv_contact.setBackgroundColor(android.graphics.Color
				.parseColor("#23D5E3"));
		iv_message.setBackgroundColor(android.graphics.Color
				.parseColor("#EDEDED"));
		iv_setting.setBackgroundColor(android.graphics.Color
				.parseColor("#EDEDED"));
	}

	private void loadMessageIcon() {
		iv_contact.setImageResource(R.drawable.lianxiren_hui);
		iv_message.setImageResource(R.drawable.liaotian_bai);
		iv_setting.setImageResource(R.drawable.geren_hui);
		iv_contact.setBackgroundColor(android.graphics.Color
				.parseColor("#EDEDED"));
		iv_message.setBackgroundColor(android.graphics.Color
				.parseColor("#23D5E3"));
		iv_setting.setBackgroundColor(android.graphics.Color
				.parseColor("#EDEDED"));
	}

	private void loadSettingIcon() {
		iv_contact.setImageResource(R.drawable.lianxiren_hui);
		iv_message.setImageResource(R.drawable.liaotian_hui);
		iv_setting.setImageResource(R.drawable.geren_bai);
		iv_contact.setBackgroundColor(android.graphics.Color
				.parseColor("#EDEDED"));
		iv_message.setBackgroundColor(android.graphics.Color
				.parseColor("#EDEDED"));
		iv_setting.setBackgroundColor(android.graphics.Color
				.parseColor("#23D5E3"));
	}

	// 联系人广播
	private class ContacterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			System.out.println("MainActivity:有广播来了action是：" + action);

			if (action.equals(ConstantUtils.CONTACT_DELETED_ACTION)) {
				System.out.println("MainActivity:接受到删除广播刷新聊天气泡");
				// 刷新聊天气泡
				initChatCount();
				// 刷新好友请求气泡
				reFreshAddFriends();
			}

			if (action.equals(ConstantUtils.ROSTER_DELETED_ACTION)) {
				System.out.println("MainActivity:接受到删除广播刷新聊天气泡");
				// 刷新聊天气泡
				initChatCount();
				// 刷新好友请求气泡
				reFreshAddFriends();
			}

			if (action.equals(ConstantUtils.ROSTER_SUBSCRIPTION_ACTION)) {
				System.out.println("MainActivity:接受到好友请求广播刷新好友气泡");
				// 刷新好友请求气泡
				reFreshAddFriends();
			}
		}
	}

	private void initPopupWindow() {
		view = this.getLayoutInflater().inflate(R.layout.popup_window, null);
		addFriBtn = (Button) view.findViewById(R.id.addFriend);
		createChatRoomBtn = (Button) view.findViewById(R.id.createGroup);

		addFriBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this,
						AddFriendsActivity.class));
				pop.dismiss();
			}
		});

		createChatRoomBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this,
						CreateChatRoomGetNameActivity.class));
				pop.dismiss();
			}
		});

		pop = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		pop.setFocusable(true);
		pop.setOutsideTouchable(true);
		ColorDrawable dw = new ColorDrawable(0000000000);
		pop.setBackgroundDrawable(dw);
		pop.setAnimationStyle(R.style.AnimationPreview);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				pop.dismiss();
			}
		});
	}

	public class RefreshMainLayoutReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "主界面收到未读消息，通知最下方菜单栏的小红更新吧");
			new Thread(new Runnable() {
				@Override
				public void run() {
					Message message = new Message();
					message.what = 1;
					mHandler.sendMessage(message);
					// Log.i("revice message", "refresh main layout");
				}
			}).start();
		}

	}

}
