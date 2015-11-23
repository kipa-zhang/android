package com.st.stchat.utils;

/**
 * 日志
 * 
 * @author juwei 2014.11.20
 * 
 */
public class Logger {
	private static boolean isDebug = true;
	private static boolean saveToFile = false;

	public static void saveToFile(boolean saveToFile) {
		Logger.saveToFile = saveToFile;
	}

	public static void debug(boolean isDebug) {
		Logger.isDebug = isDebug;
	}

	public static void v(String tag, String msg) {
		if (isDebug) {
			System.out.println("v " + tag + " :" + msg);
		}
	}

	public static void v(String tag, String msg, Throwable e) {
		if (isDebug) {
			System.out
					.println("v " + tag + " :" + msg + " e:" + e.getMessage());
		}
	}

	public static void d(String tag, String msg) {
		if (isDebug) {
			System.out.println("d " + tag + " :" + msg);
		}
	}

	public static void d(String tag, String msg, Throwable e) {
		if (isDebug) {
			System.out
					.println("d " + tag + " :" + msg + " e:" + e.getMessage());
		}
	}

	public static void i(String tag, String msg) {
		if (isDebug) {
			System.out.println("i " + tag + " :" + msg);
		}
	}

	public static void i(String tag, String msg, Throwable e) {
		if (isDebug) {
			System.out
					.println("i " + tag + " :" + msg + " e:" + e.getMessage());
		}
	}

	public static void w(String tag, String msg) {
		if (isDebug) {
			System.out.println("w " + tag + " :" + msg);
		}
	}

	public static void w(String tag, String msg, Throwable e) {
		if (isDebug) {
			System.out
					.println("w " + tag + " :" + msg + " e:" + e.getMessage());
		}
	}

	public static void e(String tag, String msg) {
		if (isDebug) {
			System.out.println("e " + tag + " :" + msg);
		}
	}

	public static void e(String tag, String msg, Throwable e) {
		if (isDebug) {
			System.out.println("e" + tag + " :" + msg + " e:" + e.getMessage());
		}
	}

	public static void open() {

	}

	public static void close() {

	}

	public static void println(int priority, String tag, String message) {

	}
}
