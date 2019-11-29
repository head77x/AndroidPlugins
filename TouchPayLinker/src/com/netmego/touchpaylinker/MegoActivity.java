package com.netmego.touchpaylinker;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import android.os.Handler;
import android.os.Bundle;
//import android.content.Context;
//import android.widget.*;
import android.view.KeyEvent;

import com.chinagame.billing.GameInfoBean;
import com.chinagame.billing.TouchPay;
import com.chinagame.billing.TouchPay.ExitCallBack;
import com.chinagame.billing.TouchPay.PayCallBack;


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
        
	public void OnInit(final String uni_AppID, final String uni_CpCode, final String uni_CpID, final String CompanyName, final String QAPhone, final String AppName, final boolean OtherPay, final boolean CpOtherPay, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("init" + uni_AppID + ":"  + CompanyName);

		postHandler.post( new Runnable()
		{
			public void run()
			{
				PayCallBack fff = new PayCallBack()
				{
					public void onPayResult(int resultCode, int operator, String code, String desc) 
					{
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, resultCode + "|" + code + "|" + desc );
						System.out.println(resultCode + ":" + code + ":" + desc);
					}
				
				};
				
				
				GameInfoBean infoBean = new GameInfoBean(uni_CpID, uni_CpCode, uni_AppID, AppName, CompanyName, QAPhone, "UID");
				
				TouchPay.initSdk(MegoActivity.this, infoBean, true, false, fff);
			}
		});
	}
	
	public void OnBuy(final String cm_code, final String vaccode, final String customcode, final String itemName, final String moneyYuan, final String orderIdx, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("on buy");

		postHandler.post( new Runnable()
		{
			public void run()
			{
				PayCallBack fff = new PayCallBack()
				{
					@Override
		            public void onPayResult(int resultCode, int operator, String code, String desc) 
					{
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, resultCode + "|" + code + "|" + desc );
					}
				};

				 TouchPay.pay(MegoActivity.this, itemName, moneyYuan, cm_code, vaccode, customcode, orderIdx, fff);
			}
		});
		
		
	}
	
	public boolean isMusicEnabled()
	{
		int m = TouchPay.getGameMusicState();
		
		if ( m == TouchPay.MUSIC_ENABLE )
			return true;
		
		return false;
	}
	
	
	public void exitGame() 
	{
		postHandler.post( new Runnable()
		{
			public void run()
			{
				ExitCallBack fff = new ExitCallBack()
				{
					@Override
						public void onConfirmExit() {
							System.exit(0);
						}
					@Override
					public void onCancelExit() {
					}
				};
		
				TouchPay.exit(MegoActivity.this, fff);
			}
		});
	}
	
	 @Override
	  public boolean onKeyDown(int keyCode, KeyEvent event) 
	 {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	      exitGame();
	      return true;
	    }
	    return super.onKeyDown(keyCode, event);
	  }	
}


