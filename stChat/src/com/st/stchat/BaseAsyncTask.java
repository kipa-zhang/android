package com.st.stchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * 异步类
 * 
 * @author juwei 2014.11.20
 * 
 */
public class BaseAsyncTask extends AsyncTask<Object, Void, Object> {

	private static final String TAG = "BaseAsyncTask";

	private static final int DEFAULT_TASK_ID = 10000;

	private int mTaskId;

	private ProgressDialog progressDialog;

	private BaseAsyncTaskListener taskListener;
	private LayoutInflater inflater;
	private TextView titleView;
	private TextView messageView;
	private boolean showDialog;

	public BaseAsyncTask(Context context, BaseAsyncTaskListener listener) {
		this(context, listener, DEFAULT_TASK_ID);
	}

	public BaseAsyncTask(Context context, BaseAsyncTaskListener listener,
			boolean showDialog) {
		this(context, listener, DEFAULT_TASK_ID, showDialog);
	}

	public BaseAsyncTask(Context context, BaseAsyncTaskListener listener,
			int defaultTaskId, boolean showDialog) {
		this.showDialog = showDialog;
		this.taskListener = listener;
		this.mTaskId = defaultTaskId;
		if (showDialog) {
			inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.common_dialog_layout, null);
			titleView = (TextView) v.findViewById(R.id.progressDialogTitle);
			titleView.setVisibility(View.GONE);
			messageView = (TextView) v.findViewById(R.id.progressDialogMessage);
			messageView.setVisibility(View.GONE);
			progressDialog = new ProgressDialog(context);
			progressDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕失效
			// progressDialog.setCancelable(false);// 设置右下角返回失效
			progressDialog.show();
			progressDialog.setContentView(v);
		}
	}

	public BaseAsyncTask(Context context, BaseAsyncTaskListener listener,
			int defaultTaskId) {
		this(context, listener, defaultTaskId, true);
	}

	public void setDialogTitle(String title) {
		if (showDialog) {
			titleView.setText("" + title);
			titleView.setVisibility(View.VISIBLE);
		}

	}

	public void setDialogMessage(String message) {
		if (showDialog) {
			messageView.setText("" + message);
			messageView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onPreExecute() {
		this.taskListener.doAsyncTaskBefore(this.mTaskId);
	}

	@Override
	protected Object doInBackground(Object... params) {
		return this.taskListener.doAsyncTaskIn(mTaskId, params);
	}

	@Override
	protected void onPostExecute(Object result) {
		this.taskListener.doAsyncTaskAfter(this.mTaskId, result);
		if (showDialog) {
			progressDialog.dismiss();
		}
	}

	@Override
	protected void onCancelled() {
		this.taskListener.doCancelled(this.mTaskId);
		if (showDialog) {
			progressDialog.dismiss();
		}
	}

	public interface BaseAsyncTaskListener {
		public void doAsyncTaskBefore(int taskId);

		public Object doAsyncTaskIn(int taskId, Object... params);

		public void doAsyncTaskAfter(int taskId, Object result);

		public void doCancelled(int taskId);
	}

}
