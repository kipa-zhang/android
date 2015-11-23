package com.st.stchat.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import com.st.stchat.R;

public class NotificationUtil {

	private static MediaPlayer mp;

	/**
	 * 
	 * 发出Notification的method.
	 * 
	 * @param iconId
	 *            图标
	 * @param contentTitle
	 *            标题
	 * @param contentText
	 *            你内容
	 * @param activity
	 * 
	 * 
	 */
	@SuppressWarnings("deprecation")
	public static void setNotiType(NotificationManager manager,
			Context context, int iconId, String contentTitle,
			String contentText, Class activity, String from, String noticeType) {

		/*
		 * 创建新的Intent，作为点击Notification留言条时， 会运行的Activity
		 */
		Intent notifyIntent = new Intent(context, activity);
		// notifyIntent.putExtra("to", from);
		String[] to = new String[] { "contact", from };
		notifyIntent.putExtra("give_chat", to);
		// notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		/* 创建PendingIntent作为设置递延运行的Activity */
		PendingIntent appIntent = PendingIntent.getActivity(context, 0,
				notifyIntent, 0);

		/* 创建Notication，并设置相关参数 */
		Notification myNoti = new Notification();
		// 点击自动消失
		myNoti.flags = Notification.FLAG_AUTO_CANCEL;
		/* 设置statusbar显示的icon */
		myNoti.icon = iconId;
		/* 设置statusbar显示的文字信息 */
		myNoti.tickerText = contentTitle;

		if (noticeType.equals("chat")) {

			// contentText = "one message from " + from.split("@")[0] + ": "
			// + contentText;
			contentText = "" + from.split("@")[0] + ": " + contentText;
			/* 设置notification发生时同时发出默认声音 */
			mp = MediaPlayer.create(context, R.raw.ingroup);

		}
		if (noticeType.equals("roster")) {
			myNoti.setLatestEventInfo(context, contentTitle, contentText,
					appIntent);
			mp = MediaPlayer.create(context, R.raw.outgroup);
		}
		mp.start();
		/* 设置Notification留言条的参数 */
		myNoti.setLatestEventInfo(context, contentTitle, contentText, appIntent);
		/* 送出Notification */
		manager.notify(0, myNoti);
		// mp.release();
	}
}
