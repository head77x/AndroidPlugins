package com.netmego.nativemigu;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

public class RTLogoSplash extends Activity
{
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_rtlogo);
        		
		Handler handler = new Handler()
		{
			public void handleMessage(Message msg){
				finish();
			}
		};
		
		handler.sendEmptyMessageDelayed(0, 2000);
	}
}
