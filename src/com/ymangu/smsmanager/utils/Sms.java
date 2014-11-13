package com.ymangu.smsmanager.utils;

import android.net.Uri;

public class Sms {
	
	
	/*** 查询会话的uri	 */
	public static final Uri CONVERSATION_URI = Uri.parse("content://sms/conversations");
	/** 操作SMS表的uri	 */
	public static final Uri SMS_URI = Uri.parse("content://sms/");

	/**
	 * 收件箱的uri
	 */
	public static final Uri INBOX_URI = Uri.parse("content://sms/inbox");
	
	/**
	 * 发件箱的uri
	 */
	public static final Uri OUTBOX_URI = Uri.parse("content://sms/outbox");
	
	/**
	 * 已发送的uri
	 */
	public static final Uri SENT_URI = Uri.parse("content://sms/sent");
	
	/**
	 * 草稿箱的uri
	 */
	public static final Uri DRAFT_URI = Uri.parse("content://sms/draft");
	
	
	
	public static final int RECEVIE_TYPE = 1;	// 短信类型: 接收的
	public static final int SEND_TYPE = 2;		// 短信类型: 发送的

}
