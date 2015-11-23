package com.st.stchat;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.st.stchat.utils.FileUtils;
import com.st.stchat.utils.InfoUtils;
import com.st.stchat.xmpp.XmppConnectionServer;

/**
 * 加载图片的时候，去缓冲区查找，如果有的话，则立刻返回Bitmap对象
 * 
 * @author  
 * 
 */
public class AsyncBitmapLoader {
	private static final String TAG = "AsyncBitmapLoader";
	private FileUtils fileUtils = new FileUtils();
	/**
	 * 内存图片软引用缓冲
	 */
	HashMap<String, SoftReference<Bitmap>> imageCache = STChatApplication.imageCache;

	public Bitmap loadBitmap(final ImageView imageView, final String imageURL,
			final ImageCallBack imageCallBack) {
		// 在内存缓存中，则返回Bitmap对象
		if (imageCache.containsKey(imageURL)) {
			// System.out.println("contact头像进去本地内存缓存中查找:" + imageURL);
			SoftReference<Bitmap> reference = imageCache.get(imageURL);
			Bitmap bitmap = reference.get();
			if (bitmap != null) {
				return bitmap;
			}
		} else {
			// /**
			// * 加上一个对本地缓存的查找
			// */
			// Log.d(TAG, "contact头像进去本地sd中查找");
			String bitmapName = imageURL + ".jpg";
			String account = InfoUtils.getUser(STChatApplication.getInstance());
			File cacheDir = new File(Environment.getExternalStorageDirectory()
					+ "/" + "stPhoto/" + account);

			// // 判断文件夹是否存在,如果不存在则创建文件夹
			// if (!cacheDir.exists()) {
			// fileUtils.creatSDDir("stPhoto/" + account);
			// }
			// File[] cacheFiles = cacheDir.listFiles();
			// int i = 0;
			// for (; i < cacheFiles.length; i++) {
			// if (bitmapName.equalsIgnoreCase(cacheFiles[i].getName())) {
			// break;
			// }
			// }
			// if (i < cacheFiles.length) {
			// return fileUtils.getBitmapFromPathByAccount(imageURL);
			// }
			if (cacheDir.exists()) {
				File[] cacheFiles = cacheDir.listFiles();
				int i = 0;
				for (; i < cacheFiles.length; i++) {
					if (bitmapName.equalsIgnoreCase(cacheFiles[i].getName())) {
						break;
					}
				}
				if (i < cacheFiles.length) {
					return fileUtils.getBitmapFromPathByAccount(imageURL);
				}
			}
		}

		final Handler handler = new Handler() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				imageCallBack.imageLoad(imageView, (Bitmap) msg.obj);
			}
		};

		// 如果不在内存缓存中，也不在本地（被jvm回收掉），则开启线程下载图片
		new Thread() {
			public void run() {
				Bitmap bitmap = XmppConnectionServer.getInstance()
						.getUserImage(imageURL);

				if (bitmap != null) {
					// 缓存
					imageCache.put(imageURL, new SoftReference<Bitmap>(bitmap));

					// 本地sd存储
					fileUtils.saveMyBitmap(bitmap, imageURL);
				}
				Message msg = handler.obtainMessage(0, bitmap);
				handler.sendMessage(msg);
			};
		}.start();

		return null;
	}

	/**
	 * 回调接口
	 * 
	 * @author  
	 * 
	 */
	public interface ImageCallBack {
		public void imageLoad(ImageView imageView, Bitmap bitmap);
	}
}
