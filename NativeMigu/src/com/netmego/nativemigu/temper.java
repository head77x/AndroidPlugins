package com.netmego.nativemigu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.netmego.nativemigu.MainActivity;
import com.snowfish.cn.ganga.helper.SFOnlineSplashActivity;

public class temper extends SFOnlineSplashActivity
{
	private Handler postHandler = new Handler(); 
	

	public int getBackgroundColor() {
		return Color.WHITE;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);
		
		Intent intent;
		
		Intent ring = new Intent(this, com.netmego.nativemigu.RTLogoSplash.class);
		this.startActivity(ring);
		
		Handler ringhandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				Intent intent = new Intent(temper.this, MainActivity.class);
				temper.this.startActivity(intent);
				finish();
			}
		};
		
		ringhandler.sendEmptyMessageDelayed(0, 2000);
	}
	
	@Override
	public void onSplashStop() {

	}
	
}
