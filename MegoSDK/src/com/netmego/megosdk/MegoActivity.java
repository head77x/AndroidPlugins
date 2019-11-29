package com.netmego.megosdk;

import java.util.HashMap;
import java.util.List;

import mm.purchasesdk.OnPurchaseListener;
import mm.purchasesdk.Purchase;
import mm.purchasesdk.core.PurchaseCode;

import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface;

import com.unicom.dcLoader.Utils;
import com.unicom.dcLoader.Utils.UnipayPayResultListener;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.netmego.miguyouxisdk.AnySDK_Manager;
import com.netmego.miguyouxisdk.BillingPayListener;
import com.netmego.miguyouxisdk.MiguSDKFactory;
import com.netmego.miguyouxisdk.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxisdk.MiguSDKFactory.ExitGameListener;

import com.anysdk.framework.PluginWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKAnalytics;
import com.anysdk.framework.java.AnySDKUser;


public class MegoActivity extends UnityPlayerActivity 
{
	private boolean isAppForeground = true;
	
	String _callbackGameObject;
	String _callbackFunc;
	
	boolean PromptExitInGame;
	
	private Handler postHandler = new Handler(); 
	  	
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
		System.out.println("onStop");
		 
		if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
		{
			AnySDKAnalytics.getInstance().stopSession();
			if(!isAppOnForeground())
			{
				isAppForeground = false;
			}
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
		System.out.println("onResume");
		if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
		{
			AnySDKAnalytics.getInstance().startSession();
			if(!isAppForeground)
			{
				AnySDKUser.getInstance().callFunction("pause");
				isAppForeground = true;			
			}
		}
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		System.out.println("onPause");
		
		if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
		{
			AnySDKAnalytics.getInstance().stopSession();
			if(!isAppOnForeground())
			{
				isAppForeground = false;
			}
		}
	}

	// #2. 초기화 함수 - Unity 에서 호출
	public void OnInit(
			final String AnySDK_AppKey, final String AnySDK_AppSecret, final String AnySDK_privateKey, final String AnySDK_oauthLoginServer,
			final String MM_appID, final String MM_appkey, 
			final String uni_AppID, final String uni_CpCode, final String uni_CpID, 
			final String ty_channelID, final String ty_seccode, 
			final String CompanyName, final String QAPhone, 
			final String AppName, final boolean OtherPay )
	{
		System.out.println("Migu SDK init :" + AppName + ":"  + CompanyName);

		postHandler.post( new Runnable()
		{
			public void run()
			{
				MiguSDKFactory.init(
						MegoActivity.this, 
						AnySDK_AppKey, AnySDK_AppSecret, AnySDK_privateKey, AnySDK_oauthLoginServer,
						MM_appID, MM_appkey,
						uni_AppID, uni_CpCode, uni_CpID, ty_channelID, 
						ty_seccode, CompanyName, QAPhone, AppName, OtherPay );
			}
		});
	}
	
	// #3. 결제 신청
	public void OnBuy(
			final String smsPayItem, final String vaccode, final String props, 
			final String money, final String appID, 
			final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("san wang on buy :" + smsPayItem);
		
		_callbackGameObject = callbackGameObject;
		_callbackFunc = callbackFunc;

		postHandler.post( new Runnable()
		{
			public void run()
			{
				BillingListener fff = new BillingListener()
				{
					@Override
					public void onPurchaseSucceed(String item) 
					{
						UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "0|0|success" );
					}
					
					@Override
					public void onPurchaseInfo(String msg) 
					{
						UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "0|0|" + msg );
					}
					
					@Override
					public void onPurchaseFailed(String item, String msg) 
					{
						UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "1|1|" + msg );
					}
					
					@Override
					public void onPurchaseCanceld(String item) 
					{
						UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, "3|3|canceled" );
					}
				};
					
				MiguSDKFactory.getInstance().pay(MegoActivity.this, smsPayItem, vaccode, props, money, appID, fff, true);
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
			System.out.println("onDestroy");
		 
		if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
		{
	     AnySDKUser.getInstance().callFunction("destroy");
	     AnySDK.getInstance().release();
		}
	 };
	 	
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) 
		{
		    if (keyCode == KeyEvent.KEYCODE_BACK) 
		    {
				if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
				{
					if (AnySDKUser.getInstance().isSupportFunction("exit")) 
					{
						AnySDKUser.getInstance().callFunction("exit");
						return true;
					}
				}
		    	
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
			postHandler.post( new Runnable()
			{
				public void run()
				{
			    	MiguSDKFactory.getInstance().exitGame(MegoActivity.this, new ExitGameListener() 
			    	{
			    		@Override
			    		public void onExitGameConfirmExit() 
			    		{
			    			MegoActivity.this.exit();// .finish();
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
					MiguSDKFactory.getInstance().exitGame(MegoActivity.this, new ExitGameListener() 
					{
			          @Override
			          public void onExitGameConfirmExit() 
			          {
			        	  MegoActivity.this.exit();// .finish();
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
	    	return MiguSDKFactory.getInstance().getMobileOperatorType(); 
	    }
	    
	    public void viewMoreGames()
	    {
	    	MiguSDKFactory.getInstance().viewMoreGames(MegoActivity.this);
	    	
	    }
		
	    public boolean isMusicEnabled()
	    {
	    	return MiguSDKFactory.getInstance().isMusicEnabled();
	    }
	
}


public class BillingPayListener implements 	GameInterface.IPayCallback, 			/* CMGD callback */
UnipayPayResultListener,	/* Unicom callback */
OnPurchaseListener /* CMM */
{
	private BillingListener _listener;

	BillingPayListener(MiguSDKFactory factory, BillingListener listener) 
	{
		//_factory = factory;
		_listener= listener;
	}

	public void setBillingListener(BillingListener listener) 
	{
		_listener = listener;
	}

	public BillingListener getBillingListener()
	{
		return _listener;
	}

	/**
	* CMCC_GC Listener
	*/
	@Override
	public void onResult(int resultCode, String billingIndex, Object arg) 
	{
		String result = "";
		
		switch (resultCode) 
		{
			case BillingResult.SUCCESS:
				result = "CMGD Billing :" + billingIndex + " - success";
				_listener.onPurchaseSucceed(billingIndex);
			break;
			case BillingResult.FAILED:
				result = "CMGD Billing :" + billingIndex + " - failed";
				_listener.onPurchaseFailed(billingIndex, result);
			break;
			default:
				result = "CMGD Billing :" + billingIndex + " - canceled";
				_listener.onPurchaseCanceld(billingIndex);
			break;
		}
		Log.e("CMGD Result : ", result);
	}
	
	/**
	* CMCC_MM Listener
	*/
	@Override
	public void onAfterApply() {
	
	}
	
	@Override
	public void onAfterDownload() {
	
	}
	
	@Override
	public void onBeforeApply() {
	
	}
	
	@Override
	public void onBeforeDownload() {
	
	}
	
	@Override
	public void onInitFinish(int code) 
	{
		System.out.println("Brandon : Init finish, status code = " + code + ":" + Purchase.getReason(code) );
	
	//mycontext.IAPInitResult(true);
	}
	
	@Override
	public void onBillingFinish(int code, HashMap arg1) 
	{
		System.out.println("Brandon : billing finish, status code = " + code);
		String result = "billing result :";
		String orderID = null;
		String paycode = null;
		String leftday = null;
		String tradeID = null;
		
		String ordertype = null;
	
		if (code == PurchaseCode.ORDER_OK || (code == PurchaseCode.AUTH_OK) ||(code == PurchaseCode.WEAK_ORDER_OK)) 
		{
			if (arg1 != null) 
			{
				leftday = (String) arg1.get(OnPurchaseListener.LEFTDAY);
				if (leftday != null && leftday.trim().length() != 0) {
					result = result + ",Left :" + leftday;
				}
				orderID = (String) arg1.get(OnPurchaseListener.ORDERID);
				if (orderID != null && orderID.trim().length() != 0) {
					result = result + ",OrderID :" + orderID;
				}
				paycode = (String) arg1.get(OnPurchaseListener.PAYCODE);
				if (paycode != null && paycode.trim().length() != 0) {
					result = result + ",Paycode:" + paycode;
				}
				tradeID = (String) arg1.get(OnPurchaseListener.TRADEID);
				if (tradeID != null && tradeID.trim().length() != 0) {
					result = result + ",tradeID:" + tradeID;
				}
				ordertype = (String) arg1.get(OnPurchaseListener.ORDERTYPE);
				if (tradeID != null && tradeID.trim().length() != 0) {
					result = result + ",ORDERTYPE:" + ordertype;
				}
	
				if (_listener != null) {
					_listener.onPurchaseSucceed(tradeID);
					_listener.onPurchaseInfo(result);
				}
	
			}
		}
		else 
		{
			result = "failed" + Purchase.getReason(code);
	
			_listener.onPurchaseFailed(tradeID, result);
		}
		
		System.out.println(result);
	}
	
	@Override
	public void onQueryFinish(int code, HashMap arg1) {
		System.out.println("Brandon : license finish, status code = " + code);
		String result = "onQueryFinish :";
		String orderID = null;
		String paycode = null;
		String leftday = null;
		if (code != PurchaseCode.QUERY_OK) 
		{
			result = "query failed :" + Purchase.getReason(code);
		} else {
			leftday = (String) arg1.get(OnPurchaseListener.LEFTDAY);
			if (leftday != null && leftday.trim().length() != 0) {
				result = result + ",Left :" + leftday;
			}
			orderID = (String) arg1.get(OnPurchaseListener.ORDERID);
			if (orderID != null && orderID.trim().length() != 0) {
				result = result + ",OrderID :" + orderID;
			}
			paycode = (String) arg1.get(OnPurchaseListener.PAYCODE);
			if (paycode != null && paycode.trim().length() != 0) {
				result = result + ",Paycode:" + paycode;
			}
		}
		
		System.out.println(result);
	//context.dismissProgressDialog();
	}
	
	
	@Override
	public void onUnsubscribeFinish(int code) {
	// TODO Auto-generated method stub
		String result = "��溫®퍜�옖竊�" + Purchase.getReason(code);
		System.out.println(result);
	//context.dismissProgressDialog();
	}
	
	
	/**
	* SmsPayUnicom Listener
	*/
	@Override
	public void PayResult(String paycode, int flag, String desc) 
	{
		// ��빰��퍡�먨뒣
		if(flag == Utils.SUCCESS_SMS ){
			if (_listener != null) {
				Log.e("======�붼� SDK=======", "SUCCESS_SMS: " + desc);
				_listener.onPurchaseSucceed(paycode);
			}
		}
	
		// SDK鵝욜뵪寧т툒�방뵱餓섋퓭�욄닇��			
		if(flag == Utils.SUCCESS_3RDPAY ) {
			if (_listener != null) {
				Log.e("======�붼� SDK=======", "SUCCESS_3RDPAY: " + desc);
				_listener.onPurchaseSucceed(paycode);
			}
		}
	
		// ��퍡鸚김뇰
		if (flag == Utils.FAILED) {
			if (_listener != null) {
				Log.e("======�붼� SDK=======", "FAILED: " + desc);
				_listener.onPurchaseFailed(paycode, desc);
			}
		}
	
		// ��퍡�뽪텋
		if (flag == Utils.CANCEL) {
			if (_listener != null) {
				Log.e("======�붼� SDK=======", "CANCEL: " + desc);
				_listener.onPurchaseCanceld(paycode);
			}
		}
	
		// �욆걫�싩К訝됪뼶��퍡
		if (flag == Utils.OTHERPAY) {
			if (_listener != null) {
				Log.e("======�붼� SDK=======", "OTHERPAY: " + desc);
			}
		}
	}
}

public class ExitListener implements GameInterface.GameExitCallback {
	
	private ExitGameListener _listener;
	ExitListener(ExitGameListener listener) {
		_listener= listener;
	}
	
	public void forceExitingGame() {
		_listener.onExitGameInGame();
	}
	
	@Override
	public void onCancelExit() {
		_listener.onExitGameCancelExit();
	}

	@Override
	public void onConfirmExit() {
		_listener.onExitGameConfirmExit();
	}
}
