package com.netmego.telepaylinker;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import android.os.Bundle;
import android.os.Handler;

import cn.egame.terminal.smspay.EgamePay;
import cn.egame.terminal.smspay.EgamePayListener;

public class MegoActivity extends UnityPlayerActivity 
{
	private Handler postHandler = new Handler(); 
  	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
	
	
	public void onResume() 
	{  
        super.onResume();  
    }
	
    public void onPause() 
    {  
    	super.onPause();  
    }  
	
	public void OnBuy(final String alias, final String callbackGameObject, final String callbackFunc )
	{
		postHandler.post( new Runnable()
		{
			public void run()
			{
				EgamePayListener fff = new EgamePayListener()
				{
					@Override
					public void paySuccess(String alias) {
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, alias + ", 1" );
					}

					@Override
					public void payFailed(String alias, int errorInt) {
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, alias + ", 2," + errorInt );
					}

					@Override
					public void payCancel(String alias) {
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, alias + ", 3" );
					}
					
				};
				
				EgamePay.pay(MegoActivity.this, alias, fff );
			}
		});
	}
    
    
}


