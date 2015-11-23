package com.st.stchat.adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.model.ChatRoom;
import com.st.stchat.model.CombineBitmap;
import com.st.stchat.utils.CombineBitmapUtil;
import com.st.stchat.utils.PropertiesUtil;

/**
 * 聊天室列表适配器
 * 
 * @author  
 * 
 */
public class ChatRoomListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<ChatRoom> rooms;
	private List<CombineBitmap> combineBitmaps = null;
	private boolean catalogTag = false;
	private List<String> key = null;

	public ChatRoomListAdapter(Context context, List<ChatRoom> list) {
		// TODO Auto-generated constructor stub
		this.rooms = list;
		mInflater = LayoutInflater.from(context);

		key = new ArrayList<String>();
		key.add("serverTag");
		key.add("dbTag");
	}

	/**
	 * 添加数据列表项
	 * 
	 * @param item
	 */
	public void addNewsItem(ChatRoom item) {
		rooms.add(item);
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param list
	 */
	public void updateListView(List<ChatRoom> list, boolean catalogTag) {
		this.rooms = list;
		this.catalogTag = catalogTag;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return rooms == null ? 0 : rooms.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return rooms.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// @Override
	// public boolean isEnabled(int position) {
	// // TODO Auto-generated method stub
	// if (key.contains(((ChatRoom) getItem(position)).getSource())) {
	// return false;
	// }
	// return super.isEnabled(position);
	// }

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		ChatRoom chatRoom = rooms.get(position);
		ViewHolder viewHolder = null;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = mInflater.inflate(R.layout.activity_chatroomlist_item, null);
			viewHolder.crlTitle = (TextView) view
					.findViewById(R.id.name_chatroomlist);
			viewHolder.crlLetter = (TextView) view
					.findViewById(R.id.catalog_chatroomlist);
			viewHolder.crlHead = (ImageView) view
					.findViewById(R.id.head_img_chatroomlist);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		if (key.contains(chatRoom.getSource())) {
			viewHolder.crlLetter.setVisibility(View.VISIBLE);
			if (chatRoom.getSource().equals("serverTag")) {
				viewHolder.crlLetter.setText("" + "All rooms");
			} else if (chatRoom.getSource().equals("dbTag")) {
				viewHolder.crlLetter.setText("" + "History room");
			}
		} else {
			viewHolder.crlLetter.setVisibility(View.GONE);
		}

		// 处理搜索时隐藏掉catalog
		if (catalogTag) {
			if (chatRoom.getSource().equals("serverTag")) {
				viewHolder.crlLetter.setVisibility(View.GONE);
			} else if (chatRoom.getSource().equals("dbTag")) {
				viewHolder.crlLetter.setVisibility(View.GONE);
			}
		}

		viewHolder.crlTitle.setText("" + chatRoom.getRoomJid().split("@")[0]);
		// 已存在的聊天室才会合成头像 否侧用默认的（因为只有join到该聊天室才能取到成员）
		if (chatRoom.getSource().equals("db")
				|| chatRoom.getSource().equals("dbTag")) {
			Bitmap combineBitmap = getRoomIcon(chatRoom.getRoomJid());
			if (combineBitmap != null) {
				viewHolder.crlHead.setImageBitmap(combineBitmap);
			}
		} else {
			viewHolder.crlHead.setImageResource(R.drawable.contact_chatroom);
		}

		return view;
	}

	final static class ViewHolder {
		ImageView crlHead;
		TextView crlLetter;
		TextView crlTitle;
	}

	private Bitmap getRoomIcon(final String roomTitle) {
		Bitmap combineBitmap = null;
		if (STChatApplication.imageCache.containsKey(roomTitle)) {
			SoftReference<Bitmap> reference = STChatApplication.imageCache
					.get(roomTitle);
			combineBitmap = reference.get();
			if (combineBitmap != null) {
				return combineBitmap;
			}
		}
		List<String> list = CombineBitmapUtil.getRoomMembers(roomTitle);
		Bitmap[] bitmaps = null;
		if (list == null || list.size() <= 1) {
			return null;
		}
		if (list.size() > 9) {
			List<String> newList = new ArrayList<String>();
			for (int i = 0; i < 9; i++) {
				newList.add(list.get(i));
			}
			combineBitmaps = getBitmapEntitys(newList.size());
			bitmaps = CombineBitmapUtil.getBitmaps(newList, combineBitmaps);
		} else {
			combineBitmaps = getBitmapEntitys(list.size());
			bitmaps = CombineBitmapUtil.getBitmaps(list, combineBitmaps);
		}
		combineBitmap = CombineBitmapUtil.getCombineBitmaps(combineBitmaps,
				bitmaps);
		// 缓存
		STChatApplication.imageCache.put(roomTitle, new SoftReference<Bitmap>(
				combineBitmap));

		return combineBitmap;
	}

	private List<CombineBitmap> getBitmapEntitys(int count) {
		List<CombineBitmap> mList = new ArrayList<CombineBitmap>();
		String value = PropertiesUtil.readData(STChatApplication.getInstance(),
				String.valueOf(count), R.raw.data);
		String[] arr1 = value.split(";");
		int length = arr1.length;
		for (int i = 0; i < length; i++) {
			String content = arr1[i];
			String[] arr2 = content.split(",");
			CombineBitmap entity = null;
			for (int j = 0; j < arr2.length; j++) {
				entity = new CombineBitmap();
				entity.x = Float.valueOf(arr2[0]);
				entity.y = Float.valueOf(arr2[1]);
				entity.width = Float.valueOf(arr2[2]);
				entity.height = Float.valueOf(arr2[3]);
			}
			mList.add(entity);
		}
		return mList;
	}
}
