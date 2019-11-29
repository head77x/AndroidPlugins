package com.netmego.miguyouxisdk;

import com.netmego.miguyouxisdk.MiguSDKFactory.SDKSelector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class temper extends Activity
{
	private final static int opMISMATCHES	= 0x00000000;
	private final static int opCMCC_GC 		= 0x00000001;
	private final static int opUNICOM 		= 0x00000002;
	private final static int opTELECOM 		= 0x00000004;
	private final static int opCMCC_MM 		= 0x00000008;
	private final static int opANYSDK		= 0x00000010;
	private final static int opSP			= 0x00000020;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);
		
		Intent intent;
		
		switch (getMobileOperator()) 
		{
			case BILL_CMGD:
				try
				{
					intent = new Intent(this, cn.cmgame.billing.api.GameOpenActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					this.startActivity(intent);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			break;
			default:
				intent = new Intent(this, MegoActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent);
			break;
		}
		
		finish();
	}
	
	public SDKSelector getMobileOperator() 
	{
        TelephonyManager telManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); 
        String operator = telManager.getSimOperator();
        
        Bundle metaData = null;
		try 
		{
			metaData = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		// 강제로 셋팅된 SDK 선택자가 있는지 여부
		int BillType = Integer.valueOf(metaData.getString("BTypeValue"));
		
		Log.e("BTypeValue: ", BillType + " - ");

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
	
}
