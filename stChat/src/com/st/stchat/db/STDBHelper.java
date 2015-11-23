package com.st.stchat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.st.stchat.STChatApplication;

/**
 * 数据库使用类
 * 
 * @author juwei 2014.11.26
 * 
 */
public class STDBHelper extends SQLiteOpenHelper {
	private final static String TAG = "STDBHelper";

	public static STDBHelper getSTDBHelper() {
		return new STDBHelper(STChatApplication.getInstance(),
				STChatApplication.DATABASE_NAME, null,
				STChatApplication.DATABASE_VERSION);
	}

	private STDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate");
		db.execSQL("CREATE TABLE [stChat_notice]  ([_id] INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, [type] INTEGER, [title] NVARCHAR, [content] NVARCHAR, [notice_from] NVARCHAR, [notice_to] NVARCHAR, [notice_time] TEXT, [status] INTEGER);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade ... 数据库更新");
		// 如果对于字段做了修改，或者是添加表，那么就在这里将版本号取出加1并且保存在sharePer中
		// String sqlStr = "alter table xxxx add remark varchar(20)";
		// db.execSQL(sqlStr);
	}

}
