package com.netmego.miguyouxisdk;

import java.util.HashMap;

import com.netmego.miguyouxisdk.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxisdk.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyouxisdk.SDKFactoryBase;

import mm.purchasesdk.OnPurchaseListener;
import mm.purchasesdk.Purchase;
import mm.purchasesdk.core.PurchaseCode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class CMMM_Manager extends SDKFactoryBase
{
	private static Activity _context;
	
	private String _appid;
	private String _appKey;
	
	public  Purchase purchase;
	
	BillingListener MasterListener;
	MMListener myListener;
	
	private static CMMM_Manager _singletonSmsPayCMCCMM;
	public static CMMM_Manager getInstance() 
	{
		return _singletonSmsPayCMCCMM;
	}
	
		private CMMM_Manager(BillingListener listener, String MM_appid, String MM_appkey) 
		{
			MasterListener = listener;
			
			_appid = MM_appid;
			_appKey = MM_appkey;
			
			purchase = Purchase.getInstance();
			
			myListener = new MMListener(listener);
			
			try {
				purchase.setAppInfo(_appid, _appKey);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			try {
				purchase.init(_context, myListener);
			} catch (Exception e) {
				 e.printStackTrace();
			}
		}
		
		public static CMMM_Manager initSingleton(Activity context, String MM_appid, String MM_appkey, BillingListener listener) 
		{
			_context = context;
			if (_singletonSmsPayCMCCMM == null) {
				_singletonSmsPayCMCCMM = new CMMM_Manager(listener, MM_appid, MM_appkey);
			}
			return _singletonSmsPayCMCCMM;
		}
		
		
		
		
		@Override
		public void pay(Context context, String smsPayItem, String ext_code, String props, String Money, BillingListener listener, boolean isRepeated) 
		{
			System.out.println("Brandon : Pay MM start = " + myListener );
			
		    Bundle metaData = null;
			try {
				metaData = _context.getPackageManager().getApplicationInfo(_context.getPackageName(), PackageManager.GET_META_DATA).metaData;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				MasterListener.onPurchaseFailed(smsPayItem, "Cannot get Paycode from Androidmanifest.xml");
				return;
			}

			String Paycode = metaData.getString(smsPayItem);

			System.out.println("Brandon : Mobile MM Paycode : " + Paycode + ":" + props );
			
			purchase.order(context, Paycode, 1, props, isRepeated, myListener );
		}

		private Handler mUIHandler = new Handler();

		@Override
		public void exitGame(final Context context, final ExitGameListener listener) 
		{
	        mUIHandler.post(new Runnable() {
	            @Override
	            public void run() {
	                new AlertDialog.Builder(context)
	                .setTitle("游戏退出")
	                .setMessage("现在游戏退出吗？")
	                .setPositiveButton("不", 
	                        new DialogInterface.OnClickListener() {
	                            
	                            @Override
	                            public void onClick(DialogInterface dialog, int which) 
	                            {
	                        		listener.onExitGameCancelExit();
	                            }
	                        })
	                .setNegativeButton("是", 
	                        new DialogInterface.OnClickListener() {
	                            
	                            @Override
	                            public void onClick(DialogInterface dialog, int which) 
	                            {
	                        		listener.onExitGameConfirmExit();
	                            }
	                        }).create().show();
	            }
	        });
			
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

