package com.nhn.android.appstore.iap.payment.common;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import android.util.Log;
import com.naver.android.appstore.iap.NIAPHelperErrorType;
import com.naver.android.appstore.iap.Purchase;
import com.unity3d.player.UnityPlayer;

public class ManagerHelperBase {
	
	/**
	 * NIAPResult瑜� String�쑝濡� 蹂��솚
	 * @param result
	 */
	public static String getResult(Bundle paramBundle) {
		return paramBundle.getString("result");
	}
	
	/**
	 * Unity濡� �꽦怨� 硫붿꽭吏�瑜� 蹂대궦�떎
	 * @param invokeMethod
	 * @param response
	 */
	public static void sendSuccess(UnityPluginIAPConstant.InvokeMethod invokeMethod, String response) {
		Log.d(UnityPluginIAPConstant.TAG_IAP, "sendSuccess : " + response);
		JSONObject json = new JSONObject();		
		try	{
			json.put(UnityPluginIAPConstant.INVOKEMETHOD, invokeMethod.getName());
			json.put(UnityPluginIAPConstant.Param.RESULT, response);
		} catch(JSONException e) {
			Log.e(UnityPluginIAPConstant.TAG_IAP, "sendSuccess json parse error!", e);
		}
		
		UnityPlayer.UnitySendMessage(UnityPluginIAPConstant.UNITY_IAP_OBJECT_NAME, "returnSuccess", json.toString());
	}	


	/**
	 * Unity濡� �떎�뙣 硫붿꽭吏�瑜� 蹂대궦�떎 
	 * @param invokeMethod
	 * @param errorResult
	 */
	public static void sendFailure(UnityPluginIAPConstant.InvokeMethod invokeMethod, NIAPHelperErrorType errorResult) {
		Log.d(UnityPluginIAPConstant.TAG_IAP, "sendFailure : " + errorResult.getErrorDetails());
		JSONObject json = new JSONObject();
		try	{
			json.put(UnityPluginIAPConstant.INVOKEMETHOD, invokeMethod.getName());
			json.put(UnityPluginIAPConstant.Param.CODE, errorResult.getErrorCode());
			json.put(UnityPluginIAPConstant.Param.MESSAGE, errorResult.getErrorDetails());
		}catch(JSONException e) {
			Log.e(UnityPluginIAPConstant.TAG_IAP, "sendFailure json parse error!", e);
		}
		Log.d(UnityPluginIAPConstant.TAG_IAP, "sendFailureResult : " + json.toString());
		UnityPlayer.UnitySendMessage(UnityPluginIAPConstant.UNITY_IAP_OBJECT_NAME, "returnFailure", json.toString());
	}

	/**
	 * Unity濡� 濡쒓렇�씤 痍⑥냼 硫붿꽭吏�瑜� 蹂대궦�떎
	 * @param invokeMethod
	 * @param response
	 */
	public static void sendCancel(UnityPluginIAPConstant.InvokeMethod invokeMethod) {
		Log.d(UnityPluginIAPConstant.TAG_IAP, "sendCancel");
		JSONObject json = new JSONObject();
		try {
			json.put(UnityPluginIAPConstant.INVOKEMETHOD, invokeMethod.getName());
		} catch(JSONException e) {
			Log.e(UnityPluginIAPConstant.TAG_IAP, "sendCancel json parse error!", e);
		}
		
		UnityPlayer.UnitySendMessage(UnityPluginIAPConstant.UNITY_IAP_OBJECT_NAME, "returnCancel", json.toString());
	}
	
	/**
	 * Unity濡� 寃곗젣 �젙蹂대�� �쟾�떖�븳�떎.
	 * @param invokeMethod
	 * @param purchase
	 */
	public static void sendPurchaseSuccess(UnityPluginIAPConstant.InvokeMethod invokeMethod, Purchase purchase) {
		Log.d(UnityPluginIAPConstant.TAG_IAP, "sendSuccess : " + purchase);
		JSONObject json = new JSONObject();		
		try	{
			json.put(UnityPluginIAPConstant.INVOKEMETHOD, invokeMethod.getName());
			json.put(UnityPluginIAPConstant.Param.SIGNATURE, purchase.getSignature());
			json.put(UnityPluginIAPConstant.Param.RESULT, purchase.getOriginalPurchaseAsJsonText());
			
		} catch(JSONException e) {
			Log.e(UnityPluginIAPConstant.TAG_IAP, "sendSuccess json parse error!", e);
		}
		
		UnityPlayer.UnitySendMessage(UnityPluginIAPConstant.UNITY_IAP_OBJECT_NAME, "returnSuccess", json.toString());
	}	
}
