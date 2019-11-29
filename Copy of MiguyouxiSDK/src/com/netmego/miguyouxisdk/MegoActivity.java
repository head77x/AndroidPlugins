package com.netmego.miguyouxisdk;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import cn.cmgame.billing.api.GameInterface;
import cn.egame.terminal.sdk.log.EgameAgent;

import com.anysdk.framework.PluginWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKAnalytics;
import com.anysdk.framework.java.AnySDKUser;
import com.netmego.miguyouxisdk.AnySDK_Manager;
import com.netmego.miguyouxisdk.MegoActivity;
import com.netmego.miguyouxisdk.MiguSDKFactory;
import com.netmego.miguyouxisdk.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxisdk.MiguSDKFactory.ExitGameListener;
import com.unicom.dcLoader.Utils;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MegoActivity extends UnityPlayerActivity 
{
	private boolean isAppForeground = true;
	
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
	
	@SuppressLint("NewApi") public boolean isAppOnForeground() 
	{
		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		
		String packageName = getApplicationContext().getPackageName();
		
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null)
			return false;
		
		for (RunningAppProcessInfo appProcess : appProcesses) 
		{
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) 
			{
				return true;
			}
		}
		
		return false;
	}
	 
	@Override
	protected void onStop() 
	{
		super.onStop();
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Stop(this);
		if(!isAppOnForeground())
		{
			isAppForeground = false;
		}
	}
	 
	@Override
	protected void onRestart() 
	{
		// TODO Auto-generated method stub
		super.onRestart();
		System.out.println("onRestart");
	}
	 
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Resume(this, isAppForeground);
		
		if(!isAppForeground)
		{
			isAppForeground = true;			
		}
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Pause(this);
		
		if(!isAppOnForeground())
		{
			isAppForeground = false;
		}
	}

	// #2. 초기화 함수 - Unity 에서 호출
	public void OnInit(
			final String AnySDK_AppKey, final String AnySDK_AppSecret, final String AnySDK_privateKey, final String AnySDK_oauthLoginServer,
			final String MM_appID, final String MM_appkey, 
			final String CompanyName, final String QAPhone, 
			final String AppName )
	{
		System.out.println("Migu SDK init :" + AppName + ":"  + CompanyName);

		MainListener = new BillingListener()
		{
			@Override
			public void onPurchaseSucceed(String item) 
			{
				UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "1|" + item + "|success" );
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
						AnySDK_AppKey, AnySDK_AppSecret, AnySDK_privateKey, AnySDK_oauthLoginServer,
						MM_appID, MM_appkey,
						CompanyName, QAPhone, AppName );
			}
		});
	}
	
	// #3. 결제 신청
	public void OnBuy(
			final String smsPayItem, final String vaccode, final String props, 
			final String money, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("san wang on buy :" + smsPayItem);
		
		_callbackGameObject = callbackGameObject;
		_callbackFunc = callbackFunc;

		postHandler.post( new Runnable()
		{
			public void run()
			{
				MiguSDKFactory.getInstance().pay(MegoActivity.this, smsPayItem, vaccode, props, money, MainListener, true);
			}
		});
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
			PluginWrapper.onActivityResult(requestCode, resultCode, data);
	}
	
	 @Override
	 protected void onDestroy() {
		 super.onDestroy();
		 if ( MiguSDKFactory.getInstance() != null)
			 MiguSDKFactory.getInstance().Destroy(this);
	 };
	 	
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
	       
  	public void exit()
  	{
  		if ( MiguSDKFactory.getInstance() != null )
		postHandler.post( new Runnable()
		{
			public void run()
			{
		    	MiguSDKFactory.getInstance().exitGame(MegoActivity.this, new ExitGameListener() 
		    	{
		    		@Override
		    		public void onExitGameConfirmExit() 
		    		{
		    			System.exit(0);
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
  		MegoActivity.this.exit();
  	}
	  	
    public int CheckMobile()
    {
    	if ( MiguSDKFactory.getInstance() == null )
    	{
    		return -1;
    	}
    	
    	return MiguSDKFactory.getInstance().getMobileOperatorType(); 
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
	    
}
