package com.st.stchat.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.adapter.STChatAdapter;
import com.st.stchat.bean.MessageEntity;
import com.st.stchat.bean.MessageItem;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.manager.ChatManager;
import com.st.stchat.message.STChatMessage;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.DateUtil;
import com.st.stchat.utils.FileUtils;
import com.st.stchat.utils.ImageUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.Logger;
import com.st.stchat.utils.StatusUtils;
import com.st.stchat.utils.StringUtil;
import com.st.stchat.utils.TimeUtil;
import com.st.stchat.xmpp.XmppConnectionServer;

public class SingleChatActivity extends BaseActivity implements
		OnClickListener, TextWatcher, OnItemLongClickListener,
		OnLongClickListener, OnTouchListener {
	private static final String TAG = "SingleChatActivity";
	private static final int RECEIVE_MESSAGE = 1;
	private static final int PIC_RECEIVE = 2;
	private static final int PIC_SEND = 3;
	private static final int RECEIVE_FRIEND_STATUS = 4;

	private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int PHOTO_REQUEST_CUT = 3;// 结果

	/**
	 * 调用系统相机之后的缓存目录对象
	 */
	private File fileCache;

	private TextView textViewTitle, textViewState, tv_voice;
	private Button butt_send;
	private ImageButton buttonTitleLeft;
	private EditText et_text;
	private ImageView iv_singlechat_pic, iv_singlechat_emoji,
			iv_singlechat_voice, iv_singlechat_keyboard, iv_singlechat_send;
	private List<STChatMessage> mData;
	private static STChatAdapter mAdapter;
	private String accountName, shortJID, longJID, chatName,
			chatFriendStatus = ConstantUtils.STATUS_UNAVAILABLE;
	private ListView mListView;
	private SingleChatReceiver mSingleChatReceiver;

	private boolean longClicked = false;
	private Handler receiveMessageHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RECEIVE_MESSAGE:
				System.out.println("Handler ---- case RECEIVE_MESSAGE:");

				// mAdapter.Refresh();
				mAdapter.notifyDataSetChanged();
				// mListView.smoothScrollToPositionFromTop(mData.size(), 0);
				// mListView.setSelection(mAdapter.getCount());
				break;
			case PIC_RECEIVE:

				break;
			case PIC_SEND:

				break;
			case RECEIVE_FRIEND_STATUS:
				// 在这里改变Title的在线情况状态

				textViewState.setText("" + chatFriendStatus);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_single_chat);

		accountName = InfoUtils.getUser(SingleChatActivity.this);// 拿到当前登陆账户
		Intent intent = getIntent();
		String[] intentStrArr = intent.getStringArrayExtra("give_chat");
		shortJID = intentStrArr[1];
		// longJID =
		chatName = StringUtils.parseName(shortJID);// 拿到当前是在和谁聊天
		Log.d(TAG, "当前聊天对象： " + shortJID + " \n chatName = " + chatName);
		// 取得内存中的用户状态
		String[] chatArr = XmppConnectionServer.map.get(chatName);
		if (chatArr != null) {
			String statuStr = StatusUtils.getStatusStr(chatArr[0], chatArr[1],
					chatArr[2], chatArr[3]);
			chatFriendStatus = statuStr;
			longJID = chatArr[0];
		}

		initView();
		initListener();
		textViewTitle.setVisibility(View.VISIBLE);
		textViewState.setVisibility(View.VISIBLE);
		textViewTitle.setText("" + chatName);
		textViewState.setText("" + chatFriendStatus);
		mListView.setVerticalScrollBarEnabled(false);
		mData = loadData();

		mAdapter = new STChatAdapter(this, mData);
		mListView.setAdapter(mAdapter);
		// mListView.smoothScrollToPositionFromTop(mData.size(), 0);

		// 注册来文字图片消息的广播接收器
		mSingleChatReceiver = new SingleChatReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConstantUtils.SINGLE_CHAT_ACTION);
		filter.addAction(ConstantUtils.SINGLE_CHAT_PIC_ACTION);
		filter.addAction(ConstantUtils.SINGLE_CHAT_MESSAGE_STATUS);
		filter.addAction(ConstantUtils.SINGLE_CHAT_FRIEND_STATUS);
		SingleChatActivity.this.registerReceiver(mSingleChatReceiver, filter);
		// 从数据库里面取出以前的聊天记录并显示
		loadOldMessage();

	}

	private void initView() {
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		textViewState = (TextView) findViewById(R.id.textViewState);
		mListView = (ListView) findViewById(R.id.lv_mainlist);
		et_text = (EditText) findViewById(R.id.et_text);
		tv_voice = (TextView) findViewById(R.id.tv_voice);
		buttonTitleLeft = (ImageButton) findViewById(R.id.buttonTitleLeft);
		butt_send = (Button) findViewById(R.id.butt_send);
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
		tv_voice.setOnClickListener(this);
		tv_voice.setOnLongClickListener(this);
		tv_voice.setOnTouchListener(this);
		mListView.setOnItemLongClickListener(this);

	}

	@Override
	protected void onStart() {
		Log.e(TAG, "onStart()");
		super.onStart();
	}

	@Override
	protected void onResume() {
		// Log.e(TAG, "onResume()");
		super.onResume();

	}

	private void loadOldMessage() {
		// Log.d(TAG, "----loadOldMessage----");
		STChatMessage message = null;
		ChatMessageDao cmd = new ChatMessageDao();
		List<MessageEntity> messageEntitys = cmd.findAllByAccount(accountName,
				chatName);
		for (MessageEntity me : messageEntitys) {
			// Logger.d(TAG, "----进入遍历----"+me.getChatTime());
			String dateStr = TimeUtil.parseDate(TimeUtil.millisToData(1, Long
					.valueOf(me.getChatTime()).longValue()));
			// TODO
			// message = new STChatMessage(STChatMessage.MessageType_Time,
			// dateStr);
			// mData.add(message);
			if (me.getChatType().equals("1")) {
				// Logger.d(TAG, "消息类型为 >>>> 1 ：发送出去的消息 <<<<");
				if (me.getChatStyle().equals(STChatMessage.MessageStyle_Text)) {
					message = new STChatMessage(me.getChatPacketId(),
							me.getChatSendStatus(),
							STChatMessage.MessageType_To, me.getChatText(), "",
							Long.valueOf(me.getChatTime()).longValue(),
							STChatMessage.MessageStyle_Text, "");
				} else if (me.getChatStyle().equals(
						STChatMessage.MessageStyle_Pic)) {
					message = new STChatMessage(me.getChatPacketId(),
							me.getChatSendStatus(),
							STChatMessage.MessageType_To, me.getChatText(), "",
							Long.valueOf(me.getChatTime()).longValue(),
							STChatMessage.MessageStyle_Pic, me.getChatPicPath());
				}
			} else if (me.getChatType().equals("2")) {
				// Logger.d(TAG, "消息类型为 <<<< 2： 接收的消息 <<<<");
				if (me.getChatStyle().equals(STChatMessage.MessageStyle_Text)) {
					message = new STChatMessage("", "",
							STChatMessage.MessageType_From, me.getChatText(),
							me.getChatFrom(), Long.valueOf(me.getChatTime())
									.longValue(),
							STChatMessage.MessageStyle_Text, "");

				} else if (me.getChatStyle().equals(
						STChatMessage.MessageStyle_Pic)) {
					message = new STChatMessage("", "",
							STChatMessage.MessageType_From, me.getChatText(),
							me.getChatFrom(), Long.valueOf(me.getChatTime())
									.longValue(),
							STChatMessage.MessageStyle_Pic, me.getChatPicPath());
				}
				// 如果该条消息为未读，就通过该条消息的时间，将未读0改未已读1
				if (me.getChatRead().equals("0")) {
					ChatMessageDao cmdR = new ChatMessageDao();
					int changeCount = cmdR.updateReadByTime(me.getChatTime(),
							"1");
					Log.d(TAG, "---- 修改了：" + changeCount + "条消息阅读状态");
				}
			}
			mData.add(message);
		}

		// Logger.d(TAG, "----准备frash----");

		// mAdapter.Refresh();
		mAdapter.notifyDataSetChanged();
		// mListView.smoothScrollToPositionFromTop(mData.size(), 0);
		// mListView.smoothScrollToPosition(mListView.getCount() - 1);// 移动到尾部
		mListView.setSelection(mAdapter.getCount());
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.iv_singlechat_pic:
			// 选择发送图片
			if (!TextUtils.isEmpty(longJID)) {
				selectImgToSend();
			} else {
				Toast.makeText(this,
						"Friend is not online, you can't send a picture",
						Toast.LENGTH_SHORT).show();// 对方当前不在线，无法发送图片
			}

			break;
		case R.id.iv_singlechat_send:
			// 发送消息
			final String chatStr = et_text.getText().toString().trim();
			if (!TextUtils.isEmpty(chatStr)) {
				STChatMessage message = null;
				// 获取当前发送时间
				final long nowTime = new Date().getTime();
				// String dateStr = TimeUtil.parseDate(TimeUtil.millisToData(1,
				// Long.valueOf(nowTime).longValue()));

				// 添加消息到message缓存里面
				Date date = new Date(Long.parseLong(nowTime + ""));
				// 判断该用户消息是否存在缓存中 不存就创建item
				if (STChatApplication.messageCache.get(chatName) == null) {
					// 创建
					MessageItem item = new MessageItem();
					item.setIconRes(R.drawable.default_nor_man);
					item.setMsg(chatStr);
					item.setTime(DateUtil.date2Str(date));
					item.setTitle(chatName);
					item.setUnReadSum(0);
					STChatApplication.messageCache.put(chatName, item);
					item = null;
				} else {
					// 修改
					STChatApplication.messageCache.get(chatName)
							.setMsg(chatStr);
					STChatApplication.messageCache.get(chatName)
							.setUnReadSum(0);
					STChatApplication.messageCache.get(chatName).setTime(
							DateUtil.date2Str(date));
				}

				// TODO
				// 构造时间消息
				// message = new STChatMessage(STChatMessage.MessageType_Time,
				// dateStr);
				// mData.add(message);
				// 测试发送聊天消息
				// 先存数据库，在发送出去，最后显示
				message = new STChatMessage("" + nowTime,
						ConstantUtils.MESSAGE_SEND_SSSS,
						STChatMessage.MessageType_To, chatStr, "", Long
								.valueOf(nowTime).longValue(),
						STChatMessage.MessageStyle_Text, "");
				mData.add(message);

				new Thread(new Runnable() {
					@Override
					public void run() {

						org.jivesoftware.smack.packet.Message mMessage = new org.jivesoftware.smack.packet.Message();
						mMessage.setFrom(accountName);
						mMessage.setTo(shortJID);
						mMessage.setBody(chatStr);
						mMessage.setPacketID("" + nowTime);

						boolean isSend = false;
						if (XmppConnectionServer.getInstance().isConn()) {

							// 对该消息要求有回执
							DeliveryReceiptManager
									.addDeliveryReceiptRequest(mMessage);
							isSend = ChatManager.sendMeassage(
									SingleChatActivity.this, chatName,
									mMessage, null);
							Log.e(TAG, "发送状态：---" + isSend);

						} else {
							// 这个地方要通过handler来发送
							// Toast.makeText(SingleChatActivity.this,
							// "You have dropped", Toast.LENGTH_SHORT)
							// .show();
						}
						ChatMessageDao cmd = new ChatMessageDao();
						String sendStatus = "";
						if (isSend) {
							// 发送成功
							sendStatus = ConstantUtils.MESSAGE_SEND_SUCCESS;
						} else {
							// 发送失败
							sendStatus = ConstantUtils.MESSAGE_SEND_ERROR;
						}
						long lon = cmd.add("" + nowTime, accountName, "1",
								accountName, chatName,
								ConstantUtils.MESSAGE_RECEIVE_READ, sendStatus,
								"" + nowTime, chatStr,
								STChatMessage.MessageStyle_Text, "");
						System.out.println("----存进去第：" + lon + " 条聊天数据----");
						// 在这里刷新适配器重新修改发送状态数据
						refreshSendStatus("" + nowTime, sendStatus);
						sendStatus = "";
					}
				}).start();

				// 测试结束

				// 构造文本输入消息
				// Message = new STChatMessage(STChatMessage.MessageType_To,
				// et_text.getText().toString());
				// mData.add(Message);
				// 构造模拟的返回消息，
				// Message = new STChatMessage(STChatMessage.MessageType_From,
				// "收到！");
				// mData.add(Message);
				// 更新数据
				// mAdapter.Refresh();
				mAdapter.notifyDataSetChanged();

			}
			// 清空输入框
			et_text.setText("");
			// 滚动列表到当前消息
			// mListView.smoothScrollToPositionFromTop(mData.size(), 0);

			mListView.setSelection(mAdapter.getCount());
			break;
		case R.id.buttonTitleLeft:
			SingleChatActivity.this.finish();
			// STChatApplication.getInstance().closeActivity(this);
			break;
		case R.id.et_text:
			// System.out.println("et_text点击--------");
			// mAdapter.Refresh();
			// mListView.smoothScrollToPositionFromTop(mData.size(), 0);
			break;
		case R.id.iv_singlechat_voice:
			iv_singlechat_voice.setVisibility(View.GONE);
			iv_singlechat_keyboard.setVisibility(View.VISIBLE);
			et_text.setVisibility(View.GONE);
			tv_voice.setVisibility(View.VISIBLE);
			break;
		case R.id.iv_singlechat_keyboard:
			iv_singlechat_keyboard.setVisibility(View.GONE);
			iv_singlechat_voice.setVisibility(View.VISIBLE);
			tv_voice.setVisibility(View.GONE);
			et_text.setVisibility(View.VISIBLE);
			break;
		case R.id.tv_voice:
			Toast.makeText(this, "时间太短", Toast.LENGTH_SHORT).show();

			break;
		default:
			break;
		}
	}

	/**
	 * 选择或者拍摄一张照片并发送
	 */
	private void selectImgToSend() {
		new com.st.stchat.widget.AlertDialog(SingleChatActivity.this).builder()
				.setTitle("Take Photo")
				.setPositiveButton("Camera", new OnClickListener() {
					@Override
					public void onClick(View v) {
						fileCache = new FileUtils().creatPhotoCache();
						// 调用系统的拍照功能
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						// 指定调用相机拍照后照片的储存路径
						intent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(fileCache));
						startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);

					}
				}).setNegativeButton("Photo", new OnClickListener() {
					@Override
					public void onClick(View v) {

						Intent intent = new Intent(Intent.ACTION_PICK, null);
						intent.setDataAndType(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
						startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
					}
				}).show();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "requestCode -- " + requestCode + " \n resultCode -- "
				+ resultCode + " \n data=null?" + (data == null));
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case PHOTO_REQUEST_TAKEPHOTO:
			// 将通过照相保存文件后返回的File对象或者是拿到
			if (fileCache != null) {
				// 将本地路径传入参数，执行发送文件
				sendPicToView(fileCache.getPath());
			} else {
				Toast.makeText(this, "没有选取照片", Toast.LENGTH_SHORT).show();
			}
			break;

		case PHOTO_REQUEST_GALLERY:
			// 将通过从相册选取的照片的路径Uri拿到
			if (data != null) {
				Uri uri = data.getData();
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = this.managedQuery(uri, proj, null, null, null);
				int index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String img_path = cursor.getString(index);
				System.out.println("选择的img_path ==== " + img_path);
				// 将本地路径传入参数，执行发送文件
				sendPicToView(img_path);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 进入聊天界面的时候，加载以前的聊天数据并返回
	 * 
	 * @return
	 */
	private List<STChatMessage> loadData() {
		List<STChatMessage> messages = new ArrayList<STChatMessage>();
		return messages;
	}

	@Override
	protected void onPause() {

		// Log.e(TAG, "onPause()");
		super.onPause();

	}

	@Override
	protected void onStop() {

		// Log.e(TAG, "onStop()");

		super.onStop();

	}

	@Override
	protected void onDestroy() {
		// Log.e(TAG, "onDestroy()");
		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().clearDiskCache();
		unregisterReceiver(mSingleChatReceiver);
		super.onDestroy();

	}

	// 将进行剪裁后的图片显示到UI界面上并且发送
	private void sendPicToView(final String imgPath) {
		System.out.println("---- 传进path  ：" + imgPath);
		// 将本地图片或者是照相图片的路径传进来之后，通过路径压缩后，再发送
		final String path = new FileUtils().saveBitmapToPic(ImageUtils
				.getimage(imgPath));

		// 将该文件路径添加线程进行发送
		STChatMessage message = null;
		// 获取当前发送时间
		long nowTime = new Date().getTime();
		// String dateStr = TimeUtil.parseDate(TimeUtil.millisToData(1, Long
		// .valueOf(nowTime).longValue()));

		// 添加消息到message缓存里面
		Date date = new Date(Long.parseLong(nowTime + ""));
		// 判断该用户消息是否存在缓存中 不存就创建item
		if (STChatApplication.messageCache.get(chatName) == null) {
			// 创建
			MessageItem item = new MessageItem();
			item.setIconRes(R.drawable.default_nor_man);
			item.setMsg("[picture]");
			item.setTime(DateUtil.date2Str(date));
			item.setTitle(chatName);
			item.setUnReadSum(0);
			STChatApplication.messageCache.put(chatName, item);
		} else {
			// 修改
			STChatApplication.messageCache.get(chatName).setMsg("[picture]");
			STChatApplication.messageCache.get(chatName).setUnReadSum(0);
			STChatApplication.messageCache.get(chatName).setTime(
					DateUtil.date2Str(date));
		}

		// TODO
		// 构造时间消息
		// message = new
		// STChatMessage(STChatMessage.MessageType_Time,
		// dateStr);
		// mData.add(message);
		// 测试发送聊天消息
		// 先存数据库，在发送出去，最后显示
		ChatMessageDao cmd = new ChatMessageDao();
		long lon = cmd.add("" + nowTime, accountName, "1", accountName,
				chatName, ConstantUtils.MESSAGE_RECEIVE_READ,
				ConstantUtils.MESSAGE_SEND_SUCCESS, "" + nowTime, "[picture]",
				STChatMessage.MessageStyle_Pic, path);
		System.out.println("----存进去第：" + lon + " 条聊天数据----");

		message = new STChatMessage(STChatMessage.MessageType_To, "[picture]",
				"", Long.valueOf(nowTime).longValue(),
				STChatMessage.MessageStyle_Pic, path);
		mData.add(message);

		if (XmppConnectionServer.getInstance().isConn()) {
			new Thread(new Runnable() {
				@Override
				public void run() {

					System.out.println("开启线程");
					// 128.199.252.234
					OutgoingFileTransfer transfer = XmppConnectionServer
							.getInstance().getOutgoingFileTransfer(longJID);// chatName
																			// +
																			// "@127.0.0.1/Spark 2.6.3"
					if (transfer != null) {
						// 发送图片文件
						try {
							System.out.println("发送路径为：" + path);
							transfer.sendFile(new File(path),
									"You won't believe this!");
							// while (!transfer.isDone()) {
							// if (transfer.getStatus().equals(
							// Status.error)) {
							// System.out.println("ERROR!!! "
							// + transfer.getError());
							// } else {
							// System.out.println(transfer.getStatus()
							// + "进度 "
							// + transfer.getProgress());
							// }
							// try {
							// Thread.sleep(500);
							// } catch (InterruptedException e) {
							//
							// e.printStackTrace();
							// }
							// }
						} catch (XMPPException e) {
							Log.e(TAG, "----照片文件发送异常");
							e.printStackTrace();

						} finally {

						}
					}

					System.out.println("线程开启完毕");
				}
			}).start();
		} else {
			Log.e(TAG, "失去连接");
			Toast.makeText(this, "You have dropped", Toast.LENGTH_SHORT).show();
		}

		// mAdapter.Refresh();
		mAdapter.notifyDataSetChanged();

		mListView.setSelection(mAdapter.getCount());
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

	private class SingleChatReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.d(TAG, "聊天界面收到一条广播，通知界面更新吧");
			if (intent.getAction().equals(ConstantUtils.SINGLE_CHAT_ACTION)) {
				Logger.d(TAG, "----收到聊天消息----");
				Bundle bundle = intent.getExtras();
				String[] receiveStrArr = bundle.getStringArray("msg");
				if (receiveStrArr[1].equalsIgnoreCase(accountName)
						&& receiveStrArr[0].equalsIgnoreCase(chatName)) {
					Logger.d(TAG, "----是[" + receiveStrArr[0] + "]这个对象发的----");
					// String dateStr =
					// TimeUtil.parseDate(TimeUtil.millisToData(
					// 1, Long.valueOf(receiveStrArr[2]).longValue()));

					// 修改内存该用户发来消息的状态 改未读消息数量为0
					STChatApplication.messageCache.get(receiveStrArr[0])
							.setUnReadSum(0);

					STChatMessage message = null;
					// TODO
					// message = new STChatMessage(
					// STChatMessage.MessageType_Time, dateStr);
					// mData.add(message);
					message = new STChatMessage(STChatMessage.MessageType_From,
							receiveStrArr[3], receiveStrArr[0], Long.valueOf(
									receiveStrArr[2]).longValue(),
							STChatMessage.MessageStyle_Text, "");
					mData.add(message);

					// 如果这里执行，那么必定是在聊天界面注册过的接受广播，
					// 将receiveStrArr[2]这个时间接受的消息，从未读改为已读
					ChatMessageDao cmdR = new ChatMessageDao();
					int changeCount = cmdR.updateReadByTime(receiveStrArr[2],
							"1");
					Log.d(TAG, "---- 修改了：" + changeCount + "条消息阅读状态");
					new Thread(new Runnable() {

						@Override
						public void run() {
							Message message = new Message();
							message.what = RECEIVE_MESSAGE;
							receiveMessageHandler.sendMessage(message);
						}

					}).start();
				} else {

				}
			} else if (intent.getAction().equals(
					ConstantUtils.SINGLE_CHAT_PIC_ACTION)) {
				Logger.d(TAG, "----收到图片消息----");
				Bundle bundle = intent.getExtras();
				String[] receiveStrArr = bundle.getStringArray("msg_pic");
				if (receiveStrArr[1].equalsIgnoreCase(accountName)
						&& receiveStrArr[0].equalsIgnoreCase(chatName)) {
					Logger.d(TAG, "----是[" + receiveStrArr[0] + "]这个人发的----");
					// String dateStr =
					// TimeUtil.parseDate(TimeUtil.millisToData(
					// 1, Long.valueOf(receiveStrArr[3]).longValue()));

					// 修改内存该用户发来消息的状态 改未读消息数量为0
					STChatApplication.messageCache.get(receiveStrArr[0])
							.setUnReadSum(0);

					STChatMessage message = null;
					// TODO
					// message = new STChatMessage(
					// STChatMessage.MessageType_Time, dateStr);
					// mData.add(message);
					message = new STChatMessage(STChatMessage.MessageType_From,
							receiveStrArr[2], receiveStrArr[0], Long.valueOf(
									receiveStrArr[3]).longValue(),
							STChatMessage.MessageStyle_Pic, receiveStrArr[5]);
					mData.add(message);
					// 如果这里执行，那么必定是在聊天界面注册过的接受广播，
					// 将receiveStrArr[3]这个时间接受的消息，从未读改为已读
					ChatMessageDao cmdR = new ChatMessageDao();
					int changeCount = cmdR.updateReadByTime(receiveStrArr[3],
							"1");
					Log.d(TAG, "---- 修改了：" + changeCount + "条消息阅读状态----设置为：已读");
					new Thread(new Runnable() {

						@Override
						public void run() {
							Message message = new Message();
							message.what = RECEIVE_MESSAGE;
							receiveMessageHandler.sendMessage(message);
						}

					}).start();

				}
			} else if (intent.getAction().equals(
					ConstantUtils.SINGLE_CHAT_MESSAGE_STATUS)) {
				Logger.d(TAG, "----收到Message反馈状态----");
				Bundle bundle = intent.getExtras();
				String[] receiveStrArr = bundle
						.getStringArray("message_status");

				refreshSendStatus(receiveStrArr[2],
						ConstantUtils.MESSAGE_SEND_HASRECEIVED);

			} else if (intent.getAction().equals(
					ConstantUtils.SINGLE_CHAT_FRIEND_STATUS)) {
				Logger.d(TAG, "----收到好友在线情况的改变----");
				Bundle bundle = intent.getExtras();
				// 四个参数 ，从哪来 ，状态的status的描述，状态的mode，状态的type类型
				String[] receiveStrArr = bundle.getStringArray("friend_status");
				if (StringUtil.getUserNameByJid(receiveStrArr[0])
						.equalsIgnoreCase(chatName)) {
					String statuStr = StatusUtils.getStatusStr(
							receiveStrArr[0], receiveStrArr[1],
							receiveStrArr[2], receiveStrArr[3]);
					chatFriendStatus = statuStr;
					if (chatFriendStatus.equals("unavailable")) {
						longJID = "";
					} else {
						longJID = receiveStrArr[0];
					}

					// 发送广播刷新数据
					new Thread(new Runnable() {
						@Override
						public void run() {
							Message message = new Message();
							message.what = RECEIVE_FRIEND_STATUS;
							receiveMessageHandler.sendMessage(message);
						}
					}).start();
				}

			}

		}

	}

	private void refreshSendStatus(String packedId, String messageSendStatus) {

		// 在这里new一个线程通过packetID去查看该消息是属于哪个item的
		for (int i = mData.size() - 1; i >= 0; i--) {
			if (mData.get(i).getPacketId().equals(packedId)) {
				System.out.println("前面mData.get(" + i
						+ ").getPacketId()---------"
						+ mData.get(i).getPacketId() + ",状态为："
						+ mData.get(i).getStatus());
				mData.get(i).setStatus(messageSendStatus);
				System.out.println("后面mData.get(" + i
						+ ").getPacketId()---------"
						+ mData.get(i).getPacketId() + ",状态为："
						+ mData.get(i).getStatus());
				new Thread(new Runnable() {
					@Override
					public void run() {
						Message message = new Message();
						message.what = RECEIVE_MESSAGE;
						receiveMessageHandler.sendMessage(message);
					}
				}).start();
				break;
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, long id) {
		System.out.println("position ---" + position);
		System.out.println("mData.get(position)----" + mData.get(position));
		System.out.println("mData.get(position).getStyle()-----"
				+ mData.get(position).getStyle());
		System.out.println("mData.get(position).getType()-----"
				+ mData.get(position).getType());
		System.out.println("mData.get(position).getStatus()-----"
				+ mData.get(position).getStatus());
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(chatName);
		// 如果statu是发送失败，则加载三个惨淡的menu，否则，就加载两个菜单的menu

		builder.setItems(R.array.thread_menu,
				new DialogInterface.OnClickListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Toast.makeText(
						// SingleChatActivity.this,
						// "position=== " + position + " ,which = "
						// + which, Toast.LENGTH_SHORT).show();
						switch (which) {

						case 0:
							if (mData.get(position).getStyle().equals("2")) {
								Toast.makeText(SingleChatActivity.this,
										"The picture could not be copied",
										Toast.LENGTH_SHORT).show();// 图片不能被复制
								return;
							}
							// 复制
							String mContent = mData.get(position).getContent();
							if (android.os.Build.VERSION.SDK_INT > 11) {
								android.content.ClipboardManager c = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
								c.setPrimaryClip(ClipData.newPlainText(
										"stcopy", mContent));
							} else {
								android.text.ClipboardManager c = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
								c.setText("" + mContent);
							}
							Toast.makeText(SingleChatActivity.this,
									"Has been copied", Toast.LENGTH_SHORT)
									.show();// 已复制
							break;
						case 1:
							if (!mData.get(position).getStatus()
									.equals(ConstantUtils.MESSAGE_SEND_ERROR)) {
								Toast.makeText(SingleChatActivity.this,
										"该条消息不需要重新发送", Toast.LENGTH_SHORT)
										.show();
								System.out.println("不需要重新发送，所以 ，返回了");
								return;
							}
							System.out.println("继续执行");
							new Thread(new Runnable() {
								@Override
								public void run() {
									System.out.println("执行线程");
									// 在这里刷新适配器重新修改发送状态数据
									refreshSendStatus(
											""
													+ mData.get(position)
															.getPacketId(),
											ConstantUtils.MESSAGE_SEND_SSSS);
									org.jivesoftware.smack.packet.Message mMessage = new org.jivesoftware.smack.packet.Message();
									mMessage.setFrom(accountName);
									mMessage.setTo(chatName);
									mMessage.setBody(mData.get(position)
											.getContent());
									mMessage.setPacketID(""
											+ mData.get(position).getPacketId());

									boolean isSend = false;
									if (XmppConnectionServer.getInstance()
											.isConn()) {

										// 对该消息要求有回执
										DeliveryReceiptManager
												.addDeliveryReceiptRequest(mMessage);
										isSend = ChatManager.sendMeassage(
												SingleChatActivity.this,
												chatName, mMessage, null);
										Log.e(TAG, "发送状态：---" + isSend);

									} else {
										// 这个地方要通过handler来发送
										// Toast.makeText(SingleChatActivity.this,
										// "You have dropped",
										// Toast.LENGTH_SHORT)
										// .show();
									}

									String sendStatus = "";
									if (isSend) {
										// 发送成功
										sendStatus = ConstantUtils.MESSAGE_SEND_SUCCESS;
									} else {
										// 发送失败
										sendStatus = ConstantUtils.MESSAGE_SEND_ERROR;
									}
									// 修改一条记录的发送接收状态
									ChatMessageDao cmd = new ChatMessageDao();
									long lon = cmd.updateSendStatusByPacketID(
											mData.get(position).getPacketId(),
											sendStatus);
									System.out.println("----修改了第：" + lon
											+ " 条聊天数据的状态----");

									// 在这里刷新适配器重新修改发送状态数据
									refreshSendStatus(
											""
													+ mData.get(position)
															.getPacketId(),
											sendStatus);
									sendStatus = "";
								}
							}).start();

							break;
						case 2:

							// 通过消息的毫秒，删除该条消息
							long mTime = mData.get(position).getTime();
							ChatMessageDao cmd = new ChatMessageDao();
							cmd.delSingleChatOneMessageWithTime("" + mTime);
							mData.remove(position);
							// 发送广播刷新数据

							new Thread(new Runnable() {
								@Override
								public void run() {
									Message message = new Message();
									message.what = RECEIVE_MESSAGE;
									receiveMessageHandler.sendMessage(message);
								}
							}).start();

							break;
						default:

							break;
						}

					}
				});
		builder.show();
		return true;
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		longClicked = true;
		System.out.println("onLongClick长按");
		Toast.makeText(SingleChatActivity.this,
				"This function is not yet open", Toast.LENGTH_SHORT).show();// 该功能尚未开放
		return true;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		// 元事件：action_down , action_move , action_up
		// 这三个元事件组成了我们丰富的各种各样的事件
		// System.out.println("触摸触摸触摸");
		// event 对象记录了事件发生的现场情况
		int actionType = event.getAction();
		if (actionType == MotionEvent.ACTION_DOWN) {
			// System.out.println("touch____按下....");
			if (longClicked) {
				// 快进
				// System.out.println("touch___并且长按了....");
			}
		} else if (actionType == MotionEvent.ACTION_MOVE) {
			// System.out.println("touch____移动....");
			// butt_click3.setX(event.getX());
			// butt_click3.setY(event.getY());
		} else if (actionType == MotionEvent.ACTION_UP) {
			// System.out.println("touch____松开....");
			if (longClicked) {
				System.out.println("touch____并且是长按的松开....");
			}
			longClicked = false;
		} else {
			System.out.println("touch_else");
		}
		return false;
	}

}
