package com.netmego.anysdklinking;

import java.util.HashMap;

import android.content.Context;

import com.netmego.anysdklinking.MMActivity;

import mm.purchasesdk.OnPurchaseListener;
import mm.purchasesdk.Purchase;
import mm.purchasesdk.core.PurchaseCode;


public class IAPListener implements OnPurchaseListener 
{
	private MMActivity mycontext;

	public IAPListener(Context context) 
	{
		System.out.println("Brandon : Listner maked");
		
		mycontext = (MMActivity) context;
	}

	@Override
	public void onAfterApply() {

	}

	@Override
	public void onAfterDownload() {

	}

	@Override
	public void onBeforeApply() {

	}

	@Override
	public void onBeforeDownload() {

	}

	@Override
	public void onInitFinish(int code) 
	{
		System.out.println("Brandon : Init finish, status code = " + code + ":" + Purchase.getReason(code) );
		
		mycontext.IAPInitResult(true);
	}
	
	@Override
	public void onBillingFinish(int code, HashMap arg1) 
	{
		System.out.println("Brandon : billing finish, status code = " + code);
		String result = "billing result :";
		String orderID = null;
		String paycode = null;
		String leftday = null;
		String tradeID = null;
		
		String ordertype = null;
		
		if (code == PurchaseCode.ORDER_OK || (code == PurchaseCode.AUTH_OK) ||(code == PurchaseCode.WEAK_ORDER_OK)) 
		{
			if (arg1 != null) 
			{
				leftday = (String) arg1.get(OnPurchaseListener.LEFTDAY);
				if (leftday != null && leftday.trim().length() != 0) {
					result = result + ",Left :" + leftday;
				}
				orderID = (String) arg1.get(OnPurchaseListener.ORDERID);
				if (orderID != null && orderID.trim().length() != 0) {
					result = result + ",OrderID :" + orderID;
				}
				paycode = (String) arg1.get(OnPurchaseListener.PAYCODE);
				if (paycode != null && paycode.trim().length() != 0) {
					result = result + ",Paycode:" + paycode;
				}
				tradeID = (String) arg1.get(OnPurchaseListener.TRADEID);
				if (tradeID != null && tradeID.trim().length() != 0) {
					result = result + ",tradeID:" + tradeID;
				}
				ordertype = (String) arg1.get(OnPurchaseListener.ORDERTYPE);
				if (tradeID != null && tradeID.trim().length() != 0) {
					result = result + ",ORDERTYPE:" + ordertype;
				}
				
				mycontext.PurchaseResultSuccess(tradeID);
			}
		} else 
		{
			result = "failed" + Purchase.getReason(code);
			
			mycontext.PurchaseResultFailed(Purchase.getReason(code));
		}
		System.out.println(result);

	}

	@Override
	public void onQueryFinish(int code, HashMap arg1) {
		System.out.println("Brandon : license finish, status code = " + code);
		String result = "onQueryFinish :";
		String orderID = null;
		String paycode = null;
		String leftday = null;
		if (code != PurchaseCode.QUERY_OK) 
		{
			result = "query failed :" + Purchase.getReason(code);
		} else {
			leftday = (String) arg1.get(OnPurchaseListener.LEFTDAY);
			if (leftday != null && leftday.trim().length() != 0) {
				result = result + ",Left :" + leftday;
			}
			orderID = (String) arg1.get(OnPurchaseListener.ORDERID);
			if (orderID != null && orderID.trim().length() != 0) {
				result = result + ",OrderID :" + orderID;
			}
			paycode = (String) arg1.get(OnPurchaseListener.PAYCODE);
			if (paycode != null && paycode.trim().length() != 0) {
				result = result + ",Paycode:" + paycode;
			}
		}
		System.out.println(result);
	}

	

	@Override
	public void onUnsubscribeFinish(int code) {
		// TODO Auto-generated method stub
		String result = "Brandon : onUnsubscribe :" + Purchase.getReason(code);
		System.out.println(result);
	}

}
