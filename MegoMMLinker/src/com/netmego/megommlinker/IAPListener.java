package com.netmego.megommlinker;

import java.util.HashMap;

import mm.purchasesdk.OnPurchaseListener;
import mm.purchasesdk.Purchase;
import mm.purchasesdk.PurchaseCode;
import android.content.Context;
import android.util.Log;

import com.netmego.megommlinker.MegoActivity;

public class IAPListener implements OnPurchaseListener 
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
		String result = "?앭쭓?뽫퍜?쒙폏" + SMSPurchase.getReason(code);
		Log.d(TAG, "Init finish, status code = " + code + ":" + result);
	}

	@Override
	public void onBillingFinish(int code, HashMap arg1) 
	{
		String result = "溫?눌瀯볠옖竊싪?兀?닇??{";
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
	
	@Override
	public void onQueryFinish(int code, HashMap arg1) {
		Log.d(TAG, "license finish, status code = " + code);
		Message message = iapHandler.obtainMessage(IAPHandler.QUERY_FINISH);
		String result = "?θ??먨뒣,瑥ε븚?곩럴兀?물";
		// 閭ㅶА溫?눌?꼘rderID
		String orderID = null;
		// ?녶뱚?꼙aycode
		String paycode = null;
		// ?녶뱚?꾣쐣?덃쐿(餓끿쭫壅곭굳?뗥븚?곫쐣??
		String leftday = null;
		if (code != PurchaseCode.QUERY_OK) {
			/**
			 * ?θ?訝띶댆?녶뱚兀?물?꾤쎑?념에??
			 */
			result = "?θ?瀯볠옖竊?" + Purchase.getReason(code);
		} else {
			/**
			 * ?θ??겼븚?곭쉪?멨뀽岳→겘??
			 * 閭ㅶ뿶鵝졾룾餓θ렩孃쀥븚?곭쉪paycode竊똮rderid竊뚥빳?듿븚?곭쉪?됪븞?웞eftday竊덁퍎燁잒탛映삣엹?녶뱚??빳瓦붷썮竊?
			 */
			leftday = (String) arg1.get(OnPurchaseListener.LEFTDAY);
			if (leftday != null && leftday.trim().length() != 0) {
				result = result + ",?⒳퐰?띌뿴 竊?" + leftday;
			}
			orderID = (String) arg1.get(OnPurchaseListener.ORDERID);
			if (orderID != null && orderID.trim().length() != 0) {
				result = result + ",OrderID 竊?" + orderID;
			}
			paycode = (String) arg1.get(OnPurchaseListener.PAYCODE);
			if (paycode != null && paycode.trim().length() != 0) {
				result = result + ",Paycode:" + paycode;
			}
		}
		System.out.println(result);
		context.dismissProgressDialog();
	}

	

	@Override
	public void onUnsubscribeFinish(int code) {
		// TODO Auto-generated method stub
		String result = "?溫®퍜?쒙폏" + Purchase.getReason(code);
		System.out.println(result);
		context.dismissProgressDialog();
	}	

}
