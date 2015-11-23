package com.st.stchat.dao;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.st.stchat.db.STDBHelper;

/**
 * 示例DAO层 如果是使用到结果集，记得关闭 推荐添加事务回滚操作
 * 
 * @author juwei 2014.11.26
 * 
 */
public class STDao {

	public STDao() {

	}

	/**
	 * 添加一条记录到数据库
	 * 
	 * @param name
	 *            账户名
	 * @param number
	 *            JID
	 */
	public long add(String accoutn, String jid) {
		SQLiteDatabase db = STDBHelper.getSTDBHelper().getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("accoutn", accoutn);
		values.put("jid", jid);
		long id = db.insert("notifFriends", null, values);
		db.close();
		return id;// 添加的行数，如果返回值为-1，则添加失败
	}

	/**
	 * 查询记录是否存在
	 */
	public boolean find() {
		SQLiteDatabase db = STDBHelper.getSTDBHelper().getReadableDatabase();

		db.close();
		return false;
	}

	/**
	 * 修改一条记录
	 */
	public int update() {
		SQLiteDatabase db = STDBHelper.getSTDBHelper().getWritableDatabase();

		db.close();
		return Integer.MAX_VALUE;// 影响了多少行，如果number大于0,就说明修改成功
	}

	/**
	 * 删除一条记录
	 */
	public void del() {
		SQLiteDatabase db = STDBHelper.getSTDBHelper().getWritableDatabase();

		db.close();
	}

	/**
	 * 返回全部的数据库信息
	 * 
	 * @return
	 */
	public void findAll() {
		SQLiteDatabase db = STDBHelper.getSTDBHelper().getReadableDatabase();

		db.close();
	}
}
