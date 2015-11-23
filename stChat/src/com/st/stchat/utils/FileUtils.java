package com.st.stchat.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.st.stchat.STChatApplication;

/**
 * 文件读取工具类
 * 
 * @author juwei 2014.12.5
 * 
 */
public class FileUtils {
	private static final String TAG = "FileUtils";
	private String SDPATH;
	private String STPHOTOPATH = "stPhoto";
	private String MYPATH;
	private String PIC_RECEIVE_PATH = "receive";
	private String PIC_SEND_PATH = "send";
	private String PIC_CACHE_PATH = "cache";
	private File tempSendFile;
	private Bitmap imageBitmap;

	public String getSDPATH() {
		return SDPATH;
	}

	public FileUtils() {
		// 得到当前外部存储设备的目录
		SDPATH = Environment.getExternalStorageDirectory() + "/";
		MYPATH = InfoUtils.getUser(STChatApplication.getInstance())
				+ InfoUtils.getXmppHost(STChatApplication.getInstance());
	}

	/**
	 * 从指定位置（实际是相对应用户名的位置）取出文件并且转化成bitmap
	 * 
	 * @param imageAccountName
	 * @return
	 */
	public Bitmap getBitmapFromPathByAccount(String imageAccountName) {
		imageBitmap = BitmapFactory.decodeFile(SDPATH + STPHOTOPATH + "/"
				+ MYPATH + "/" + imageAccountName + ".jpg");
		return imageBitmap;
	}

	// public Bitmap getBitmapFromPathByPath(String picPath) {
	// System.out.println("方法内路径为：" + SDPATH + STPHOTOPATH + "/" + PICPATH
	// + "/" + MYPATH + "/");
	//
	// Bitmap imageBitmap = BitmapFactory.decodeFile(picPath);
	// return imageBitmap;
	// }

	/**
	 * 获得当前用户名头像的完整路径
	 * 
	 * @param imageName
	 *            accoutn
	 * @return /storage/emulated/0/stPhoto/ora/ora.jpg
	 */
	public String getImagePath(String imageName) {

		return SDPATH + STPHOTOPATH + "/" + MYPATH + "/" + imageName + ".jpg";
	}

	/**
	 * 在SD卡上创建文件
	 * 
	 * @param fileName
	 *            文件名称
	 * @return
	 * @throws IOException
	 */
	private File creatSDFile(String fileName) throws IOException {
		Log.e(TAG, "creatSDFile >>>> " + fileName);
		File file = new File(fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * 在SD卡上创建目录
	 * 
	 * @param dirName
	 *            目录名称
	 * @return
	 */
	public File creatSDDir(String dirName) {
		Log.d(TAG, "SD卡上创建的目录名称====" + SDPATH + dirName);
		File dir = new File(SDPATH + dirName);
		dir.mkdir();
		return dir;
	}

	/**
	 * 判断头像文件是否存在
	 * 
	 * @param accountFileName
	 *            用户名account
	 * @return 返回当前这个头像文件是否存在
	 */
	public boolean isFileExist(String accountFileName) {
		// System.out.println("检查SDPATH路径===" + SDPATH + STPHOTOPATH + "/"
		// + MYPATH + "/" + accountFileName + ".jpg");
		File file = new File(SDPATH + STPHOTOPATH + "/" + MYPATH + "/"
				+ accountFileName + ".jpg");
		return file.exists();
	}

	/**
	 * 将Bitmap对象存储为本地jpg(JPEG)头像图片
	 * 
	 * @param mBitmap
	 *            图片对象
	 * @param bitName
	 *            图片名称（username）
	 * @return 返回 0 存储成功，-1 文件或文件夹没找到 ，-2 存储失败
	 */
	public String saveMyBitmap(Bitmap mBitmap, String bitName) {
		if (mBitmap == null) {
			return "-10";
		}
		File file = null;
		FileOutputStream fos = null;
		try {
			creatSDDir(STPHOTOPATH + "/");
			creatSDDir(STPHOTOPATH + "/" + MYPATH + "/");
			file = creatSDFile(SDPATH + STPHOTOPATH + "/" + MYPATH + "/"
					+ bitName + ".jpg");
			Log.e(TAG, "创建的文件为：" + file.getPath());
			fos = new FileOutputStream(file);
			Log.e(TAG, "文件输出流fos == null么？    " + (fos == null));
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			return "0";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "-1";
		} catch (IOException e) {
			e.printStackTrace();
			return "-2";
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 将压缩后的Bitmap图像保存本地并且返回一个保存成功的路径
	 * 
	 * @param mBitmap
	 *            压缩后的bitmap对象
	 * @param picName
	 *            图片名称 xxx.jpeg
	 * @return
	 */
	public String saveBitmapToPic(Bitmap mBitmap) {
		if (mBitmap == null) {
			return "-10";
		}
		File file = null;
		FileOutputStream fos = null;
		try {
			creatSDDir(STPHOTOPATH + "/");
			creatSDDir(STPHOTOPATH + "/" + MYPATH + "/");
			creatSDDir(STPHOTOPATH + "/" + MYPATH + "/" + PIC_CACHE_PATH + "/");
			file = creatSDFile(SDPATH + STPHOTOPATH + "/" + MYPATH + "/"
					+ PIC_CACHE_PATH + "/" + getPhotoFileName());

			fos = new FileOutputStream(file);
			// Log.e(TAG, "文件输出流fos == null么？    " + (fos == null));
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();

			return file.getPath();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "-1";
		} catch (IOException e) {
			e.printStackTrace();
			return "-2";
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/** 保存方法 */
	// public boolean saveBitmap(Bitmap mBitmap, String bitName) {
	// Log.e(TAG, "保存图片");
	// File f = new File(SDPATH+"namecard/", bitName);
	// if (f.exists()) {
	// f.delete();
	// }
	// try {
	// FileOutputStream out = new FileOutputStream(f);
	// mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
	// out.flush();
	// out.close();
	// Log.i(TAG, "已经保存");
	// return true;
	// } catch (FileNotFoundException e) {
	//
	// e.printStackTrace();
	// return false;
	// } catch (IOException e) {
	//
	// e.printStackTrace();
	// return false;
	// }
	//
	// }

	/**
	 * 创建一个收到照片的接受路径 将照片写入SD卡中
	 * 
	 * @param path
	 *            在SD卡中的存放路径
	 * @param fileName
	 *            文件名
	 * @param input
	 *            读取到的输入流
	 * @return
	 */
	public String writeToSDFromInput(InputStream input, long inputSize,
			String mimeType) {
		File file = null;

		OutputStream output = null;

		try {
			creatSDDir(STPHOTOPATH + "/");
			creatSDDir(STPHOTOPATH + "/" + MYPATH + "/");
			creatSDDir(STPHOTOPATH + "/" + MYPATH + "/" + PIC_RECEIVE_PATH
					+ "/");
			file = creatSDFile(SDPATH + STPHOTOPATH + "/" + MYPATH + "/"
					+ PIC_RECEIVE_PATH + "/" + getPhotoFileName());
			// file = creatSDFile(SDPATH + STPHOTOPATH + "/" + PICPATH
			// +"/"+MYPATH + "/"
			// + fileName + ".jpg");

			output = new FileOutputStream(file);
			byte[] buffer = new byte[4 * 1024];
			// while ((input.read(buffer)) != -1) {
			// System.out.println("读取了："+buffer.length);
			// output.write(buffer);
			//
			// }
			int byteCount = 0;
			while ((byteCount = input.read(buffer)) != -1) {
				output.write(buffer, 0, byteCount);
			}
			output.flush();
			return new FileUtils().saveBitmapToPic(ImageUtils.getimage(file
					.getPath()));
		} catch (IOException e) {
			e.printStackTrace();
			return "-10";
		} finally {
			try {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 创建一个照相之后的照相发送路径
	 * 
	 * @return
	 */
	public File creatPhotoCache() {
		// 创建一个以当前时间为名称的缓存照片文件
		try {
			creatSDDir(STPHOTOPATH + "/");
			creatSDDir(STPHOTOPATH + "/" + MYPATH + "/");
			creatSDDir(STPHOTOPATH + "/" + MYPATH + "/" + PIC_SEND_PATH + "/");
			tempSendFile = creatSDFile(SDPATH + STPHOTOPATH + "/" + MYPATH
					+ "/" + PIC_SEND_PATH + "/" + getPhotoFileName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tempSendFile;
	}

	// 使用系统当前日期加以调整作为照片的名称
	@SuppressLint("SimpleDateFormat")
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}

}
