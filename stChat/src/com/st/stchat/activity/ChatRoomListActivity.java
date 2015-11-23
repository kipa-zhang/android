package com.st.stchat.activity;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.AsyncTaskBase;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.adapter.ChatRoomListAdapter;
import com.st.stchat.dao.ChatRoomDao;
import com.st.stchat.listener.ChatRoomMsgListener;
import com.st.stchat.manager.ChatRoomManager;
import com.st.stchat.model.ChatRoom;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.view.DropDownListView;
import com.st.stchat.view.LoadingView;
import com.st.stchat.widget.AlertDialog;
import com.st.stchat.widget.SearchEditText;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 聊天室列表--Activity
 * 
 * @author  
 * 
 */
public class ChatRoomListActivity extends BaseActivity {
	private TextView chatRoomListTitle;
	private ImageButton backBtn;
	private List<HostedRoom> hostRooms;
	private LoadingView mLoadingView;
	private DropDownListView chatRoomList;
	private View headSearchView;
	private SearchEditText searchEditText;
	private List<ChatRoom> rooms = new ArrayList<ChatRoom>();
	private ChatRoomListAdapter chatRoomListAdapter;
	private String accountName = InfoUtils.getUser(STChatApplication
			.getInstance());
	private int initRoomCountFromServer = 3;// 初始加载从服务器获取的房间数量
	private List<ChatRoom> chatRooms = null;
	private RoomDataTask roomDataTask;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatroomlist);
		findView();
		initView();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		rooms.clear();
		roomDataTask = new RoomDataTask(mLoadingView);
		roomDataTask.execute(0);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		roomDataTask.cancel(true);
	}

	@SuppressLint("NewApi")
	private void initView() {
		chatRoomListTitle.setText("" + "Rooms");
		backBtn.setOnClickListener(chatRoomBackListener);
		chatRoomList.setOnItemClickListener(chatRoomListListener);
		chatRoomList.setShowFooterWhenNoMore(true);
		chatRoomList.setOnBottomListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new GetMoreDataTask().execute();
			}
		});

		// 根据关键字查询
		searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});
	}

	private void findView() {
		chatRoomListTitle = (TextView) findViewById(R.id.textViewTitle);
		backBtn = (ImageButton) findViewById(R.id.buttonTitleLeft);
		mLoadingView = (LoadingView) findViewById(R.id.loadingView_chatroomlist);
		chatRoomList = (DropDownListView) findViewById(R.id.chat_room_list);

		LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
		headSearchView = inflater.inflate(R.layout.common_search, null);
		chatRoomList.addHeaderView(headSearchView);

		searchEditText = (SearchEditText) headSearchView
				.findViewById(R.id.search_edit);
	}

	private class RoomDataTask extends AsyncTaskBase {

		public RoomDataTask(LoadingView loadingView) {
			super(loadingView);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			int result = 0;
			hostRooms = ChatRoomManager.getHostRooms();
			if (hostRooms != null) {
				if (hostRooms.size() > 0) {
					// 获取本地存储的房间
					chatRooms = ChatRoomDao.getInstance().getChatRoomByAccount(
							accountName);
					if (chatRooms.size() != 0) {
						rooms.addAll(chatRooms);
					}
					if (hostRooms.size() <= 3) {
						initRoomCountFromServer = hostRooms.size();
					}
					// 获取服务器上的房间
					for (int i = 0; i < initRoomCountFromServer; i++) {
						ChatRoom room = null;
						if (i == 0) {
							room = new ChatRoom("serverTag", accountName,
									hostRooms.get(i).getJid());
						} else {
							room = new ChatRoom("server", accountName,
									hostRooms.get(i).getJid());
						}
						rooms.add(room);
					}
					result = 1;
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == 1) {
				chatRoomListAdapter = new ChatRoomListAdapter(
						ChatRoomListActivity.this, rooms);
				chatRoomList.setAdapter(chatRoomListAdapter);
			}
		}
	}

	/**
	 * 异步加载更多数据
	 * 
	 * @author  
	 * 
	 */
	private class GetMoreDataTask extends AsyncTask<Integer, Integer, Integer> {
		@Override
		protected Integer doInBackground(Integer... params) {
			loadMoreData();
			return chatRoomListAdapter.getCount();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			chatRoomListAdapter.notifyDataSetChanged();
			if (result == chatRooms.size() + hostRooms.size()) {
				chatRoomList.setHasMore(false);
			}
			chatRoomList.onBottomComplete();
		}
	}

	/**
	 * 加载更多数据
	 */
	private void loadMoreData() {
		int count = chatRoomListAdapter.getCount() - chatRooms.size();
		int autoCount = count + initRoomCountFromServer;
		if (autoCount < hostRooms.size()) {
			for (int i = count; i < autoCount; i++) {
				ChatRoom room = new ChatRoom("server", accountName, hostRooms
						.get(i).getJid());
				chatRoomListAdapter.addNewsItem(room);
			}
		} else {
			for (int i = count; i < hostRooms.size(); i++) {
				ChatRoom room = new ChatRoom("server", accountName, hostRooms
						.get(i).getJid());
				chatRoomListAdapter.addNewsItem(room);
			}
		}
	}

	/**
	 * list 监听
	 */
	private OnItemClickListener chatRoomListListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {
			String roomJid = "";
			if (!XmppConnectionServer.getInstance().isConn()) {
				Toast.makeText(ChatRoomListActivity.this, "You have dropped",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (position > 0) {
				// roomJid = rooms.get(position - 1).getRoomJid();
				roomJid = (((ChatRoom) chatRoomListAdapter
						.getItem(position - 1)).getRoomJid());
				// 加入聊天室的方法
				MultiUserChat multiUserChat = ChatRoomManager
						.joinMultiUserChat(XmppConnectionServer.getInstance()
								.getConnection().getUser().toString()
								.split("@")[0], roomJid);

				if (multiUserChat != null) {
					// 允许进入此房间

					// 1.查询本地是否存储该房间，如没有存储即可存储
					int result = ChatRoomDao.getInstance().findRoom(
							accountName, roomJid);
					if (result <= 0) {
						System.out.println("ChatRoomListActivity: 存入房间到数据库"
								+ roomJid);
						ChatRoomDao.getInstance().saveChatRoom(accountName,
								roomJid);
						// 给该房间添加监听
						ChatRoomMsgListener chatRoomMsgListener = new ChatRoomMsgListener();
						multiUserChat.addMessageListener(chatRoomMsgListener);
					}

					Toast.makeText(ChatRoomListActivity.this, roomJid,
							Toast.LENGTH_SHORT).show();
					String[] to = new String[] { "roomJid", roomJid };
					Intent intent = new Intent(ChatRoomListActivity.this,
							GroupChatActivity.class);
					intent.putExtra("give_chatroom", to);
					startActivity(intent);
					finish();
				} else {
					// 进入房间失败
					new AlertDialog(ChatRoomListActivity.this).builder()
							.setMsg("Not members cannot enter the room")
							.setNegativeButton("Commit", new OnClickListener() {
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
								}
							}).show();
				}
			}
		}
	};

	/**
	 * back 监听
	 */
	private OnClickListener chatRoomBackListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			finish();
		}
	};

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		boolean catalogTag = false;
		List<ChatRoom> list = new ArrayList<ChatRoom>();
		if (TextUtils.isEmpty(filterStr)) {
			list = rooms;
			catalogTag = false;
			chatRoomList.addFooterView();
		} else {
			list.clear();
			catalogTag = true;
			chatRoomList.removeFooterView();
			for (ChatRoom chatRoom : rooms) {
				String roomName = chatRoom.getRoomJid().split("@")[0];
				if (roomName.indexOf(filterStr.toString()) != -1) {
					list.add(chatRoom);
				}
			}
		}
		// 过滤重复的数据
		if (!TextUtils.isEmpty(filterStr) && list.size() != 1) {
			for (int i = 0; i < list.size() - 1; i++) {
				for (int j = list.size() - 1; j > i; j--) {
					if (list.get(j).getRoomJid()
							.equals(list.get(i).getRoomJid())
							&& list.get(i).getRoomJid().length() != 0) {
						list.remove(j);
					}
				}
			}
		}
		chatRoomListAdapter.updateListView(list, catalogTag);
	}
}
