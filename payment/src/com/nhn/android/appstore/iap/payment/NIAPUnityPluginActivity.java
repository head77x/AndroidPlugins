/*
 * @(#)NIAPUnityPluginActivity.java $version 2013. 4. 17.
 *
 * Copyright 2013 NHN Corp. All rights Reserved.
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.nhn.android.appstore.iap.payment;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.naver.android.appstore.iap.NIAPHelper;
import com.naver.android.appstore.iap.NIAPHelper.OnInitializeFinishedListener;
import com.naver.android.appstore.iap.NIAPHelperErrorType;
import com.nhn.android.appstore.iap.payment.common.UnityPluginIAPConstant;
import com.unity3d.player.UnityPlayerActivity;

/**
 * NIAP SDK Java Wrapper �씪�씠釉뚮윭由�
 */
public class NIAPUnityPluginActivity extends UnityPlayerActivity 
{
	private Handler postHandler = new Handler(); 
	
	private NIAPHelper niapHelper;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (niapHelper != null) {
			Log.d("DEBUG", "release helper");
			niapHelper.terminate();
			niapHelper = null;
		}
	}

	/*
	 * - 寃곗젣 �븸�떚鍮꾪떚�뿉�꽌 �꽆�뼱�삩 �씤�뀗�듃瑜� 泥섎━�븯�뒗 硫붿냼�뱶 �삤踰꾨씪�씠�뵫(�븘�닔) - Override onActivityResult
	 * method for handling Purchase Activity's Intents (IMPORTANT - You have to
	 * override this method for receive other activity's intents)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// �븘�옒 肄붾뱶�뒗 諛섎뱶�떆 �룷�븿�릺�뼱�빞 �빀�땲�떎.
		if (!niapHelper.handleActivityResult(requestCode, resultCode, data)) {
			// NIAPHelper媛� 援щℓ 寃곌낵瑜� 泥섎━�븯吏� �븡�쓬.
			Log.d(NIAPUnityPluginUtil.NIAP_LOG_TAG, "NIAPHelper does not handle onActivityResult");
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			// NIAPHelper媛� 援щℓ 寃곌낵瑜� 泥섎━ �셿猷�.
			Log.d(NIAPUnityPluginUtil.NIAP_LOG_TAG, "NIAP Helper handles onActivityResult");
		}
	}

	/**
	 * niapHelper瑜� 珥덇린�솕 �븳�떎.
	 * 
	 * @param publicKey
	 */
	public void initialize(String publicKey) {
		niapHelper = new NIAPHelper(NIAPUnityPluginActivity.this, publicKey);
		niapHelper.initialize(new OnInitializeFinishedListener() {
			@Override
			public void onSuccess() {
				Log.i(NIAPUnityPluginUtil.NIAP_LOG_TAG, "niapHelper initialize  Success");
			}

			@Override
			public void onFail(NIAPHelperErrorType errorType) {
				if (errorType == NIAPHelperErrorType.NEED_INSTALL_OR_UPDATE_APPSTORE) 
				{
					postHandler.post( new Runnable()
					{
						public void run()
						{
							niapHelper.updateOrInstallAppstore(NIAPUnityPluginActivity.this);
						}
					});
					
				}
			}
		});
	}

	/**
	 * Unity�뿉�꽌 �샇異쒗븯�뒗 �씤�빋寃곗젣 硫붿꽌�뱶
	 * 
	 * @param requestParam
	 */
	public void callNIAPNativeExtension(String jsonRequestParam) {
		JSONObject requestParam = null;
		try {
			requestParam = new JSONObject(jsonRequestParam);
			String invokeMethod = requestParam.getString(UnityPluginIAPConstant.INVOKEMETHOD);

			if (UnityPluginIAPConstant.InvokeMethod.findBy(invokeMethod) == UnityPluginIAPConstant.InvokeMethod.INITIAP) {
				String publicKey = requestParam.getString(UnityPluginIAPConstant.Param.PUBLIC_KEY);
				initialize(publicKey);
			} else {
				NIAPUnityPluginUtil.runIAPRequestedMethod(jsonRequestParam, this, niapHelper);
			}
		} catch (JSONException e) {
			Log.e(UnityPluginIAPConstant.TAG_IAP, "IAP unknown error : " + e);
		}
	}

	/**
	 * �씪諛� �뀓�뒪�듃 硫붿떆吏� 異쒕젰
	 * 
	 * @param message
	 *            : 異쒕젰�븷 硫붿떆吏�
	 */
	@SuppressLint("NewApi") public void showMessage(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), message,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

}
