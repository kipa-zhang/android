package com.st.stchat.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.st.stchat.AsyncBitmapLoader;
import com.st.stchat.AsyncBitmapLoader.ImageCallBack;
import com.st.stchat.R;

public class SortAdapter extends BaseAdapter implements SectionIndexer {
	private List<SortModel> list = null;
	private Context mContext;

	// private Bitmap bitmap;
	private AsyncBitmapLoader asyncLoader = null;

	public SortAdapter(Context mContext, List<SortModel> list) {
		this.mContext = mContext;
		this.list = list;
		asyncLoader = new AsyncBitmapLoader();
	}

	public void updateListView(List<SortModel> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final SortModel mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(
					R.layout.fragment_contact_item, null);
			viewHolder.tvTitle = (TextView) view
					.findViewById(R.id.contact_name);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.tvHead = (ImageView) view.findViewById(R.id.head_img);
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

		viewHolder.tvTitle.setText(""
				+ this.list.get(position).getName().split("@")[0]);

		// AsyncBitmapLoader asyncLoader = new AsyncBitmapLoader();
		Bitmap bitmap = asyncLoader.loadBitmap(viewHolder.tvHead, this.list
				.get(position).getName().split("@")[0], new ImageCallBack() {
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

	final static class ViewHolder {
		ImageView tvHead;
		TextView tvLetter;
		TextView tvTitle;
	}

	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
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