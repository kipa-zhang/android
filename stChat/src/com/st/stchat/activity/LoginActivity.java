package com.st.stchat.activity;

import java.util.Collection;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.st.stchat.BaseAsyncTask;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.dao.ContactDao;
import com.st.stchat.utils.FileUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 
 * @author juwei 2014.11.20
 * 
 */
public class LoginActivity extends BaseActivity implements OnClickListener,
		BaseAsyncTaskListener {
	private static final String TAG = "LoginActivity";
	private static final int LOGIN = 1;
	private ImageView login_picture;
	private EditText et_account, et_password;
	private Button butt_login, butt_register, butt_ipsetting;
	private RelativeLayout rl_user;
	private String userAccount, userPassword;
	private BaseAsyncTask task;
	private Map<String, String> userMap;
	private boolean isSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		// Log.d(TAG, "---------------onCreate");
		initView();
		initAnim();
		setListener();

		userMap = InfoUtils.getUserAccount(STChatApplication.getInstance());
		et_account.setText("" + userMap.get("userAccountName"));
		et_password.setText("" + userMap.get("userAccountPassword"));

		// 在登陆界面显示当前账户头像
		FileUtils fileUtils = new FileUtils();
		if (fileUtils.isFileExist(userMap.get("userAccountName"))) {
			// Log.d(TAG, "---------------准备显示头像");
			// 如果有头像文件，就取出来bitmap，然后显示
			login_picture
					.setImageBitmap(fileUtils
							.getBitmapFromPathByAccount(userMap
									.get("userAccountName")));
		}

		// Log.d(TAG, "---------------准备判断");
		// 判断是否是被T出来的
		if (!getIntent().getBooleanExtra("isOutAlive", false)) {
			Log.d(TAG, "---------------不是被T出来的");
			// 程序异常结束断开连接之后，再次打开自动登陆
			if (userMap != null) {
				if (!TextUtils.isEmpty(userMap.get("userAccountName"))
						&& !TextUtils.isEmpty(userMap
								.get("userAccountPassword"))) {
					userAccount = userMap.get("userAccountName");
					userPassword = userMap.get("userAccountPassword");
					// Log.d(TAG, "---------------准备登陆");
					task = new BaseAsyncTask(this, this, LOGIN);
					task.setDialogMessage("Is landing....");
					task.execute();
				}
			}
		} else {
			Log.d(TAG, "---------------是被T出来的");

			et_password.setText("");
		}

	}

	private void initView() {
		login_picture = (ImageView) findViewById(R.id.login_picture);
		et_account = (EditText) findViewById(R.id.et_account);
		et_password = (EditText) findViewById(R.id.et_password);
		butt_login = (Button) findViewById(R.id.butt_login);
		butt_register = (Button) findViewById(R.id.butt_register);
		butt_ipsetting = (Button) findViewById(R.id.butt_ipsetting);
		rl_user = (RelativeLayout) findViewById(R.id.rl_user);
	}

	private void initAnim() {
		Animation anim = AnimationUtils.loadAnimation(
				STChatApplication.getInstance(), R.anim.login_anim);
		anim.setFillAfter(true);
		rl_user.startAnimation(anim);
	}

	private void setListener() {
		butt_login.setOnClickListener(this);
		butt_register.setOnClickListener(this);
		butt_ipsetting.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Log.i(TAG,
		// "服务器地址为："
		// + InfoUtils.getXmppHost(STChatApplication.getInstance())
		// + " ："
		// + Integer.parseInt(InfoUtils
		// .getXmppPort(STChatApplication.getInstance())));

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.butt_login:
			// 设置键盘收回
			View view = getWindow().peekDecorView();
			if (view != null) {
				InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}

			userAccount = et_account.getText().toString().trim();
			userPassword = et_password.getText().toString().trim();
			if (TextUtils.isEmpty(userAccount)) {
				Toast.makeText(this, "Please enter account", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (TextUtils.isEmpty(userPassword)) {
				Toast.makeText(this, "Please enter the password",
						Toast.LENGTH_SHORT).show();
				return;
			}

			task = new BaseAsyncTask(this, this, LOGIN);
			task.setDialogMessage("Is landing....");
			task.execute();
			break;
		case R.id.butt_register:
			startActivity(new Intent(this, RegisterActivity.class));
			break;
		case R.id.butt_ipsetting:
			startActivity(new Intent(this, ConnectSettingActivity.class));
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
		Log.d(TAG, "---------------doAsyncTaskIn");

		String resu = "-100";
		if (XmppConnectionServer.getInstance().getConnection() != null) {
			resu = XmppConnectionServer.getInstance().login(userAccount,
					userPassword);
		}
		// Log.d(TAG, "---------------请求结果："+resu);
		Log.d(TAG, "登陆代码   : " + resu);
		// TODO
		try {
			Thread.sleep(111);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return resu;
	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {
		String s = (String) result;
		String str = "-100";
		if (s.equals("3")) {
			str = "And the server connection failed";// 与服务器连接失败
		} else if (s.equals("0") && XmppConnectionServer.getInstance().isConn()) {
			String userJID = XmppConnectionServer.getInstance().getConnection()
					.getUser();
			isSave = InfoUtils.saveUserAccount(STChatApplication.getInstance(),
					userAccount, userPassword, userJID, "1");

			str = "The successful landing ";// 登陆成功

			new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "---------------创建当前登陆用户头像线程------");
					FileUtils fileUtils = new FileUtils();
					// 返回 0 存储成功，-1 文件或文件夹没找到 ，-2 存储失败
					fileUtils.saveMyBitmap(XmppConnectionServer.getInstance()
							.getUserImage(userAccount), userAccount);
					Log.d(TAG, "---------------当前登陆用户头像线程执行完毕------");

					Log.d(TAG, "---------------初始化联系人数据------");
					if (XmppConnectionServer.getInstance().isConn()) {
						// 去服务器拿联系人
						Connection connection = XmppConnectionServer
								.getInstance().getConnection();
						Collection<RosterEntry> entries = connection
								.getRoster().getEntries();
						// 判断联系人数量与服务器端
						if (!ContactDao.getInstance()
								.getContactCountByAccount(userAccount)
								.equals(entries.size())) {
							ContactDao.getInstance().delAllContactByAccount(
									userAccount);
							if (entries != null) {
								// 将联系人写入本地
								for (RosterEntry entry : entries) {
									ContactDao.getInstance()
											.saveContact(entry,
													connection.getRoster(),
													userAccount);
								}
							}
						}
					}
				}
			}).start();

		} else if (s.equals("1")) {
			str = "Login failed";// 登陆失败
		} else if (s.equals("2")) {
			str = "This account has been landing";// 这个账号已经被登陆
		} else if (s.equals("4")) {
			str = "The user name or password is not correct";// 用户名或者密码错误
		} else {
			str = "Unknown Exception";
		}
		Log.d(TAG, "登陆情况----" + str);
		// TODO
		Toast.makeText(STChatApplication.getInstance(), "" + str,
				Toast.LENGTH_SHORT).show();

		if (s.equals("0")) {
			if (isSave) {
				// Log.i(TAG, "----下次将自动登陆----" + isSave);
				startActivity(new Intent(this, MainActivity.class));
				finish();
			} else {
				Toast.makeText(
						STChatApplication.getInstance(),
						"Internal data file error, please uninstall and reinstall",
						Toast.LENGTH_LONG).show();// 内部数据文件错误，请卸载后重新安装
			}
		}

	}

	@Override
	public void doCancelled(int taskId) {
		isSave = false;
	}

}
