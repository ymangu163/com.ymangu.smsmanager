package com.ymangu.smsmanager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class GroupOpenHelper extends SQLiteOpenHelper {
	private static GroupOpenHelper mInstance;

	private GroupOpenHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	/**
	 * 获得单例对象
	 * @param context
	 * @return
	 */
	public static GroupOpenHelper getInstance(Context context){
		if(mInstance==null){			
		/**线程1 和 线程2 都进入这里，都还没初始化，所以就生成了两个对象，故要加个锁
			 * */
		synchronized (GroupOpenHelper.class) {	
			//拿到锁后再判断一次
			if(mInstance==null){
				mInstance=new GroupOpenHelper(context, "ymangu", null, 1);	
			}
		}
	}
		
		return mInstance;
	}
	
	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// 群组表
		String sql = "create table groups(_id integer primary key, group_name varchar(30));";
		db.execSQL(sql);

		// 关联关系表
		sql = "create table thread_group(_id integer primary key,group_id integer,thread_id integer);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
