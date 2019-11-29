package com.netmego.miguyouxisdk;

import java.util.HashMap;
import java.util.Map;

import cn.egame.terminal.paysdk.EgamePay;
import cn.egame.terminal.paysdk.EgamePayListener;
import cn.egame.terminal.sdk.log.EgameAgent;
import cn.play.dserv.CheckTool;
import cn.play.dserv.ExitCallBack;

import com.estore.lsms.tools.ApiParameter;

import com.netmego.miguyouxisdk.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxisdk.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyouxisdk.SDKFactoryBase;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;

public class Telecom_Manager extends SDKFactoryBase
{
	final EgamePayListener payCallback;
	private String _channelID;
		
	BillingListener MasterListener;
	
	private Telecom_Manager(Activity context, BillingListener listener) 
	{
		MasterListener = listener;
		
		EgamePay.init(context);
		
	    payCallback = new EgamePayListener() 
	    {
            String result = "";
	    	
	    	@Override
			public void paySuccess(Map<String, String> params) 
	    	{
              result = params.get(EgamePay.PAY_PARAMS_KEY_CP_PARAMS) + "|1|付款成功";
              
	    		System.out.println("Brandon : dian xin pay success :" + result);
	    		
              MasterListener.onPurchaseSucceed(params.get(EgamePay.PAY_PARAMS_KEY_CP_PARAMS));
			}
			
			@Override
			public void payFailed(Map<String, String> params, int errorInt) 
			{
              result = params.get(EgamePay.PAY_PARAMS_KEY_CP_PARAMS) + "|0|支付失败：错误代码："+errorInt;
              
	    		System.out.println("Brandon : dian xin pay failed :" + result);
	    		
              MasterListener.onPurchaseFailed(params.get(EgamePay.PAY_PARAMS_KEY_CP_PARAMS), result);
			}
			
			@Override
			public void payCancel(Map<String, String> params) 
			{
              result = params.get(EgamePay.PAY_PARAMS_KEY_CP_PARAMS) + "|2|付款取消";
	    		System.out.println("Brandon : dian xin pay cancel :" + result);
              MasterListener.onPurchaseCanceld(params.get(EgamePay.PAY_PARAMS_KEY_CP_PARAMS), result);
			}
	      };
		
		
		System.out.println("Brandon : dian xin Init :" + _channelID);
	}
		
	private static Telecom_Manager _singletonSmsPayTelecom;
	public static Telecom_Manager getInstance() 
	{
		return _singletonSmsPayTelecom;
	}
		
	public static Telecom_Manager initSingleton(Activity context, BillingListener listener ) 
	{
			if (_singletonSmsPayTelecom == null) 
			{
				_singletonSmsPayTelecom = new Telecom_Manager(context, listener);
			}
			return _singletonSmsPayTelecom;
		}
		
		@Override
		public void pay(Context context, String smsPayItem, String ext_code, String props, String Money, BillingListener listener, boolean isRepeated) 
		{
		    Bundle metaData = null;
			try {
				metaData = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				MasterListener.onPurchaseFailed(smsPayItem, "Cannot get DX Paycode from Androidmanifest.xml");
				return;
			}

			String Paycode = metaData.getString("DX" + smsPayItem);

    		System.out.println("Brandon : dian xin pay request :" + Paycode + ":" + props);
			
			HashMap<String, String> payParams=new HashMap<String, String>();
			payParams.put(EgamePay.PAY_PARAMS_KEY_TOOLS_ALIAS, Paycode);
			payParams.put(EgamePay.PAY_PARAMS_KEY_TOOLS_DESC, props);
			payParams.put(EgamePay.PAY_PARAMS_KEY_CP_PARAMS, smsPayItem);
			EgamePay.pay(context, payParams, payCallback);
		}

		
		
		@Override
		public void exitGame(Context context, final ExitGameListener listener) 
		{
			CheckTool.exit(context, new ExitCallBack() { //Main.this为主Activity

    			@Override
    			public void exit() {
    				listener.onExitGameConfirmExit();
    				//退出游戏操作
    				//Main.this.finish();
    			}

    			@Override
    			public void cancel() {
    				//取消退出，返回游戏
    				listener.onExitGameCancelExit();
    			}});
		}

		@Override
		public void viewMoreGames(Context context) 
		{
			CheckTool.more(context);	
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
