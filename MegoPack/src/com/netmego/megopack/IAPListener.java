package com.netmego.megopack;

import java.util.HashMap;

import mm.sms.purchasesdk.OnSMSPurchaseListener;
import mm.sms.purchasesdk.PurchaseCode;
import mm.sms.purchasesdk.SMSPurchase;
import android.content.Context;
import android.util.Log;

import com.netmego.megopack.MegoActivity;

public class IAPListener implements OnSMSPurchaseListener 
{
	private final String TAG = "IAPListener";
	private MegoActivity context;

	public IAPListener(Context context) 
	{
		this.context = (MegoActivity) context;
	}

	@Override
	public void onInitFinish(int code) 
	{
		String result = "?ùÂßã?ñÁªì?úÔºö" + SMSPurchase.getReason(code);
		Log.d(TAG, "Init finish, status code = " + code + ":" + result);
	}

	@Override
	public void onBillingFinish(int code, HashMap arg1) 
	{
		String result = "ËÆ?¥≠ÁªìÊûúÔºöË?Ë¥?àê??{";
		String paycode = null;
		String tradeID = null;
		String netType = null;
		
		if (code == PurchaseCode.ORDER_OK) 
		{
			if (arg1 != null) 
			{
				paycode = (String) arg1.get(OnSMSPurchaseListener.PAYCODE);
				tradeID = (String) arg1.get(OnSMSPurchaseListener.TRADEID);
				netType = (String) arg1.get(OnSMSPurchaseListener.NETTYPE);
				
				context.PurchaseResultSuccess(paycode, tradeID, netType,code, SMSPurchase.getReason(code));
			}
			else
				context.PurchaseResultFailed(code, "Hashmap null");
		} 
		else 
		{
			context.PurchaseResultFailed(code, SMSPurchase.getReason(code));
		}
		
		System.out.println(result);

	}

}
