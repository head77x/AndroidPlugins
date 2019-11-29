package com.netmego.miguyouxisdk;

import com.snowfish.cn.ganga.helper.SFOnlineSplashActivity;

import android.content.Intent;
import android.graphics.Color;


public class YiJieSplash extends SFOnlineSplashActivity 
{  
	public int getBackgroundColor() 
	{  
		// 返回闪屏的背景颜色    
		return Color.WHITE;    
	}
	
	@Override  public void onSplashStop() 
	{  
		// 闪屏结束进入游戏        
		Intent intent = new Intent(YiJieSplash.this, MegoActivity.class);    
		startActivity(intent);    
		finish();
	}
}