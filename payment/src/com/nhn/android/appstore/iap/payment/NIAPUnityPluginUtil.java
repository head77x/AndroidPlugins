/*
 * @(#)NIAPUnityPluginUtil.java $version 2014. 11. 26.
 *
 * Copyright 2014 NHN Corp. All rights Reserved.
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.nhn.android.appstore.iap.payment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import com.naver.android.appstore.iap.InvalidProduct;
import com.naver.android.appstore.iap.NIAPHelper;
import com.naver.android.appstore.iap.NIAPHelper.ConsumeListener;
import com.naver.android.appstore.iap.NIAPHelper.GetProductDetailsListener;
import com.naver.android.appstore.iap.NIAPHelper.GetPurchasesListener;
import com.naver.android.appstore.iap.NIAPHelper.GetSinglePurchaseListener;
import com.naver.android.appstore.iap.NIAPHelper.RequestPaymentListener;
import com.naver.android.appstore.iap.NIAPHelperErrorType;
import com.naver.android.appstore.iap.NIAPHelperException;
import com.naver.android.appstore.iap.Product;
import com.naver.android.appstore.iap.Purchase;
import com.nhn.android.appstore.iap.payment.common.ManagerHelperBase;
import com.nhn.android.appstore.iap.payment.common.UnityPluginIAPConstant;
import com.unity3d.player.UnityPlayerActivity;

public class NIAPUnityPluginUtil extends ManagerHelperBase {
	
	public static final String NIAP_LOG_TAG = "NIAP_UNITY";
	
	/**
	 * �꽕�씠踰� �씤�빋 V2 RequestedMethod
	 * @param jsonRequestParam : �떎�뻾�떆�궗 method 紐�
	 * @param activity : activity
	 * @param niapHelper : niapHelper 
	 */
	public static void runIAPRequestedMethod(String jsonRequestParam, UnityPlayerActivity activity, NIAPHelper niapHelper) {
		JSONObject requestParam = null;
		try {
			requestParam = new JSONObject(jsonRequestParam);
			String invokeMethod = requestParam.getString(UnityPluginIAPConstant.INVOKEMETHOD);
			
			switch (UnityPluginIAPConstant.InvokeMethod.findBy(invokeMethod)) {					
				case GET_PRODUCT_INFOS:
					JSONArray jsonArray = requestParam.getJSONArray(UnityPluginIAPConstant.Param.PRODUCT_CODES);
					getProductDetails(jsonArrayToList(jsonArray), activity, niapHelper);
				break;
				case REQUEST_PAYMENT:
					String productCode = requestParam.getString(UnityPluginIAPConstant.Param.PRODUCT_CODE);
	    			int niapRequestCode = requestParam.getInt(UnityPluginIAPConstant.Param.PAYMENT_REQUEST_CODE);
	    			String payLoad = requestParam.getString(UnityPluginIAPConstant.Param.PAYLOAD);
	    			requestPayment(productCode, niapRequestCode, payLoad, activity, niapHelper);
				break;
				case REQUEST_CONSUME:
					String purchaseAsJsonText = requestParam.getString(UnityPluginIAPConstant.Param.PURCHASE_JSONTEXT);	    			
	    			String signature = requestParam.getString(UnityPluginIAPConstant.Param.SIGNATURE);
					consumeAsync(purchaseAsJsonText, signature, activity, niapHelper);
				break;
				case GET_PURCHASES:
					requestPurchases(activity, niapHelper);
				break;
				case GET_SINGLEPURCHASE:
					String paymentSeq = requestParam.getString(UnityPluginIAPConstant.Param.PAYMENTSEQ);
					requestSinglePurchase(paymentSeq, activity, niapHelper);
				break;
				default:
					Log.e(NIAP_LOG_TAG, "IAP unknown invoke method : " + invokeMethod);
				break;
			}
		} catch (Exception e) {
			Log.e(NIAP_LOG_TAG, "IAP unity java bridge error has occured!", e);
		}
	}
	
	/**
	 * �씤�빋 �긽�뭹 由ъ뒪�듃瑜� 媛��졇�삩�떎. 
	 * @param productCodeList : �씤�빋�긽�뭹 code List
	 * @param activity : activity
	 * @param niapHelper : niapHelper
	 */
	public static void getProductDetails(final ArrayList<String> productCodeList, final UnityPlayerActivity activity, final NIAPHelper niapHelper) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				niapHelper.getProductDetailsAsync(productCodeList, new GetProductDetailsListener() {
					@Override
					public void onSuccess(final List<Product> validProducts, List<InvalidProduct> invalidProducts) {
						
						JSONArray validProductList = new JSONArray();						
						for (Product product : validProducts) {
							JSONObject productJson = new JSONObject();
							try {								
								productJson.put("productCode", product.getProductCode());
								productJson.put("productName", product.getProductName());
								productJson.put("productType", product.getProductType().toString());
								productJson.put("productPrice", product.getProductPrice());
								productJson.put("sellPrice", product.getSellPrice());								
								productJson.put("advantage", product.getAdvantage().getMileage());
								productJson.put("productStatus", product.getProductStatus().toString());
								productJson.put("offerCancelableYn", product.getOfferCancelableYn());
								productJson.put("discount", product.getDiscount().getPrice());
								validProductList.put(productJson);
							} catch (JSONException e) {
								Log.e(NIAP_LOG_TAG, "requestProductInfos JSONException", e);		
							}
						}
						sendSuccess(UnityPluginIAPConstant.InvokeMethod.GET_PRODUCT_INFOS, validProductList.toString());											
					}			
					
					@Override
					public void onFail(final NIAPHelperErrorType errorType) {			
						sendFailure(UnityPluginIAPConstant.InvokeMethod.GET_PRODUCT_INFOS, errorType);				
					}
				});
			}		
		});
	}
	
	/**
	 * 寃곗젣瑜� �슂泥��븳�떎. 
	 * @param productCode : �씤�빋�긽�뭹 code
	 * @param niapRequestCode : 寃곗젣 Activity媛� �넻�떊�쓣 �쐞�븳 Request 肄붾뱶 
	 * @param payLoad : �긽�뭹 援щℓ媛� �셿猷뚮릺�뿀�쓣 寃쎌슦 泥섏쓬 寃곗젣�떆 �쟾�떖�븳 Payload瑜� 泥댄겕�븯�뿬 �젙�긽�쟻�씤 援щℓ �슂泥�嫄댁씤吏� 媛쒕컻�궗 validation�쓣 嫄곗튇 �썑
	 *           1. �냼紐⑥꽦 �긽�뭹�씤 寃쎌슦 諛붾줈 �냼吏꾩떆�궓�떎(consumeAsync API �궗�슜)
	 *           2. �쁺援ъ꽦 �긽�뭹�씤 寃쎌슦 �삙�깮�쓣 �쟻�슜�떆�궓�떎(鍮꾩쫰�땲�뒪 濡쒖쭅) 
	 * @param activity : activity
	 * @param niapHelper : niapHelper
	 */
	public static void requestPayment(String productCode, int niapRequestCode, String payLoad, final UnityPlayerActivity activity, NIAPHelper niapHelper) 
	{		
		niapHelper.requestPayment(activity, productCode, payLoad, niapRequestCode, new RequestPaymentListener() 
		{
			@Override
			public void onSuccess(final Purchase purchase) 
			{
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
				
				sendPurchaseSuccess(UnityPluginIAPConstant.InvokeMethod.REQUEST_PAYMENT, purchase);
					}});
			}
			
			@Override
			public void onFail(final NIAPHelperErrorType errorType) 
			{
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
				sendFailure(UnityPluginIAPConstant.InvokeMethod.REQUEST_PAYMENT, errorType);
					}});
			}
			
			@Override
			public void onCancel() 
			{
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
				sendCancel(UnityPluginIAPConstant.InvokeMethod.REQUEST_PAYMENT);
					}});
			}
		});
	}
	
	/**
	 * 援щℓ�븳 �긽�뭹�씠 �냼紐⑥꽦 �긽�뭹�씪 寃쎌슦 諛붾줈 �냼吏꾩떆�궓�떎.
	 * @param purchaseAsJsonText
	 * @param signature
	 */
	public static void consumeAsync(final String purchaseAsJsonText, final String signature, UnityPlayerActivity activity, final NIAPHelper niapHelper) {		
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					final Purchase purchase = Purchase.parseFromJson(purchaseAsJsonText, signature);
				
					niapHelper.consumeAsync(purchase, new ConsumeListener() {
						@Override
						public void onSuccess(Purchase purchase) {
							sendSuccess(UnityPluginIAPConstant.InvokeMethod.REQUEST_CONSUME, purchase.toString());					
						}
						
						@Override
						public void onFail(NIAPHelperErrorType errorType) {
							sendFailure(UnityPluginIAPConstant.InvokeMethod.REQUEST_CONSUME, errorType);	
						}
					});
				} catch (NIAPHelperException e) {
					Log.e(NIAP_LOG_TAG, purchaseAsJsonText, e);
				}
			}
		});
	}
	
	
	/**
	 * 援щℓ�궡�뿭 List 瑜� 媛��졇�삩�떎. 
	 */
	public static void requestPurchases(UnityPlayerActivity activity, final NIAPHelper niapHelper) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				niapHelper.getPurchasesAsync(new GetPurchasesListener() {
					@Override
					public void onSuccess(List<Purchase> purchases) {				
						JSONArray purchaseList = new JSONArray();
						if(purchases != null) {											
							for (Purchase purchase : purchases) {							
								purchaseList.put(converToJsonByPurchase(purchase));
							}					
						}
						sendSuccess(UnityPluginIAPConstant.InvokeMethod.GET_PURCHASES, purchaseList.toString());						
					}
					
					@Override
					public void onFail(NIAPHelperErrorType errorType) {
						sendFailure(UnityPluginIAPConstant.InvokeMethod.GET_PURCHASES, errorType);	
					}
				});
			}
		});
	}
	
	/**
	 * �듅�젙 援щℓ�궡�뿭 �젙蹂대�� 媛��졇�삩�떎.
	 * @param paymentSeq : 寃곗젣 踰덊샇
	 */
	public static void requestSinglePurchase(final String paymentSeq, UnityPlayerActivity activity, final NIAPHelper niapHelper) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				niapHelper.getSinglePurchaseAsync(paymentSeq, new GetSinglePurchaseListener() {
					@Override
					public void onSuccess(Purchase purchase) {
						
						sendSuccess(UnityPluginIAPConstant.InvokeMethod.GET_SINGLEPURCHASE, purchase.toString());
					}
					@Override
					public void onFail(NIAPHelperErrorType errorType) {
						sendFailure(UnityPluginIAPConstant.InvokeMethod.GET_SINGLEPURCHASE, errorType);		
					}
				});
			}
		});
	}


	private static JSONObject converToJsonByPurchase(Purchase purchase) {
		try {	
			JSONObject purchaseJson = new JSONObject();
			purchaseJson.put("paymentSeq", purchase.getPaymentSeq());
			purchaseJson.put("purchaseToken", purchase.getPurchaseToken());
			purchaseJson.put("purchaseType", purchase.getPurchaseType().toString());
			purchaseJson.put("environment", purchase.getEnvironment().toString());
			purchaseJson.put("packageName", purchase.getPackageName());
			purchaseJson.put("appName", purchase.getAppName());
			purchaseJson.put("productCode", purchase.getProductCode());
			purchaseJson.put("paymentTime", purchase.getPaymentTime());
			purchaseJson.put("developerPayload", purchase.getDeveloperPayload());
			purchaseJson.put("nonce", purchase.getNonce());
			purchaseJson.put("signature", purchase.getSignature());
			purchaseJson.put("originalPurchaseAsJsonText", purchase.getOriginalPurchaseAsJsonText());	
			
			return purchaseJson;
		} catch (JSONException e) {
			Log.e(NIAP_LOG_TAG, "converToJsonByPurchase JSONException", e);
			return null;
		}
	}
	
	private static ArrayList<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
		ArrayList<String> list = new ArrayList<String>();
		
		if (jsonArray == null) {
			return list;
		}
		
		for (int i = 0; i < jsonArray.length(); i++) {
			list.add(jsonArray.get(i).toString());
		}
		
		return list;
	}
}
