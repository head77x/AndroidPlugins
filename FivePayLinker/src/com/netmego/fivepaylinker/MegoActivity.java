package com.netmego.fivepaylinker;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import android.os.Handler;
import android.os.Bundle;

import billingSDK.billingDemo.SmsPayFactory;
import billingSDK.billingDemo.SmsPayFactory.SmsExitGameListener;
import billingSDK.billingDemo.SmsPayFactory.SmsPurchaseListener;

public class MegoActivity extends UnityPlayerActivity 
{
	private Handler postHandler = new Handler(); 
  	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		OnInit();
	}
	
	public void OnInit()
	{
		System.out.println("5 wang init");

		postHandler.post( new Runnable()
		{
			public void run()
			{
				SmsPayFactory.init(MegoActivity.this);
			}
		});
	}
	
	
	
	public void OnBuy(final int ItemIndex, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("5 wang on buy :" + ItemIndex);

		postHandler.post( new Runnable()
		{
			public void run()
			{
				SmsPurchaseListener fff = new SmsPurchaseListener()
				{
					@Override
					public void onPurchaseSucceed() 
					{
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, "0|0|success" );
					}
					
					@Override
					public void onPurchaseInfo(String msg) 
					{
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, "0|0|" + msg );
					}
					
					@Override
					public void onPurchaseFailed(String msg) 
					{
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, "1|1|" + msg );
					}
					
					@Override
					public void onPurchaseCanceld() 
					{
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, "3|3|canceled" );
					}
				};
					
				SmsPayFactory.getInstance().pay(MegoActivity.this, ItemIndex, fff, false);
			}
		});
	}
	
    public void viewMoreGames()
    {
    	SmsPayFactory.getInstance().viewMoreGames(MegoActivity.this);
    	
    }
	
    public boolean isMusicEnabled()
    {
    	return SmsPayFactory.getInstance().isMusicEnabled();
    }
	
  	public void exitApp()
  	{
		postHandler.post( new Runnable()
		{
			public void run()
			{
		  		
		    	SmsPayFactory.getInstance().exitGame(MegoActivity.this, new SmsExitGameListener() {
		          @Override
		          public void onExitGameConfirmExit() {
		        	  MegoActivity.this.finish();
		          }
		
		          @Override
		          public void onExitGameCancelExit() 
		          {
		          }
		      });
			}
		});
  	}

}
