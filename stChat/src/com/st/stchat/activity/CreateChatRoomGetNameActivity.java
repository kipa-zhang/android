package com.st.stchat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.R;

/**
 * 填写聊天室名称页面
 * 
 * @author  
 * 
 */
public class CreateChatRoomGetNameActivity extends BaseActivity {

	private ImageButton backBtn;
	private TextView title;
	private Button nextBtn;
	private EditText roomName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_createchatroom_roomname);
		findView();
		initView();
		initListener();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("FinishCreateChatRoomGetNameActivity");
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void findView() {
		nextBtn = (Button) findViewById(R.id.buttonTitleRight);
		backBtn = (ImageButton) findViewById(R.id.buttonTitleLeft);
		title = (TextView) findViewById(R.id.textViewTitle);
		roomName = (EditText) findViewById(R.id.chatroom_title_edittext);
	}

	private void initView() {
		nextBtn.setText("" + "Next");
		title.setText("" + "Create a Room");
		nextBtn.setBackgroundResource(R.drawable.title_selector);
		nextBtn.setTextSize(16);
		nextBtn.setVisibility(View.VISIBLE);
	}

	private void initListener() {
		backBtn.setOnClickListener(backListener);
		nextBtn.setOnClickListener(nextListener);
	}

	/**
	 * back 监听
	 */
	private OnClickListener backListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			finish();
		}
	};

	/**
	 * next 监听
	 */
	private OnClickListener nextListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			String title = roomName.getText().toString();
			if (title.length() == 0) {
				Toast.makeText(CreateChatRoomGetNameActivity.this,
						"The name should not be empty", Toast.LENGTH_SHORT)
						.show();
			} else {
				String[] to = new String[] { "roomTitle", title };
				Intent intent = new Intent(CreateChatRoomGetNameActivity.this,
						CreateChatRoomActivity.class);
				intent.putExtra("send_CreateChatRoomActivity", to);
				startActivity(intent);
			}
		}
	};

	/**
	 * 广播监听 成功创建了房间之后 finish掉
	 */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			finish();
		}
	};

}
