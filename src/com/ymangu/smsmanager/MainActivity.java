package com.ymangu.smsmanager;


import com.ymangu.smsmanager.view.ConversationUI;
import com.ymangu.smsmanager.view.FolderUI;
import com.ymangu.smsmanager.view.GroupUI;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity implements OnClickListener {

	private TabHost mTabHost;
	private View mSlideView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  //去标题
		
		setContentView(R.layout.activity_main);
		initTabHost();
	    //创建快捷图标
		createShortCut();
	}
	
	/**
	 * 初始化tabhost
	 */
	private void initTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		
		mSlideView = findViewById(R.id.slide_view);   // 页签的滑动背景
		final View llConversation = findViewById(R.id.ll_conversation); //一个页签的布局
		
		//设置3个页签的监听事件
		llConversation.setOnClickListener(this);
		findViewById(R.id.ll_folder).setOnClickListener(this);
		findViewById(R.id.ll_group).setOnClickListener(this);
		
		/**
		 * 功能：初始化滑动背景的宽和高
		 **/
		/**
		 * 功能： 获得视图树的观察者对象, 添加一个当全部布局(layout)完成时的监听事件
		 *   measure-->layout -->draw  只要布局中的组件大小等改变，就会调用这3个方法
		 * 
		 **/
		llConversation.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			//全局布局完成时回调.
			@Override
			public void onGlobalLayout() {
				// 移除全局布局的监听事件，这个this在OnGlobalLayoutListener类中，所以代表OnGlobalLayoutListener类。
				llConversation.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				
				// 得到会话布局的参数, 设置给滑动块
				
				//因为背景块是在RelativeLayout下，所以要导入RelativeLayout的包
				LayoutParams lp = (LayoutParams) mSlideView.getLayoutParams();  //得到这个View的布局参数
				lp.width = llConversation.getWidth();
				lp.height = llConversation.getHeight();
				lp.leftMargin = llConversation.getLeft();
				mSlideView.setLayoutParams(lp);  //初始化后，设置这个view的布局参数 
				
				basicWidth = findViewById(R.id.rl_conversation).getWidth(); //得到一等份的宽度，滑动背景时要用
			}
		});		
		
		addTabSpec("conversation","会话",R.drawable.tab_conversation,new Intent(this,ConversationUI.class));
		addTabSpec("folder", "文件夹", R.drawable.tab_folder, new Intent(this,FolderUI.class));
		addTabSpec("group", "群组", R.drawable.tab_group, new Intent(this,GroupUI.class));
	}


	/**
	 * 添加一个页签 : 3步曲
	 * ① 生成一个页签对象；②设置页签；③应用页签
	 * @param tag 标记
	 * @param label 标题
	 * @param icon 图标
	 * @param intent 指向的activity
	 */
	private void addTabSpec(String tag, String label, int icon, Intent intent) {
		//得到面签对象
		TabSpec newTabSpec = mTabHost.newTabSpec(tag);
		// 设置页签的标题和图标
		newTabSpec.setIndicator(label, getResources().getDrawable(icon));
		// 设置页签指向的显示内容问activity
		newTabSpec.setContent(intent);
		// 添加页签
		mTabHost.addTab(newTabSpec);
		
	}
	
	private int basicWidth = 0;	// 一等分的宽度
	private int startX = 0;		// 记住上一次移动完成之后的x轴的偏移量
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ll_conversation: // 切换到会话页签
			if(!"conversation".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("conversation");  //这就与前面添加的页签联系起来了
				startTranslateAnimation(startX, 0);
				startX = 0;
			}
			break;
		case R.id.ll_folder: // 切换到文件夹页签
			if(!"folder".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("folder");
				startTranslateAnimation(startX, basicWidth);
				startX = basicWidth;
			}
			break;
		case R.id.ll_group: // 切换到群组页签
			if(!"group".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("group");
				startTranslateAnimation(startX, basicWidth * 2);
				startX = basicWidth * 2;
			}
			break;
		default:
			break;
		
		}
		
		
	}
	
	/**
	 * 给滑动块执行唯一动画
	 * @param fromXDelta 开始位移x轴的偏移量,这些值 都是相对于自己的
	 * @param toXDelta	结束位移x轴的偏移量
	 */
	private void startTranslateAnimation(int fromXDelta, int toXDelta) {
		TranslateAnimation translateAnimation = new TranslateAnimation(fromXDelta, toXDelta, 0, 0); //Y轴不关心
		translateAnimation.setDuration(300);
		translateAnimation.setFillAfter(true);// 执行完成后停留在动画结束的位置上
		mSlideView.startAnimation(translateAnimation);
	}
	
	//创建快捷图标
	private void createShortCut(){
		//先判断该快捷图标是否存在
		if(isExist()){
			Log.d("1", "已有图标，返回了");
			return ;
		}
		Log.d("1", "运行到这了");
		Intent intent=new Intent();
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		Parcelable icon=Intent.ShortcutIconResource.fromContext(this, R.drawable.tab_folder);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);  //设置快捷方式的图标
		Log.d("1", "运行到这了222");
		//指定快捷方式的名称
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "短信管理器");		
		//指定快捷图标激活哪一个activity，只有主Activity才能被激活
		Intent intent2=new Intent();
		intent2.setAction(Intent.ACTION_MAIN);
		intent2.addCategory(Intent.CATEGORY_LAUNCHER);
		ComponentName component=new ComponentName(this,MainActivity.class);
		intent2.setComponent(component);		
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent2);
		sendBroadcast(intent);  //发送广播
		Log.d("1", "运行到这了333");
		
		
	}
	
	private boolean isExist(){
		boolean isExist=false;
		int version=getSdkVersion();
		Log.d("1", "版本号是" + version);
		Uri uri=null;
		if(version<2.0){
		 uri=Uri.parse("content://com.android.launcher.settings/favorites");			
		}else{
			uri=Uri.parse("content://com.android.launcher2.settings/favorites");
		}
		String selection=" title=? ";
		String[] selectionArgs=new String[]{"短信管理器"};
		Cursor cursor=getContentResolver().query(uri, null, selection, selectionArgs, null);
		if(cursor.getCount()>0){
			isExist=true;
		}
		cursor.close();		
		return isExist;
	}
	
	 /*** 得到当前系统sdk版本     */
	private int getSdkVersion(){		
		return android.os.Build.VERSION.SDK_INT;
	}
	
	
}
