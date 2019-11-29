package com.netmego.megommlinker;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import mm.purchasesdk.Purchase;

import android.os.Bundle;
import android.os.Handler;

public class MegoActivity extends UnityPlayerActivity
{
	public Purchase purchase;

	private IAPListener mListener;

	private Handler postHandler = new Handler(); 
	
    String _callbackGameObject;
    String _callbackFunc;
	
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
    
        
	public void OnInit(final String AppID, final String AppKey, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("init" + AppID + ":"  + AppKey);
		mListener = new IAPListener(this);
		purchase = Purchase.getInstance();

		_callbackGameObject = callbackGameObject;
		_callbackFunc = callbackFunc;
		
		postHandler.post( new Runnable()
		{
			public void run()
			{
				try {
					purchase.setAppInfo(AppID, AppKey);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				try {
					purchase.init(MegoActivity.this, mListener);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
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
				    purchase.order(MegoActivity.this, Paycode, mListener, "Netmego" );
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}	
	
	public void PurchaseResultSuccess(String paycode, String tradeID, String netType, int code, String desc)
	{
		System.out.println("billing finish, status code = " +code + ":" + desc + ":" + netType);

		String result = "DESC:" + Purchase.getReason(code) + "|" + code;
		
		result = result + "|";
				
		if (paycode != null && paycode.trim().length() != 0) 
		{
			result = result + paycode;
		}
		
		result = result + "|";
				
		if (tradeID != null && tradeID.trim().length() != 0) 
		{
			result = result + tradeID;
		}

		UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, result );
		System.out.println(result);
		
	}
	
	public void PurchaseResultFailed(int code, String desc)
	{
		System.out.println("billing failed, status code = " +code + ":" + desc);

		String result = "DESC:" + desc + "|" + code;
		
		result = result + "|0";
		result = result + "|0";

		UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, result );
		System.out.println(result);
	}
	

}
