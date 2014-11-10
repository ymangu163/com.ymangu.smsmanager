package com.ymangu.smsmanager;


import com.ymangu.smsmanager.view.ConversationUI;
import com.ymangu.smsmanager.view.FolderUI;
import com.ymangu.smsmanager.view.GroupUI;

import android.os.Bundle;

import android.app.TabActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

	private TabHost mTabHost;
	private View mSlideView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  //去标题
		
		setContentView(R.layout.activity_main);
		initTabHost();
		
		
		
		
		
		
		
		
		
	}
	
	/**
	 * 初始化tabhost
	 */
	private void initTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		
		mSlideView = findViewById(R.id.slide_view);   // 页签的滑动背景
		final View llConversation = findViewById(R.id.ll_conversation); //一个页签的布局
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


}
