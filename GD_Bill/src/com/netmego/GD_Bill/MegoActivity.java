package com.netmego.GD_Bill;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface;
import cn.cmgame.billing.api.LoginResult;
import cn.cmgame.gamepad.api.Gamepad;
import cn.cmgame.gamepad.api.KeyState;
import cn.cmgame.leaderboard.api.GameLeaderboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.Toast;

public class MegoActivity extends UnityPlayerActivity
{
	private Handler postHandler = new Handler(); 
	
    String _callbackGameObject;
    String _callbackFunc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		GameInterface.initializeApp(this);
	}
	
	
	  @Override
	  public void onResume() {
	    super.onResume();
	  }

	  private void exitGame() {
	    GameInterface.exit(this, new GameInterface.GameExitCallback() {
	      @SuppressLint("NewApi") @Override
	      public void onConfirmExit() {
	        MegoActivity.this.finish();
	        System.exit(0);
	      }

	      @Override
	      public void onCancelExit() {
	        Toast.makeText(MegoActivity.this, "取消退出", Toast.LENGTH_SHORT).show();
	      }
	    });
	  }

	  @Override
	  public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	      exitGame();
	      return true;
	    }
	    return super.onKeyDown(keyCode, event);
	  }
    
        
	public void OnInit(final String callbackGameObject, final String callbackFunc )
	{
		_callbackGameObject = callbackGameObject;
		_callbackFunc = callbackFunc;
				
		System.out.println("Brandon : GD init");

	    GameInterface.setLoginListener(this, new GameInterface.ILoginCallback()
	    {
	        @Override
	        public void onResult(int i, String s, Object o) 
	        {
	          System.out.println("Login.Result=" + s);
	          if(i == LoginResult.SUCCESS_EXPLICIT){
	            System.out.println("用户显式登录成功");
	          }
	          if(i == LoginResult.FAILED_EXPLICIT){
	            System.out.println("用户显式登录失败");
	          }
	          if(i == LoginResult.UNKOWN){
	            System.out.println("用户取消登录，或无网络状态，未触发登录");
	          }
	        }
	      });
/*		
		_callbackGameObject = callbackGameObject;
		_callbackFunc = callbackFunc;
		
		postHandler.post( new Runnable()
		{
			public void run()
			{
			}
		});
*/		
	}
	
    final GameInterface.IPayCallback payCallback = new GameInterface.IPayCallback() 
    {
        @Override
        public void onResult(int resultCode, String billingIndex, Object obj) 
        {
          String result = "";
          switch (resultCode) {
            case BillingResult.SUCCESS:
              result = billingIndex + "|1";
              break;
            case BillingResult.FAILED:
            	result = billingIndex + "|0";
              break;
            default:
            	result = billingIndex + "|2";
              break;
          }
          Toast.makeText(MegoActivity.this, result, Toast.LENGTH_SHORT).show();
          UnityPlayer.UnitySendMessage( _callbackGameObject , _callbackFunc, "0|0|success" );
        }
      };
	
	public void OnBuy(final String Paycode, final String userdata) 
	{
		System.out.println("on buy");

		postHandler.post( new Runnable()
		{
			public void run()
			{
				try 
				{
		          GameInterface.doBilling(MegoActivity.this, true, false, Paycode, null, payCallback );
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}	
	

}
