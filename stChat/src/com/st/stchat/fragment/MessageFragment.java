package com.st.stchat.fragment;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.AsyncBitmapLoader;
import com.st.stchat.AsyncBitmapLoader.ImageCallBack;
import com.st.stchat.AsyncTaskBase;
import com.st.stchat.BaseAsyncTask;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.activity.CreateChatRoomGetNameActivity;
import com.st.stchat.activity.GroupChatActivity;
import com.st.stchat.activity.SingleChatActivity;
import com.st.stchat.bean.LastChatMsg;
import com.st.stchat.bean.MessageItem;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.dao.ChatRoomMessageDao;
import com.st.stchat.dao.ContactDao;
import com.st.stchat.model.CombineBitmap;
import com.st.stchat.slideview.ListViewCompat;
import com.st.stchat.slideview.SlideView;
import com.st.stchat.slideview.SlideView.OnSlideListener;
import com.st.stchat.utils.CombineBitmapUtil;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.DateUtil;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.Logger;
import com.st.stchat.utils.PropertiesUtil;
import com.st.stchat.utils.StringUtil;
import com.st.stchat.utils.TimeUtil;
import com.st.stchat.view.LoadingView;
import com.st.stchat.widget.AlertDialog;
import com.st.stchat.widget.CircularImageView;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 消息列表 显示最后一条聊天信息
 * 
 * @author  
 * 
 */
public class MessageFragment extends BaseFragment implements OnClickListener,
		OnItemClickListener, OnSlideListener, BaseAsyncTaskListener {
	private static final String TAG = "MessageFragment";
	private static final int DELETE_MESSAGE = 10;
	private ListViewCompat mListView;
	public List<MessageItem> mMessageItems = new ArrayList<MessageItem>();
	private SlideView mLastSlideViewWithStatusOn; // 上次处于打开状态的SlideView
	private String currentUser;
	private String msgTitle;
	private RefreshMessageList refreshMessageList;
	protected static final int REFRESH_MESSAGE_LIST = 0;
	private SlideAdapter slideAdapter;
	private LoadingView mLoadingView;
	private Context mContext;
	private IntentFilter filter;
	private BaseAsyncTask task;
	private GetDataTask getDataTask;// 异步加载类
	private boolean receiverFlag = true;// 判断广播注册标识
	private boolean asyncTaskFlag = false;// 判断广播注册标识
	private ChatMessageDao chatMessageDao;// 单人聊天dao
	private ChatRoomMessageDao roomMessageDao;// 聊天室dao
	List<CombineBitmap> combineBitmaps = null;// 存放聊天室合成的头像
	private AsyncBitmapLoader asyncLoader;
	private ImageButton buttonBottomLeft, buttonBottomRight;
	private Handler RefreshListHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_MESSAGE_LIST:
				refreshView();
				break;
			default:
				break;
			}
		}
	};

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
		View view = inflater.inflate(R.layout.fragment_message, container,
				false);
		mContext = getActivity();
		// 加载Widget
		initView(view);
		initListener();
		slideAdapter = new SlideAdapter();

		buttonBottomLeft.setImageResource(R.drawable.sousuo_bai);
		buttonBottomRight.setImageResource(R.drawable.qunliao_bai);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		refreshMessageList = new RefreshMessageList();
		filter = new IntentFilter();
		filter.addAction(ConstantUtils.SINGLE_CHAT_ACTION);
		filter.addAction(ConstantUtils.SINGLE_CHAT_PIC_ACTION);
		filter.addAction(ConstantUtils.GROUP_CHAT_ACTION);
		filter.addAction(ConstantUtils.ROSTER_DELETED_ACTION);// 好友删除
	}

	@Override
	public void onResume() {
		super.onResume();
		mMessageItems.clear();
		// 判断从哪加载数据本地数据库or内存
		if (STChatApplication.messageCache.size() == 0
				|| STChatApplication.firstLoadMessageTag) {
			getDataTask = new GetDataTask(mLoadingView);
			getDataTask.execute(0);
		} else {
			if (refreshMessageList != null && filter != null) {
				MessageFragment.this.getActivity().registerReceiver(
						refreshMessageList, filter);
				receiverFlag = false;
			}
			for (String key : STChatApplication.messageCache.keySet()) {
				mMessageItems.add(STChatApplication.messageCache.get(key));
			}
			messageListSort();// 按照时间降序排列
			mListView.setAdapter(slideAdapter);
		}
	}

	// 初始化按钮部件
	@SuppressLint("NewApi")
	private void initView(View view) {
		mListView = (ListViewCompat) view.findViewById(R.id.list);
		mLoadingView = (LoadingView) view.findViewById(R.id.msgfgmloading);
		buttonBottomLeft = (ImageButton) view
				.findViewById(R.id.buttonBottomLeft);
		buttonBottomRight = (ImageButton) view
				.findViewById(R.id.buttonBottomRight);

		currentUser = InfoUtils.getUser(this.getActivity());
	}

	@SuppressLint("NewApi")
	private void initListener() {
		mListView.setOnItemClickListener(this);
		buttonBottomLeft.setOnClickListener(this);
		buttonBottomRight.setOnClickListener(this);
		buttonBottomLeft.setVisibility(View.INVISIBLE);
	}

	/**
	 * 异步加载初始数据 初始化广播监听
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

			STChatApplication.firstLoadMessageTag = false;
			asyncTaskFlag = true;
			int result = 0;
			initData();
			if (mMessageItems != null) {
				if (mMessageItems.size() > 0) {
					result = 1;
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {

			super.onPostExecute(result);
			if (result == 0) {
				mListView.setVisibility(View.GONE);
			} else {
				mListView.setVisibility(View.VISIBLE);
				messageListSort();// 按照时间降序排列
			}
			mListView.setAdapter(slideAdapter);
			if (refreshMessageList != null && filter != null) {
				MessageFragment.this.getActivity().registerReceiver(
						refreshMessageList, filter);
				receiverFlag = false;
			}
		}
	}

	public void initData() {
		Logger.d(TAG, "messageFragment: Init list data");
		// 初始化单人聊天最后一条记录
		chatMessageDao = new ChatMessageDao();
		List<LastChatMsg> last = chatMessageDao.getRecentContactsWithLastMsg();
		for (int i = 0; i < last.size(); i++) {
			MessageItem item = new MessageItem();
			item.setIconRes(R.drawable.head_two);
			item.setMsg(last.get(i).getContent());
			Date date = new Date(Long.parseLong(last.get(i).getNoticeTime()));
			item.setTime(DateUtil.date2Str(date));

			// juwei
			// 使用这个方法(可代替上面一行代码)，可以将日期转换为昨天前天以及很早的时间，如果是今天就只显示时间，但是使用了会报错，因为其他地方调用了该日期，所以如果这里的日期格式不对，其他地方会报空指针异常
			// item.time =
			// TimeUtil.parseDate(TimeUtil.millisToData(1,Long.valueOf(last.get(i).getNoticeTime())
			// .longValue()));

			item.setUnReadSum(Integer.parseInt(last.get(i).getNoticeSum()));

			if (last.get(i).getFrom().equalsIgnoreCase(currentUser)) {
				item.setTitle(last.get(i).getTo());
				STChatApplication.messageCache.put(last.get(i).getTo(), item);
			} else {
				item.setTitle(last.get(i).getFrom());
				STChatApplication.messageCache.put(last.get(i).getFrom(), item);
			}
		}

		// 初始化群聊的最后一条消息
		roomMessageDao = new ChatRoomMessageDao();
		List<LastChatMsg> roomList = roomMessageDao.getRecentRoomMsg();
		for (int i = 0; i < roomList.size(); i++) {
			MessageItem item = new MessageItem();
			item.setIconRes(R.drawable.default_nor_room);
			item.setMsg(roomList.get(i).getContent());
			item.setTitle(roomList.get(i).getFrom());
			Date date = new Date(
					Long.parseLong(roomList.get(i).getNoticeTime()));
			item.setTime(DateUtil.date2Str(date));
			item.setUnReadSum(Integer.parseInt(roomList.get(i).getNoticeSum()));

			STChatApplication.messageCache.put(roomList.get(i).getFrom(), item);
		}

		for (String key : STChatApplication.messageCache.keySet()) {
			mMessageItems.add(STChatApplication.messageCache.get(key));
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonBottomLeft:
			// 搜索
			Toast.makeText(getActivity(), "This function is not yet open",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.buttonBottomRight:
			// 群聊
			startActivity(new Intent(getActivity(),
					CreateChatRoomGetNameActivity.class));
			break;
		case R.id.holder:
			// 在这里删除和当前联系人的所有聊天记录
			int position = mListView.getPositionForView(v);
			msgTitle = ((MessageItem) slideAdapter.getItem(position))
					.getTitle();
			new AlertDialog(getActivity())
					.builderPick()
					.setTitlePick(msgTitle.split("@")[0])
					.setCancleBttonPick("Cancel", new OnClickListener() {
						@Override
						public void onClick(View v) {

						}
					})
					.setDeleteBttonPick("Delete conversation",
							new OnClickListener() {
								@Override
								public void onClick(View v) {

									task = new BaseAsyncTask(mContext,
											MessageFragment.this,
											DELETE_MESSAGE);
									task.setDialogMessage("Deleting...");
									task.execute();
								}
							}).show();

			mLastSlideViewWithStatusOn.shrink(); // 关闭SlideView
			Logger.e(TAG, "onClick v=" + v + ", title=" + msgTitle);
			break;
		default:
			break;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.e(TAG, "onItemClick position=" + position);
		// 判断是单人聊天 还是群聊
		final String title = ((MessageItem) slideAdapter.getItem(position))
				.getTitle();
		if (title.contains("conference")) {
			// 群聊
			Toast.makeText(getActivity(),
					((MessageItem) slideAdapter.getItem(position)).getTitle(),
					Toast.LENGTH_SHORT).show();
			STChatApplication.messageCache.get(title).setUnReadSum(0);// 标示已读
			String[] to = new String[] { "roomJid", title };
			Intent intent = new Intent(getActivity(), GroupChatActivity.class);
			intent.putExtra("give_chatroom", to);
			getActivity().startActivity(intent);
		} else {
			// 单聊
			// 根据title取出完整的JID
			String jid = ContactDao.getInstance().getContactJid(currentUser,
					title);
			if (jid.length() == 0) {
				new AlertDialog(getActivity()).builder()
						.setMsg("This contact has been deleted")
						.setPositiveButton("Commit", new OnClickListener() {
							@Override
							public void onClick(View v) {

								String jid = title
										+ "@"
										+ XmppConnectionServer.getInstance()
												.getConnection()
												.getServiceName();
								STChatApplication.messageCache.get(title)
										.setUnReadSum(0);// 标示已读
								Toast.makeText(getActivity(), jid,
										Toast.LENGTH_SHORT).show();
								String[] to = new String[] { "contact", jid };
								Intent intent = new Intent(getActivity(),
										SingleChatActivity.class);
								intent.putExtra("give_chat", to);
								getActivity().startActivity(intent);
							}
						}).show();
			} else {
				STChatApplication.messageCache.get(title).setUnReadSum(0);// 标示已读
				Toast.makeText(getActivity(), jid, Toast.LENGTH_SHORT).show();
				String[] to = new String[] { "contact", jid };
				Intent intent = new Intent(getActivity(),
						SingleChatActivity.class);
				intent.putExtra("give_chat", to);
				getActivity().startActivity(intent);
			}
		}
	}

	@Override
	public void onSlide(View view, int status) {
		// 如果当前存在已经打开的SlideView，那么将其关闭
		if (mLastSlideViewWithStatusOn != null
				&& mLastSlideViewWithStatusOn != view) {
			mLastSlideViewWithStatusOn.shrink();
		}
		// 记录本次处于打开状态的view
		if (status == SLIDE_STATUS_ON) {
			mLastSlideViewWithStatusOn = (SlideView) view;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		// 取消异步线程
		if (asyncTaskFlag) {
			getDataTask.cancel(true);
		}
		// 卸载单人多人聊天消息，好友变化广播接收器
		if (refreshMessageList != null && filter != null) {
			if (receiverFlag) {
				MessageFragment.this.getActivity().registerReceiver(
						refreshMessageList, filter);
			}
			MessageFragment.this.getActivity().unregisterReceiver(
					refreshMessageList);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@SuppressLint("NewApi")
	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private class SlideAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		@SuppressLint("NewApi")
		SlideAdapter() {
			super();
			asyncLoader = new AsyncBitmapLoader();
			mInflater = getActivity().getLayoutInflater();
		}

		@Override
		public int getCount() {
			return mMessageItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mMessageItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			SlideView slideView = (SlideView) convertView;
			if (slideView == null) {
				View itemView = mInflater.inflate(
						R.layout.fragment_message_item, null);
				slideView = new SlideView(MessageFragment.this.getActivity());
				slideView.setContentView(itemView);
				holder = new ViewHolder(slideView);
				slideView.setOnSlideListener(MessageFragment.this);
				slideView.setTag(holder);
			} else {
				holder = (ViewHolder) slideView.getTag();
			}
			final MessageItem item = mMessageItems.get(position);
			item.setSlideView(slideView);
			item.getSlideView().shrink();

			Bitmap bitmap = asyncLoader.loadBitmap(holder.icon,
					item.getTitle(), new ImageCallBack() {
						@Override
						public void imageLoad(ImageView imageView, Bitmap bitmap) {

							if (bitmap != null) {
								imageView.setImageBitmap(bitmap);
							} else {
								// 判断默认显示的是群聊的还是单人聊的
								if (item.getTitle().contains("conference")) {
									Bitmap combineBitmap = getRoomIcon(item
											.getTitle());
									if (combineBitmap == null) {
										imageView
												.setImageResource(R.drawable.contact_chatroom);
									} else {
										imageView.setImageBitmap(combineBitmap);
									}
								} else {
									imageView
											.setImageResource(R.drawable.default_nor_man);
								}
							}
						}
					});

			if (bitmap != null) {
				holder.icon.setImageBitmap(bitmap);
			}
			if (item.getTitle().contains("conference")) {
				holder.icon.setBorderColor(getResources().getColor(
						R.color.lightblue));
				holder.icon.setBorderWidth(2);
			}

			holder.title.setText("" + item.getTitle().split("@")[0]);
			holder.msg.setText("" + item.getMsg());
			holder.time.setText("" + TimeUtil.parseDate(item.getTime()));
			if (item.getUnReadSum() > 0) {
				holder.unReadSum.setVisibility(View.VISIBLE);
				if (item.getUnReadSum() > 99) {
					holder.unReadSum.setText("" + "99+");
				} else {
					holder.unReadSum.setText("" + item.getUnReadSum() + "");
				}
			} else {
				holder.unReadSum.setVisibility(View.GONE);
			}
			holder.deleteHolder.setOnClickListener(MessageFragment.this);
			return slideView;
		}
	}

	private static class ViewHolder {
		public CircularImageView icon;
		public TextView title;
		public TextView msg;
		public TextView time;
		public TextView unReadSum;
		public ViewGroup deleteHolder;

		ViewHolder(View view) {
			icon = (CircularImageView) view.findViewById(R.id.icon);
			title = (TextView) view.findViewById(R.id.title);
			msg = (TextView) view.findViewById(R.id.msg);
			time = (TextView) view.findViewById(R.id.time);
			deleteHolder = (ViewGroup) view.findViewById(R.id.holder);
			unReadSum = (TextView) view.findViewById(R.id.unread_chat_contact);
		}
	}

	// 排序
	private void messageListSort() {
		Collections.sort(mMessageItems, new Comparator<MessageItem>() {
			@Override
			public int compare(MessageItem lhs, MessageItem rhs) {
				Date date1 = DateUtil.str2Date(lhs.getTime());
				Date date2 = DateUtil.str2Date(rhs.getTime());
				// 对日期字段进行降序，如果欲升序可采用date1.compareTo(date2)方法
				return date2.compareTo(date1);
			}
		});
	}

	// 刷新mMessageItems
	private void refreshMessageItems(String[] message) {
		mMessageItems.remove(STChatApplication.messageCache.get(message[0]));
		mMessageItems.add(STChatApplication.messageCache.get(message[0]));
	}

	// 刷新View
	private void refreshView() {
		messageListSort();// 按照时间降序排列
		if (mMessageItems.size() <= 0) {
			mListView.setVisibility(View.GONE);
			mLoadingView.setVisibility(View.VISIBLE);
			mLoadingView.setText(R.string.no_data);
		} else {
			mListView.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
		}
		if (mListView.getAdapter() != null) {
			slideAdapter.notifyDataSetChanged();
		}
	}

	// 聊天室头像
	private Bitmap getRoomIcon(final String roomTitle) {
		Bitmap combineBitmap = null;
		if (STChatApplication.imageCache.containsKey(roomTitle)) {
			SoftReference<Bitmap> reference = STChatApplication.imageCache
					.get(roomTitle);
			combineBitmap = reference.get();
			if (combineBitmap != null) {
				return combineBitmap;
			}
		}
		List<String> list = CombineBitmapUtil.getRoomMembers(roomTitle);
		Bitmap[] bitmaps = null;
		if (list == null || list.size() <= 1) {
			return null;
		}
		if (list.size() > 9) {
			List<String> newList = new ArrayList<String>();
			for (int i = 0; i < 9; i++) {
				newList.add(list.get(i));
			}
			combineBitmaps = getBitmapEntitys(newList.size());
			bitmaps = CombineBitmapUtil.getBitmaps(newList, combineBitmaps);
		} else {
			combineBitmaps = getBitmapEntitys(list.size());
			bitmaps = CombineBitmapUtil.getBitmaps(list, combineBitmaps);
		}
		combineBitmap = CombineBitmapUtil.getCombineBitmaps(combineBitmaps,
				bitmaps);
		// 缓存
		STChatApplication.imageCache.put(roomTitle, new SoftReference<Bitmap>(
				combineBitmap));

		return combineBitmap;
	}

	private List<CombineBitmap> getBitmapEntitys(int count) {
		List<CombineBitmap> mList = new ArrayList<CombineBitmap>();
		String value = PropertiesUtil.readData(STChatApplication.getInstance(),
				String.valueOf(count), R.raw.data);
		String[] arr1 = value.split(";");
		int length = arr1.length;
		for (int i = 0; i < length; i++) {
			String content = arr1[i];
			String[] arr2 = content.split(",");
			CombineBitmap entity = null;
			for (int j = 0; j < arr2.length; j++) {
				entity = new CombineBitmap();
				entity.x = Float.valueOf(arr2[0]);
				entity.y = Float.valueOf(arr2[1]);
				entity.width = Float.valueOf(arr2[2]);
				entity.height = Float.valueOf(arr2[3]);
			}
			mList.add(entity);
		}
		return mList;
	}

	public class RefreshMessageList extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "消息列表界面收到一条广播，通知界面更新吧");
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();
			// 单人聊天文本消息
			if (action.equals(ConstantUtils.SINGLE_CHAT_ACTION)) {
				String[] message = bundle.getStringArray("msg");
				if (message[1].equalsIgnoreCase(currentUser)
						|| message[0].equalsIgnoreCase(currentUser)) {
					refreshMessageItems(message);
				}
			}
			// 单人聊天图片消息
			if (action.equals(ConstantUtils.SINGLE_CHAT_PIC_ACTION)) {
				String[] message = bundle.getStringArray("msg_pic");
				if (message[1].equalsIgnoreCase(currentUser)
						|| message[0].equalsIgnoreCase(currentUser)) {
					refreshMessageItems(message);
				}
			}

			// 群组聊天消息
			if (action.equals(ConstantUtils.GROUP_CHAT_ACTION)) {
				String[] message = bundle.getStringArray("msg");
				refreshMessageItems(message);
			}

			// 接收到删除好友的广播，同时删除和该好友的消息列表
			if (action.equals(ConstantUtils.ROSTER_DELETED_ACTION)) {
				String delUser = bundle.getString("deleteName");
				Logger.d(TAG, "messageFragment:接收到删除" + delUser + "的广播 刷新一下页面");
				mMessageItems.remove(STChatApplication.messageCache
						.get(StringUtil.getUserNameByJid(delUser)));
			}

			new Thread(new Runnable() {
				@Override
				public void run() {
					// 通过 Handler的 obtainMessage回收 Message对象，减少 Message对象的创建开销
					RefreshListHandler.sendMessage(RefreshListHandler
							.obtainMessage(REFRESH_MESSAGE_LIST));
				}

			}).start();
		}
	}

	@Override
	public void doAsyncTaskBefore(int taskId) {

	}

	@Override
	public Object doAsyncTaskIn(int taskId, Object... params) {
		boolean bool = false;
		int delChatMessage = 0;
		// 清除本地数据库
		if (msgTitle.contains("conference")) {
			ChatRoomMessageDao chatRoomMessageDao = new ChatRoomMessageDao();
			delChatMessage = chatRoomMessageDao.delChatRoomMsgWithSb(
					currentUser, msgTitle);
		} else {
			ChatMessageDao chatMessageDao = new ChatMessageDao();
			delChatMessage = chatMessageDao.delChatWithSb(msgTitle);
		}
		// 从mMessageItems移除
		mMessageItems.remove(STChatApplication.messageCache.get(msgTitle));
		// 清除该用户缓存
		STChatApplication.messageCache.remove(msgTitle);
		if (delChatMessage > 0) {
			bool = true;
		}

		return bool;
	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {

		boolean bool = (Boolean) result;
		if (bool) {
			// 刷新页面
			refreshView();
			// 发送广播刷新红气泡
			Intent intent = new Intent();
			intent.setAction(ConstantUtils.MESSAGE_DELETED_ACTION);
			STChatApplication.getInstance().sendBroadcast(intent);
		} else {
			Toast.makeText(mContext, "Delete failed, please try again",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void doCancelled(int taskId) {

	}
}
