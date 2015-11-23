package com.st.stchat.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.st.stchat.STChatApplication;
import com.st.stchat.bean.GroupMessageEntity;
import com.st.stchat.bean.LastChatMsg;
import com.st.stchat.db.ChatDBHelper;
import com.st.stchat.utils.InfoUtils;

/**
 * 聊天室消息Dao
 * 
 * @author  
 * 
 */
public class ChatRoomMessageDao {
	private static final String TAG = "ChatRoomMessageDao";
	private static final String TABLE_NAME = "chatroommessages_table";

	/**
	 * 
	 * @param messageId
	 *            服务器返回消息的Id
	 * @param chatAccount
	 *            当前登录的用户账户
	 * @param chatType
	 *            该消息类型"1"为发送(to)，"2"为接收(from)
	 * @param chatFrom
	 *            消息的发送方
	 * @param roomJid
	 *            消息的接收房间
	 * @param chatRead
	 *            阅读类型0为未读，1为已读
	 * @param chatTime
	 *            消息的时间（精确到毫秒）
	 * @param chatText
	 *            消息内容
	 * @return
	 */
	public long add(String messageId, String chatAccount, String chatType,
			String chatFrom, String roomJid, String chatRead, String chatTime,
			String chatText) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getWritableDatabase();
		db.beginTransaction();
		long id = 0;
		try {
			// values是一个集合，key就是列的名字，value就是列的值
			ContentValues values = new ContentValues();
			values.put("messageId", messageId);
			values.put("chatAccount", chatAccount);
			values.put("chatType", chatType);
			values.put("chatFrom", chatFrom);
			values.put("roomJid", roomJid);
			if (chatType.equals("2")) {
				values.put("chatRead", chatRead);
			}
			values.put("chatTime", chatTime);
			values.put("chatText", chatText);
			id = db.insert(TABLE_NAME, null, values);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}

		return id;
	}

	/**
	 * 根据账户和房间id获取 所有消息记录
	 * 
	 * @param account
	 * @param roomJid
	 * @return
	 */
	public List<GroupMessageEntity> getAllListByAccountWithRoomJid(
			String account, String roomJid) {
		List<GroupMessageEntity> list = new ArrayList<GroupMessageEntity>();
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		String sql = "select * from " + TABLE_NAME + " where chatAccount = '"
				+ account + "' and roomJid = '" + roomJid + "' "
				+ "order by chatTime asc";
		System.out.println(sql);
		Cursor cursor = db.rawQuery(sql, null);
		System.out.println("--------获取聊天室消息结果集总共有：" + cursor.getCount()
				+ " 条数据");
		while (cursor.moveToNext()) {
			String messageId = cursor.getString(cursor
					.getColumnIndex("messageId"));
			String chatAccount = cursor.getString(cursor
					.getColumnIndex("chatAccount"));
			String chatType = cursor.getString(cursor
					.getColumnIndex("chatType"));
			String chatFrom = cursor.getString(cursor
					.getColumnIndex("chatFrom"));
			String roomJidDt = cursor.getString(cursor
					.getColumnIndex("roomJid"));
			String chatRead = cursor.getString(cursor
					.getColumnIndex("chatRead"));
			String chatTime = cursor.getString(cursor
					.getColumnIndex("chatTime"));
			String chatText = cursor.getString(cursor
					.getColumnIndex("chatText"));
			GroupMessageEntity entity = new GroupMessageEntity(messageId,
					chatAccount, chatType, chatFrom, roomJidDt, chatRead,
					chatTime, chatText);

			list.add(entity);
		}
		cursor.close();
		db.close();
		return list;
	}

	/**
	 * 修改一条记录的阅读状态
	 * 
	 * @param chatTime
	 *            该条记录的时间（毫秒）
	 * @param chatRead
	 *            阅读状态 0为未读，1为已读
	 * @return
	 */
	public int updateReadByTime(String chatTime, String chatRead) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getWritableDatabase();
		db.beginTransaction();

		int number = 0;
		try {
			ContentValues values = new ContentValues();
			values.put("chatRead", chatRead);
			number = db.update(TABLE_NAME, values, "chatTime=?",
					new String[] { chatTime });
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
		return number;
	}

	// 通过当前登陆账户，查询出有多少条未读消息
	public int findNotReadRoomMsg(String chatAccount) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from chatroommessages_table where chatAccount='"
						+ chatAccount + "' and chatRead='0'", null);
		int count = cursor.getCount();
		Log.i(TAG, "--------结果集总共有：" + count + " 条未读的聊天室信息");
		cursor.close();
		db.close();
		return count;
	}

	// 通过当前登陆账户和roomjid，查询出有多少条未读消息
	public int findNotReadRoomMsg(String chatAccount, String roomJid) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from chatroommessages_table where chatAccount='"
						+ chatAccount + "' and chatRead='0'" + " and roomJid='"
						+ roomJid + "'", null);
		int count = cursor.getCount();
		System.out.println("--------Room结果集总共有：" + count + " 条数据");
		cursor.close();
		db.close();
		return count;
	}

	/**
	 * 获取最近聊天人聊天最后一条消息  
	 * 
	 * @return
	 */
	public List<LastChatMsg> getRecentRoomMsg() {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();

		List<LastChatMsg> lastChatMsgs = new ArrayList<LastChatMsg>();

		String sql = "select roomJid,max(chatTime) as time,chatText from chatroommessages_table "
				+ "where chatAccount = '"
				+ InfoUtils.getUser(STChatApplication.getInstance())
				+ "' "
				+ "group by roomJid order by time desc";

		Cursor cursor = db.rawQuery(sql, null);

		// Log.i(TAG, "--------Room最后一条消息结果集总共有：" + cursor.getCount() + " 条数据");

		while (cursor.moveToNext()) {
			LastChatMsg lastChatMsg = new LastChatMsg();
			lastChatMsg.setNoticeTime(cursor.getString(cursor
					.getColumnIndex("time")));
			lastChatMsg.setContent(cursor.getString(cursor
					.getColumnIndex("chatText")));
			lastChatMsg.setFrom(cursor.getString(cursor
					.getColumnIndex("roomJid")));

			lastChatMsgs.add(lastChatMsg);
		}
		cursor.close();

		for (LastChatMsg l : lastChatMsgs) {
			String sql2 = "select * from chatroommessages_table where "
					+ "chatAccount='"
					+ InfoUtils.getUser(STChatApplication.getInstance())
					+ "' and chatRead='0'" + " and roomJid='" + l.getFrom()
					+ "'";
			Cursor cursor2 = db.rawQuery("select count(*) from (" + sql2 + ")",
					null);

			if (cursor2.moveToNext()) {
				l.setNoticeSum(cursor2.getInt(0) + "");
				cursor2.close();
			}
		}

		db.close();
		return lastChatMsgs;
	}

	/**
	 * 删除与某人的聊天记录
	 * 
	 * @param from
	 * @return
	 */
	public int delChatRoomMsgWithSb(String accountName, String roomJid) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		int result = db.delete(TABLE_NAME, "chatAccount='" + accountName
				+ "' and roomJid='" + roomJid + "'", null);
		db.close();
		return result;
	}

	/**
	 * 查询 记录是否存在
	 * 
	 * @param messageId
	 * @return
	 */
	public boolean find(String messageId) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		// 表名，查询哪一列(为一个String类型的数组，如果是全部列，就写null),某一列的某个值的那行数据，占位符，分组查询（如果不关心，可以写为null）
		Cursor cursor = db.query(TABLE_NAME, null, "messageId=?",
				new String[] { messageId }, null, null, null);
		boolean result = cursor.moveToNext();
		cursor.close();
		db.close();
		return result;
	}
}
