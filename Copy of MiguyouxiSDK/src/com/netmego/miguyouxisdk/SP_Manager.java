package com.netmego.miguyouxisdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface;

import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKIAP;
import com.anysdk.framework.java.AnySDKPush;
import com.anysdk.framework.java.AnySDKUser;
import com.netmego.miguyouxisdk.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxisdk.MiguSDKFactory.ExitGameListener;
import com.qy.pay.listener.PayAgent;

public class SP_Manager extends SDKFactoryBase
{
	private static Activity _context;
	
	private Handler mUIHandler = new Handler();
	Handler PayCallback;
	
	public boolean Initialized = false;
	
	private BillingListener MasterListener;
	
	private SP_Manager(Activity context, BillingListener listener) 
	{
		MasterListener = listener;

		PayAgent.init(context);
		
		PayCallback = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	TryToPay = false;
	          Bundle b = msg.getData();
	          if (b != null) {
	            int code = b.getInt("code", -1);
	            String msg1 = b.getString("msg");
	            String result = "";
	            if (code == 0) 
	            {
		            result = TryToPayItem + "|1|付款成功" + msg1;
		            MasterListener.onPurchaseSucceed(TryToPayItem);
	            } else {
	            	result = TryToPayItem + "|0|付款失败" + msg1;
	            	MasterListener.onPurchaseFailed(TryToPayItem, result);
	            }
	          }
	        }
	      };
	}
	
	private static SP_Manager _singletonSP;
	public static SP_Manager getInstance() 
	{
		return _singletonSP;
	}
	
	// #3. 처리자 생성 
	public static SP_Manager initSingleton(Activity context, BillingListener listener) 
	{
		if (_singletonSP == null) 
		{
			_singletonSP = new SP_Manager(context, listener);
		}
		return _singletonSP;
	}
	
	private boolean TryToPay = false;
	private String TryToPayItem;
	
	@Override
	public void pay(Context context, final String smsPayItem, final String ext_code, final String props, final String Money, BillingListener listener, boolean isRepeated) 
	{
	    Bundle metaData = null;
		try {
			metaData = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			MasterListener.onPurchaseFailed(smsPayItem, "Cannot get SP Paycode from Androidmanifest.xml");
			return;
		}

		String Paycode = metaData.getString("SP" + smsPayItem);

		System.out.println("Brandon : sp pay request :" + Paycode + ":" + props);
		
		
		if ( TryToPay == true )
		{
			MasterListener.onPurchaseCanceld(smsPayItem, "Already payment process...");
			return;
		}
		
		TryToPay = true;
		TryToPayItem = smsPayItem;
		
		MasterListener = listener;
				
		PayAgent.pay(_context, PayCallback, Paycode, Integer.parseInt(Money));
	}
	

	@Override
	public void exitGame(Context context, ExitGameListener listener) 
	{
	}

	@Override
	public void viewMoreGames(Context context) {
		// Do nothing...
	}

	@Override
	public boolean isMusicEnabled() {
		return true;
	}

	@Override
	public void doScreenShotShare (Context context, Uri uri) {
		// Do nothing...
	}
	
}