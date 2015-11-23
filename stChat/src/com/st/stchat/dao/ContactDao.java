package com.st.stchat.dao;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.st.stchat.db.ChatDBHelper;
import com.st.stchat.manager.ContactManager;
import com.st.stchat.model.Contact;
import com.st.stchat.model.User;

/**
 * 联系人Dao
 * 
 * @author  
 * 
 */
public class ContactDao {

	private static final String TABLE_NAME = "contact_table";

	private static ContactDao instance = null;
	private static Object obj = new Object();

	private ContactDao() {

	}

	public static ContactDao getInstance() {
		// if already inited, no need to get lock everytime
		if (instance == null) {
			synchronized (obj) {
				if (instance == null) {
					instance = new ContactDao();
				}
			}
		}

		return instance;
	}

	public boolean tabbleIsExist() {
		boolean result = false;
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();

		String sql = "select count(*) from sqlite_master where type='table' and name='contact_table'";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.moveToNext()) {
			int count = cursor.getInt(0);
			if (count > 0) {
				result = true;
			}
		}
		cursor.close();
		db.close();
		return result;
	}

	public boolean contactIsExist(String accountName) {
		boolean result = false;
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();

		String sql = "select * from contact_table where accountName='"
				+ accountName + "'";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			result = true;
		}
		;
		cursor.close();
		db.close();
		return result;
	}

	public boolean contactIsExist(String accountName, String contactName) {
		boolean result = false;
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();

		String sql = "select * from contact_table where accountName='"
				+ accountName + "' " + "and contactName='" + contactName + "'";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			result = true;
		}
		;
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * 保存联系人
	 * 
	 * @param entry
	 * @param roster
	 * @param accountName
	 * @return
	 */
	public long saveContact(RosterEntry entry, Roster roster, String accountName) {
		long id = -1;
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		ContentValues contentValues = new ContentValues();
		User user = ContactManager.transEntryToUser(entry, roster);
		if (user.getType().toString().equals("both")) {
			contentValues.put("accountName", accountName);
			contentValues.put("contactName", user.getJID());
			contentValues.put("contactNickname", user.getJID().split("@")[0]);
			id = db.insert(TABLE_NAME, null, contentValues);
		}
		db.close();
		return id;// 添加的行数，如果返回值为-1，则添加失败
	}

	/**
	 * 查询所有联系人
	 * 
	 * @param accountName
	 * @return
	 */
	public List<Contact> getContactByAccount(String accountName) {
		List<Contact> list = new ArrayList<Contact>();
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		String sql = "select * from contact_table where accountName='"
				+ accountName + "'";
		Cursor cursor = db.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			Contact contact = new Contact(cursor.getString(cursor
					.getColumnIndex("accountName")), cursor.getString(cursor
					.getColumnIndex("contactName")), cursor.getString(cursor
					.getColumnIndex("contactNickname")));
			list.add(contact);
		}
		cursor.close();
		db.close();
		return list;
	}

	/**
	 * 查询完整的用户名
	 * 
	 * @param accountName
	 * @param contactNikeName
	 * @return
	 */
	public String getContactJid(String accountName, String contactNickname) {
		String contactName = "";
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		String sql = "select contactName from contact_table where accountName='"
				+ accountName
				+ "' "
				+ "and contactNickname='"
				+ contactNickname + "'";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.moveToNext()) {
			contactName = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return contactName;
	}

	/**
	 * 删除联系人
	 * 
	 * @param accountName
	 * @param contactName
	 * @return
	 */
	public int delContact(String accountName, String contactName) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		int result = db.delete("contact_table", "accountName='" + accountName
				+ "' " + "and contactName='" + contactName + "'", null);
		System.out.println("--------delContact结果集总共有：" + result + " 条数据");
		db.close();
		return result;
	}

	/**
	 * 删除该账户下所有联系人
	 * 
	 * @param accountName
	 *            账户
	 * @return
	 */
	public int delAllContactByAccount(String accountName) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		int result = db.delete("contact_table", "accountName='" + accountName
				+ "'", null);
		System.out.println("--------delAllContactByAccount结果集总共有：" + result
				+ " 条数据");
		db.close();
		return result;
	}

	/**
	 * 查询该账户下所有联系人的数量
	 * 
	 * @param accountName
	 * @return
	 */
	public Integer getContactCountByAccount(String accountName) {
		SQLiteDatabase db = ChatDBHelper.getChatDBHelper()
				.getReadableDatabase();
		int result = 0;
		Cursor cursor = db.rawQuery(
				"select count(*) from contact_table where accountName='"
						+ accountName + "'", null);
		if (cursor.moveToNext()) {
			result = cursor.getInt(0);
		}
		cursor.close();
		db.close();
		return result;
	}

}
