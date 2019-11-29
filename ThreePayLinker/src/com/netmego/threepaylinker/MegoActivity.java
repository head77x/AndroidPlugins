package com.netmego.threepaylinker;


import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import android.os.Handler;
import android.os.Bundle;
// import android.view.KeyEvent;
//
import billingSDK.billingDemo.SmsPayFactory;
import billingSDK.billingDemo.SmsPayFactory.SmsExitGameListener;

import billingSDK.billingDemo.SmsPayFactory.SmsPurchaseListener;


public class MegoActivity extends UnityPlayerActivity 
{
	String _callbackGameObject;
	String _callbackFunc;
	
	boolean PromptExitInGame;
	
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
/*    
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) 
    {
    	PromptExitInGame = false;
    	SmsPayFactory.getInstance().exitGame(MegoActivity.this, new SmsExitGameListener() {
          @Override
          public void onExitGameConfirmExit() {
        	  MegoActivity.this.finish();
          }

          @Override
          public void onExitGameCancelExit() 
          {
          }
          
          @Override
          public void onExitGameInGame()
          {
        	  PromptExitInGame = true; 
          }
      });
    	
    	if ( PromptExitInGame == true )
    		return super.onKeyDown(keyCode, event);
    	
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }    
 */      
  	public void exit()
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
		          
		          @Override
		          public void onExitGameInGame()
		          {
		          }
		      });
			}
		});
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
		          
		          @Override
		          public void onExitGameInGame()
		          {
		          }
		      });
			}
		});
  	}
  	
    public int CheckMobile()
    {
    	return SmsPayFactory.getInstance().getMobileOperatorType(); 
    }
    
    public void viewMoreGames()
    {
    	SmsPayFactory.getInstance().viewMoreGames(MegoActivity.this);
    	
    }
	
    public boolean isMusicEnabled()
    {
    	return SmsPayFactory.getInstance().isMusicEnabled();
    }
    
	public void OnInit(final String uni_AppID, final String uni_CpCode, final String uni_CpID, final String ty_channelID, final String ty_seccode, final String CompanyName, final String QAPhone, final String AppName, final boolean OtherPay, final String MM_appID, final String MM_appkey, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("san wang init" + uni_AppID + ":"  + CompanyName);

		postHandler.post( new Runnable()
		{
			public void run()
			{
				SmsPayFactory.init(MegoActivity.this, AppName, uni_AppID, uni_CpCode, uni_CpID, ty_channelID, ty_seccode, CompanyName, QAPhone, OtherPay, MM_appID, MM_appkey );
				UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, "0|0|success" );
			}
		});
	}
	
	public void OnBuy(final String smsPayItem, final String vaccode, final String props, final String money, final String appID, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("san wang on buy :" + smsPayItem);
		_callbackGameObject = callbackGameObject;
		_callbackFunc = callbackFunc;

		postHandler.post( new Runnable()
		{
			public void run()
			{
				SmsPurchaseListener fff = new SmsPurchaseListener()
				{
					@Override
					public void onPurchaseSucceed() 
					{
						UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "0|0|success" );
					}
					
					@Override
					public void onPurchaseInfo(String msg) 
					{
						UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "0|0|" + msg );
					}
					
					@Override
					public void onPurchaseFailed(String msg) 
					{
						UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "1|1|" + msg );
					}
					
					@Override
					public void onPurchaseCanceld() 
					{
						UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "3|3|canceled" );
					}
				};
					
				SmsPayFactory.getInstance().pay(MegoActivity.this, smsPayItem, vaccode, props, money, appID, fff, true);
			}
		});
	}

	
	
	
}


