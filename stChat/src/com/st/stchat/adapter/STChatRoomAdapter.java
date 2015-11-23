package com.st.stchat.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.message.STChatMessage;
import com.st.stchat.utils.FileUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.TimeUtil;

/**
 * 
 * @author juwei 2014.11.27
 * 
 */
public class STChatRoomAdapter extends BaseAdapter {

	private Context mContext;
	private List<STChatMessage> mData;
	FrameLayout fl_content_to;

	public STChatRoomAdapter(Context context, List<STChatMessage> data) {
		this.mContext = context;
		this.mData = data;
	}

	public void Refresh() {
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int Index) {
		return mData.get(Index);
	}

	@Override
	public long getItemId(int Index) {
		return Index;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int Index, View mView, ViewGroup mParent) {
		TextView Content, FromName, tv_time;
		ImageView ToHead, FromHead;
		Bitmap bitmap;
		switch (mData.get(Index).getType()) {
		case STChatMessage.MessageType_Time:
			mView = LayoutInflater.from(mContext).inflate(
					R.layout.message_single_send_time, null);
			Content = (TextView) mView.findViewById(R.id.Time);
			Content.setText("" + mData.get(Index).getContent());
			break;
		case STChatMessage.MessageType_From:
			mView = LayoutInflater.from(mContext).inflate(
					R.layout.message_group_receive, null);
			Content = (TextView) mView.findViewById(R.id.From_Content);
			FromName = (TextView) mView.findViewById(R.id.From_Name);
			FromHead = (ImageView) mView.findViewById(R.id.From_Header);
			tv_time = (TextView) mView.findViewById(R.id.tv_time);
			bitmap = getHeadByName(mData.get(Index).getfrom());
			if (bitmap != null) {
				FromHead.setImageBitmap(bitmap);
			}
			FromName.setText("" + mData.get(Index).getfrom());
			if (mData.get(Index).getContent().length() < (11 + 1)) { // (Content.getMaxEms()+1)
				Content.setText("" + mData.get(Index).getContent() + "        ");
			} else {

				Content.setText("" + mData.get(Index).getContent() + "\n");
			}
			tv_time.setText(""
					+ TimeUtil.millisToData(5, mData.get(Index).getTime()));

			break;
		case STChatMessage.MessageType_To:
			mView = LayoutInflater.from(mContext).inflate(
					R.layout.message_single_send, null);
			Content = (TextView) mView.findViewById(R.id.To_Content);
			ToHead = (ImageView) mView.findViewById(R.id.To_Header);
			tv_time = (TextView) mView.findViewById(R.id.tv_time);
			fl_content_to = (FrameLayout) mView
					.findViewById(R.id.fl_content_to);
			bitmap = getHeadByName(InfoUtils.getUser(STChatApplication
					.getInstance()));
			fl_content_to.setBackgroundResource(R.drawable.textview_style_send);
			if (bitmap != null) {
				ToHead.setImageBitmap(bitmap);
			}
			if (mData.get(Index).getContent().length() < (11 + 1)) { // (Content.getMaxEms()+1)
				Content.setText("" + mData.get(Index).getContent() + "        ");
			} else {

				Content.setText("" + mData.get(Index).getContent() + "\n");
			}
			tv_time.setText(""
					+ TimeUtil.millisToData(5, mData.get(Index).getTime()));
			break;
		}
		return mView;
	}

	private Bitmap getHeadByName(String name) {
		Bitmap bitmap;
		FileUtils fileUtils = new FileUtils();
		if (fileUtils.isFileExist(name)) {
			// 如果有头像文件，就取出来bitmap，然后显示
			bitmap = fileUtils.getBitmapFromPathByAccount(name);
		} else {
			bitmap = null;
		}
		return bitmap;
	}

}
