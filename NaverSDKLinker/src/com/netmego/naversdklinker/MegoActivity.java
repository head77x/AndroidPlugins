package com.netmego.naversdklinker;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.android.appstore.iap.InvalidProduct;
import com.naver.android.appstore.iap.NIAPHelper;
import com.naver.android.appstore.iap.NIAPHelper.ConsumeListener;
import com.naver.android.appstore.iap.NIAPHelper.GetProductDetailsListener;
import com.naver.android.appstore.iap.NIAPHelper.GetPurchasesListener;
import com.naver.android.appstore.iap.NIAPHelper.GetSinglePurchaseListener;
import com.naver.android.appstore.iap.NIAPHelper.OnInitializeFinishedListener;
import com.naver.android.appstore.iap.NIAPHelper.RequestPaymentListener;
import com.naver.android.appstore.iap.NIAPHelperErrorType;
import com.naver.android.appstore.iap.Product;
import com.naver.android.appstore.iap.Purchase;
import com.nhn.android.appstore.iap.v2.sample.R;

public class MegoActivity extends UnityPlayerActivity 
{
	// Tag for Logging
	protected static final String TAG = "IapOfficialSample/MainActivity";

	// Declare NaverInAppPurchaseHelper Class Instance
	private NIAPHelper niapHelper = null;

	// Verification Key that provides from Developer Center
	private static final String BASE64_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCNWUuwdj32WHZVImkoXRM27cDj1RmMkLAFIZ8U/JPfV0q5GwgAtsBnb5AZZB0ZT3aLM2HqnRNqDO4HFFiLtVAh6OwGIQ/evJ6C8caAa9ZdCKHn0u84f2Gxm/Zu+rAtw6Um8Wp2BOEJCn54EXBT1FnHu2L3w5QZB/JpQWxfFAZvuwIDAQAB";

	// Consumable Product Code
	private static final String PRODUCT_CODE_CONSUMABLE_JEWEL_50 = "1000013114";

	// Permanent Product Code
	private static final String PRODUCT_CODE_PERMANENT_GOLDEN_ANVIL = "1000013115";

	// Periodic Product Code
	private static final String PRODUCT_CODE_PERIODIC_SHIELD = "1000013116";

	// Product List for getProductDetails
	private static ArrayList<String> PRODUCT_CODE_LIST;

	// Request code for communication between the Payment Activity - Developers can freely modify
	private static final int NIAP_REQUEST_CODE = 100;

	// Declare UI Instance for business logic
	private ImageView reinforceImg;
	private ImageView buyImg;
	private ImageView upgradeImg;
	private ImageView borrowImg;
	private ImageView anvilImg;

	private TextView jewelCountTxt;
	private TextView attackRateTxt;
	private TextView percentTxt;
	private TextView itemListTxt;

	private ProgressDialog reinforceProgressDialog;

	// business Data
	private int mJewels;
	private int mCurrentAttackRate;
	private int mCurrentPercent;
	private boolean mHasGoldenAnvil = false;
	private final long mShieldDueTime = 60000L;
	private String mShieldPaymentSeq = null;
	
	private String userId = "user001";

	private Handler mHandler = new Handler();

	/*
	 * - Method for verification Purchase signature.
	 */
	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
	private static final String KEY_ALGORITHM = "RSA";

	private PublicKey generatePublicKey(String encodedPublicKeyString) throws Exception {
		byte[] publicKeyBytes = android.util.Base64.decode(encodedPublicKeyString, android.util.Base64.DEFAULT);
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);

		try {
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			return keyFactory.generatePublic(x509KeySpec);
		} catch (GeneralSecurityException e) {
			throw new Exception("Fail to create public RSA key.", e);
		}
	}
	
	private boolean isValidSignature(String signatureStr, String signedDataStr) {
		try {
			PublicKey publicKey = generatePublicKey(BASE64_PUBLIC_KEY);

			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initVerify(publicKey);
			signature.update(signedDataStr.getBytes());
			byte[] signatureBytes = Base64.decode(signatureStr, Base64.DEFAULT);

			boolean isVeryfied = signature.verify(signatureBytes);
			Log.d(TAG, "Signature Veryfied - " + isVeryfied);

			return isVeryfied;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return false;
		}
	}
	
	/*
	 * - Override onDestory method for release NIAPHelper instance. (Important!!)
	 */
	@Override
	public void onDestroy() 
	{
		super.onDestroy();

		// release NIAPHelper (IMPORTANT)
		if (niapHelper != null) {
			Log.d(TAG, "release helper");
			niapHelper.terminate();
			niapHelper = null;
		}

		PRODUCT_CODE_LIST = null;
	}

	/*
	 * - Override onRestart method for release NIAPHelper instance.
	 *   When user get back to activity after installs NAVER APPSTORE APP, initialize NIAPHelper instance. 
	 */
	@Override
	public void onRestart() 
	{
		Log.d(TAG, "onRestart Activity");
		super.onRestart();

		if (niapHelper == null) {
			// Create NIAP Helper Instance
			Log.d(TAG, "onRestart - Recreate helper");
			niapHelper = new NIAPHelper(this, BASE64_PUBLIC_KEY);
		}

		if (niapHelper.isInitialized() == false) {
			// Initialize NIAP Helper Instance
			Log.d(TAG, "onRestart - Initialize helper");
			niapHelper.initialize(onInitializeFinishedListener);
		}
	}

	/*
	 * - Method for error AlertDialog
	 */
	private void complain(String message) {
		String errorMessage = message + " error has occurred";
		Log.e(TAG, errorMessage);
		alert("Error: " + errorMessage);
	}

	private void complain(String message, NIAPHelperErrorType errorType) {
		String errorMessage = message + " error has occurred \ncode : " + errorType.getErrorCode() + ", details : " + errorType.getErrorDetails(); 
		Log.e(TAG, errorMessage);
		alert(errorMessage);
	}

	/*
	 * - Method for error AlertDialog
	 */
	private void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setMessage(message).setNeutralButton("OK", null).create().show();
	}
	
/*	
	private void loadData() {
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		mJewels = sharedPreferences.getInt("jewel", 20);
		mCurrentAttackRate = sharedPreferences.getInt("attackRate", 10);
		mCurrentPercent = sharedPreferences.getInt("percent", 60);
		mShieldPaymentSeq = sharedPreferences.getString("shieldPaymentSeq", null);
	}
*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	String _callback_gameObject;
	String _Init_callback;
	String _Before_purchased_callback;
	String _Product_details_callback;
	
	public void NaverSDK_Initialize_Step_1(String gameObject, String init_callback, 
			String before_purchased_callback, String product_details_callback )
	{
		_callback_gameObject = gameObject;
		_Init_callback = init_callback;
		_Before_purchased_callback = before_purchased_callback;
		_Product_details_callback = product_details_callback;
		
		
		
		PRODUCT_CODE_LIST = new ArrayList<String>();
	}
	
	public void NaverSDK_Initialize_Step_2_Add_productCodes(String product_code)
	{
		PRODUCT_CODE_LIST.add(product_code);
/*		
		// Add product code to list for getProductDetailsAsync
		PRODUCT_CODE_LIST.add(PRODUCT_CODE_CONSUMABLE_JEWEL_50);
		PRODUCT_CODE_LIST.add(PRODUCT_CODE_PERMANENT_GOLDEN_ANVIL);
		PRODUCT_CODE_LIST.add(PRODUCT_CODE_PERIODIC_SHIELD);
*/		
	}
	
	public void NaverSDK_Initialize_Step_3()
	{
		
		// loadData();

		// Create NIAP Helper Instance
		niapHelper = new NIAPHelper(this, BASE64_PUBLIC_KEY);

		// Initialize NIAP Helper Instance
		niapHelper.initialize(onInitializeFinishedListener);

		// initialize business logic
//		initButton();
//		updateUi();
//		saveData();
		
		
	}
	

	/*
	 * - NIAPHelper Initialize Callback Listener.
	 */
	OnInitializeFinishedListener onInitializeFinishedListener = new OnInitializeFinishedListener() 
	{
		@Override
		public void onSuccess() 
		{
			// If activity terminated during callback listener, quit it.
			if (niapHelper == null)
				return;

			UnityPlayer.UnitySendMessage(_Init_gameObject, _Init_function_name, result );
			
			
			// When NIAPHelper initialize finished, 
			// call getPurchasesAsync API for Process logics that depends on 
			// user's purchased product.
			// 이미 결제한 내역 얻어오기
			niapHelper.getPurchasesAsync(getPurchasesListener);


			// Call getProductDetailsAsync API if you want product's detail information.
			niapHelper.getProductDetailsAsync(PRODUCT_CODE_LIST, getProductDetailsListener);

			// you can call specific purchase information directly by getSinglePurchaseAsync API.
			// When you call getSinglePurchaseAsync, you need PaymentSeq which has been returned from payment API result.
			// In this sample app, It calls this API at once on initializing for convenient implementation,
			// but your app should call it on demand to check duration of periodic product.
			if (mShieldPaymentSeq != null) 
			{
				niapHelper.getSinglePurchaseAsync(mShieldPaymentSeq, getPurchaseListener);
			}

			Toast.makeText(MegoActivity.this, "NIAPHelper initialize finished", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "initialize finished.");
		}

		@Override
		public void onFail(NIAPHelperErrorType errorType) 
		{
			if (errorType == NIAPHelperErrorType.NEED_INSTALL_OR_UPDATE_APPSTORE) 
			{
				// If user does not install NAVER APPSTORE APP or has old version, induce user to install.
				niapHelper.updateOrInstallAppstore(MegoActivity.this);
			} 
			else 
			{
				// When other NIAPHelper initial error has been occurred.
				complain("NIAPHelper initialize failed", errorType);
			}
		}
	};

	/*
	 * - NIAPHelper getPurchasesAsync API Callback Listener.
	 * While first initializing App's data, you should access user's purchases list and
	 * 1. If user has not-consumed consumable product, consume it.
	 * 2. If user has permanent product, supply gifts that depends on business logic.(Do not consume it.)
	 */
	GetPurchasesListener getPurchasesListener = new GetPurchasesListener() 
	{
		@Override
		public void onSuccess(List<Purchase> purchases) 
		{
			if (niapHelper == null)
				return;

			for (Purchase purchase : purchases) 
			{
				// validate Payload.
				if (!verifyDeveloperPayload(userId, purchase)) 
				{
					complain("Error has occurred while getting purchased list. Payload verification failed.");
					return;
				}

				if (purchase.getProductCode().equals(PRODUCT_CODE_CONSUMABLE_JEWEL_50)) 
				{
					// If user has consumable product, consume it.
					niapHelper.consumeAsync(purchase, consumeListener);
				} 
				else 
				if (purchase.getProductCode().equals(PRODUCT_CODE_PERMANENT_GOLDEN_ANVIL)) 
				{
					// If user has permanent product, supply gifts that depends on business logic. Do not consume it.
					mHasGoldenAnvil = true;
					mCurrentPercent = 70;
				}
			}
//			updateUi();
		}

		@Override
		public void onFail(NIAPHelperErrorType errorType) 
		{
			if (errorType == NIAPHelperErrorType.SIGNATURE_VERIFICATION_ERROR) 
			{
				// If purchase signature verification has been failed, you should log it on your owned server for finding why this error has been occurred.
				complain("Purchase signature verification has been failed", errorType);
			} 
			else 
			if (errorType == NIAPHelperErrorType.USER_NOT_LOGGED_IN) 
			{
				// This case may be caused by because user account has been logged off in NAVER APPSTORE APP.
				complain("getPurchasesListener - Please login NAVER APPSTORE.", errorType);
			} 
			else 
			{
				complain("getPurchasesAsync failed", errorType);
			}
		}
	};
	
	
	
	/*
	 * - NIAPHelper getProductDetailsAsync API Callback Listener.
	 * You can load product's detail information from NAVER APPSTORE DEV CENTER for getting product's name or price.
	 */
	GetProductDetailsListener getProductDetailsListener = new GetProductDetailsListener() 
	{
		@Override
		public void onSuccess(List<Product> validProductList, List<InvalidProduct> inValidProductList) 
		{
			// When API call finished successfully, display product's name and price.
			for (Product validProduct : validProductList) 
			{
				itemListTxt.setText(itemListTxt.getText() + " " + validProduct.getProductName() + "(KRW" + validProduct.getSellPrice() + ") ");
			}
		}

		@Override
		public void onFail(NIAPHelperErrorType errorType) 
		{
			complain("error has occurred on getProductDetails.", errorType);
		}
	};

	/*
	 * - NIAPHelper getSinglePurchaseAsync API Callback Listener.
	 * getSinglePurchaseAsync API tend to be used by periodic products.
	 * In this listener, you can process business logic if time remains for periodic product
	 * or expired, It must be consumed so that the user can buy this product again.
	 * You have to check duration of periodic product by yourself. NIAP only provides consume API for expired one.
	 */
	GetSinglePurchaseListener getPurchaseListener = new GetSinglePurchaseListener() 
	{
		@Override
		public void onSuccess(Purchase purchase) 
		{
			// Verify Payload
			if (!verifyDeveloperPayload(userId, purchase)) {
				complain("Error has occurred while getting purchased list. Payload verification failed.");
				return;
			}

			// If purchase is periodic product,
			if (purchase.getProductCode().equals(PRODUCT_CODE_PERIODIC_SHIELD)) 
			{
				// Check the product's expiration period. In this sample app, It checks time locally for convenient implementation but, your app should check it on your owned server.
				if (purchase.getPaymentTime() + mShieldDueTime <= System.currentTimeMillis()) 
				{
					// If expired, consume product.
					niapHelper.consumeAsync(purchase, consumeListener);

					// Process your business logic.
					mShieldPaymentSeq = null;
					complain("Your Shield has been expired");
				} 
				else 
				{
					// If not expired yet, process only product's business logic.
					mShieldPaymentSeq = purchase.getPaymentSeq();
				}
			}
//			updateUi();
//			saveData();
		}

		@Override
		public void onFail(NIAPHelperErrorType errorType) 
		{
			if (errorType == NIAPHelperErrorType.SIGNATURE_VERIFICATION_ERROR) 
			{
				// If purchase signature verification has been failed, you should log it on your owned server for find why this error has been occurred.
				complain("Purchase signature verification has been failed", errorType);
			} 
			else 
			if (errorType == NIAPHelperErrorType.USER_NOT_LOGGED_IN) 
			{
				// This case may be caused by because user account has been logged off in NAVER APPSTORE APP.
				complain("getSinglePurchaseListener - Please login NAVER APPSTORE.", errorType);
			} 
			else 
			if (errorType == NIAPHelperErrorType.NON_EXISTENT_PURCHASE) 
			{
				// This case may be caused by because there is no purchase information for that paymentSeq.
				complain("There is no purchase informaion for that paymentSeq.", errorType);
			} 
			else 
			if (errorType == NIAPHelperErrorType.ALREADY_CONSUMED) 
			{
				// This case may be caused by because target purchase had been already consumed.
				complain("Target purchase had been already consumed.", errorType);
			} 
			else 
			{
				complain("getPurchasesAsync failed", errorType);
			}
		}
	};

	/*
	 * - Override onClick method for button click event.
	 */
	public void requestPayment( String ItemCode  )
	{
		niapHelper.requestPayment(MegoActivity.this, ItemCode, getPayLoad(userId), NIAP_REQUEST_CODE, requestPaymentListener);
	}
	
	
	
	/*
	 * - NIAPHelper consumeAsunc API Callback Listener.
	 * This Listener runs as callback request exhausted after consuming products.
	 * From here, you can supply gifts or provide items that depends on business logic.
	 */
	ConsumeListener consumeListener = new ConsumeListener() 
	{
		@Override
		public void onSuccess(Purchase purchase) {
			// When consumption is successfully complete, provide items.
			if (purchase.getProductCode().equals(PRODUCT_CODE_CONSUMABLE_JEWEL_50)) 
			{
				mJewels = mJewels + 50;
//				saveData();
//				updateUi();
			}
		}

		@Override
		public void onFail(NIAPHelperErrorType errorType) 
		{
			if (errorType == NIAPHelperErrorType.PRODUCT_NOT_OWNED) 
			{
				// Can not consume because user does not have product.
				complain("You don't have product.", errorType);

			} 
			else 
			{
				// If error occurred while consuming, logging it.
				complain("consumeAsync failed", errorType);
			}
		}
	};

	/*
	 * - NIAPHelper requestPayment API Callback Listener.
	 * This Listener runs as callback request exhausted after purchase products.
	 * From here, you have to validate Payload and
	 * 1. If purchased product is consumable product, consume it.
	 * 2. If purchased product is permanent product, supply gifts that depends on business logic.
	 */
	RequestPaymentListener requestPaymentListener = new RequestPaymentListener() 
	{
		@Override
		public void onSuccess(Purchase purchase) 
		{
			if (niapHelper == null)
				return;

			// Verify Payload.
			if (!verifyDeveloperPayload(userId, purchase)) 
			{
				complain("Error has occurred while purchasing. Payload verification failed.");
				return;
			}
			
			boolean isSignatureVerified = isValidSignature(purchase.getSignature(), purchase.getOriginalPurchaseAsJsonText());
			if (!isSignatureVerified) 
			{
				complain("Error has occurred while purchasing. Signature verification failed.");
				return;
			}

			if (purchase.getProductCode().equals(PRODUCT_CODE_CONSUMABLE_JEWEL_50)) 
			{
				// If purchased product is consumable product, consume it
				alert("Thank you for buying Jewels");
				niapHelper.consumeAsync(purchase, consumeListener);
			} 
			else 
			if (purchase.getProductCode().equals(PRODUCT_CODE_PERMANENT_GOLDEN_ANVIL)) 
			{
				// If purchased product is permanent product, supply gifts that depends on business logic. Do not consume it.
				alert("Thank you for buying Golden Anvil");
				mHasGoldenAnvil = true;
			} 
			else 
			if (purchase.getProductCode().equals(PRODUCT_CODE_PERIODIC_SHIELD)) 
			{
				alert("Thank you for lending Shield");
				mShieldPaymentSeq = purchase.getPaymentSeq();
			}
			
//			saveData();
//			updateUi();
		}

		@Override
		public void onFail(NIAPHelperErrorType errorType) 
		{
			if (errorType == NIAPHelperErrorType.SIGNATURE_VERIFICATION_ERROR) 
			{
				// If purchase signature verification has been failed, you should log it on your owned server for find why this error has been occurred.
				complain("Purchase signature verification has been failed", errorType);
			} else if (errorType == NIAPHelperErrorType.PURCHASE_PROCESS_NOT_WORKED) {
				// This case may be caused by because of NAVER APPSTORE APP's internal error. You should log it on your own server and, deliver it to NAVER APPSTORE to find problem.
				complain("There is some problem on NAVER APPSTORE.", errorType);
			} else if (errorType == NIAPHelperErrorType.NETWORK_ERROR) {
				// This case may be caused by because of network connection problem.
				complain("There is some network problem on purchasing. Please retry.", errorType);
			} else if (errorType == NIAPHelperErrorType.PRODUCT_ALREADY_OWNED) {
				// This case may be caused by because user already have same product.
				// If purchasing product was permanent product, this error worked correctly. but in case of consumable product, you have to consume user's already owned product for purchase for new one.
				// (In this case, there is some missing logic in your purchase code that is consuming consumable product after purchase.)
				complain("You already owned same product.", errorType);
			} else {
				// If other type of error occurred during purchase, log it.
				complain("requestPayment fail", errorType);
			}
		}

		@Override
		public void onCancel() {
			// If user cancels purchase
			Toast.makeText(MegoActivity.this, "Purchase has been canceled.", Toast.LENGTH_SHORT).show();
		}
	};



	/*
	 * - Override onActivityResult method for handling Purchase Activity's Intents
	 * (IMPORTANT - You have to override this method for receive other activity's intents)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
		if (niapHelper == null)
			return;

		if (!niapHelper.handleActivityResult(requestCode, resultCode, data)) {
			// You can handle result here that NIAPHelper does not handle.

			Log.d(TAG, "NIAP Helper does not handle onActivityResult");
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			// onActivityResult handled by NIAPHelper.
			Log.d(TAG, "NIAP Helper handles onActivityResult");
		}
	}

	/*
	 * - Method for getting Payload string. (You have to implement this method by yourself.)
	 * We recommend to create any random string from your own server for payload string.
	 * Payload should be different between each of the payment transactions.
	 */
	private String getPayLoad(String userId) {
		return userId + "PayLoad!@#$%";
	}

	/*
	 * - Payload validation method (You have to implement this method by yourself.)
	 */
	boolean verifyDeveloperPayload(String userId, Purchase purchase) {
		String payload = purchase.getDeveloperPayload();

		// Logic for Payload verification.
		if (getPayLoad(userId).equals(payload)) {
			return true;
		} else {
			return false;
		}
	}

	
	

	/*
	 * - Method for updating UI. (business logic)
	 */
/*	
	private void updateUi() {
		jewelCountTxt.setText(" : " + String.valueOf(mJewels));
		attackRateTxt.setText(" : " + String.valueOf(mCurrentAttackRate));
		percentTxt.setText(" : " + String.valueOf(mCurrentPercent) + "%");

		if (mHasGoldenAnvil) {
			anvilImg.setImageResource(R.drawable.anvil2);
			upgradeImg.setVisibility(ImageView.INVISIBLE);
		}

		if (mShieldPaymentSeq != null) {
			attackRateTxt.setText(attackRateTxt.getText() + " +(10)");
			borrowImg.setVisibility(ImageView.INVISIBLE);
		} else {
			borrowImg.setVisibility(ImageView.VISIBLE);
			attackRateTxt.setText(attackRateTxt.getText() + " +(0)");
		}
	}
*/

	/*
	 * - 寃뚯엫 �뜲�씠�꽣 ���옣 硫붿냼�뱶 (寃뚯엫 鍮꾩쫰�땲�뒪 濡쒖쭅�쑝濡� �씤�빋寃곗젣�� 臾닿��븿.)
	 */
	/*
	 * - Method for saving business data. (business logic)
	 */
/*	
	private void saveData() {
		SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
		spe.putInt("jewel", mJewels);
		spe.putInt("ar", mCurrentAttackRate);
		spe.putInt("percent", mCurrentPercent);
		spe.putString("shieldPaymentSeq", mShieldPaymentSeq);

		spe.commit();
	}
*/
	
	/*
	 * - Method for initializing UI buttons. (business logic)
	 */
/*	
	private void initButton() {
		reinforceImg = (ImageView) findViewById(R.id.reinforceImg);
		buyImg = (ImageView) findViewById(R.id.buyImg);
		upgradeImg = (ImageView) findViewById(R.id.upgradeImg);
		borrowImg = (ImageView) findViewById(R.id.borrowImg);

		reinforceImg.setOnClickListener(this);
		buyImg.setOnClickListener(this);
		upgradeImg.setOnClickListener(this);
		borrowImg.setOnClickListener(this);

		jewelCountTxt = (TextView) findViewById(R.id.jewelCount);
		attackRateTxt = (TextView) findViewById(R.id.ar);
		percentTxt = (TextView) findViewById(R.id.percent);
		itemListTxt = (TextView) findViewById(R.id.itemList);

		anvilImg = (ImageView) findViewById(R.id.anvilImg);
	}
*/
	
	
}
