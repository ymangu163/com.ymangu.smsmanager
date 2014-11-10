package com.ymangu.smsmanager;


import com.ymangu.smsmanager.view.ConversationUI;
import com.ymangu.smsmanager.view.FolderUI;
import com.ymangu.smsmanager.view.GroupUI;

import android.os.Bundle;

import android.app.TabActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

	private TabHost mTabHost;

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
