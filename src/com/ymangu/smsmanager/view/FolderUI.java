package com.ymangu.smsmanager.view;


import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ymangu.smsmanager.R;
import com.ymangu.smsmanager.utils.CommonAsyncQuery;
import com.ymangu.smsmanager.utils.CommonAsyncQuery.OnQueryNotifyCompleteListener;
import com.ymangu.smsmanager.utils.Utils;

/**
 * 接口的用法：
*	① 定义一个接口B，定义它的方法；
*	② 在一个类A中，定义一个接口B的对象b，用这个对象b去调用接口B的方法b.xxx()；在A中还要实现get/set 这个对象b的方法；
*	③ 在类C中生成了 A类的对象a，然后调用a.setxx() 设置接口B的监听，并实现接口B的方法；
*	这样，在A类中原来执行 b.xxx()方法的地方就会去执行C类实现的接口B的方法。
 **/


/**
 * 功能：页面上只有一个ListView
 **/
public class FolderUI extends ListActivity implements OnQueryNotifyCompleteListener, OnItemClickListener {
	private int[] imageIDs;
	private String[] typeArrays;
	private HashMap<Integer, Integer> countMap;
	private FolderAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initView();
		
		
		
	}

	private void initView() {
	ListView mListView = getListView();
		
		imageIDs = new int[] {
				R.drawable.a_f_inbox,
				R.drawable.a_f_outbox,
				R.drawable.a_f_sent,
				R.drawable.a_f_draft
		};
		
		typeArrays = new String[] {
				"收件箱",
				"发件箱",
				"已发送",
				"草稿箱"
		};
		countMap = new HashMap<Integer, Integer>();
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		//设置查询完成时监听 ，去刷新数据
		asyncQuery.setOnQueryNotifyCompleteListener(this);
		
		Uri uri;
		for (int i = 0; i < 4; i++) {
			countMap.put(i, 0);
			
			uri = Utils.getUriFromIndex(i);
			asyncQuery.startQuery(i, null, uri, new String[]{"count(*)"}, null, null, null);
		}
		mAdapter = new FolderAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		
	}

	
	
	class FolderAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return imageIDs.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			
			
			
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if(convertView == null) {
				view = View.inflate(FolderUI.this, R.layout.folder_item, null);
			} else {
				view = convertView;
			}
			
			ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_folder_item_icon);
			TextView tvType = (TextView) view.findViewById(R.id.tv_folder_item_type);
			TextView tvCount = (TextView) view.findViewById(R.id.tv_folder_item_count);
			
			ivIcon.setImageResource(imageIDs[position]);
			tvType.setText(typeArrays[position]);
			tvCount.setText(countMap.get(position) + "");
			return view;
		}		
	}



	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		
	}

	@Override
	public void onPostNotify(int token, Object cookie, Cursor cursor) {
		if(cursor != null && cursor.moveToFirst()) {
			countMap.put(token, cursor.getInt(0));
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		Intent intent = new Intent(this, FolderDetailUI.class);
		intent.putExtra("index", position);
		startActivity(intent);
		
		
	}
	
	
	
	

}
