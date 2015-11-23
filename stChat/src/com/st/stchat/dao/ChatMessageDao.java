package com.st.stchat.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.st.stchat.STChatApplication;
import com.st.stchat.bean.LastChatMsg;
import com.st.stchat.bean.MessageEntity;
import com.st.stchat.db.ChatDBHelper;
import com.st.stchat.utils.InfoUtils;

/**
 * 聊天消息处理Dao层
 * 
 * @author juwei 2014.11.26
 * 
 */
public class ChatMessageDao {
	private static final String TAG = "ChatMessageDao";
	private static final String TABLE_NAME = "chatmessages_table";

	public ChatMessageDao() {

	}

	/**
	 * 添加一条记录到数据库
	 * 
	 * @param chatPacketId
	 *            当前消息记录的packetID
	 * @param chatAccount
	 *            当前apk的使用账户
	 * @param chatType
	 *            该消息状况 "1"为发送(to)，"2"为接收(from)
	 * @param chatFrom
	 *            消息的发送方
	 * @param chatTo
	 *            消息的接收方
	 * @param chatRead
	 *            本地阅读类型0为未读，1为已读
	 * @param chatSendStatus
	 *            发送状态为-1正在发送， 0为发送成功，1为发送失败,2为对方已接收 ，
	 * @param chatTime
	 *            消息的时间（精确到毫秒）
	 * @param chatText
	 *            消息内容
	 * @param chatStyle
	 *            消息样式类型 "1"为文本，"2"为图片，"3"为语音， "4"为表情
	 * @param chatPicPath
	 *            图片消息的本地缓存路径
	 * @return
	 */
	public long add(String chatPacketId, String chatAccount, String chatType,
			String chatFrom, String chatTo, String chatRead,
			String chatSendStatus, String chatTime, String chatText,
			String chatStyle, String chatPicPath) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getWritableDatabase();
		db.beginTransaction();
		// 其实就是根据集合中的名值对，循环插入，根据列的名字，插入值
		long id = 0;
		try {
			// values是一个集合，key就是列的名字，value就是列的值
			ContentValues values = new ContentValues();
			if (chatType.equals("1")) {
				values.put("chatPacketId", chatPacketId);
			}

			values.put("chatAccount", chatAccount);
			values.put("chatType", chatType);
			values.put("chatFrom", chatFrom);
			values.put("chatTo", chatTo);
			if (chatType.equals("2")) {// 如果是接受到的，就保存本地阅读状态
				values.put("chatRead", chatRead);
			}
			if (chatType.equals("1")) {// 如果是发送出去，就保存发送状态
				values.put("chatSendStatus", chatSendStatus);
			}
			values.put("chatTime", chatTime);
			values.put("chatText", chatText);
			values.put("chatStyle", chatStyle);
			values.put("chatPicPath", chatPicPath);
			id = db.insert(TABLE_NAME, null, values);
			db.setTransactionSuccessful();
		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}

		// 添加完成之后会得到一个返回值id，为添加的行数,如果返回值为-1，则添加失败
		return id;
	}

	// /**
	// * 查询记录
	// *
	// * @param name
	// * 姓名
	// * @return true 存在, false 不存在
	// */
	// public boolean find(String name) {
	// SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
	// .getReadableDatabase();
	// // Cursor cursor =
	// db.rawQuery("select * from person where name=?",newString[]{name});
	// //
	// 表名，查询哪一列(为一个String类型的数组，如果是全部列，就写null),某一列的某个值的那行数据，占位符，分组查询（如果不关心，可以写为null）
	// Cursor cursor = db.query("person", null, "name=?",new String[] { name },
	// null, null, null);
	// boolean result = cursor.moveToNext();
	// cursor.close();
	// db.close();
	// return result;
	// }

	/**
	 * 修改一条记录的本地阅读状态
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
			// db.execSQL("update XX set chatRead=? where chatTime=?", new
			// Object[]{chatRead,chatTime});
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

		// 返回的number表示影响了多少行，如果number大于0,就说明修改成功了。
		return number;
	}

	/**
	 * 修改一条发送记录的发送状态
	 * 
	 * @param chatPacketID
	 *            消息packetID
	 * @param chatSendStatus
	 *            消息发送的状态
	 * @return
	 */
	public int updateSendStatusByPacketID(String chatPacketID,
			String chatSendStatus) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getWritableDatabase();
		db.beginTransaction();
		int number = 0;
		try {
			// db.execSQL("update XX set chatSendStatus=? where chatPacketID=?",
			// new
			// Object[]{chatSendStatus,chatPacketID});
			ContentValues values = new ContentValues();
			values.put("chatSendStatus", chatSendStatus);
			number = db.update(TABLE_NAME, values, "chatPacketID=?",
					new String[] { chatPacketID });
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}

		// 返回的number表示影响了多少行，如果number大于0,就说明修改成功了。
		return number;
	}

	// /**
	// * 删除一条记录
	// * @param name 根据这个姓名删除该记录
	// */
	// public int del(String name){
	// SQLiteDatabase db = psoh.getWritableDatabase();
	// // db.execSQL("delete from person where name=?", new Object[]{name});
	// //删除名字为name的一行记录
	// int number = db.delete("person", "name=?", new String[]{name});
	// db.close();
	// //返回的number表示删除操作影响了多少行，如果大于0，则成功，如果等于0，则删除失败
	// return number;
	// }

	/**
	 * 返回全部的数据库信息
	 * 
	 * @return
	 */
	public List<MessageEntity> findAllByAccount(String accountName,
			String chatName) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		// System.out.println("db  is null ?--" + (db == null));
		// Cursor cursor = db.rawQuery("select * from chatmessages_table",null);

		Cursor cursor = db.rawQuery("select * from " + TABLE_NAME
				+ " where chatAccount='" + accountName + "' and((chatFrom='"
				+ accountName + "' and chatTo='" + chatName
				+ "') or (chatFrom='" + chatName + "' and chatTo='"
				+ accountName + "')) order by chatTime asc;", null);
		// System.out.println("cursor  is null ?--" + (cursor == null));
		// 查询
		// Cursor cursor = db.query(TABLE_NAME, new String[] { "chatAccount",
		// "chatType", "chatFrom", "chatTo", "chatTime", "chatText" },
		// "chatAccount", new String[] {accountName}, null, null,
		// "chatTime asc");
		Log.i(TAG, "--------结果集总共有：" + cursor.getCount() + " 条数据");
		List<MessageEntity> messageEntitys = new ArrayList<MessageEntity>();
		while (cursor.moveToNext()) {
			String chatPacketId = cursor.getString(cursor
					.getColumnIndex("chatPacketId"));
			String chatAccount = cursor.getString(cursor
					.getColumnIndex("chatAccount"));
			String chatType = cursor.getString(cursor
					.getColumnIndex("chatType"));
			String chatFrom = cursor.getString(cursor
					.getColumnIndex("chatFrom"));
			String chatTo = cursor.getString(cursor.getColumnIndex("chatTo"));
			String chatRead = cursor.getString(cursor
					.getColumnIndex("chatRead"));
			String chatSendStatus = cursor.getString(cursor
					.getColumnIndex("chatSendStatus"));
			String chatTime = cursor.getString(cursor
					.getColumnIndex("chatTime"));
			String chatText = cursor.getString(cursor
					.getColumnIndex("chatText"));
			String chatStyle = cursor.getString(cursor
					.getColumnIndex("chatStyle"));
			String chatPicPath = cursor.getString(cursor
					.getColumnIndex("chatPicPath"));
			MessageEntity messageEntity = new MessageEntity(chatPacketId,
					chatAccount, chatType, chatFrom, chatTo, chatRead,
					chatSendStatus, chatTime, chatText, chatStyle, chatPicPath);
			// System.out.println("-------messageEntity:"
			// + messageEntity.toString());
			messageEntitys.add(messageEntity);
		}
		Log.i(TAG, "-----成功取出所有数据");
		cursor.close();
		db.close();

		return messageEntitys;
	}

	/**
	 * 通过当前登陆账户，查询出一共有多少条未读消息
	 * 
	 * @param chatAccount
	 * @return
	 */
	public int findNotReadByAccount(String chatAccount) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from chatmessages_table where chatAccount='"
						+ chatAccount + "' and chatRead='0'", null);
		int count = cursor.getCount();
		Log.i(TAG, "--------结果集总共有：" + count + " 条未读的单人信息");
		cursor.close();
		db.close();
		return count;

	}

	/**
	 * 通过当前登陆账户和对方用户，查询出有多少条对方发来的未读消息
	 * 
	 * @param chatAccount
	 * @param from
	 * @return
	 */
	public int findNotReadByAccount(String chatAccount, String from) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from chatmessages_table where chatAccount='"
						+ chatAccount + "' and chatRead='0'"
						+ " and chatFrom='" + from + "'", null);
		int count = cursor.getCount();
		Log.i(TAG, "|--------和 " + from + " 总共有：" + count + " 条未读数据");
		cursor.close();
		db.close();
		return count;

	}

	/**
	 * 更具时间chatTime删除与某个单条聊天记录
	 * 
	 * @param from
	 * @return
	 */
	public int delSingleChatOneMessageWithTime(String chatTime) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getWritableDatabase();
		int result = 0;
		db.beginTransaction();
		try {
			result = db.delete(TABLE_NAME, "chatTime=?",
					new String[] { chatTime });
			Log.i(TAG, "--------delete结果集删除了：" + result + " 条数据");
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}

		return result;
	}

	/**
	 * 删除与某人的聊天记录
	 * 
	 * @param from
	 * @return
	 */
	public int delChatWithSb(String fromUser) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		int result = db.delete("chatmessages_table", "chatFrom='" + fromUser
				+ "' or chatTo='" + fromUser + "'", null);
		Log.i(TAG, "--------delete结果集总共有：" + result + " 条数据");
		db.close();
		return result;
	}

	/**
	 * 获取最近聊天人聊天最后一条消息  
	 * 
	 * @return
	 */
	public List<LastChatMsg> getRecentContactsWithLastMsg() {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();

		List<LastChatMsg> lastChatMsgs = new ArrayList<LastChatMsg>();

		String sql = "select t.[chatText],t.[chatTime],t.[chatFrom],t.[chatTo] from chatmessages_table t "
				+ "join (select chatFrom,max(chatTime) as time from chatmessages_table "
				+ "group by chatFrom order by time desc) as tem on "
				+ "tem.chatFrom=t.chatFrom "
				+ "where chatAccount = '"
				+ InfoUtils.getUser(STChatApplication.getInstance())
				+ "' order by chatTime asc";

		Cursor cursor = db.rawQuery(sql, null);

		// Log.i(TAG, "--------最后一条消息结果集总共有：" + cursor.getCount() + " 条数据");

		while (cursor.moveToNext()) {
			LastChatMsg lastChatMsg = new LastChatMsg();
			lastChatMsg.setNoticeTime(cursor.getString(cursor
					.getColumnIndex("chatTime")));
			lastChatMsg.setContent(cursor.getString(cursor
					.getColumnIndex("chatText")));
			lastChatMsg
					.setTo(cursor.getString(cursor.getColumnIndex("chatTo")));
			lastChatMsg.setFrom(cursor.getString(cursor
					.getColumnIndex("chatFrom")));

			lastChatMsgs.add(lastChatMsg);
		}
		cursor.close();

		for (LastChatMsg l : lastChatMsgs) {
			String sql2 = "select * from chatmessages_table where "
					+ "chatAccount='"
					+ InfoUtils.getUser(STChatApplication.getInstance())
					+ "' and chatRead='0'" + " and chatFrom='" + l.getFrom()
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
	 * 清空数据库所有表（该方法用于切换IP地址）
	 * 
	 * @return
	 */
	public boolean clear() {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getWritableDatabase();
		// int result = 0;
		db.beginTransaction();
		try {
			db.execSQL("delete from chatmessages_table");
			// db.execSQL("update chatmessages_table set id = 0 where name ='chatmessages_table'");//自增长ID为0);
			db.execSQL("delete from chatroommessages_table");
			db.execSQL("delete from chatroom_table");
			db.execSQL("delete from contact_table");
			db.execSQL("delete from stChat_notice");
			Log.e(TAG, "清除了五张表的数据");
			// Log.i(TAG, "--------delete结果集删除了：" + result + " 条数据");
			db.setTransactionSuccessful();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
		return false;
	}
}
