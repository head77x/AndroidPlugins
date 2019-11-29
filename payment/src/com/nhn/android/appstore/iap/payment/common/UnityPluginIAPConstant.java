package com.nhn.android.appstore.iap.payment.common;

public class UnityPluginIAPConstant {
	public static final String TAG_IAP = "IAPUnityPlugin";
	public static final String UNITY_IAP_OBJECT_NAME 		= "NIAPNativeExtension";
	public static final String INVOKEMETHOD = "invokeMethod";
	
	public enum InvokeMethod {
		INITIAP("initIAP"),
		GET_PRODUCT_INFOS("getProductDetails"),
		REQUEST_PAYMENT("requestPayment"),
		REQUEST_CONSUME("requestConsume"),
		GET_PURCHASES("getPurchases"),
		GET_SINGLEPURCHASE("getSinglePurchase"),
		COMMON_ERROR("commonError");
		
		String name;
		
		InvokeMethod(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}

		/**
		 * find method name
		 * @param name
		 * @return
		 */
		public static InvokeMethod findBy(String name) {
			for (InvokeMethod invokeMethod : InvokeMethod.values()) {
				if (invokeMethod.name.equals(name)) {
					return invokeMethod;
				}
			}
			throw new IllegalArgumentException("unsupported method name : " + name);
		}
	}
	
	public class Param {		
		
		public static final String RESULT					= "result";
		public static final String CODE	 					= "code";		
		public static final String MESSAGE		 			= "message";
		public static final String PRODUCT_CODE				= "productCode";
		public static final String PRODUCT_CODES			= "productCodes";
		public static final String PAYMENT_REQUEST_CODE		= "niapRequestCode";
		public static final String PAYLOAD					= "payLoad";		// 媛쒕컻�궗�뿉�꽌 �엯�젰�븳 payLoad 媛�
		public static final String PUBLIC_KEY				= "publicKey"; 
		public static final String SIGNATURE				= "signature";
		public static final String PURCHASE_JSONTEXT		= "purchaseAsJsonText";
		public static final String PAYMENTSEQ				= "paymentSeq";
		public static final String ERROR					= "error";
	}
}