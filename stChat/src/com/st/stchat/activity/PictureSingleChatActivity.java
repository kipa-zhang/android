package com.st.stchat.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.st.stchat.R;

public class PictureSingleChatActivity extends BaseActivity {
	private ImageView iv_pic;

	// private DisplayImageOptions options;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// options = new DisplayImageOptions.Builder()
		// .showImageOnLoading(R.drawable.ic_launcher)
		// .showImageForEmptyUri(R.drawable.ic_launcher)
		// .showImageOnFail(R.drawable.ic_launcher)
		// .cacheInMemory(true)
		// .cacheOnDisk(true)
		// .considerExifParams(true)
		// .displayer(new RoundedBitmapDisplayer(20))
		// .build();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_single_chat);
		String picPath = getIntent().getStringExtra("picPath");
		iv_pic = (ImageView) findViewById(R.id.iv_pic);
		iv_pic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		// Bitmap bm = BitmapFactory.decodeFile(picPath);
		// iv_pic.setImageBitmap(bm);
		ImageLoader.getInstance().displayImage(
				"file://" + getIntent().getStringExtra("picPath"), iv_pic);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().clearDiskCache();

		super.onDestroy();
	}
}
