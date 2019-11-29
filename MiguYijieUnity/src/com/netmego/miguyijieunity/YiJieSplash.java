package com.netmego.miguyijieunity;

import com.snowfish.cn.ganga.offline.helper.SFGameSplashActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;


public class YiJieSplash extends SFGameSplashActivity 
{  
	public int getBackgroundColor() 
	{  
		// 返回闪屏的背景颜色    
		return Color.WHITE;    
	}
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        		
		// 闪屏结束进入游戏        
		Intent intent = new Intent(YiJieSplash.this, MegoActivity.class);    
		startActivity(intent);    
		finish();
	}
}	
