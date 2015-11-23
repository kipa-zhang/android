package com.st.stchat.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.AsyncTaskBase;
import com.st.stchat.BaseAsyncTask;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.dao.ContactDao;
import com.st.stchat.manager.ContactManager;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.view.LoadingView;
import com.st.stchat.widget.AlertDialog;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 添加朋友的搜索结果页面
 * 
 * @author  
 * 
 */
public class AddFriendsSearchActivity extends BaseActivity implements
		BaseAsyncTaskListener {
	private static final String TAG = "AddFriendsSearchActivity";

	private TextView title;
	private ImageButton buttonTitleLeft;
	private ListView searchList;
	private SearchListAdapter adapter;
	private LoadingView mLoadingView;
	private String userJid;
	private List<String> userList;
	private List<SearchResultItem> resultItems;
	private BaseAsyncTask task;
	private SearchDataTask searchDataTask;
	private static final int ADD_FRIEND_SEND = 7;
	private String sendName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addfriend_search);
		Intent intent = getIntent();
		String[] intentStrArr = intent
				.getStringArrayExtra("send_searchActivity");
		userJid = intentStrArr[1];
		Log.d(TAG, "当前搜索的用户Jid " + userJid + "");

		initView();
		initListener();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		searchDataTask = new SearchDataTask(mLoadingView);
		searchDataTask.execute(0);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		searchDataTask.cancel(true);
	}

	private void initView() {
		buttonTitleLeft = (ImageButton) findViewById(R.id.buttonTitleLeft);
		title = (TextView) findViewById(R.id.textViewTitle);
		title.setText("" + "Results");
		searchList = (ListView) findViewById(R.id.search_user_list);
		mLoadingView = (LoadingView) findViewById(R.id.loadingView);

		adapter = new SearchListAdapter();
	}

	private void initListener() {
		searchList.setOnItemClickListener(inviteListClick);
		buttonTitleLeft.setOnClickListener(addFriendSearchBackListener);
	}

	private class SearchDataTask extends AsyncTaskBase {

		public SearchDataTask(LoadingView loadingView) {
			super(loadingView);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			mLoadingView.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			int result = 0;
			userList = ContactManager.searchUsers(userJid);
			String account = InfoUtils.getUser(STChatApplication.getInstance());
			if (userList != null) {
				if (userList.size() > 0) {
					if (userList.size() == 1 && userList.get(0).equals(account)) {
						result = 0;
					} else {
						result = 1;
						resultItems = new ArrayList<SearchResultItem>();
						for (int i = 0; i < userList.size(); i++) {
							SearchResultItem item = new SearchResultItem();
							item.userJid = userList.get(i);
							if (!userList.get(i).equals(account)) {
								resultItems.add(item);
							}
						}
					}
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == 1) {
				searchList.setAdapter(adapter);
			}
		}
	}

	// list监听
	private OnItemClickListener inviteListClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int postion,
				long arg3) {
			sendName = resultItems.get(postion).userJid;
			showAlertDialog();
		}
	};
	// back 监听
	private OnClickListener addFriendSearchBackListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			finish();
		}
	};

	private void showAlertDialog() {
		boolean isExist = ContactDao.getInstance().contactIsExist(
				InfoUtils.getUser(STChatApplication.getInstance()),
				sendName
						+ "@"
						+ XmppConnectionServer.getInstance().getConnection()
								.getServiceName());
		if (isExist) {
			new AlertDialog(AddFriendsSearchActivity.this).builder()
					.setMsg("This user already exists the contact list")
					.setPositiveButton("Cancel", new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
						}
					}).show();
		} else {
			new AlertDialog(AddFriendsSearchActivity.this).builder()
					.setMsg("Add buddy request")
					.setNegativeButton("Cancel", new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
						}
					}).setPositiveButton("Send", new OnClickListener() {
						@Override
						public void onClick(View v) {
							task = new BaseAsyncTask(
									AddFriendsSearchActivity.this,
									AddFriendsSearchActivity.this,
									ADD_FRIEND_SEND);
							task.setDialogMessage("Loading...");
							task.execute();
						}
					}).show();

		}
	}

	private class SearchListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		// private AsyncBitmapLoader asyncLoader = null;

		SearchListAdapter() {
			mInflater = AddFriendsSearchActivity.this.getLayoutInflater();
			// asyncLoader = new AsyncBitmapLoader();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return resultItems.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return resultItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.activity_addfriend_search_item, null);
				holder = new ViewHolder();
				// holder.userIconView = (ImageView) convertView
				// .findViewById(R.id.add_friend_head);
				holder.userJidView = (TextView) convertView
						.findViewById(R.id.add_friend_name);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String userName = resultItems.get(position).userJid;

			// Bitmap bitmap = asyncLoader.loadBitmap(holder.userIconView,
			// userName, new ImageCallBack() {
			// @Override
			// public void imageLoad(ImageView imageView, Bitmap bitmap) {
			// // TODO Auto-generated method stub
			// if (bitmap != null) {
			// imageView.setImageBitmap(bitmap);
			// } else {
			// imageView
			// .setImageResource(R.drawable.default_nor_man);
			// }
			// }
			// });
			// if (bitmap != null) {
			// holder.userIconView.setImageBitmap(bitmap);
			// }

			// holder.userIconView.setImageResource(R.drawable.default_nor_man);
			holder.userJidView.setText("" + userName);

			return convertView;
		}
	}

	public class SearchResultItem {
		public String userJid;
	}

	private class ViewHolder {
		// public ImageView userIconView;
		public TextView userJidView;
	}

	@Override
	public void doAsyncTaskBefore(int taskId) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object doAsyncTaskIn(int taskId, Object... params) {
		// TODO Auto-generated method stub
		boolean addResult = ContactManager.addUser(sendName, sendName);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return addResult;
	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {
		// TODO Auto-generated method stub
		boolean bool = (Boolean) result;
		if (bool) {
			Toast.makeText(AddFriendsSearchActivity.this, "Send successfully",
					Toast.LENGTH_LONG).show();
			finish();
		} else {
			Toast.makeText(AddFriendsSearchActivity.this, "Send failed",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void doCancelled(int taskId) {
		// TODO Auto-generated method stub

	}

}
