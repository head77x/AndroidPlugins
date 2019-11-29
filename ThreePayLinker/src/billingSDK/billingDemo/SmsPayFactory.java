package billingSDK.billingDemo;

import java.util.HashMap;

import mm.sms.purchasesdk.OnSMSPurchaseListener;
import mm.sms.purchasesdk.PurchaseCode;
import mm.sms.purchasesdk.SMSPurchase;

import com.unicom.dcLoader.Utils;
import com.unicom.dcLoader.Utils.UnipayPayResultListener;

import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface.GameExitCallback;
import cn.cmgame.billing.api.GameInterface.IPayCallback;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.content.Intent;


public class SmsPayFactory
{
	public interface SmsPurchaseListener {
		public void onPurchaseSucceed();
		public void onPurchaseCanceld();
		public void onPurchaseFailed(String msg);
		public void onPurchaseInfo(String msg);
	};
	
	public interface SmsExitGameListener {
		public void onExitGameCancelExit();
		public void onExitGameConfirmExit();
		public void onExitGameInGame();
	};
	
	public enum SmsMobileOperator {
		kMOBILE_OPERATOR_UNKNOWN,
		kMOBILE_OPERATOR_CMCC_GC,
		kMOBILE_OPERATOR_CMCC_MM,
		kMOBILE_OPERATOR_UNICOM,
		kMOBILE_OPERATOR_TELECOM_CTE,
		kMOBILE_OPERATOR_OTHER,
	};
	
	
	private static boolean _bIniting = false;
	private static boolean _bInited = false;
	private static Activity _context;
	
	private SmsPayBase _smsPayer;
	
	private static SmsPayFactory _singleton;
	
	private SmsPayFactory(String appName, String appId, String cpCode, String cpId, String channelID, String seccode, String Company, String Telephone, boolean otherpay, String MM_appid, String MM_appkey) 
	{
/*		
		String _appid;
		String _cpId;
		String _cpCode;
		String _key;
		String _companyName;
		String _telephone;
		String _appName;
		
		_appid = appId;
		_cpId = cpId;
		_cpCode = cpCode;
		_key = KeyCode;
		_companyName = Company;
		_telephone = Telephone;
		_appName = appName; 
*/
		// �ㅶ뼪SIM�→�掠욆퓧�ε븚
		switch (getMobileOperator()) {
		case kMOBILE_OPERATOR_OTHER:
		case kMOBILE_OPERATOR_UNKNOWN:
			_smsPayer = null;
			break;
		case kMOBILE_OPERATOR_CMCC_MM:
			_smsPayer = SmsPayCMCC_MM.initSingleton(_context, MM_appid, MM_appkey, new SmsPayListener(this, null));
			_bInited = true;
			break;
		case kMOBILE_OPERATOR_CMCC_GC:
			_smsPayer = SmsPayCMCC_GC.initSingleton(_context, new SmsPayListener(this, null));
			_bInited = true;
			break;
		case kMOBILE_OPERATOR_UNICOM:
			_smsPayer = SmsPayUnicom.initSingleton(_context, appName, appId, cpCode, cpId, Company, Telephone, otherpay, new SmsPayListener(this, null));
			_bInited = true;
			break;
		case kMOBILE_OPERATOR_TELECOM_CTE:
			_smsPayer = SmsPayTelecom_CTE.initSingleton(_context, channelID, seccode, appName, Company);
			_bInited = true;
			break;
		default:
			_smsPayer = null;
			break;
		}
	}
	
	public static void init(Activity context, String appName, String appId, String cpCode, String cpId, String channelID, String seccode, String Company, String Telephone, boolean otherpay, String MM_appID, String MM_appkey) {
		if (!_bIniting && !_bInited) 
		{
			_bIniting = true;
			_context = context;
			if (_singleton == null) 
			{
				_singleton = new SmsPayFactory(appName, appId, cpCode, cpId, channelID, seccode, Company, Telephone, otherpay, MM_appID, MM_appkey);
			}
			_bIniting = false;
		}
	}
	
	public Activity getContext() {
		return _context;
	}
	
	public synchronized static SmsPayFactory getInstance() 
	{
		return _singleton;
	}
	
	protected void initFinished() {
		_bInited = true;
	}
	
	public int getMobileOperatorType()
	{
		SmsMobileOperator whattype = getMobileOperator();
		
		if( whattype == SmsMobileOperator.kMOBILE_OPERATOR_CMCC_GC)
			return 0;
		else
			if( whattype == SmsMobileOperator.kMOBILE_OPERATOR_UNICOM)
				return 1;
			else
				if( whattype == SmsMobileOperator.kMOBILE_OPERATOR_TELECOM_CTE)
					return 2;
				else
					if( whattype == SmsMobileOperator.kMOBILE_OPERATOR_CMCC_MM)
						return 3;
		
		return 0;
	}
	
	private final static int opMISMATCHES	= 0x00000000;
	private final static int opCMCC_GC 		= 0x00000001;
	private final static int opUNICOM 		= 0x00000010;
	private final static int opTELECOM 		= 0x00000100;
	private final static int opCMCC_MM 		= 0x00001000;
	static public SmsMobileOperator getMobileOperator() 
	{
        TelephonyManager telManager = (TelephonyManager)_context.getSystemService(Context.TELEPHONY_SERVICE); 
        String operator = telManager.getSimOperator();
        
        Bundle metaData = null;
		try {
			metaData = _context.getPackageManager().getApplicationInfo(_context.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		int nSOValue = Integer.valueOf(metaData.getString("SOValue"));
		Log.e("SOValue: ", nSOValue + "");

		/* ?ζ뙁若싦틙?륲DK竊뚦닕凉뷴댍鵝욜뵪瑥쩢DK竊덁툗?뗦쑛SIM?↑퓧?ε븚?졾뀽竊?*/
		if (nSOValue == opCMCC_GC) {
			return SmsMobileOperator.kMOBILE_OPERATOR_CMCC_GC;
		} else if (nSOValue == opUNICOM) {
			return SmsMobileOperator.kMOBILE_OPERATOR_UNICOM;
		} else if (nSOValue == opTELECOM) {
			return SmsMobileOperator.kMOBILE_OPERATOR_TELECOM_CTE;
		} else if (nSOValue == opCMCC_MM) {
			return SmsMobileOperator.kMOBILE_OPERATOR_CMCC_MM;
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
            	
            	
            	return SmsMobileOperator.kMOBILE_OPERATOR_CMCC_GC;
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
            
    		System.out.println("Brandon : Cannot check !! :" + operator);
        }
        else
    		System.out.println("Brandon : operator null !!" );
        	

        
		return SmsMobileOperator.kMOBILE_OPERATOR_CMCC_GC;
    }
	
	public void pay(Context context, String smsPayItem, String paycode_3rd, String props, String Money, String AppID, SmsPayFactory.SmsPurchaseListener listener, boolean isRepeated) 
	{
		if (_smsPayer == null) 
		{
			System.out.println("Brandon : mei you singleton!!!! :");
			listener.onPurchaseFailed("mei you singleton!!!!");
		}
		if (!_bInited) 
		{
			System.out.println("Brandon : mei you init!!!! :");
		}
		if (_smsPayer != null && _bInited) 
		{
			
			if (getMobileOperator() != SmsMobileOperator.kMOBILE_OPERATOR_TELECOM_CTE) 
			{
			
				_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, AppID, new SmsPayListener(this, listener), isRepeated);
				
			} 
			else 
			{
				Intent intent = new Intent(context, Telecom_CTE_Activity.class);
				Telecom_CTE_Activity._smsPayItem = smsPayItem;
				Telecom_CTE_Activity._props = props;
				Telecom_CTE_Activity._money = Money;
				Telecom_CTE_Activity._appid = AppID;
				Telecom_CTE_Activity._smsPayer = _smsPayer;
				Telecom_CTE_Activity._factory = this;
				Telecom_CTE_Activity._listener = listener;
				Telecom_CTE_Activity._paylistener = new SmsPayListener(this, listener);
				context.startActivity(intent);
			}
			
		}
	}
	
	public void exitGame(Context context, SmsExitGameListener listener) {
		if (_smsPayer != null) {
			System.out.println("Brandon : ask exit!!!! :");
			
			_smsPayer.exitGame(context, new SmsExitListener(listener));
		} else {
			listener.onExitGameConfirmExit();
		}
	}
	
	public void viewMoreGames(Context context) {
		if (_smsPayer != null) {
			_smsPayer.viewMoreGames(context);
		}
	}
	
	public boolean isMusicEnabled() 
	{
		if (_smsPayer == null) {
			return true;
		}
		return _smsPayer.isMusicEnabled();
	}
	
	public class SmsPayListener implements 	IPayCallback, 			/* CMCC_GC callback */
											UnipayPayResultListener,	/* Unicom callback */
											OnSMSPurchaseListener /* CMM */
											{

//		private SmsPayFactory _factory;
		private SmsPurchaseListener _listener;
		
		SmsPayListener(SmsPayFactory factory, SmsPurchaseListener listener) {
//			_factory = factory;
			_listener= listener;
		}

		public void setSmsPurchaseListener(SmsPurchaseListener listener) {
			_listener = listener;
		}

		/**
		* CMCC_GC Listener
		*/
		@Override
		public void onResult(int resultCode, String billingIndex, Object arg) {
			String result = "";
	        switch (resultCode) {
	          case BillingResult.SUCCESS:
	            result = "兀�물�볟끁竊�" + billingIndex + "] �먨뒣竊�";
	            _listener.onPurchaseSucceed();
	            break;
	          case BillingResult.FAILED:
	            result = "兀�물�볟끁竊�" + billingIndex + "] 鸚김뇰竊�";
	            _listener.onPurchaseFailed(result);
	            break;
	          default:
	            result = "兀�물�볟끁竊�" + billingIndex + "] �뽪텋竊�";
	            _listener.onPurchaseCanceld();
	            break;
	        }
	        Log.e("======燁삣뒯GC SDK=======", result);
		}
		
		/**
		* CMCC_MM Listener
		*/
		@Override
		public void onInitFinish(int code) 
		{
			String result = "?앭쭓?뽫퍜?쒙폏" + SMSPurchase.getReason(code);
			Log.d("Brandon", "Init finish, status code = " + code + ":" + result);
//			_factory.initFinished();
		}
		
		@Override
		public void onBillingFinish(int code, HashMap arg1) 
		{
			String result = "MM result :";
			String paycode = null;
			String tradeID = null;
			if (code == PurchaseCode.ORDER_OK) 
			{
				// || code == PurchaseCode.ORDER_OK_TIMEOUT) {
			// ?녶뱚兀?물?먨뒣?뽬끻럴瀯뤺눌阿겹?閭ㅶ뿶鴉싪퓭?욃븚?곭쉪paycode,tradeID
				if (arg1 != null) {
					paycode = (String)arg1.get(OnSMSPurchaseListener.PAYCODE); 
					if (paycode != null && paycode.trim().length() != 0) {
						result = result + ",Paycode:" + paycode;
					}
					tradeID = (String)arg1.get(OnSMSPurchaseListener.TRADEID); 
					if (tradeID != null && tradeID.trim().length() != 0) {
			            result = result + ",tradeid:" + tradeID;
					}
					if (_listener != null) {
						_listener.onPurchaseSucceed();
						_listener.onPurchaseInfo(result);
					}
				}
			} else { 
				// 烏①ㅊ溫?눌鸚김뇰
				result = "溫?눌瀯볠옖:" + SMSPurchase.getReason(code);
				if (_listener != null) {
					_listener.onPurchaseFailed(result);
				}
			}
			Log.e("======燁삣뒯MM SDK=======", "pay end:  "+result);
		}

		/**
		 * SmsPayUnicom Listener
		 */
		@Override
		public void PayResult(String paycode, int flag, String desc) 
		{
			// ��빰��퍡�먨뒣
			if(flag == Utils.SUCCESS_SMS ){
				if (_listener != null) {
					Log.e("======�붼� SDK=======", "SUCCESS_SMS: " + desc);
					_listener.onPurchaseSucceed();
				}
			}
			
			// SDK鵝욜뵪寧т툒�방뵱餓섋퓭�욄닇��			
			if(flag == Utils.SUCCESS_3RDPAY ) {
				if (_listener != null) {
					Log.e("======�붼� SDK=======", "SUCCESS_3RDPAY: " + desc);
					_listener.onPurchaseSucceed();
				}
			}

			// ��퍡鸚김뇰
			if (flag == Utils.FAILED) {
				if (_listener != null) {
					Log.e("======�붼� SDK=======", "FAILED: " + desc);
					_listener.onPurchaseFailed(desc);
				}
			}
			
			// ��퍡�뽪텋
			if (flag == Utils.CANCEL) {
				if (_listener != null) {
					Log.e("======�붼� SDK=======", "CANCEL: " + desc);
					_listener.onPurchaseCanceld();
				}
			}

			// �욆걫�싩К訝됪뼶��퍡
			if (flag == Utils.OTHERPAY) {
				if (_listener != null) {
					Log.e("======�붼� SDK=======", "OTHERPAY: " + desc);
				}
			}
		}
	}
	
	public class SmsExitListener implements GameExitCallback {
		
		private SmsExitGameListener _listener;
		SmsExitListener(SmsExitGameListener listener) {
			_listener= listener;
		}
		
		// 凉뷴댍��눣
		public void forceExitingGame() {
			_listener.onExitGameInGame();
		}
		
		@Override
		public void onCancelExit() {
			_listener.onExitGameCancelExit();
		}

		@Override
		public void onConfirmExit() {
			_listener.onExitGameConfirmExit();
		}
	}
}

