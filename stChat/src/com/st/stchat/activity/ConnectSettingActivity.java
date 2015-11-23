package com.st.stchat.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.BaseAsyncTask;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.dao.ChatMessageDao;
import com.st.stchat.utils.InfoUtils;

/**
 * 
 * @author juwei 2014.11.20
 * 
 */
public class ConnectSettingActivity extends BaseActivity implements
		OnClickListener, BaseAsyncTaskListener {
	private static final int IPSETTING = 1;
	private Button butt_change;
	private ImageButton buttonTitleLeft;
	private TextView tv_ip;
	private EditText et_ip, et_port;
	private String ip_server, port_server, ip, port;
	private BaseAsyncTask task;
	private TextView textViewTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_connectsetting);

		initView();
		setListener();
		textViewTitle.setVisibility(View.VISIBLE);
		textViewTitle.setText("" + "IP Setting");

		// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		// .detectDiskReads().detectDiskWrites().detectNetwork()
		// .penaltyLog().build());
		// StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		// .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
		// .build());
		//
		// // 测试数据
		// et_ip.setText(""+"10.41.88.254");
		// et_ip.setText(""+"10.41.88.254");
		// et_port.setText(""+"5222");

	}

	@Override
	protected void onStart() {
		super.onStart();
		ip_server = InfoUtils.getXmppHost(STChatApplication.getInstance());
		port_server = InfoUtils.getXmppPort(STChatApplication.getInstance());
		tv_ip.setText("" + ip_server + " : " + port_server);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		buttonTitleLeft = (ImageButton) findViewById(R.id.buttonTitleLeft);
		butt_change = (Button) findViewById(R.id.butt_change);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		tv_ip = (TextView) findViewById(R.id.tv_ip);
		et_ip = (EditText) findViewById(R.id.et_ip);
		et_port = (EditText) findViewById(R.id.et_port);
	}

	private void setListener() {
		buttonTitleLeft.setOnClickListener(this);
		butt_change.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonTitleLeft:
			finish();
			break;
		case R.id.butt_change:
			ip = et_ip.getText().toString().trim();
			port = et_port.getText().toString().trim();
			if (TextUtils.isEmpty(ip)) {
				Toast.makeText(this, "Please enter IP", Toast.LENGTH_SHORT)
						.show();// 请输入IP地址
				return;
			}
			// if (!IsUtils.isIPAdress(ip)) {
			// Toast.makeText(this, "IP is not in the correct format",
			// Toast.LENGTH_SHORT).show();// ip地址格式不正确
			// return;
			// }
			if (TextUtils.isEmpty(port)) {
				Toast.makeText(this, "Please input port", Toast.LENGTH_SHORT)
						.show();// 请输入端口号
				return;
			}
			task = new BaseAsyncTask(this, this, IPSETTING);
			task.setDialogMessage("Being revised, please wait....");// 正在修改，请稍等
			task.execute();
			break;
		default:
			break;
		}

	}

	@Override
	public void doAsyncTaskBefore(int taskId) {

	}

	@Override
	public Object doAsyncTaskIn(int taskId, Object... params) {
		boolean bool = InfoUtils.saveXmppAddress(
				STChatApplication.getInstance(), ip, port);
		// 还原第一次加载messagefragment标示
		STChatApplication.firstLoadMessageTag = true;
		// 在这里清空数据库所有数据
		ChatMessageDao cmd = new ChatMessageDao();
		cmd.clear();
		// 清空sharePerference缓存数据
		InfoUtils.deleteUserAccount(STChatApplication.getInstance());
		InfoUtils.deletePacketDomain(STChatApplication.getInstance());
		// 清空图片和头像缓存文件夹
		// （目前是通过stPhoto下的ora192.168.0.1 与ora10.41.88.254文件夹 来区别不同IP地址时候的用户文件夹）

		try {
			Thread.sleep(111);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return bool;
	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {

		boolean bool = (Boolean) result;
		if (bool) {
			Toast.makeText(this, "Successful modification", Toast.LENGTH_SHORT)
					.show();
			finish();
		} else {
			Toast.makeText(this, "Modify the failure", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void doCancelled(int taskId) {

	}

}
