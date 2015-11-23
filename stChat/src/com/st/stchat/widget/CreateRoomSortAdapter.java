package com.st.stchat.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.st.stchat.AsyncBitmapLoader;
import com.st.stchat.AsyncBitmapLoader.ImageCallBack;
import com.st.stchat.R;

public class CreateRoomSortAdapter extends BaseAdapter implements
		SectionIndexer {
	private List<Map<String, SortModel>> mData;
	public static HashMap<Integer, Boolean> isSelected;
	private Context mContext;
	private AsyncBitmapLoader asyncLoader = null;

	public CreateRoomSortAdapter(Context mContext,
			List<Map<String, SortModel>> mData) {
		this.mContext = mContext;
		this.mData = mData;
		isSelected = new HashMap<Integer, Boolean>();
		this.asyncLoader = new AsyncBitmapLoader();
		initDate();
	}

	private void initDate() {
		for (int i = 0; i < mData.size(); i++) {
			getIsSelected().put(i, false);
		}
	}

	// public void updateListView(List<SortModel> list) {
	// this.list = list;
	// notifyDataSetChanged();
	// }

	public int getCount() {
		return this.mData.size();
	}

	public Object getItem(int position) {
		return mData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final SortModel mContent = mData.get(position).get("SM");
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(
					R.layout.choose_friend_list_item, null);
			viewHolder.tvTitle = (TextView) view
					.findViewById(R.id.contact_name);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.tvHead = (ImageView) view.findViewById(R.id.head_img);
			viewHolder.cBox = (CheckBox) view.findViewById(R.id.cb);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		int section = getSectionForPosition(position);

		if (position == getPositionForSection(section)) {
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText("" + mContent.getSortLetters());
		} else {
			viewHolder.tvLetter.setVisibility(View.GONE);
		}

		viewHolder.tvTitle.setText("" + mContent.getName().split("@")[0]);
		viewHolder.cBox.setChecked(isSelected.get(position));
		Bitmap bitmap = asyncLoader.loadBitmap(viewHolder.tvHead, mContent
				.getName().split("@")[0], new ImageCallBack() {
			@Override
			public void imageLoad(ImageView imageView, Bitmap bitmap) {
				// TODO Auto-generated method stub
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
				} else {
					imageView.setImageResource(R.drawable.default_nor_man);
				}
			}
		});
		if (bitmap != null) {
			viewHolder.tvHead.setImageBitmap(bitmap);
		}

		return view;

	}

	public final static class ViewHolder {
		public ImageView tvHead;
		public TextView tvLetter;
		public TextView tvTitle;
		public CheckBox cBox;
	}

	public static HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}

	public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
		CreateRoomSortAdapter.isSelected = isSelected;
	}

	public int getSectionForPosition(int position) {
		return mData.get(position).get("SM").getSortLetters().charAt(0);
	}

	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = mData.get(i).get("SM").getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}