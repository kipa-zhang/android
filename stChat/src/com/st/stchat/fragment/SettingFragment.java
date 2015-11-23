package com.st.stchat.fragment;

import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.activity.PersonalProfileActivity;
import com.st.stchat.activity.SettingActivity;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.FileUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 
 * @author juwei 2014.11.20
 * 
 */
public class SettingFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "SettingFragment";
	private static final int STATU_CHANGED = 0;

	private LinearLayout ll_personInfo, ll_status, ll_photo_album, ll_setting;
	private ImageView rl_iv_personInfo_head, iv_status;
	private TextView rl_tv_personInfo_name, rl_tv_personInfo_account,
			tv_status;

	private String userJID, userAccount, userStatus;
	private Bitmap bitmap;
	private ImageButton buttonBottomLeft, buttonBottomRight;

	private Handler statusHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STATU_CHANGED:
				System.out.println("Handler ---- case statu_changed");
				// tv_status.setText("" + msg.getData().getString("status"));
				changeStatus(userStatus);
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
		View view = inflater.inflate(R.layout.fragment_setting, container,
				false);

		// 加载Widget
		initWidgets(view);
		initListener();

		buttonBottomLeft.setImageResource(R.drawable.tianjia_bai);
		buttonBottomRight.setImageResource(R.drawable.tianjia_bai);

		buttonBottomLeft.setVisibility(View.INVISIBLE);
		buttonBottomRight.setVisibility(View.INVISIBLE);
		return view;
	}

	// 初始化按钮部件
	private void initWidgets(View view) {
		ll_personInfo = (LinearLayout) view.findViewById(R.id.ll_personInfo);
		ll_status = (LinearLayout) view.findViewById(R.id.ll_status);
		ll_photo_album = (LinearLayout) view.findViewById(R.id.ll_photo_album);
		ll_setting = (LinearLayout) view.findViewById(R.id.ll_setting);
		rl_iv_personInfo_head = (ImageView) view
				.findViewById(R.id.rl_iv_personInfo_head);
		iv_status = (ImageView) view.findViewById(R.id.iv_status);

		tv_status = (TextView) view.findViewById(R.id.tv_status);
		rl_tv_personInfo_name = (TextView) view
				.findViewById(R.id.rl_tv_personInfo_name);
		rl_tv_personInfo_account = (TextView) view
				.findViewById(R.id.rl_tv_personInfo_account);

		buttonBottomLeft = (ImageButton) view
				.findViewById(R.id.buttonBottomLeft);
		buttonBottomRight = (ImageButton) view
				.findViewById(R.id.buttonBottomRight);
	}

	private void initListener() {
		ll_personInfo.setOnClickListener(this);
		ll_status.setOnClickListener(this);
		ll_photo_album.setOnClickListener(this);
		ll_setting.setOnClickListener(this);
		rl_iv_personInfo_head.setOnClickListener(this);
		buttonBottomLeft.setOnClickListener(this);
		buttonBottomRight.setOnClickListener(this);
		// iv_personInfo_head.setOnClickListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		// Log.e(TAG, "onStartonStartonStartonStartonStartonStart");
		userJID = InfoUtils.getUserJID(getActivity());
		if (!TextUtils.isEmpty(userJID)) {
			userAccount = StringUtils.parseName(userJID);
			rl_tv_personInfo_name.setText("" + userAccount);
			rl_tv_personInfo_account.setText("" + userJID);
		}
		userStatus = InfoUtils.getUserStatus(STChatApplication.getInstance());
		changeStatus(userStatus);
		FileUtils fileUtils = new FileUtils();
		if (fileUtils.isFileExist(userAccount)) {
			// 如果有头像文件，就取出来bitmap，然后显示
			bitmap = fileUtils.getBitmapFromPathByAccount(userAccount);
			if (bitmap != null) {
				rl_iv_personInfo_head.setImageBitmap(bitmap);
			}
			bitmap = null;
		} else {
			Toast.makeText(getActivity(), "Please set the head",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void changeStatus(String status) {

		if (!TextUtils.isEmpty(status)) {
			switch (Integer.parseInt(status)) {
			case 0:
				iv_status.setImageResource(R.drawable.status_free_to_chat);
				tv_status.setText("" + ConstantUtils.STATUS_FREE_TO_CHAT);
				break;
			case 1:
				iv_status.setImageResource(R.drawable.status_available);
				tv_status.setText("" + ConstantUtils.STATUS_AVAULABLE);
				break;
			case 2:
				iv_status.setImageResource(R.drawable.status_away);
				tv_status.setText("" + ConstantUtils.STATUS_AWAY);
				break;
			case 3:
				iv_status.setImageResource(R.drawable.status_on_phone);
				tv_status.setText("" + ConstantUtils.STATUS_ON_PHONE);
				break;
			case 4:
				iv_status.setImageResource(R.drawable.status_away);
				tv_status.setText("" + ConstantUtils.STATUS_EXTENDED_AWAY);
				break;
			case 5:
				iv_status.setImageResource(R.drawable.status_on_the_road);
				tv_status.setText("" + ConstantUtils.STATUS_ON_THE_ROAD);
				break;
			case 6:
				iv_status.setImageResource(R.drawable.status_do_not_disturb);
				tv_status.setText("" + ConstantUtils.STATUS_DO_NOT_DISTURB);
				break;

			default:
				break;
			}
			Log.i(TAG, "当前状态为：" + tv_status.getText().toString());
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		// Log.e(TAG,
		// "onResumeonResumeonResumeonResumeonResumeonResumeonResume");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_personInfo_head:

			break;
		case R.id.rl_iv_personInfo_head:
			// 跳转到个人信息详情
			// Toast.makeText(getActivity(), "Please click on the picture",
			// Toast.LENGTH_SHORT).show();// 请点击头像
			// if (XmppConnectionServer.getInstance().isConn()) {
			// Toast.makeText(
			// getActivity(),
			// "是否验证通过？"
			// + XmppConnectionServer.getInstance()
			// .getConnection().isAuthenticated(),
			// Toast.LENGTH_SHORT).show();
			// }

			startActivity(new Intent(getActivity(),
					PersonalProfileActivity.class));
			break;
		case R.id.ll_status:
			// 用Dialog修改当前在线状态
			changeStatusDialog();

			break;
		case R.id.ll_photo_album:
			// 跳转到个人相册
			Toast.makeText(getActivity(), "This function is not yet open",
					Toast.LENGTH_SHORT).show();// 该功能尚未开放
			//
			// 暂时用来测试心跳
			//
			// if (XmppConnectionServer.getInstance().isConn()) {
			// Toast.makeText(
			// getActivity(),
			// "conn 为空嘛？："
			// + (XmppConnectionServer.getInstance()
			// .getConnection() == null)
			// + "\n isConnected() 连接着的嘛？"
			// + XmppConnectionServer.getInstance()
			// .getConnection().isConnected(),
			// Toast.LENGTH_SHORT).show();
			// }

			break;
		case R.id.ll_setting:
			// 跳转到系统设置界面
			Intent intent = new Intent(getActivity(), SettingActivity.class);
			startActivity(intent);
			break;
		case R.id.buttonBottomLeft:

			break;
		case R.id.buttonBottomRight:

			break;
		default:
			break;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
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

	private void changeStatusDialog() {
		AlertDialog.Builder buider = new Builder(getActivity());
		// buider.setTitle("这是标题：");
		// final String[] items = new String[]{"条目1","条目2","条目3","条目4"};
		final String[] items = getResources().getStringArray(R.array.my_state);
		// 传进去一个字符数组，然后索引0,1,2,3,是默认选中的条目(-1为一个都不选中)
		buider.setSingleChoiceItems(items, Integer.parseInt(userStatus),
				new DialogInterface.OnClickListener() {
					@Override
					// 传进来当前的对话框，和第几个条目被点击了
					public void onClick(DialogInterface dialog, final int which) {

						// 当选中点击了其中一个条目时，就会自动退出该对话框
						// 在这里创建线程在向服务器改变在线状态
						// 0.(空闲)Free To Chat
						// 1.(在线)Available
						// 2.(离开)Away
						// 3.(电话中)On Phone
						// 4.(自动离开)Extended Away
						// 5.(在路上)On The Road
						// 6.(请勿打扰)Do Not Disturb(正忙)

						if (!userStatus.equals("" + which)
								&& XmppConnectionServer.getInstance().isConn()) {
							// Toast.makeText(getActivity(),
							// which + "," + items[which] + "按钮被点击了",
							// Toast.LENGTH_SHORT).show();
							new Thread(new Runnable() {
								@Override
								public void run() {

									if (XmppConnectionServer.getInstance()
											.setPresence(which)) {
										InfoUtils.saveUserStatus(
												STChatApplication.getInstance(),
												"" + which);
										userStatus = "" + which;
										Message message = new Message();
										Bundle bundle = new Bundle();
										bundle.putString("status", items[which]);
										message.what = STATU_CHANGED;
										message.setData(bundle);
										statusHandler.sendMessage(message);
									}

								}
							}).start();
						} else {
							if (!userStatus.equals("" + which)) {
								Toast.makeText(getActivity(),
										"You have dropped", Toast.LENGTH_SHORT)
										.show();
							}

						}

						dialog.dismiss();

					}
				});
		buider.show();
	}

}
