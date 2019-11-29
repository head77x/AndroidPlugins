package com.netmego.unipaylinker;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import android.os.Handler;
import android.os.Bundle;
//import android.content.Context;
//import android.widget.*;

import com.unicom.dcLoader.Utils;
import com.unicom.dcLoader.Utils.UnipayPayResultListener;


public class MegoActivity extends UnityPlayerActivity 
{
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
        
	public void OnInit(final String AppID, final String CpCode, final String CpID, final String CompanyName, final String QAPhone, final String AppName, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("init" + AppID + ":"  + CompanyName);

		postHandler.post( new Runnable()
		{
			public void run()
			{
				UnipayPayResultListener fff = new UnipayPayResultListener()
				{
					@Override
					public void PayResult(String paycode, int flag, String desc) 
					{
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, paycode + "," + flag + "," + desc );
						System.out.println(paycode + ":" + flag + ":" + desc);
					}
				
				};
				
				Utils.getInstances().init(MegoActivity.this, AppID, CpCode, CpID, CompanyName, QAPhone, AppName,"uid", fff);
			}
		});
		
		
	}
	
	public void OnBuy(final boolean useSms, final boolean otherPay, final String callbackUrl, final String vaccode, final String customcode, final String itemName, final String moneyYuan, final String orderIdx, final String callbackGameObject, final String callbackFunc )
	{
		System.out.println("on buy");

		postHandler.post( new Runnable()
		{
			public void run()
			{
				UnipayPayResultListener fff = new UnipayPayResultListener()
				{
					@Override
					public void PayResult(String paycode, int flag, String desc) 
					{
						UnityPlayer.UnitySendMessage(callbackGameObject, callbackFunc, paycode + "," + flag + "," + desc );
					}
				
				};
				
				Utils.getInstances().setBaseInfo(MegoActivity.this, otherPay, useSms, callbackUrl);
				Utils.getInstances().pay(MegoActivity.this,vaccode,
						customcode, itemName, moneyYuan, orderIdx, fff);
			}
		});
	}
}


