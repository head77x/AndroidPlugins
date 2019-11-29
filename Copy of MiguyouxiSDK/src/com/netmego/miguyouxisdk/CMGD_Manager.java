package com.netmego.miguyouxisdk;

import java.io.File;

import com.netmego.miguyouxisdk.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxisdk.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyouxisdk.SDKFactoryBase;
import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;
import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface;
import cn.cmgame.billing.api.LoginResult;
import cn.cmgame.gamepad.api.Gamepad;
import cn.cmgame.gamepad.api.KeyState;
import cn.cmgame.leaderboard.api.GameLeaderboard;

public class CMGD_Manager extends SDKFactoryBase
{
		private static CMGD_Manager _singletonSmsPayCMCC;
		
		BillingListener MasterListener;
		final GameInterface.IPayCallback payCallback;
		
		private Handler postHandler = new Handler(); 
		
		private CMGD_Manager(Activity activity, BillingListener listener) 
		{
			MasterListener = listener;
			
			GameInterface.initializeApp(activity);
			
		    payCallback = new GameInterface.IPayCallback() 
		    {
		        @Override
		        public void onResult(int resultCode, String billingIndex, Object obj) 
		        {
		          String result = "";
		          switch (resultCode) {
		            case BillingResult.SUCCESS:
		              result = billingIndex + "|1|付款成功";
		              MasterListener.onPurchaseSucceed(billingIndex);
		              break;
		            case BillingResult.FAILED:
		            	result = billingIndex + "|0|付款失败";
		            	MasterListener.onPurchaseFailed(billingIndex, result);
		              break;
		            default:
		            	result = billingIndex + "|2|付款取消";
						MasterListener.onPurchaseCanceld(billingIndex, result);
		              break;
		          }
		        }
		      };
			
/* 20120 에서부터 없어짐			
			GameInterface.setLoginListener(activity, new GameInterface.ILoginCallback()
			{
			      @Override
			      public void onResult(int i, String s, Object o) 
			      {
			        System.out.println("Login.Result=" + s);
			        if(i == LoginResult.SUCCESS_EXPLICIT){
			          System.out.println("CMGD Login Success");
			        }
			        if(i == LoginResult.FAILED_EXPLICIT){
			          System.out.println("CMGD Login Failed");
			        }
			        if(i == LoginResult.UNKOWN){
			          System.out.println("CMGD Login Canceled.Internet problem");
			        }
			      }
			    });			
*/			
			
		}
		
		public static CMGD_Manager initSingleton(Activity context, BillingListener listener) 
		{
			if (_singletonSmsPayCMCC == null) 
			{
				_singletonSmsPayCMCC = new CMGD_Manager(context, listener);
			}
			return _singletonSmsPayCMCC;
		}
		
		public static CMGD_Manager getInstance() 
		{
			return _singletonSmsPayCMCC;
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
	      
		
		@Override
		public void pay(Context context, String smsPayItem, String ext_code, String props, String Money, BillingListener listener, boolean isRepeated) 
		{
			MasterListener = listener;
			
			GameInterface.doBilling(context, true, isRepeated, smsPayItem, ext_code, payCallback);
		}

		@Override
		public void exitGame(Context context, ExitGameListener listener) 
		{
			GameInterface.exit(context, new ExitListener(listener));
		}


		@Override
		public void viewMoreGames(Context context) 
		{
			GameInterface.viewMoreGames(context);
		}


		@Override
		public boolean isMusicEnabled() 
		{
			return GameInterface.isMusicEnabled();
		}
		
		@Override
		public void doScreenShotShare (final Context context, final Uri uri) 
		{
			postHandler.post( new Runnable()
			{
				public void run()
				{
					GameInterface.doScreenShotShare(context, uri);
				}
			});
		}
		
		
/*		
		public void Leaderboard_init(String key, String secret, String appid)
		{
	        GameLeaderboard.initializeLeaderboard(context, key, secret, appid);
		}
		
		public void Leaderboard_show()
		{
	        GameLeaderboard.showLeaderboard(context);
		}
		
		final GameLeaderboard.ISimpleCallback simpleCallback = new GameLeaderboard.ISimpleCallback() 
		{
	        @Override
	        public void onFailure(String exceptionMessage) {
	          Toast.makeText(context, exceptionMessage, Toast.LENGTH_SHORT).show();
	        }

	        @Override
	        public void onSuccess(String message) {
	          Toast.makeText(context, "上传分数成功", Toast.LENGTH_SHORT).show();
	        }
	      };
		
		public void Leaderboard_SetScore(int score)
		{
	        GameLeaderboard.commitScore(context, score, simpleCallback );
	    }

	/*	
	    final Gamepad.GamepadCallback gamepadCallback = new Gamepad.GamepadCallback() 
	    {
	        @Override
	        public void onReceiveData(KeyState[] keyStates) 
	        {
	        	
	        	
	          Toast.makeText(MegoActivity.this, "虚拟按键：" + keyStates, Toast.LENGTH_SHORT).show();
	        }
	    };
		
		public void init_GamePad(final String callbackGameObject, final String callbackFunc )
		{
			_gamepadCallbackGameObject = callbackGameObject;
			_gamepadCallbackFunc = callbackFunc;
			
	      Gamepad.initGamepad(MegoActivity.this);
	      Gamepad.setConnectionListener(new Gamepad.GamepadConnectionListener() 
	      {
	        @Override
	        public void onConnectionState(int status) {
	          if (status == Gamepad.ConnectionState.CONNECTED) 
	          {
	        	  MegoActivity.this.runOnUiThread(new Runnable() {
	              @Override
	              public void run() { Toast.makeText(MegoActivity.this, "手柄自动连接成功", Toast.LENGTH_SHORT).show();
	              }
	            });
	          } else {
	        	  MegoActivity.this.runOnUiThread(new Runnable() {
	              @Override
	              public void run() { Toast.makeText(MegoActivity.this, "手柄自动连接失败", Toast.LENGTH_SHORT).show();
	              }
	            });
	          }
	        }
	      });
	      Gamepad.setGamepadCallback(gamepadCallback);
		}
	*/
		
}
