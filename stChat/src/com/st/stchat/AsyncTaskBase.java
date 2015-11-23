package com.st.stchat;

import android.os.AsyncTask;
import android.view.View;

import com.st.stchat.view.LoadingView;

/**
 * 异步操作 子线程里进行UI操作
 * 
 * @author  
 * 
 */
public class AsyncTaskBase extends AsyncTask<Integer, Integer, Integer> {
	private LoadingView mLoadingView;

	public AsyncTaskBase(LoadingView loadingView) {
		this.mLoadingView = loadingView;
	}

	@Override
	protected Integer doInBackground(Integer... params) {

		return null;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (result == 1) {
			mLoadingView.setVisibility(View.GONE);
		} else {
			// mLoadingView.setImageVisible();
			mLoadingView.setVisibility(View.VISIBLE);
			mLoadingView.setText(R.string.no_data);
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// mLoadingView.setImageGone();
		mLoadingView.setVisibility(View.VISIBLE);
	}

}
