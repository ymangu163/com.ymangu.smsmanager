package com.ymangu.smsmanager.view;

import java.util.HashSet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
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
	private HashSet<Integer> mMultiDeleteSet;
	
	
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
	 * token	令牌
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
			
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			
			
			mHolder.tvName.setText(address);
		}
		
		
		public class ConversationHolderView {
			public CheckBox checkBox;
			public ImageView ivIcon;
			public TextView tvName;
			public TextView tvDate;
			public TextView tvBody;
		}
		
		
	}




	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		
	}

	@Override
	public void onClick(View v) {
		
		
		
		
	}	
		
		
			
	
	
	
}
