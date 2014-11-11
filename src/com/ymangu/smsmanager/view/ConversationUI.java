package com.ymangu.smsmanager.view;

import com.ymangu.smsmanager.R;
import com.ymangu.smsmanager.utils.Sms;
import com.ymangu.smsmanager.utils.Utils;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
/**
 *  在手机的 /data/data/com.android.providers.telephony/databases 下找到mmssms.db 短信数据库
 *  查找哪些列呢？
 *  系统需要的列：sms.body AS snippet,sms.thread_id AS thread_id,groups.msg_count AS msg_count,
 *  我们需要的列：sms.date,sms.address
 *  头像是根据号码到联系人里面查的
 *  AS --更名
 *	用户的列没有添加as关键字会报异常: Invalid column sms.date 
 *  用户查询: 在系统查询的基础上添加了: 自定义的列, 排序
 **/


public class ConversationUI extends Activity {
	private String[] projection = {
			"sms.thread_id AS _id",
			"sms.body AS body",
			"groups.msg_count AS count",
			"sms.date AS date",
			"sms.address AS address"
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation);
		
		/**
		 * 功能：使用内容分解者来查询ContentProvider提供的数据
		 * ① uri 获取数据的URI
		 * ② projection--要查询哪些列
		 * ③ selection --  查询条件
		 * ④ selectionArgs -- 
		 * ⑤ sortOrder 排序 ---order by{自动添加的} date{列名} desc{递减}
		 * 返回一个 Cursor 游标
		 **/
		Cursor cursor = getContentResolver().query(Sms.CONVERSATION_URI, projection, null, null, "date desc");
		Utils.printCursor(cursor);
	
	}

	
	
	
	
	
	
	
	
	
	
	
	
}
