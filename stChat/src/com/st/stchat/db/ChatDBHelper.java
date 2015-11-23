package com.st.stchat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.st.stchat.STChatApplication;

/**
 * 
 * @author juwei 2014.11.26
 * 
 */
public class ChatDBHelper extends SQLiteOpenHelper {
	private final static String TAG = "STDBHelper";

	public static ChatDBHelper getChatDBHelper() {
		return new ChatDBHelper(STChatApplication.getInstance(),
				STChatApplication.CHATDATABASE_NAME, null,
				STChatApplication.CHATDATABASE_VERSION);
	}

	private ChatDBHelper(Context context, String name, CursorFactory factory,
			int version) {

		super(context, name, factory, version);
		// System.out.println("-------------new ChatDBHelper---------");

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "----- 数据库helper____onCreate()");
		db.execSQL("create table chatmessages_table (id integer primary key autoincrement, chatPacketId varchar(20) ,chatAccount varchar(20) NOT NULL , chatType varchar(20) NOT NULL, chatFrom varchar(20) NOT NULL, chatTo varchar(20) NOT NULL, chatRead varchar(20),chatSendStatus varchar(20), chatTime varchar(20) NOT NULL, chatText varchar(100), chatStyle varchar(20) NOT NULL,chatPicPath varchar(100))");
		db.execSQL("create table chatroommessages_table (id integer primary key autoincrement, messageId varchar(100) , chatAccount varchar(20) NOT NULL , chatType varchar(20) NOT NULL, chatFrom varchar(20) NOT NULL, roomJid varchar(100) NOT NULL, chatRead varchar(20), chatTime varchar(20) NOT NULL, chatText varchar(100))");
		db.execSQL("create table chatroom_table (id integer primary key autoincrement, accountName varchar(20) NOT NULL , roomJid varchar(100) NOT NULL)");
		db.execSQL("create table contact_table (id integer primary key autoincrement, accountName varchar(20) NOT NULL , contactName varchar(100) NOT NULL, contactNickname varchar(50) NOT NULL)");
		db.execSQL("CREATE TABLE [stChat_notice]  ([_id] INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, [type] INTEGER, [title] NVARCHAR, [content] NVARCHAR, [notice_from] NVARCHAR, [notice_to] NVARCHAR, [notice_time] TEXT, [status] INTEGER);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "-----  数据库更新____onUpgrade() .... ");
		// 如果对于字段做了修改，或者是添加表，那么就在这里将版本号取出加1并且保存在sharePer中
		// String sqlStr = "alter table xxxx add remark varchar(20)";
		// db.execSQL(sqlStr);

	}

}
