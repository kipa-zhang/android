package com.st.stchat.receiver;

import com.st.stchat.STChatApplication;
import com.st.stchat.activity.MainActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class BootReceiver extends BroadcastReceiver {
	private PendingIntent mAlarmSender;

	@Override
	public void onReceive(Context context, Intent intent) {
		// 在这里干你想干的事（启动一个Service，Activity等），本例是启动一个定时调度程序，每30分钟启动一个Service去更新数据
		// mAlarmSender = PendingIntent.getService(context, 0, new
		// Intent(context,
		// RefreshDataService.class), 0);
		// long firstTime = SystemClock.elapsedRealtime();
		// AlarmManager am = (AlarmManager) context
		// .getSystemService(Activity.ALARM_SERVICE);
		// am.cancel(mAlarmSender);
		// am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
		// 30 * 60 * 1000, mAlarmSender);
		// 启动一个登陆异步，当手机开机后执行软件的默认登陆
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int i = 0;
				while (i < 1000) {
					System.out.println("自动登陆");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					i++;
				}

			}
		}).start();

		// Intent intent1 = new Intent(STChatApplication.getInstance(),
		// MainActivity.class);
		// STChatApplication.getInstance().startActivity(intent1);
	}
}
