package com.st.stchat.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.XMPPException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.AsyncTaskBase;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.activity.AddFriendsActivity;
import com.st.stchat.activity.ChatRoomListActivity;
import com.st.stchat.activity.NewFriendActivity;
import com.st.stchat.activity.SingleChatActivity;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.dao.ContactDao;
import com.st.stchat.dao.NoticeDao;
import com.st.stchat.manager.ContactManager;
import com.st.stchat.model.Contact;
import com.st.stchat.model.Notice;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.view.LoadingView;
import com.st.stchat.widget.AlertDialog;
import com.st.stchat.widget.CharacterParser;
import com.st.stchat.widget.PinyinComparator;
import com.st.stchat.widget.SideBar;
import com.st.stchat.widget.SideBar.OnTouchingLetterChangedListener;
import com.st.stchat.widget.SortAdapter;
import com.st.stchat.widget.SortModel;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 联系人页面
 * 
 * @author  
 * 
 */
public class ContactFragment extends BaseFragment implements
		BaseAsyncTaskListener, OnClickListener {
	private static final String TAG = "ContactFragment";
	// private static final int DELETE_CONTACT = 5;
	protected static final int REFRESH_CONTACT_LIST = 0;
	private boolean receiverFlag = true;
	private boolean asyncTaskFlag = false;// 判断广播注册标识
	private Context mContext;
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog, unread_msg_contact;
	private SortAdapter adapter;
	// private View mChatroomList;
	private LinearLayout mHeaderLayout;
	private RelativeLayout mChatroomLayout, mNewUserLayout;
	private CharacterParser characterParser;// 汉字转换成拼音的类
	private List<SortModel> SourceDateList = new ArrayList<SortModel>();
	private PinyinComparator pinyinComparator;// 根据拼音来排列ListView里面的数据类
	private List<String> listData;
	private String accountName;
	private String delNameJid;
	private boolean delResultFromServer;
	private List<Contact> contactList;
	private ContacterReceiver receiver;
	// private BaseAsyncTask task;
	private GetDataTask getDataTask;
	private LoadingView mLoadingView;
	private IntentFilter filter;
	private ImageButton buttonBottomLeft, buttonBottomRight;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mContext = getActivity();
		View rootView = inflater.inflate(R.layout.fragment_contact, container,
				false);
		// mChatroomList = inflater.inflate(R.layout.fragment_contact_head,
		// null);
		accountName = InfoUtils.getUser(STChatApplication.getInstance());
		// 加载Widget
		initWidgets(rootView);
		initListener();
		buttonBottomLeft.setImageResource(R.drawable.sousuo_bai);
		buttonBottomRight.setImageResource(R.drawable.tianjia_bai);
		return rootView;
	}

	@Override
	public void onStart() {

		super.onStart();
		// 初始化广播 注册广播接收器
		receiver = new ContacterReceiver();
		filter = new IntentFilter();
		filter.addAction(ConstantUtils.ROSTER_DELETED_ACTION);
		filter.addAction(ConstantUtils.ROSTER_UPDATED_ACTION);
		filter.addAction(ConstantUtils.ROSTER_SUBSCRIPTION_ACTION);// 好友请求
	}

	@Override
	public void onResume() {
		super.onResume();
		SourceDateList.clear();
		// 刷新气泡
		initUnreadMsgSum();
		getDataTask = new GetDataTask(mLoadingView);
		getDataTask.execute(0);
	}

	// 初始化按钮部件
	private void initWidgets(View view) {
		sideBar = (SideBar) view.findViewById(R.id.sidrbar);
		dialog = (TextView) view.findViewById(R.id.dialog);
		sortListView = (ListView) view.findViewById(R.id.contact_list);
		// sortListView.addHeaderView(mChatroomList);

		mHeaderLayout = (LinearLayout) view.findViewById(R.id.contact_header);

		mChatroomLayout = (RelativeLayout) mHeaderLayout
				.findViewById(R.id.chatroomlayout);
		mNewUserLayout = (RelativeLayout) mHeaderLayout
				.findViewById(R.id.newuserlayout);
		unread_msg_contact = (TextView) mHeaderLayout
				.findViewById(R.id.unread_msg_contact);
		unread_msg_contact.setVisibility(View.GONE);

		mLoadingView = (LoadingView) view.findViewById(R.id.loading);

		buttonBottomLeft = (ImageButton) view
				.findViewById(R.id.buttonBottomLeft);
		buttonBottomRight = (ImageButton) view
				.findViewById(R.id.buttonBottomRight);

		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();

		sideBar.setTextView(dialog);

	}

	private void initListener() {
		// 新联系人监听
		mNewUserLayout.setOnClickListener(new OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(getActivity(), NewFriendActivity.class);
				startActivity(intent);
			}
		});

		// 聊天室监听
		mChatroomLayout.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {

				Toast.makeText(getActivity(), "chatroom", Toast.LENGTH_SHORT)
						.show();
				Intent intent = new Intent();
				intent.setClass(getActivity(), ChatRoomListActivity.class);
				startActivity(intent);
			}
		});

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(touchingLetterChangedListener);

		// 联系人list点击监听
		sortListView.setOnItemClickListener(onItemClickListener);

		// 联系人list长按监听
		sortListView.setOnItemLongClickListener(onItemLongClickListener);

		buttonBottomLeft.setOnClickListener(this);
		buttonBottomRight.setOnClickListener(this);

		buttonBottomLeft.setVisibility(View.INVISIBLE);
	}

	/**
	 * 异步加载初始数据 并且初始化广播监听器
	 * 
	 * @author  
	 * 
	 */
	private class GetDataTask extends AsyncTaskBase {

		public GetDataTask(LoadingView loadingView) {
			super(loadingView);

		}

		@Override
		protected Integer doInBackground(Integer... params) {

			asyncTaskFlag = true;
			int result = 0;
			initData();
			if (SourceDateList != null) {
				if (SourceDateList.size() > 0) {
					result = 1;
				}
			}
			return result;
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Integer result) {

			super.onPostExecute(result);
			if (result == 0) {
				sortListView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			} else {
				sortListView.getLayoutParams().height = LayoutParams.FILL_PARENT;
			}
			adapter = new SortAdapter(mContext, SourceDateList);
			sortListView.setAdapter(adapter);

			if (receiver != null && filter != null) {
				ContactFragment.this.getActivity().registerReceiver(receiver,
						filter);
				receiverFlag = false;
			}

		}
	}

	private void initData() {
		listData = new ArrayList<String>();
		contactList = ContactDao.getInstance().getContactByAccount(accountName);
		for (int i = 0; i < contactList.size(); i++) {
			listData.add(contactList.get(i).getContactName());
		}

		String[] names = new String[] {};
		names = listData.toArray(names);
		SourceDateList = filledData(names);

		// 根据a-z进行排序源数据
		Collections.sort(SourceDateList, pinyinComparator);
	}

	/**
	 * 长按list监听
	 */
	private OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {

		@SuppressLint("NewApi")
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			delNameJid = ((SortModel) adapter.getItem(position)).getName();
			new AlertDialog(getActivity()).builderPick()
					.setTitlePick(delNameJid.split("@")[0])
					.setCancleBttonPick("Cancel", new OnClickListener() {
						@Override
						public void onClick(View v) {

						}
					}).setDeleteBttonPick("Delete", new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!XmppConnectionServer.getInstance().isConn()) {
								Toast.makeText(getActivity(),
										"You have dropped", Toast.LENGTH_SHORT)
										.show();
								return;
							}
							// 删除用户
							try {
								String delName = delNameJid.split("@")[0];
								boolean result = ContactManager
										.deleteUser(delName);// 删除服务上的花名册
								if (!result) {
									deleteDBContact(delName);
								}
							} catch (XMPPException e) {
								e.printStackTrace();
							}
						}
					}).show();
			return true;
		}

	};

	/**
	 * 点击list监听
	 */
	private OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@SuppressLint("NewApi")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
			Toast.makeText(getActivity(),
					((SortModel) adapter.getItem(position)).getName(),
					Toast.LENGTH_SHORT).show();
			String[] to = new String[] { "contact",
					((SortModel) adapter.getItem(position)).getName() };
			Intent intent = new Intent(getActivity(), SingleChatActivity.class);
			intent.putExtra("give_chat", to);
			getActivity().startActivity(intent);
		}
	};

	/**
	 * 右侧触摸监听
	 */
	private OnTouchingLetterChangedListener touchingLetterChangedListener = new OnTouchingLetterChangedListener() {
		@Override
		public void onTouchingLetterChanged(String s) {
			// 该字母首次出现的位置
			int position = adapter.getPositionForSection(s.charAt(0));
			if (position != -1) {
				sortListView.setSelection(position);
			}
		}
	};

	/**
	 * 删除本地以及缓存中联系人
	 * 
	 * @param delName
	 */
	private void deleteDBContact(String delName) {
		// 清除本地数据库
		ChatMessageDao chatMessageDao = new ChatMessageDao();
		chatMessageDao.delChatWithSb(delName);
		NoticeDao.getInstance().delContactReq(delNameJid);
		ContactDao.getInstance().delContact(accountName, delNameJid);
		// 清除该用户缓存
		STChatApplication.messageCache.remove(delName);

		// 发送广播
		Intent intent = new Intent();
		intent.setAction(ConstantUtils.CONTACT_DELETED_ACTION);
		STChatApplication.getInstance().sendBroadcast(intent);

		String[] names = new String[] {};
		listData.remove(delNameJid);
		names = listData.toArray(names);
		SourceDateList = filledData(names);
		// 根据a-z进行排序源数据
		Collections.sort(SourceDateList, pinyinComparator);
		adapter.updateListView(SourceDateList);
		// 刷新气泡
		initUnreadMsgSum();
	}

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

	// 联系人广播
	private class ContacterReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			Log.i(TAG, "接受到联系人广播，action : " + action);
			if (action.equals(ConstantUtils.ROSTER_DELETED_ACTION)) {
				delResultFromServer = intent.getBooleanExtra("deleteResult",
						false);
				if (delResultFromServer) {
					String[] names = new String[] {};
					listData.remove(intent.getStringExtra("deleteName"));
					names = listData.toArray(names);
					SourceDateList = filledData(names);
					// 根据a-z进行排序源数据
					Collections.sort(SourceDateList, pinyinComparator);
					adapter.updateListView(SourceDateList);
					// 刷新气泡
					initUnreadMsgSum();
				} else {

					Toast.makeText(mContext, "Delete failed, please try again",
							Toast.LENGTH_LONG).show();
				}
				// task = new BaseAsyncTask(mContext,
				// ContactFragment.this, DELETE_CONTACT);
				// task.setDialogMessage("Deleting...");
				// task.execute();
			}

			if (action.equals(ConstantUtils.ROSTER_UPDATED_ACTION)) {
				String[] names = new String[] {};
				listData.add(intent.getStringExtra("updateName"));
				names = listData.toArray(names);
				SourceDateList = filledData(names);
				// 根据a-z进行排序源数据
				Collections.sort(SourceDateList, pinyinComparator);
				adapter.updateListView(SourceDateList);
			}

			if (action.equals(ConstantUtils.ROSTER_SUBSCRIPTION_ACTION)) {
				unread_msg_contact.setVisibility(View.VISIBLE);
				Integer countAdd = NoticeDao.getInstance()
						.getUnReadNoticeCountByType(Notice.ADD_FRIEND);
				unread_msg_contact.setText("" + countAdd + "");
			}
		}
	}

	/**
	 * 初始化未处理联系人请求数量
	 */
	private void initUnreadMsgSum() {
		Integer countAdd = NoticeDao.getInstance().getUnReadNoticeCountByType(
				Notice.ADD_FRIEND);
		if (countAdd > 0) {
			unread_msg_contact.setVisibility(View.VISIBLE);
			unread_msg_contact.setText("" + countAdd + "");
		} else {
			unread_msg_contact.setVisibility(View.GONE);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onPause() {

		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (asyncTaskFlag) {
			getDataTask.cancel(true);
		}
		// 卸载广播接收器
		if (receiver != null && filter != null) {
			if (receiverFlag) {
				ContactFragment.this.getActivity().registerReceiver(receiver,
						filter);
			}
			ContactFragment.this.getActivity().unregisterReceiver(receiver);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {

		super.onDetach();
	}

	/**
	 * 异步删除联系人搁置不用 备留着代码  
	 */
	@Override
	public void doAsyncTaskBefore(int taskId) {

	}

	@Override
	public Object doAsyncTaskIn(int taskId, Object... params) {
		// boolean bool = false;

		// 睡一会 等待监听服务删除返回的结果 delResultFromServer
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// Log.i(TAG, "----向服务器删除成功? ----" + delResultFromServer);
		// if (delResultFromServer) {
		// ChatMessageDao chatMessageDao = new ChatMessageDao();
		// int delChatMessage = chatMessageDao.delChatWithSb(delName
		// .split("@")[0]);
		// NoticeDao noticeDao = new NoticeDao();
		// int delNotice = noticeDao.delContactReq(delName);
		// int delContact = ContactDao.getInstance().delContact(accountName,
		// delName);
		// if (delChatMessage >= 0 && delNotice >= 0 && delContact >= 0) {
		// bool = true;
		// }
		// }
		return null;
	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {

		// boolean bool = (Boolean) result;
		// if (bool) {
		// new GetDataTask(mLoadingView).execute(0);
		// } else {
		// Toast.makeText(mContext, "Delete failed, please try again",
		// Toast.LENGTH_LONG).show();
		// }
	}

	@Override
	public void doCancelled(int taskId) {

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.buttonBottomLeft:
			// 搜索
			// buttonBottomLeft.setImageResource(R.drawable.sousuo_lan);
			Toast.makeText(getActivity(), "This function is not yet open",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.buttonBottomRight:
			// 添加联系人
			// buttonBottomRight.setImageResource(R.drawable.tianjia_lan);
			startActivity(new Intent(getActivity(), AddFriendsActivity.class));
			break;
		default:
			break;
		}
	}

}
