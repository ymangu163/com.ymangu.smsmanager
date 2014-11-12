package com.ymangu.smsmanager.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ymangu.smsmanager.R;
import com.ymangu.smsmanager.utils.CommonAsyncQuery;
import com.ymangu.smsmanager.utils.CommonAsyncQuery.OnQueryNotifyCompleteListener;
import com.ymangu.smsmanager.utils.Sms;
import com.ymangu.smsmanager.utils.Utils;

public class ConversationDetailUI extends Activity  implements OnQueryNotifyCompleteListener, OnClickListener{
	
	private ConversationDetailAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.conversation_detail);
		
		initTitle();
		initView();  //先设置好一个cursor为空的CursorAdapter
		prepareData(); //紧接着为它提供数据
		
	}
	
	private String []  projection={
			"_id",
			"body",
			"date",
			"type"
	};
	private final int ID_COLUMN_INDEX = 0;
	private final int BODY_COLUMN_INDEX = 1;
	private final int DATE_COLUMN_INDEX = 2;
	private final int TYPE_COLUMN_INDEX = 3;
	private int thread_id;
	private ListView mListView;
	private EditText etContent;
	private String address;

	
	private void prepareData() {
		CommonAsyncQuery asyncQuery=new CommonAsyncQuery(getContentResolver());
		//要在这里设置滚动动最下面的监听
		asyncQuery.setOnQueryNotifyCompleteListener(this);
		String[] selectionArgs={
				String.valueOf(thread_id)
		};
		String selection="thread_id=?";
		asyncQuery.startQuery(0, mAdapter, Sms.SMS_URI, projection, selection, selectionArgs, "date");
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.lv_conversation_detail_sms);
		etContent = (EditText) findViewById(R.id.et_conversation_detail_content);
		findViewById(R.id.btn_conversation_detail_send).setOnClickListener(this);
		findViewById(R.id.btn_conversation_detail_back).setOnClickListener(this);
		
		mAdapter = new ConversationDetailAdapter(this, null);
		mListView.setAdapter(mAdapter);
		
		
		
	}
	/**
	 * 功能：设置详细页的title 为对方姓名 
	 **/
	private void initTitle() {
		Intent intent=getIntent();
		thread_id = intent.getIntExtra("thread_id", -1);
		address = intent.getStringExtra("address");
		
		String contactName = Utils.getContactName(getContentResolver(), address);
		TextView tvName=(TextView) findViewById(R.id.tv_conversation_detail_name);
		if(TextUtils.isEmpty(contactName)){
			tvName.setText(address);
		}else{
			tvName.setText(contactName);
		}
	}
	/**
	 * 功能：给 ListView 提供数据的 CursorAdapter
	 **/
	class ConversationDetailAdapter extends CursorAdapter{
		private ConversationDetailViewHolder mHolder;
		
		
		public ConversationDetailAdapter(Context context, Cursor c) {
			super(context, c);
		}
		
		/***  当游标结果集内容改变时回调. 
		 *  发送短信后，滚动到最后
		 *  */
		@Override
		protected void onContentChanged() {
			super.onContentChanged();  // 执行完成之后, 数据才更新到adapter
			
			// 把listview滚动到底部
			mListView.setSelection(mListView.getCount());
			
		}
		
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			mHolder = new ConversationDetailViewHolder();
			View view = View.inflate(context, R.layout.conversation_detail_item, null);
			mHolder.receiveView = view.findViewById(R.id.tl_conversation_detail_item_receive);
			mHolder.tvReceiveBody = (TextView) view.findViewById(R.id.tv_conversation_detail_item_receive_body);
			mHolder.tvReceiveDate = (TextView) view.findViewById(R.id.tv_conversation_detail_item_receive_date);
			
			mHolder.sendView = view.findViewById(R.id.tl_conversation_detail_item_send);
			mHolder.tvSendBody = (TextView) view.findViewById(R.id.tv_conversation_detail_item_send_body);
			mHolder.tvSendDate = (TextView) view.findViewById(R.id.tv_conversation_detail_item_send_date);
			
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder = (ConversationDetailViewHolder) view.getTag();
			String body = cursor.getString(BODY_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			int type = cursor.getInt(TYPE_COLUMN_INDEX);
			
			// 处理时间
			String strDate = null;
		    if(DateUtils.isToday(date)) {
				strDate = DateFormat.getTimeFormat(context).format(date);
			} else {
					strDate = DateFormat.getDateFormat(context).format(date);
			}
			
			if(type == Sms.RECEVIE_TYPE) {
				// 显示的左边的起泡 receive
				mHolder.receiveView.setVisibility(View.VISIBLE);
				mHolder.sendView.setVisibility(View.GONE);
				
				mHolder.tvReceiveBody.setText(body);
				mHolder.tvReceiveDate.setText(strDate);
			} else {
				// 显示的右边的起泡 send
				mHolder.receiveView.setVisibility(View.GONE);
				mHolder.sendView.setVisibility(View.VISIBLE);
				
				mHolder.tvSendBody.setText(body);
				mHolder.tvSendDate.setText(strDate);
			}
			
			
		}
		
		
		
		
		public class ConversationDetailViewHolder{
			public View receiveView;
			public TextView tvReceiveBody;
			public TextView tvReceiveDate;
			
			public View sendView;
			public TextView tvSendBody;
			public TextView tvSendDate;			
		}
		
		
		
	}
	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		
	}
	
	/**
	 * adapter绑定数据完成之后回调
	 */
	@Override
	public void onPostNotify(int token, Object cookie, Cursor cursor) {
		mListView.setSelection(mListView.getCount());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_conversation_detail_back:
			finish();
			break;
		case R.id.btn_conversation_detail_send:
			String content = etContent.getText().toString();
			if(TextUtils.isEmpty(content)) {
				Toast.makeText(this, "请输入短信内容", 0).show();
				break;
			}
			//发送短信
			Utils.sendMessage(this, address, content);
			//发送完后，清空短信的内容
			etContent.setText("");
			break;
		default:
			break;
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

}
