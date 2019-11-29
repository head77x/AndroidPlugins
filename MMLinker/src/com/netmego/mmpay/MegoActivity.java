package com.netmego.mmpay;

import java.util.List;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import mm.purchasesdk.Purchase;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

public class MegoActivity extends UnityPlayerActivity
{
	private boolean isAppForeground = true;
		
	boolean PromptExitInGame;
	
	private Handler postHandler = new Handler(); 
	
	public  Purchase purchase;
	
	public IAPListener myListener;
	
	String _callbackGameObject;
	String _callbackFunc;
	
	
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
		
		if(!isAppForeground)
		{
			isAppForeground = true;			
		}
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
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
	
		postHandler.post( new Runnable()
		{
			public void run()
			{
				purchase = Purchase.getInstance();
				
				myListener = new IAPListener(MegoActivity.this);
				
				try {
					purchase.setAppInfo(MM_appID, MM_appkey);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				System.out.println("Brandon : MM app info = " + MM_appID + ":" + MM_appkey );
				
				
				try {
					purchase.init(MegoActivity.this, myListener);
				} catch (Exception e) {
					 e.printStackTrace();
				}
			}
		});
		
		System.out.println("Brandon : MM init info = " + this + ":" + myListener );
	}
	
        
	
	// #3. 결제 신청
	@SuppressLint("NewApi") public void OnBuy(
			final String smsPayItem, final String vaccode, final String props, 
			final String money, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("san wang on buy :" + smsPayItem);

		_callbackGameObject = callbackGameObject;
		_callbackFunc = callbackFunc;
		
	    Bundle metaData = null;
		try {
			metaData = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			onPurchaseFailed(smsPayItem, "Cannot get Paycode from Androidmanifest.xml");
			return;
		}

		final String Paycode = metaData.getString(smsPayItem);

		System.out.println("Brandon : Mobile MM Paycode : " + Paycode + ":" + props );
		
		postHandler.post( new Runnable()
		{
			public void run()
			{
				purchase.order(MegoActivity.this, Paycode, 1, props, false, myListener );
			}
		});
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	 @Override
	 protected void onDestroy() {
		 super.onDestroy();
	 };
	 	
		private Handler mUIHandler = new Handler();
	 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK ) 
	    {
	    	PromptExitInGame = false;
	        mUIHandler.post(new Runnable() {
	            @Override
	            public void run() {
	                new AlertDialog.Builder(MegoActivity.this)
	                .setTitle("游戏退出")
	                .setMessage("现在游戏退出吗？")
	                .setPositiveButton("不", 
	                        new DialogInterface.OnClickListener() {
	                            
	                            @Override
	                            public void onClick(DialogInterface dialog, int which) 
	                            {
	                            }
	                        })
	                .setNegativeButton("是", 
	                        new DialogInterface.OnClickListener() {
	                            
	                            @Override
	                            public void onClick(DialogInterface dialog, int which) 
	                            {
	                        		System.exit(0);
	                            }
	                        }).create().show();
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
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MegoActivity.this)
                .setTitle("游戏退出")
                .setMessage("现在游戏退出吗？")
                .setPositiveButton("不", 
                        new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) 
                            {
                            }
                        })
                .setNegativeButton("是", 
                        new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) 
                            {
                        		System.exit(0);
                            }
                        }).create().show();
            }
        });
  	}

  	public void exitGame()
  	{
  		exit();
  	}
	  	
    public int CheckMobile()
    {
    	return 3; 
    }
    
    public void viewMoreGames()
    {
    }
	
    public boolean isMusicEnabled()
    {
    	return true;
    }

	public void screenShotShare(final String filepath)
	{
	}
	    
	public void AboutUs()
	{
	}
	
	public void onPurchaseSucceed(String item) 
	{
		UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "1|" + item + "|success" );
	}
	
	public void onPurchaseInfo(String item, String msg) 
	{
		UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "-1|" + item + "|" + msg );
	}
	
	public void onPurchaseFailed(String item, String msg) 
	{
		UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "0|" + item + "|" + msg );
	}
	
	public void onPurchaseCanceld(String item, String msg) 
	{
		UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "2|" + item + "|canceled" );
	}
	

}
