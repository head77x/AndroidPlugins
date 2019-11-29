package com.netmego.miguyouxisdk;

import java.util.HashMap;

import mm.purchasesdk.OnPurchaseListener;
import mm.purchasesdk.Purchase;
import mm.purchasesdk.core.PurchaseCode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;


import cn.egame.terminal.sdk.log.EgameAgent;

import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKAnalytics;
import com.anysdk.framework.java.AnySDKUser;
import com.unicom.dcLoader.Utils;
import com.unicom.dcLoader.Utils.UnipayPayResultListener;

public class MiguSDKFactory 
{
	// SDK 선택자
	public enum SDKSelector {
		BILL_UNKNOWN,
		BILL_CMGD,
		BILL_CMMM,
		BILL_UNICOM,
		BILL_DIANXIN,
		BILL_OTHER,
		BILL_SP
	};
	
	// 빌링 결과 처리 공통 리스너
	public interface BillingListener {
		public void onPurchaseSucceed(String item);
		public void onPurchaseCanceld(String item, String msg);
		public void onPurchaseFailed(String item, String msg);
		public void onPurchaseInfo(String item, String msg);
	};
	
	// 게임 종료 처리 리스너 - CMGD 용
	public interface ExitGameListener {
		public void onExitGameCancelExit();
		public void onExitGameConfirmExit();
		public void onExitGameInGame();
	};
	
	private static boolean _bIniting = false;
	private static boolean _bInited = false;
	
	private static Activity _context;
	
	private SDKFactoryBase _smsPayer;
	
	private static MiguSDKFactory _singleton;

	private final static int opMISMATCHES	= 0x00000000;
	private final static int opCMCC_GC 		= 0x00000001;
	private final static int opUNICOM 		= 0x00000002;
	private final static int opTELECOM 		= 0x00000004;
	private final static int opCMCC_MM 		= 0x00000008;
	private final static int opANYSDK		= 0x00000010;
	private final static int opSP			= 0x00000020;
	
	static public SDKSelector getMobileOperator() 
	{
        TelephonyManager telManager = (TelephonyManager)_context.getSystemService(Context.TELEPHONY_SERVICE); 
        String operator = telManager.getSimOperator();
        
        Bundle metaData = null;
		try 
		{
			metaData = _context.getPackageManager().getApplicationInfo(_context.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		// 강제로 셋팅된 SDK 선택자가 있는지 여부
		int BillType = Integer.valueOf(metaData.getString("BTypeValue"));
		
		Log.e("BTypeValue: ", BillType + "");

		if (BillType == opCMCC_GC) {
			return SDKSelector.BILL_CMGD;
		} else if (BillType == opUNICOM) {
			return SDKSelector.BILL_UNICOM;
		} else if (BillType == opTELECOM) {
			return SDKSelector.BILL_DIANXIN;
		} else if (BillType == opCMCC_MM) {
			return SDKSelector.BILL_CMMM;
		} else if (BillType == opANYSDK) {
			return SDKSelector.BILL_OTHER;
		} else if (BillType == opSP) {
			return SDKSelector.BILL_SP;
		}
		
		
		// 강제 셋팅한게 없다면, SIM 에 따라서 자동 선택 되도록 처리
        if (operator != null)
        {
            if ((operator.equals("46000") || operator.equals("46002"))) 
            {
            	if ((BillType & opCMCC_MM) != opMISMATCHES) 
            	{
        			System.out.println("Brandon : CMCC_MM!!!! :");
            		return SDKSelector.BILL_CMMM;		
				} 
            	else if ((BillType & opCMCC_GC) != opMISMATCHES) 
            	{
        			System.out.println("Brandon : CMCC_GC!!!! :");
					return SDKSelector.BILL_CMGD;
				}
            	
            	return SDKSelector.BILL_CMGD;
            } 
            else 
            if (operator.equals("46001") && (BillType & opUNICOM) != opMISMATCHES) 
            {
    			System.out.println("Brandon : UNICOM!!!! :");
    			
            	return SDKSelector.BILL_UNICOM;
            } 
            else 
            if (operator.equals("46003") && (BillType & opTELECOM) != opMISMATCHES) 
            {
    			System.out.println("Brandon : CTE!!!! :");
            	
            	return SDKSelector.BILL_DIANXIN;
            }
            else
                if ((BillType & opSP) != opMISMATCHES) 
                {
        			System.out.println("Brandon : SP!!!! :");
                	
                	return SDKSelector.BILL_SP;
                }
            	
            
    		System.out.println("Brandon : Cannot check !! :" + operator);
        }
        else
    		System.out.println("Brandon : operator null !!" );
        
		return SDKSelector.BILL_OTHER;
    }
	
	// #2-1. 결제 모듈 팩토리 초기화
	public static void init(
			Activity context, BillingListener MainListener,
			String AnySDK_AppKey, String AnySDK_AppSecret, String AnySDK_privateKey, String AnySDK_oauthLoginServer,
			String MM_appID, String MM_appkey, 
			String Company, String Telephone, String appName
			) 
	{
		if (!_bIniting && !_bInited) 
		{
			_bIniting = true;
			_context = context;
			if (_singleton == null) 
			{
				_singleton = new MiguSDKFactory( MainListener,
						AnySDK_AppKey, AnySDK_AppSecret, AnySDK_privateKey, AnySDK_oauthLoginServer,
						MM_appID, MM_appkey,
						Company, Telephone, appName);
			}
			_bIniting = false;
		}
	}
	
	// #2-2. 팩토리 생성자
	private MiguSDKFactory( BillingListener MainListener,
			String AnySDK_AppKey, String AnySDK_AppSecret, String AnySDK_privateKey, String AnySDK_oauthLoginServer,
			String MM_appid, String MM_appkey,
			String Company, String Telephone, String appName 
			) 
	{
		CMGD_Manager.initSingleton(_context, MainListener);
		
		switch (getMobileOperator()) 
		{
			case BILL_OTHER:		// 여기에 ANYSDK 처리할것
				AnySDK_Manager.initSingleton(_context, AnySDK_AppKey, AnySDK_AppSecret, AnySDK_privateKey, AnySDK_oauthLoginServer);
				break;
			case BILL_CMMM:
				CMMM_Manager.initSingleton(_context, MM_appid, MM_appkey, MainListener );
			break;
			case BILL_UNICOM:
				Unicom_Manager.initSingleton(_context, MainListener);
			break;
			case BILL_DIANXIN:
				Telecom_Manager.initSingleton(_context, MainListener);
			break;
			case BILL_SP:
				SP_Manager.initSingleton(_context, MainListener);
			break;
			default:
				AnySDK_Manager.initSingleton(_context, AnySDK_AppKey, AnySDK_AppSecret, AnySDK_privateKey, AnySDK_oauthLoginServer);
				break;
		}
		
		_bInited = true;
	}
	
	
	public Activity getContext() {
		return _context;
	}
	
	public synchronized static MiguSDKFactory getInstance() 
	{
		return _singleton;
	}
	
	protected void initFinished() {
		_bInited = true;
	}
	
	public int getMobileOperatorType()
	{
		SDKSelector whattype = getMobileOperator();
		
		if( whattype == SDKSelector.BILL_CMGD)
			return 0;
		else
			if( whattype == SDKSelector.BILL_UNICOM)
				return 1;
			else
				if( whattype == SDKSelector.BILL_DIANXIN)
					return 2;
				else
					if( whattype == SDKSelector.BILL_CMMM)
						return 3;
		
		return 0;
	}
	
	
	public void pay(Context context, String smsPayItem, String paycode_3rd, String props, String Money, BillingListener listener, boolean isRepeated) 
	{
		if (!_bInited) 
		{
			System.out.println("Brandon : not yet init !!!!");
		}
		else  
		{
			switch (getMobileOperator()) 
			{
				case BILL_OTHER:		// 여기에 ANYSDK 처리할것
					_smsPayer = AnySDK_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
					break;
				case BILL_CMMM:
					_smsPayer = CMMM_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
				break;
				case BILL_CMGD:
					_smsPayer = CMGD_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
				break;
				case BILL_UNICOM:
					_smsPayer = Unicom_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
				break;
				case BILL_DIANXIN:
					_smsPayer = Telecom_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
				break;
				case BILL_SP:
					_smsPayer = SP_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
				break;
				default:
					listener.onPurchaseFailed(smsPayItem, "no singleton!!!!");
				break;
			}
			
		}
	}
	
	public void exitGame(Context context, ExitGameListener listener) 
	{
		switch (getMobileOperator()) 
		{
			case BILL_OTHER:		// 여기에 ANYSDK 처리할것
				_smsPayer = AnySDK_Manager.getInstance();
				break;
			case BILL_CMMM:
				_smsPayer = CMMM_Manager.getInstance();
			break;
			case BILL_CMGD:
				_smsPayer = CMGD_Manager.getInstance();
			break;
			case BILL_UNICOM:
				_smsPayer = Unicom_Manager.getInstance();
			break;
			case BILL_DIANXIN:
				_smsPayer = Telecom_Manager.getInstance();
			break;
			default:
				listener.onExitGameConfirmExit();
			return;
		}
		
		System.out.println("Brandon : ask exit!!!! :");
		
		_smsPayer.exitGame(context, listener);
	}
	
	public void viewMoreGames(Context context) 
	{
		switch (getMobileOperator()) 
		{
			case BILL_OTHER:		// 여기에 ANYSDK 처리할것
				_smsPayer = AnySDK_Manager.getInstance();
				break;
			case BILL_CMMM:
				_smsPayer = CMMM_Manager.getInstance();
			break;
			case BILL_CMGD:
				_smsPayer = CMGD_Manager.getInstance();
			break;
			case BILL_UNICOM:
				_smsPayer = Unicom_Manager.getInstance();
			break;
			case BILL_DIANXIN:
				_smsPayer = Telecom_Manager.getInstance();
			break;
			default:
			return;
		}
		
		_smsPayer.viewMoreGames(context);
	}
	
	public boolean isMusicEnabled() 
	{
		switch (getMobileOperator()) 
		{
			case BILL_OTHER:		// 여기에 ANYSDK 처리할것
				_smsPayer = AnySDK_Manager.getInstance();
				break;
			case BILL_CMMM:
				_smsPayer = CMMM_Manager.getInstance();
			break;
			case BILL_CMGD:
				_smsPayer = CMGD_Manager.getInstance();
			break;
			case BILL_UNICOM:
				_smsPayer = Unicom_Manager.getInstance();
			break;
			case BILL_DIANXIN:
				_smsPayer = Telecom_Manager.getInstance();
			break;
			default:
			return true;
		}
		
		return _smsPayer.isMusicEnabled();
	}	
	
	public void doScreenShotShare(Context context, final Uri uri)
	{
		switch (getMobileOperator()) 
		{
			case BILL_OTHER:		// 여기에 ANYSDK 처리할것
				_smsPayer = AnySDK_Manager.getInstance();
				break;
			case BILL_CMMM:
				_smsPayer = CMMM_Manager.getInstance();
			break;
			case BILL_CMGD:
				_smsPayer = CMGD_Manager.getInstance();
			break;
			case BILL_UNICOM:
				_smsPayer = Unicom_Manager.getInstance();
			break;
			case BILL_DIANXIN:
				_smsPayer = Telecom_Manager.getInstance();
			break;
			default:
			return;
		}
		
		_smsPayer.doScreenShotShare(context, uri);
	}
	
	public void Pause(Context context)
	{
		System.out.println("onPause");
		
		switch (getMobileOperator()) 
		{
			case BILL_OTHER:		// 여기에 ANYSDK 처리할것
				if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
				{
					AnySDKAnalytics.getInstance().stopSession();
				}
				break;
			case BILL_CMMM:
				_smsPayer = CMMM_Manager.getInstance();
			break;
			case BILL_CMGD:
				_smsPayer = CMGD_Manager.getInstance();
			break;
			case BILL_UNICOM:
				if ( Unicom_Manager.getInstance() != null )
					Utils.getInstances().onPause(context);
			break;
			case BILL_DIANXIN:
				EgameAgent.onPause(context);
			break;
			default:
			return;
		}
	}
	
	public void Resume(Context context, boolean appforeground)
	{
		System.out.println("onResume");

		switch (getMobileOperator()) 
		{
			case BILL_OTHER:		// 여기에 ANYSDK 처리할것
				if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
				{
					AnySDKAnalytics.getInstance().startSession();
					if(!appforeground)
						AnySDKUser.getInstance().callFunction("pause");
				}
				break;
			case BILL_CMMM:
				_smsPayer = CMMM_Manager.getInstance();
			break;
			case BILL_CMGD:
				_smsPayer = CMGD_Manager.getInstance();
			break;
			case BILL_UNICOM:
				if ( Unicom_Manager.getInstance() != null )
					Utils.getInstances().onResume(context);
			break;
			case BILL_DIANXIN:
				EgameAgent.onResume(context);
			break;
			default:
			return;
		}
	}
	
	public void Stop(Context context)
	{
		System.out.println("onStop");

		switch (getMobileOperator()) 
		{
			case BILL_OTHER:		// 여기에 ANYSDK 처리할것
				if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
				{
					AnySDKAnalytics.getInstance().stopSession();
				}
				break;
			case BILL_CMMM:
			break;
			case BILL_CMGD:
			break;
			case BILL_UNICOM:
			break;
			case BILL_DIANXIN:	
			break;
			default:
			return;
		}
		
	}
	
	public void Destroy(Context context)
	{
		System.out.println("onDestroy");
		 
		if ( AnySDK_Manager.getInstance() != null && AnySDK_Manager.getInstance().Initialized )
		{
			AnySDKUser.getInstance().callFunction("destroy");
			AnySDK.getInstance().release();
		}
	}
}
