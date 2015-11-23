package com.st.stchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.BaseAsyncTask;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 
 * @author juwei 2014.11.20
 * 
 */
public class RegisterActivity extends BaseActivity implements OnClickListener,
		BaseAsyncTaskListener {
	private static final String TAG = "RegisterActivity";
	private static final int REGISTER = 1;
	private Button butt_register;
	private ImageButton buttonTitleLeft;
	private EditText et_useraccount, et_userpassword, et_userrepassword;
	private String account, password, repassword;
	private BaseAsyncTask task;
	private TextView textViewTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register);

		initView();
		setListener();

		textViewTitle.setVisibility(View.VISIBLE);
		textViewTitle.setText("" + "User Register");
	}

	private void initView() {
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		buttonTitleLeft = (ImageButton) findViewById(R.id.buttonTitleLeft);
		butt_register = (Button) findViewById(R.id.butt_register);
		et_useraccount = (EditText) findViewById(R.id.et_useraccount);
		et_userpassword = (EditText) findViewById(R.id.et_userpassword);
		et_userrepassword = (EditText) findViewById(R.id.et_userrepassword);
	}

	private void setListener() {
		buttonTitleLeft.setOnClickListener(this);
		butt_register.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.butt_register:
			// 收起输入法
			View view = getWindow().peekDecorView();
			if (view != null) {
				InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}

			account = et_useraccount.getText().toString().trim();
			password = et_userpassword.getText().toString().trim();
			repassword = et_userrepassword.getText().toString().trim();

			if (TextUtils.isEmpty(account)) {
				Toast.makeText(this, "Please enter account", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (TextUtils.isEmpty(password)) {
				Toast.makeText(this, "Please enter the password",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (TextUtils.isEmpty(repassword)) {
				Toast.makeText(this, "Please enter your password again",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (!password.equals(repassword)) {
				Toast.makeText(this, "The two input is not consistent",
						Toast.LENGTH_SHORT).show();
				return;
			}

			task = new BaseAsyncTask(this, this, REGISTER);
			task.setDialogMessage("Registration....");
			task.execute();

			break;
		case R.id.buttonTitleLeft:
			finish();
			// startActivity(new Intent(RegisterActivity.this,
			// TestActivity.class));
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
		String resu = "-100";
		if (XmppConnectionServer.getInstance().getConnection() != null) {
			resu = XmppConnectionServer.getInstance().regist(account, password);
		}
		try {
			Thread.sleep(111);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "注册结果码------" + resu);
		return resu;
	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {

		String s = (String) result;
		String str = "XXXX";
		if (s.equals("-1")) {
			str = "The server returned no results";// 服务器没有返回结果
		} else if (s.equals("0")) {
			str = "The success of the registration ";// 注册成功
		} else if (s.equals("1")) {
			str = "Registration failed";// 注册失败
		} else if (s.equals("2")) {
			str = "The account already exists";// 该账号已经存在
		} else if (s.equals("3")) {
			str = "And the server connection failed";// 与服务器连接失败
		} else {

		}
		Log.d(TAG, "注册情况------" + str);
		Toast.makeText(this, "" + str, Toast.LENGTH_SHORT).show();
		XmppConnectionServer.getInstance().closeConnection();
		if (s.equals("0")) {
			finish();
		}
	}

	@Override
	public void doCancelled(int taskId) {
	}
}
