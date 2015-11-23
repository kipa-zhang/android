package com.st.stchat;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.st.stchat.bean.MessageItem;
import com.st.stchat.db.ChatDBHelper;
import com.st.stchat.utils.Logger;

/**
 * 
 * @author juwei 2014.11.20 (juwei007[at]vip[dot]qq[dot]com)
 * 
 */
public class STChatApplication extends Application {
	private static final String TAG = "STChatApplication";
	/**
	 * 整个应用程序的context,可以通过STChatApplication.getInstance()获得
	 */
	private static STChatApplication mInstance = null;
	/**
	 * Activity的管理集合
	 */
	private List<Activity> activities = new ArrayList<Activity>();
	/**
	 * 当前最顶端Activity
	 */
	private List<Activity> activitiesTop = new ArrayList<Activity>();

	/**
	 * 定义一个全局的内存图片软引用缓冲，用于头像
	 */
	public static HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
	/**
	 * 定义一个全局的内存MessageFragment消息
	 */
	public static HashMap<String, MessageItem> messageCache = new HashMap<String, MessageItem>();

	public static boolean firstLoadMessageTag = true;
	/**
	 * 数据库路径文件夹
	 */
	public static final String DATABASE_DIR_NAME = "databases";

	/**
	 * 数据库的名称
	 */
	public final static String DATABASE_NAME = "stchat.db";

	/**
	 * 当前数据库版本
	 */
	public static int DATABASE_VERSION = 1;
	/**
	 * 消息库名称
	 */
	public final static String CHATDATABASE_NAME = "stmessages.db";

	/**
	 * 消息库版本
	 */
	public static int CHATDATABASE_VERSION = 1;

	@Override
	public void onCreate() {

		Logger.i(TAG, "Application----------run");
		// if (ConstantUtils.Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >=
		// Build.VERSION_CODES.GINGERBREAD) {
		// Log.i(TAG, "开启Debug模式");
		// StrictMode.setThreadPolicy(new
		// StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
		// StrictMode.setVmPolicy(new
		// StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
		// }
		super.onCreate();
		mInstance = this;

		initImageLoader(getApplicationContext());

		// 测试数据库
		testDB();
	}

	public void addActivity(Activity activity) {
		if (!activities.contains(activity)) {
			activities.add(activity);
		}
	}

	public void addActivityTop(Activity activity) {
		if (!activitiesTop.contains(activity)) {
			activitiesTop.add(activity);
		}
	}

	public void removeActivityTop(Activity activity) {
		activitiesTop.remove(activity);
	}

	public static STChatApplication getInstance() {
		return mInstance;
	}

	public String getAPPVersionCode() {
		String version = "1";
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			version = packInfo.versionCode + "";
		} catch (NameNotFoundException e) {

		}
		return version;
	}

	public String getAPPVersionName() {
		String version = "1.0.0";
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			version = packInfo.versionName;
		} catch (NameNotFoundException e) {

		}
		return version;
	}

	@Override
	public void onTerminate() {

		super.onTerminate();
		System.out.println("-----------------onTerminate");
		clearActivitys();
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
				.cancelAll();
		activities = null;
		System.exit(0);
	}

	public void clearActivitys() {
		for (Activity activity : activities) {
			if (activity != null) {
				Logger.i(TAG + "关闭activity", activity.toString());
				activity.finish();
			}
		}
	}

	public void clearActivitys(Activity exceptActiv) {
		for (Activity activity : activities) {
			if (activity != null) {

				if (exceptActiv != activity) {
					Logger.i(TAG + "注销activity", activity.toString());
					activity.finish();
				}

			}
		}
	}

	// public void closeActivity(Activity activ) {
	// for (Activity activity : activities) {
	// if (activity != null) {
	//
	// if (activ == activity) {
	// Logger.i(TAG + "关闭activity", activity.toString());
	// activity.finish();
	// }
	//
	// }
	// }
	// }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
	}

	SQLiteDatabase sqlDB;
	Cursor cursor;

	/**
	 * DB测试代码
	 */
	public void testDB() {
		Log.d(TAG, "开始测试数据库 ...");
		try {
			sqlDB = ChatDBHelper.getChatDBHelper().getReadableDatabase();
			cursor = sqlDB.rawQuery(
					"select DISTINCT chatAccount from chatmessages_table ",
					null);
			if (cursor == null) {
				return;
			}
			while (cursor.moveToNext()) {
				Log.d(TAG,
						"db -- accountList: "
								+ cursor.getString(cursor
										.getColumnIndex("chatAccount")));
			}
		} catch (Exception e) {
			Log.e(TAG, "数据库测试出错");
		} finally {
			cursor.close();
			sqlDB.close();
		}
	}

	/**
	 * 初始化ImageLoader
	 * 
	 * @param context
	 */
	public static void initImageLoader(Context context) {

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
				// 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
}
