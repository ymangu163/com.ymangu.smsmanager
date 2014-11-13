package com.ymangu.smsmanager.provider;

import com.ymangu.smsmanager.db.GroupOpenHelper;
import com.ymangu.smsmanager.utils.Sms;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * ContentProvider 的作用 把数据库暴露出去，让其它应用能访问到
 **/

public class GroupContentProvider extends ContentProvider {
	/**
	 * ① 写出 UriMatcher 静态代码块,添加能响应的Uri
	 * ② 得到单例模式的 SQLiteOpenHelper
	 * ③ 复写增删改查方法，匹配Uri
	 **/
	private static UriMatcher uriMatcher;
	private static final String AUTHORITY="com.ymangu.smsmanager.provider.GroupContentProvider";
	private static final int GROUPS_INSERT=0;  // 添加到群组的匹配码
	private static final int GROUPS_QUERY_ALL = 1;
	private static final int THREAD_GROUP_QUERY_ALL = 2;
	private static final int THREAD_GROUP_INSERT = 3;
	private static final int GROUPS_UPDATE = 4;
	private static final int GROUPS_SINGLE_DELETE = 5;
	//定义能响应哪些uri
	static {
		uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
		
		// content://com.ymangu.smsmanager.provider.GroupContentProvider/groups/insert
		uriMatcher.addURI(AUTHORITY, "groups/insert", GROUPS_INSERT); //code 响应码
		
		// content://com.itheima26.smsmanager.provider.GroupContentProvider/groups
		uriMatcher.addURI(AUTHORITY, "groups", GROUPS_QUERY_ALL);

		// content://com.itheima26.smsmanager.provider.GroupContentProvider/thread_group
		uriMatcher.addURI(AUTHORITY, "thread_group", THREAD_GROUP_QUERY_ALL);

		// content://com.itheima26.smsmanager.provider.GroupContentProvider/thread_group/insert
		uriMatcher
				.addURI(AUTHORITY, "thread_group/insert", THREAD_GROUP_INSERT);

		// content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/update
		uriMatcher.addURI(AUTHORITY, "groups/update", GROUPS_UPDATE);

		// content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/delete/#
		uriMatcher.addURI(AUTHORITY, "groups/delete/#", GROUPS_SINGLE_DELETE);

	}
	
	private GroupOpenHelper mOpenHelper;
	private final String GROUPS_TABLE = "groups";	// 群组表名
	private final String THREAD_GROUP_TABLE = "thread_group";	// 关联关系表名
	
	@Override
	public boolean onCreate() {
		mOpenHelper=GroupOpenHelper.getInstance(getContext());		
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case GROUPS_QUERY_ALL:		// 查询所有的群组数据
			if(db.isOpen()) {
				Cursor cursor = db.query(GROUPS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
				// 给游标结果集设置一个通知的uri
				cursor.setNotificationUri(getContext().getContentResolver(), Sms.GROUPS_QUERY_ALL_URI);
				return cursor;
			}
		case THREAD_GROUP_QUERY_ALL:	// 查询所有关联关系表的内容
			if(db.isOpen()) {
				Cursor cursor = db.query(THREAD_GROUP_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
				return cursor;
			}
			break;
		default:
			throw new IllegalArgumentException("UnKnow Uri : " + uri);
		}
		return null;

	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case GROUPS_INSERT:	// 添加到群组
			if(db.isOpen()) {
				long id = db.insert(GROUPS_TABLE, null, values);  //返回添加到数据库哪行
				// 通知Sms.GROUPS_QUERY_ALL_URI, 数据改变了, 它会执行重新查询操作, 更新数据
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_ALL_URI, null);
				return ContentUris.withAppendedId(uri, id);
			}
			break;
		case THREAD_GROUP_INSERT:		// 添加到关联关系表中
			if(db.isOpen()) {
				long id = db.insert(THREAD_GROUP_TABLE, null, values);
				return ContentUris.withAppendedId(uri, id);
			}
			break;
		default:
			throw new IllegalArgumentException("UnKnow Uri : " + uri);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
