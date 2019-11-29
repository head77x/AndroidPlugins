package com.netmego.miguyouxisdk;

import com.anysdk.framework.java.AnySDKIAP;
import com.netmego.miguyouxisdk.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyouxisdk.SDKFactoryBase;
import com.netmego.miguyouxisdk.MiguSDKFactory.BillingListener;

import com.unicom.dcLoader.Utils;
import com.unicom.dcLoader.Utils.UnipayPayResultListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class Unicom_Manager extends SDKFactoryBase
{
	private static Activity _context;
		
	BillingListener MasterListener; 
	UniPayListener myListener;
	
	String process_payitem;
	
	private Unicom_Manager(BillingListener listener) {
		
		
		myListener = new UniPayListener();
		MasterListener = listener;
		
		Utils.getInstances().initSDK(_context,new UniPayListener());
	}
	
	private static Unicom_Manager _singletonSmsPayUnicom;
	public static Unicom_Manager getInstance() 
	{
		return _singletonSmsPayUnicom;
	}
	
	public static Unicom_Manager initSingleton(Activity context, BillingListener listener) 
	{
		_context = context;
		if (_singletonSmsPayUnicom == null) 
		{
			_singletonSmsPayUnicom = new Unicom_Manager(listener);
		}
		return _singletonSmsPayUnicom;
	}
	
	public class UniPayListener implements 	UnipayPayResultListener	/* Unicom callback */
	{
		@Override
		public void PayResult(String paycode, int flag, int flag2, String desc) 
		{
			System.out.println("Brandon : Pay UN start = " + paycode + ":" + flag + ":" + flag2 + ":" + desc + ":" + process_payitem );
			
			// ��빰��퍡�먨뒣
			switch(flag )
			{
				case 1:
					Log.e("======�붼� SDK=======", "SUCCESS : " + flag + ":" + desc);
					MasterListener.onPurchaseSucceed(process_payitem);
				break;
				case 2:
					Log.e("======�붼� SDK=======", "FAILED: " + desc);
					MasterListener.onPurchaseFailed(process_payitem, desc);
				break;
				case 3:
					Log.e("======�붼� SDK=======", "CANCEL: " + desc);
					MasterListener.onPurchaseCanceld(process_payitem, "Cancel");
				break;
			}
		
		}
	}
	
	
	@Override
	public void pay(Context context, String smsPayItem, String ext_code, String props, String Money, BillingListener listener, boolean isRepeated) 
	{
		process_payitem = smsPayItem;
		
		Utils.getInstances().pay(context, smsPayItem, myListener );		
//		Utils.getInstances().setBaseInfo(context, _otherpay, true, "http://uniview.wostore.cn/log-app/test");
//		Utils.getInstances().pay(context, paycode_3rd, smsPayItem, props, Money, AppID, myListener);
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
	public void viewMoreGames(Context context) 
	{
		Utils.getInstances().MoreGame(context);
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
