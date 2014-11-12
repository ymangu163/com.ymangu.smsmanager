package com.ymangu.smsmanager.utils;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
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
	
	
	/**
	 * 发送短信
	 */
	public static void sendMessage(Context context, String address, String content) {
		SmsManager smsManager = SmsManager.getDefault();
		
		// 以70个字符分割短信
		ArrayList<String> divideMessage = smsManager.divideMessage(content);
		
		// 必须设置为隐士intent
		Intent intent = new Intent("com.ymangu.smsmanager.receiver.ReceiveSmsBroadcastReceive");
		
		PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, 
				intent, PendingIntent.FLAG_ONE_SHOT);
		
		for (String sms : divideMessage) {
			smsManager.sendTextMessage(
					address, 	// 接收人的号码 
					null, 		// 短信中心的号码
					sms, 		// 短信的内容
					sentIntent, // 发送成功的回调广播，回调方式：延期意图(延期意图指向的是广播接收者)
					null); 		// 接收成功的回调广播
		}
		
		// 把 发出的短信 插入到数据库
		writeMessage(context, address, content);
	}

	private static void writeMessage(Context context, String address,
			String content) {
		ContentValues values=new ContentValues();
		values.put("address", address);
		values.put("body", content);		
		context.getContentResolver().insert(Sms.SENT_URI, values);
		
		
		
		
		
	}
	
	
	
}
