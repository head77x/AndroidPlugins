package com.netmego.anysdklinking;

import java.util.ArrayList;
import java.util.HashMap;

import mm.purchasesdk.Purchase;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MMActivity extends UnityPlayerActivity 
{
	public enum SmsMobileOperator {
		kMOBILE_OPERATOR_UNKNOWN,
		kMOBILE_OPERATOR_CMCC_GC,
		kMOBILE_OPERATOR_CMCC_MM,
		kMOBILE_OPERATOR_UNICOM,
		kMOBILE_OPERATOR_TELECOM_CTE,
		kMOBILE_OPERATOR_OTHER,
	};

	private final static int opMISMATCHES	= 0;
	private final static int opCMCC_GC 		= 1;
	private final static int opUNICOM 		= 10;
	private final static int opTELECOM 		= 100;
	private final static int opCMCC_MM 		= 1000;
	private final static int opANYSDK 		= 10000;
		
    String MMID;
    String MMKey;
	
    SmsMobileOperator bill_type;

	private ProgressDialog mProgressDialog;
	private IAPListener mListener;
	public  Purchase purchase;
	
	private static Activity mAct = null;
	private Handler mUIHandler = new Handler();
    
    String Init_callbackGameObject;
    String Init_callbackFunc;
	
    String IAP_callbackGameObject;
    String IAP_callbackFunc;
	
	public SmsMobileOperator getMobileOperator() 
	{
		System.out.println("Brandon : GetMobile!!!! :");
		
		String operator = null;
		TelephonyManager telManager = null;
		
		try {
        telManager = (TelephonyManager)mAct.getSystemService(Context.TELEPHONY_SERVICE); 
        operator = telManager.getSimOperator();
		} catch (Exception e) {
			e.printStackTrace();
		}
							
        Bundle metaData = null;
        int nSOValue = 0;
		try {
			System.out.println(mAct + ":" + mAct.getPackageManager() + ":" + mAct.getPackageName() + ":" +
					mAct.getPackageManager().getApplicationInfo(mAct.getPackageName(), PackageManager.GET_META_DATA));
			metaData = mAct.getPackageManager().getApplicationInfo(mAct.getPackageName(), PackageManager.GET_META_DATA).metaData;
			nSOValue = Integer.valueOf(metaData.getString("SOBill"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("SOBill: " + nSOValue);

		if (nSOValue == opCMCC_GC) {
			return SmsMobileOperator.kMOBILE_OPERATOR_CMCC_GC;
		} else if (nSOValue == opUNICOM) {
			return SmsMobileOperator.kMOBILE_OPERATOR_UNICOM;
		} else if (nSOValue == opTELECOM) {
			return SmsMobileOperator.kMOBILE_OPERATOR_TELECOM_CTE;
		} else if (nSOValue == opCMCC_MM) 
		{
			return SmsMobileOperator.kMOBILE_OPERATOR_CMCC_MM;
		} else if (nSOValue == opANYSDK) {
			return SmsMobileOperator.kMOBILE_OPERATOR_OTHER;
		}
		
        if (operator != null)
        {
            if ((operator.equals("46000") || operator.equals("46002"))) 
            {
            	if ((nSOValue & opCMCC_MM) != opMISMATCHES) 
            	{
        			System.out.println("Brandon : CMCC_MM!!!! :");
            		return SmsMobileOperator.kMOBILE_OPERATOR_CMCC_MM;		// 燁삣뒯MM
				} 
            	else if ((nSOValue & opCMCC_GC) != opMISMATCHES) 
            	{
        			System.out.println("Brandon : CMCC_GC!!!! :");
					return SmsMobileOperator.kMOBILE_OPERATOR_CMCC_GC;		// 燁삣뒯?뷴쑑
				}
            	
            	
            	return SmsMobileOperator.kMOBILE_OPERATOR_OTHER;
            } 
            else 
            if (operator.equals("46001") && (nSOValue & opUNICOM) != opMISMATCHES) 
            {
    			System.out.println("Brandon : UNICOM!!!! :");
    			
            	return SmsMobileOperator.kMOBILE_OPERATOR_UNICOM;
            } 
            else 
            if (operator.equals("46003") && (nSOValue & opTELECOM) != opMISMATCHES) 
            {
    			System.out.println("Brandon : CTE!!!! :");
            	
            	return SmsMobileOperator.kMOBILE_OPERATOR_TELECOM_CTE;
            }
            
    		System.out.println("Brandon : Cannot check, ANYSDK !! :" + operator);
        }
        else
    		System.out.println("Brandon : operator null !!" );
        	
        
		return SmsMobileOperator.kMOBILE_OPERATOR_OTHER;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
/*		
		mUIHandler.post( new Runnable()
		{
			public void run()
			{
				bill_type = getMobileOperator();
				System.out.println("Brandon : init finish !" + bill_type);
			}
		});
*/
		System.out.println("Brandon : initMMSDK");
		
		mAct = MMActivity.this;
		
		mProgressDialog = new ProgressDialog(mAct);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setMessage("瑥루쮰??..");
		
		mListener = new IAPListener(this);
		purchase = Purchase.getInstance();

        Bundle metaData = null;
		try {
			metaData = mAct.getPackageManager().getApplicationInfo(mAct.getPackageName(), PackageManager.GET_META_DATA).metaData;
			MMID = metaData.getString("MMID");
			MMKey = metaData.getString("MMKey");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("Brandon : Get MMSDK :" + MMID + ":" + MMKey);
				
		try 
		{
			purchase.setAppInfo(MMID, MMKey);
		} 
		catch (Exception e1) 
		{
			e1.printStackTrace();
			System.out.println("Brandon : setAppInfo Failed");
			IAPInitResult(false);
			return;
		}
				
		System.out.println("Brandon : setAppInfo :" + MMID + ":" + MMKey);
		
		try 
		{
			purchase.init(mAct, mListener);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Brandon : initMMSDK Failed");
			IAPInitResult(false);
			return;
		}
		
//		showProgressDialog();
	}
	
	public void OnInit(final String appKey, final String appSecret, final String privateKey, final String oauthLoginServer,
			final String callbackGameObject, final String callbackFunc )
	{
		Init_callbackGameObject = callbackGameObject;
		Init_callbackFunc = callbackFunc;
	}

public void OnBuy(final String Product_Price, final String Product_Id, final String Product_Name, final String Server_Id, final String Product_Count, 
		final String Role_Id, final String Role_Name, final String Role_Grade, final String Role_Balance, final String Ext,
		final String callbackGameObject, final String callbackFunc ) 
{
	IAP_callbackGameObject = callbackGameObject;
	IAP_callbackFunc = callbackFunc;
	
	System.out.println("Brandon : Mobile MM : " + Product_Id);
	
    Bundle metaData = null;
	try {
		metaData = mAct.getPackageManager().getApplicationInfo(mAct.getPackageName(), PackageManager.GET_META_DATA).metaData;
	} catch (NameNotFoundException e) {
		e.printStackTrace();
		PurchaseResultFailed("Cannot get Paycode from Androidmanifest.xml");
		return;
	}

	String Paycode = metaData.getString(Product_Id);

	System.out.println("Brandon : Mobile MM Paycode : " + Paycode + ":" + Integer.valueOf(Product_Count) + ":" + Product_Name );
			
	try 
	{
		String result = purchase.order(mAct, Paycode, Integer.valueOf(Product_Count), Product_Name, false, mListener );
		System.out.println("Brandon : Failed buy" + result );
	} 
	catch (Exception e) 
	{
		e.printStackTrace();
		System.out.println("Brandon : Failed buy" );
		PurchaseResultFailed("Cannot get Paycode from Androidmanifest.xml");
		return;
	}
}	

public void IAPInitResult(boolean flag)
{
	System.out.println("Brandon : Init result : " + flag);
	
/*	
	if ( flag == true )
	{
		UnityPlayer.UnitySendMessage( Init_callbackGameObject, Init_callbackFunc, "111" );
	}
	else
	{
		UnityPlayer.UnitySendMessage( Init_callbackGameObject, Init_callbackFunc, "222" );
	}
*/
}

public void PurchaseResultSuccess(String orderid)
{
	String result = "111";
	
	result = result + "|";
			
	if (orderid != null && orderid.trim().length() != 0) 
	{
		result = result + orderid;
	}
	
	UnityPlayer.UnitySendMessage( IAP_callbackGameObject, IAP_callbackFunc, result );
	System.out.println(result);
	
}

public void PurchaseResultFailed(String msg)
{
	String result = "222";
	
	result = result + "|0|" + msg;

	UnityPlayer.UnitySendMessage( IAP_callbackGameObject, IAP_callbackFunc, result );
	System.out.println(result);
}

public static void Exit() {
 mAct.finish();
 System.exit(0);
 
}
}
