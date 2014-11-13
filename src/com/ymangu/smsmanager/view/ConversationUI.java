package com.ymangu.smsmanager.view;

import java.util.HashSet;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ymangu.smsmanager.R;
import com.ymangu.smsmanager.utils.CommonAsyncQuery;
import com.ymangu.smsmanager.utils.Sms;
import com.ymangu.smsmanager.utils.Utils;
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


/**
 * 版本流程说明：
 * ① oncreate的initView()中找到ListView，给它设置CursorAdapter，先把cursor传null 
 * ② 紧着着调用prepareData()，启动AsyncQueryHandler，把cursorAdapter传递过去
 * ③ 查询完毕后，cursor结果会传给 onQueryComplete(),cookie就是传过来的cursorAdapter对象
 * ④  adapter.changeCursor(cursor); 刷新数据
 * ⑤ 调用刷新之后，就会调用 cursorAdapter 中的newView和bindView方法来回的去绑定数据
 * 
 **/
public class ConversationUI extends Activity implements OnItemClickListener, OnClickListener {
	
	//CursorAdapter 必须有一列名字是"_id"
	private String[] projection = {
			"sms.thread_id AS _id",
			"sms.body AS body",
			"groups.msg_count AS count",
			"sms.date AS date",
			"sms.address AS address"
	};
	
	private final int THREAD_ID_COLUMN_INDEX=0;
	private final int BODY_COLUMN_INDEX=1;
	private final int COUNT_COLUMN_INDEX=2;
	private final int DATE_COLUMN_INDEX=3;
	private final int ADDRESS_COLUMN_INDEX=4;
	
	
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
//		Cursor cursor = getContentResolver().query(Sms.CONVERSATION_URI, projection, null, null, "date desc");
//		Utils.printCursor(cursor);
		
		initView();
		prepareData();
		
			
	}
	
	private ConversationAdapter mAdapter;
	
	private Button btnNewMessage;
	private Button btnSelectAll;
	private Button btnCancelSelect;
	private Button btnDeleteMessage;
	private ListView mListView;
	private ProgressDialog mProgressDialog;
	private boolean isStop = false;		// 是否停止

	private static final int SEARCH_ID = 0;
	private static final int EDIT_ID = 1;
	private static final int CANCEL_EDIT_ID = 2;
	
	private final int LIST_STATE = -1;
	private final int EDIT_STATE = -2;
	private int currentState = LIST_STATE;		// 当前默认的状态为列表状态
	private HashSet<Integer> mMultiDeleteSet;	//HashSet有个特点就是值不能重复
	
	
	private void initView() {
		mMultiDeleteSet = new HashSet<Integer>();
		
		mListView = (ListView) findViewById(R.id.lv_conversation);
		btnNewMessage = (Button) findViewById(R.id.btn_conversation_new_message);
		btnSelectAll = (Button) findViewById(R.id.btn_conversation_select_all);
		btnCancelSelect = (Button) findViewById(R.id.btn_conversation_cancel_select);
		btnDeleteMessage = (Button) findViewById(R.id.btn_conversation_delete_message);
		
		btnNewMessage.setOnClickListener(this);
		btnSelectAll.setOnClickListener(this);
		btnCancelSelect.setOnClickListener(this);
		btnDeleteMessage.setOnClickListener(this);
		
		/**
		 * 功能：给ListView 设置适配器 和 item点击监听方法
		 **/
		mAdapter = new ConversationAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		
		
	}

	/**
	 * 异步查询数据 ,查询完后 会跳到 asyncQuery.onQueryComplete()
	 * startQuery(int token, Object cookie, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy)
	 * token	令牌, 第几次查询
	 * cookie	传一个adapter
	 * uri		去哪查询数据
	 * projection	查询哪些列
	 * selection	where 条件 ?
	 * selectionArgs  where 条件的参数 ,替换掉 ?
	 * orderBy  	排序
	 */
	private void prepareData() {
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		asyncQuery.startQuery(0, mAdapter, Sms.CONVERSATION_URI, projection, null, null, "date desc"); 
	}
		
		
		
		
	/**
	 * 功能：① 如果用BaseAdapter，要先把数据一个个取出来，放到List中，再在getView 中赋值
	 *  使用 CursorAdapter 可以省略这步。
	 *  ② 只要数据库一改变，cursor 自动更新，完全自动 的，不需要我们做任务操作
	 **/	
	class ConversationAdapter extends CursorAdapter{
		private ConversationHolderView mHolder;
		private String strDate;  
		public ConversationAdapter(Context context, Cursor c) {
			super(context, c); 			
		}
		
		/**
		 * 功能：通过 xml布局 返回一个View
		 **/
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = View.inflate(context, R.layout.conversation_item, null);
			mHolder=new ConversationHolderView();
			mHolder.ivIcon=(ImageView) view.findViewById(R.id.iv_conversation_item_icon); 
			mHolder.tvName=(TextView) view.findViewById(R.id.tv_conversation_item_name);
			mHolder.tvDate=(TextView) view.findViewById(R.id.tv_conversation_item_date);
			mHolder.tvBody=(TextView) view.findViewById(R.id.tv_conversation_item_body);
			mHolder.checkBox=(CheckBox) view.findViewById(R.id.cb_conversation_item);
			view.setTag(mHolder);
			return view;
		}
		
		/**
		 * 功能：绑定数据
		 *  view 为 newView()返回的view
		 **/
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder=(ConversationHolderView) view.getTag();			
			int id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			int count = cursor.getInt(COUNT_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);			
			

			// 判断当前的状态是否是编辑
			if(currentState == EDIT_STATE) {
				// 显示checkbox
				mHolder.checkBox.setVisibility(View.VISIBLE);
				
				// 当前的会话id是否存在与deleteSet集合中
				mHolder.checkBox.setChecked(mMultiDeleteSet.contains(id));
			} else {
				// 隐藏checkbox
				mHolder.checkBox.setVisibility(View.GONE);
			}
			
			
			
			//根据号码查联系人姓名
			String contactName = Utils.getContactName(getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)){
				//显示号码
				mHolder.tvName.setText(address+"("+count+")");
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			}else{
				//显示名称
				mHolder.tvName.setText(contactName+"("+count+")");
				
				//根据号码查询联系人头像
				Bitmap contactIcon=Utils.getContactIcon(getContentResolver(), address);
				if(contactIcon!=null){
					mHolder.ivIcon.setBackgroundDrawable(new BitmapDrawable(contactIcon));
				}else{
					mHolder.ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
				}
			}
			if(DateUtils.isToday(date)){
				strDate = DateFormat.getTimeFormat(context).format(date);
				
			}else{
				//显示日期
				strDate = DateFormat.getDateFormat(context).format(date);
				
			}
			mHolder.tvDate.setText(strDate);
			mHolder.tvBody.setText(body);
			
		}
		
		
		public class ConversationHolderView {
			public CheckBox checkBox;
			public ImageView ivIcon;
			public TextView tvName;
			public TextView tvDate;
			public TextView tvBody;
		}
		
		
	}
	/**
	 * 此方法是创建options菜单调用, 只会被调用一次
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SEARCH_ID, 0, "搜索");
		menu.add(0, EDIT_ID, 0, "编辑");
		menu.add(0, CANCEL_EDIT_ID, 0, "取消编辑");	
		
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * 当菜单将要显示在屏幕上时, 回调此方法
	 * 控制显示哪一个菜单
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(currentState == EDIT_STATE) {
			// 显示取消编辑, 隐藏另外两个
			menu.findItem(SEARCH_ID).setVisible(false);
			menu.findItem(EDIT_ID).setVisible(false);
			menu.findItem(CANCEL_EDIT_ID).setVisible(true);
		} else {
			menu.findItem(SEARCH_ID).setVisible(true);
			menu.findItem(EDIT_ID).setVisible(true);
			menu.findItem(CANCEL_EDIT_ID).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	/**
	 * 当options菜单被选中时回调
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case SEARCH_ID:		// 搜索菜单被选中
			
			break;
		case EDIT_ID:		// 编辑菜单
			currentState = EDIT_STATE;
			refreshState();
			break;
		case CANCEL_EDIT_ID:	// 取消编辑
			currentState = LIST_STATE;
			mMultiDeleteSet.clear();
			refreshState();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * 刷新状态
	 * 问1： 我们点击菜单，ListView 会更新吗？（checkBox等视图）
	 * 答：当Listview的高度等参数变化后  就会去重绘，就会回调CusorAdapter.
	 */
	private void refreshState() {
		if(currentState == EDIT_STATE) {
			// 新建信息隐藏, 其他按钮显示, 每一个item都要显示一个checkBox 
			btnNewMessage.setVisibility(View.GONE);
			btnSelectAll.setVisibility(View.VISIBLE);
			btnCancelSelect.setVisibility(View.VISIBLE);
			btnDeleteMessage.setVisibility(View.VISIBLE);
			
			if(mMultiDeleteSet.size() == 0) {
				// 没有选中任何checkbox
				btnCancelSelect.setEnabled(false);
				btnDeleteMessage.setEnabled(false);
			} else {
				btnCancelSelect.setEnabled(true);
				btnDeleteMessage.setEnabled(true);
			}
			
			// 全选按钮状态
			btnSelectAll.setEnabled(mMultiDeleteSet.size()!= (mListView.getCount()-1));
			Log.d("111", ""+mMultiDeleteSet.size()+"---->   "+mListView.getCount());
			
		} else {
			// 新建信息显示, 其他的隐藏
			btnNewMessage.setVisibility(View.VISIBLE);
			btnSelectAll.setVisibility(View.GONE);
			btnCancelSelect.setVisibility(View.GONE);
			btnDeleteMessage.setVisibility(View.GONE);
		}		
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
				// 把当前被点击的item的会话id添加到集合中, 刷新checkbox
				Cursor cursor = mAdapter.getCursor();
				// 移动到当前被点的索引
				cursor.moveToPosition(position);
				
				// 会话的id
				int thread_id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
				String address = cursor.getString(ADDRESS_COLUMN_INDEX);
				
				if(currentState == EDIT_STATE) {
					
					CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_conversation_item);
					
					if(checkBox.isChecked()) {
						// 移除id
						mMultiDeleteSet.remove(thread_id);
					} else {
						mMultiDeleteSet.add(thread_id);
					}
					//更改checkBox 状态
					checkBox.setChecked(!checkBox.isChecked());
					
					// 每一次点击刷新一下按钮的状态
					refreshState();
				} else {
					Intent intent = new Intent(this, ConversationDetailUI.class);
					intent.putExtra("thread_id", thread_id);
					intent.putExtra("address", address);
					startActivity(intent);
				}
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.btn_conversation_new_message: // 新建信息
			startActivity(new Intent(this, NewMessageUI.class));
			break;
		case R.id.btn_conversation_select_all: // 全选
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(-1);		// 复位到初始的位置
			//把所有的会话id 都加到集合中
			while(cursor.moveToNext()) {
				mMultiDeleteSet.add(cursor.getInt(THREAD_ID_COLUMN_INDEX));
			}
			mAdapter.notifyDataSetChanged();	// 刷新数据
			refreshState();
			break;
		case R.id.btn_conversation_cancel_select: // 取消选择
			mMultiDeleteSet.clear();
			mAdapter.notifyDataSetChanged();	// 刷新数据
			refreshState();
			break;
		case R.id.btn_conversation_delete_message: // 删除信息
			showConfirmDeleteDialog();
			break;
		default:
			break;
		}
		
		
	}
	protected static final String TAG = "ConversationUI";
	/**
	 * 确认删除对话框
	 */
	private void showConfirmDeleteDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);		// 设置图标
		builder.setTitle("删除");
		builder.setMessage("确认删除选中的会话吗?");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "确认删除");
				
				// 弹出进度对话框
				showDeleteProgressDialog();
				isStop = false;
				// 开启子线程, 真正删除短信, 每删除一条短信, 更新进度条
				new Thread(new DeleteRunnable()).start();
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();	
	}	
		
	// 删除会话
	class DeleteRunnable implements Runnable{

		@Override
		public void run() {
			Iterator<Integer> iterator=mMultiDeleteSet.iterator();
			int thread_id;
			String where;
			String[] selectionArgs;
			while(iterator.hasNext()){
				thread_id=iterator.next();
				where="thread_id=?";
				selectionArgs=new String[] {String.valueOf(thread_id)};
				getContentResolver().delete(Sms.SMS_URI, where, selectionArgs);
				SystemClock.sleep(1000);
				
				//更新进度条
				mProgressDialog.incrementProgressBy(1);
				
			}
			//全部删除完后的操作
			mMultiDeleteSet.clear();
			mProgressDialog.dismiss();
			
		}
	}
	
	
	
	
	
	/**
	 * 弹出删除进度对话框
	 */
	@SuppressWarnings("deprecation")
	protected void showDeleteProgressDialog() {
		mProgressDialog = new ProgressDialog(this);
		// 设置最大值
		mProgressDialog.setMax(mMultiDeleteSet.size());
		// 设置进度条的演示为长条
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "终止删除");
				isStop = true;
			}
		});
		mProgressDialog.show();
		//对话框关闭事件的监听
		mProgressDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				currentState = LIST_STATE;
				refreshState();
			}
		});
		
		
	}

	/**
	 * 功能：按下返回键时的回调函数
	 **/	
	@Override
	public void onBackPressed() {
		if(currentState == EDIT_STATE) {
			currentState = LIST_STATE;
			mMultiDeleteSet.clear();
			refreshState();
			return;
		}
		super.onBackPressed();		
	}		
	
	
	
}
