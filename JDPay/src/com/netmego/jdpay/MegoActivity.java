package com.netmego.jdpay;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import android.os.Bundle;
import android.os.Handler;

import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface;
import cn.cmgame.billing.api.GameInterface.*;
import android.view.KeyEvent;


public class MegoActivity extends UnityPlayerActivity 
{
	private Handler postHandler = new Handler(); 
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    GameInterface.initializeApp(this);
	    

	    //*******************************營묉만?삣퐬役곭쮮?멨뀽?ε룭******************************//
	    // ?ⓩ댎?삣퐬????뉓칳竊뚧캀轝←쇉壤뺧펽瑥ε쇔틪訝띶릪?뽩뵱訝
//	    GameInterface.setExtraArguments(new String[]{"abc201311131352000"});
	//
//	    // ?묈맟?삣퐬瀯볠옖竊뚧만?뤸졊??눎翁ヤ툣?↓삭풌竊뚥슴?①㎉?ⓩ만?뢘DK?먧풘?꾤쇉壤뺟퍜??//    GameInterface.setLoginListener(this, new ILoginCallback(){
//	      @Override
//	      public void onResult(int i, String s, Object o) {
//	        System.out.println("Login.Result=" + s);
//	        if(i == LoginResult.SUCCESS_EXPLICIT){
//	          System.out.println("?ⓩ댎?삣퐬?먨뒣");
//	        }
//	        if(i == LoginResult.FAILED_EXPLICIT){
//	          System.out.println("?ⓩ댎?삣퐬鸚김뇰");
//	        }
//	        if(i == LoginResult.UNKOWN){
//	          System.out.println("?ⓩ댎?뽪텋?삣퐬竊뚧닑?좂퐨瀯쒐듁?곻펽?よ㏄?묊쇉壤?);
//	        }
//	      }
//	    });
    }

	  private String getBillingIndex(int i) {
	    if (i < 9) {
	      return "00" + (++i);
	    } else {
	      return "0" + (++i);
	    }
	  }	    
	    
		public void OnBuy(final String smsPayItem, final boolean isRepeated, final String callbackGameObject, final String callbackFunc )
		{
			System.out.println("Brandon : Try to pay - " + smsPayItem);

			postHandler.post( new Runnable()
			{
				public void run()
				{
					IPayCallback fff = new IPayCallback()
					{
					      @Override
					      public void onResult(int resultCode, String billingIndex, Object obj) 
					      {
					        switch (resultCode) 
					        {
					          case BillingResult.SUCCESS:
								UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, "1|" + billingIndex + "|success" );
					            break;
					          case BillingResult.FAILED:
								UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, "2|" + billingIndex + "|failed" );
					            break;
					          default:
								UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, "3|" + billingIndex + "|cancel" );
					            break;
					        }
					      }
					};
						
//			          GameInterface.retryBilling(BillingDemo.this, true, true, "001", null, payCallback);
			          
//					String billingIndex = "001";//getBillingIndex(position);
					
					GameInterface.doBilling(MegoActivity.this, true, isRepeated, smsPayItem, null, fff);
					
				}
			});
			
			
		}
	  
	  
	  
	  @Override
	  public void onResume() {
	    super.onResume();
	  }

	  /**
	   * 燁삣뒯歷멩닆SDK竊싨룓堊쏁쉪??뷸렏??   */
	  private void exitGame() 
	  {
	    GameInterface.exit(this, new GameExitCallback() {
	      @Override
	      public void onConfirmExit() {
	    	  MegoActivity.this.finish();
	      }

	      @Override
	      public void onCancelExit() {
//	        Toast.makeText(BillingDemo.this, "onCancelExit", Toast.LENGTH_SHORT).show();
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
}
