package com.ymangu.smsmanager.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class GroupUI extends Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv=new TextView(this);
		tv.setText("组");
		setContentView(tv);
		
		
		
	}

	
	
	
	
	
	

}
