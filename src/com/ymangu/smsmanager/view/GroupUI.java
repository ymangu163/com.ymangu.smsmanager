package com.ymangu.smsmanager.view;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ymangu.smsmanager.R;
import com.ymangu.smsmanager.utils.CommonAsyncQuery;
import com.ymangu.smsmanager.utils.Sms;

public class GroupUI extends ListActivity implements OnItemClickListener, OnItemLongClickListener {
	
	
	private GroupAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		prepareData();
		
	}
	private void prepareData() {
		
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		asyncQuery.startQuery(0, mAdapter, Sms.GROUPS_QUERY_ALL_URI, null, null, null, null);		
	}
	private void init() {
		ListView mListView = getListView();
		mAdapter = new GroupAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		
		
	}
	/**
	 * 功能：在xml 中定义菜单，在java中通过inflate找到
	 **/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.create_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_create_group) {
//			Log.i(TAG, "创建群组");			
			showCreateGroupDialog();
		}
		return super.onOptionsItemSelected(item);
	}	
	
	/** 弹出新建群组对话框	 */
	private void showCreateGroupDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("新建群组");
		final AlertDialog dialog = builder.create();
		
		View view = View.inflate(this, R.layout.create_group_layout, null);
		final EditText etName = (EditText) view.findViewById(R.id.et_create_group_name);
		view.findViewById(R.id.btn_create_group).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String groupName = etName.getText().toString();
				if(!TextUtils.isEmpty(groupName)) {
					createGroup(groupName);
					dialog.dismiss();
				}
					}

				});

		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();

		// 获得对话框窗体的属性
		LayoutParams lp = dialog.getWindow().getAttributes();

		// 整个屏幕的宽度

		lp.width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.7);

		dialog.getWindow().setAttributes(lp);

	}
	
	/**
	 * 创建群组
	 * @param groupName
	 */
	protected void createGroup(String groupName) {
		ContentValues values = new ContentValues();
		values.put("group_name", groupName);
		Uri uri = getContentResolver().insert(Sms.GROUPS_INSERT_URI, values);
		//失败时，返回-1
		if(ContentUris.parseId(uri) >= 0) {
			Toast.makeText(this, "群组创建成功", 0).show();
		}
	}
	
	/**
	 * CursorAdapter 在查询系统的数据库时会自动 更新，要实现自己的数据库也自动更新 要添加 两条代码
	 * 
	 * 
	 **/
	
	class GroupAdapter extends CursorAdapter {

		public GroupAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.group_item, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tvName = (TextView) view.findViewById(R.id.tv_group_item_name);
			//得到 group_name 列的String值
			tvName.setText(cursor.getString(cursor.getColumnIndex("group_name")));
		}}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return false;
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
	}
	
	
	
	
	

}
