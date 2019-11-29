package com.netmego.gdpay;

import java.io.File;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface;
import cn.cmgame.billing.api.LoginResult;
import cn.cmgame.gamepad.api.Gamepad;
import cn.cmgame.gamepad.api.KeyState;
import cn.cmgame.leaderboard.api.GameLeaderboard;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.Toast;

public class MegoActivity extends UnityPlayerActivity
{
	private Handler postHandler = new Handler(); 
	
    String _callbackGameObject;
    String _callbackFunc;
    
    String _gamepadCallbackGameObject;
    String _gamepadCallbackFunc;
	
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
              result = billingIndex + "|1|付款成功";
              break;
            case BillingResult.FAILED:
            	result = billingIndex + "|0|付款失败";
              break;
            default:
            	result = billingIndex + "|2|付款取消";
              break;
          }
          Toast.makeText(MegoActivity.this, result, Toast.LENGTH_SHORT).show();
          UnityPlayer.UnitySendMessage( _callbackGameObject , _callbackFunc, result );
        }
      };

  	public boolean CheckingItemActivate( String item )
  	{
  		return GameInterface.getActivateFlag(item);
  	}
      
	public void OnBuy(final String Paycode, final String userdata) 
	{
		System.out.println("on buy");

		postHandler.post( new Runnable()
		{
			public void run()
			{
				try 
				{
		          GameInterface.doBilling(MegoActivity.this, true, true, Paycode, null, payCallback );
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}	
	

	public void viewMoreGames()
	{
		GameInterface.viewMoreGames(MegoActivity.this);
	}
	
	public boolean isMusicEnabled()
	{
		return GameInterface.isMusicEnabled();
	}

	public void Leaderboard_init(String key, String secret, String appid)
	{
        GameLeaderboard.initializeLeaderboard(MegoActivity.this, key, secret, appid);
	}
	
	public void Leaderboard_show()
	{
        GameLeaderboard.showLeaderboard(MegoActivity.this);
	}
	
	final GameLeaderboard.ISimpleCallback simpleCallback = new GameLeaderboard.ISimpleCallback() 
	{
        @Override
        public void onFailure(String exceptionMessage) {
          Toast.makeText(MegoActivity.this, exceptionMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(String message) {
          Toast.makeText(MegoActivity.this, "上传分数成功", Toast.LENGTH_SHORT).show();
        }
      };
	
	public void Leaderboard_SetScore(int score)
	{
        GameLeaderboard.commitScore(MegoActivity.this, score, simpleCallback );
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
	
	public void screenShotShare(final String filepath)
	{
		System.out.println("Brandon : scr path" + filepath);
		postHandler.post( new Runnable()
		{
			public void run()
			{
				GameInterface.doScreenShotShare(MegoActivity.this, Uri.fromFile(new File(filepath)));
			}
		});
	}
	
}
