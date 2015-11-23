package com.st.stchat.adapter;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.activity.PictureSingleChatActivity;
import com.st.stchat.message.STChatMessage;
import com.st.stchat.utils.ConstantUtils;
import com.st.stchat.utils.FileUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.utils.TimeUtil;

/**
 * 
 * @author juwei 2014.11.27
 * 
 */
@SuppressLint({ "NewApi", "View ", "InflateParams" })
public class STChatAdapter extends BaseAdapter {
	private static final String TAG = "STChatAdapter";
	private Context mContext;
	private List<STChatMessage> mData;
	private LayoutInflater mInflater;
	private TextView tv_name, tv_content, tv_time, tv_statu;
	private ImageView iv_tohead, iv_fromhead, iv_pic_receive, iv_pic_send;

	FrameLayout fl_content_receive, fl_content_to;
	Bitmap bitmap = null;

	// private DisplayImageOptions options;
	// private ImageLoadingListener animateFirstListener = new
	// AnimateFirstDisplayListener();
	public STChatAdapter(Context context, List<STChatMessage> data) {
		this.mContext = context;
		this.mData = data;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// options = new DisplayImageOptions.Builder()
		// .showImageOnLoading(R.drawable.ic_launcher)
		// .showImageForEmptyUri(R.drawable.ic_launcher)
		// .showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true)
		// .cacheOnDisk(true).considerExifParams(true)
		// .displayer(new RoundedBitmapDisplayer(20)).build();

	}

	public String getPercent(long x, long total) {
		String result = "";// 接受百分比的值
		double x_double = x * 1.0;
		double tempresult = x / total;
		// NumberFormat nf = NumberFormat.getPercentInstance(); 注释掉的也是一种方法
		// nf.setMinimumFractionDigits( 2 ); 保留到小数点后几位
		DecimalFormat df1 = new DecimalFormat("0.00%"); // ##.00%
														// 百分比格式，后面不足2位的用0补齐
		// result=nf.format(tempresult);
		result = df1.format(tempresult);
		return result;
	}

	public void Refresh() {
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		long totalMem = Runtime.getRuntime().totalMemory();
		long maxMem = Runtime.getRuntime().maxMemory();
		long freeMem = Runtime.getRuntime().freeMemory();

		Log.e(TAG, "总内存为：" + totalMem);
		Log.e(TAG, "最大内存为：" + maxMem);
		Log.e(TAG, "空闲内存为：" + freeMem);
		Log.e(TAG, "百分比为：" + getPercent(freeMem, maxMem));
		if (mData == null) {
			return convertView;
		}
		if (mData.get(position) == null) {
			return convertView;
		}

		switch (mData.get(position).getType()) {
		case STChatMessage.MessageType_Time:
			convertView = mInflater.inflate(R.layout.message_single_send_time,
					null);

			tv_content = (TextView) convertView.findViewById(R.id.Time);

			tv_content.setText("" + mData.get(position).getContent());
			break;
		case STChatMessage.MessageType_From:

			convertView = mInflater.inflate(R.layout.message_single_receive,
					null);

			tv_content = (TextView) convertView.findViewById(R.id.From_Content);
			tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			tv_name = (TextView) convertView.findViewById(R.id.From_Name);
			iv_fromhead = (ImageView) convertView
					.findViewById(R.id.From_Header);
			fl_content_receive = (FrameLayout) convertView
					.findViewById(R.id.fl_content_receive);
			iv_pic_receive = (ImageView) convertView
					.findViewById(R.id.iv_pic_receive);

			bitmap = getHeadByName(mData.get(position).getfrom());

			if (bitmap != null) {
				iv_fromhead.setImageBitmap(bitmap);
			}
			tv_name.setText(mData.get(position).getfrom());
			if (mData.get(position).getStyle()
					.equals(STChatMessage.MessageStyle_Text)) {
				iv_pic_receive.setVisibility(View.GONE);
				iv_pic_receive = null;
				tv_content.setVisibility(View.VISIBLE);
				fl_content_receive
						.setBackgroundResource(R.drawable.textview_style_receive);
				// System.out.println("———————— 长度为："+mData.get(position).getContent().length());
				if (mData.get(position).getContent().length() < (11 + 1)) { // (Content.getMaxEms()+1)
					tv_content.setText("" + mData.get(position).getContent()
							+ "        ");
				} else {

					tv_content.setText("" + mData.get(position).getContent()
							+ "\n");
				}

			} else if (mData.get(position).getStyle()
					.equals(STChatMessage.MessageStyle_Pic)) {
				// fl_content_receive.setBackground(null);
				fl_content_receive.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						// 打开当前图片的大图模式 mData.get(position).getPicPath()
						Intent intent = new Intent(mContext,
								PictureSingleChatActivity.class);
						intent.putExtra("picPath", mData.get(position)
								.getPicPath());
						mContext.startActivity(intent);
					}
				});
				tv_content.setVisibility(View.GONE);
				tv_content = null;
				iv_pic_receive.setVisibility(View.VISIBLE);
				tv_time.setVisibility(View.INVISIBLE);

				ImageLoader.getInstance().displayImage(
						"file://" + mData.get(position).getPicPath(),
						iv_pic_receive);

				System.out.println("getView图片路径为： "
						+ mData.get(position).getPicPath());
				// Bitmap bm = picLoader.showCacheBitmap(mData.get(position)
				// .getPicPath());
				// Bitmap bm = BitmapFactory.decodeFile(mData.get(position)
				// .getPicPath());
				// if (bm != null) {
				// iv_pic_receive.setImageBitmap(bm);
				// } else {
				// iv_pic_receive.setImageDrawable(mContext.getResources()
				// .getDrawable(R.drawable.ic_launcher));
				// }

			}

			tv_time.setText("" + "\n"
					+ TimeUtil.millisToData(5, mData.get(position).getTime()));
			break;
		case STChatMessage.MessageType_To:
			convertView = mInflater.inflate(R.layout.message_single_send, null);
			tv_content = (TextView) convertView.findViewById(R.id.To_Content);
			tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			iv_tohead = (ImageView) convertView.findViewById(R.id.To_Header);
			tv_statu = (TextView) convertView.findViewById(R.id.tv_statu);
			fl_content_to = (FrameLayout) convertView
					.findViewById(R.id.fl_content_to);
			iv_pic_send = (ImageView) convertView
					.findViewById(R.id.iv_pic_send);
			bitmap = getHeadByName(InfoUtils.getUser(STChatApplication
					.getInstance()));
			if (bitmap != null) {
				iv_tohead.setImageBitmap(bitmap);
			}
			if (mData.get(position).getStyle()
					.equals(STChatMessage.MessageStyle_Text)) {
				iv_pic_send.setVisibility(View.GONE);
				iv_pic_send = null;
				tv_content.setVisibility(View.VISIBLE);
				tv_statu.setVisibility(View.VISIBLE);
				fl_content_to
						.setBackgroundResource(R.drawable.textview_style_send);
				if (mData.get(position).getContent().length() < (11 + 1)) { // (Content.getMaxEms()+1)
					tv_content.setText("" + mData.get(position).getContent()
							+ "        ");
				} else {

					tv_content.setText("" + mData.get(position).getContent()
							+ "\n");
				}
				if (mData.get(position).getStatus()
						.equals(ConstantUtils.MESSAGE_SEND_SSSS)) {
					// 正在发送
					tv_statu.setText("" + "Sending");
					tv_statu.setTextColor(android.graphics.Color
							.parseColor("#CBBF00"));
					// tv_statu.setBackgroundColor(android.graphics.Color
					// .parseColor("#CBBF00"));

				} else if (mData.get(position).getStatus()
						.equals(ConstantUtils.MESSAGE_SEND_SUCCESS)) {
					// 已发送
					tv_statu.setText("" + "Sent");
					tv_statu.setTextColor(android.graphics.Color
							.parseColor("#41AED7"));
					// tv_statu.setBackgroundColor(android.graphics.Color
					// .parseColor("#41AED7"));
				} else if (mData.get(position).getStatus()
						.equals(ConstantUtils.MESSAGE_SEND_HASRECEIVED)) {
					// 已接收
					tv_statu.setText("Received");
					tv_statu.setTextColor(android.graphics.Color
							.parseColor("#85CA23"));
					// tv_statu.setBackgroundColor(android.graphics.Color
					// .parseColor("#85CA23"));
				} else if (mData.get(position).getStatus()
						.equals(ConstantUtils.MESSAGE_SEND_ERROR)) {
					// 发送失败
					tv_statu.setText("" + "Send failed");
					tv_statu.setTextColor(android.graphics.Color
							.parseColor("#F4494E"));
					// tv_statu.setBackgroundColor(android.graphics.Color
					// .parseColor("#F4494E"));
				}
			} else if (mData.get(position).getStyle()
					.equals(STChatMessage.MessageStyle_Pic)) {
				// fl_content_to.setBackground(null);
				fl_content_to.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						// 打开当前图片的大图模式 mData.get(position).getPicPath()
						Intent intent = new Intent(mContext,
								PictureSingleChatActivity.class);
						intent.putExtra("picPath", mData.get(position)
								.getPicPath());
						mContext.startActivity(intent);

					}
				});

				tv_statu.setVisibility(View.GONE);
				tv_statu = null;
				tv_content.setVisibility(View.GONE);
				tv_content = null;
				iv_pic_send.setVisibility(View.VISIBLE);
				tv_time.setVisibility(View.INVISIBLE);
				ImageLoader.getInstance().displayImage(
						"file://" + mData.get(position).getPicPath(),
						iv_pic_send);
				System.out.println("getView图片路径为： "
						+ mData.get(position).getPicPath());
				// Bitmap bm = picLoader.showCacheBitmap(mData.get(position)
				// .getPicPath());
				// Bitmap bm = BitmapFactory.decodeFile(mData.get(position)
				// .getPicPath());
				// if (bm != null) {
				// iv_pic_send.setImageBitmap(bm);
				// } else {
				// iv_pic_send.setImageDrawable(mContext.getResources()
				// .getDrawable(R.drawable.ic_launcher));
				// }

			}
			tv_time.setText("" + "\n"
					+ TimeUtil.millisToData(5, mData.get(position).getTime()));
			break;

		}
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap = null;
		}
		return convertView;
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

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

}
