package com.st.stchat.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.st.stchat.R;
import com.st.stchat.widget.SearchEditText;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 添加朋友页面
 * 
 * @author  
 * 
 */
public class AddFriendsActivity extends BaseActivity {

	private TextView addFriendTitle;
	private SearchEditText searchEditText;
	private ImageButton backBtn;
	private ListView search_result_list;
	private View headSearchView;
	private List<SearchResultItem> resultItems = new ArrayList<AddFriendsActivity.SearchResultItem>();
	private SearchListAdapter searchListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addfriend);
		findView();
		initView();
	}

	private void findView() {
		addFriendTitle = (TextView) findViewById(R.id.textViewTitle);
		backBtn = (ImageButton) findViewById(R.id.buttonTitleLeft);
		search_result_list = (ListView) findViewById(R.id.search_friend_list);

		LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
		headSearchView = inflater.inflate(R.layout.common_search, null);
		searchEditText = (SearchEditText) headSearchView
				.findViewById(R.id.search_edit);
		search_result_list.addHeaderView(headSearchView);

		searchListAdapter = new SearchListAdapter();
		search_result_list.setAdapter(searchListAdapter);
	}

	private void initView() {
		addFriendTitle.setText("" + "Add Contacts");
		backBtn.setOnClickListener(addFriendBackListener);
		search_result_list.setOnItemClickListener(searchListListener);
		searchEditText.setHint("Search by Name");
		// 根据关键字查询
		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				showSearchContent(s.toString());
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

	private void showSearchContent(String content) {
		if (TextUtils.isEmpty(content)) {
			resultItems.clear();
		} else {
			resultItems.clear();
			SearchResultItem item = new SearchResultItem();
			item.userJid = content;
			resultItems.add(item);
		}

		searchListAdapter.notifyDataSetChanged();
	}

	/**
	 * list 监听
	 */
	private OnItemClickListener searchListListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			if (position > 0) {

				if (!XmppConnectionServer.getInstance().isConn()) {
					Toast.makeText(AddFriendsActivity.this, "You have dropped",
							Toast.LENGTH_SHORT).show();
					return;
				}

				String userJid = resultItems.get(position - 1).userJid;
				// showAlertDialog(userJID);
				String[] to = new String[] { "userJid", userJid };
				Intent intent = new Intent(AddFriendsActivity.this,
						AddFriendsSearchActivity.class);
				intent.putExtra("send_searchActivity", to);
				startActivity(intent);
			}
		}
	};

	private class SearchListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		SearchListAdapter() {
			super();
			mInflater = AddFriendsActivity.this.getLayoutInflater();
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
						R.layout.activity_addfriend_item, null);
				holder = new ViewHolder();
				holder.userJid = (TextView) convertView
						.findViewById(R.id.search_result);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.userJid.setText("" + resultItems.get(position).userJid);

			return convertView;
		}

	};

	public class SearchResultItem {
		public String userJid;
	}

	private static class ViewHolder {
		private TextView userJid;
	}

	private OnClickListener addFriendBackListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			finish();
		}
	};
}
