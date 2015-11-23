package com.st.stchat.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jivesoftware.smack.util.StringUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.st.stchat.BaseAsyncTask;
import com.st.stchat.BaseAsyncTask.BaseAsyncTaskListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.utils.FileUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.widget.CircularImageView;
import com.st.stchat.xmpp.XmppConnectionServer;

public class PersonalProfileActivity extends BaseActivity implements
		OnClickListener, BaseAsyncTaskListener {
	private static final String TAG = "PersonalProfileActivity";

	private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int PHOTO_REQUEST_CUT = 3;// 结果
	// 创建一个以当前时间为名称的缓存照片文件
	private File tempFile = new File(Environment.getExternalStorageDirectory(),
			getPhotoFileNameByTime());

	private static final int SETPHOTO = 5;
	private BaseAsyncTask task;
	private LinearLayout ll_personInfo_head, ll_personInfo_name,
			ll_personInfo_account;
	private ImageButton buttonTitleLeft;
	private TextView textViewTitle, tv_personInfo_name, tv_personInfo_account;
	private CircularImageView iv_personInfo_head;

	private String userJID, userAccount;
	private Bitmap bitmapPhotoCache, bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_profile);

		// 加载view
		initView();
		initListener();
		textViewTitle.setVisibility(View.VISIBLE);
		textViewTitle.setText("" + "Me");
	}

	private void initView() {

		buttonTitleLeft = (ImageButton) findViewById(R.id.buttonTitleLeft);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		ll_personInfo_head = (LinearLayout) findViewById(R.id.ll_personInfo_head);
		iv_personInfo_head = (CircularImageView) findViewById(R.id.iv_personInfo_head);
		ll_personInfo_name = (LinearLayout) findViewById(R.id.ll_personInfo_name);
		tv_personInfo_name = (TextView) findViewById(R.id.tv_personInfo_name);
		ll_personInfo_account = (LinearLayout) findViewById(R.id.ll_personInfo_account);
		tv_personInfo_account = (TextView) findViewById(R.id.tv_personInfo_account);

	}

	private void initListener() {

		buttonTitleLeft.setOnClickListener(this);
		ll_personInfo_head.setOnClickListener(this);
		ll_personInfo_name.setOnClickListener(this);
		ll_personInfo_account.setOnClickListener(this);
	}

	@Override
	protected void onStart() {

		super.onStart();
		userJID = InfoUtils.getUserJID(STChatApplication.getInstance());
		if (!TextUtils.isEmpty(userJID)) {
			userAccount = StringUtils.parseName(userJID);
			tv_personInfo_name.setText("" + userAccount);
			tv_personInfo_account.setText("" + userJID);
		}

		FileUtils fileUtils = new FileUtils();
		if (fileUtils.isFileExist(userAccount)) {
			// 如果有头像文件，就取出来bitmap，然后显示
			bitmap = fileUtils.getBitmapFromPathByAccount(userAccount);
			if (bitmap != null) {
				iv_personInfo_head.setImageBitmap(bitmap);
			}
			bitmap = null;
		} else {
			Toast.makeText(this, "Please set the head", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.buttonTitleLeft:
			finish();
			break;
		case R.id.ll_personInfo_head:
			showDialog();
			break;
		case R.id.ll_personInfo_name:

			break;
		case R.id.ll_personInfo_account:

			break;
		default:
			break;
		}
	}

	// 提示对话框方法
	private void showDialog() {
		new com.st.stchat.widget.AlertDialog(this).builder()
				.setTitle("Select photos")
				.setPositiveButton("Camera", new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 调用系统的拍照功能
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						// 指定调用相机拍照后照片的储存路径
						intent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(tempFile));
						startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
					}
				}).setNegativeButton("Photo", new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_PICK, null);
						intent.setDataAndType(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
						startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
					}
				}).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "requestCode  ------ " + requestCode);
		Log.e(TAG, "resultCode  ------ " + resultCode);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case PHOTO_REQUEST_TAKEPHOTO:
			startPhotoZoom(Uri.fromFile(tempFile), 180);
			break;

		case PHOTO_REQUEST_GALLERY:
			if (data != null) {
				startPhotoZoom(data.getData(), 180);
			}
			break;

		case PHOTO_REQUEST_CUT:
			// 剪裁之后，进行本地保存，然后将本地保存，保存成功后将图片进行异步上传，如果上传成功则本地设置头像。
			if (data != null) {
				saveAndSetPicToView(data);
			} else {
				// Toast.makeText(getActivity(), "Making head failure",
				// Toast.LENGTH_SHORT)
				// .show();//没有选择头像
			}

			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startPhotoZoom(Uri uri, int size) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// crop为true是设置在开启的intent中设置显示的view可以剪裁
		intent.putExtra("crop", "true");

		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		// outputX,outputY 是剪裁图片的宽高
		intent.putExtra("outputX", size);
		intent.putExtra("outputY", size);
		intent.putExtra("return-data", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

	// 将进行剪裁后的图片显示到UI界面上
	private void saveAndSetPicToView(Intent picdata) {
		Bundle bundle = picdata.getExtras();
		if (bundle != null) {
			bitmapPhotoCache = bundle.getParcelable("data");
			task = new BaseAsyncTask(this, this, SETPHOTO);
			task.setDialogMessage("Lodding....");
			task.execute();

		} else {
			Toast.makeText(PersonalProfileActivity.this,
					"The interception of avatar fail", Toast.LENGTH_SHORT)
					.show();// 截取头像失败
		}
	}

	// 使用系统当前日期加以调整作为照片的名称
	@SuppressLint("SimpleDateFormat")
	private String getPhotoFileNameByTime() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}

	@Override
	public void doAsyncTaskBefore(int taskId) {
	}

	@Override
	public Object doAsyncTaskIn(int taskId, Object... params) {
		String strResult = "-100";
		String strSave = "-100";

		if (!XmppConnectionServer.getInstance().isConn()) {
			strResult = "-5";
			return strResult;
		}
		if (bitmapPhotoCache != null) {
			FileUtils fileUtils = new FileUtils();
			// 返回 0 存储成功，-1 文件或文件夹没找到 ，-2 存储失败
			// Bitmap bbb = decodeUriAsBitmap(Uri.fromFile(tempFile));
			strSave = fileUtils.saveMyBitmap(bitmapPhotoCache, userAccount);
			if (strSave.equals("0")) {
				// 如果存储成功，就将图片保存在服务器

				if (XmppConnectionServer.getInstance().isConn()) {
					bitmap = XmppConnectionServer.getInstance()
							.changeUserImage(
									new File(fileUtils
											.getImagePath(userAccount)));
					if (bitmap != null) {
						strResult = "0";
					} else {
						strResult = "-4";
					}
				} else {
					strResult = "-5";
				}

			} else {
				strResult = strSave;
			}

		} else {
			strResult = "-3";
		}

		try {
			Thread.sleep(444);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return strResult;

	}

	@Override
	public void doAsyncTaskAfter(int taskId, Object result) {
		Log.e(TAG, "ok--ok--ok--ok--ok--ok--ok--ok--ok--ok--");
		String resu = (String) result;
		if (resu.equals("0")) {
			iv_personInfo_head.setImageBitmap(bitmap);
		} else if (resu.equals("-1")) {
			Toast.makeText(PersonalProfileActivity.this, "文件或文件夹没找到",
					Toast.LENGTH_SHORT).show();
		} else if (resu.equals("-2")) {
			// IO存取失败
			Toast.makeText(PersonalProfileActivity.this, "IO Exception",
					Toast.LENGTH_SHORT).show();
		} else if (resu.equals("-3")) {
			Toast.makeText(PersonalProfileActivity.this, "没有得到截取后的图像",
					Toast.LENGTH_SHORT).show();
		} else if (resu.equals("-4")) {
			Toast.makeText(PersonalProfileActivity.this, "向服务器上传头像失败",
					Toast.LENGTH_SHORT).show();
		} else if (resu.equals("-5")) {
			// 您已掉线
			Toast.makeText(PersonalProfileActivity.this, "You have dropped",
					Toast.LENGTH_SHORT).show();
		} else if (resu.equals("-6")) {
			// 与服务器连接失败
			Toast.makeText(PersonalProfileActivity.this,
					"And the server connection failed", Toast.LENGTH_SHORT)
					.show();
		} else if (resu.equals("-7")) {
			Toast.makeText(PersonalProfileActivity.this, "当前连接不可用",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void doCancelled(int taskId) {
		bitmapPhotoCache = null;
		bitmap = null;
	}

	private Bitmap decodeUriAsBitmap(Uri uri) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(PersonalProfileActivity.this
					.getContentResolver().openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

}
