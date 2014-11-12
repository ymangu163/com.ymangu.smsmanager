package com.ymangu.smsmanager.utils;

import java.io.InputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class Utils {
	private static final String TAG = "Utils";
	
	/**
	 * 输出游标结果集
	 * @param cursor
	 */
	public static void printCursor(Cursor cursor) {
		if(cursor != null && cursor.getCount() > 0) {
			String columnName;
			String columnValue;
			while(cursor.moveToNext()) {
				//得到第i列的列名和值
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					columnName = cursor.getColumnName(i);
					columnValue = cursor.getString(i);
					Log.i(TAG, "第" + cursor.getPosition() + "行: " + columnName + " = " + columnValue);
				}
			}
			
			cursor.close();
		}
	}
	
	/**
	 * 根据号码获取联系人的姓名
	 */
	public static String getContactName(ContentResolver resolver, String address) {
		// content://com.android.contacts/phone_lookup/95556
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address);  //拼凑 string类型的 Uri
		Cursor cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
		if(cursor!=null && cursor.moveToFirst()){
			String contactName = cursor.getString(0); //只有一列
			cursor.close();
			return contactName;
		}		
		return null;
	}
	
	/**
	 * 根据联系人的号码查询联系人的头像
	 * */
	public static Bitmap getContactIcon(ContentResolver resolver, String address) {
		
		// 1.根据号码取得联系人的id
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address);
		
		Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
		
		if(cursor != null && cursor.moveToFirst()) {
			long id = cursor.getLong(0);
			cursor.close();
			
			// 2.根据id获取联系人的头像
			
			uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id); //拼凑long类型的Uri
			InputStream is = Contacts.openContactPhotoInputStream(resolver, uri);
			return BitmapFactory.decodeStream(is);
		}
		return null;
	}
	
	
	
	
	
}
