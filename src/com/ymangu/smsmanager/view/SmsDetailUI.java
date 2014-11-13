package com.ymangu.smsmanager.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ymangu.smsmanager.R;
import com.ymangu.smsmanager.utils.Utils;

public class SmsDetailUI extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_detail);
		initView();
		
		
		
		
	}

	private void initView() {
		ImageView ivIcon=(ImageView) findViewById(R.id.iv_sms_detail_icon);
		TextView tvName=(TextView) findViewById(R.id.tv_sms_detail_name);
		TextView tvAddress=(TextView) findViewById(R.id.tv_sms_detail_address);
		TextView tvType=(TextView) findViewById(R.id.tv_sms_detail_type);
		TextView tvDate=(TextView) findViewById(R.id.tv_sms_detail_date);
		TextView tvBody=(TextView) findViewById(R.id.tv_sms_detail_body);
		
		Intent intent=getIntent();
		int index=intent.getIntExtra("index", -1);
		String address=intent.getStringExtra("address");
		String body=intent.getStringExtra("body");
		long date=intent.getLongExtra("date", -1);
		
		
		//根据号码查联系人姓名
		String contactName = Utils.getContactName(getContentResolver(), address);
		if(TextUtils.isEmpty(contactName)){
			//显示号码
			tvName.setText("");
			ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
		}else{
			//显示名称
			tvName.setText(contactName);
			
			//根据号码查询联系人头像
			Bitmap contactIcon=Utils.getContactIcon(getContentResolver(), address);
			if(contactIcon!=null){
				ivIcon.setBackgroundDrawable(new BitmapDrawable(contactIcon));
			}else{
				ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
			}
		}
		
		String strDate=null;
		if(DateUtils.isToday(date)){
			strDate = DateFormat.getTimeFormat(this).format(date);
			
		}else{
			//显示日期
			strDate = DateFormat.getDateFormat(this).format(date);
			
		}
		tvDate.setText(strDate);
		tvAddress.setText(address);
		
		//设置类型
		switch(index){
		case 0:
			tvType.setText("接收于：");
			break;
		case 1:
			tvType.setText("正在发送中于：");
			break;
		case 2:
			tvType.setText("发送于：");
			break;
		case 3:
			tvType.setText("存储于：");
			break;
		}
		tvBody.setText(body); 
		
		
	}
	
	

}
