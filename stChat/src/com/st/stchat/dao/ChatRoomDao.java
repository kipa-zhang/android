package com.st.stchat.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.st.stchat.db.ChatDBHelper;
import com.st.stchat.model.ChatRoom;

/**
 * 聊天室信息dao
 * 
 * @author  
 * 
 */
public class ChatRoomDao {

	private static final String TABLE_NAME = "chatroom_table";
	private static ChatRoomDao instance = null;
	private static Object obj = new Object();

	private ChatRoomDao() {

	}

	public static ChatRoomDao getInstance() {
		// if already inited, no need to get lock everytime
		if (instance == null) {
			synchronized (obj) {
				if (instance == null) {
					instance = new ChatRoomDao();
				}
			}
		}
		return instance;
	}

	public long saveChatRoom(String accountName, String roomJid) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("accountName", accountName);
		contentValues.put("roomJid", roomJid);

		long id = db.insert(TABLE_NAME, null, contentValues);
		db.close();
		return id;// 添加的行数，如果返回值为-1，则添加失败
	}

	// 查询
	public int findRoom(String accountName, String roomJid) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		Cursor cursor = db
				.rawQuery("select * from chatroom_table where accountName='"
						+ accountName + "' and roomJid='" + roomJid + "'", null);
		int count = cursor.getCount();
		System.out.println("--------结果集总共有room：" + count + " 条数据");
		cursor.close();
		db.close();
		return count;
	}

	public List<ChatRoom> getChatRoomByAccount(String accountName) {
		List<ChatRoom> list = new ArrayList<ChatRoom>();
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		String sql = "select * from chatroom_table where accountName='"
				+ accountName + "'";
		Cursor cursor = db.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			ChatRoom chatRoom = null;
			if (cursor.isFirst()) {
				chatRoom = new ChatRoom("dbTag", cursor.getString(cursor
						.getColumnIndex("accountName")),
						cursor.getString(cursor.getColumnIndex("roomJid")));
			} else {
				chatRoom = new ChatRoom("db", cursor.getString(cursor
						.getColumnIndex("accountName")),
						cursor.getString(cursor.getColumnIndex("roomJid")));
			}
			list.add(chatRoom);
		}
		cursor.close();
		db.close();
		return list;
	}

}
