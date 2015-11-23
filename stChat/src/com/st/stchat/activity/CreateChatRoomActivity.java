package com.st.stchat.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.BaseAsyncTask;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.dao.ChatRoomDao;
import com.st.stchat.dao.ContactDao;
import com.st.stchat.listener.ChatRoomMsgListener;
import com.st.stchat.manager.ChatRoomManager;
import com.st.stchat.model.Contact;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.widget.AlertDialog;
import com.st.stchat.widget.CharacterParser;
import com.st.stchat.widget.CreateRoomSortAdapter;
import com.st.stchat.widget.CreateRoomSortAdapter.ViewHolder;
import com.st.stchat.widget.PinyinComparator;
import com.st.stchat.widget.SideBar;
import com.st.stchat.widget.SortModel;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 创建聊天室 页面
 * 
 * @author  
 * 
 */

public class CreateChatRoomActivity extends BaseActivity implements
		BaseAsyncTaskListener {
	private Button createBtn;
	private TextView addChatRoomTitle, dialog;
	private SideBar sideBar;
	private CreateRoomSortAdapter adapter;
	private ImageButton backBtn;
	private ListView myFriendsList;
	private List<Map<String, SortModel>> SourceDateList;
	private String accountName;
	private String roomTitle;
	private BaseAsyncTask task;
	private static final int CREATEROOM = 8;

	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private List<SortModel> SortModelList;

	private List<String> listData;
	private List<String> userJidList;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_createchatroom);
		accountName = InfoUtils.getUser(STChatApplication.getInstance());
		Intent intent = getIntent();
		String[] intentStrArr = intent
				.getStringArrayExtra("send_CreateChatRoomActivity");
		roomTitle = intentStrArr[1];

		findView();
		initView();
		initListener();
	}

	private void findView() {
		createBtn = (Button) findViewById(R.id.buttonTitleRight);

		addChatRoomTitle = (TextView) findViewById(R.id.textViewTitle);
		backBtn = (ImageButton) findViewById(R.id.buttonTitleLeft);
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		myFriendsList = (ListView) findViewById(R.id.allFriendsList);
	}

	private void initView() {
		createBtn.setText("Save");
		createBtn.setBackgroundResource(R.drawable.title_selector);
		createBtn.setTextSize(16);
		addChatRoomTitle.setText("" + "Invite");
		createBtn.setVisibility(View.VISIBLE);

		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();

		sideBar.setTextView(dialog);

		// 初始化相关数据
		initData();

		adapter = new CreateRoomSortAdapter(mContext, SourceDateList);
		myFriendsList.setAdapter(adapter);
	}

	public void initData() {
		listData = new ArrayList<String>();
		userJidList = new ArrayList<String>();
		SortModelList = new ArrayList<SortModel>();
		SourceDateList = new ArrayList<Map<String, SortModel>>();
		List<Contact> contactList = ContactDao.getInstance()
				.getContactByAccount(accountName);
		for (int i = 0; i < contactList.size(); i++) {
			listData.add(contactList.get(i).getContactName());
		}

		String[] names = new String[] {};
		names = listData.toArray(names);
		SortModelList = filledData(listData.toArray(names));
		// 根据a-z进行排序源数据
		Collections.sort(SortModelList, pinyinComparator);
		for (int i = 0; i < SortModelList.size(); i++) {
			Map<String, SortModel> map = new HashMap<String, SortModel>();
			map.put("SM", SortModelList.get(i));
			SourceDateList.add(map);
		}
	}

	private void initListener() {
		createBtn.setOnClickListener(createRoomListener);
		backBtn.setOnClickListener(createRoomBackListener);
		myFriendsList.setOnItemClickListener(itemClickListener);
	}

	/**
	 * list 勾选监听
	 */
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ViewHolder vHollder = (ViewHolder) view.getTag();
			vHollder.cBox.toggle();
			CreateRoomSortAdapter.isSelected.put(position,
					vHollder.cBox.isChecked());
			if (vHollder.cBox.isChecked()) {
				userJidList.add(SourceDateList.get(position).get("SM")
						.getName());
			} else {
				userJidList.remove(SourceDateList.get(position).get("SM")
						.getName());
			}
		}
	};

	/**
	 * 创建 room 监听
	 */
	private OnClickListener createRoomListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (!XmppConnectionServer.getInstance().isConn()) {
				Toast.makeText(CreateChatRoomActivity.this, "You have dropped",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (userJidList.size() <= 0) {
				Toast.makeText(CreateChatRoomActivity.this,
						"Please check the user", Toast.LENGTH_SHORT).show();
				return;
			}

			if (userJidList.size() < 2) {
				Toast.makeText(CreateChatRoomActivity.this,
						"The number of members is greater than one",
						Toast.LENGTH_SHORT).show();
				return;
			}

			task = new BaseAsyncTask(CreateChatRoomActivity.this,
					CreateChatRoomActivity.this, CREATEROOM);
			task.setDialogMessage("Loading...");
			task.execute();
		}
	};

	/**
	 * back 监听
	 */
	private OnClickListener createRoomBackListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			finish();
		}
	};

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private List<SortModel> filledData(String[] date) {
		List<SortModel> mSortList = new ArrayList<SortModel>();

		for (int i = 0; i < date.length; i++) {
			SortModel sortModel = new SortModel();
			sortModel.setName(date[i]);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void doAsyncTaskBefore(int taskId) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object doAsyncTaskIn(int taskId, Object... params) {
		boolean bool = false;
		// 创建聊天室
		MultiUserChat room = ChatRoomManager.createRoom("", roomTitle, "");

		// 邀请选中的好友进入聊天室 成为该聊天室的成员
		for (int i = 0; i < userJidList.size(); i++) {
			try {
				ChatRoomManager.inviteChatRoom(roomTitle, userJidList.get(i));
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// 添加聊天室监听
		if (room != null) {
			ChatRoomMsgListener chatRoomMsgListener = new ChatRoomMsgListener();
			room.addMessageListener(chatRoomMsgListener);

			// 将该聊天室存入本地数据库
			String roomJid = roomTitle.toLowerCase()
					+ "@conference."
					+ XmppConnectionServer.getInstance().getConnection()
							.getServiceName();
			ChatRoomDao.getInstance().saveChatRoom(accountName, roomJid);

			bool = true;
		}

		return bool;
	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {
		// TODO Auto-generated method stub
		boolean bool = (Boolean) result;
		if (bool) {
			// 完成之后 跳转到 群聊的页面 进行聊天
			String roomJid = roomTitle.toLowerCase()
					+ "@conference."
					+ XmppConnectionServer.getInstance().getConnection()
							.getServiceName();

			Toast.makeText(CreateChatRoomActivity.this, roomJid,
					Toast.LENGTH_SHORT).show();
			String[] to = new String[] { "roomJid", roomJid };
			Intent intent = new Intent(CreateChatRoomActivity.this,
					GroupChatActivity.class);
			intent.putExtra("give_chatroom", to);
			startActivity(intent);

			// 发送广播 finish掉之前的activity
			sendBroadcast(new Intent("FinishCreateChatRoomGetNameActivity"));

			finish();
		} else {
			// 不可重复创建房间
			new AlertDialog(CreateChatRoomActivity.this).builder()
					.setMsg("The room has been in existence can't create")
					.setNegativeButton("Cancel", new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

						}
					}).show();
		}
	}

	@Override
	public void doCancelled(int taskId) {
		// TODO Auto-generated method stub

	}
}
