package com.st.stchat.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.stchat.AsyncBitmapLoader;
import com.st.stchat.AsyncBitmapLoader.ImageCallBack;
import com.st.stchat.R;
import com.st.stchat.model.Notice;

public class NoticeAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Notice> inviteNotices;
	private Context context;
	private AsyncBitmapLoader asyncLoader = null;

	public NoticeAdapter(Context context, List<Notice> inviteUsers) {
		this.context = context;
		mInflater = LayoutInflater.from(context);
		this.inviteNotices = inviteUsers;
		this.asyncLoader = new AsyncBitmapLoader();
	}

	public void setNoticeList(List<Notice> inviteUsers) {
		this.inviteNotices = inviteUsers;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return inviteNotices == null ? 0 : inviteNotices.size();
	}

	@Override
	public Object getItem(int position) {
		return inviteNotices.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Notice notice = inviteNotices.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_new_friend_item,
					null);
			holder = new ViewHolder();
			holder.contactName = (TextView) convertView
					.findViewById(R.id.newContactName);
			holder.newContent = (TextView) convertView
					.findViewById(R.id.content);
			holder.contactIcon = (ImageView) convertView
					.findViewById(R.id.newFriendHead);
			holder.paopao = (TextView) convertView
					.findViewById(R.id.unread_user_num);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (notice.getNoticeType() == Notice.ADD_FRIEND) {// 添加好友未处理
			holder.newContent.setText("" + notice.getContent());
		}

		Bitmap bitmap = asyncLoader.loadBitmap(holder.contactIcon,
				notice.getTitle(), new ImageCallBack() {
					@Override
					public void imageLoad(ImageView imageView, Bitmap bitmap) {
						// TODO Auto-generated method stub
						if (bitmap != null) {
							imageView.setImageBitmap(bitmap);
						} else {
							imageView
									.setImageResource(R.drawable.default_nor_man);
						}
					}
				});
		if (bitmap != null) {
			holder.contactIcon.setImageBitmap(bitmap);
		}

		holder.contactName.setText("" + notice.getTitle());
		holder.newContent.setTag(notice);
		if (Notice.UNREAD == notice.getStatus()) {
			holder.paopao.setText("" + "1");
			holder.paopao.setVisibility(View.VISIBLE);
		} else {
			holder.paopao.setVisibility(View.GONE);
		}

		return convertView;
	}

	private class ViewHolder {
		public ImageView contactIcon;
		public TextView contactName;
		public TextView newContent;
		public TextView paopao;
	}

}
