package com.st.stchat.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import com.st.stchat.R;
import com.st.stchat.STChatApplication;
import com.st.stchat.manager.ChatRoomManager;
import com.st.stchat.model.CombineBitmap;

public class CombineBitmapUtil {
	static public Drawable getScaleDraw(String imgPath, Context mContext) {

		Bitmap bitmap = null;
		try {
			Log.d("BitmapUtil",
					"[getScaleDraw]imgPath is " + imgPath.toString());
			File imageFile = new File(imgPath);
			if (!imageFile.exists()) {
				Log.d("BitmapUtil", "[getScaleDraw]file not  exists");
				return null;
			}
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imgPath, opts);

			opts.inSampleSize = computeSampleSize(opts, -1, 800 * 480);
			// Log.d("BitmapUtil","inSampleSize===>"+opts.inSampleSize);
			opts.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(imgPath, opts);

		} catch (OutOfMemoryError err) {
			Log.d("BitmapUtil", "[getScaleDraw] out of memory");

		}
		if (bitmap == null) {
			return null;
		}
		Drawable resizeDrawable = new BitmapDrawable(mContext.getResources(),
				bitmap);
		return resizeDrawable;
	}

	public static void saveMyBitmap(Context mContext, Bitmap bitmap,
			String desName) throws IOException {
		FileOutputStream fOut = null;

		if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
			fOut = mContext.openFileOutput(desName + ".png",
					Context.MODE_PRIVATE);
		} else {
			File f = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/Asst/cache/" + desName + ".png");
			f.createNewFile();
			fOut = new FileOutputStream(f);
		}
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static public Bitmap getScaleBitmap(Resources res, int id) {

		Bitmap bitmap = null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(res, id, opts);

			opts.inSampleSize = computeSampleSize(opts, -1, 800 * 480);
			// Log.d("BitmapUtil","inSampleSize===>"+opts.inSampleSize);
			opts.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeResource(res, id, opts);
		} catch (OutOfMemoryError err) {
			Log.d("BitmapUtil", "[getScaleBitmap] out of memory");

		}
		return bitmap;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {

		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;

	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {

		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}

	}

	public static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public static Bitmap decodeBitmap(Resources res, int id) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeResource(res, id, options);
		if (bitmap == null) {
		}
		float realWidth = options.outWidth;
		float realHeight = options.outHeight;
		int scale = (int) ((realHeight > realWidth ? realHeight : realWidth) / 100);
		if (scale <= 0) {
			scale = 1;
		}
		options.inSampleSize = scale;
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeResource(res, id, options);
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		return bitmap;
	}

	public static Bitmap getCombineBitmaps(List<CombineBitmap> mEntityList,
			Bitmap... bitmaps) {
		Bitmap newBitmap = Bitmap.createBitmap(200, 200, Config.ARGB_8888);
		for (int i = 0; i < mEntityList.size(); i++) {
			newBitmap = mixtureBitmap(newBitmap, bitmaps[i], new PointF(
					mEntityList.get(i).x, mEntityList.get(i).y));
		}
		return newBitmap;
	}

	public static Bitmap combineBitmaps(int columns, Bitmap... bitmaps) {
		if (columns <= 0 || bitmaps == null || bitmaps.length == 0) {
			throw new IllegalArgumentException(
					"Wrong parameters: columns must > 0 and bitmaps.length must > 0.");
		}
		int maxWidthPerImage = 20;
		int maxHeightPerImage = 20;
		for (Bitmap b : bitmaps) {
			maxWidthPerImage = maxWidthPerImage > b.getWidth() ? maxWidthPerImage
					: b.getWidth();
			maxHeightPerImage = maxHeightPerImage > b.getHeight() ? maxHeightPerImage
					: b.getHeight();
		}
		int rows = 0;
		if (columns >= bitmaps.length) {
			rows = 1;
			columns = bitmaps.length;
		} else {
			rows = bitmaps.length % columns == 0 ? bitmaps.length / columns
					: bitmaps.length / columns + 1;
		}
		Bitmap newBitmap = Bitmap.createBitmap(columns * maxWidthPerImage, rows
				* maxHeightPerImage, Config.ARGB_8888);
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int index = x * columns + y;
				if (index >= bitmaps.length)
					break;
				newBitmap = mixtureBitmap(newBitmap, bitmaps[index],
						new PointF(y * maxWidthPerImage, x * maxHeightPerImage));
			}
		}
		return newBitmap;
	}

	/**
	 * Mix two Bitmap as one.
	 * 
	 * @param bitmapOne
	 * @param bitmapTwo
	 * @param point
	 *            where the second bitmap is painted.
	 * @return
	 */
	public static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
			PointF fromPoint) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}
		Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(),
				first.getHeight(), Config.ARGB_8888);
		Canvas cv = new Canvas(newBitmap);
		cv.drawBitmap(first, 0, 0, null);
		cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
		cv.save(Canvas.ALL_SAVE_FLAG);
		cv.restore();
		return newBitmap;
	}

	public static void getScreenWidthAndHeight(Activity mContext) {
		DisplayMetrics metric = new DisplayMetrics();
		mContext.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels;
		int height = metric.heightPixels;
	}

	// 得到聊天室成员（必须当前用户进入聊天室之后才能拿到）
	public static List<String> getRoomMembers(String roomJid) {
		List<String> listMembers = new ArrayList<String>();
		ChatRoomManager roomManager = new ChatRoomManager();
		try {
			listMembers = roomManager.findMulitUser(roomJid);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listMembers;
	}

	// 根据成员取各自头像放入数组
	public static Bitmap[] getBitmaps(List<String> userList,
			List<CombineBitmap> combineBitmaps) {
		Bitmap[] bitmaps = new Bitmap[userList.size()];

		for (int i = 0; i < userList.size(); i++) {
			Bitmap bitmap = getHeadByName(userList.get(i).split("@")[0]);
			if (bitmap != null) {
				bitmaps[i] = ThumbnailUtils.extractThumbnail(bitmap,
						(int) combineBitmaps.get(1).width,
						(int) combineBitmaps.get(1).width);
			} else {
				bitmaps[i] = ThumbnailUtils.extractThumbnail(CombineBitmapUtil
						.getScaleBitmap(STChatApplication.getInstance()
								.getResources(), R.drawable.default_nor_man),
						(int) combineBitmaps.get(1).width, (int) combineBitmaps
								.get(1).width);
			}
		}
		return bitmaps;
	}

	// 根据用户名取头像Bitmap
	public static Bitmap getHeadByName(String name) {
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
