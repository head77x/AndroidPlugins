package com.netmego.miguyijieunity;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.netmego.miguyijie.MiguSDKFactory;
import com.netmego.miguyijie.MiguSDKFactory.BillingListener;
import com.netmego.miguyijie.MiguSDKFactory.ExitGameListener;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MegoActivity extends UnityPlayerActivity 
{
	String _callbackGameObject;
	String _callbackFunc;
	
	boolean PromptExitInGame;
	
	private Handler postHandler = new Handler(); 
	
	BillingListener MainListener;
	// #1.  생성시 특별한 조치 없음
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
		 
	@Override
	protected void onStop() 
	{
		super.onStop();
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Stop(this);
	}
	 
	@Override
	protected void onRestart() 
	{
		// TODO Auto-generated method stub
		super.onRestart();
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Restart(this);
	}
	 
	@Override
	protected void onResume() 
	{
		super.onResume();

		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Resume(this);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Pause(this);
	}

	 @Override
	 protected void onDestroy() {
		 super.onDestroy();
		 if ( MiguSDKFactory.getInstance() != null)
			 MiguSDKFactory.getInstance().Destroy(this);
	 };

	// #2. 초기화 함수 - Unity 에서 호출
	public void OnInit(
			final String MM_appID, final String MM_appkey, 
			final String CompanyName, final String QAPhone, 
			final String AppName )
	{
		System.out.println("Migu SDK init :" + AppName + ":"  + CompanyName);

		MainListener = new BillingListener()
		{
			@SuppressLint("NewApi") @Override
			public void onPurchaseSucceed(String item, String code3rd) 
			{
				UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "1|" + item + "|success|" + code3rd + "|" + 
						MiguSDKFactory.getInstance().getAppID() + "|" + MiguSDKFactory.getInstance().getAppKey() + "|" +
						MiguSDKFactory.getInstance().getLoginID() + "|" + getApplicationContext().getPackageName());
			}
			
			@Override
			public void onPurchaseInfo(String item, String msg) 
			{
				UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "-1|" + item + "|" + msg );
			}
			
			@Override
			public void onPurchaseFailed(String item, String msg) 
			{
				UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "0|" + item + "|" + msg );
			}
			
			@Override
			public void onPurchaseCanceld(String item, String msg) 
			{
				UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "2|" + item + "|canceled" );
			}
		};
		
		postHandler.post( new Runnable()
		{
			public void run()
			{
				MiguSDKFactory.init(
						MegoActivity.this, MainListener,
						MM_appID, MM_appkey,
						CompanyName, QAPhone, AppName );
			}
		});
	}
	
	public void Login()
	{
		MiguSDKFactory.getInstance().login();
	}
	
	// #3. 결제 신청
	public void OnBuy(
			final String smsPayItem, final String vaccode, final String props, 
			final String money, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("try to buy :" + smsPayItem);
		
		_callbackGameObject = callbackGameObject;
		_callbackFunc = callbackFunc;
/*
		if ( MiguSDKFactory.getInstance().isLogined() == false )
		{
			MainListener.onPurchaseFailed(smsPayItem, "Need to login");
			Login();
			return;
		}
*/		
		postHandler.post( new Runnable()
		{
			public void run()
			{
				MiguSDKFactory.getInstance().pay(MegoActivity.this, smsPayItem, vaccode, props, money, MainListener, true);
			}
		});
	}


/*	 	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK && MiguSDKFactory.getInstance() != null) 
	    {
	    	PromptExitInGame = false;
	    	MiguSDKFactory.getInstance().exitGame(MegoActivity.this, new ExitGameListener() 
	    	{
	    		@Override
	    		public void onExitGameConfirmExit() 
	    		{
	    			MegoActivity.this.exit();
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
  	public void exitwithUI()
  	{
  		if ( MiguSDKFactory.getInstance() != null )
		postHandler.post( new Runnable()
		{
			public void run()
			{
		    	MiguSDKFactory.getInstance().exitGame(MegoActivity.this, new ExitGameListener() 
		    	{
		    		@SuppressLint("NewApi") @Override
		    		public void onExitGameConfirmExit() 
		    		{
		    			System.out.println("Brandon : Real Exit");
		    			finish();
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

  	public void exitGame()
  	{
  		MegoActivity.this.exitwithUI();
  	}
	  	    
    public void viewMoreGames()
    {
    	if ( MiguSDKFactory.getInstance() == null )
    		return;
    	
    	MiguSDKFactory.getInstance().viewMoreGames(MegoActivity.this);
    }
	
    public boolean isMusicEnabled()
    {
    	if ( MiguSDKFactory.getInstance() == null )
    		return true;
    	
    	return MiguSDKFactory.getInstance().isMusicEnabled();
    }

	public void screenShotShare(final String filepath)
	{
    	if ( MiguSDKFactory.getInstance() == null )
    		return;
    	
		System.out.println("Brandon : scr path" + filepath);
    	MiguSDKFactory.getInstance().doScreenShotShare(MegoActivity.this, Uri.fromFile(new File(filepath)));
	}
	    
	public void AboutUs()
	{
    	if ( MiguSDKFactory.getInstance() == null )
    		return;

    	MiguSDKFactory.getInstance().AboutUs(MegoActivity.this);
	}
}
