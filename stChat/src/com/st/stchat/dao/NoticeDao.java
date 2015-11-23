package com.st.stchat.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.st.stchat.STChatApplication;
import com.st.stchat.db.ChatDBHelper;
import com.st.stchat.model.Notice;
import com.st.stchat.utils.InfoUtils;

/**
 * 消息Dao
 * 
 * @author  
 * 
 */
public class NoticeDao {

	/**
	 * Default Primary key
	 */
	protected String mPrimaryKey = "_id";

	private static NoticeDao instance = null;
	private static Object obj = new Object();

	public NoticeDao() {
		// TODO Auto-generated constructor stub
	}

	public static NoticeDao getInstance() {
		// if already inited, no need to get lock everytime
		if (instance == null) {
			synchronized (obj) {
				if (instance == null) {
					instance = new NoticeDao();
				}
			}
		}

		return instance;
	}

	/**
	 * 保存好友请求消息
	 * 
	 * @param notice
	 * @return
	 */
	public long saveNotice(Notice notice) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("title", notice.getTitle());
		contentValues.put("content", notice.getContent());
		contentValues.put("notice_to", notice.getTo());
		contentValues.put("notice_from", notice.getFrom());
		contentValues.put("type", notice.getNoticeType());
		contentValues.put("status", notice.getStatus());
		contentValues.put("notice_time", notice.getNoticeTime());

		long id = db.insert("stChat_notice", null, contentValues);
		db.close();
		return id;// 添加的行数，如果返回值为-1，则添加失败
	}

	/**
	 * 获取所有请求添加好友的消息
	 * 
	 * @param isRead
	 * @return
	 */
	public List<Notice> getNoticeListByType(int isRead) {
		Map<String, String> userMap = InfoUtils
				.getUserAccount(STChatApplication.getInstance());
		String user = userMap.get("userAccountName");
		String to = user + "@"
				+ InfoUtils.getPacketDomain(STChatApplication.getInstance());

		StringBuilder sb = new StringBuilder();
		String[] str = null;
		sb.append("select * from stChat_notice where type in(1,2) and notice_to=?");
		if (Notice.UNREAD == isRead || Notice.READ == isRead) {
			str = new String[] { "" + to, "" + isRead };
			sb.append(" and status=? ");
		} else {
			str = new String[] { "" + to };
		}
		sb.append(" order by notice_time desc");
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();

		Cursor cursor = db.rawQuery(sb.toString(), str);

		List<Notice> notices = new ArrayList<Notice>();

		while (cursor.moveToNext()) {
			Notice notice = new Notice();
			notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
			notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
			notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
			notice.setFrom(cursor.getString(cursor
					.getColumnIndex("notice_from")));
			notice.setTo(cursor.getString(cursor.getColumnIndex("notice_to")));
			notice.setNoticeType(cursor.getInt(cursor.getColumnIndex("type")));
			notice.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
			notice.setNoticeTime(cursor.getString(cursor
					.getColumnIndex("notice_time")));
			notices.add(notice);
		}
		cursor.close();
		db.close();
		return notices;
	}

	/**
	 * 获取未读消息的数量
	 * 
	 * @param type
	 * @return
	 */
	public Integer getUnReadNoticeCountByType(int type) {
		Map<String, String> userMap = InfoUtils
				.getUserAccount(STChatApplication.getInstance());
		String user = userMap.get("userAccountName");
		String to = user + "@"
				+ InfoUtils.getPacketDomain(STChatApplication.getInstance());

		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		Cursor cursor = null;
		int result = 0;
		String sql = "select _id from stChat_notice where status=? and type=? and notice_to=?";
		cursor = db.rawQuery("select count(*) from (" + sql + ")",
				new String[] { "" + Notice.UNREAD, "" + type, "" + to });
		if (cursor.moveToNext()) {
			result = cursor.getInt(0);
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * 删除联系人请求记录
	 * 
	 * @param fromUser
	 * @return
	 */
	public int delContactReq(String fromUser) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		int result = db.delete("stChat_notice", "notice_from='" + fromUser
				+ "'", null);
		System.out.println("--------delContactReq结果集总共有：" + result + " 条数据");
		db.close();
		return result;
	}

	/**
	 * 更新添加好友的状态
	 * 
	 * @param id
	 * @param status
	 * @param content
	 */
	public void updateAddFriendStatus(String id, Integer status, String content) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", status);
		contentValues.put("content", content);

		db.update("stChat_notice", contentValues, mPrimaryKey + "=?",
				new String[] { id });
		db.close();
	}

	public int getNoticeStatus(String notice_from) {
		int result = 0;
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select status from stChat_notice where notice_from = '"
						+ notice_from + "'", null);
		if (cursor.moveToNext()) {
			result = cursor.getInt(cursor.getColumnIndex("status"));
		}
		cursor.close();
		db.close();
		return result;
	}
}
