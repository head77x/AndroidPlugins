package com.netmego.miguyijieonlineunity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.netmego.miguyijieonlineunity.MegoActivity;
import com.snowfish.cn.ganga.helper.SFOnlineSplashActivity;

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
