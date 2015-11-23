package com.st.stchat.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.adapter.STChatRoomAdapter;
import com.st.stchat.bean.GroupMessageEntity;
import com.st.stchat.bean.MessageItem;
import com.st.stchat.dao.ChatRoomMessageDao;
import com.st.stchat.manager.ChatRoomManager;
import com.st.stchat.message.STChatMessage;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.DateUtil;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.Logger;
import com.st.stchat.utils.TimeUtil;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 聊天室 页面
 * 
 * @author  
 * 
 */
public class GroupChatActivity extends BaseActivity implements OnClickListener,
		TextWatcher {
	private static final String TAG = "GroupChatActivity";
	private static final int RECEIVE_MESSAGE = 1;
	private static final String GROUP_CHAT_ACTION = "com.st.groupchat";
	private TextView textViewTitle;
	// private Button butt_send;
	private ImageButton buttonTitleLeft;
	private ImageView iv_singlechat_pic, iv_singlechat_emoji,
			iv_singlechat_voice, iv_singlechat_keyboard, iv_singlechat_send;
	private EditText et_text;
	private String roomJid;
	private String accountName;
	private GroupChatReceiver groupChatReceiver;
	private List<STChatMessage> mData;
	private ListView mListView;
	private static STChatRoomAdapter chatRoomAdapter;

	private Handler receiveMessageHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RECEIVE_MESSAGE:
				System.out
						.println("Handler  ---------    case RECEIVE_MESSAGE:");

				chatRoomAdapter.Refresh();
				// mListView.smoothScrollToPositionFromTop(mData.size(), 0);
				mListView.setSelection(chatRoomAdapter.getCount());
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_chat);

		accountName = InfoUtils.getUser(GroupChatActivity.this);// 拿到当前登陆账户
		Intent intent = getIntent();
		String[] intentStrArr = intent.getStringArrayExtra("give_chatroom");
		roomJid = intentStrArr[1];// 当前聊天室JID
		initView();
		initListener();

		textViewTitle.setVisibility(View.VISIBLE);
		textViewTitle.setText("" + roomJid.split("@")[0]);
		mListView.setVerticalScrollBarEnabled(false);

		mData = loadData();
	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	private void initView() {
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		mListView = (ListView) findViewById(R.id.lv_mainlist);
		buttonTitleLeft = (ImageButton) findViewById(R.id.buttonTitleLeft);
		et_text = (EditText) findViewById(R.id.et_text);
		// butt_send = (Button) findViewById(R.id.butt_send);
		iv_singlechat_pic = (ImageView) findViewById(R.id.iv_singlechat_pic);
		iv_singlechat_emoji = (ImageView) findViewById(R.id.iv_singlechat_emoji);
		iv_singlechat_voice = (ImageView) findViewById(R.id.iv_singlechat_voice);
		iv_singlechat_keyboard = (ImageView) findViewById(R.id.iv_singlechat_keyboard);
		iv_singlechat_send = (ImageView) findViewById(R.id.iv_singlechat_send);
	}

	private void initListener() {
		buttonTitleLeft.setOnClickListener(this);
		// butt_send.setOnClickListener(this);
		iv_singlechat_pic.setOnClickListener(this);
		iv_singlechat_emoji.setOnClickListener(this);
		iv_singlechat_voice.setOnClickListener(this);
		iv_singlechat_keyboard.setOnClickListener(this);
		iv_singlechat_send.setOnClickListener(this);
		et_text.addTextChangedListener(this);
	}

	@Override
	protected void onStart() {

		super.onStart();
		chatRoomAdapter = new STChatRoomAdapter(this, mData);
		mListView.setAdapter(chatRoomAdapter);

		groupChatReceiver = new GroupChatReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GROUP_CHAT_ACTION);
		GroupChatActivity.this.registerReceiver(groupChatReceiver, filter);
		// 从数据库里面取出以前的聊天记录并显示
		loadOldMessage();
	}

	private void loadOldMessage() {
		STChatMessage message = null;
		ChatRoomMessageDao roomMessageDao = new ChatRoomMessageDao();
		List<GroupMessageEntity> list = roomMessageDao
				.getAllListByAccountWithRoomJid(accountName, roomJid);

		for (GroupMessageEntity gme : list) {
			String dateStr = TimeUtil.parseDate(TimeUtil.millisToData(1, Long
					.valueOf(gme.getChatTime()).longValue()));
			// message = new STChatMessage(STChatMessage.MessageType_Time,
			// dateStr);
			// mData.add(message);

			if (gme.getChatType().equals("1")) {
				// Logger.d(TAG, "消息类型为 >>>> 1 ：发送出去的消息 <<<<");
				message = new STChatMessage(STChatMessage.MessageType_To,
						gme.getChatText(), "", Long.valueOf(gme.getChatTime())
								.longValue(), STChatMessage.MessageStyle_Text,
						"");
			} else if (gme.getChatType().equals("2")) {
				// Logger.d(TAG, "消息类型为 <<<< 2： 接收的消息 <<<<");
				message = new STChatMessage(STChatMessage.MessageType_From,
						gme.getChatText(), gme.getChatFrom(), Long.valueOf(
								gme.getChatTime()).longValue(),
						STChatMessage.MessageStyle_Text, "");

				// 如果该条消息为未读，就通过该条消息的时间，将未读0改未已读1
				if (gme.getChatRead().equals("0")) {
					ChatRoomMessageDao dao = new ChatRoomMessageDao();
					int changeCount = dao.updateReadByTime(gme.getChatTime(),
							"1");
					Log.d(TAG, "---- 修改了：" + changeCount + "条消息阅读状态");
				}
			}
			mData.add(message);
		}

		chatRoomAdapter.Refresh();
		mListView.setSelection(chatRoomAdapter.getCount());
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.iv_singlechat_send:

			if (!XmppConnectionServer.getInstance().isConn()) {
				Toast.makeText(this, "You have not connection",
						Toast.LENGTH_SHORT).show();
				return;
			}
			String chatStr = et_text.getText().toString();
			if (!TextUtils.isEmpty(chatStr)) {
				STChatMessage message = null;
				// 获取当前发送时间
				long nowTime = new Date().getTime();
				// String dateStr = TimeUtil.parseDate(TimeUtil.millisToData(1,
				// Long.valueOf(nowTime).longValue()));

				// 添加消息到message缓存里面
				Date date = new Date(Long.parseLong(nowTime + ""));
				// 判断该用户消息是否存在缓存中 不存就创建item
				if (STChatApplication.messageCache.get(roomJid) == null) {
					// 创建
					MessageItem item = new MessageItem();
					item.setIconRes(R.drawable.default_nor_man);
					item.setMsg(chatStr);
					item.setTime(DateUtil.date2Str(date));
					item.setTitle(roomJid);
					item.setUnReadSum(0);
					STChatApplication.messageCache.put(roomJid, item);
				} else {
					// 修改
					STChatApplication.messageCache.get(roomJid).setMsg(chatStr);
					STChatApplication.messageCache.get(roomJid).setUnReadSum(0);
					STChatApplication.messageCache.get(roomJid).setTime(
							DateUtil.date2Str(date));
				}

				// 构造时间消息
				// message = new STChatMessage(STChatMessage.MessageType_Time,
				// dateStr);
				// mData.add(message);
				// 先存数据库，在发送出去，最后显示
				ChatRoomMessageDao roomMessageDao = new ChatRoomMessageDao();
				long lon = roomMessageDao.add("", accountName, "1",
						accountName, roomJid,
						ConstantUtils.MESSAGE_RECEIVE_READ, nowTime + "",
						chatStr);
				System.out.println("----chatroom存进去第：" + lon + " 条聊天数据----");

				ChatRoomManager.sendMeassage(GroupChatActivity.this, chatStr,
						roomJid);
				message = new STChatMessage(STChatMessage.MessageType_To,
						chatStr, "", Long.valueOf(nowTime).longValue(),
						STChatMessage.MessageStyle_Text, "");
				mData.add(message);

				chatRoomAdapter.Refresh();
			}
			// 清空输入框
			et_text.setText("");

			mListView.setSelection(chatRoomAdapter.getCount());
			break;
		case R.id.buttonTitleLeft:
			GroupChatActivity.this.finish();
			break;
		}
	}

	private class GroupChatReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			Logger.d(TAG, "聊天界面收到一条广播，通知界面更新吧");

			Bundle bundle = intent.getExtras();
			String[] receiveStrArr = bundle.getStringArray("msg");
			if (receiveStrArr[0].equalsIgnoreCase(roomJid)) {
				// String dateStr = TimeUtil.parseDate(TimeUtil.millisToData(1,
				// Long.valueOf(receiveStrArr[2]).longValue()));

				// 修改内存该用户发来消息的状态 改未读消息数量为0
				STChatApplication.messageCache.get(receiveStrArr[0])
						.setUnReadSum(0);

				STChatMessage message = null;
				// message= new STChatMessage(
				// STChatMessage.MessageType_Time, dateStr);
				// mData.add(message);
				message = new STChatMessage(STChatMessage.MessageType_From,
						receiveStrArr[3], receiveStrArr[1], Long.valueOf(
								receiveStrArr[2]).longValue(),
						STChatMessage.MessageStyle_Text, "");
				mData.add(message);

				// 下面要更改消息的状态
				ChatRoomMessageDao dao = new ChatRoomMessageDao();
				int changeCount = dao.updateReadByTime(receiveStrArr[2], "1");
				Log.d(TAG, "---- 修改了：" + changeCount + "条消息阅读状态");
				new Thread(new Runnable() {
					@Override
					public void run() {
						// Message message = new Message();
						// message.what = RECEIVE_MESSAGE;
						receiveMessageHandler.sendMessage(receiveMessageHandler
								.obtainMessage(RECEIVE_MESSAGE));
					}

				}).start();
			}

		}

	}

	private List<STChatMessage> loadData() {
		List<STChatMessage> messages = new ArrayList<STChatMessage>();

		return messages;
	}

	@Override
	protected void onStop() {

		super.onStop();
		unregisterReceiver(groupChatReceiver);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

		// Log.i("beforeTextChanged", "-----------------------");
		// Log.e("beforeTextChanged", "s:" + s + " start:" + start + " count:"
		// + count + " after:" + after);

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

		// Log.i("onTextChanged", "-----------------------");
		// Log.e("onTextChanged", "s:" + s + " start:" + start + " before:"
		// + before + " count:" + count);

	}

	@Override
	public void afterTextChanged(Editable s) {

		// Log.i("afterTextChanged", "-----------------------");
		// Log.e("afterTextChanged", "s:" + s);
		// s:之后的文字内容
		// System.out.println("+++++" + s);
		if (s.length() == 0) {
			iv_singlechat_send.setVisibility(View.GONE);
			iv_singlechat_voice.setVisibility(View.VISIBLE);
			iv_singlechat_keyboard.setVisibility(View.GONE);
		} else {
			iv_singlechat_voice.setVisibility(View.GONE);
			iv_singlechat_keyboard.setVisibility(View.GONE);
			iv_singlechat_send.setVisibility(View.VISIBLE);

		}

	}

}
